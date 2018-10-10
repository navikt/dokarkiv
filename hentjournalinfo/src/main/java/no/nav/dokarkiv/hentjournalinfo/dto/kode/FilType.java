package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum FilType {

    PDF(FilTypeCode.PDF),
    PDFA(FilTypeCode.PDFA),
    XML(FilTypeCode.XML),
    RTF(FilTypeCode.RTF),
    AFP(FilTypeCode.AFP),
    META(FilTypeCode.META),
    DLF(FilTypeCode.DLF),
    JPEG(FilTypeCode.JPEG),
    TIFF(FilTypeCode.TIFF),
    DOC(FilTypeCode.DOC),
    DOCX(FilTypeCode.DOCX),
    XLS(FilTypeCode.XLS),
    XLSX(FilTypeCode.XLSX),
    AXML(FilTypeCode.AXML),
    DXML(FilTypeCode.DXML),
    JSON(FilTypeCode.JSON),
    PNG(FilTypeCode.PNG);

    public final FilTypeCode mapFromValue;

    FilType(FilTypeCode mappedValue) {
        this.mapFromValue = mappedValue;
    }

    public static FilType mapFromFilTypeCode(FilTypeCode filTypeCode) {
        return Arrays.stream(FilType.values())
                .filter(filType -> filType.getMapFromValue() == filTypeCode)
                .findFirst()
                .orElse(null);
    }
}
