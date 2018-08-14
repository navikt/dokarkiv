package no.nav.dokarkiv.core.util;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeJPG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeTIF;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

/**
 * The SpecialFilTypeConverter is used to map filtypes TIF or JPG, submitted from legacy systems, to appropriate filetypes in JOARK
 * The preferred solution would be to have all the legacy systems post valid filetypes, but the simplest solution is this.
 *
 * @author Ketill Fenne, Visma Consulting
 */
public class SpecialFilTypeConverter {
	public static String mapFiltype(String filtype) {
		if (specialFiltypeTIF.equals(filtype)) {
			return FilTypeCode.TIFF.name();
		}
		if (specialFiltypeJPG.equals(filtype)) {
			return FilTypeCode.JPEG.name();
		}
		return filtype;
	}
}
