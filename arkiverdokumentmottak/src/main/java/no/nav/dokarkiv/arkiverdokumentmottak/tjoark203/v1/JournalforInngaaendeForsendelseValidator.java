package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;


import static org.apache.commons.lang.Validate.notNull;
import static org.springframework.util.Assert.hasLength;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;

/**
 * Implementation of JournalforInngaaendeForsendelseValidator
 * <p>
 * Check for {@code lists == null || object == null} is validated by {@link JournalforInngaaendeForsendelseRequestTo}
 *
 * @author Stig Strøm
 * @author Leo-Andreas Ervik
 */
@Component
public class JournalforInngaaendeForsendelseValidator {

	@Inject
	private JournalpostStructureVerifier verifier;

	@Inject
	private MandatoryFieldsVerifier mandatoryFieldsVerifier;

	public void validate(final Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFields(journalpost);
		verifier.verifyJournalpostStructure(journalpost);
		validateJournalpost(journalpost);
		validateDokumentInfoRelasjonList(journalpost.getJournalpostDokumentInfoRelasjoner());
	}

	/**
	 * Validates {@link Journalpost}
	 *
	 * @param journalpost the Journalpost to be validated
	 */
	private void validateJournalpost(Journalpost journalpost) {
		notNull(journalpost.getJournalForendeEnhetId(), "Missing required field in request: Journalpost.JournalForendeEnhetId");
		notNull(journalpost.getDokumentDato(), "Missing required field in request: Journalpost.DokumentDato");
		notNull(journalpost.getMottattDato(), "Missing required field in request: Journalpost.MottatDato");
		notNull(journalpost.getMottakskanal(), "Missing required field in request: Journalpost.Mottakskanal");
	}

	/**
	 * Ensures that the request contains one, and only one {@link TilknyttetJournalpostSomCode} HOVEDDOKUMENT.
	 * Calls futher validation of {@link DokumentInfo} and {@link JournalpostDokumentInfoRelasjon}
	 *
	 * @param dokumentInfoRelasjonList the list of {@link JournalpostDokumentInfoRelasjon} to be validated.
	 */
	private void validateDokumentInfoRelasjonList(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonList) {
		for (JournalpostDokumentInfoRelasjon jdir : dokumentInfoRelasjonList) {
			validateJournalpostDokumentInfoRelasjon(jdir);
			validateFildetaljerList(jdir.getDokumentInfo().getFildetaljerListe());
			validateDokumentInfo(jdir.getDokumentInfo());
		}
	}

	/**
	 * Validates {@link JournalpostDokumentInfoRelasjon}
	 *
	 * @param jdir the JournalpostDokumentInfoRelasjon to be validated
	 */
	private void validateJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon jdir) {
		if (!Arrays.asList(TilknyttetJournalpostSomCode.values()).contains(jdir.getTilknyttetJournalpostSom())) {
			throw new IllegalArgumentException(
					"TilknyttetJournalpostSomCode must match codes defined in the TilknyttetJournalpostSomCode domain enum");
		}
	}

	/**
	 * Validates {@link DokumentInfo}
	 *
	 * @param dokumentInfo the DokumentInfo to be validated
	 */
	private void validateDokumentInfo(DokumentInfo dokumentInfo) {
		hasLength(dokumentInfo.getDokumenttypeId(), "Missing required field in request: DokumentInfo.DokumenttypeId");
	}

	/**
	 * Validates {@link FilDetaljer}
	 *
	 * @param filDetaljerList the list of {@link FilDetaljer} in the {@link Journalpost} in the request
	 */
	private void validateFildetaljerList(Set<FilDetaljer> filDetaljerList) {
		for (FilDetaljer filDetaljer : filDetaljerList) {
			if (ArrayUtils.isEmpty(filDetaljer.getFileContent())) {
				throw new IllegalArgumentException("Missing required field in request: FilDetaljer.FileContent");
			}
		}
	}
}