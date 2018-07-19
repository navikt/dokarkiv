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
	private MandatoryFieldsVerifier mandatoryFieldsVerifier;

	public void validate(final Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFieldsSkipJournalForendeEnhetId(journalpost);
		validateJournalpost(journalpost);
		validateDokumentInfoRelasjonList(journalpost.getJournalpostDokumentInfoRelasjoner());
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
