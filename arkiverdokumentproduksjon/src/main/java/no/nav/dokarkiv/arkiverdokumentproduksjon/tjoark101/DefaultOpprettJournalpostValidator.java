package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static org.apache.commons.lang.StringUtils.isBlank;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Validates Journalpost for OpprettJournalpost
 *
 * @author Stig Strøm
 */
@Component
public class DefaultOpprettJournalpostValidator implements
		OpprettJournalpostValidator {

	@Inject
	protected JournalpostStructureVerifier verifier;

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	@Inject
	protected OpprettJournalpostPostUpdateVerifier postUpdateVerifier;

	@Override
	public void validate(final Journalpost journalpost) {
		verifier.verifyJournalpostStructure(journalpost);
		mandatoryFieldsVerifier.verifyFields(journalpost);

		if (isBlank(journalpost.getJournalForendeEnhetId())) {
			throw new ApplicationException(
					"Field journalfoerendeEnhetId must be set");
		}

		validateCustomDokumentInfoValues(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		postUpdateVerifier.validate(journalpost);
	}


	private void validateCustomDokumentInfoValues(final DokumentInfo dokumentInfo) {
		if (dokumentInfo == null) {
			throw new ApplicationException("dokumentInfo must be set");
		}

		if (StringUtils.isBlank(dokumentInfo.getBrevkode())) {
			throw new ApplicationException("Brevkode must be set");
		}

		if (StringUtils.isBlank(dokumentInfo.getDokumenttypeId())) {
			throw new ApplicationException("DokumenttypeId must be set");
		}

		if (dokumentInfo.getFildetaljerListe() == null || dokumentInfo.getFildetaljerListe().size() == 0) {
			throw new ApplicationException("Fildetaljer must be set");
		}

		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			validateCustomFildetaljer(filDetaljer);
		}
	}

	private void validateCustomFildetaljer(final FilDetaljer filDetaljer) {
		if (filDetaljer.getMetaforceInstanceId() == null || filDetaljer.getMetaforceInstanceId() == 0) {
			throw new ApplicationException("MetaforceInstanceId must be set");
		}
	}
}
