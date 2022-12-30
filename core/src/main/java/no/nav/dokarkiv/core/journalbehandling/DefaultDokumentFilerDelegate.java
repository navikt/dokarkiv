package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultDokumentFilerDelegate implements DokumentFilerDelegate {

	private DokumentFilRepository dokumentFilRepository;

	public DefaultDokumentFilerDelegate(DokumentFilRepository dokumentFilRepository) {
		this.dokumentFilRepository = dokumentFilRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveUpdateDokumentFiler(Journalpost journalpost) {
		saveNewDokumentFiler(journalpost);
		updateExistingDokumentFiler(journalpost);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveNewDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> newDokumentFiler = createNewDokumentFiler(journalpost);
		for (DokumentFil dokumentFil : newDokumentFiler) {
			saveDokumentFil(dokumentFil);
		}
	}

	private List<DokumentFil> createNewDokumentFiler(Journalpost journalpost) {
		List<DokumentFil> dokumentFiler = new ArrayList<>();
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.getFildetaljerId() == null && filDetaljer.hasFileContent()) {
				dokumentFiler.add(filDetaljer.createDokumentFil());
			}
		}
		return dokumentFiler;
	}

	private void saveDokumentFil(DokumentFil dokumentFil) {
		dokumentFilRepository.persist(dokumentFil);
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
		DokumentFil existingDokumentFil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());
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
