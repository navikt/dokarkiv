package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;


import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FeilStrukturException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.oppdaterjournalpostarkiverdokument.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import java.util.List;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the arkiverDokumentOgFerdigstillJournalpost operation
 * in the ArkiverDokumentproduksjon webservice.
 *
 * @author Torgeir Cook
 */
public class OppdaterJournalpostArkiverDokumentIT extends AbstractArkiverdokumentproduksjonItest {

	private static final Long METAFORCE_INSTANCE_ID = 2738L;
	private static final FilTypeCode FILTYPE = FilTypeCode.PDF;
	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Tester";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Tester2";
	private static final byte[] DOKUMENTINNHOLD = "DOKUMENT".getBytes();
	private static final String OPPRETTETKILDENAVN = "NAV";
	private static final String UTSENDINGS_KANAL_CODE = UtsendingsKanalCode.NAV_NO.name();
	private OppdaterJournalpostArkiverDokumentRequest request;
	private Journalpost journalpost;

	@BeforeEach
	public void setUp() throws Exception {
		journalpost = buildAndPersistJournalpost();
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.findAllDokumentInfos().get(0).getDokumentInfoId();
		request = createWsRequest(journalpostId, dokumentInfoId);
	}

	@Test
	public void shouldArkivereOgFerdigstilleJournalpost() throws Exception {
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
		Journalpost journalpostById = journalpostTestRepository.findById(journalpost.getJournalpostId()).get();
		Long dokumentInfoId = journalpost.findDokumentInfoById(request.getDokumentInfoId()).getDokumentInfoId();

		assertThat(journalpostById.getJournalpostId(), is(request.getJournalpostId()));
		assertThat(dokumentInfoId, is(request.getDokumentInfoId()));
	}

	@Test
	public void shouldVerifyJournalpostFields() throws Exception {
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
		Journalpost journalpostById = journalpostTestRepository.findById(journalpost.getJournalpostId()).get();

		assertThat(journalpostById.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(journalpostById.getUtsendingskanal().name(), is(UTSENDINGS_KANAL_CODE));
		assertThat(journalpostById.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(journalpostById.getSaksrelasjon().getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertNotNull(journalpostById.getEndretKildeNavn());
	}

	@Test
	public void shouldVerifyJournalpostFieldsLokalprint() throws Exception {
		request.setUtsendingskanal(UtsendingsKanalCode.L.toString());

		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
		Journalpost journalpostById = journalpostTestRepository.findById(journalpost.getJournalpostId()).get();

		assertThat(journalpostById.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(journalpostById.getUtsendingskanal(), is(UtsendingsKanalCode.L));
	}

	@Test
	public void shouldVerifyDokumentInfoAndFilDetaljer() throws Exception {
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
		DokumentInfo dokumentInfoById = journalpost.findDokumentInfoById(request.getDokumentInfoId());

		assertThat(dokumentInfoById.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		assertThat(dokumentInfoById.getEndretAvNavn(), is(ENDRET_AV_NAVN));
	}

	@Test
	public void shouldVerifyFilDetaljerAddedToJournalpost() throws Exception {
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		FilDetaljer filDetalj = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);

		assertThat(dokumentInfo.getFildetaljerListe().size(), is(2));
		assertThat(filDetalj.getFilstorrelse(), is(String.valueOf(DOKUMENTINNHOLD.length)));
		assertThat(filDetalj.getFiltype(), is(FILTYPE));
	}

	@Test
	public void shouldVerifyFilDetaljWithProduksjonFormatIsUpdated() throws Exception {
		List<Fildetaljer> fildetaljerListe = request.getFildetaljerListe();
		fildetaljerListe.add(createWsFilDetaljer(VariantFormatCode.PRODUKSJON));
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);

		DokumentInfo dokumentInfoById = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		FilDetaljer filDetalj = dokumentInfoById.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON);

		assertThat(dokumentInfoById.getFildetaljerListe().size(), is(2));
		assertThat(filDetalj.getFiltype(), is(FILTYPE));
		assertThat(filDetalj.getFilstorrelse(), is(Integer.toString(DOKUMENTINNHOLD.length)));
		assertNull(filDetalj.getMetaforceInstanceId());
	}

	@Test
	public void shouldThrowExceptionIfDuplicateVariantFormatsInRequest() throws Exception {
		request.getFildetaljerListe().add(createWsFilDetaljer(VariantFormatCode.SLADDET));
		request.getFildetaljerListe().add(createWsFilDetaljer(VariantFormatCode.SLADDET));

		assertThrows(FeilStrukturException.class,
				() -> arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request),
				"Input til tjenesten inneholder flere fildetaljer med samme variantformat");
	}

	private Journalpost buildAndPersistJournalpost() {
		Journalpost journalpost = getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.PEN)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
								.fagsystem(FagsystemCode.PEN)
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.dokumentInfo(createDokumentInfo())
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfo())
								.build())

				.build();

		journalpostTestRepository.persist(journalpost);
		return journalpost;
	}

	private DokumentInfo createDokumentInfo() {
		return getDokumentInfoBuilder()
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
				.endretAvNavn(ENDRET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTETKILDENAVN)
				.filDetaljerList(getFilDetaljerBuilder()
						.filtype(FilTypeCode.AXML)
						.fileContent("test".getBytes())
						.metaforceInstanceId(METAFORCE_INSTANCE_ID)
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.variantFormat(VariantFormatCode.PRODUKSJON)
						.build())
				.build();

	}

	private OppdaterJournalpostArkiverDokumentRequest createWsRequest(Long journalpostId, Long dokumentInfoId) throws Exception {
		OppdaterJournalpostArkiverDokumentRequest request =
				new OppdaterJournalpostArkiverDokumentRequest();
		request.setFerdigstillJournalpost(true);
		request.setJournalpostId(journalpostId);
		request.setDokumentInfoId(dokumentInfoId);
		request.setEndretAvNavn(ENDRET_AV_NAVN);
		request.setUtsendingskanal(UTSENDINGS_KANAL_CODE);
		request.getFildetaljerListe().add(createWsFilDetaljer(VariantFormatCode.ARKIV));
		request.setDatoDokument(DatatypeFactory.newInstance().newXMLGregorianCalendar("2015-01-01"));
		return request;
	}

	private Fildetaljer createWsFilDetaljer(VariantFormatCode code) {
		Fildetaljer filDetaljer = new Fildetaljer();
		filDetaljer.setVariantformat(code.name());
		filDetaljer.setRedigerbartDokument(DOKUMENTINNHOLD);
		filDetaljer.setFiltype(FILTYPE.name());
		return filDetaljer;
	}
}
