package no.nav.dokarkiv.journalpost.v1.api.sladddokument;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.KanIkkeSladdeDokumentException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;

@Slf4j
@Service
public class SladdDokumentService {

	private final DokumentInfoRepository dokumentInfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JournalpostRepository journalpostRepository;
	private final AksjonsLoggService aksjonsLoggService;
	private final EntityManager entityManager;

	public SladdDokumentService(DokumentInfoRepository dokumentInfoRepository,
	                            DokumentFilRepository dokumentFilRepository,
	                            JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
	                            JournalpostRepository journalpostRepository,
	                            AksjonsLoggService aksjonsLoggService,
	                            EntityManager entityManager) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.entityManager = entityManager;
	}

	@Transactional
	public void sladdDokument(long dokumentInfoId, byte[] fil) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findById(dokumentInfoId)
			.orElseThrow(() -> new DokumentInfoIkkeFunnetException(dokumentInfoId));

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		SkjermingTypeCode skjermingType = utledSkjermingType(relasjoner, dokumentInfoId);

		sladdDokumentOgLogg(dokumentInfoId, fil, dokumentInfo, relasjoner, skjermingType);

		Optional<FilDetaljer> arkivVariant = dokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV);
		arkivVariant.ifPresent(variant -> variant.setSkjermingType(skjermingType, MDC.get(MDC_CONSUMER_ID)));

		fjernSkjermingForDokumentInfo(relasjoner, dokumentInfo);
		fjernSkjermingForJournalposter(relasjoner);

		log.info("sladdDokument har sladdet dokument med dokumentInfoId={} med skjermingType={}", dokumentInfoId, skjermingType);
	}

	@Transactional
	public void opphevSladdDokument(long dokumentInfoId) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findById(dokumentInfoId)
			.orElseThrow(() -> new DokumentInfoIkkeFunnetException(dokumentInfoId));

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		String sladdetFilUuid = dokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET)
			.map(FilDetaljer::getFilUuid)
			.orElseThrow(() -> new KanIkkeSladdeDokumentException(
				"Kan ikke oppheve sladding for dokument med dokumentInfoId=%d som ikke er sladdet".formatted(dokumentInfoId)));

		Optional<FilDetaljer> arkivVariant = dokumentInfo.findFilDetaljerByVariantFormatAdmin(ARKIV);
		SkjermingTypeCode forrigeArkivSkjerming = arkivVariant.map(FilDetaljer::getSkjermingType).orElse(null);
		arkivVariant.ifPresent(filDetaljer -> filDetaljer.setSkjermingType(null, MDC.get(MDC_CONSUMER_ID)));

		boolean sladdetVariantSlettet = slettSladdetVariant(dokumentInfo);

		loggOpphevSladdDokument(relasjoner, dokumentInfoId, forrigeArkivSkjerming, sladdetFilUuid, sladdetVariantSlettet);

		gjenopprettSkjermingForDokumentInfo(relasjoner, dokumentInfo, forrigeArkivSkjerming);
		gjenopprettSkjermingForJournalposter(relasjoner, forrigeArkivSkjerming);

		log.info("opphevSladdDokument har fjernet sladding fra dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private void sladdDokumentOgLogg(long dokumentInfoId, byte[] fil, DokumentInfo dokumentInfo, List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode skjermingType) {
		if (dokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET).isPresent()) {
			FilDetaljer sladdetVariant = dokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET).get();
			DokumentFil existingFil = dokumentFilRepository.findByFilUuid(sladdetVariant.getFilUuid());
			if (existingFil != null) {
				existingFil.setFil(fil);
				sladdetVariant.setFilstorrelse(String.valueOf(fil.length));
				existingFil.setEndretKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
			} else {
				sladdetVariant.setFileContent(fil);
				DokumentFil nyDokumentFil = sladdetVariant.createDokumentFil();
				dokumentFilRepository.persist(nyDokumentFil);
			}
			sladdetVariant.setEndretKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
			relasjoner.stream()
				.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
				.distinct()
				.forEach(journalpostId -> {
					aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
						.journalpostId(journalpostId)
						.dokumentInfoId(dokumentInfoId)
						.hjemmel(enumToString(skjermingType))
						.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
						.build(), List.of(
						ArkivElementEndringTO.arkivElementEndringNew(fildetaljerSkjermingTypeVariant(ARKIV), enumToString(skjermingType))
					));
					aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
						.journalpostId(journalpostId)
						.dokumentInfoId(dokumentInfoId)
						.hjemmel(enumToString(skjermingType))
						.aksjon(AksjonsTypeCode.SLADD_DOKUMENT)
						.build(), List.of(
						ArkivElementEndringTO.arkivElementEndringNew(FILDETALJER_FILUUID, sladdetVariant.getFilUuid()),
						ArkivElementEndringTO.arkivElementEndringNew(FILDETALJER_VARIANTFORMAT, SLADDET.name()),
						ArkivElementEndringTO.arkivElementEndringNew(fildetaljerSkjermingTypeVariant(ARKIV), enumToString(skjermingType))
					));
				});
		} else {
			FilDetaljer sladdetVariant = opprettSladdetVariant(dokumentInfo, fil);
			loggSladdDokument(relasjoner, dokumentInfoId, skjermingType, sladdetVariant);
		}
	}

	private SkjermingTypeCode utledSkjermingType(List<JournalpostDokumentInfoRelasjon> relasjoner, long dokumentInfoId) {
		return relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getSkjermingType)
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow(() -> new KanIkkeSladdeDokumentException(
				"Kan ikke sladde dokument med dokumentInfoId=%d som ikke er skjermet".formatted(dokumentInfoId)));
	}

	private FilDetaljer opprettSladdetVariant(DokumentInfo dokumentInfo, byte[] fil) {
		FilDetaljer sladdetVariant = FilDetaljer.builder()
			.filUuid(FilDetaljer.generateUuid())
			.filtype(FilTypeCode.PDF)
			.variantFormat(SLADDET)
			.fileContent(fil)
			.dokumentInfo(dokumentInfo)
			.build();
		sladdetVariant.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		dokumentInfo.addFilDetaljer(sladdetVariant);
		dokumentFilRepository.persist(sladdetVariant.createDokumentFil());
		dokumentInfoRepository.persist(dokumentInfo);
		return sladdetVariant;
	}

	private boolean slettSladdetVariant(DokumentInfo dokumentInfo) {
		Optional<FilDetaljer> sladdetVariantOptional = dokumentInfo.findFilDetaljerByVariantFormatAdmin(SLADDET);
		if (sladdetVariantOptional.isEmpty()) {
			return false;
		}

		var sladdetVariant = sladdetVariantOptional.get();
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(sladdetVariant.getFilUuid());
		if (dokumentFil != null) {
			entityManager.remove(dokumentFil);
		}

		dokumentInfo.removeFilDetaljer(sladdetVariant);
		entityManager.remove(sladdetVariant);
		return true;
	}

	private void fjernSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner, DokumentInfo dokumentInfo) {
		SkjermingTypeCode skjermingType = relasjoner.stream().findAny().map(JournalpostDokumentInfoRelasjon::getSkjermingType).orElse(null);

		long dokumentInfoId = dokumentInfo.getDokumentInfoId();

		// dette fases ut når skjerming settes rett på dokumentinfo
		relasjoner.stream()
			.distinct()
			.forEach(journalpostDokumentInfoRelasjon -> {
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostDokumentInfoRelasjon.getJournalpostId())
					.dokumentInfoId(dokumentInfoId)
					.hjemmel(enumToString(skjermingType))
					.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
					.build(), List.of(ArkivElementEndringTO.builder()
					.arkivElement(RELASJON_SKJERMING_TYPE)
					.fraVerdi(enumToString(journalpostDokumentInfoRelasjon.getSkjermingType()))
					.tilVerdi(null)
					.build())
				);
				journalpostDokumentInfoRelasjon.setSkjermingType(null);
			});

		if (dokumentInfo.getSkjermingType() != null) {
			aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
				.dokumentInfoId(dokumentInfoId)
				.hjemmel(enumToString(skjermingType))
				.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
				.build(), List.of(ArkivElementEndringTO.builder()
				.arkivElement(DOKUMENT_INFO_SKJERMING_TYPE)
				.fraVerdi(enumToString(dokumentInfo.getSkjermingType()))
				.tilVerdi(null)
				.build()));
			dokumentInfo.setSkjermingType(null);
		}
	}

	private void fjernSkjermingForJournalposter(List<JournalpostDokumentInfoRelasjon> relasjoner) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.distinct()
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(journalpost -> journalpost.getSkjermingType() != null)
			.forEach(journalpost -> {
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
						.journalpostId(journalpost.getJournalpostId())
						.hjemmel(null)
						.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
						.build(),
					List.of(ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(enumToString(journalpost.getSkjermingType()))
						.tilVerdi(null)
						.build()));
				journalpost.setSkjermingType(null, MDC.get(MDC_CONSUMER_ID), MDC.get(MDC_USER_NAME));
			});
	}

	private void loggSladdDokument(List<JournalpostDokumentInfoRelasjon> relasjoner, long dokumentInfoId, SkjermingTypeCode skjermingType, FilDetaljer sladdetVariant) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.distinct()
			.forEach(journalpostId -> {
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.hjemmel(enumToString(skjermingType))
					.aksjon(AksjonsTypeCode.SLADD_DOKUMENT)
					.build(), List.of(
					ArkivElementEndringTO.arkivElementEndringNew(FILDETALJER_FILUUID, sladdetVariant.getFilUuid()),
					ArkivElementEndringTO.arkivElementEndringNew(FILDETALJER_VARIANTFORMAT, SLADDET.name()),
					ArkivElementEndringTO.arkivElementEndringNew(fildetaljerSkjermingTypeVariant(ARKIV), enumToString(skjermingType))
				));
			});
	}

	private void loggOpphevSladdDokument(List<JournalpostDokumentInfoRelasjon> relasjoner, long dokumentInfoId, SkjermingTypeCode forrigeArkivSkjerming, String sladdetFilUuid, boolean sladdetVariantSlettet) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.distinct()
			.forEach(journalpostId -> {
				List<ArkivElementEndringTO> endringer = new ArrayList<>();
				endringer.add(ArkivElementEndringTO.builder()
					.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
					.fraVerdi(enumToString(forrigeArkivSkjerming))
					.tilVerdi(null)
					.build());
				if (sladdetVariantSlettet) {
					endringer.add(ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_FILUUID)
						.fraVerdi(sladdetFilUuid)
						.tilVerdi(null)
						.build());
					endringer.add(ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(SLADDET.name())
						.tilVerdi(null)
						.build());
				}
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.hjemmel(enumToString(forrigeArkivSkjerming))
					.aksjon(AksjonsTypeCode.SLADD_DOKUMENT)
					.build(), endringer);
			});
	}

	private void gjenopprettSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner, DokumentInfo dokumentInfo, SkjermingTypeCode skjermingType) {
		if (skjermingType == null) {
			return;
		}

		long dokumentInfoId = dokumentInfo.getDokumentInfoId();

		// dette fases ut når skjerming settes rett på dokumentinfo
		relasjoner.stream()
			.distinct()
			.forEach(relasjon -> {
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(relasjon.getJournalpostId())
					.dokumentInfoId(dokumentInfoId)
					.hjemmel(enumToString(skjermingType))
					.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
					.build(), List.of(ArkivElementEndringTO.builder()
					.arkivElement(RELASJON_SKJERMING_TYPE)
					.fraVerdi(null)
					.tilVerdi(enumToString(skjermingType))
					.build()));
				relasjon.setSkjermingType(skjermingType);
			});

		if (dokumentInfo.getSkjermingType() == null) {
			aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
				.dokumentInfoId(dokumentInfoId)
				.hjemmel(enumToString(skjermingType))
				.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
				.build(), List.of(ArkivElementEndringTO.builder()
				.arkivElement(DOKUMENT_INFO_SKJERMING_TYPE)
				.fraVerdi(null)
				.tilVerdi(enumToString(skjermingType))
				.build()));
			dokumentInfo.setSkjermingType(skjermingType);
		}
	}

	private void gjenopprettSkjermingForJournalposter(List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode skjermingType) {
		if (skjermingType == null) {
			return;
		}

		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.distinct()
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(journalpost -> Objects.isNull(journalpost.getSkjermingType()))
			.filter(SladdDokumentService::journalpostHarKunSkjermedeRelasjoner)
			.forEach(journalpost -> {
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
						.journalpostId(journalpost.getJournalpostId())
						.hjemmel(null)
						.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
						.build(),
					List.of(ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_SKJERMING_TYPE)
						.fraVerdi(null)
						.tilVerdi(enumToString(skjermingType))
						.build()));
				journalpost.setSkjermingType(skjermingType, MDC.get(MDC_CONSUMER_ID), MDC.get(MDC_USER_NAME));
			});
	}

	private static boolean journalpostHarKunSkjermedeRelasjoner(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjoner().stream()
			.allMatch(relasjon -> relasjon.getSkjermingType() != null);
	}

}
