package no.nav.dokarkiv.core.journalbehandling;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.util.PdfValidator;
import no.nav.dokarkiv.core.util.PdfValidatorResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
			saveDokumentFil(dokumentFil, journalpost);
			validateAndLogDokumentFil(dokumentFil, journalpost);
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

	private void saveDokumentFil(DokumentFil dokumentFil, Journalpost journalpost) {
		dokumentFilRepository.save(dokumentFil);

		//Midlertidig plassering.
		//Tar inn journalpost for bedre logging.
		validateAndLogDokumentFil(dokumentFil, journalpost);
	}

	private void updateExistingDokumentFiler(Journalpost journalpost) {
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.hasId() && filDetaljer.hasFileContent()) {
				updateDokumentFil(filDetaljer, journalpost);
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
	private void updateDokumentFil(FilDetaljer filDetaljer, Journalpost journalpost) {
		DokumentFil existingDokumentFil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());
		if (existingDokumentFil == null) {
			saveDokumentFil(filDetaljer.createDokumentFil(), journalpost);
		} else {
			existingDokumentFil.setFil(filDetaljer.getFileContent());
			existingDokumentFil.setEndretKildeNavn(filDetaljer.getEndretKildeNavn());
			filDetaljer.setFilstorrelse(String.valueOf(filDetaljer.getFileContent().length));
		}
	}

	//Legger denne bare her i mens. Trenger nok noe mer logikk for å legge den på rett sted
	private void validateAndLogDokumentFil(DokumentFil dokumentFil, Journalpost journalpost){
		try {

			String tema = null == journalpost.getBehandlingstema() ? "TEMA_IKKE_SATT" : journalpost.getBehandlingstema();
			//Problemer i test, dette er korteste veien til mål
			String journalpostId = journalpost.getId() == null ? "INGEN_ID" : journalpost.getId().toString();
			String dokumentFilId = null == dokumentFil.getId() ? "INGEN_DOKUMENTFIL_ID" : dokumentFil.getId().toString();
			PdfValidatorResponse response = PdfValidator.isValidPdf(dokumentFil.getFil());
			log.info(response.toString(journalpostId, tema, dokumentFilId));
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
