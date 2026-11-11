package no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.DOKUMENT_TILLATES_IKKE_GJENBRUKT;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.IKKE_FUNNET;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.TILKNYTNING_FEILET;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.UGYLDIG_STATUS;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggMapper.addToFeiletDokumentList;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggMapper.createFildetaljerCopy;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggMapper.finnArkivFildetaljer;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggMapper.finnSladdetFildetaljer;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggMapper.mapDokumentInfoSladdet;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggService.TILLEGGOPPLYSNINGER_KEY;

@Slf4j
@Service
public class TilknyttVedleggTransaksjonService {

	private final DokumentInfoRepository dokumentInfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final TilknyttVedleggValidator tilknyttVedleggValidator = new TilknyttVedleggValidator();
	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	public TilknyttVedleggTransaksjonService(DokumentInfoRepository dokumentInfoRepository,
											 DokumentFilRepository dokumentFilRepository,
											 JournalpostRepositorySkjermet journalpostRepositorySkjermet,
											 JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;

	}

	@Transactional
	public List<FeiledeDokumenter> validateAndTilknyttVedlegg(Long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		List<FeiledeDokumenter> failedDocuments = new ArrayList<>();
		final String tilKnyttetAvNavn = tilknyttVedleggRequest.getTilknyttetAvNavn();

		Journalpost targetJournalpost = finnJournalpost(targetJournalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=%s i joark".formatted(targetJournalpostId)));
		tilknyttVedleggValidator.validateJournalpostStatus(targetJournalpost);

		for (DokumentVedlegg dokumentVedlegg : tilknyttVedleggRequest.getDokument()) {

			Journalpost sourceJournalpost = finnJournalpost(dokumentVedlegg.getKildeJournalpostId())
					.orElse(null);
			DokumentInfo sourceDokumentInfo = dokumentInfoRepository.findById(Long.parseLong(dokumentVedlegg.getDokumentInfoId()))
					.orElse(null);

			FilDetaljer filDetaljerSladdet = finnSladdetFildetaljer(sourceDokumentInfo);
			FilDetaljer filDetaljerArkiv = finnArkivFildetaljer(sourceDokumentInfo);


			if (!validateSourceJournalpost(sourceJournalpost, failedDocuments, dokumentVedlegg)) {
				continue;
			}

			if (!validateSourceDokumentInfo(sourceDokumentInfo, targetJournalpostId, failedDocuments, dokumentVedlegg)) {
				continue;
			}

			if (filDetaljerSladdet != null) {
				if (erDokumentFilExists(filDetaljerSladdet.getFilUuid())) {
					tilknyttSladdetDokumentInfoCopySomVedleggPaaJournalpost(tilKnyttetAvNavn, targetJournalpostId,
							sourceDokumentInfo, filDetaljerSladdet, dokumentVedlegg, targetJournalpost, failedDocuments);
				} else {
					addToFeiletDokumentList(failedDocuments, IKKE_FUNNET, dokumentVedlegg);
				}
			} else if (filDetaljerArkiv != null) {
				if (erDokumentFilExists(filDetaljerArkiv.getFilUuid())) {
					tilknyttDokumentInfoSomVedleggPaaJournalpost(tilKnyttetAvNavn, sourceDokumentInfo, dokumentVedlegg, targetJournalpost, failedDocuments);
				} else {
					addToFeiletDokumentList(failedDocuments, IKKE_FUNNET, dokumentVedlegg);
				}
			} else {
				addToFeiletDokumentList(failedDocuments, DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			}
		}
		return failedDocuments;
	}

	private void tilknyttSladdetDokumentInfoCopySomVedleggPaaJournalpost(String tilKnyttetAvNavn, Long targetJournalpostId, DokumentInfo sourceDokumentInfo,
																		 FilDetaljer filDetaljerSladdet, DokumentVedlegg dokumentVedlegg,
																		 Journalpost journalpost, List<FeiledeDokumenter> failedDocuments) {
		log.info("{} legger til en kopi av dokumentinfo med dokumentInfoId={} på journalpost journalpostId={} da variant=SLADDET. Kopi av dokumentinfo vil få variant=ARKIV",
				MDC.get(MDC_REQUEST_ID), sourceDokumentInfo.getDokumentInfoId(), targetJournalpostId);
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		DokumentInfo dokumentInfoCopy = mapDokumentInfoSladdet(sourceDokumentInfo, consumerId);

		FilDetaljer fildetaljerCopy = createFildetaljerCopy(filDetaljerSladdet, dokumentInfoCopy, consumerId);
		fildetaljerCopy.setFileContent(dokumentFilRepository.findByFilUuid(filDetaljerSladdet.getFilUuid()).getFil());

		DokumentFil dokumentFilCopy = fildetaljerCopy.createDokumentFil();
		dokumentFilCopy.setOpprettetKildeNavn(consumerId);
		dokumentInfoCopy.addFilDetaljer(fildetaljerCopy);

		dokumentFilRepository.persist(dokumentFilCopy);
		dokumentInfoRepository.persist(dokumentInfoCopy);

		tilknyttDokumentInfoSomVedleggPaaJournalpost(tilKnyttetAvNavn, dokumentInfoCopy, dokumentVedlegg, journalpost, failedDocuments);
	}

	private Boolean checkDuplicate(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		return (checkDuplicateDokumentInfoRelasjon(targetJournalpostId, dokumentInfo) || checkDuplicateDokumentInfoCopy(targetJournalpostId, dokumentInfo));
	}

	private Boolean checkDuplicateDokumentInfoRelasjon(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		return journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.contains(targetJournalpostId);
	}

	private Boolean checkDuplicateDokumentInfoCopy(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		List<no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(targetJournalpostId);
		return journalpostDokumentInfoRelasjons.stream()
				.anyMatch(d -> dokumentInfo.getDokumentInfoId().toString().equals(d.getDokumentInfo()
						.getTilleggsopplysninger()
						.get(TILLEGGOPPLYSNINGER_KEY)));
	}

	private Optional<Journalpost> finnJournalpost(Long journalpostId) {
		return journalpostRepositorySkjermet.findById(journalpostId);
	}

	private boolean erDokumentFilExists(String filUuid) {
		return dokumentFilRepository.existsByFilUuid(filUuid);
	}

	private void tilknyttDokumentInfoSomVedleggPaaJournalpost(String tilKnyttetAvNavn, DokumentInfo dokumentInfo, DokumentVedlegg dokumentVedlegg,
															  Journalpost journalpost, List<FeiledeDokumenter> failedDocuments) {
		try {
			JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon;
			journalpostDokumentInfoRelasjon = createJournalpostDokumentInfoRelasjon(tilKnyttetAvNavn, dokumentVedlegg.getRekkefoelge(), dokumentInfo, journalpost);
			journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
			journalpostRepositorySkjermet.save(journalpost);
			log.info("Journalpost med journalpostId={} har fått tilknyttet dokument vedlegg fra DokumentInfoId={} ", journalpost
					.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		} catch (Exception e) {
			addToFeiletDokumentList(failedDocuments, TILKNYTNING_FEILET, dokumentVedlegg);
		}
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(String tilKnyttetAvNavn, Integer rekkefoelge, DokumentInfo dokumentInfo, Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(tilKnyttetAvNavn)
				.rekkefoelge(rekkefoelge)
				.build();
		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return journalpostDokumentInfoRelasjon;
	}

	private Boolean validateSourceJournalpost(Journalpost sourceJournalpost, List<FeiledeDokumenter> feiledeDokumenterList, DokumentVedlegg dokumentVedlegg) {
		if (sourceJournalpost == null) {
			addToFeiletDokumentList(feiledeDokumenterList, IKKE_FUNNET, dokumentVedlegg);
			return false;
		} else if (!tilknyttVedleggValidator.validateSourceJournalpost(sourceJournalpost)) {
			addToFeiletDokumentList(feiledeDokumenterList, UGYLDIG_STATUS, dokumentVedlegg);
			return false;
		}
		return true;
	}

	private Boolean validateSourceDokumentInfo(DokumentInfo sourceDokumentInfo, Long targetJournalpostId,
											   List<FeiledeDokumenter> feiledeDokumenterList, DokumentVedlegg dokumentVedlegg) {
		boolean valid = true;
		if (sourceDokumentInfo == null) {
			addToFeiletDokumentList(feiledeDokumenterList, IKKE_FUNNET, dokumentVedlegg);
			valid = false;
		} else if (checkDuplicate(targetJournalpostId, sourceDokumentInfo)) {
			log.info("{} kan ikke knytte dokumentinfo med dokumentInfoId={} til journalpost med journalpostId={} fordi den allerede er tilknyttet journalposten", MDC.get(MDC_REQUEST_ID), sourceDokumentInfo.getDokumentInfoId(), targetJournalpostId);
			valid = false;
		} else if (!tilknyttVedleggValidator.validateSourceDokumentInfo(sourceDokumentInfo)) {
			addToFeiletDokumentList(feiledeDokumenterList, DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			valid = false;
		}
		return valid;
	}
}
