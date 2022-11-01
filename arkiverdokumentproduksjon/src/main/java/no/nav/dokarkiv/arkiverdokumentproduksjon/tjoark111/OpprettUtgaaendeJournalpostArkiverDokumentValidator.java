package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import com.google.common.base.Strings;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ValideringAvVedleggFeiletException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;
import static org.springframework.util.Assert.hasLength;

@Component
public class OpprettUtgaaendeJournalpostArkiverDokumentValidator {

	private static final List<JournalStatusCode> VEDLEGG_EXPECTED_JOURNALSTATUSES = Arrays.asList(JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E);
	private final MandatoryFieldsVerifier mandatoryFieldsVerifier;

	public OpprettUtgaaendeJournalpostArkiverDokumentValidator(MandatoryFieldsVerifier mandatoryFieldsVerifier) {
		this.mandatoryFieldsVerifier = mandatoryFieldsVerifier;
	}

	public void validateRequiredFields(final OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo) throws UgyldigInputException {
		StringBuilder message = new StringBuilder();
		addMessageIfTrue(Strings.isNullOrEmpty(requestTo.getJournalpost().getKanalReferanseId()), message, "kanalReferanseId");
		addMessageIfTrue(Strings.isNullOrEmpty(requestTo.getJournalpost().getOpprettetAvNavn()), message, "opprettetAvNavn");
		validateRequiredFieldsSaksrelasjon(requestTo.getJournalpost().getSaksrelasjon(), message);
		validateRequiredFieldsJournalpostDokumentInfoRelasjonFields(requestTo, message);
		validateRequiredFieldsVedlegg(requestTo.getVedleggList(), message);

		String finalMessage = message.toString();
		if (!finalMessage.isEmpty()) {
			throw new UgyldigInputException("tjoark111 Validering av input feilet: Mangler påkrevde attributter:" + finalMessage
					.substring(0, finalMessage.length() - 1));
		}
	}

	private void validateRequiredFieldsJournalpostDokumentInfoRelasjonFields(final OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo, StringBuilder message) {
		addMessageIfTrue(requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner().isEmpty(), message, "journalpostDokumentInfoRelasjoner");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> {
			addMessageIfTrue(relasjon.getTilknyttetJournalpostSom() == null, message, "JournalpostDokumentInfoRelasjoner.TilknyttetJournalpostSom");
			addMessageIfTrue(relasjon.getDokumentInfo() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo");
			if (relasjon.getDokumentInfo() != null) {
				validateRequiredFieldsDokumentInfo(relasjon.getDokumentInfo(), message);
			}
		});
	}

	private void validateRequiredFieldsSaksrelasjon(final Saksrelasjon saksrelasjon, StringBuilder message) {
		if (saksrelasjon == null) {
			return;
		}

		addMessageIfTrue(saksrelasjon.getSakId() == null, message, "Saksrelasjon.Saksnummer");
		addMessageIfTrue(saksrelasjon.getFagsystem() == null, message, "Saksrelasjon.Fagsystem");
	}

	private void validateRequiredFieldsDokumentInfo(final DokumentInfo dokumentInfo, StringBuilder message) {
		addMessageIfTrue(dokumentInfo.getKategori() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Kategori");
		addMessageIfTrue(dokumentInfo.getTittel() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Tittel");
		addMessageIfTrue(dokumentInfo.getDokumenttypeId() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.DokumenttypeId");
		addMessageIfTrue(dokumentInfo.getFildetaljerListe()
				.isEmpty(), message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");
		dokumentInfo.getFildetaljerListe().forEach(filDetaljer -> validateRequiredFieldsFildetaljer(filDetaljer, message));
	}

	private void validateRequiredFieldsFildetaljer(final FilDetaljer filDetaljer, StringBuilder message) {
		addMessageIfTrue(filDetaljer.getFiltype() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Filtype");
		addMessageIfTrue(filDetaljer.getVariantFormat() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.VariantFormat");
		addMessageIfTrue(filDetaljer.getFileContent() == null, message, "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.IkkeRedigerbartDokument");
	}

	private void validateRequiredFieldsVedlegg(final List<OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg> vedleggList, StringBuilder message) {
		vedleggList.forEach(vedlegg -> {
			addMessageIfTrue(vedlegg.getDokumentInfoId() == null, message, "Vedlegg.DokumentInfoId");
			addMessageIfTrue(vedlegg.getKnyttesFraJournalpostId() == null, message, "Vedlegg.KnyttesFraJournalpostId");
		});
	}

	public void validate(final Journalpost journalpost) {
		mandatoryFieldsVerifier.verifyFieldsSkipJournalForendeEnhetId(journalpost);
		validateJournalpost(journalpost);
		validateDokumentInfoRelasjonList(journalpost.getJournalpostDokumentInfoRelasjoner());
	}

	public void validateVariantFormaterAndHoveddokument(Journalpost journalpost) throws UgyldigInputException {
		try {
			journalpost.verifyArkivVariantOfAllDocuments();
			journalpost.verifyNoDokumentVariantDuplicates();
			journalpost.verifyOnlyOneHoveddokument();
		} catch (IllegalArgumentException | InvalidJournalpostStructureException e) {
			throw new UgyldigInputException("tjoark111 Validering av input feilet: " + e.getMessage());
		}
	}

	private void validateJournalpost(Journalpost journalpost) {
		notNull(journalpost.getDokumentDato(), "Mangler påkrevd attributt: Journalpost.DokumentDato");
		notNull(journalpost.getUtsendingskanal(), "Mangler påkrevd attributt: Journalpost.Utsendingskanal");
		isFalse(Strings.isNullOrEmpty(journalpost.getJournalForendeEnhetId()), "Mangler påkrevd attributt: Journalpost.JournalForendeEnhetId");
	}

	private void validateDokumentInfoRelasjonList(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonList) {
		for (JournalpostDokumentInfoRelasjon jdir : dokumentInfoRelasjonList) {
			hasLength(jdir.getDokumentInfo()
					.getDokumenttypeId(), "Mangler påkrevd attributt: DokumentInfo.DokumenttypeId");
		}
	}

	public void validateVedlegg(Journalpost originalJournalpost, DokumentInfo dokumentInfo, OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg vedlegg) throws ValideringAvVedleggFeiletException {

		try {
			validateVedleggOriginalJournalpost(originalJournalpost);
			validateVedleggDokumentInfo(dokumentInfo);
			validateVedleggFildetaljer(dokumentInfo);
		} catch (IllegalArgumentException e) {
			throw new ValideringAvVedleggFeiletException(String.format("tjoark111 Validering av vedlegg med dokumentInfoId=%d feilet: %s. vedleggKnyttesFraJournalpostId=%s", vedlegg
					.getDokumentInfoId(), e.getMessage(), vedlegg.getKnyttesFraJournalpostId()));
		}
	}

	public void validateVedleggOriginalJournalpost(Journalpost journalpost) {
		isTrue(VEDLEGG_EXPECTED_JOURNALSTATUSES.contains(journalpost.getJournalstatus()), String.format("Journalpost.JournalStatus kan ikke være %s", journalpost
				.getJournalstatus()));
		isFalse(journalpost.getSaksrelasjon() == null ? null : journalpost.getSaksrelasjon()
				.getFeilregistrert(), "Journalpost.Saksrelasjon.Feilregistert kan ikke være Sann");
	}

	public void validateVedleggDokumentInfo(DokumentInfo dokumentInfo) {

		isTrue(DokumentStatusCode.FERDIGSTILT == dokumentInfo.getDokumentstatus(), String.format("DokumentInfo.Dokumentstatus må være FERDIGSTILT men var %s", dokumentInfo
				.getDokumentstatus()));
		isFalse(dokumentInfo.getOrganInternt(), "DokumentInfo.OrganInternt kan ikke være Sann");
		isFalse(dokumentInfo.getInnskrenketPartsinnsyn(), "DokumentInfo.InnskrenketPartsinnsyn kan ikke være Sann");
		isFalse(dokumentInfo.getInnskrenketPartsinnsynFraTredjepart(), "DokumentInfo.InnskrenketPartsinnsynFraTredjepart kan ikke være Sann");
	}

	public void validateVedleggFildetaljer(DokumentInfo dokumentInfo) {
		isTrue(dokumentInfo.hasArkivFormat(), "Vedlegg mangler Fildetaljer med variantFormat=ARKIV");
		isTrue(!hasFildetaljerWithOnDemandId(dokumentInfo), "Fildetaljer.OnDemandId kan ikke være satt");
	}

	private boolean hasFildetaljerWithOnDemandId(DokumentInfo dokumentInfo) {
		return dokumentInfo.getFildetaljerListe().stream().anyMatch(filDetaljer -> (filDetaljer.getOnDemandId() != null && filDetaljer.getOnDemandInstans() != null));
	}

	private void isFalse(Boolean statement, String message) {

		if (statement != null && statement) {
			throw new IllegalArgumentException(message);
		}
	}

	private void addMessageIfTrue(Boolean statement, StringBuilder message, String variableName) {

		if (statement != null && statement) {
			message.append(" " + variableName + ",");
		}
	}
}
