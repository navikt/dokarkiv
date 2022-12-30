package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.springframework.stereotype.Component;

@Component
public class DefaultJournalfoerInngaaendeHenvendelseV3 implements JournalfoerInngaaendeHenvendelse {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final JournalfoerInngaaendeHenvendelseV3Validator behandleJournalJournalpostValidator;
	private final DokumentFilerDelegate dokumentFilerDelegate;

	public DefaultJournalfoerInngaaendeHenvendelseV3(JournalpostRepositorySkjermet journalpostRepositorySkjermet, JournalfoerInngaaendeHenvendelseV3Validator behandleJournalJournalpostValidator, DokumentFilerDelegate dokumentFilerDelegate) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.behandleJournalJournalpostValidator = behandleJournalJournalpostValidator;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
	}

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest) {
		validateRequest(journalfoerInngaaendeHenvendelseRequest);

		Journalpost journalpost = journalfoerInngaaendeHenvendelseRequest.getJournalpost();
		updateJournalpost(journalpost);

		behandleJournalJournalpostValidator.validate(journalpost);

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		journalpostRepositorySkjermet.save(journalpost);

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
