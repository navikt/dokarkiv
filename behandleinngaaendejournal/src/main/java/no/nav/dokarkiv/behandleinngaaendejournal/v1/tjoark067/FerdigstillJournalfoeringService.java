package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.AbstractBehandleInngaaendeJournalService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class FerdigstillJournalfoeringService extends AbstractBehandleInngaaendeJournalService {
    private final JoarkRepositoryBegrenset repository;
	private final FerdigstillJournalfoeringFieldValidator fieldValidator;
	private final JournalpostStructureVerifier structureVerifier;

	@Inject
    public FerdigstillJournalfoeringService(JoarkRepositoryBegrenset repository, FerdigstillJournalfoeringFieldValidator fieldValidator,
                                            JournalpostStructureVerifier structureVerifier, NavLdapService navLdapService) {
		super(navLdapService);
		this.repository = repository;
		this.fieldValidator = fieldValidator;
		this.structureVerifier = structureVerifier;
	}


	public void ferdigstillJournalfoering(FerdigstillJournalfoeringTo ferdigstillJournalfoeringTo) {
		try {
			doFerdigstillJournalfoering(ferdigstillJournalfoeringTo);
		} catch (NumberFormatException e) {
			throw new UgyldigInputException("Tjenesten kan ikke utføres fordi input er ugyldig. journalpostId=" + ferdigstillJournalfoeringTo.getJournalpostId(), e);
		}
	}

	private void doFerdigstillJournalfoering(FerdigstillJournalfoeringTo ferdigstillJournalfoeringTo) {
		ferdigstillJournalfoeringTo.validate();

		long journalpostId = Long.parseLong(ferdigstillJournalfoeringTo.getJournalpostId());

		Journalpost journalpost = repository.findById(journalpostId).orElse(null);
		if (journalpost == null) {
			throw new JournalpostIkkeFunnetException("Oppgitt journalpostId eksisterer ikke. journalpostId=" + journalpostId);
		}

		validateJournalpostStatePreFerdigstilling(journalpost);
		String journalfortAvNavn = hentLdapBrukernavn(journalpostId);
		ferdigstill(journalpost, ferdigstillJournalfoeringTo, journalfortAvNavn);
		endeligValidation(journalpost);
	}

	private void endeligValidation(Journalpost journalpost) {
		try {
			fieldValidator.validate(journalpost);
			structureVerifier.verifyJournalpostStructure(journalpost);
		} catch (InvalidArgumentException | InvalidJournalpostStructureException e) {
			throw new FerdigstillingIkkeMuligException(e.getMessage() + " journalpostId=" + journalpost.getJournalpostId(), e);
		}
	}

	private void ferdigstill(Journalpost journalpost, FerdigstillJournalfoeringTo to, String endretAv) {
		journalpost.setJournalstatus(JournalStatusCode.J);
		journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		journalpost.setJournalForendeEnhetId(to.getEnhetId());
		journalpost.setJournalfortAvNavn(endretAv);
		journalpost.setEndretAvNavn(endretAv);
		journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	private void validateJournalpostStatePreFerdigstilling(Journalpost journalpost) {
		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException("Journalpost gjelder ikke for en inngående forsendelse. journalpostId=" + journalpost.getJournalpostId());
		}

		if (!journalpost.hasMidlertidigInngaaendeJournalforingStatus()) {
			throw new FerdigstillingIkkeMuligException("Journalpost er ikke midlertidig journalført. journalpostId=" + journalpost.getJournalpostId());
		}

		if (journalpost.isFeilregistrert()) {
			throw new FerdigstillingIkkeMuligException("Sak tilknyttet Journalpost er feilregistrert. journalpostId=" + journalpost.getJournalpostId());
		}
	}

}
