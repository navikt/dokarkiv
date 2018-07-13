package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of JournalfoerUtgaaendeHenvendelse
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultJournalfoerUtgaaendeHenvendelse implements
		JournalfoerUtgaaendeHenvendelse {
	@Inject
	private JournalfoerUtgaaendeHenvendelseValidator behandleJournalJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JoarkRepository joarkRepository;

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest journalfoerUtgaaendeHenvendelseRequest) {
		validateRequest(journalfoerUtgaaendeHenvendelseRequest);
		Journalpost journalpost = journalfoerUtgaaendeHenvendelseRequest.getJournalpost();
		updateJournalpost(journalpost);
		validateJournalpost(journalpost);
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		joarkRepository.save(journalpost);
		return createResponse(journalpost);
	}

	private void validateRequest(JournalfoerUtgaaendeHenvendelseRequest request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
		request.validate();
	}

	private void validateJournalpost(Journalpost journalpost) {
		behandleJournalJournalpostValidator.validate(journalpost);
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setJournalstatus(JournalStatusCode.FS);
		journalpost.setJournalDato(DateProvider.getToday());
		journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());

		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private JournalfoerUtgaaendeHenvendelseResponse createResponse(Journalpost journalpost) {
		return new JournalfoerUtgaaendeHenvendelseResponse(journalpost.getJournalpostId());
	}
}
