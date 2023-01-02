package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;


import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostInneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FjernFerdigstiltDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the FerdigstillJournalpost
 *
 * @author Stig Strøm
 */
public class FerdigstillJournalpostIT extends AbstractArkiverdokumentproduksjonItest {
	private static final DokumentStatusCode UNDER_REDIGERING = DokumentStatusCode.UNDER_REDIGERING;
	private static final DokumentStatusCode FERDIGSTILT = DokumentStatusCode.FERDIGSTILT;
	private static final UtsendingsKanalCode UTSENDINGKANAL = UtsendingsKanalCode.EESSI;
	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Tester";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Tester2";

	@BeforeEach
	public void setUp() throws Exception {
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldFerdigstillJournalpost() throws Exception {
		Journalpost ferdigstiltJournalpost = buildAndPersistJournalpost(FERDIGSTILT);

		arkiverDokumentproduksjonProvider.ferdigstillJournalpost(createRequest(ferdigstiltJournalpost));

		Journalpost resultJournalpost = journalpostRepositorySkjermet.findById(ferdigstiltJournalpost.getJournalpostId()).get();
		assertThat(resultJournalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(resultJournalpost.getJournalDato(), is(notNullValue()));
		assertThat(resultJournalpost.getUtsendingskanal(), is(UTSENDINGKANAL));
		assertThat(resultJournalpost.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(resultJournalpost.getJournalfortAvNavn(), is(ENDRET_AV_NAVN));

		DokumentInfo dokumentInfo = resultJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertThat(dokumentInfo.getDokumentstatus(), is(FERDIGSTILT));
		assertThat(dokumentInfo.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON).getMetaforceInstanceId(),
				is(nullValue()));
	}

	@Test
	public void shouldFerdigstillJournalpostLokalPrint() throws Exception {
		Journalpost ferdigstiltJournalpost = buildAndPersistJournalpost(FERDIGSTILT);

		FerdigstillJournalpostRequest request = createRequest(ferdigstiltJournalpost);
		request.setUtsendingskanal(UtsendingsKanalCode.L.toString());
		arkiverDokumentproduksjonProvider.ferdigstillJournalpost(request);

		Journalpost resultJournalpost = journalpostRepositorySkjermet.findById(ferdigstiltJournalpost.getJournalpostId()).get();
		assertThat(resultJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(resultJournalpost.getUtsendingskanal(), is(UtsendingsKanalCode.L));
	}

	@Test
	public void shouldFerdigstillJournalpostIngenDistribusjon() throws Exception {
		Journalpost ferdigstiltJournalpost = buildAndPersistJournalpost(FERDIGSTILT);

		FerdigstillJournalpostRequest request = createRequest(ferdigstiltJournalpost);
		request.setUtsendingskanal(UtsendingsKanalCode.INGEN_DISTRIBUSJON.toString());
		arkiverDokumentproduksjonProvider.ferdigstillJournalpost(request);

		Journalpost resultJournalpost = journalpostRepositorySkjermet.findById(ferdigstiltJournalpost.getJournalpostId()).get();
		assertThat(resultJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
		assertThat(resultJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(resultJournalpost.getUtsendingskanal(), is(UtsendingsKanalCode.INGEN_DISTRIBUSJON));
	}

	@Test
	public void shouldThrowException_missingInput() {
		assertThrows(IllegalArgumentException.class,
				() -> arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldFerdigstillJournalpost2() throws Exception {
		Journalpost ferdigstiltJournalpost = buildAndPersistJournalpost(UNDER_REDIGERING);

		assertThrows(FerdigstillJournalpostInneholderDokumenterUnderRedigering.class,
				() -> arkiverDokumentproduksjonProvider.ferdigstillJournalpost(createRequest(ferdigstiltJournalpost)));
	}


	private FerdigstillJournalpostRequest createRequest(Journalpost journalpost) {
		FerdigstillJournalpostRequest request = new FerdigstillJournalpostRequest();
		request.setJournalpostId(journalpost.getJournalpostId());
		request.setUtsendingskanal(UTSENDINGKANAL.name());
		request.setEndretAvNavn(ENDRET_AV_NAVN);
		return request;
	}

	private Journalpost buildAndPersistJournalpost(DokumentStatusCode dokumentStatusCode) {
		Journalpost journalpost = getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.dokumentDato(new Date())
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.PEN)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfo(dokumentStatusCode))
								.build())

				.build();

		journalpostRepositorySkjermet.save(journalpost);
		return journalpost;
	}

	private DokumentInfo createDokumentInfo(DokumentStatusCode dokumentStatusCode) {
		return getDokumentInfoBuilder()
				.dokumentstatus(dokumentStatusCode)
				.dokumentFerdigDato(new Date())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.filDetaljerList(createPDFFilDetaljerArkiv(), createPDFFilDetaljerProduksjon())
				.build();
	}

	private FilDetaljer createPDFFilDetaljerArkiv() {
		return getFilDetaljerBuilder()
				.fileContent("file".getBytes())
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}

	private FilDetaljer createPDFFilDetaljerProduksjon() {
		return getFilDetaljerBuilder()
				.fileContent("file".getBytes())
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.metaforceInstanceId(123L)
				.build();
	}

}
