package no.nav.dokarkiv.rjoark101;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.JournalpostDokumentInfoPair;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.exception.ArkivVariantkkeFunnetException;
import no.nav.dokarkiv.exception.JournalpostKanIkkeSlettesException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_TILKNYTTET_SOM;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Slf4j
@Service
public class SlettArkivenhetService {

	private final JoarkDeleteRepository deleteRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkRepository joarkRepository;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final EntityManager entityManager;

	public SlettArkivenhetService(JoarkDeleteRepository deleteRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentInfoRepository dokumentInfoRepository, EntityManager entityManager) {
		this.deleteRepository = deleteRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.entityManager = entityManager;
	}

	public Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> slettJournalpost(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ingen journalpost med journalpostId=%s i databasen", journalpostId)));

		validerAtJournalpostIkkeErSplittet(journalpost);
		validerAtHoveddokumentIkkeHarRelasjonerTilAndreJournalposter(journalpost);

		//Bare for logging
		sjekkOmJournalpostErSplittetUtFraEnAnnenJournalpost(journalpost);

		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();

		aksjonsLoggMap.putAll(slettDokumentInfoRelasjonerKnyttetTilJournalpost(journalpostId));
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(journalpostId, null), slettJournalpostFraDatabasen(journalpost.getJournalpostId()));

		return aksjonsLoggMap;
	}

	public Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> slettDokumentInfo(Long dokumentInfoId) {
		if (isFalse(dokumentInfoRepository.existsById(dokumentInfoId))) {
			throw new DokumentInfoIkkeFunnetException(String.format("Fant ingen dokumentInfo med dokumentInfoId=%s i databasen", dokumentInfoId));
		}

		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> relasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		//Slett alle JournalpostDokumentInfoRelasjoner
		slettJournalpostRelasjonerKnyttetTilDokumentInfo(dokumentInfoId, relasjonList, aksjonsLoggMap);
		//Slett DokumentInfo. Alle relasjoner må slettes før DokumentInfo kan slettes pga foreign key.
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(null, dokumentInfoId), slettDokumentInfoFraDatabasen(dokumentInfoId));
		//Slett Journalpost eller bytt om VEDLEGG relasjon til HOVEDDOKUMENT relasjon
		slettJournalpostHvisIngenRelasjonerEllerByttVedleggRelasjonTilHoveddokument(dokumentInfoId, relasjonList, aksjonsLoggMap);

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> slettDokumentFil(Long dokumentInfoId, VariantFormatCode variant) {

		//Sjekk om dokumentInfo eksisterer
		DokumentInfo dokumentInfo = dokumentInfoRepository.findById(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Fant ikke dokument med dokumentInfoId=%s i Joark databasen", dokumentInfoId)));

		//Sjekk om fildetaljer eksisterer
		FilDetaljer filDetaljerSomSkalSlettes = dokumentInfo.findFilDetaljerByVariantFormatAdmin(variant);
		if (Objects.isNull(filDetaljerSomSkalSlettes)) {
			throw new ArkivVariantkkeFunnetException(String.format("Dokument med dokumentInfoId=%s har ingen fildetaljer med variantFormat=%s", dokumentInfoId, variant));
		}

		return slettFilOgFildetaljerFraDatabasen(dokumentInfoId, variant);
	}


	private Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> byttFørsteVedleggRelasjonTilHoveddokument(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> relasjonList = journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpostId)
				.stream()
				.filter(rel -> rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG)
				.collect(Collectors.toList());

		JournalpostDokumentInfoRelasjon vedleggRelasjon = relasjonList.get(0);
		vedleggRelasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		journalpostDokumentInfoRelasjonRepository.save(vedleggRelasjon);

		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(journalpostId, vedleggRelasjon.getDokumentInfo()
				.getDokumentInfoId()), Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_TILKNYTTET_SOM)
						.fraVerdi(TilknyttetJournalpostSomCode.VEDLEGG.name())
						.tilVerdi(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name())
						.build()
		));
		return aksjonsLoggMap;

	}

	private Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> slettJournalpostRelasjonerKnyttetTilDokumentInfo(Long dokumentInfoId, List<JournalpostDokumentInfoRelasjon> relasjonList, Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap) {
		relasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
			arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjonFraDatabasen(relasjon));
			aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(journalpostId, dokumentInfoId), arkivElementEndringTOList);
		});

		return aksjonsLoggMap;

	}

	private Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> slettJournalpostHvisIngenRelasjonerEllerByttVedleggRelasjonTilHoveddokument(Long dokumentInfoId, List<JournalpostDokumentInfoRelasjon> relasjonList, Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap) {
		relasjonList.forEach(relasjon -> {
			Journalpost journalpost = relasjon.getJournalpost();
			entityManager.refresh(journalpost);
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			//Slett Journalpost hvis Journalposten ikke har noen dokumentInfo relasjoner.
			//Journalpost uten dokumentInfo relasjoner vil skape problemer i andre tjenester og det er heller ikke meningen å ha en Journalpost uten dokumenter.
			//DokumentInfo må slettes før Journalpost kan slettes fordi DokumentInfo objektet har peker til Journalpost via original_journalpost kolonnen
			if (isFalse(journalpost.hasAnyDokumentInfoRelasjonerIncludingSkjermet())) {
				validerAtJournalpostIkkeErSplittet(relasjon.getJournalpost());
				List<ArkivElementEndringTO> arkivElementEndringTOList = aksjonsLoggMap.get(JournalpostDokumentInfoPair.of(journalpostId, dokumentInfoId));
				arkivElementEndringTOList.addAll(slettJournalpostFraDatabasen(journalpostId));
				aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(journalpostId, dokumentInfoId), arkivElementEndringTOList);
				//Hvis Journalpost ikke har hoveddokument relasjon etter sletting (DokumentInfo var hoveddokument i Journalposten)
				//så skal en vilkårlig vedlegg settes som hoveddokument i Journalposten. Grunnen til det er at Journalpost må ha en hoveddokument ellers vil gamle tjenester feile.
			} else if (isFalse(journalpost.hasHoveddokumentRelasjon())) {
				aksjonsLoggMap.putAll(byttFørsteVedleggRelasjonTilHoveddokument(journalpostId));
			}

		});
		return aksjonsLoggMap;
	}


	private void validerAtHoveddokumentIkkeHarRelasjonerTilAndreJournalposter(Journalpost journalpost) {
		DokumentInfo dokumentInfoHoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		if (dokumentInfoHoveddokument.getJournalpostRelasjoner().size() > 1) {
			throw new JournalpostKanIkkeSlettesException(String.format("Hoveddokument er tilknyttet andre journalposter. All (gjen)bruk av dokumentinfo %s må fjernes før journalpost kan slettes.",
					dokumentInfoHoveddokument.getDokumentInfoId()));
		}
	}

	private void validerAtJournalpostIkkeErSplittet(Journalpost journalpost) {
		List<DokumentInfo> dokumenterMedJournalpostSattSomOriginalJournalpost =
				dokumentInfoRepository.findByOriginalJournalpostJournalpostId(journalpost.getJournalpostId());

		if (dokumenterMedJournalpostSattSomOriginalJournalpost.size() > journalpost.getJournalpostDokumentInfoRelasjonerAdmin()
				.size()) {
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost=%s er splittet og kan ikke slettes før de splittete dokumentene er slettet",
					journalpost.getJournalpostId()));
		}
	}

	private boolean sjekkOmJournalpostErSplittetUtFraEnAnnenJournalpost(Journalpost journalpost) {
		if (!journalpost.getJournalpostDokumentInfoRelasjoner()
				.isEmpty() && journalpost.findHoveddokumentDokumentInfoRelasjon() != null) {
			Journalpost hoveddokOrigJp = journalpost.findHoveddokumentDokumentInfoRelasjon()
					.getDokumentInfo()
					.getOriginalJournalpost();

			if (Objects.nonNull(hoveddokOrigJp) &&
					isFalse(journalpost.getJournalpostId().equals(hoveddokOrigJp.getJournalpostId()))) {
				log.warn(String.format("Journalpost som slettes er splittet hvor originale journalpost=%s", hoveddokOrigJp.getJournalpostId()));
				return true;
			}
		}

		return false;
	}

	private Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> slettDokumentInfoRelasjonerKnyttetTilJournalpost(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		relasjoner
				.forEach(relasjon -> {
					aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(journalpostId, relasjon.getDokumentInfo()
							.getDokumentInfoId()), slettJournalpostDokumentInfoRelasjon(relasjon));
				});

		return aksjonsLoggMap;
	}

	private List<ArkivElementEndringTO> slettJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			return slettJournalpostDokumentInfoRelasjonFraDatabasen(relasjonSomSkalSlettes);
		} else {
			//Hvis dokumentInfo er bare knyttet til en Journalpost betyr det at DokumentInfo ikke vil ha noen relasjoner etter sletting av Journalpost.
			//DokumentInfo skal derfor også slettes og slettingen skal logges i aksjonsloggen
			return slettRelasjonMedDokumentInfoKnyttetTilBareEnJournalpost(relasjonSomSkalSlettes);
		}
	}

	private List<ArkivElementEndringTO> slettRelasjonMedDokumentInfoKnyttetTilBareEnJournalpost(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjonFraDatabasen(relasjonSomSkalSlettes));
		arkivElementEndringTOList.addAll(slettDokumentInfoFraDatabasen(relasjonSomSkalSlettes.getDokumentInfo()
				.getDokumentInfoId()));
		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> slettJournalpostDokumentInfoRelasjonFraDatabasen(JournalpostDokumentInfoRelasjon relasjon) {
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());

		return new ArrayList<>(Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(relasjon.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build())
		);
	}


	private List<ArkivElementEndringTO> slettJournalpostFraDatabasen(Long journalpostId) {
		deleteRepository.deleteDokUrlInfoByJournalpostId(journalpostId);
		deleteRepository.deleteKryssreferanseByJournalpostId(journalpostId);
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukereByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_JOURNALPOST_ID)
						.fraVerdi(journalpostId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	private List<ArkivElementEndringTO> slettDokumentInfoFraDatabasen(Long dokumentInfoId) {
		slettAlleFilOgFildetaljerGittDokumentInfoIdFraDatabasen(dokumentInfoId);
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);
		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(dokumentInfoId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	private List<ArkivElementEndringTO> slettFilOgFildetaljerFraDatabasen(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(variantFormatCode.name())
						.tilVerdi(null)
						.build()

		);
	}

	private void slettAlleFilOgFildetaljerGittDokumentInfoIdFraDatabasen(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
