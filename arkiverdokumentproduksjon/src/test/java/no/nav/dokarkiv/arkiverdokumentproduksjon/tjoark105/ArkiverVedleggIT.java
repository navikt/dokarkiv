package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for the arkiverVedlegg operation
 * in the ArkiverDokumentProduksjon webservices.
 *
 * @author Roar Bjurstrom - (Visma Consulting)
 */
//FIXME
public class ArkiverVedleggIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String ENDRET_AV_NAVN = "Tester2";
	private static final String TITTEL = "Tittel";
	private static final String BREVKODE = "Kode";
	private static final byte[] FILE_CONTENT = "a".getBytes();
	private static final String DOKUMENT_TYPE_ID = "DokumentTypeId";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldArkiverVedlegg() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.D);
		joarkRepository.save(journalpost);
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));

		ArkiverVedleggResponse arkiverVedleggResponse = arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);

		assertThat(arkiverVedleggResponse.getJournalpostId(), is(journalpost.getId()));
		assertThat(arkiverVedleggResponse.getDokumentInfoId(), notNullValue());
	}

	@Test
	public void shouldSaveDokumentInfo() throws Exception {
		DateProvider.configure(true, "2018-06-20T14:31:54.767");

		Journalpost journalpost = createJournalpost(JournalStatusCode.D);
		joarkRepository.save(journalpost);
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));
		arkiverVedleggRequest.setFerdigstillDokument(true);

		ArkiverVedleggResponse arkiverVedleggResponse = arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);

		Journalpost persistedJournalpost = joarkRepository.findById(arkiverVedleggResponse.getJournalpostId()).get();
		DokumentInfo dokumentInfo = persistedJournalpost.findDokumentInfoById(arkiverVedleggResponse.getDokumentInfoId());

		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		assertThat(dokumentInfo.getDokumentFerdigDato(), is(DateProvider.getToday()));
		assertThat(dokumentInfo.getKategori(), is(DokumentKategoriCode.B));
		assertThat(dokumentInfo.getTittel(), is(TITTEL));
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(dokumentInfo.getSensitivt(), is(true));
		assertThat(dokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(dokumentInfo.getFildetaljerListe().size(), is(1));

		FilDetaljer filDetaljer = dokumentInfo.getFildetaljerListe().iterator().next();
		assertThat(filDetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljer.getFiltype(), is(FilTypeCode.AXML));
		assertThat(filDetaljer.getFilstorrelse(), is(String.valueOf(FILE_CONTENT.length)));

		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());
		assertThat(dokumentFil.getFil(), is(equalTo(FILE_CONTENT)));

		DateProvider.configure(false, null);
	}

	@Test
	public void shouldUpdateJournalpostDokumentInforRelasjon() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.D);
		joarkRepository.save(journalpost);

		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));

		ArkiverVedleggResponse arkiverVedleggResponse = arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);

		Journalpost journalpostFromDb = joarkRepository.findById(arkiverVedleggResponse.getJournalpostId()).get();
		assertThat(journalpostFromDb.getJournalpostDokumentInfoRelasjoner().size(), is(1));

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = journalpostFromDb.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next();
		assertThat(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.VEDLEGG));
		assertThat(journalpostDokumentInfoRelasjon.getTilknyttetAvNavn(), is(ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostNotExist() throws Exception {
		Long journalpostId = 1L;
		thrown.expect(ArkiverVedleggJournalpostIkkeFunnet.class);
		thrown.expectMessage("journalpostId=" + journalpostId + " does not exist");

		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpostId));

		arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);
	}

	@Test
	public void shouldThrowExceptionIfIkkeUnderArbeid() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.A);
		joarkRepository.save(journalpost);
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));

		thrown.expect(ArkiverVedleggJournalpostIkkeUnderArbeid.class);
		thrown.expectMessage("Journalpost with id: " + journalpost.getJournalpostId() + " can not be updated");

		arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);
	}

	private no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Journalpost buildJournalpostForWsRequest(Long journalpostId) {
		return new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Journalpost()
				.withJournalpostId(journalpostId.toString())
				.withEndretAvNavn(ENDRET_AV_NAVN)
				.withDokumentInfo(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.DokumentInfo()
						.withDokumentTypeId(DOKUMENT_TYPE_ID)
						.withKategori(DokumentKategoriCode.B.toString())
						.withTittel(TITTEL)
						.withBrevkode(BREVKODE)
						.withSensitivt(true)
						.withFildetaljer(new Fildetaljer()
								.withVariantformat(VariantFormatCode.ARKIV.toString())
								.withFiltype(FilTypeCode.AXML.toString())
								.withIkkeRedigerbartDokument(FILE_CONTENT)
						));
	}

	private Journalpost createJournalpost(JournalStatusCode journalStatusCode) {
		return getJournalpostBuilder()
				.journalStatus(journalStatusCode)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}

}
