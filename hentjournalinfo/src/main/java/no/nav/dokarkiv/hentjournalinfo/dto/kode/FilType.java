package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

import java.util.Optional;

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
        Optional<FilType> filType = Optional.empty();
        for (FilType filTypeValue : FilType.values()) {
            if (filTypeCode == filTypeValue.getMapFromValue()) {
                filType = Optional.of(filTypeValue);
            }
        }

        return filType.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe FilTypeCode=%s til FilType", filTypeCode)));
    }
}
