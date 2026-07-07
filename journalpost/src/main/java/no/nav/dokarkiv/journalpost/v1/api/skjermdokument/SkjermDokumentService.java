package no.nav.dokarkiv.journalpost.v1.api.skjermdokument;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOpphevSkjermingException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;

@Slf4j
@Service
public class SkjermDokumentService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final AksjonsLoggService aksjonsLoggService;
	private final JournalpostRepository journalpostRepository;

	public SkjermDokumentService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, AksjonsLoggService aksjonsLoggService, JournalpostRepository journalpostRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.journalpostRepository = journalpostRepository;
	}

	@Transactional
	public void skjermDokumentMedDokumentInfoId(long dokumentInfoId, SkjermDokumentHjemmelCode hjemmelCode) {

		SkjermingTypeCode skjermingTypeCode = hjemmelCode.asSkjermingTypeCode();
		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		if (relasjoner.isEmpty()) {
			throw new DokumentInfoIkkeFunnetException("Fant ikke dokument og dokumentrelasjon for dokumentInfoId=%d".formatted(dokumentInfoId));
		}

		setSkjermingForDokumentInfo(relasjoner, skjermingTypeCode);
		setSkjermingForJournalposter(dokumentInfoId, hjemmelCode, relasjoner, skjermingTypeCode);

		log.info("skjermdokument har skjermet dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private void setSkjermingForJournalposter(long dokumentInfoId, SkjermDokumentHjemmelCode hjemmelCode, List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode skjermingTypeCode) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.forEach(journalpostId ->
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.hjemmel(hjemmelCode.name())
					.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
					.build(), List.of(ArkivElementEndringTO.arkivElementEndringNew(RELASJON_SKJERMING_TYPE, skjermingTypeCode.name())))
			);

		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)

			.filter(Optional::isPresent)
			.map(Optional::get)

			.filter(SkjermDokumentService::journalpostHasOnlyRelationsThatAreSkjermet)
			.forEach(journalpost -> {
				oppdaterSkjermingForJournalpost(journalpost, skjermingTypeCode);
			});
	}

	private void setSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode skjermingTypeCode) {
		var dokumentInfo = relasjoner.getFirst().getDokumentInfo();
		dokumentInfo.setSkjermingType(skjermingTypeCode);
		relasjoner.forEach(relasjon -> oppdaterSkjermingForJournalpostDokumentRelasjon(relasjon, skjermingTypeCode));
	}

	@Transactional
	public void opphevSkjermDokumentMedDokumentInfoId(long dokumentInfoId) {

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		if (relasjoner.isEmpty()) {
			throw new DokumentInfoIkkeFunnetException("Fant ikke dokument og dokumentrelasjon for dokumentInfoId=%d".formatted(dokumentInfoId));
		}

		Optional<SkjermingTypeCode> forrigeSkjermingType = relasjoner.stream().map(JournalpostDokumentInfoRelasjon::getSkjermingType).filter(Objects::nonNull).findAny();
		if (forrigeSkjermingType.isEmpty()) {
			throw new KanIkkeOpphevSkjermingException("Kan ikke oppheve skjerming for dokument som ikke er skjermet");
		}

		opphevSkjermingForDokumentInfo(relasjoner);
		opphevSkjermingForJournalposter(dokumentInfoId, relasjoner, forrigeSkjermingType);

		log.info("opphevSkjermDokument har fjernet skjerming fra dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private void opphevSkjermingForJournalposter(long dokumentInfoId, List<JournalpostDokumentInfoRelasjon> relasjoner, Optional<SkjermingTypeCode> forrigeSkjermingType) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.forEach(journalpostId ->
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
					.build(), List.of(ArkivElementEndringTO.builder()
					.arkivElement(RELASJON_SKJERMING_TYPE)
					.fraVerdi(forrigeSkjermingType.get().name())
					.tilVerdi(null)
				.build()))
			);

		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)

			.filter(Optional::isPresent)
			.map(Optional::get)

			.filter(jp -> null != jp.getSkjermingType())
			.filter(SkjermDokumentService::journalpostHasOnlyRelationsThatAreNotSkjermet)
			.forEach(journalpost -> {
				oppdaterSkjermingForJournalpost(journalpost, null);
			});
	}

	private void opphevSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner) {
		var dokumentInfo = relasjoner.getFirst().getDokumentInfo();
		dokumentInfo.setSkjermingType(null);
		relasjoner.forEach(relasjon -> oppdaterSkjermingForJournalpostDokumentRelasjon(relasjon, null));
	}

	private static void oppdaterSkjermingForJournalpostDokumentRelasjon(JournalpostDokumentInfoRelasjon relasjon, SkjermingTypeCode skjermingTypeCode) {
		relasjon.setSkjermingType(skjermingTypeCode);
		relasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		relasjon.getDokumentInfo().setSkjermingType(skjermingTypeCode);
	}

	private static boolean journalpostHasOnlyRelationsThatAreSkjermet(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjoner().stream()
			.allMatch(relasjon -> null != relasjon.getSkjermingType());
	}

	private static boolean journalpostHasOnlyRelationsThatAreNotSkjermet(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjoner().stream()
			.allMatch(relasjon -> null == relasjon.getSkjermingType());
	}

	private void oppdaterSkjermingForJournalpost(Journalpost journalpost, SkjermingTypeCode skjermingTypeCode) {
		journalpost.setSkjermingType(skjermingTypeCode, MDC.get(MDC_CONSUMER_ID), MDC.get(MDC_USER_NAME));
	}
}
