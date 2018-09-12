package no.nav.dokarkiv.slettdokument.service;


import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettdokument.exceptions.DokumentAlleredeSlettetException;
import no.nav.dokarkiv.slettdokument.exceptions.ForMangeJournalpostDokumentInfoRelasjonerException;
import no.nav.dokarkiv.slettdokument.exceptions.IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;


@Service
@Slf4j
public class SlettDokumentRestService {

	//	private RestTemplate restTemplate;
	private Long inputJournalpostId = 0L;
	private Long inputDokumentInfoId = 0L;


	@Inject
	DokumentinfoRepository dokumentinfoRepository;
	@Inject
	JoarkRepository joarkRepository;
	@Inject
	JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;


	public void slettDokument(Long journalpostId, Long dokumentInfoId) {
		inputJournalpostId = journalpostId;
		inputDokumentInfoId = dokumentInfoId;

		List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner = journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(dokumentInfoId);
		if (gyldigSlettingAvEttDokument(jpDokInfoRelasjoner)) {
			log.info("slettDokument sletter dokument med journalpostId={}, dokumentInfoId={}", journalpostId, dokumentInfoId);
			jpDokInfoRelasjoner.get(0).getDokumentInfo().setSlettet(true);
			dokumentinfoRepository.save(jpDokInfoRelasjoner.get(0).getDokumentInfo());
		}
	}

	public void slettDokumentMedJournalpostId(Long journalpostId) {
		Long dokumentInfoId = journalpostDokumentInfoRelasjonRepository.findDokumentInfoIdByJournalpostId(journalpostId);
		if (nonNull(dokumentInfoId)) {
			slettDokument(journalpostId, dokumentInfoId);
		} else {
			throw new DocumentNotFoundException("slettDokument kan ikke finne dokument med journalpostId=" + journalpostId);
		}
	}

	private boolean gyldigSlettingAvEttDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner) {
		return (gyldigtAntallRelasjonerForSlettingAvEttDokument(jpDokInfoRelasjoner) &&
				gyldigInputForJournalpostId(jpDokInfoRelasjoner.get(0).getJournalpost().getJournalpostId()) &&
				gyldigSletteStatusForDokument(jpDokInfoRelasjoner.get(0).getDokumentInfo()));
	}

	private boolean gyldigtAntallRelasjonerForSlettingAvEttDokument(List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjoner) {
		if (jpDokInfoRelasjoner.isEmpty()) {
			throw new DocumentNotFoundException("slettDokument kan ikke finne noen journal for journalpostId=" + inputJournalpostId + ", dokumentInfoId=" + inputDokumentInfoId);
		} else if (jpDokInfoRelasjoner.size() > 1) {
			//throw toManyReationsError
			throw new ForMangeJournalpostDokumentInfoRelasjonerException("slettDokument kan ikke slette dokument som har relasjoner med flere journalposter. " +
					"DokumentInfoId=" + inputDokumentInfoId + " har relasjoner med " + jpDokInfoRelasjoner.size() + " journalposter,");
		} else //if (jpDokInfoRelasjoner.size()==1){
		{
			return true;
		}
	}

	private boolean gyldigInputForJournalpostId(Long journalpostId) {
		if (journalpostId.equals(inputJournalpostId)) {
			return true;
		} else if (nonNull(journalpostDokumentInfoRelasjonRepository.findByJournalpostId(journalpostId))) {
			throw new IngenRelasjonMellomJournalpostIdOgDokumentInfoIdException("slettDokument finner ingen relasjon mellom journalpostId=" + journalpostId
					+ " og dokumentInfoId=" + inputDokumentInfoId);
		} else
		//Journalpost ikke funnet eller invalidArgument?
		{
			throw new JournalpostIkkeFunnetException("slettDokument finner ikke en relasjon for journalpost med journalpostId=" + journalpostId);
		}
	}

	private boolean gyldigSletteStatusForDokument(DokumentInfo dokumentInfo) {
		if (isFalse(dokumentInfo.getSlettet())) {
			return true;
		} else {
			throw new DokumentAlleredeSlettetException("slettDokumet har allerede slettet dokumentet med dokumentInfoId=" + dokumentInfo
					.getDokumentInfoId());
		}
	}
}
