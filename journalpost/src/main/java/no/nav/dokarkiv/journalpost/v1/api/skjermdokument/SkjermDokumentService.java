package no.nav.dokarkiv.journalpost.v1.api.skjermdokument;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOpphevSkjermingException;
import no.nav.dokarkiv.core.exceptions.KanIkkeSkjermeDokumentException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_SKJERMING_TYPE;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;

@Slf4j
@Service
public class SkjermDokumentService {

	private static final EnumSet<JournalStatusCode> JOURNALSTATUSER_SOM_TILLATER_SKJERMING = EnumSet.of(J, U, UB, FL, FS, E);

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

		validerJournalpoststatuser(dokumentInfoId, relasjoner);
		setSkjermingForDokumentInfo(relasjoner, skjermingTypeCode);
		setSkjermingForJournalposter(dokumentInfoId, hjemmelCode, relasjoner, skjermingTypeCode);

		log.info("skjermdokument har skjermet dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private static void validerJournalpoststatuser(long dokumentInfoId, List<JournalpostDokumentInfoRelasjon> relasjoner) {
		String journalposterSomIkkeKanSkjermes = relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpost)
			.filter(journalpost -> !JOURNALSTATUSER_SOM_TILLATER_SKJERMING.contains(journalpost.getJournalstatus()))
			.sorted(Comparator.comparing(Journalpost::getJournalpostId))
			.map(journalpost -> "%d (status=%s)".formatted(journalpost.getJournalpostId(), journalpost.getJournalstatus()))
			.collect(Collectors.joining(", "));

		if (!journalposterSomIkkeKanSkjermes.isEmpty()) {
			throw new KanIkkeSkjermeDokumentException(
				"Dokument med dokumentInfoId=%d kan ikke skjermes fordi følgende journalposter har journalstatus som ikke tillater skjerming: %s. Tillatte statuser er %s.".formatted(
						dokumentInfoId,
						journalposterSomIkkeKanSkjermes,
						JOURNALSTATUSER_SOM_TILLATER_SKJERMING));
		}
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

			.filter(SkjermDokumentService::journalpostHasOnlyDocumentsThatAreSkjermet)
			.forEach(journalpost -> {
				oppdaterSkjermingForJournalpost(journalpost, skjermingTypeCode);
			});
	}

	private void setSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode skjermingTypeCode) {
		var dokumentInfo = relasjoner.getFirst().getDokumentInfo();
		dokumentInfo.setSkjermingType(skjermingTypeCode);
		dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	@Transactional
	public void opphevSkjermDokumentMedDokumentInfoId(long dokumentInfoId) {

		List<JournalpostDokumentInfoRelasjon> relasjoner = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);

		if (relasjoner.isEmpty()) {
			throw new DokumentInfoIkkeFunnetException("Fant ikke dokument og dokumentrelasjon for dokumentInfoId=%d".formatted(dokumentInfoId));
		}

		SkjermingTypeCode forrigeSkjermingType = relasjoner.getFirst().getDokumentInfo().getSkjermingType();
		if (forrigeSkjermingType == null) {
			throw new KanIkkeOpphevSkjermingException("Kan ikke oppheve skjerming for dokument som ikke er skjermet");
		}

		opphevSkjermingForDokumentInfo(relasjoner);
		opphevSkjermingForJournalposter(dokumentInfoId, relasjoner, forrigeSkjermingType);

		log.info("opphevSkjermDokument har fjernet skjerming fra dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private void opphevSkjermingForJournalposter(long dokumentInfoId, List<JournalpostDokumentInfoRelasjon> relasjoner, SkjermingTypeCode forrigeSkjermingType) {
		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.forEach(journalpostId ->
				aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
					.build(), List.of(ArkivElementEndringTO.builder()
					.arkivElement(RELASJON_SKJERMING_TYPE)
					.fraVerdi(forrigeSkjermingType.name())
					.tilVerdi(null)
				.build()))
			);

		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)

			.filter(Optional::isPresent)
			.map(Optional::get)

			.filter(Journalpost::isSkjermet)
			.filter(SkjermDokumentService::journalpostHasOnlyDocumentsThatAreNotSkjermet)
			.forEach(journalpost -> {
				oppdaterSkjermingForJournalpost(journalpost, null);
			});
	}

	private void opphevSkjermingForDokumentInfo(List<JournalpostDokumentInfoRelasjon> relasjoner) {
		var dokumentInfo = relasjoner.getFirst().getDokumentInfo();
		dokumentInfo.setSkjermingType(null);
		dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	private static boolean journalpostHasOnlyDocumentsThatAreSkjermet(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
			.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
			.allMatch(DokumentInfo::isSkjermet);
	}

	private static boolean journalpostHasOnlyDocumentsThatAreNotSkjermet(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjonerAdmin().stream()
			.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
			.allMatch(not(DokumentInfo::isSkjermet));
	}

	private void oppdaterSkjermingForJournalpost(Journalpost journalpost, SkjermingTypeCode skjermingTypeCode) {
		journalpost.setSkjermingType(skjermingTypeCode, MDC.get(MDC_CONSUMER_ID), MDC.get(MDC_USER_NAME));
	}
}
