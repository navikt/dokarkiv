package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeRedigerbart;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FjernFerdigstiltDokumentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the FjernFerdigstiltDokument
 *
 * @author Stig Strøm
 */
public class FjernFerdigstiltDokumentIT extends AbstractArkiverdokumentproduksjonItest {
	private static final DokumentStatusCode UNDER_REDIGERING = DokumentStatusCode.UNDER_REDIGERING;
	private static final DokumentStatusCode FERDIGSTILT = DokumentStatusCode.FERDIGSTILT;
	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Tester";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Tester2";

	@BeforeEach
	public void setUp() throws Exception {
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldVerfiyFjernFerdigstiltDokument() throws Exception {
		Journalpost ferdigstiltJournalpost = buildAndPersistJournalpost(FERDIGSTILT);

		arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(createRequest(ferdigstiltJournalpost));

		Journalpost resultJournalpost = joarkRepository.findById(ferdigstiltJournalpost.getJournalpostId()).get();
		assertThat(resultJournalpost.getDokumentDato(), is(nullValue()));
		assertThat(resultJournalpost.getEndretAvNavn(), is(ENDRET_AV_NAVN));

		DokumentInfo dokumentInfo = resultJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertThat(dokumentInfo.getDokumentstatus(), is(UNDER_REDIGERING));
		assertThat(dokumentInfo.getDokumentFerdigDato(), is(nullValue()));
		assertThat(dokumentInfo.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV), is(nullValue()));
	}

	@Test
	public void shouldThrowException_missingInput() {
		assertThrows(IllegalArgumentException.class,
				() -> arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(new FjernFerdigstiltDokumentRequest()));
	}

	@Test
	public void shouldThrowException_DokumentAlleredeRedigerbart() throws Exception {
		Journalpost persistedJournalpost = buildAndPersistJournalpost(UNDER_REDIGERING);

		assertThrows(FjernFerdigstiltDokumentDokumentAlleredeRedigerbart.class,
				() -> arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(createRequest(persistedJournalpost)));
	}

	@Test
	public void shouldThrowException_DokumentAlleredeAvbrutt() throws Exception {
		Journalpost persistedJournalpost = buildAndPersistJournalpost(DokumentStatusCode.AVBRUTT);

		assertThrows(FjernFerdigstiltDokumentDokumentAlleredeAvbrutt.class,
				() -> arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(createRequest(persistedJournalpost)));
	}


	private FjernFerdigstiltDokumentRequest createRequest(Journalpost journalpost) throws Exception {
		FjernFerdigstiltDokumentRequest request = new FjernFerdigstiltDokumentRequest();
		request.setJournalpostId(journalpost.getJournalpostId());
		request.setDokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());
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
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(createDokumentInfo(dokumentStatusCode))
								.build())

				.build();

		joarkRepository.save(journalpost);
		return journalpost;

	}

	private DokumentInfo createDokumentInfo(DokumentStatusCode dokumentStatusCode) {
		return getDokumentInfoBuilder()
				.dokumentstatus(dokumentStatusCode)
				.dokumentFerdigDato(new Date())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.filDetaljerList(createFilDetaljer())
				.build();
	}

	private FilDetaljer createFilDetaljer() {
		return getFilDetaljerBuilder()
				.fileContent("file".getBytes())
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}

}
