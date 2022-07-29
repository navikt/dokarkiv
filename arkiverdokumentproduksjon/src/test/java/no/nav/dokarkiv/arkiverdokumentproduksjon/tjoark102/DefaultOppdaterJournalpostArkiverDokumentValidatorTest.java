package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;


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
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultOppdaterJournalpostArkiverDokumentValidatorTest {

	private DefaultOppdaterJournalpostArkiverDokumentValidator validator = new DefaultOppdaterJournalpostArkiverDokumentValidator();

	@Test
	public void shouldThrowExceptionIfDuplicateVariantFormats() {
		Set<FilDetaljer> filDetaljer = new HashSet<>();
		filDetaljer.add(createFilDetaljer(VariantFormatCode.ARKIV));
		filDetaljer.add(createFilDetaljer(VariantFormatCode.ARKIV));

		assertThrows(FeilStrukturException.class,
				() -> validator.validateNoDuplicateVariantFormats(filDetaljer, 10L),
				"Input til tjenesten inneholder flere fildetaljer med samme variantformat");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostTypeIsInngaaendeDokument() {
		Journalpost jp = createJournalpost();
		jp.setJournalposttype(JournalpostTypeCode.I);
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();
		request.setFerdigstillJournalpost(false);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateJournalpostTypeAndStatus(jp, request),
				"Journalpost kan ikke være av typen INNGÅENDE");
	}

	@Test
	public void shouldThrowExceptionOnMissingJournalpostId() {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setJournalpostId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequest(request),
				"journalpostId");
	}

	@Test
	public void shouldThrowExceptionOnMissingDokumentInfoId() {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setDokumentInfoId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequest(request),
				"dokumentInfoId");
	}

	@Test
	public void shouldThrowExceptionOnMissingEndretAvNavn() {
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		request.setEndretAvNavn(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequest(request),
				"endretAvNavn");
	}

	@Test
	public void shouldThrowExceptionOnEmptyFilDetaljer() {
		OppdaterJournalpostArkiverDokumentRequestTo request = createBaseRequest().build();

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequest(request),
				"filDetaljer");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostStatusIsNotUnderProduction() {
		Journalpost jp = createJournalpost();
		jp.setJournalstatus(JournalStatusCode.A);
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		assertThrows(KanIkkeFerdigstillesException.class,
				() -> validator.validateJournalpostTypeAndStatus(jp, request),
				"Journal- og/eller dokumentstatus er ulik \"under arbeid\"");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoStatusIsNotUnderRedigering() {
		DokumentInfo dokumentInfo = new DokumentInfo(1L, 2L);
		dokumentInfo.setDokumentstatus(DokumentStatusCode.AVBRUTT);

		assertThrows(KanIkkeFerdigstillesException.class,
				() -> validator.validateDokumentInfoIsUnderRedigering(dokumentInfo, 10L),
				"DokumentInfo [" + dokumentInfo.getDokumentInfoId() + "] krever status UNDER REDIGERING");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsNoDatoDokument() {
		OppdaterJournalpostArkiverDokumentRequestTo request = OppdaterJournalpostArkiverDokumentRequestTo.builder().build();

		assertThrows(UgyldigInputException.class,
				() -> validator.validateDatoDokument(request),
				"Mangler påkrevd felt datoDokument");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostDoesNotContainDokumentInfoWithId() {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		Long dokumentInfoId = generateId();

		assertThrows(ObjektIkkeFunnetException.class,
				() -> validator.validateJournalpostContainsDokumentInfoWithId(journalpost, dokumentInfoId),
				"DokumentInfoId [" + dokumentInfoId + "] finnes ikke");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsNoDokumentInfoRelasjonOfTypeHoveddokument() {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(FeilStrukturException.class,
				() -> validator.validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(journalpost),
				"Journalpost har ikke korrekt struktur");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostContainsTwoDokumentInfoRelasjonsOfTypeHoveddokument() {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		addDokumentInfo(journalpost, generateId());
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);

		assertThrows(FeilStrukturException.class,
				() -> validator.validateJournalpostContainsOneRealtedDokumenInfoOfTypeHoveddokument(journalpost),
				"Journalpost har ikke korrekt struktur");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoOrFilDetaljerContainNoArkivFormat() {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, generateId());
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));

		assertThrows(FeilStrukturException.class,
				() -> validator.validateDokumentInfoOrFilDetaljerContainsArkivFormat(dokumentInfo, filDetaljerSet, 10L),
				"Arkivvariant av dokument mangler, kan ikke ferdigstille journalpost");
	}

	@Test
	public void shouldThrowExceptionIfInputContainsDuplicateVariantFormats() {

		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.BREVBESTILLING));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));

		assertThrows(FeilStrukturException.class,
				() -> validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L),
				"Variantformat");
	}

	@Test
	public void shouldNotThrowExceptionIfBothDokumentInfoAndFilDetailerContainsProduksjonFormatEvenWhenOtherDuplicate() {
		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));

		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ORIGINAL));

		validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoContainsElementsInFildetaljerEvenWhenProduksjonsFormat() {
		Set<FilDetaljer> jpFilDetaljerSet = new HashSet<>();
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));
		jpFilDetaljerSet.add(createFilDetaljer(VariantFormatCode.PRODUKSJON));

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.ARKIV));
		filDetaljerSet.add(createFilDetaljer(VariantFormatCode.BREVBESTILLING));

		assertThrows(FeilStrukturException.class,
				() -> validator.validateNoDuplicateVariantFormatsExceptProduksjon(jpFilDetaljerSet, filDetaljerSet, 10L),
				"Variantformat");
	}

	@Test
	public void shouldThrowExceptionIfNotAllDocumenStatusIsFerdigstilltWhenFerdigstillJournalPost() {
		Journalpost journalpost = createJournalpost();
		addDokumentInfo(journalpost, 2L);
		addDokumentInfo(journalpost, 2L);
		DokumentInfo dokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();

		assertThrows(KanIkkeFerdigstillesException.class,
				() -> validator.validateThatAllDocumentStatusesAreFerdigstilt(journalpost, dokumentInfo),
				"Journalposten kan ikke ferdigstilles fordi tilknyttet dokument (dokumentInfoId=" + dokumentInfo.getDokumentInfoId() + ")  ikke har status " + DokumentStatusCode.FERDIGSTILT
						.name());
	}

	@Test
	public void shouldThrowExceptionOnFerdigstilt() {
		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		journalpost.setJournalstatus(JournalStatusCode.FS);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		journalpost.findDokumentInfoById(request.getDokumentInfoId()).setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		assertThrows(AlleredeFerdigstiltException.class,
				() -> validator.validateJournalpostTypeAndStatus(journalpost, request),
				"Journalpost med dokument er allerede ferdigstilt.");
	}

	@Test
	public void shouldThrowExceptionOnFerdigLokalPrintAndLokalAndDokFerdigstiltAndFerdigstilles() {
		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();

		journalpost.setJournalstatus(JournalStatusCode.FL);
		journalpost.setUtsendingskanal(UtsendingsKanalCode.L);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		journalpost.findDokumentInfoById(request.getDokumentInfoId()).setDokumentstatus(DokumentStatusCode.FERDIGSTILT);

		request.setUtsendingskanal(UtsendingsKanalCode.L);

		assertThrows(AlleredeFerdigstiltException.class,
				() -> validator.validateJournalpostTypeAndStatus(journalpost, request),
				"Journalpost med dokument er allerede ferdigstilt lokalprint.");
	}

	@Test
	public void shouldThrowExceptionOnUnderBehandlingAndDokFerdigstilltAndIkkeFerdigstilles() {
		Journalpost journalpost = createJournalpost();
		OppdaterJournalpostArkiverDokumentRequestTo request = createRequest();
		request.setFerdigstillJournalpost(false);

		journalpost.setJournalstatus(JournalStatusCode.D);
		addDokumentInfo(journalpost, request.getDokumentInfoId());
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		}

		assertThrows(AlleredeFerdigstiltException.class,
				() -> validator.validateJournalpostTypeAndStatus(journalpost, request),
				"Dokument er allerede ferdigstilt for journalpost under arbeid.");
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