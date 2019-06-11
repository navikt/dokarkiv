package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper og lagrer AksjonsLogg i en egen transaksjon. AksjonsLoggSerivce henter Journalpost fra databasen for å hente ut verdier som settes i aksjonsloggen.
 * Etter sletting av Journalpost vil aksjonsLoggService feile fordi den ikke finner Journalpost.
 * Lagring av aksjonsLogg må derfor skje i egen transaksjon hvor Journalpost fortsatt ikke er slettet.
 * For at Spring skal kunne lage ny transaksjon må denne metoden bli definert i en egen bønne.
 * <p>
 * Denne komponenten brukes for å lagre liste med aksjonslogger
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class LagreAksjonsLoggService {

	private final AksjonsLoggService aksjonsLoggService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	public LagreAksjonsLoggService(AksjonsLoggService aksjonsLoggService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.aksjonsLoggService = aksjonsLoggService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(AksjonsTypeCode aksjonsTypeCode, Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap, String hjemmel, String melding, String utfoertAv) throws
			UgyldigAksjonsLoggException {


		mergeOrFillAksjonsLoggWithEmptyJournalpostOrDokumentInfoId(aksjonsLoggMap);

		for (JournalpostDokumentInfoPair jpDokInfoPair : aksjonsLoggMap.keySet()) {
			AksjonsLoggTO aksjonsLoggTO = mapAksjonsLoggTo(aksjonsTypeCode, jpDokInfoPair.getJournalpostId(), jpDokInfoPair.getDokumentInfoId(), melding, utfoertAv, hjemmel);
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, aksjonsLoggMap.get(jpDokInfoPair));
		}
	}

	/**
	 * Denne metoden lagrer ny aksjonslogg med samme arkivelementendringer for alle journalpostIder dokumentInfo har relasjon til.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(AksjonsTypeCode aksjonsType, Long dokumentInfoId, String hjemmel, String melding, String utfoertAv, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {

		for (JournalpostDokumentInfoRelasjon rel : journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)) {
			AksjonsLoggTO aksjonsLoggTO = mapAksjonsLoggTo(aksjonsType, rel.getJournalpost().getJournalpostId(), dokumentInfoId, melding, utfoertAv, hjemmel);
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
		}
	}

	private AksjonsLoggTO mapAksjonsLoggTo(AksjonsTypeCode aksjon, Long journalpostId, Long dokumentInfoId, String melding, String utfoertAv, String hjemmel) {

		return AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.melding(melding)
				.hjemmel(hjemmel)
				.utfoertAv(utfoertAv)
				.build();

	}

	private void mergeOrFillAksjonsLoggWithEmptyJournalpostOrDokumentInfoId(Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap){
		List<JournalpostDokumentInfoPair> removeList = new ArrayList<>();
		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> tempNewAksjonsLogg = new HashMap<>();
		aksjonsLoggMap.keySet().forEach(aksjonsLoggKey -> {
//		 Hvis journalpostId er null så opprettes enten ny aksjonslogg, eller så flettes arkivelementendringene med en eksisterende aksjonslogg med samme journalpostId og dokumentInfoId par for alle JournalpostRelasjoner dokumentInfoId har.
//		 Grunnen til at det gjøres er for å kunne gjøre det søktbar på alle journalpostIder og brukere som dokumentInfo har relasjon til og tillegg at det skal være mulig å se alle endringer som journalpost har gått gjennom inkludert endringene i dokumentRelasjoner.
			if (aksjonsLoggKey.getJournalpostId() == null) {
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(aksjonsLoggKey.getDokumentInfoId())
						.forEach(rel -> {
							JournalpostDokumentInfoPair jpDokInfoPair = JournalpostDokumentInfoPair.of(rel.getJournalpost().getJournalpostId(), aksjonsLoggKey.getDokumentInfoId());
							List<ArkivElementEndringTO> arkivElementEndringTOList = aksjonsLoggMap.getOrDefault(jpDokInfoPair, new ArrayList<>());
							arkivElementEndringTOList.addAll(aksjonsLoggMap.get(aksjonsLoggKey));
							if (aksjonsLoggMap.containsKey(jpDokInfoPair)) {
								aksjonsLoggMap.put(jpDokInfoPair, arkivElementEndringTOList);
							} else {
								//Kan ikke legge til ny verdi i map mens vi iterer gjennom keySet
								tempNewAksjonsLogg.put(jpDokInfoPair, arkivElementEndringTOList);
							}
						});
				removeList.add(aksjonsLoggKey);

//		 Hvis dokumentInfoId er null så opprettes enten ny aksjonslogg, eller så flettes arkivelementendringene med en eksisterende aksjonslogg med samme journalpostId og dokumentInfoId par for alle JournalpostRelasjoner journalpostId har.
//		 Grunnen til at det gjøres er for å kunne gjøre det søktbar på alle dokumentInfoIder og brukere som journalpost har relasjon til og tillegg at det skal være mulig å se alle endringer som dokumentInfo har gått gjennom inkludert endringene i dokumentRelasjoner.
			} else if (aksjonsLoggKey.getDokumentInfoId() == null) {
				journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(aksjonsLoggKey.getJournalpostId())
						.forEach(rel -> {
							JournalpostDokumentInfoPair jpDokInfoPair = JournalpostDokumentInfoPair.of(aksjonsLoggKey.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());
							List<ArkivElementEndringTO> arkivElementEndringTOList = aksjonsLoggMap.getOrDefault(jpDokInfoPair, new ArrayList<>());
							arkivElementEndringTOList.addAll(aksjonsLoggMap.get(aksjonsLoggKey));
							if (aksjonsLoggMap.containsKey(jpDokInfoPair)) {
								aksjonsLoggMap.put(jpDokInfoPair, arkivElementEndringTOList);
							} else {
								//Kan ikke legge til ny verdi i map mens vi iterer gjennom keySet
								tempNewAksjonsLogg.put(jpDokInfoPair, arkivElementEndringTOList);
							}
						});
				removeList.add(aksjonsLoggKey);
			}
		});

		removeList.forEach(aksjonsLoggMap::remove);
		tempNewAksjonsLogg.forEach(aksjonsLoggMap::put);

	}
}
