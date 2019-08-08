package no.nav.dokarkiv.sak.infrastruktur;


import static no.nav.dokarkiv.sak.infrastruktur.SubjectType.SUBJECT_TYPE_EKSTERNBRUKER;
import static no.nav.dokarkiv.sak.infrastruktur.SubjectType.SUBJECT_TYPE_INTERNBRUKER;
import static no.nav.dokarkiv.sak.infrastruktur.SubjectType.SUBJECT_TYPE_SYSTEMBRUKER;

import no.nav.dokarkiv.core.MDCConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

public class ContextExtractor {

	private ContextExtractor() {
		//Util
	}

	public static String getUserName() {
		return MDC.get(MDCConstants.MDC_USER_ID);
	}

	public static SubjectType getSubjectType() {
		String username = getUserName();
		if (StringUtils.startsWith(username, "srv")) {
			return SUBJECT_TYPE_SYSTEMBRUKER;
		} else if (StringUtils.isNumeric(username) && StringUtils.length(username) == 11) {
			return SUBJECT_TYPE_EKSTERNBRUKER;
		} else {
			return SUBJECT_TYPE_INTERNBRUKER;
		}
	}
}