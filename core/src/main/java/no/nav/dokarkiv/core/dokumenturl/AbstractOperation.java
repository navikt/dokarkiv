package no.nav.dokarkiv.core.dokumenturl;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;

import javax.inject.Inject;

/**
 * Contains common functionality for operation implementations.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public abstract class AbstractOperation {

	/** The repository reference for metadata access. */
	@Inject
	protected JoarkRepository joarkRepository;

	/** The repository reference for document access in DB. */
	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	/**
	 * Setter for the joarkRepository property.
	 * 
	 * @param joarkRepository
	 *            the joarkRepository to set
	 */
	public void setJoarkRepository(JoarkRepository joarkRepository) {
		this.joarkRepository = joarkRepository;
	}

	/**
	 * Setter for the dokumentFilRepository property.
	 * 
	 * @param dokumentFilRepository
	 *            the dokumentFilRepository to set
	 */
	public void setDokumentFilRepository(DokumentFilRepository dokumentFilRepository) {
		this.dokumentFilRepository = dokumentFilRepository;
	}

	public Journalpost getExistingJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		Journalpost existingJournalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (existingJournalpost == null) {
			throw new NoJournalpostFoundException("Journalpost med id " + journalpostId + " eksisterer ikke", journalpostId);
		}
		return existingJournalpost;
	}

}
