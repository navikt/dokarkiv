package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.security.ldap.NavUserLdapService;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.IdentType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public abstract class AbstractBehandleInngaaendeJournalService {
	private static final String UKJENT_BRUKER = "Ukjent";
	private final NavUserLdapService navUserLdapService;

	protected AbstractBehandleInngaaendeJournalService(NavUserLdapService navUserLdapService) {
		this.navUserLdapService = navUserLdapService;
	}

	protected String hentLdapBrukernavn(Long journalpostId) {
		String userId = MDC.get(MDC_USER_ID);
		if (StringUtils.isEmpty(userId)) {
			log.warn(String.format("Kan ikke utlede brukerident på rett format fra SAML-token. journalpostId=%s", journalpostId.toString()));
			return UKJENT_BRUKER;
		}

		String ldapNavn = userId;
		IdentType type = SubjectHandler.getSubjectHandler().getIdentType();
		if (type.equals(IdentType.InternBruker)) {
			ldapNavn = navUserLdapService.findByUserId(userId).getFullname();
			if (ldapNavn.trim().equals(userId.trim())) {
				log.warn(String.format("Feil ved søk mot LDAP. journalpostId=%s", journalpostId.toString()));
			}
		}
		return ldapNavn;
	}
}
