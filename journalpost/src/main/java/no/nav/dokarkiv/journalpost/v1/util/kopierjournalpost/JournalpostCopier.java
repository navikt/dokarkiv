package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

public class JournalpostCopier {

	public Journalpost copy(Journalpost journalpost) {
		Journalpost kopiertJournalpost = journalpost.toBuilder()
				.journalpostId(null)
				.opprettetAvNavn(null)
				.tilleggsopplysninger(copyTilleggsopplysninger(journalpost.getTilleggsopplysninger()))
				.saksrelasjon(null)
				.kanalReferanseId(copyKanalReferanseId(journalpost.getKanalReferanseId()))
				.build();

		kopiertJournalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		kopiertJournalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
		kopiertJournalpost.setJournalfortAvNavn(MDC.get(MDC_USER_ID));
		kopiertJournalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		kopiertJournalpost.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		kopiertJournalpost.setSaksrelasjon(copySaksrelasjon(kopiertJournalpost, journalpost.getSaksrelasjon()));

		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(
				journalpostDokumentInfoRelasjon ->
						kopiertJournalpost.addJournalpostDokumentInfoRelasjon(copyJournalpostDokumentInfoRelasjon(kopiertJournalpost, journalpostDokumentInfoRelasjon))
		);

		for (Bruker bruker : journalpost.getBrukere()) {
			kopiertJournalpost.addBruker(cloneBruker(bruker));
		}

		journalpost.getKryssreferanser().forEach(kopiertJournalpost::addKryssReferanse);

		return kopiertJournalpost;
	}

	private String copyKanalReferanseId(String kanalReferanseId) {
		if (kanalReferanseId != null) {
			return kanalReferanseId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		}
		return null;
	}

	private Bruker cloneBruker(Bruker originalBruker) {
		Bruker nyBruker = originalBruker.toBuilder().brukerInfoId(null).build();
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		nyBruker.setEndretKildeNavn(consumerId);
		nyBruker.setOpprettetKildeNavn(consumerId);
		return nyBruker;
	}

	private Map<String, String> copyTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		HashMap<String, String> kopiertTilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.forEach(kopiertTilleggsopplysninger::put);
		return kopiertTilleggsopplysninger;
	}

	private Saksrelasjon copySaksrelasjon(Journalpost kopiertJournalpost, Saksrelasjon saksrelasjon) {
		Saksrelasjon kopiertSaksrelasjon = Saksrelasjon.builder()
				.fagsystem(saksrelasjon.getFagsystem())
				.sakId(saksrelasjon.getSakId())
				.journalpost(kopiertJournalpost)
				.endretAvNavn(MDC.get(MDC_USER_ID))
				.feilregistrert(saksrelasjon.getFeilregistrert())
				.build();
		kopiertSaksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		kopiertSaksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return kopiertSaksrelasjon;
	}

	private JournalpostDokumentInfoRelasjon copyJournalpostDokumentInfoRelasjon(Journalpost kopiertJournalpost, JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
		JournalpostDokumentInfoRelasjon kopiertJournalpostdokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(kopiertJournalpost)
				.dokumentInfo(journalpostDokumentInfoRelasjon.getDokumentInfo())
				.tilknyttetJournalpostSom(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom())
				.tilknyttetAvNavn(MDC.get(MDC_CONSUMER_ID))
				.build();
		kopiertJournalpostdokumentInfoRelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		kopiertJournalpostdokumentInfoRelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return kopiertJournalpostdokumentInfoRelasjon;
	}
}
