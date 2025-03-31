package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak.Dokument;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;

public class JournalpostCopier {

	public Journalpost copy(Journalpost journalpost, String eksternReferanseId, List<Dokument> dokumenter) {
		Journalpost kopiertJournalpost = journalpost.toBuilder()
				.journalpostId(null)
				.opprettetAvNavn(null)
				.tilleggsopplysninger(copyTilleggsopplysninger(journalpost.getTilleggsopplysninger()))
				.saksrelasjon(null)
				.kanalReferanseId(mapKanalReferanseId(journalpost, eksternReferanseId))
				.build();

		kopiertJournalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		kopiertJournalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
		kopiertJournalpost.setJournalfortAvNavn(MDC.get(MDC_USER_ID));
		kopiertJournalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		kopiertJournalpost.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));

		kopiertJournalpost.setSaksrelasjon(copySaksrelasjon(kopiertJournalpost, journalpost.getSaksrelasjon()));

		kopierDokumentinfoRelasjoner(journalpost, dokumenter, kopiertJournalpost);

		for (Bruker bruker : journalpost.getBrukere()) {
			kopiertJournalpost.addBruker(cloneBruker(bruker));
		}

		journalpost.getKryssreferanser().forEach(kopiertJournalpost::addKryssReferanse);

		return kopiertJournalpost;
	}

	private void kopierDokumentinfoRelasjoner(Journalpost journalpost, List<Dokument> dokumenter, Journalpost kopiertJournalpost) {

		if (dokumenter == null) {
			journalpost.getJournalpostDokumentInfoRelasjoner().forEach(
					journalpostDokumentInfoRelasjon ->
							kopiertJournalpost.addJournalpostDokumentInfoRelasjon(copyJournalpostDokumentInfoRelasjon(kopiertJournalpost, journalpostDokumentInfoRelasjon)));
		} else {
			Map<Long, JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonMap = journalpost.getJournalpostDokumentInfoRelasjoner().stream()
					.collect(HashMap::new, (map, relasjon) -> map.put(relasjon.getDokumentInfo().getDokumentInfoId(), relasjon), HashMap::putAll);

			var hoveddokument = dokumentInfoRelasjonMap.get(Long.parseLong(dokumenter.getFirst().dokumentInfoId()));
			kopiertJournalpost.addJournalpostDokumentInfoRelasjon(copyJournalpostDokumentInfoRelasjon(kopiertJournalpost, hoveddokument, HOVEDDOKUMENT));

			dokumenter.stream()
					.skip(1) //Første element i dokumenter skal være hoveddokument
					.map(Dokument::dokumentInfoId)
					.map(Long::parseLong)
					.map(dokumentInfoRelasjonMap::get)
					.forEach(relasjon -> kopiertJournalpost.addJournalpostDokumentInfoRelasjon(copyJournalpostDokumentInfoRelasjon(kopiertJournalpost, relasjon, VEDLEGG)));
		}
	}

	private String mapKanalReferanseId(Journalpost journalpost, String eksternReferanseId) {
		return eksternReferanseId == null ? copyKanalReferanseId(journalpost.getKanalReferanseId()) : eksternReferanseId;
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

	private JournalpostDokumentInfoRelasjon copyJournalpostDokumentInfoRelasjon(Journalpost kopiertJournalpost,
																				JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
		return copyJournalpostDokumentInfoRelasjon(kopiertJournalpost, journalpostDokumentInfoRelasjon, null);
	}

	private JournalpostDokumentInfoRelasjon copyJournalpostDokumentInfoRelasjon(Journalpost kopiertJournalpost,
																				JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon,
																				TilknyttetJournalpostSomCode tilknyttetJournalpostSom) {
		JournalpostDokumentInfoRelasjon kopiertJournalpostdokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(kopiertJournalpost)
				.dokumentInfo(journalpostDokumentInfoRelasjon.getDokumentInfo())
				.tilknyttetJournalpostSom(tilknyttetJournalpostSom != null ? tilknyttetJournalpostSom : journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom())
				.tilknyttetAvNavn(MDC.get(MDC_CONSUMER_ID))
				.build();
		kopiertJournalpostdokumentInfoRelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		kopiertJournalpostdokumentInfoRelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return kopiertJournalpostdokumentInfoRelasjon;
	}
}
