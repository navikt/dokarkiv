package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DokumentVariant {

	@Schema(
			description = "Filtypen til filen som følger, f.eks. PDF/A, JSON eller XML.",
			requiredMode = REQUIRED,
			example = "PDFA"
	)
	private String filtype;

	@Schema(
			description = """
					Typen variant som arkiveres. ARKIV-varianten vil være den som vises frem til bruker i Gosys og på nav.no. Alle dokumenter som arkiveres må ha én variant med variantformat ARKIV. Variantformat ARKIV skal ha filtype PDF eller (helst) PDFA.
					
					ORIGINAL skal brukes for dokumentvariant i maskinlesbart format (for eksempel XML og JSON) som brukes for automatisk saksbehandling.
					""",
			requiredMode = REQUIRED,
			example = "ARKIV"
	)
	private String variantformat;

	@ArraySchema(arraySchema = @Schema(
			description = """
					Selve dokumentet. Hvis filtype er PDF/XML, ved fysisk dokument brukes bytearray.
					
					Dette feltet må være satt hvis `dokumentFilId` ikke er satt. Hvis feltet er satt så kan ikke `dokumentFilId` være satt.
					""",
			requiredMode = NOT_REQUIRED,
			example = "JVBERi0xLgoxIDAgb2JqPDwvUGFnZXMgMiAwIFI+PmVuZG9iagoyIDAgb2JqPDwvS2lkc1szIDAgUl0vQ291bnQgMT4+ZW5kb2JqCjMgMCBvYmo8PC9QYXJlbnQgMiAwIFI+PmVuZG9iagp0cmFpbGVyIDw8L1Jvb3QgMSAwIFI+Pgo=")
	)
	private byte[] fysiskDokument;

	@Schema(
			description = """
					`dokumentFilId` opprettet med lastOppDokumentFil
					
					Dette feltet må være satt hvis `dokumentFilId` ikke er satt. Hvis feltet er satt så kan ikke `fysiskDokument` være satt.
					
					`dokumentFilId` må eksistere og være i en tilstand som gjør den klar for arkivering i arkivet.
					""",
			requiredMode = NOT_REQUIRED,
			example = "01a086a4-7cfa-7f7f-a19a-1339a4028587"
	)
	private UUID dokumentFilId;

	@Hidden
	@Schema(
			description = "Navnet filen skal ha i arkivet. Brukes for sporingsformål ved arkivering av skannede dokumenter.",
			example = "eksempeldokument.pdf"
	)
	private String filnavn;

	@Hidden
	@Schema(
			description = "Navnet på skanningsbatchen som produserte filen. Feltet skal kun brukes etter avtale.",
			example = "R512345678"
	)
	private String batchnavn;
}
