package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static org.assertj.core.api.Assertions.assertThat;

public class MottaDokumentUtgaaendeSkanningValidatorTest {

	MottaDokumentUtgaaendeSkanningValidator mottaDokumentUtgaaendeSkanningValidator = new MottaDokumentUtgaaendeSkanningValidator();

	private static final Date mockDate = new Date(1000000L);
	private static final String mockMottaksKanal = MottaksKanalCode.SKAN_NETS.toString();
	private static final List<Tilleggsopplysning> mockTilleggsopplysninger = List.of(new Tilleggsopplysning("mockNoekkel", "mockVerdi"));
	private static final String mockBatchnavn = "mockBatchnavn";
	private static final byte[] mockData = "mockData".getBytes();
	private static final String mockFilnavn = "mockFilnavn";
	private static final String mockEksternReferanse = "mockEksternReferanse";
	private static final JournalpostTypeCode VALID_JOURNALPOSTTYPECODE = U;
	private static final JournalpostTypeCode INVALID_JOURNALPOSTTYPECODE = I;
	private static final JournalStatusCode VALID_JOURNALSTATUSCODE = R;
	private static final JournalStatusCode INVALID_JOURNALSTATUSCODE = FL;
	private static final String UGYLDIG_MOTTAKSKANAL = "UGYLDIG_MOTTAKSKANAL";

	@Test
	public void shouldValidateRequest() {
		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				List.of(DokumentVariant.builder()
						.filtype(PDF.toString())
						.variantformat(ORIGINAL.toString())
						.fysiskDokument(mockData)
						.filnavn(mockFilnavn)
						.build())
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isEmpty();
	}

	@Test
	public void shouldNotValidateWhenRequiredFieldsAreMissing() {
		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(null,
				List.of(DokumentVariant.builder()
						.filtype(null)
						.variantformat(null)
						.fysiskDokument(null)
						.filnavn(null)
						.build())
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isPresent();
		assertThat(valdationResult.get())
				.isEqualTo("Kan ikke validere request: " +
						   "mottakskanal kan ikke være null; " +
						   "dokumentvarianter[0] mangler filtype, mangler variantformat, mangler fysiskDokument");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " "})
	@NullSource
	public void shouldNotValidateWhenTilleggsopplysningerIsInvalid(String nokkelOrVerdi) {
		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				mockMottaksKanal,
				List.of(DokumentVariant.builder()
						.filtype(PDF.toString())
						.variantformat(ORIGINAL.toString())
						.fysiskDokument(mockData)
						.filnavn(mockFilnavn)
						.build()),
				List.of(new Tilleggsopplysning(nokkelOrVerdi, nokkelOrVerdi))
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isPresent();
		assertThat(valdationResult.get())
				.isEqualTo("Kan ikke validere request: " +
						"tilleggsopplysninger[] kan ikke inneholde tilleggsopplysning der nokkel er null eller blank; " +
						"tilleggsopplysninger[] kan ikke inneholde tilleggsopplysning der verdi er null eller blank");
	}

	@Test
	public void shouldNotValidateWhenRequiredFieldsAreInvalid() {
		String ulovligVariantformat = PDF.name();

		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				UGYLDIG_MOTTAKSKANAL,
				List.of(DokumentVariant.builder()
						.filtype(ARKIV.toString())
						.variantformat(ulovligVariantformat)
						.fysiskDokument(mockData)
						.filnavn(mockFilnavn)
						.build()
				)
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isPresent();
		assertThat(valdationResult.get())
				.isEqualTo("Kan ikke validere request: " +
						   "mottakskanal er ugyldig; " +
						   "dokumentvarianter[0] har ugyldig filtype ARKIV, har ugyldig variantformat PDF");
	}

	@Test
	public void shouldValidateRequestWithMultipleDokumentVariants() {
		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				List.of(DokumentVariant.builder()
								.filtype(PDF.toString())
								.variantformat(ORIGINAL.toString())
								.fysiskDokument(mockData)
								.filnavn(mockFilnavn)
								.build(),
						DokumentVariant.builder()
								.filtype(PDF.toString())
								.variantformat(ORIGINAL.toString())
								.fysiskDokument(mockData)
								.filnavn(mockFilnavn)
								.build()
				)
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isEmpty();
	}

	@Test
	public void shouldNotValidateWhenMultipleDokumentVariantsInvalid() {
		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				UGYLDIG_MOTTAKSKANAL,
				List.of(DokumentVariant.builder()
								.filtype(null)
								.variantformat(null)
								.fysiskDokument(null)
								.filnavn(null)
								.build(),
						DokumentVariant.builder()
								.filtype(ARKIV.toString())
								.variantformat(PDF.toString())
								.fysiskDokument(mockData)
								.filnavn(mockFilnavn)
								.build()
				)
		);

		Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);

		assertThat(valdationResult).isPresent();
		assertThat(valdationResult.get())
				.isEqualTo("Kan ikke validere request: " +
						   "mottakskanal er ugyldig; " +
						   "dokumentvarianter[0] mangler filtype, mangler variantformat, mangler fysiskDokument; " +
						   "dokumentvarianter[1] har ugyldig filtype ARKIV, har ugyldig variantformat PDF");
	}

	@Test
	public void shouldValidateJournalpost() {
		Journalpost journalpost = generateValidJournalpost();

		Optional<String> validationResultHasAllElements = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostHasAllElements(journalpost);
		assertThat(validationResultHasAllElements).isEmpty();

		Optional<String> validationResultMetadata = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostMetadata(journalpost);
		assertThat(validationResultMetadata).isEmpty();
	}

	@Test
	public void shouldNotValidateJournalpostNoHoveddokument() {
		Journalpost journalpost = generateJournalpostNoHoveddokument();

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostHasAllElements(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get()).isEqualTo("Kan ikke validere journalpost: Har ikke hoveddokument");
	}

	@Test
	public void shouldNotValidateJournalpostNoDokumentInfo() {
		Journalpost journalpost = generateJournalpostNoDokumentInfo();

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostHasAllElements(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get()).isEqualTo("Kan ikke validere journalpost: Mangler DokumentInfo");
	}

	@Test
	public void shouldNotValidateInvalidJournalPostTypeCode() {
		Journalpost journalpost = generateJournalpostWithHoveddokument(INVALID_JOURNALPOSTTYPECODE, VALID_JOURNALSTATUSCODE);

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostMetadata(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get()).isEqualTo("Kan ikke validere journalpost: Journalposten har ugyldig journalposttype=I");
	}

	@Test
	public void shouldNotValidateInvalidJournalStatusCode() {
		Journalpost journalpost = generateJournalpostWithHoveddokument(VALID_JOURNALPOSTTYPECODE, INVALID_JOURNALSTATUSCODE);

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostMetadata(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get()).isEqualTo("Kan ikke validere journalpost: Journalposten har ugyldig journalpostStatus=FL");
	}

	@Test
	public void shouldNotValidateTwoDokumentInfo() {
		Journalpost journalpost = generateJournalpostTwoDokumentInfo();

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostMetadata(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get()).isEqualTo("Kan ikke validere journalpost: Journalposten har mer enn ett DokumentInfo-objekt");
	}

	@Test
	public void shouldNotValidateJournalpostWithExistingFildetaljer() {
		Journalpost journalpost = generateInvalidJournalpostWithFilDetaljer();

		Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpostMetadata(journalpost);

		assertThat(validationResult).isPresent();
		assertThat(validationResult.get())
				.isEqualTo("Kan ikke validere journalpost: Journalposten har ugyldig journalposttype=I; Journalposten har ugyldig journalpostStatus=FL; " +
						   "Journalposten har mer enn ett DokumentInfo-objekt; Journalposten har allerede fildetaljer og kan ikke oppdateres. JournalpostId er ugyldig eller samme førsteside er benyttet flere ganger.");
	}

	private MottaDokumentUtgaaendeSkanningRequest buildRequest(List<DokumentVariant> dokumentVarianter) {
		return buildRequest(mockMottaksKanal, dokumentVarianter);
	}

	private MottaDokumentUtgaaendeSkanningRequest buildRequest(String mottaksKanal, List<DokumentVariant> dokumentVarianter) {
		return new MottaDokumentUtgaaendeSkanningRequest(
				mockDate,
				mottaksKanal,
				mockTilleggsopplysninger,
				mockBatchnavn,
				dokumentVarianter,
				mockEksternReferanse
		);
	}

	private MottaDokumentUtgaaendeSkanningRequest buildRequest(String mottaksKanal, List<DokumentVariant> dokumentVarianter, List<Tilleggsopplysning> tilleggsopplysninger) {
		return new MottaDokumentUtgaaendeSkanningRequest(
				mockDate,
				mottaksKanal,
				tilleggsopplysninger,
				mockBatchnavn,
				dokumentVarianter,
				mockEksternReferanse
		);
	}

	private Journalpost generateValidJournalpost() {
		return generateJournalpost(VALID_JOURNALPOSTTYPECODE, VALID_JOURNALSTATUSCODE, TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
	}

	private Journalpost generateJournalpostNoHoveddokument() {
		return generateJournalpost(VALID_JOURNALPOSTTYPECODE, VALID_JOURNALSTATUSCODE, VEDLEGG);
	}

	private Journalpost generateJournalpostWithHoveddokument(JournalpostTypeCode journalpostTypeCode, JournalStatusCode journalstatuscode) {
		return generateJournalpost(journalpostTypeCode, journalstatuscode, TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
	}

	private Journalpost generateJournalpost(JournalpostTypeCode journalpostTypeCode, JournalStatusCode journalstatuscode, TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(0L)
				.opprettetKildeNavn("unitTest")
				.journalpostType(journalpostTypeCode)
				.journalStatus(journalstatuscode)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("unitTest")
								.tilknyttetAvNavn("unitTest")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").build())
								.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
								.build())
				.build();
	}

	private Journalpost generateJournalpostNoDokumentInfo() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(0L)
				.opprettetKildeNavn("unitTest")
				.journalpostType(VALID_JOURNALPOSTTYPECODE)
				.journalStatus(VALID_JOURNALSTATUSCODE)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("unitTest")
								.tilknyttetAvNavn("unitTest")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.build())
				.build();
	}

	private Journalpost generateJournalpostTwoDokumentInfo() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(0L)
				.opprettetKildeNavn("unitTest")
				.journalpostType(VALID_JOURNALPOSTTYPECODE)
				.journalStatus(VALID_JOURNALSTATUSCODE)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("unitTest")
								.tilknyttetAvNavn("unitTest")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").build())
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.build(),
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("unitTest")
								.tilknyttetAvNavn("unitTest")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").build())
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.build())
				.build();
	}

	private Journalpost generateInvalidJournalpostWithFilDetaljer() {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.filtype(PDF)
				.filnavn("mock")
				.variantFormat(ARKIV)
				.fileContent("mock".getBytes())
				.batchNavn("mock")
				.filUuid(FilDetaljer.generateUuid())
				.build();
		filDetaljer.setOpprettetKildeNavn("unitTest");

		Journalpost journalpost = JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(0L)
				.opprettetKildeNavn("unitTest")
				.journalpostType(INVALID_JOURNALPOSTTYPECODE)
				.journalStatus(INVALID_JOURNALSTATUSCODE)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("unitTest")
								.tilknyttetAvNavn("unitTest")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").filDetaljerList(filDetaljer).build())
								.tilknyttetJournalpostSom(VEDLEGG)
								.build())
				.build();

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.fildetaljerListe(Set.of(filDetaljer))
				.build();
		dokumentInfo.setOpprettetKildeNavn("unitTest");

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(VEDLEGG)
				.journalpost(journalpost)
				.journalpostDokumentInfoRelasjonId(10L)
				.dokumentInfo(
						dokumentInfo
				)
				.tilknyttetAvNavn("unitTest")
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn("unitTest");


		journalpost.addJournalpostDokumentInfoRelasjon(
				journalpostDokumentInfoRelasjon
		);

		return journalpost;
	}

}