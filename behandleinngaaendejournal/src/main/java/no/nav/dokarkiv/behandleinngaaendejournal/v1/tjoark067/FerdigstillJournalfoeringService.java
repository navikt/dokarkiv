package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.ldap.BrukernavnLdapService;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.IdentType;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.MDC;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class FerdigstillJournalfoeringService {
	private static final String UKJENT_BRUKER = "Ukjent";

	private final JoarkRepository repository;
	private final FerdigstillJournalfoeringFieldValidator fieldValidator;
	private final JournalpostStructureVerifier structureVerifier;
	private final BrukernavnLdapService brukernavnLdapService;

	@Inject
	public FerdigstillJournalfoeringService(JoarkRepository repository, FerdigstillJournalfoeringFieldValidator fieldValidator,
											JournalpostStructureVerifier structureVerifier, BrukernavnLdapService brukernavnLdapService) {
		this.repository = repository;
		this.fieldValidator = fieldValidator;
		this.structureVerifier = structureVerifier;
		this.brukernavnLdapService = brukernavnLdapService;
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

		Long journalpostId = Long.parseLong(ferdigstillJournalfoeringTo.getJournalpostId());

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

	private String hentLdapBrukernavn(Long journalpostId) {
		String userId = MDC.get(MDC_USER_ID);
		if (StringUtils.isEmpty(userId)) {
			log.warn(String.format("Kan ikke utlede brukerident på rett format fra SAML-token. journalpostId=%s", journalpostId.toString()));
			return UKJENT_BRUKER;
		}
		
		String ldapNavn = userId;
		IdentType type = SubjectHandler.getSubjectHandler().getIdentType();
		if (type.equals(IdentType.InternBruker)) {
			ldapNavn = brukernavnLdapService.searchWithRetry(userId);
			if (ldapNavn.trim().equals(userId.trim())) {
				log.warn(String.format("Feil ved søk mot LDAP. journalpostId=%s", journalpostId.toString()));
			}
		}
		return ldapNavn;
	}

	private void ferdigstill(Journalpost journalpost, FerdigstillJournalfoeringTo to, String endretAv) {
		journalpost.setJournalstatus(JournalStatusCode.J);
		journalpost.setJournalDato(LocalDateTime.now().toDate());
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
