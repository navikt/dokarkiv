package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

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
 * Implementation of JournalfoerNotatHenvendelse
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultJournalfoerNotatHenvendelse implements JournalfoerNotatHenvendelse {
	@Inject
	private JournalfoerNotatHenvendelseValidator behandleJournalJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private JoarkRepository joarkRepository;

	@Override
	public JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest) {
		validateRequest(journalfoerNotatHenvendelseRequest);
		Journalpost journalpost = journalfoerNotatHenvendelseRequest.getJournalpost();
		updateJournalpost(journalpost);
		behandleJournalJournalpostValidator.validate(journalpost);
		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		joarkRepository.save(journalpost);
		return createResponse(journalpost);
	}

	private void validateRequest(JournalfoerNotatHenvendelseRequest request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
		request.validate();
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.N);
		journalpost.setJournalstatus(JournalStatusCode.FL);
		journalpost.setJournalDato(DateProvider.getToday());

		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		relasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.FERDIGSTILT);
		if (relasjon.getDokumentInfo().getDokumentFerdigDato() == null) {
			dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		} else {
			dokumentInfo.setDokumentFerdigDato(relasjon.getDokumentInfo().getDokumentFerdigDato());
		}
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	private JournalfoerNotatHenvendelseResponse createResponse(Journalpost journalpost) {
		return new JournalfoerNotatHenvendelseResponse(journalpost.getJournalpostId());
	}
}
