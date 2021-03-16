package no.nav.dokarkiv.core.journalbehandling;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorUtil;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponse;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of DokumentFilerDelegate.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Slf4j
@Component
public class DefaultDokumentFilerDelegate implements DokumentFilerDelegate {

	@Inject
	private DokumentFilRepository dokumentFilRepository;

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
				PdfValidatorUtil.logJournalpost(journalpost, filDetaljer.getFilUuid());
			}
		}
		return dokumentFiler;
	}

	private void saveDokumentFil(DokumentFil dokumentFil) {
		dokumentFilRepository.save(dokumentFil);

		validateAndLogDokumentFil(dokumentFil);
	}

	private void updateExistingDokumentFiler(Journalpost journalpost) {
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.hasId() && filDetaljer.hasFileContent()) {
				PdfValidatorUtil.logJournalpost(journalpost, filDetaljer.getFilUuid());
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
			validateAndLogDokumentFil(existingDokumentFil);
		}
	}

	//Legger denne bare her imens. Trenger nok noe mer logikk for å legge den på rett sted
	private void validateAndLogDokumentFil(DokumentFil dokumentFil){
		try {
			String dokumentFilId = null == dokumentFil.getId() ? "INGEN_DOKUMENTFIL_ID" : dokumentFil.getId().toString();
			PdfValidatorResponse response = PdfValidatorUtil.validatePdf(dokumentFil.getFil());
			log.info(response.toString(dokumentFilId));
		}catch(Exception e){
			//catchall for ikke å påvirke testmiljøet om noe går galt.
			log.info("Kunne ikke validere dokumentfil", e);
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
