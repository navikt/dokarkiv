package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.JournalpostNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Sjekker om journalpost og dokumentet er i riktig status for HentFerdigstilteDokumenter(TJOARK121)
 * 
 *  
 * @author Stig Strøm
 *
 */
@Component
public class HentFerdigstilteDokumenterValidator {

	/**
	 * Sjekker om journalpost eksisterer og om det har journalstatus FS
	 * 
	 * @param journalpostId journalpostId fra web requesten
	 * @param journalpost journalpost fra databasen
	 */
	public void validateJournalpost(Long journalpostId, Journalpost journalpost) throws JournalpostNotFoundException, IllegalJournalStatusException {
		if (journalpost == null) {
			throw new JournalpostNotFoundException("journalpostId=" + journalpostId + " eksisterer ikke");
		}
	
		if (!journalpost.hasFerdigOgSentralPrintJournalforingStatus()) {
			throw new IllegalJournalStatusException("journalpostId=" + journalpostId
					+ " forventet JournalStatus FS, men har journalStatus=" + journalpost.getJournalstatus());
		}
	}

	/**
	 * Sjekker om dokumentInfo eksister og at de er ferdigstilt
	 *  
	 * @param journalpostId journalpostId fra web request
	 * @param dokumentInfoId dokumentInfoId fra web request
	 * @param dokumentInfo dokumentInfo fra databasen
	 */
	public void validateDokumentInfo(Long journalpostId, Long dokumentInfoId, DokumentInfo dokumentInfo) throws DokumentInfoNotFoundException, IllegalDokumentstatusException {
		if (dokumentInfo == null) {
			throw new DokumentInfoNotFoundException("dokumentInfoId=" + dokumentInfoId
					+ " hører ikke til journalpost med journalpostId=" + journalpostId);
		}
	
		if (!dokumentInfo.isFerdigstilt()) {
			throw new IllegalDokumentstatusException("dokumentInfoId=" + dokumentInfoId + " som tilhører journalpostId=" + journalpostId
					+ " er ikke ferdigstilt");
		}
	}

	/**
	 * Sjekker om filDetaljer eksister og at dokumentet er pdf eller pdfa
	 * 
	 * @param dokumentInfoId dokumentInfoId fra web request
	 * @param filDetaljer Fildetaljer fra databasen
	 */
	public void validateFildetaljer(Long dokumentInfoId, FilDetaljer filDetaljer) throws IllegalVariantFormatException {
		if (filDetaljer == null) {
			throw new IllegalVariantFormatException("dokumentInfoId=" + dokumentInfoId + " mangler variantformat arkiv");
		}
		if (!filDetaljer.isAPdf()) {
			throw new IllegalVariantFormatException("dokumentInfoId=" + dokumentInfoId + ",fildetaljerId=" + filDetaljer.getId()
					+ " er ikke av type PDF/PDFA");
		}
	}
}
