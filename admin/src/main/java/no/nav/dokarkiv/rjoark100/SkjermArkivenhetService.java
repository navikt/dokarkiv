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
public class SkjermArkivenhetService {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkRepository joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final EntityManager entityManager;

	@Inject
	public SkjermArkivenhetService(
			SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository, EntityManager entityManager) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.entityManager = entityManager;
	}

	public List<ArkivElementEndringTO> skjermJournalpost(Long journalpostId, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		Journalpost journalpost = hentJournalpost(journalpostId);
		if (isNull(journalpost.getSkjermingType())) {
			skjermingService.skjermJournalpost(journalpostId, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(JOURNALPOST_SKJERMING_TYPE)
							.fraVerdi(null)
							.tilVerdi(skjerming.name())
							.build()
			);
		}
		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> skjermJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode forrigeSkjerming, SkjermingTypeCode nySkjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (isNull(forrigeSkjerming)) {
			skjermingService.skjermJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, nySkjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(RELASJON_SKJERMING_TYPE)
							.fraVerdi(null)
							.tilVerdi(nySkjerming.name())
							.build()
			);
		}

		return arkivElementEndringTOList;
	}

	public Map<Long, List<ArkivElementEndringTO>> skjermDokumentInfo(Long dokumentInfoId, SkjermingTypeCode skjerming) {
		Map<Long, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = hentJournalpostDokumentInfoRelasjonerByDokumentInfoId(dokumentInfoId);
		journalpostDokumentInfoRelasjonList.forEach(relasjon -> {
			List<ArkivElementEndringTO> arkivElementEndringList = new ArrayList<>();

			Long journalpostId = relasjon.getJournalpost().getJournalpostId();
			arkivElementEndringList.addAll(skjermJournalpostDokumentInfoRelasjon(journalpostId, dokumentInfoId, relasjon.getSkjermingType(), skjerming));

			entityManager.refresh(relasjon);

			if (isJournalpostHarIngenJournalpostRelasjoner(journalpostId)) {
				arkivElementEndringList.addAll(skjermJournalpost(journalpostId, skjerming));
			}

			aksjonsLoggMap.put(journalpostId, arkivElementEndringList);
		});

		return aksjonsLoggMap;
	}

	public List<ArkivElementEndringTO> skjermDokumentFil(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		FilDetaljer filDetaljer = hentFildetaljerByVariantFormat(dokumentInfoId, variant);
		if (isNull(filDetaljer.getSkjermingType())) {
			skjermingService.skjermFildetaljerByVariant(dokumentInfoId, variant, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(FILDETALJER_SKJERMING_TYPE_VARIANT(variant))
							.fraVerdi(enumToString(filDetaljer.getSkjermingType()))
							.tilVerdi(skjerming.name())
							.build()

			);
		}

		return arkivElementEndringTOList;
	}

	private boolean isJournalpostHarIngenJournalpostRelasjoner(Long journalpostId) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonList = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		return journalpostDokumentInfoRelasjonList.stream().noneMatch(relasjon -> isNull(relasjon.getSkjermingType()));

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
