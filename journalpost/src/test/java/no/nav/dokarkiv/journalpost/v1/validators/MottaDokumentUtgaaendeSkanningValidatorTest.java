package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MottaDokumentUtgaaendeSkanningValidatorTest {
    MottaDokumentUtgaaendeSkanningValidator mottaDokumentUtgaaendeSkanningValidator = new MottaDokumentUtgaaendeSkanningValidator();

    private final Date mockDate = new Date(1000000L);
    private final String mockMottaksKanal = MottaksKanalCode.SKAN_NETS.toString();
    private final List<Tilleggsopplysning> mockTilleggsopplysninger = List.of(new Tilleggsopplysning("mockNoekkel", "mockVerdi"));
    private final String mockBatchnavn = "mockBatchnavn";
    private final byte[] mockData = "mockData".getBytes();
    private final String mockFilnavn = "mockFilnavn";

    private final String INVALID_MOTTAKSKANAL = "INVALID_MOCK";

    @Test
    public void shouldValidatehappyPathRequest(){
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
                List.of(
                        DokumentVariant
                                .builder()
                                .filtype(FilTypeCode.PDF.toString())
                                .variantformat(VariantFormatCode.ORIGINAL.toString())
                                .fysiskDokument(mockData)
                                .filnavn(mockFilnavn)
                                .build()
                )
        );
        Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);
        assertTrue(valdationResult.isEmpty());
    }

    @Test
    public void shouldNotValidateWithMissingRequiredFields(){
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
                null,
                List.of(
                        DokumentVariant
                                .builder()
                                .filtype(null)
                                .variantformat(null)
                                .fysiskDokument(null)
                                .filnavn(null)
                                .build()
                )
        );
        Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);
        assertTrue(valdationResult.isPresent());
        assertEquals(
                "Kan ikke validere request: " +
                "mottakskanal kan ikke være null; " +
                "dokumentvarianter[0] mangler filtype, mangler variantformat, mangler fysiskDokument",
                valdationResult.get()
        );
    }

    @Test
    public void shouldNotValidateWithInvalidRequiredFields(){
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
                INVALID_MOTTAKSKANAL,
                List.of(
                        DokumentVariant
                                .builder()
                                .filtype(VariantFormatCode.ARKIV.toString())
                                .variantformat(FilTypeCode.PDF.toString())
                                .fysiskDokument(mockData)
                                .filnavn(mockFilnavn)
                                .build()
                )
        );
        Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);
        assertTrue(valdationResult.isPresent());
        assertEquals(
                "Kan ikke validere request: " +
                "mottakskanal er ugyldig; " +
                "dokumentvarianter[0] har ugyldig filtype ARKIV, har ugyldig variantformat PDF",
                valdationResult.get()
        );
    }

    @Test
    public void ShouldValidateMultipleDokumentVariantsHappy() {
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
                List.of(
                        DokumentVariant
                                .builder()
                                .filtype(FilTypeCode.PDF.toString())
                                .variantformat(VariantFormatCode.ORIGINAL.toString())
                                .fysiskDokument(mockData)
                                .filnavn(mockFilnavn)
                                .build(),
                        DokumentVariant
                                .builder()
                                .filtype(FilTypeCode.PDF.toString())
                                .variantformat(VariantFormatCode.ORIGINAL.toString())
                                .fysiskDokument(mockData)
                                .filnavn(mockFilnavn)
                                .build()
                )
        );
        Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);
        assertTrue(valdationResult.isEmpty());
    }

    @Test
    public void ShouldValidateMultipleDokumentVariantsInvalid(){
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
                INVALID_MOTTAKSKANAL,
                List.of(
                        DokumentVariant
                                .builder()
                                .filtype(null)
                                .variantformat(null)
                                .fysiskDokument(null)
                                .filnavn(null)
                                .build(),
                        DokumentVariant
                                .builder()
                                .filtype(VariantFormatCode.ARKIV.toString())
                                .variantformat(FilTypeCode.PDF.toString())
                                .fysiskDokument(mockData)
                                .filnavn(mockFilnavn)
                                .build()
                )
        );
        Optional<String> valdationResult = mottaDokumentUtgaaendeSkanningValidator.validateRequest(request);
        assertTrue(valdationResult.isPresent());
        assertEquals(
            "Kan ikke validere request: " +
                "mottakskanal er ugyldig; " +
                "dokumentvarianter[0] mangler filtype, mangler variantformat, mangler fysiskDokument; " +
                "dokumentvarianter[1] har ugyldig filtype ARKIV, har ugyldig variantformat PDF",
                valdationResult.get()
        );
    }

    @Test
    public void shouldValidateJournalpost() {
        Journalpost journalpost = generateValidJournalpost();

        Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpost(journalpost);

        assertTrue(validationResult.isEmpty());
    }

    @Test
    public void shouldNotValidateInvalidJournalpost() {
        String errorMessage = "Kan ikke validere journalpost: JournalpostType er ikke U eller N; JournalStatus er ikke R; Har mer enn ett DokumentInfo objekt; Har ikke hoveddokument; Har tilknyttede fildetaljer";

        Journalpost journalpost = generateInvalidJournalpost();

        Optional<String> validationResult = mottaDokumentUtgaaendeSkanningValidator.validateJournalpost(journalpost);

        assertTrue(validationResult.isPresent());

        assertEquals(errorMessage, validationResult.get());
    }

    private MottaDokumentUtgaaendeSkanningRequest buildRequest(List<DokumentVariant> dokumentVarianter){
        return buildRequest(mockMottaksKanal, dokumentVarianter);
    }

    private MottaDokumentUtgaaendeSkanningRequest buildRequest(String mottaksKanal, List<DokumentVariant> dokumentVarianter){
        return new MottaDokumentUtgaaendeSkanningRequest(
                mockDate,
                mottaksKanal,
                mockTilleggsopplysninger,
                mockBatchnavn,
                dokumentVarianter
        );
    }

    private Journalpost generateValidJournalpost() {

        Journalpost journalpost = JournalpostBuilder
                .getJournalpostBuilder()
                .journalpostId(0L)
                .opprettetKildeNavn("unitTest")
                .journalpostType(JournalpostTypeCode.U)
                .journalStatus(JournalStatusCode.R)
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .opprettetKildeNavn("unitTest")
                                .tilknyttetAvNavn("unitTest")
                                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").build())
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                                .build())
                .build();

        return journalpost;
    }

    private Journalpost generateInvalidJournalpost() {
        FilDetaljer filDetaljer = FilDetaljer
                .builder()
                .filtype(FilTypeCode.PDF)
                .filnavn("mock")
                .variantFormat(VariantFormatCode.ARKIV)
                .fileContent("mock".getBytes())
                .batchNavn("mock")
                .filUuid(FilDetaljer.generateUuid())
                .build();
        filDetaljer.setOpprettetKildeNavn("unitTest");

        Journalpost journalpost = JournalpostBuilder
                .getJournalpostBuilder()
                .journalpostId(0L)
                .opprettetKildeNavn("unitTest")
                .journalpostType(JournalpostTypeCode.I)
                .journalStatus(JournalStatusCode.FL)
                .dokumentInfoRelasjoner(
                        JournalpostDokumentInfoRelasjonBuilder
                                .getJournalpostDokumentInfoRelasjonBuilder()
                                .opprettetKildeNavn("unitTest")
                                .tilknyttetAvNavn("unitTest")
                                .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("unitTest").filDetaljerList(filDetaljer).build())
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                                .build())
                .build();

        DokumentInfo dokumentInfo = DokumentInfo
                .builder()
                .fildetaljerListe(Set.of(filDetaljer))
                .build();
        dokumentInfo.setOpprettetKildeNavn("unitTest");

        JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon
                .builder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
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
