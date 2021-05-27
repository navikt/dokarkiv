package no.nav.dokarkiv.journalpost.v1.util;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.*;

public final class JournalStatusCodeConstants {
	public static final List<JournalStatusCode> INNGAAENDE_JOURNAL_STATUS_CODE = Arrays.asList(OD, M, MO, UB);
	public static final List<JournalStatusCode> UTGAAENDE_OR_NOTAT_JOURNAL_STATUS_CODE = Arrays.asList(D, R);
	public static final List<JournalStatusCode> AVBRUT_JOURNAL_STATUS_CODE = Arrays.asList(A, U);
}
