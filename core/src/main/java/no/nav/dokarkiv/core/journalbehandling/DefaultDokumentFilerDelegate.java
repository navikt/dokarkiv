package no.nav.dokarkiv.core.journalbehandling;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponseToGrafana;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorUtil;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponse;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDFA;

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
			}
		}
		return dokumentFiler;
	}

	private void saveDokumentFil(DokumentFil dokumentFil) {
		dokumentFilRepository.save(dokumentFil);
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
	 * {@inheritDoc}
	 */
	@Override
	public List<PdfValidatorResponseToGrafana> saveUpdateValidateDokumentFiler(Journalpost journalpost) {
		List<PdfValidatorResponseToGrafana> responses = new ArrayList<>();
		saveAndValidateNewDokumentFiler(journalpost, responses);
		updateAndValidateExistingDokumentFiler(journalpost, responses);
		return responses;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveAndValidateNewDokumentFiler(Journalpost journalpost, List<PdfValidatorResponseToGrafana> responses) {
		List<DokumentFil> newDokumentFiler = createAndValidateNewDokumentFiler(journalpost, responses);
		for (DokumentFil dokumentFil : newDokumentFiler) {
			saveDokumentFil(dokumentFil);
		}
	}

	private void updateAndValidateExistingDokumentFiler(Journalpost journalpost, List<PdfValidatorResponseToGrafana> responses) {
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.hasId() && filDetaljer.hasFileContent()) {
				if(PDFA.equals(filDetaljer.getFiltype())) {
					updateAndValidateDokumentFil(filDetaljer, responses);
				} else {
					updateDokumentFil(filDetaljer);
				}

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
	private void updateAndValidateDokumentFil(FilDetaljer filDetaljer, List<PdfValidatorResponseToGrafana> responses) {
		DokumentFil existingDokumentFil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid());
		if (existingDokumentFil == null) {
			DokumentFil dokumentFil = filDetaljer.createDokumentFil();
			saveDokumentFil(dokumentFil);
			if(PDFA.equals(filDetaljer.getFiltype())) {
				safeValidateDokukmentFil(dokumentFil, filDetaljer).ifPresent(response -> responses.add(response));
			}
		} else {
			existingDokumentFil.setFil(filDetaljer.getFileContent());
			existingDokumentFil.setEndretKildeNavn(filDetaljer.getEndretKildeNavn());
			filDetaljer.setFilstorrelse(String.valueOf(filDetaljer.getFileContent().length));
			validateIfPDFA(filDetaljer, existingDokumentFil, responses);
		}
	}

	private List<DokumentFil> createAndValidateNewDokumentFiler(Journalpost journalpost, List<PdfValidatorResponseToGrafana> responses) {
		List<DokumentFil> dokumentFiler = new ArrayList<>();
		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			if (filDetaljer.getFildetaljerId() == null && filDetaljer.hasFileContent()) {
				DokumentFil dokumentfil = filDetaljer.createDokumentFil();
				dokumentFiler.add(dokumentfil);

				validateIfPDFA(filDetaljer, dokumentfil, responses);
			}
		}
		return dokumentFiler;
	}

	private void validateIfPDFA(FilDetaljer filDetaljer, DokumentFil dokumentFil, List<PdfValidatorResponseToGrafana> responses){
		if(filDetaljer.getFiltype() == PDFA){
			safeValidateDokukmentFil(dokumentFil,filDetaljer).ifPresent(response -> responses.add(response));
		}
	}

	//Legger denne bare her imens. Trenger nok noe mer logikk for å legge den på rett sted
	private Optional<PdfValidatorResponseToGrafana> safeValidateDokukmentFil(DokumentFil dokumentFil, FilDetaljer filDetaljer){
		try {
			PdfValidatorResponse response = PdfValidatorUtil.validatePdf(dokumentFil);
			return Optional.of(new PdfValidatorResponseToGrafana(response, filDetaljer));
		}catch(Exception e){
			log.warn("Kunne ikke validere dokumentfil", e);
			return Optional.empty();
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
