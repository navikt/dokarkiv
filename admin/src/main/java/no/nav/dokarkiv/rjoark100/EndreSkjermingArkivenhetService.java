package no.nav.dokarkiv.rjoark100;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class EndreSkjermingArkivenhetService {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkRepository joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final EntityManager entityManager;

	@Inject
	public EndreSkjermingArkivenhetService(
			SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository, EntityManager entityManager) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.entityManager = entityManager;
	}

	public Map<Pair<Long, Long>, List<ArkivElementEndringTO>> endreSkjermingJournalpost(Long journalpostId, SkjermingTypeCode tilSkjerming) {
		Journalpost journalpost = hentJournalpost(journalpostId);
		Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();

		//Skjerm JournalpostRelasjoner. For at oppførsel skal bli likt med skjermDokumentInfo må alle relasjoner skjermes før Journalpost skjermes.
		journalpost.getJournalpostDokumentInfoRelasjonerAdmin()
				.forEach(rel -> aksjonsLoggMap.put(Pair.of(journalpostId, rel.getDokumentInfo()
						.getDokumentInfoId()), endreSkjermingJournalpostDokumentInfoRelasjon(rel, tilSkjerming)));

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		//Skjerm Journalpost
		if (journalpost.getSkjermingType() != tilSkjerming) {
			skjermingService.setJournalpostSkjerming(journalpostId, tilSkjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(enumToString(journalpost.getSkjermingType()))
							.tilVerdi(enumToString(tilSkjerming))
							.build());
		}
		aksjonsLoggMap.put(Pair.of(journalpostId, null), arkivElementEndringTOList);
		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> endreSkjermingDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		FilDetaljer filDetaljer = hentFildetaljerByVariantFormat(dokumentInfoId, variant);
		arkivElementEndringTOList.addAll(endreSkjermingFildetaljer(filDetaljer, tilSkjerming));
		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> endreSkjermingFildetaljer(FilDetaljer filDetaljer, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		SkjermingTypeCode forrigeSkjerming = filDetaljer.getSkjermingType();
		if (forrigeSkjerming != tilSkjerming) {
			//Skal ikke fjerne skjerming fra ARKIV variant hvis det finnes en SLADDET variant som ikke er skjermet.
			//Det betyr at dokumentet er sladdet hvor originalen som er ARKIV variant er skjermet.
			//Skal være mulig å endre skjerming til noe annet hvis dokumentet er sladdet
			if (tilSkjerming != null || !filDetaljer.isArkivVariant() || canRemoveSkjermingFromArkivVariant(filDetaljer.getDokumentInfo())) {
				skjermingService.setFildetaljerSkjerming(filDetaljer.getDokumentInfo()
						.getDokumentInfoId(), filDetaljer.getVariantFormat(), tilSkjerming);
				arkivElementEndringTOList.add(
						ArkivElementEndringTO.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(filDetaljer.getVariantFormat()))
								.fraVerdi(enumToString(forrigeSkjerming))
								.tilVerdi(enumToString(tilSkjerming))
								.build());
			}

		}
		return arkivElementEndringTOList;
	}

	private boolean canRemoveSkjermingFromArkivVariant(DokumentInfo dokumentInfo) {
		FilDetaljer sladdet = dokumentInfo.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.SLADDET);
		return sladdet == null;
	}

	public Map<Pair<Long, Long>, List<ArkivElementEndringTO>> endreSkjermingDokumentInfo(Long dokumentInfoId, SkjermingTypeCode tilSkjerming) {
		Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(dokumentInfoId);

		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringList = new ArrayList<>();
			arkivElementEndringList.addAll(endreSkjermingJournalpostDokumentInfoRelasjon(relasjon, tilSkjerming));
			entityManager.refresh(relasjon);
			//Hvis tilSkjerming=null (Opphev skjerming) og Journalpost er skjermet så skal skjermingen i Journalpost fjernes. Hvis ikke dette gjøres vil ikke dokumentet være synlig.
			//Hvis tilSkjerming!=null og Journalposten ikke har noen flere dokumentInfo relasjoner som IKKE er skjermet så skal journalposten også skjermes.
			if (tilSkjerming == null && skjermingService.isJournalpostSkjermet(journalpostId)) {
				arkivElementEndringList.addAll(endreSkjermingJournalpost(journalpostId, null).getOrDefault(Pair.of(journalpostId, null), new ArrayList<>()));
			} else if (tilSkjerming != null && isJournalpostHarIngenDokumentInfoRelasjoner(journalpostId)) {
				arkivElementEndringList.addAll(endreSkjermingJournalpost(journalpostId, tilSkjerming).getOrDefault(Pair.of(journalpostId, null), new ArrayList<>()));
			}

			aksjonsLoggMap.put(Pair.of(journalpostId, dokumentInfoId), arkivElementEndringList);

		});

		return aksjonsLoggMap;
	}

	private List<ArkivElementEndringTO> endreSkjermingJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		Long journalpostId = relasjon.getJournalpost().getJournalpostId();
		Long dokumentInfoId = relasjon.getDokumentInfo().getDokumentInfoId();
		//Hvis relasjon er HOVEDDOKUMENT så skal bare alle fildetaljer skjermes og ikke selve relasjonen.
		//Grunnen til det er at Journalpost uten HOVEDDOKUMENT relasjon skaper problemer i gamle Joark tjenester og fagsystemer som bruker de gamle Joark tjenestene.
		if (relasjon.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.HOVEDDOKUMENT) {
			arkivElementEndringTOList.addAll(endreSkjermingAlleFildetaljer(relasjon.getDokumentInfo(), tilSkjerming));
		} else {
			arkivElementEndringTOList.addAll(endreSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, relasjon
					.getSkjermingType(), tilSkjerming));
		}

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> endreSkjermingJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode forrigeSkjerming, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (forrigeSkjerming != tilSkjerming) {
			skjermingService.setJpDokInfoRelSkjerming(journalpostId, dokumentInfoId, tilSkjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(RELASJON_SKJERMING_TYPE)
							.fraVerdi(enumToString(forrigeSkjerming))
							.tilVerdi(enumToString(tilSkjerming))
							.build()
			);
		}
		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> endreSkjermingAlleFildetaljer(DokumentInfo dokumentInfo, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		//Skal ikke fjerne skjerming fra Fildetaljer hvis dokumentet er kassert. I det tilfellet så må kassering tjenesten kalles for å kunne gjøre endring på fildetaljer skjerming
		if (isFalse(dokumentInfo.isKassert())) {
			dokumentInfo.getFildetaljerListeAdmin().forEach(filDetaljer -> {
				arkivElementEndringTOList.addAll(endreSkjermingFildetaljer(filDetaljer, tilSkjerming));
					entityManager.refresh(filDetaljer);
			});
		}
		return arkivElementEndringTOList;
	}

	private boolean isJournalpostHarIngenDokumentInfoRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		return journalpostDokumentInfoRelasjonList.stream().allMatch(relasjon -> {
			if (relasjon.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.HOVEDDOKUMENT) {
				//Hvis dokumentet er kassert og alle fildetaljer er skjermet så betyr det at hoveddokument ikke er skjermet men er kassert.
				//Ellers hvis alle fildetaljer på hoveddokument er skjermet så betyr det at hoveddokumentet er skjermet
				return skjermingService.isAllFildetaljerSkjermet(relasjon.getDokumentInfo()) && isFalse(relasjon.getDokumentInfo()
						.isKassert());
			} else {
				return Objects.nonNull(relasjon.getSkjermingType());
			}
		});
	}

	private Journalpost hentJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException("Fant ikke journalpost med journalpostId=" + journalpostId));
	}

	private FilDetaljer hentFildetaljerByVariantFormat(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		return dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() ->
						new DokumentInfoIkkeFunnetException(String.format("Fant ikke dokumentInfo med dokumentInfoId=%s", dokumentInfoId)))
				.findFilDetaljerByVariantFormatAdmin(variantFormatCode);
	}


	private List<JournalpostDokumentInfoRelasjon> hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(Long dokumentInfoId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		if (journalpostDokumentInfoRelasjonList.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
					"Fant ikke journalpostDokumentInfoRelasjoner dokumentInfoId=%s", dokumentInfoId));
		}
		return journalpostDokumentInfoRelasjonList;
	}

}
