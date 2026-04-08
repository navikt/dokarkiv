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
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;

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
			throw new DocumentNotFoundException("Fant ikke dokument og dokumentrelasjon for id=%d".formatted(dokumentInfoId));
		}

		relasjoner.forEach(relasjon -> relasjon.setSkjermingType(skjermingTypeCode));

		aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
			.dokumentInfoId(dokumentInfoId)
			.hjemmel(hjemmelCode.name())
			.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
			.build(), List.of(ArkivElementEndringTO.arkivElementEndringNew("k_skjerming_t", skjermingTypeCode.name())));

		relasjoner.stream()
			.map(JournalpostDokumentInfoRelasjon::getJournalpostId)
			.map(journalpostRepository::fetchByIdWithJournalpostDokumentInfoRelasjoner)

			.filter(Optional::isPresent)
			.map(Optional::get)

			.filter(SkjermDokumentService::journalpostHasOnlyRelationsThatAreSkjermet)
			.forEach(journalpost -> {
				skrivAksjonsloggForJournalpost(journalpost, hjemmelCode);
				journalpost.setSkjermingType(skjermingTypeCode);
			});

		log.info("skjermdokument har skjermet dokument med dokumentInfoId={}", dokumentInfoId);
	}

	private static boolean journalpostHasOnlyRelationsThatAreSkjermet(Journalpost journalpost) {
		return journalpost.getJournalpostDokumentInfoRelasjoner().stream()
			.allMatch(relasjon -> null != relasjon.getSkjermingType());
	}

	private void skrivAksjonsloggForJournalpost(Journalpost journalpost, SkjermDokumentHjemmelCode hjemmelCode) {
			aksjonsLoggService.validateAndSaveAksjonsLogg(AksjonsLoggTO.builder()
				.journalpostId(journalpost.getJournalpostId())
				.hjemmel(enumToString(hjemmelCode))
				.aksjon(AksjonsTypeCode.ENDRE_SKJERMING)
				.build(),
				List.of(ArkivElementEndringTO.builder()
					.arkivElement("k_skjerming_t")
					.fraVerdi(enumToString(journalpost.getSkjermingType()))
					.tilVerdi(enumToString(hjemmelCode == null ? null : hjemmelCode.asSkjermingTypeCode()))
					.build()));
	}
}
