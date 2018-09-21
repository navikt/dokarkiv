package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum VariantFormat {

    @GraphQLEnumValue(description = "Produksjonsformat")
    PRODUKSJON(VariantFormatCode.PRODUKSJON),
    @GraphQLEnumValue(description = "Arkivformat")
    ARKIV(VariantFormatCode.ARKIV),
    @GraphQLEnumValue(description = "SkanningMetadata")
    SKANNING_META(VariantFormatCode.SKANNING_META),
    @GraphQLEnumValue(description = "BrevbestillingData")
    BREVBESTILLING(VariantFormatCode.BREVBESTILLING),
    @GraphQLEnumValue(description = "Originalformat")
    ORIGINAL(VariantFormatCode.ORIGINAL),
    @GraphQLEnumValue(description = "Sladdetformat")
    SLADDET(VariantFormatCode.SLADDET),
    @GraphQLEnumValue(description = "Produksjonsformat DLF")
    PRODUKSJON_DLF(VariantFormatCode.PRODUKSJON_DLF),
    @GraphQLEnumValue(description = "versjon med infotekster")
    FULLVERSJON(VariantFormatCode.FULLVERSJON);

    public final VariantFormatCode mapFromValue;

    VariantFormat(VariantFormatCode mappedValue) {
        this.mapFromValue = mappedValue;
    }

    public static VariantFormat mapFromVariantFormatCode(VariantFormatCode variantFormatCode) {
        Optional<VariantFormat> variantFormat = Optional.empty();
        for (VariantFormat variantFormatValue : VariantFormat.values()) {
            if (variantFormatCode == variantFormatValue.getMapFromValue()) {
                variantFormat = Optional.of(variantFormatValue);
            }
        }

        return variantFormat.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe VariantFormatCode=%s til VariantFormat", variantFormatCode)));
    }
}
