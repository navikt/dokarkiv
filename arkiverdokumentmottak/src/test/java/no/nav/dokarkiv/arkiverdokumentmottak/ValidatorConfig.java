package no.nav.dokarkiv.arkiverdokumentmottak;

import no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203.DefaultJournalforInngaaendeForsendelseValidator;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.context.annotation.Bean;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ValidatorConfig {

	@Bean
	public MandatoryFieldsVerifier mandatoryFieldsVerifier() {
		return new DefaultMandatoryFieldsVerifier();
	}

	@Bean
	public JournalpostStructureVerifier journalpostStructureVerifier() {
		return new DefaultJournalpostStructureVerifier();
	}


	@Bean
	public DefaultJournalforInngaaendeForsendelseValidator journalforInngaaendeForsendelseValidator() {
		return new DefaultJournalforInngaaendeForsendelseValidator();
	}

}
