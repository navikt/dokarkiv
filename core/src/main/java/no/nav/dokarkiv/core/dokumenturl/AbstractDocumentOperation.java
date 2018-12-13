package no.nav.dokarkiv.core.dokumenturl;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.logging.AuditLogger;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;

import javax.inject.Inject;

/**
 * Contains functionality shared between HentDokument and HentDokumentUrl.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public abstract class AbstractDocumentOperation {

	@Inject
	protected JoarkRepositoryBegrenset joarkRepository;

	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Inject
	protected BegrensningService begrensningService;

	public void setJoarkRepository(JoarkRepositoryBegrenset joarkRepository) {
		this.joarkRepository = joarkRepository;
	}

	public void setDokumentFilRepository(DokumentFilRepository dokumentFilRepository) {
		this.dokumentFilRepository = dokumentFilRepository;
	}

	public void setBegrensningService(BegrensningService begrensningService) {
		this.begrensningService= begrensningService;
	}

	/**
	 * Get a FilDetaljer from a Journalpost by filuuid.
	 * 
	 * @param filUuid The filUuid.
	 * @param journalpost The Journalpost.
	 * @return The FilDetaljer.
	 * @throws InvalidFilUuidException if FilDetaljer is not found.
	 */
	protected FilDetaljer getFilDetaljer(String filUuid, Journalpost journalpost) throws InvalidFilUuidException {
		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilUuid(filUuid);
		if (filDetaljer == null) {
			throw new InvalidFilUuidException("Could not find FilDetaljer with filUuid " + filUuid + " for Journalpost "
					+ journalpost.getId(), filUuid);
		}
		return filDetaljer;
	}

	/**
	 * Check if a dokument is sensitivt and log access if it is.
	 * 
	 * @param journalpost The Journalpost.
	 * @param fildetaljer The FilDetaljer.
	 * @param operationName The operation generating the log.
	 */
	protected void generateAuditLogIfDokumentIsSensitivt(Journalpost journalpost, FilDetaljer fildetaljer, 
			String operationName) {
		Boolean sensitivt = fildetaljer.getDokumentInfo().getSensitivt();
		if (sensitivt != null && sensitivt) {
			AuditLogger.generateAuditLog(operationName, journalpost, fildetaljer);
		}
	}

	/**
	 * Retrieve a Journalpost by Id.
	 * 
	 * @param journalpostId The id.
	 * @return The Journalpost
	 * @throws NoJournalpostFoundException if the Journalpost is not found.
	 */
	protected Journalpost getJournalpost(Long journalpostId) throws NoJournalpostFoundException {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost med id " + journalpostId + " eksisterer ikke", journalpostId);
		}
		return journalpost;
	}

	/**
	 * Retrieve a physical document from the database.
	 * 
	 * @param filUuid The filuuid of the DokumentFil.
	 * @return The DokumentFil.
	 * @throws InvalidFilUuidException if the DokumentFil is not found.
	 */
	protected DokumentFil getDocumentFromDBRepository(String filUuid)
			throws InvalidFilUuidException {
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
		if (dokumentFil == null) {
			throw new InvalidFilUuidException("Could not find DokumentFil with filUuid " + filUuid, filUuid); 
		}
		return dokumentFil;
	}

}
