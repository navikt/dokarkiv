package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of the OpprettJournalpostService
 *
 * @author Stig Strøm
 */
@Component
public class DefaultOpprettJournalpostService implements OpprettJournalpostService {

	@Inject
    private JoarkRepositoryBegrenset joarkRepository;
	@Inject
	private OpprettJournalpostValidator opprettJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Override
	public OpprettJournalpostResponseTo opprettJournalpost(
			OpprettJournalpostRequestTo opprettJournalpostRequest) {
		validateRequest(opprettJournalpostRequest);

		Journalpost journalpost = opprettJournalpostRequest.getJournalpost();
		updateJournalpost(journalpost);
		opprettJournalpostValidator.validate(journalpost);
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		Journalpost storedJournalpost = joarkRepository.save(journalpost);
		return createResponse(storedJournalpost);
	}

	private void validateRequest(OpprettJournalpostRequestTo request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
		request.validate();
	}

	private OpprettJournalpostResponseTo createResponse(Journalpost journalpost) {
		return OpprettJournalpostResponseTo.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.build();
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setJournalstatus(JournalStatusCode.D);
		journalpost.setJournalDato(DateProvider.getToday());
		journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}
}
