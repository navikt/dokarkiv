package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.domain.DokumentFil;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.repository.dok.joark.DokumentFilRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DokumentFilerDelegate.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultDokumentFilerDelegate implements DokumentFilerDelegate {

	private DokumentFilRepository dokumentFilRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveUpdateDokumentFiler(Journalpost journalpost) {
		saveNewDokumentFiler(journalpost);
		updateExistingDokumentFiler(journalpost);
	}

	private void saveNewDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> newDokumentFiler = createNewDokumentFiler(journalpost);
		for (DokumentFil dokumentFil : newDokumentFiler) {
			saveDokumentFil(dokumentFil);
		}
	}

	private List<DokumentFil> createNewDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> dokumentFiler = new ArrayList<DokumentFil>();
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.getFildetaljerId() == null && filDetaljer.hasFileContent()) {
				dokumentFiler.add(filDetaljer.createDokumentFil());
			}
		}
		return dokumentFiler;
	}

	private void saveDokumentFil(DokumentFil dokumentFil) {
		dokumentFilRepository.saveDokumentFil(dokumentFil);
	}

	private void updateExistingDokumentFiler(Journalpost journalpost) {
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.hasId() && filDetaljer.hasFileContent()) {
				updateDokumentFil(filDetaljer);
			}
		}
	}

	/**
	 * Since it is possible to store a FilDetaljer without a corresponding
	 * DokumentFil we must check if we are updating an existing DokumentFil or
	 * saving a new one.
	 *
	 * @param filDetaljer The existing FilDetaljer.
	 */
	private void updateDokumentFil(FilDetaljer filDetaljer) {
		DokumentFil existingDokumentFil = dokumentFilRepository.findDokumentFil(filDetaljer.getFilUuid());
		if (existingDokumentFil == null) {
			saveDokumentFil(filDetaljer.createDokumentFil());
		} else {
			existingDokumentFil.setFil(filDetaljer.getFileContent());
			existingDokumentFil.setEndretKildeNavn(filDetaljer.getEndretKildeNavn());
			filDetaljer.setFilstorrelse(String.valueOf(filDetaljer.getFileContent().length));
		}
	}

	/**
	 * Setter for the dokumentFilRepository property.
	 *
	 * @param dokumentFilRepository the dokumentFilRepository to set
	 */
	public void setDokumentFilRepository(DokumentFilRepository dokumentFilRepository) {
		this.dokumentFilRepository = dokumentFilRepository;
	}

}
