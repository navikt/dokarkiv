package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
                "Kan ikke validere request:\n" +
                "mottakskanal kan ikke være null\n" +
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
                "Kan ikke validere request:\n" +
                "mottakskanal er ugyldig\n" +
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
            "Kan ikke validere request:\n" +
                "mottakskanal er ugyldig\n" +
                "dokumentvarianter[0] mangler filtype, mangler variantformat, mangler fysiskDokument\n" +
                "dokumentvarianter[1] har ugyldig filtype ARKIV, har ugyldig variantformat PDF",
                valdationResult.get()
        );
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
}
