package no.nav.dokarkiv.journalpost.v1.rjoark201;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class JournalpostValidator {

	private static final List<JournalStatusCode> MIDLERTIDIG_JOURNALSTATUS = Arrays.asList(M, MO, UB, D, R);

	JournalpostValidator() {
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
		List<String> manglendePaakrevdeFelter = new ArrayList<>();
		verifyPaakrevdeFelterJournalpost(journalpost, manglendePaakrevdeFelter);
		verifyPaakrevdeFelterSaksrelasjon(journalpost.getSaksrelasjon(), manglendePaakrevdeFelter);
		verifyPaakrevdeFelterBruker(journalpost, manglendePaakrevdeFelter);
		verifyPaakrevdeFelterDokumentInfo(journalpost, manglendePaakrevdeFelter);
		if (!manglendePaakrevdeFelter.isEmpty()) {
			String manglendeFelter = StringUtils.join(manglendePaakrevdeFelter, ", ");
			throw new KanIkkeFerdigstilleException("Kan ikke ferdigstille journalpost, følgende felt(er) mangler: " + manglendeFelter);
		}
	}

	private void verifyMidlertidigJournalfoert(Journalpost jp) {
		if (MIDLERTIDIG_JOURNALSTATUS.contains(jp.getJournalstatus()) || Boolean.TRUE.equals(jp.getSaksrelasjon() == null ? Boolean.FALSE : jp.getSaksrelasjon()
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

	private void verifyPaakrevdeFelterJournalpost(Journalpost journalpost, List<String> manglendePaakrevdeFelter) {
		verifyFieldNotNull(journalpost, journalpost.getFagomrade(), "fagomrade", manglendePaakrevdeFelter);
		verifyStringNotBlank(journalpost, journalpost.getInnhold(), "innhold", manglendePaakrevdeFelter);
		if (!JournalpostTypeCode.N.equals(journalpost.getJournalposttype())) {
			verifyStringNotBlank(journalpost, journalpost.getAvsenderMottaker(), "avsendMottaker", manglendePaakrevdeFelter);
		}
		if (JournalpostTypeCode.I.equals(journalpost.getJournalposttype())) {
			verifyFieldNotNull(journalpost, journalpost.getMottakskanal(), "mottakskanal", manglendePaakrevdeFelter);
		}
	}

	private void verifyPaakrevdeFelterDokumentInfo(Journalpost journalpost, List<String> manglendePaakrevdeFelter) {
		journalpost.getJournalpostDokumentInfoRelasjoner()
				.forEach(journalpostDokumentInfoRelasjon -> verifyMandatoryFelterDokumentinfo(journalpostDokumentInfoRelasjon.getDokumentInfo(), manglendePaakrevdeFelter));
	}

	private void verifyPaakrevdeFelterBruker(Journalpost journalpost, List<String> manglendePaakrevdeFelter) {
		journalpost.getBrukere().forEach(bruker -> {
			verifyStringNotBlank(bruker, bruker.getBrukerId(), "brukerId", manglendePaakrevdeFelter);
			verifyFieldNotNull(bruker, bruker.getBrukerType(), "brukerType", manglendePaakrevdeFelter);
		});
	}

	private void verifyPaakrevdeFelterSaksrelasjon(Saksrelasjon saksrelasjon, List<String> manglendePaakrevdeFelter) {
		if (saksrelasjon != null) {
			verifyStringNotBlank(saksrelasjon, saksrelasjon.getSakId(), "sakId", manglendePaakrevdeFelter);
			verifyFieldNotNull(saksrelasjon, saksrelasjon.getFagsystem(), "fagsystem", manglendePaakrevdeFelter);
		}
	}

	private void verifyMandatoryFelterDokumentinfo(DokumentInfo dokumentInfo, List<String> manglendePaakrevdeFelter) {
		verifyFieldNotNull(dokumentInfo, dokumentInfo.getKategori(), "kategori", manglendePaakrevdeFelter);
		verifyStringNotBlank(dokumentInfo, dokumentInfo.getTittel(), "tittel", manglendePaakrevdeFelter);
	}

	/**
	 * Checks that a field is not null.
	 *
	 * @param fieldValue The value to check.
	 * @param fieldName  The fieldName.
	 */
	private void verifyFieldNotNull(Object parent, Object fieldValue, String fieldName, List<String> manglendePaakrevdeFelter) {
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
	private void verifyStringNotBlank(Object parent, String fieldValue, String fieldName, List<String> manglendePaakrevdeFelter) {
		if (isBlank(fieldValue)) {
			manglendePaakrevdeFelter.add(parent.getClass().getSimpleName() + "." + fieldName);
		}
	}
}
