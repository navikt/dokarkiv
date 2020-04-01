package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MottaDokumentUtgaaendeSkanningValidatorTest {
    MottaDokumentUtgaaendeSkanningValidator mottaDokumentUtgaaendeSkanningValidator = new MottaDokumentUtgaaendeSkanningValidator();

    private final Date mockDate = new Date(1000000L);
    private final String mockEndorsernr = "mockEndorsernr";
    private final String mockMottattfra = "mockMottattfra";
    private final String mockMottatti = "mockMottatti";
    private final String mockBatchnavn = "mockBatchnavn";
    private final byte[] mockData = "mockData".getBytes();
    private final String mockFilnavn = "mockFilnavn";

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
                "DokumentVariant i request kan ikke valideres: mangler filtype, mangler variantformat, mangler fysiskDokument",
                valdationResult.get()
        );
    }

    @Test
    public void shouldNotValidateWithInvalidRequiredFields(){
        MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
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
                "DokumentVariant i request kan ikke valideres: ugyldig filtype ARKIV, ugyldig variantformat PDF",
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
                "DokumentVariant i request kan ikke valideres: mangler filtype, mangler variantformat, mangler fysiskDokument\n"
                + "DokumentVariant i request kan ikke valideres: ugyldig filtype ARKIV, ugyldig variantformat PDF",
                valdationResult.get()
        );
    }

    private MottaDokumentUtgaaendeSkanningRequest buildRequest(List<DokumentVariant> dokumentVarianter){
        return new MottaDokumentUtgaaendeSkanningRequest(
                mockDate,
                mockEndorsernr,
                mockMottattfra,
                mockMottatti,
                mockBatchnavn,
                dokumentVarianter
        );
    }
}
