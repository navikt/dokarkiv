package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;


import static org.apache.commons.lang.Validate.notNull;
import static org.springframework.util.Assert.hasLength;

import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journabehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journabehandling.MandatoryFieldsVerifier;
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
 * @author Stig Str�m
 * @author Leo-Andreas Ervik
 */
@Component
public class DefaultJournalforInngaaendeForsendelseValidator implements JournalforInngaaendeForsendelseValidator {

	@Inject
	protected JournalpostStructureVerifier verifier;

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	@Override
	public void validate(final Journalpost journalpost, boolean verifyStructure) {
		mandatoryFieldsVerifier.verifyFields(journalpost);
		if (verifyStructure) {
			verifier.verifyJournalpostStructure(journalpost);
		}
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
	 * @param filDetaljerList the list of {@link FilDetaljer} in the {@link no.nav.dokarkiv.core.domain.entities.Journalpost} in the request
	 */
	private void validateFildetaljerList(Set<FilDetaljer> filDetaljerList) {
		for (FilDetaljer filDetaljer : filDetaljerList) {
			if (ArrayUtils.isEmpty(filDetaljer.getFileContent())) {
				throw new IllegalArgumentException("Missing required field in request: FilDetaljer.FileContent");
			}
		}
	}
}