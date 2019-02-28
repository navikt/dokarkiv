package no.nav.dokarkiv.ferdigstilljournalpost.v1.rjoark201;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class JournalpostValidator {

	private List<String> manglendePaakrevdeFelter;

	JournalpostValidator() {
		manglendePaakrevdeFelter = new ArrayList<>();
	}

	void validateJournalpostTilstand(Journalpost journalpost) {
		verifyMidlertidigJournalfoert(journalpost);
		verifyDokumenttilstand(journalpost);
	}

	void validateJournalpostStruktur(Journalpost journalpost) {
		verifyExactlyOneHoveddokument(journalpost);
		verifyFildetaljerVariantFormat(journalpost);
	}

	void validatePaakrevdeFelter(Journalpost journalpost) {
		verifyPaakrevdeFelterJournalpost(journalpost);
		verifyPaakrevdeFelterSaksrelasjon(journalpost.getSaksrelasjon());
		verifyPaakrevdeFelterBruker(journalpost);
		verifyPaakrevdeFeterDokumentInfo(journalpost);
		if (!manglendePaakrevdeFelter.isEmpty()) {
			String manglendeFelter = StringUtils.join(manglendePaakrevdeFelter, ", ");
			throw new KanIkkeFerdigstilleException("Kan ikke ferdigstille journalpost, følgende felt(er) mangler: " + manglendeFelter);
		}
	}

	private void verifyMidlertidigJournalfoert(Journalpost jp) {
		if (!jp.hasMidlertidigInngaaendeJournalforingStatus() || Boolean.TRUE.equals(jp.getSaksrelasjon() == null ? Boolean.FALSE : jp.getSaksrelasjon()
				.getFeilregistrert())) {
			throw new JournalpostIkkeMidlertidigException(String.format("Journalpost med journalpostId=%s er ikke midlertidig journalført", jp
					.getJournalpostId()));
		}
	}

	private void verifyDokumenttilstand(Journalpost jp) {
		try {
			jp.verifyNoDokumentInfosUnderRedigering();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokumentUnderRedigeringException(String.format("Ett eller flere av dokumentene som forsøkes oppdatert på journalpost med journalpostId=%s er under redigering",
					jp.getJournalpostId()));
		}
	}

	private void verifyExactlyOneHoveddokument(Journalpost jp) {
		try {
			jp.verifyOnlyOneHoveddokument();
		} catch (InvalidJournalpostStructureException e) {
			throw new KanIkkeFerdigstilleException(String.format("Kan ikke ferdigstille: Journalpost med journalpostId=%s inneholder null eller flere enn ett hoveddokument",
					jp.getJournalpostId()));
		}
	}

	private void verifyFildetaljerVariantFormat(Journalpost jp) {
		try {
			jp.verifyArkivVariantOfAllDocuments();
		} catch (InvalidJournalpostStructureException e) {
			throw new KanIkkeFerdigstilleException(String.format("Kunne ikke endelig journalføre: Journalpost med journalpostId=%s mangler arkivvariant",
					jp.getJournalpostId()));
		}
		jp.getJournalpostDokumentInfoRelasjoner().forEach(dr -> {
			try {
				dr.getDokumentInfo().verifyNoVariantDuplicates();
			} catch (InvalidJournalpostStructureException e) {
				throw new KanIkkeFerdigstilleException(String.format("Kunne ikke endelig journalføre: Journalpost med journalpostId=%s inneholder flere fildetaljer med samme variantformat",
						jp.getJournalpostId()));
			}
		});
	}

	private void verifyPaakrevdeFelterJournalpost(Journalpost journalpost) {
		verifyFieldNotNull(journalpost, journalpost.getFagomrade(), "fagomrade");
		verifyStringNotBlank(journalpost, journalpost.getInnhold(), "innhold");
		verifyStringNotBlank(journalpost, journalpost.getAvsenderMottaker(), "avsendMottaker");
	}

	private void verifyPaakrevdeFeterDokumentInfo(Journalpost journalpost) {
		journalpost.getJournalpostDokumentInfoRelasjoner()
				.forEach(journalpostDokumentInfoRelasjon -> verifyMandatoryFelterDokumentinfo(journalpostDokumentInfoRelasjon.getDokumentInfo()));
	}

	private void verifyPaakrevdeFelterBruker(Journalpost journalpost) {
		journalpost.getBrukere().forEach(this::verifyMandatoryFelterBruker);
	}

	private void verifyPaakrevdeFelterSaksrelasjon(Saksrelasjon saksrelasjon) {
		if (saksrelasjon != null) {
			verifyStringNotBlank(saksrelasjon, saksrelasjon.getSakId(), "sakId");
			verifyFieldNotNull(saksrelasjon, saksrelasjon.getFagsystem(), "fagsystem");
		}
	}

	private void verifyMandatoryFelterBruker(Bruker bruker) {
		verifyStringNotBlank(bruker, bruker.getBrukerId(), "brukerId");
		verifyFieldNotNull(bruker, bruker.getBrukerType(), "brukerType");
	}

	private void verifyMandatoryFelterDokumentinfo(DokumentInfo dokumentInfo) {
		verifyFieldNotNull(dokumentInfo, dokumentInfo.getKategori(), "kategori");
		verifyStringNotBlank(dokumentInfo, dokumentInfo.getTittel(), "tittel");
	}

	/**
	 * Checks that a field is not null.
	 *
	 * @param fieldValue The value to check.
	 * @param fieldName  THe fieldName.
	 */
	private void verifyFieldNotNull(Object parent, Object fieldValue, String fieldName) {
		if (fieldValue == null) {
			manglendePaakrevdeFelter.add(parent.getClass().getSimpleName() + "." + fieldName);
		}
	}

	/**
	 * Checks that a String is not null or empty.
	 *
	 * @param fieldValue The String to check.
	 * @param fieldName  The fieldName.
	 */
	private void verifyStringNotBlank(Object parent, String fieldValue, String fieldName) {
		if (isBlank(fieldValue)) {
			manglendePaakrevdeFelter.add(parent.getClass().getSimpleName() + "." + fieldName);
		}
	}
}
