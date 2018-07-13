package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

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
 * Implementation of JournalfoerInngaaendeHenvendelseMedHoveddokument
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultJournalfoerInngaaendeHenvendelse implements
		JournalfoerInngaaendeHenvendelse {

	@Inject
	private JoarkRepository joarkRepository;
	@Inject
	private JournalfoerInngaaendeHenvendelseValidator behandleJournalJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest) {
		validateRequest(journalfoerInngaaendeHenvendelseRequest);

		Journalpost journalpost = journalfoerInngaaendeHenvendelseRequest.getJournalpost();
		updateJournalpost(journalpost);

		behandleJournalJournalpostValidator.validate(journalpost);

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		joarkRepository.save(journalpost);

		return createResponse(journalpost);
	}

	private JournalfoerInngaaendeHenvendelseResponse createResponse(Journalpost journalpost) {
		return new JournalfoerInngaaendeHenvendelseResponse(journalpost.getJournalpostId());
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		journalpost.setJournalstatus(JournalStatusCode.J);
		journalpost.setJournalDato(DateProvider.getToday());

		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private void validateRequest(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest) {
		if (journalfoerInngaaendeHenvendelseRequest == null) {
			throw new ApplicationException("Missing parameter: journalfoerInngaaendeHenvendelseRequest");
		}
		journalfoerInngaaendeHenvendelseRequest.validate();
	}

}
