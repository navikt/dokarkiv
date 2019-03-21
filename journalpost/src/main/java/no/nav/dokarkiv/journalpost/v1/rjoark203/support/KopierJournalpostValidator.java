package no.nav.dokarkiv.journalpost.v1.rjoark203.support;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeKopiereException;
import no.nav.dokarkiv.journalpost.v1.api.KopierJournalpostRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KopierJournalpostValidator {

	public static final List<JournalStatusCode> COPYABLE_JOURNALSTATUS_LIST = Arrays.asList(FS, FL, E, J);

	public void validate(Journalpost journalpost, KopierJournalpostRequest request) {
		JournalStatusCode status = journalpost.getJournalstatus();

		// Verifisere at journalposten er i en tilstand som kan kopieres (status FL, FS, E eller J, eventuelt har en feilregistrert saksrelasjon)
		if (!journalpostHasCopyableStatus(status) || !journalpostHasFeilregistrertSaksrelasjon(journalpost)) {
			throw new KanIkkeKopiereException(String.format("Kan ikke kopiere journalpost med journalpostId=%s, journalpost har ugyldig status ELLER mangler feilregistrert saksrelasjon", journalpost.getJournalpostId()));
		}

		// valider at alle oppgitte dokumentInfoId-er faktisk finnes i tilknytning til journalposten
		List<Long> dokumentInfoIdList = journalpost.findAllDokumentInfos().stream().map(DokumentInfo::getDokumentInfoId).collect(Collectors.toList());
		if (!request.getGjenbrukDokumenter().stream().allMatch(dokumentInfoId -> dokumentInfoIdList.contains(Long.parseLong(dokumentInfoId)))) {
			throw new KanIkkeKopiereException(String.format("Kan ikke kopiere journalpost med journalpostId=%s, dokumentInfoId oppgitt finnes ikke på journalpost", journalpost.getJournalpostId()));
		}
	}

	private boolean journalpostHasCopyableStatus(JournalStatusCode status) {
		return COPYABLE_JOURNALSTATUS_LIST.contains(status);
	}

	private boolean journalpostHasFeilregistrertSaksrelasjon(Journalpost journalpost) {
		return journalpost.getSaksrelasjon() != null && journalpost.getSaksrelasjon().getFeilregistrert();
	}
}
