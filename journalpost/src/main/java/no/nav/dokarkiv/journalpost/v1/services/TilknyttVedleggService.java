package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.ShallowDokumentInfoCopier;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.DOKUMENT_TILLATES_IKKE_GJENBRUKT;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.IKKE_FUNNET;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.TILKNYTNING_FEILET;
import static no.nav.dokarkiv.journalpost.v1.api.ArsakKode.UGYLDIG_STATUS;

@Service(value = "tilknyttVedleggService")
@Slf4j
public class TilknyttVedleggService {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final ShallowDokumentInfoCopier shallowDokumentInfoCopier;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final TilknyttVedleggValidator tilknyttVedleggValidator;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator;
	private final AccessLookupJournalpost accessLookupJournalpost;
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";

	public TilknyttVedleggService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, DokumentInfoRepository dokumentInfoRepository, DokumentFilRepository dokumentFilRepository,
								  JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, AccessLookupJournalpost accessLookupJournalpost) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.dokumentFilRepository = dokumentFilRepository;
		this.accessLookupJournalpost = accessLookupJournalpost;
		this.shallowDokumentInfoCopier = new ShallowDokumentInfoCopier();
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.tilknyttVedleggValidator = new TilknyttVedleggValidator();
		this.tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();
	}

	@Deprecated  // Fjernes når vi har skrudd av dokarkivproxy
	public List<FeiledeDokumenter> tilknyttVedleggWithoutQueryingSaf(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		return validateAndTilknyttVedlegg(targetJournalpostId, tilknyttVedleggRequest);
	}

	public List<FeiledeDokumenter> tilknyttVedlegg(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		String tilknyttetAvNavn = MDC.get(MDC_CONSUMER_ID);

		var accessControlledDocuments = accessLookupJournalpost.checkDocumentsCanBeAccessedByActor(tilknyttVedleggRequest);

		List<FeiledeDokumenter> failedDocuments = accessControlledDocuments.failedDocuments();

		failedDocuments.addAll(validateAndTilknyttVedlegg(targetJournalpostId, new TilknyttVedleggRequest(tilknyttetAvNavn, accessControlledDocuments.okDocuments())));

		return failedDocuments;
	}


	private List<FeiledeDokumenter> validateAndTilknyttVedlegg(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {

		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);

		List<FeiledeDokumenter> feiledeDokumenter = new ArrayList<>();
		String tilKnyttetAvNavn = tilknyttVedleggRequest.getTilknyttetAvNavn();

		Journalpost targetJournalpost = journalpostRepositorySkjermet.findById(targetJournalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", targetJournalpostId)));
		tilknyttVedleggValidator.validateJournalpostStatus(targetJournalpost);


		for (DokumentVedlegg dokumentVedlegg : tilknyttVedleggRequest.getDokument()) {
			Journalpost sourceJournalpost = journalpostRepositorySkjermet.findById(dokumentVedlegg.getKildeJournalpostId()).orElse(null);
			DokumentInfo sourceDokumentInfo = dokumentInfoRepository.findById(Long.parseLong(dokumentVedlegg.getDokumentInfoId()))
					.orElse(null);
			FilDetaljer filDetaljerSladdet = finnSladdetFildetaljer(sourceDokumentInfo);
			FilDetaljer filDetaljerArkiv = finnArkivFildetaljer(sourceDokumentInfo);

			if (!validateSourceJournalpost(sourceJournalpost, feiledeDokumenter, dokumentVedlegg)) {
				continue;
			}

			if (!validateSourceDokumentInfo(sourceDokumentInfo, targetJournalpostId, feiledeDokumenter, dokumentVedlegg)) {
				continue;
			}

			if (filDetaljerSladdet != null) {
				if (dokumentFilRepository.existsByFilUuid(filDetaljerSladdet.getFilUuid())) {
					tilknyttDokumentInfoCopySomVedleggPaaJournalpost(tilKnyttetAvNavn, targetJournalpostId, sourceDokumentInfo, filDetaljerSladdet, dokumentVedlegg, targetJournalpost, feiledeDokumenter);
				} else {
					addToFeiletDokumentList(feiledeDokumenter, IKKE_FUNNET, dokumentVedlegg);
				}
			} else if (filDetaljerArkiv != null) {
				if (dokumentFilRepository.existsByFilUuid(filDetaljerArkiv.getFilUuid())) {
					tilknyttDokumentInfoSomVedleggPaaJournalpost(tilKnyttetAvNavn, sourceDokumentInfo, dokumentVedlegg, targetJournalpost, feiledeDokumenter);
				} else {
					addToFeiletDokumentList(feiledeDokumenter, IKKE_FUNNET, dokumentVedlegg);
				}
			} else {
				addToFeiletDokumentList(feiledeDokumenter, DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			}

		}
		return feiledeDokumenter;
	}

	private void tilknyttDokumentInfoCopySomVedleggPaaJournalpost(String tilKnyttetAvNavn, Long targetJournalpostId, DokumentInfo sourceDokumentInfo, FilDetaljer filDetaljerSladdet, DokumentVedlegg dokumentVedlegg, Journalpost journalpost, List<FeiledeDokumenter> feiledeDokumenterList) {
		log.info(MDC.get(MDC_REQUEST_ID) + " legger til en kopi av dokumentinfo med dokumentInfoId={} på journalpost journalpostId={} da variant=SLADDET. Kopi av dokumentinfo vil få variant=ARKIV", dokumentVedlegg
				.getDokumentInfoId(), targetJournalpostId);
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		DokumentInfo dokumentInfoCopy = createDokumentInfoCopy(sourceDokumentInfo, consumerId);

		FilDetaljer fildetaljerCopy = createFildetaljerCopy(filDetaljerSladdet, dokumentInfoCopy, consumerId);

		DokumentFil dokumentFilCopy = fildetaljerCopy.createDokumentFil();
		dokumentFilCopy.setOpprettetKildeNavn(consumerId);
		dokumentInfoCopy.addFilDetaljer(fildetaljerCopy);

		dokumentFilRepository.persist(dokumentFilCopy);
		dokumentInfoRepository.persist(dokumentInfoCopy);

		tilknyttDokumentInfoSomVedleggPaaJournalpost(tilKnyttetAvNavn, dokumentInfoCopy, dokumentVedlegg, journalpost, feiledeDokumenterList);
	}

	private DokumentInfo createDokumentInfoCopy(DokumentInfo dokumentInfo, String consumerId) {
		DokumentInfo dokumentInfoCopy = shallowDokumentInfoCopier.copy(dokumentInfo);
		dokumentInfoCopy.setOpprettetKildeNavn(consumerId);
		dokumentInfoCopy.setEndretAvNavn(null);
		dokumentInfoCopy.setOriginalJournalpost(null);
		dokumentInfoCopy.getTilleggsopplysninger().put(TILLEGGOPPLYSNINGER_KEY, dokumentInfo.getDokumentInfoId().toString());

		return dokumentInfoCopy;
	}

	private FilDetaljer createFildetaljerCopy(FilDetaljer filDetaljer, DokumentInfo dokumentInfo, String consumerId) {
		FilDetaljer filDetaljerCopy = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(filDetaljer.getFileContent())
				.filUuid(FilDetaljer.generateUuid())
				.onDemandId(filDetaljer.getOnDemandId())
				.onDemandInstans(filDetaljer.getOnDemandInstans())
				.metaforceInstanceId(filDetaljer.getMetaforceInstanceId())
				.filtype(filDetaljer.getFiltype())
				.variantFormat(VariantFormatCode.ARKIV)
				.batchNavn(filDetaljer.getBatchNavn())
				.filnavn(filDetaljer.getFilnavn())
				.filstorrelse(filDetaljer.getFilstorrelse())
				.build();


		filDetaljerCopy.setOpprettetKildeNavn(consumerId);
		byte[] fil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid()).getFil();
		filDetaljerCopy.setFileContent(fil);

		return filDetaljerCopy;
	}

	private List<FeiledeDokumenter> addToFeiletDokumentList(List<FeiledeDokumenter> feiledeDokumenterList, ArsakKode arsakKode, DokumentVedlegg dokumentVedlegg) {
		feiledeDokumenterList.add(FeiledeDokumenter.builder()
				.kildeJournalpostId(dokumentVedlegg.getKildeJournalpostId().toString())
				.dokumentInfoId(dokumentVedlegg.getDokumentInfoId())
				.arsakKode(arsakKode)
				.build());
		return feiledeDokumenterList;
	}

	private void tilknyttDokumentInfoSomVedleggPaaJournalpost(String tilKnyttetAvNavn, DokumentInfo dokumentInfo, DokumentVedlegg dokumentVedlegg, Journalpost journalpost, List<FeiledeDokumenter> feiledeDokumenterList) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon;
		try {
			journalpostDokumentInfoRelasjon = createJournalpostDokumentInfoRelasjon(tilKnyttetAvNavn, dokumentInfo, journalpost);
			journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
			journalpostRepositorySkjermet.save(journalpost);
			log.info("Journalpost med journalpostId={} har fått tilknyttet dokument vedlegg fra DokumentInfoId={} ", journalpost
					.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		} catch (Exception e) {
			addToFeiletDokumentList(feiledeDokumenterList, TILKNYTNING_FEILET, dokumentVedlegg);
		}
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(String tilKnyttetAvNavn, DokumentInfo dokumentInfo, Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(tilKnyttetAvNavn)
				.build();
		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		return journalpostDokumentInfoRelasjon;
	}

	private FilDetaljer finnSladdetFildetaljer(DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			return dokumentInfo.getFildetaljerListe().stream()
					.filter(filDetaljer1 -> VariantFormatCode.SLADDET.equals(filDetaljer1.getVariantFormat()))
					.findAny()
					.orElse(null);
		} else {
			return null;
		}
	}

	private FilDetaljer finnArkivFildetaljer(DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			return dokumentInfo.getFildetaljerListe().stream()
					.filter(filDetaljer1 -> VariantFormatCode.ARKIV.equals(filDetaljer1.getVariantFormat()))
					.findAny()
					.orElse(null);
		} else {
			return null;
		}
	}

	private Boolean checkDuplicate(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		return (checkDuplicateDokumentInfoRelasjon(targetJournalpostId, dokumentInfo) || checkDuplicateDokumentInfoCopy(targetJournalpostId, dokumentInfo));
	}

	private Boolean checkDuplicateDokumentInfoRelasjon(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		return journalpostRepositorySkjermet.findAllJournalpostIdsByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.contains(targetJournalpostId);
	}

	private Boolean checkDuplicateDokumentInfoCopy(Long targetJournalpostId, DokumentInfo dokumentInfo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(targetJournalpostId);
		return journalpostDokumentInfoRelasjons.stream()
				.anyMatch(d -> dokumentInfo.getDokumentInfoId().toString().equals(d.getDokumentInfo()
						.getTilleggsopplysninger()
						.get(TILLEGGOPPLYSNINGER_KEY)));
	}

	private Boolean validateSourceJournalpost(Journalpost sourceJournalpost, List<FeiledeDokumenter> feiledeDokumenterList, DokumentVedlegg dokumentVedlegg) {
		if (sourceJournalpost == null) {
			addToFeiletDokumentList(feiledeDokumenterList, IKKE_FUNNET, dokumentVedlegg);
			return false;
		} else if (!tilknyttVedleggValidator.validateSourceJournalpostStatus(sourceJournalpost)) {
			addToFeiletDokumentList(feiledeDokumenterList, UGYLDIG_STATUS, dokumentVedlegg);
			return false;
		}
		return true;
	}

	private Boolean validateSourceDokumentInfo(DokumentInfo sourceDokumentInfo, Long targetJournalpostId, List<FeiledeDokumenter> feiledeDokumenterList, DokumentVedlegg dokumentVedlegg) {
		boolean valid = true;
		if (sourceDokumentInfo == null) {
			addToFeiletDokumentList(feiledeDokumenterList, IKKE_FUNNET, dokumentVedlegg);
			valid = false;
		} else if (checkDuplicate(targetJournalpostId, sourceDokumentInfo)) {
			log.info(MDC.get(MDC_REQUEST_ID) + " kan ikke knytte dokumentinfo med dokumentInfoId={} til journalpost med journalpostId={} fordi den allerede er tilknyttet journalposten", dokumentVedlegg
					.getDokumentInfoId(), targetJournalpostId);
			valid = false;
		} else if (!tilknyttVedleggValidator.validateDokumentInfo(sourceDokumentInfo)) {
			addToFeiletDokumentList(feiledeDokumenterList, DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			valid = false;
		}
		return valid;
	}

}
