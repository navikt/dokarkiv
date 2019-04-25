package no.nav.dokarkiv.rjoark100;

import static java.util.Objects.isNull;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public List<ArkivElementEndringTO> endreSkjermingJournalpost(Long journalpostId, SkjermingTypeCode tilSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		Journalpost journalpost = hentJournalpost(journalpostId);
		if (journalpost.getSkjermingType() != tilSkjerming) {
			skjermingService.setJournalpostSkjerming(journalpostId, tilSkjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(enumToString(journalpost.getSkjermingType()))
							.tilVerdi(enumToString(tilSkjerming))
							.build()
			);
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

	public List<ArkivElementEndringTO> endreSkjermingDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		FilDetaljer filDetaljer = hentFildetaljerByVariantFormat(dokumentInfoId, variant);
		if (filDetaljer.getSkjermingType() != skjerming) {
			skjermingService.setFildetaljerSkjerming(dokumentInfoId, variant, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
							.fraVerdi(enumToString(filDetaljer.getSkjermingType()))
							.tilVerdi(enumToString(skjerming))
							.build()
			);
		}
		return arkivElementEndringTOList;
	}


	public Map<Long, List<ArkivElementEndringTO>> endreSkjermingDokumentInfo(Long dokumentInfoId, SkjermingTypeCode tilSkjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(dokumentInfoId);

		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringList = endreSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, relasjon
					.getSkjermingType(), tilSkjerming);

			entityManager.refresh(relasjon);
			//Hvis tilSkjerming=null (Opphev skjerming) og Journalpost er skjermet så skal skjermingen i Journalpost fjernes. Hvis ikke dette gjøres vil ikke dokumentet være synlig.
			//Hvis tilSkjerming!=null og Journalposten ikke har noen flere dokumentInfo relasjoner som IKKE er skjermet så skal journalposten også skjermes.
			if (tilSkjerming == null && skjermingService.isJournalpostSkjermet(journalpostId)) {
				arkivElementEndringList.addAll(endreSkjermingJournalpost(journalpostId, null));
			} else if (tilSkjerming != null && isJournalpostHarIngenDokumentInfoRelasjoner(journalpostId)) {
				arkivElementEndringList.addAll(endreSkjermingJournalpost(journalpostId, tilSkjerming));
			}

			aksjonsLoggMap.put(journalpostId, arkivElementEndringList);

		});

		return aksjonsLoggMap;
	}

	private boolean isJournalpostHarIngenDokumentInfoRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		return journalpostDokumentInfoRelasjonList.stream().noneMatch(relasjon -> isNull(relasjon.getSkjermingType()));

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
