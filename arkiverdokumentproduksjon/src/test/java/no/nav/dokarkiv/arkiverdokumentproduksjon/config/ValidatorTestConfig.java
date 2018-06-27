package no.nav.dokarkiv.arkiverdokumentproduksjon.config;


import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.DefaultOpprettJournalpostArkiverDokumentValidator;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentValidator;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Spring config used to test the validator class for OpprettOgFerdigstillJournalpost
 *
 * @author Stig Strøm
 */
@Configuration
@Profile("test")
public class ValidatorTestConfig {

	@Bean
	MandatoryFieldsVerifier mandatoryFieldsVerifier() {
		return new DefaultMandatoryFieldsVerifier();
	}

	@Bean
	JournalpostStructureVerifier journalpostStructureVerifier() {
		return new DefaultJournalpostStructureVerifier();
	}

//	@Bean
//	public OpprettJournalpostArkiverDokumentValidator opprettOgFerdigstillJournalpostValidator() {
//		return new DefaultOpprettJournalpostArkiverDokumentValidator();
//	}

//	FIXME
//	@Bean
//	public JournalforInngaaendeForsendelseValidator journalforInngaaendeForsendelseValidator() {
//		return new DefaultJournalforInngaaendeForsendelseValidator();
//	}

//	FIXME
//	@Bean
//	public JournalforInngaaendeForsendelseV2Validator journalforInngaaendeForsendelseV2Validator() {
//		return new DefaultJournalforInngaaendeForsendelseV2Validator();
//	}
}
