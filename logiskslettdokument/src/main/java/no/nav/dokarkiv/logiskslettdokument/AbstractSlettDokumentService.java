package no.nav.dokarkiv.logiskslettdokument;

import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;

import javax.inject.Inject;

public abstract class AbstractSlettDokumentService {

	protected final String SLETTEMELDING = " - slettet";

	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
}
