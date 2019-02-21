package no.nav.dokarkiv.ferdigstilljournalpost.v1.ferdigstill;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KanIkkeFerdigstilleException;
import org.springframework.stereotype.Component;

@Component
class JournalpostValidator {

	void validateJournalpostTilstand(Journalpost journalpost) {
		verifyMidlertidigJournalfoert(journalpost);
		verifyDokumenttilstand(journalpost);
	}

	void validateJournalpostStruktur(Journalpost journalpost) {
		verifyExactlyOneHoveddokument(journalpost);
		verifyFildetaljerVariantFormat(journalpost);
	}

	void validatePaakrevdeFelter(Journalpost journalpost) {
		// TODO: skal vi gjøre dette _før_ endelig JF eller etter felter har blitt satt?
		journalpost.verifyMandatoryFields();
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
}
