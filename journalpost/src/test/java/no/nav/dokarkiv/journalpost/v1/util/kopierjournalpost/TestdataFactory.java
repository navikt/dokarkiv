package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import static no.nav.dokarkiv.core.util.TestdataFactory.createJournalpost;
import static no.nav.dokarkiv.core.util.TestdataFactory.setSkjermingVedlegg;

public class TestdataFactory {

	private static final long SAK_ID = 12223344L;

	public static Journalpost createJournalpostWithHoveddokumentAndSkjermetVedlegg(JournalpostTypeCode type, JournalStatusCode status) {
		Journalpost journalpost = createJournalpost(SAK_ID, type, status);
		setSkjermingVedlegg(journalpost);
		return journalpost;
	}

}