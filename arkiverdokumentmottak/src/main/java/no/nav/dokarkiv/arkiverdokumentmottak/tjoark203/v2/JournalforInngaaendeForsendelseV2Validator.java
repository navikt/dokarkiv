package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static org.apache.commons.lang.Validate.notNull;
import static org.springframework.util.Assert.hasLength;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Set;

/**
 * Validator class for JournalforInngaaendeForsendelseV2 (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class JournalforInngaaendeForsendelseV2Validator {

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	private static final String DUMMY_STRING = "dummy_null";

	public void validate(final Journalpost journalpost) {
		verifyFieldsSkipJournalForendeEnhetId(journalpost);
		validateJournalpost(journalpost);
		validateDokumentInfoRelasjonList(journalpost.getJournalpostDokumentInfoRelasjoner());
	}

	private void verifyFieldsSkipJournalForendeEnhetId(final Journalpost journalpost) {

		//Skip verify of JournalForendeEnhetId by using dummy value
		if (journalpost.getJournalForendeEnhetId() == null) {
			journalpost.setJournalForendeEnhetId(DUMMY_STRING);
		}

		mandatoryFieldsVerifier.verifyFields(journalpost);

		if (journalpost.getJournalForendeEnhetId().equals(DUMMY_STRING)) {
			journalpost.setJournalForendeEnhetId(null);
		}
	}

	public void validateVariantFormaterAndHoveddokument(Journalpost journalpost) {
		journalpost.verifyArkivVariantOfAllDocuments();
		journalpost.verifyNoDokumentVariantDuplicates();
		journalpost.verifyOnlyOneHoveddokument();
	}

	private void validateJournalpost(Journalpost journalpost) {
		notNull(journalpost.getDokumentDato(), "Missing required field in request: Journalpost.DokumentDato");
		notNull(journalpost.getMottattDato(), "Missing required field in request: Journalpost.MottatDato");
		notNull(journalpost.getMottakskanal(), "Missing required field in request: Journalpost.Mottakskanal");
	}

	private void validateDokumentInfoRelasjonList(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonList) {
		for (JournalpostDokumentInfoRelasjon jdir : dokumentInfoRelasjonList) {
			hasLength(jdir.getDokumentInfo()
					.getDokumenttypeId(), "Missing required field in request: DokumentInfo.DokumenttypeId");
		}
	}
}
