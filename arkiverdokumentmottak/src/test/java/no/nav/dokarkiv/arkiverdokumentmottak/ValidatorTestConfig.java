package no.nav.dokarkiv.arkiverdokumentmottak;

import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1.JournalforInngaaendeForsendelseValidator;
import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.JournalforInngaaendeForsendelseV2Validator;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.context.annotation.Bean;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ValidatorTestConfig {

	@Bean
	public MandatoryFieldsVerifier mandatoryFieldsVerifier() {
		return new DefaultMandatoryFieldsVerifier();
	}

	@Bean
	public JournalpostStructureVerifier journalpostStructureVerifier() {
		return new DefaultJournalpostStructureVerifier();
	}


	@Bean
	public JournalforInngaaendeForsendelseValidator journalforInngaaendeForsendelseValidator() {
		return new JournalforInngaaendeForsendelseValidator();
	}


	@Bean
	public JournalforInngaaendeForsendelseV2Validator journalforInngaaendeForsendelseV2Validator() {
		return new JournalforInngaaendeForsendelseV2Validator();
	}

}
