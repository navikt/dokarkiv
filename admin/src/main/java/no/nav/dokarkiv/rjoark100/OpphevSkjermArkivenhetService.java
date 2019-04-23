package no.nav.dokarkiv.rjoark100;

import static java.util.Objects.isNull;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

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

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpphevSkjermArkivenhetService {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkRepository joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final EntityManager entityManager;

	public OpphevSkjermArkivenhetService(
			SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository, EntityManager entityManager) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.entityManager = entityManager;
	}

	public List<ArkivElementEndringTO> opphevSkjermJournalpost(Long journalpostId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		Journalpost journalpost = hentJournalpost(journalpostId);
		if (isFalse(isNull(journalpost.getSkjermingType()))) {
			skjermingService.opphevSkjermJournalpostByJournalpostId(journalpostId);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(enumToString(journalpost.getSkjermingType()))
							.tilVerdi(null)
							.build()
			);
		}

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> opphevSkjermingJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode forrigeSkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (isFalse(isNull(forrigeSkjerming))) {
			skjermingService.opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(RELASJON_SKJERMING_TYPE)
							.fraVerdi(enumToString(forrigeSkjerming))
							.tilVerdi(null)
							.build()
			);
		}
		return arkivElementEndringTOList;
	}

	public Map<Long, List<ArkivElementEndringTO>> opphevSkjermDokumentInfo(Long dokumentInfoId, SkjermingTypeCode skjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(dokumentInfoId);

		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			List<ArkivElementEndringTO> arkivElementEndringList = opphevSkjermingJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, relasjon
					.getSkjermingType());

			entityManager.refresh(relasjon);
			if (skjermingService.isJournalpostSkjermet(journalpostId, skjerming)) {
				arkivElementEndringList.addAll(opphevSkjermJournalpost(journalpostId));
			}

			aksjonsLoggMap.put(journalpostId, arkivElementEndringList);

		});

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> opphevSkjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		FilDetaljer filDetaljer = hentFildetaljerByVariantFormat(dokumentInfoId, variant);
		if (isFalse(isNull(filDetaljer.getSkjermingType()))) {
			skjermingService.opphevSkjermFildetaljerByVariant(dokumentInfoId, variant);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
							.fraVerdi(enumToString(filDetaljer.getSkjermingType()))
							.tilVerdi(null)
							.build()
			);
		}
		return arkivElementEndringTOList;
	}

	private Journalpost hentJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException("Kan ikke finne journalpost med journalpostId=" + journalpostId));
	}

	private FilDetaljer hentFildetaljerByVariantFormat(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		return dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() ->
						new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s", dokumentInfoId)))
				.findFilDetaljerByVariantFormatAdmin(variantFormatCode);
	}


	private List<JournalpostDokumentInfoRelasjon> hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(Long dokumentInfoId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		if (journalpostDokumentInfoRelasjonList.isEmpty()) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
					"Kan ikke finne journalpostDokumentInfoRelasjoner dokumentInfoId=%s", dokumentInfoId));
		}
		return journalpostDokumentInfoRelasjonList;
	}


}
