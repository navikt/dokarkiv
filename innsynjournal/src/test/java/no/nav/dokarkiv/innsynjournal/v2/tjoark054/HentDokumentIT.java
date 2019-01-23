package no.nav.dokarkiv.innsynjournal.v2.tjoark054;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID_SLADDET;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.createDokumentFil;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.createDokumentFilSladdet;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.DOKUMENT_TITTEL;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createDokumentInfo;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.createFildetaljerFil;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock;
import no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.innsynjournal.v2.AbstractInnsynJournalV2Itest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Integration test for HentDokument(TJOARK054).
 *
 * @author Ketill Fenne, Visma Consulting.
 */
public class HentDokumentIT extends AbstractInnsynJournalV2Itest {

	private static final String FNR = "***gammelt_fnr***";

	@Value("#{T(java.time.LocalDate).parse(\"${innsynjournal.v2.innsyn.earliest.date}\")}")
	private LocalDate earliestAllowedDate;

	@BeforeClass
	public static void setUpSecurity() {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() throws Exception {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId("123");
		request.setDokumentId("123");

		expectedException.expect(isA(HentDokumentDokumentIkkeFunnet.class));
		expectedException.expectMessage("Journalpost med id 123 eksisterer ikke");

		innsynJournalV2Provider.hentDokument(request);
	}

	@Test
	public void shouldGetDokumentWhenMottakskanalNavNOAndAvsenderMottakerIdMatches() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.NAV_NO).build());

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));

		assertThat(response.getDokument(), is(DokumentFilTestDataProvider.FIL_CONTENT));
	}

	@Test
	public void shouldGetDokumentWhenNotatAndAvsenderMottakerIdIsNull() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.NAV_NO)
				.journalpostType(JournalpostTypeCode.N)
				.avsenderMottakerId(null).build());

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));

		assertThat(response.getDokument(), is(DokumentFilTestDataProvider.FIL_CONTENT));
	}

	@Test
	public void shouldGetDokumentWhenMottakskanalNavNOAndAvsendetMottakerIdHistorical() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.NAV_NO)
				.avsenderMottakerId(AktoerConsumerV2Mock.HISTORICAL_IDENTS.get(0)).build());

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));

		assertThat(response.getDokument(), is(DokumentFilTestDataProvider.FIL_CONTENT));
	}

	@Test
	public void shouldNotReturnDocumentCreatedToEarly() throws Exception {
		Date toEarlyDate = Date.from(earliestAllowedDate.minusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.changeStamp(new ChangeStamp("test", toEarlyDate, null, null))
				.journalDato(toEarlyDate).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentWrongJournalstatus() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.journalStatus(JournalStatusCode.A).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentFeilregistrert() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon()
						.feilregistrert(Boolean.TRUE)
						.build()).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentFagomradeKon() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.fagomrade(FagomradeCode.KTR).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentMottakskanalSkanPen() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.mottakskanal(MottaksKanalCode.SKAN_PEN).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentWrongAvsenderMottaker() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildJournalpost(MottaksKanalCode.E_POST)
				.avsenderMottakerId("***gammelt_fnr***").build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentWrongKategori() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.kategori(DokumentKategoriCode.E_BLANKETT))
				.journalpostType(JournalpostTypeCode.N).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentOrgInternt() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.kategori(DokumentKategoriCode.FORVALTNINGSNOTAT)
						.organInternt(Boolean.TRUE))
				.journalpostType(JournalpostTypeCode.N).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentWrongDokumentstatus() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING))
				.journalpostType(JournalpostTypeCode.U).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnDocumentOnDemandIdNotNull() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo(DOKUMENT_TITTEL,
						createFildetaljerFil(DokumentFilTestDataProvider.FIL_UUID)
								.onDemandInstans(OnDemandInstansCode.SYFO)
								.onDemandId("1232131"), FIL_UUID_SLADDET)).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldNotReturnBegrensetPartInnsyn() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.innskrenketPartsinnsyn(Boolean.TRUE)).build());

		expectAccessDenied();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}

	@Test
	public void shouldReturnDocumentWithCorrectDokumentstatus() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT))
				.journalpostType(JournalpostTypeCode.U).build());

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));

		assertThat(response.getDokument(), is(DokumentFilTestDataProvider.FIL_CONTENT));
	}

	@Test
	public void shouldReturnSladdetDocumentWithCorrectDokumentstatus() throws Exception {
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT))
				.journalpostType(JournalpostTypeCode.U).build());

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));

		assertThat(response.getDokument(), is(DokumentFilTestDataProvider.FIL_CONTENT_SLADDET));
	}

	@Test
	public void shouldThrowDokumentIkkeFinnetWhenKassert() throws Exception {
		expectedException.expect(HentDokumentDokumentIkkeFunnet.class);
		Journalpost journalpost = joarkRepository.save(buildDokInfoStructure(
				createDokumentInfo()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT))
				.journalpostType(JournalpostTypeCode.U).build());

		skjermingService.setDokumentKassert(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		innsynJournalV2Provider.hentDokument(createRequestFromJournalpost(journalpost));
	}
	private void expectAccessDenied() {
		expectedException.expect(isA(HentDokumentSikkerhetsbegrensning.class));
		expectedException.expectMessage("Access denied");
	}

	private HentDokumentRequest createRequestFromJournalpost(Journalpost journalpost) {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(journalpost.getId().toString());
		request.setDokumentId(journalpost.findAllDokumentInfos().iterator().next().getId().toString());
		return request;
	}
	
	private JournalpostBuilder buildDokInfoStructure(DokumentInfoBuilder dokumentInfoBuilder) {
		dokumentFilRepository.save(createDokumentFil().build());
		dokumentFilRepository.save(createDokumentFilSladdet().build());
		return appendServiceSpecificAttributes(JournalpostTestDataProvider
				.createJournalpost(dokumentInfoBuilder)
				.mottakskanal(MottaksKanalCode.E_POST));

	}

	private JournalpostBuilder buildJournalpost(MottaksKanalCode mottaksKanalCode) {
		dokumentFilRepository.save(createDokumentFil().build());
		dokumentFilRepository.save(createDokumentFilSladdet().build());
		return appendServiceSpecificAttributes(JournalpostTestDataProvider
				.createJournalpost(DokumentFilTestDataProvider.FIL_UUID)
				.mottakskanal(mottaksKanalCode));
	}

	private JournalpostBuilder appendServiceSpecificAttributes(JournalpostBuilder journalpostBuilder) {
		Date legalJournalDate = Date.from(earliestAllowedDate.plusYears(5).atStartOfDay(ZoneId.systemDefault()).toInstant());
		return journalpostBuilder
				.avsenderMottakerId(FNR)
				.changeStamp(new ChangeStamp("test", legalJournalDate, null, null))
				.journalDato(legalJournalDate);
	}
}
