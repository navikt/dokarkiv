package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.util.Arrays;

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
        return Arrays.stream(VariantFormat.values())
                .filter(variantFormat -> variantFormat.getMapFromValue() == variantFormatCode)
                .findFirst()
                .orElse(null);
    }
}
