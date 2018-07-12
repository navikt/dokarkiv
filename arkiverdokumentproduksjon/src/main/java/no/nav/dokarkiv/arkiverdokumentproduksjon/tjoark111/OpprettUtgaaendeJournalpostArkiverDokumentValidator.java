package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static org.apache.commons.lang.Validate.notNull;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.springframework.util.Assert.hasLength;

import com.google.common.base.Strings;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Validates Journalpost for OpprettOgFerdigstillJournalpost
 *
 * @author Stig Strøm
 */
@Component
public class OpprettUtgaaendeJournalpostArkiverDokumentValidator {

	@Inject
	protected MandatoryFieldsVerifier mandatoryFieldsVerifier;

	private List<JournalStatusCode> VEDLEGG_EXPECTED_JOURNALSTATUSES = Arrays.asList(JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E);

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
		notNull(journalpost.getUtsendingskanal(), "Missing required field in request: Journalpost.Utsendingskanal");
		isFalse(Strings.isNullOrEmpty(journalpost.getJournalForendeEnhetId()), "Missing required field in request: Journalpost.JournalForendeEnhetId");
	}

	private void validateDokumentInfoRelasjonList(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonList) {
		for (JournalpostDokumentInfoRelasjon jdir : dokumentInfoRelasjonList) {
			hasLength(jdir.getDokumentInfo()
					.getDokumenttypeId(), "Missing required field in request: DokumentInfo.DokumenttypeId");
		}
	}

	public void validateVedleggOriginalJournalpost(Journalpost journalpost, Long dokumentInfoId) {
		isTrue(VEDLEGG_EXPECTED_JOURNALSTATUSES.contains(journalpost.getJournalstatus()), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. Journalpost.JournalStatus kan ikke være %s", dokumentInfoId, journalpost
				.getJournalstatus()));
		isFalse(journalpost.getSaksrelasjon() == null ? null : journalpost.getSaksrelasjon()
				.getFeilregistrert(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. Journalpost.Saksrelasjon.Feilregistert kan ikke være True", dokumentInfoId));
	}

	public void validateVedleggDokumentInfo(DokumentInfo dokumentInfo) {

		isTrue(DokumentStatusCode.FERDIGSTILT == dokumentInfo.getDokumentstatus(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. DokumentInfo.Dokumentstatus må være FERDIGSTILT men var %s", dokumentInfo
				.getDokumentInfoId(), dokumentInfo.getDokumentstatus()));
		isFalse(dokumentInfo.getSlettet(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. DokumentInfo.slettet kan ikke være True", dokumentInfo
				.getDokumentInfoId()));
		isFalse(dokumentInfo.getOrganInternt(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. DokumentInfo.OrganInternt kan ikke være True", dokumentInfo
				.getDokumentInfoId()));
		isFalse(dokumentInfo.getInnskrenketPartsinnsyn(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. DokumentInfo.innskrenketPartsinnsyn kan ikke være True", dokumentInfo
				.getDokumentInfoId()));
		isFalse(dokumentInfo.getInnskrenketPartsinnsynFraTredjepart(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. DokumentInfo.innskrenketPartsinnsynFraTredjepart kan ikke være True", dokumentInfo
				.getDokumentInfoId()));
	}

	public void validateVedleggFildetaljer(DokumentInfo dokumentInfo) {
		isTrue(dokumentInfo.hasArkivFormat(), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. Vedlegg mangler Fildetaljer med variantFormat=ARKIV", dokumentInfo
				.getDokumentInfoId()));
		isTrue(!hasFildetaljerWithOnDemandId(dokumentInfo), String.format("Validering av vedlegg med dokumentInfoId=%d feilet. Fildetaljer.OnDemandId kan ikke være satt", dokumentInfo
				.getDokumentInfoId()));
	}

	private boolean hasFildetaljerWithOnDemandId(DokumentInfo dokumentInfo) {
		return dokumentInfo.getFildetaljerListe().stream().anyMatch(filDetaljer -> filDetaljer.getOnDemandId() != null);
	}

	private void isFalse(Boolean statement, String message) {
		statement = statement == null ? false : statement;
		if (statement) {
			throw new IllegalArgumentException(message);
		}
	}

}
