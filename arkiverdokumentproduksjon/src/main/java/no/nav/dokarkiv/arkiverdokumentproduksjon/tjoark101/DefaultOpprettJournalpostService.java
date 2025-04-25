package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultOpprettJournalpostService implements OpprettJournalpostService {

    private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final OpprettJournalpostValidator opprettJournalpostValidator;
	private final DokumentFilerDelegate dokumentFilerDelegate;

	public DefaultOpprettJournalpostService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, OpprettJournalpostValidator opprettJournalpostValidator, DokumentFilerDelegate dokumentFilerDelegate) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.opprettJournalpostValidator = opprettJournalpostValidator;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
	}

	@Override
	public OpprettJournalpostResponseTo opprettJournalpost(
			OpprettJournalpostRequestTo opprettJournalpostRequest) {
		validateRequest(opprettJournalpostRequest);

		Journalpost journalpost = opprettJournalpostRequest.getJournalpost();
		updateJournalpost(journalpost);
		opprettJournalpostValidator.validate(journalpost);
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		Journalpost storedJournalpost = journalpostRepositorySkjermet.save(journalpost);
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
		journalpost.setJournalDato(LocalDateTime.now());
		journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		dokumentInfo.setDokumentFerdigDato(LocalDateTime.now());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}
}
