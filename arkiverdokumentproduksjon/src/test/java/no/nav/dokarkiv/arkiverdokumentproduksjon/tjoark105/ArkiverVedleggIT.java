package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
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
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the arkiverVedlegg operation
 * in the ArkiverDokumentProduksjon webservices.
 *
 * @author Roar Bjurstrom - (Visma Consulting)
 */
public class ArkiverVedleggIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String ENDRET_AV_NAVN = "Tester2";
	private static final String TITTEL = "Tittel";
	private static final String BREVKODE = "Kode";
	private static final byte[] FILE_CONTENT = "a".getBytes();
	private static final String DOKUMENT_TYPE_ID = "DokumentTypeId";

	@Test
	public void shouldArkiverVedlegg() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.D);
		journalpostTestRepository.persist(journalpost);
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
		journalpostTestRepository.persist(journalpost);
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));
		arkiverVedleggRequest.setFerdigstillDokument(true);

		ArkiverVedleggResponse arkiverVedleggResponse = arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);

		Journalpost persistedJournalpost = journalpostTestRepository.findById(arkiverVedleggResponse.getJournalpostId()).get();
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

		DokumentFil dokumentFil = dokumentFilTestRepository.findByFilUuid(filDetaljer.getFilUuid());
		assertThat(dokumentFil.getFil(), is(equalTo(FILE_CONTENT)));

		DateProvider.configure(false, null);
	}

	@Test
	public void shouldUpdateJournalpostDokumentInforRelasjon() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.D);
		journalpostTestRepository.persist(journalpost);

		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));

		ArkiverVedleggResponse arkiverVedleggResponse = arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);

		Journalpost journalpostFromDb = journalpostTestRepository.findById(arkiverVedleggResponse.getJournalpostId()).get();
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

		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpostId));

		assertThrows(ArkiverVedleggJournalpostIkkeFunnet.class,
				() -> arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest),
				"journalpostId=" + journalpostId + " does not exist");
	}

	@Test
	public void shouldThrowExceptionIfIkkeUnderArbeid() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.A);
		journalpostTestRepository.persist(journalpost);
		ArkiverVedleggRequest arkiverVedleggRequest = new ArkiverVedleggRequest();
		arkiverVedleggRequest.setJournalpost(buildJournalpostForWsRequest(journalpost.getId()));

		assertThrows(ArkiverVedleggJournalpostIkkeUnderArbeid.class,
				() -> arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest),
				"Journalpost with id: " + journalpost.getJournalpostId() + " can not be updated");
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
				.fagomrade(FagomradeCode.FOR)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}

}
