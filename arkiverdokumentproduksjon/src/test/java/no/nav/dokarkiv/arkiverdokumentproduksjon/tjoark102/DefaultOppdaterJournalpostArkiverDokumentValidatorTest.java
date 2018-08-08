package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;


import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.AlleredeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilStrukturException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.KanIkkeFerdigstillesException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ObjektIkkeFunnetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DefaultOppdaterJournalpostArkiverDokumentValidatorTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private DefaultOppdaterJournalpostArkiverDokumentValidator validator = new DefaultOppdaterJournalpostArkiverDokumentValidator();

	@Test
	public void shouldThrowExceptionIfDuplicateVariantFormats() throws Exception {
		Set<FilDetaljer> filDetaljer = new HashSet<>();
		filDetaljer.add(createFilDetaljer(VariantFormatCode.ARKIV));
		filDetaljer.add(createFilDetaljer(VariantFormatCode.ARKIV));

		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Input til tjenesten inneholder flere fildetaljer med samme variantformat");
		validator.validateNoDuplicateVariantFormats(filDetaljer, 10L);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostTypeIsInngaaendeDokument() throws Exception {
		Journalpost jp = createJournalpost();
		jp.setJournalposttype(JournalpostTypeCode.I);
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();
		request.setFerdigstillJournalpost(false);

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Journalpost kan ikke være av typen INNGÅENDE");
		validator.validateJournalpostTypeAndStatus(jp, request);
	}

	@Test
	public void shouldThrowExceptionOnMissingJournalpostId() throws Exception {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setJournalpostId(null);

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("journalpostId");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionOnMissingDokumentInfoId() throws Exception {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setDokumentInfoId(null);

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("dokumentInfoId");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionOnMissingEndretAvNavn() throws Exception {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setEndretAvNavn(null);

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("endretAvNavn");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionOnEmptyFilDetaljer() throws Exception {
		OppdaterJournalpostArkiverDokumentRequestTo request = createBaseRequest().build();

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("filDetaljer");
		validator.validateRequest(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostStatusIsNotUnderProduction() throws Exception {
		Journalpost jp = createJournalpost();
		jp.setJournalstatus(JournalStatusCode.A);
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		expected.expect(KanIkkeFerdigstillesException.class);
		expected.expectMessage("Journal- og/eller dokumentstatus er ulik \"under arbeid\"");
		validator.validateJournalpostTypeAndStatus(jp, request);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoStatusIsNotUnderRedigering() throws Exception {
		DokumentInfo dokumentInfo = new DokumentInfo(1L, 2L);
		dokumentInfo.setDokumentstatus(DokumentStatusCode.AVBRUTT);

		expected.expect(KanIkkeFerdigstillesException.class);
		expected.expectMessage("DokumentInfo [" + dokumentInfo.getDokumentInfoId() + "] krever status UNDER REDIGERING");
		validator.validateDokumentInfoIsUnderRedigering(dokumentInfo, 10L);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsNoDatoDokument() throws Exception {
		OppdaterJournalpostArkiverDokumentRequestTo request = OppdaterJournalpostArkiverDokumentRequestTo.builder().build();

		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler påkrevd felt datoDokument");
		validator.validateDatoDokument(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostDoesNotContainDokumentInfoWithId() throws Exception {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		Long dokumentInfoId = generateId();

		expected.expect(ObjektIkkeFunnetException.class);
		expected.expectMessage("DokumentInfoId [" + dokumentInfoId + "] finnes ikke");
		validator.validateJournalpostContainsDokumentInfoWithId(journalpost, dokumentInfoId);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsNoDokumentInfoRelasjonOfTypeHoveddokument() throws Exception {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK);

		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Journalpost har ikke korrekt struktur");
		validator.validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsTwoDokumentInfoRelasjonsOfTypeHoveddokument() throws Exception {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		addDokumentInfo(journalpost, generateId());
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);

		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Journalpost har ikke korrekt struktur");
		validator.validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoOrFilDetaljerContainNoArkivFormat() throws Exception {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));
		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Arkivvariant av dokument mangler, kan ikke ferdigstille journalpost");
		validator.validateDokumentInfoOrFilDetaljerContainsArkivFormat(dokumentInfo, filDetaljerSet, 10L);
	}

	@Test
	public void shouldThrowExceptionIfInputContainsDuplicateVariantFormats() throws Exception {

		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.BREVBESTILLING));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));

		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Variantformat");
		validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L);
	}

	@Test
	public void shouldNotThrowExceptionIfBothDokumentInfoAndFilDetailerContainsProduksjonFormatEvenWhenOtherDuplicate() throws Exception {
		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));

		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));

		validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoContainsElementsInFildetaljerEvenWhenProduksjonsFormat() throws Exception {
		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.BREVBESTILLING));

		expected.expect(FeilStrukturException.class);
		expected.expectMessage("Variantformat");
		validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L);
	}

	@Test
	public void shouldThrowExceptionIfNotAllDocumenStatusIsFerdigstilltWhenFerdigstillJournalPost() throws Exception {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, 2L);
		addDokumentInfo(journalpost, 2L);
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();

		expected.expect(KanIkkeFerdigstillesException.class);
		expected.expectMessage("Journalposten kan ikke ferdigstilles fordi tilknyttet dokument (dokumentInfoId=" + dokumentInfo.getDokumentInfoId() + ")  ikke har status " + DokumentStatusCode.FERDIGSTILT
				.name());
		validator.validateThatAllDocumentStatusesAreFerdigstilt(journalpost, dokumentInfo);
	}

	@Test
	public void shouldThrowExceptionOnFerdigstilt() throws Exception {
		expected.expect(AlleredeFerdigstiltException.class);
		expected.expectMessage("Journalpost med dokument er allerede ferdigstilt.");

		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		journalpost.setJournalstatus(JournalStatusCode.FS);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		journalpost.findDokumentInfoById(request.getDokumentInfoId()).setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		validator.validateJournalpostTypeAndStatus(journalpost, request);
	}

	@Test
	public void shouldThrowExceptionOnFerdigLokalPrintAndLokalAndDokFerdigstiltAndFerdigstilles() throws Exception {
		expected.expect(AlleredeFerdigstiltException.class);
		expected.expectMessage("Journalpost med dokument er allerede ferdigstilt lokalprint.");

		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		journalpost.setJournalstatus(JournalStatusCode.FL);
		journalpost.setUtsendingskanal(UtsendingsKanalCode.L);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		journalpost.findDokumentInfoById(request.getDokumentInfoId()).setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		request.setUtsendingskanal(UtsendingsKanalCode.L);

		validator.validateJournalpostTypeAndStatus(journalpost, request);
	}

	@Test
	public void shouldThrowExceptionOnUnderBehandlingAndDokFerdigstilltAndIkkeFerdigstilles() throws Exception {
		expected.expect(AlleredeFerdigstiltException.class);
		expected.expectMessage("Dokument er allerede ferdigstilt for journalpost under arbeid.");

		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();
		request.setFerdigstillJournalpost(false);

		journalpost.setJournalstatus(JournalStatusCode.D);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		}


		validator.validateJournalpostTypeAndStatus(journalpost, request);
	}

	private FilDetaljer createFilDetaljer(VariantFormatCode variantFormatCode) {
		FilDetaljer filDetaljer = new FilDetaljer();
		filDetaljer.setVariantFormat(variantFormatCode);
		return filDetaljer;
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(2379873L)
				.build();
	}

	private void addDokumentInfo(Journalpost journalpost, Long dokumentInfoId) {
		journalpost.addJournalpostDokumentInfoRelasjon(
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.dokumentInfo(
								getDokumentInfoBuilder()
										.dokumentInfoId(dokumentInfoId)
										.filDetaljerList(
												getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ORIGINAL)
														.build())
										.build())
						.build()
		);
	}

	private OppdaterJournalpostArkiverDokumentRequestTo createRequest() {
		OppdaterJournalpostArkiverDokumentRequestTo request = createBaseRequest().build();
		request.addFilDetaljer(createFilDetaljer());
		return request;
	}

	private OppdaterJournalpostArkiverDokumentRequestTo.OppdaterJournalpostArkiverDokumentRequestToBuilder createBaseRequest() {
		return OppdaterJournalpostArkiverDokumentRequestTo.builder()
				.journalpostId(2379873L)
				.dokumentInfoId(123456L)
				.endretAvNavn("endretAv")
				.datoDokument(new Date())
				.ferdigstillJournalpost(true);
	}

	private FilDetaljer createFilDetaljer() {
		FilDetaljer filDetaljer = new FilDetaljer();
		filDetaljer.setFileContent("CONTENT".getBytes());
		return filDetaljer;
	}

	private Long generateId() {
		double seed = Math.random() * 10000000;
		return Math.round(seed);
	}
}