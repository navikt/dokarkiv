package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper og lagrer AksjonsLogg i en egen transaksjon. AksjonsLoggSerivce henter Journalpost fra databasen for å hente ut verdier som settes i aksjonsloggen.
 * Etter sletting av Journalpost vil aksjonsLoggService feile fordi den ikke finner Journalpost.
 * Lagring av aksjonsLogg må derfor skje i egen transaksjon hvor Journalpost fortsatt ikke er slettet.
 * For at Spring skal kunne lage ny transaksjon må denne metoden bli definert i en egen bønne.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class LagreAksjonsLoggService {

	private final AksjonsLoggService aksjonsLoggService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	public LagreAksjonsLoggService(AksjonsLoggService aksjonsLoggService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.aksjonsLoggService = aksjonsLoggService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	/**
	 * @param aksjonsTypeCode
	 * @param aksjonsLoggMap I form av Map<Pair<Long, Long>, List<ArkivElementEndringTO>> hvor Pair<Long, Long> er JournalpostId og DokumentInfoId
	 * @param hjemmel
	 * @param melding
	 * @param utfoertAv
	 * @throws UgyldigAksjonsLoggException
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(AksjonsTypeCode aksjonsTypeCode, Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap, String hjemmel, String melding, String utfoertAv) throws
			UgyldigAksjonsLoggException {

		/*
		 Hvis journalpostId(Left) er null så opprettes det ny aksjonslogg eller at arkivelementendringer legges til eksisterende aksjonslogg med journalpostId og dokumentInfoId par for alle JournalpostRelasjoner dokumentInfoId(right) har.
		 Grunnen til at det gjøres er for å kunne gjøre det søktbar på alle journalpostIder og brukere som dokumentInfo relasjon til og tillegg at det skal være mulig å se alle endringer som journalpost har gått gjennom inkludert endringene i dokumentRelasjoner.
		 */
		List<JournalpostDokumentInfoPair> removeList = new ArrayList<>();
		aksjonsLoggMap.keySet().forEach(aksjonsLoggKey -> {
			if (aksjonsLoggKey.getJournalpostId() == null) {
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(aksjonsLoggKey.getDokumentInfoId())
						.forEach(rel -> {
							JournalpostDokumentInfoPair jpDokInfoPair = JournalpostDokumentInfoPair.of(rel.getJournalpost().getJournalpostId(), aksjonsLoggKey.getDokumentInfoId());
							List<ArkivElementEndringTO> arkivElementEndringTOList = aksjonsLoggMap.getOrDefault(jpDokInfoPair, new ArrayList<>());
							arkivElementEndringTOList.addAll(aksjonsLoggMap.get(aksjonsLoggKey));
							aksjonsLoggMap.put(jpDokInfoPair, arkivElementEndringTOList);
						});
				removeList.add(aksjonsLoggKey);
			}
		});

		removeList.forEach(aksjonsLoggMap::remove);


		for (JournalpostDokumentInfoPair aksjonsLoggJournalpostDokumentInfo : aksjonsLoggMap.keySet()) {
			lagreAksjonsLogg(aksjonsTypeCode, aksjonsLoggJournalpostDokumentInfo.getJournalpostId(), aksjonsLoggJournalpostDokumentInfo.getDokumentInfoId(), hjemmel, null, melding, utfoertAv, aksjonsLoggMap
					.get(aksjonsLoggJournalpostDokumentInfo));
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(AksjonsTypeCode aksjonsType, Long dokumentInfoId, String hjemmel, String melding, String utfoertAv, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {

		for (JournalpostDokumentInfoRelasjon rel : journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId)) {
			lagreAksjonsLogg(aksjonsType, rel.getJournalpost().getJournalpostId(), dokumentInfoId, hjemmel, null, melding, utfoertAv, arkivElementEndringTOList);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(AksjonsTypeCode aksjonsType, Long journalpostId, Long dokumentInfoId, String hjemmel, String bruker, String melding, String utfoertAv, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggTo(melding, bruker, utfoertAv, hjemmel, aksjonsType, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}
}
