package no.nav.dokarkiv.journalfoerinngaaende.v1.support;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KunneIkkeEndeligJournalfoereException;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;

public class JournalpostValidator {

	public static void validateJournalpostStatuser(Journalpost journalpost) {
		Utils.assertJournalpostIsInngaaende(journalpost);
		verifyMidlertidigJournalfoert(journalpost);
		verifyDokumentInfos(journalpost);
	}

	public static void validateJournalpostStrukturOgPaakrevdeAttributter(Journalpost journalpost) {
		verifyHoveddokument(journalpost);
		verifyFildetaljer(journalpost);
		verifyRequiredFields(journalpost);
	}

	private static void verifyMidlertidigJournalfoert(Journalpost jp) {
		if (!jp.hasMidlertidigInngaaendeJournalforingStatus() || Boolean.TRUE.equals(jp.getSaksrelasjon() == null ? Boolean.FALSE : jp.getSaksrelasjon()
				.getFeilregistrert())) {
			throw new JournalpostIkkeMidlertidigException(String.format("Journalpost med journalpostId=%s er ikke midlertidig journalført", jp
					.getJournalpostId()));
		}
	}

	private static void verifyDokumentInfos(Journalpost journalpost) {
		try {
			journalpost.verifyNoDokumentInfosUnderRedigering();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokumentUnderRedigeringException(String.format("Ett eller flere av dokumentene som forsøkes oppdatert på journalpost med journalpostId=%s er under redigering",
					journalpost.getJournalpostId()));
		}
	}

	private static void verifyHoveddokument(Journalpost journalpost) {
		try {
			journalpost.verifyOnlyOneHoveddokument();
		} catch (InvalidJournalpostStructureException e) {
			throw new KunneIkkeEndeligJournalfoereException(String.format("Kunne ikke endelig journalføre: Journalpost med journalpostId=%s inneholder null eller flere enn ett hoveddokument",
					journalpost.getJournalpostId()));
		}
	}

	private static void verifyFildetaljer(Journalpost journalpost) {
		try {
			journalpost.verifyArkivVariantOfAllDocuments();
		} catch (InvalidJournalpostStructureException e) {
			throw new KunneIkkeEndeligJournalfoereException(String.format("Kunne ikke endelig journalføre: Journalpost med journalpostId=%s mangler arkivvariant",
					journalpost.getJournalpostId()));
		}
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(dr -> {
			try {
				dr.getDokumentInfo().verifyNoVariantDuplicates();
			} catch (InvalidJournalpostStructureException e) {
				throw new KunneIkkeEndeligJournalfoereException(String.format("Kunne ikke endelig journalføre: Journalpost med journalpostId=%s inneholder flere fildetaljer med samme variantformat",
						journalpost.getJournalpostId()));
			}
		});
	}

	private static void verifyRequiredFields(Journalpost journalpost) {
		try {
			journalpost.verifyMandatoryFieldsSkipJournalforendeEnhetId();
		} catch (Exception e) {
			throw new KunneIkkeEndeligJournalfoereException(String.format("Kunne ikke endelig journalfoere: Journalpost med journalpostId=%s mangler paakrevde felt for endelig journalføring. %s",
					journalpost.getJournalpostId(), e.getMessage()));
		}
	}

}
