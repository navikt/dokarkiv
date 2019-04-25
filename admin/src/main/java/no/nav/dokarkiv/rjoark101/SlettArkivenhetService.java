package no.nav.dokarkiv.rjoark101;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_TILKNYTTET_SOM;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.exception.ArkivVariantkkeFunnetException;
import no.nav.dokarkiv.exception.JournalpostKanIkkeSlettesException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("Duplicates")
public class SlettArkivenhetService {

	private final JoarkDeleteRepository deleteRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkRepository joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;

	@Inject
	public SlettArkivenhetService(JoarkDeleteRepository deleteRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository) {
		this.deleteRepository = deleteRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public List<ArkivElementEndringTO> slettJournalpost(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ingen journalpost med journalpostId=%s i databasen", journalpostId)));

		validerAtJournalpostIkkeErSplittet(journalpost);
		sjekkOmJournalpostErSplittetUtFraEnAnnenJournalpost(journalpost);
		validerAtHoveddokumentIkkeHarRelasjonerTilAndreJournalposter(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(slettDokumentInfoRelasjonerKnyttetTilJournalpost(journalpostId));
		arkivElementEndringTOList.addAll(slettJournalpostFraDatabasen(journalpost.getJournalpostId()));

		return arkivElementEndringTOList;
	}

	public Map<Long, List<ArkivElementEndringTO>> slettDokumentInfo(Long dokumentInfoId) {
		if (isFalse(dokumentinfoRepository.existsById(dokumentInfoId))) {
			throw new DokumentInfoIkkeFunnetException(String.format("Fant ingen dokumentInfo med dokumentInfoId=%s i databasen", dokumentInfoId));
		}

		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();

		List<JournalpostDokumentInfoRelasjon> relasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		//Slett alle JournalpostDokumentInfoRelasjoner
		relasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
			arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjonFraDatabasen(relasjon));
			aksjonsLoggMap.put(journalpostId, arkivElementEndringTOList);
		});

		//Slett DokumentInfo. Alle relasjoner må slettes før DokumentInfo kan slettes pga foreign key.
		aksjonsLoggMap.put(null, slettDokumentInfoFraDatabasen(dokumentInfoId));


		relasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringTOList = aksjonsLoggMap.get(journalpostId);
			//Slett Journalpost hvis Journalposten ikke har noen dokumentInfo relasjoner.
			//Journalpost uten dokumentInfo relasjoner vil skape problemer i andre tjenester og det er heller ikke meningen å ha en Journalpost uten dokumenter.
			//DokumentInfo må slettes før Journalpost kan slettes fordi DokumentInfo objektet har peker til Journalpost via original_journalpost parameteren
			if (isJournalpostHarIngenDokumentInfoRelasjoner(journalpostId)) {
				validerAtJournalpostIkkeErSplittet(relasjon.getJournalpost());
				arkivElementEndringTOList.addAll(slettJournalpostFraDatabasen(journalpostId));
				//Hvis Journalpost ikke har hoveddokument relasjon etter sletting (DokumentInfo var hoveddokument i Journalposten)
				//så skal en vilkårlig vedlegg settes som hoveddokument i Journalposten
			} else if (isFalse(hasJournalpostHoveddokumentRelasjon(journalpostId))) {
				//TODO: Mangler aksjonslogg
				byttFørsteVedleggRelasjonTilHoveddokument(journalpostId);
			}
			aksjonsLoggMap.put(journalpostId, arkivElementEndringTOList);

		});

		return aksjonsLoggMap;
	}


	private List<ArkivElementEndringTO> byttFørsteVedleggRelasjonTilHoveddokument(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> relasjonList = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId)
				.stream()
				.filter(rel -> rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG)
				.filter(rel -> rel.getDokumentInfo()
						.findHoveddokumentJournalpostRelasjon() == null)//Ikke bytt om dokumentInfo som allerede er hoveddokument i en annen Journalpost
				.collect(Collectors.toList());

		JournalpostDokumentInfoRelasjon vedleggRelasjon = relasjonList.get(0);
		vedleggRelasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		journalpostDokumentInfoRelasjonRepository.save(vedleggRelasjon);
		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_TILKNYTTET_SOM)
						.fraVerdi(TilknyttetJournalpostSomCode.VEDLEGG.name())
						.tilVerdi(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name())
						.build()

		);

	}

	private boolean hasJournalpostHoveddokumentRelasjon(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).get();
		return journalpost.findHoveddokumentDokumentInfoRelasjon() != null;
	}

	public List<ArkivElementEndringTO> slettDokumentFil(Long dokumentInfoId, VariantFormatCode variant) {

		//Sjekk om dokumentInfo eksisterer
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Fant ikke dokument med dokumentInfoId=%s i Joark databasen", dokumentInfoId)));

		//Sjekk om fildetaljer eksisterer
		FilDetaljer filDetaljerSomSkalSlettes = dokumentInfo.findFilDetaljerByVariantFormatAdmin(variant);
		if (Objects.isNull(filDetaljerSomSkalSlettes)) {
			throw new ArkivVariantkkeFunnetException(String.format("Dokument med dokumentInfoId=%s har ingen fildetaljer med variantFormat=%s", dokumentInfoId, variant));
		}

		return slettFilOgFildetaljerFraDatabasen(dokumentInfoId, variant);
	}

	private boolean isJournalpostHarIngenDokumentInfoRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		return journalpostDokumentInfoRelasjonList.isEmpty();

	}

	private void validerAtHoveddokumentIkkeHarRelasjonerTilAndreJournalposter(DokumentInfo dokumentInfoHoveddokument) {
		if (dokumentInfoHoveddokument.getJournalpostRelasjoner().size() > 1) {
			throw new JournalpostKanIkkeSlettesException(String.format("Hoveddokument er tilknyttet andre journalposter. All (gjen)bruk av dokumentinfo %s må fjernes før journalpost kan slettes.",
					dokumentInfoHoveddokument.getDokumentInfoId()));
		}
	}

	private void validerAtJournalpostIkkeErSplittet(Journalpost journalpost) {
		List<DokumentInfo> dokumenterMedJournalpostSattSomOriginalJournalpost =
				dokumentinfoRepository.findByOriginalJournalpostJournalpostId(journalpost.getJournalpostId());

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

	private List<ArkivElementEndringTO> slettDokumentInfoRelasjonerKnyttetTilJournalpost(Long journalpostId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		relasjoner
				.forEach(relasjon -> arkivElementEndringTOList.addAll(slettJournalpostDokumentInfoRelasjon(relasjon)));

		return arkivElementEndringTOList;
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

		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(relasjon.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()
		);
	}

	private List<ArkivElementEndringTO> slettJournalpostFraDatabasen(Long journalpostId) {
		deleteRepository.deleteDokUrlInfoByJournalpostId(journalpostId);
		deleteRepository.deleteKryssreferanseByJournalpostId(journalpostId);
		deleteRepository.deleteReturInfoByJournalpostId(journalpostId);
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
