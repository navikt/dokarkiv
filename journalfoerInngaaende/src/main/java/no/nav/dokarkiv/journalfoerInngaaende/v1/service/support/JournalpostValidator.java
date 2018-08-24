package no.nav.dokarkiv.journalfoerInngaaende.v1.service.support;

import static org.apache.cxf.common.util.StringUtils.isEmpty;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.springframework.http.HttpStatus;

public class JournalpostValidator {

	public static void validateJournalpostStatuser(Journalpost journalpost) {
		if (!journalpost.isInngaende()) {
			throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngaaende", HttpStatus.BAD_REQUEST);
		}
		verifyMidlertidigJournalfoert(journalpost);

		verifyDokumentInfos(journalpost);
	}

	public static void validateJournalpostStrukturOgPaakrevdeAttributter(Journalpost journalpost) {
		verifyHoveddokument(journalpost);
		verifyFildetaljer(journalpost);

		verifyRequiredFields(journalpost);
	}

	private static void verifyMidlertidigJournalfoert(Journalpost jp) {
		if (!jp.hasMidlertidigInngaaendeJournalforingStatus() || Boolean.TRUE.equals(jp.getSaksrelasjon().getFeilregistrert())) {
			throw new DokarkivRestFunctionalException("Journalposten er ikke midlertidig journalført", HttpStatus.BAD_REQUEST);
		}
	}

	private static void verifyDokumentInfos(Journalpost journalpost) {
		try {
			journalpost.verifyNoDokumentInfosUnderRedigering();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokarkivRestFunctionalException("Ett eller flere av dokumentene som forsøkes oppdatert er under redigering", HttpStatus.BAD_REQUEST);
		}
	}

	private static void verifyHoveddokument(Journalpost journalpost) {
		try {
			journalpost.verifyOnlyOneHoveddokument();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokarkivRestFunctionalException("Journalpost inneholder ikke ett hoveddokument", HttpStatus.BAD_REQUEST);
		}
	}

	private static void verifyFildetaljer(Journalpost journalpost) {
		try {
			journalpost.verifyArkivVariantOfAllDocuments();
		} catch (InvalidJournalpostStructureException e) {
			throw new DokarkivRestFunctionalException("Det mangler arkivvariant, dette er påkrevd for å ferdigstille journalposter", HttpStatus.BAD_REQUEST);
		}
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(dr -> {
			try {
				dr.getDokumentInfo().verifyNoVariantDuplicates();
			} catch (InvalidJournalpostStructureException e) {
				throw new DokarkivRestFunctionalException("Journalpost inneholder flere fildetaljer med samme variantformat", HttpStatus.BAD_REQUEST);
			}
		});
	}

	private static void verifyRequiredFields(Journalpost journalpost) {
		try {
			journalpost.verifyMandatoryFieldsSkipJournalforendeEnhetId();
		} catch (Exception e) {
			throw new DokarkivRestFunctionalException("Journalpost mangler påkrevde felt for endelig journalføring", HttpStatus.BAD_REQUEST);
		}
//		if (journalpost.getSaksrelasjon() == null || (isEmpty(journalpost.getSaksrelasjon().getSakId()) || journalpost.getSaksrelasjon().getFagsystem() == null)) {
//			throw new DokarkivRestFunctionalException("Journalpost mangler påkrevde felt for endelig journalføring", HttpStatus.BAD_REQUEST);
//		}
//		if (journalpost.findAllDokumentInfos().stream().anyMatch(dokumentInfo -> dokumentInfo.getKategori() == null || dokumentInfo.getTittel() == null)){
//			throw new DokarkivRestFunctionalException("Journalpost mangler påkrevde felt for endelig journalføring", HttpStatus.BAD_REQUEST);
//		}
	}
}
