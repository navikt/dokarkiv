package no.nav.dokarkiv.journalpost.v1.services;

import com.nimbusds.jwt.JWTClaimsSet;
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
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.security.TokenGrantValidator;
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.ShallowDokumentInfoCopier;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggRequestValidator;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Service(value = "tilknyttVedleggService")
@Slf4j
public class TilknyttVedleggService {

	private final JoarkRepositorySkjermet joarkRepository;
	private final ShallowDokumentInfoCopier shallowDokumentInfoCopier;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final TilknyttVedleggValidator tilknyttVedleggValidator;
	private final TilknyttVedleggRequestValidator tilknyttVedleggRequestValidator;
	private final AccessLookupJournalpost accessLookupJournalpost;
	private final TokenGrantValidator tokenGrantValidator;
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";
	public static final String BEARER = "Bearer ";

	@Inject
	public TilknyttVedleggService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, AccessLookupJournalpost accessLookupJournalpost, TokenGrantValidator tokenGrantValidator) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.accessLookupJournalpost = accessLookupJournalpost;
		this.tokenGrantValidator = tokenGrantValidator;
		this.shallowDokumentInfoCopier = new ShallowDokumentInfoCopier();
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.tilknyttVedleggValidator = new TilknyttVedleggValidator();
		this.tilknyttVedleggRequestValidator = new TilknyttVedleggRequestValidator();
	}

	@Deprecated
	public List<FeiledeDokumenter> tilknyttVedleggWithoutQueryingSaf(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		return tilknyttVedlegg(targetJournalpostId, tilknyttVedleggRequest);
	}

	public List<FeiledeDokumenter> tilknyttVedlegg(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest, String auth) {
		JWTClaimsSet tokenClaims = tokenGrantValidator.validateOnBehalfOfAccessToken(auth);
		String tilknyttetAvNavn = tokenClaims.getSubject();

		// For hvert vedlegg må saksbehandlers tilgang sjekkes i saf med 🎷OBO-tokenet 🎷
		var accessControlledDocuments = accessLookupJournalpost.checkDocumentsCanBeAccessedByActor(targetJournalpostId, tilknyttVedleggRequest, auth);

		List<FeiledeDokumenter> failedDocuments = accessControlledDocuments.failedDocuments();
		if (!accessControlledDocuments.okDocuments().isEmpty()) {
			failedDocuments.addAll(tilknyttVedlegg(targetJournalpostId, new TilknyttVedleggRequest(tilknyttetAvNavn, accessControlledDocuments.okDocuments())));
		}

		return failedDocuments;
	}


	private List<FeiledeDokumenter> tilknyttVedlegg(long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {

		tilknyttVedleggRequestValidator.validateRequest(tilknyttVedleggRequest);

		List<FeiledeDokumenter> feiledeDokumenter = new ArrayList<>();
		String tilKnyttetAvNavn = tilknyttVedleggRequest.getTilknyttetAvNavn();

		Journalpost targetJournalpost = joarkRepository.findById(targetJournalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", targetJournalpostId)));
		tilknyttVedleggValidator.validateJournalpostStatus(targetJournalpost);


		for (DokumentVedlegg dokumentVedlegg : tilknyttVedleggRequest.getDokument()) {
			Journalpost sourceJournalpost = joarkRepository.findById(dokumentVedlegg.getKildeJournalpostId()).orElse(null);
			DokumentInfo sourceDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(Long.parseLong(dokumentVedlegg.getDokumentInfoId()))
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
				tilknyttDokumentInfoCopySomVedleggPaaJournalpost(tilKnyttetAvNavn, targetJournalpostId, sourceDokumentInfo, filDetaljerSladdet, dokumentVedlegg, targetJournalpost, feiledeDokumenter);
			} else if (filDetaljerArkiv != null) {
				tilknyttDokumentInfoSomVedleggPaaJournalpost(tilKnyttetAvNavn, sourceDokumentInfo, dokumentVedlegg, targetJournalpost, feiledeDokumenter);
			} else {
				addToFeiletDokumentList(feiledeDokumenter, ArsakKode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
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

		dokumentFilRepository.save(dokumentFilCopy);
		dokumentinfoRepository.save(dokumentInfoCopy);

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
			joarkRepository.save(journalpost);
			log.info("Journalpost med journalpostId={} har fått tilknyttet dokument vedlegg fra DokumentInfoId={} ", journalpost
					.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		} catch (Exception e) {
			addToFeiletDokumentList(feiledeDokumenterList, ArsakKode.TILKNYTNING_FEILET, dokumentVedlegg);
		}
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(String tilKnyttetAvNavn, DokumentInfo dokumentInfo, Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn(tilKnyttetAvNavn)
				.build();
		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(tilKnyttetAvNavn);
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
		return joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfo.getDokumentInfoId())
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
			addToFeiletDokumentList(feiledeDokumenterList, ArsakKode.IKKE_FUNNET, dokumentVedlegg);
			return false;
		} else if (!tilknyttVedleggValidator.validateSourceJournalpostStatus(sourceJournalpost)) {
			addToFeiletDokumentList(feiledeDokumenterList, ArsakKode.UGYLDIG_STATUS, dokumentVedlegg);
			return false;
		}
		return true;
	}

	private Boolean validateSourceDokumentInfo(DokumentInfo sourceDokumentInfo, Long targetJournalpostId, List<FeiledeDokumenter> feiledeDokumenterList, DokumentVedlegg dokumentVedlegg) {
		boolean valid = true;
		if (sourceDokumentInfo == null) {
			addToFeiletDokumentList(feiledeDokumenterList, ArsakKode.IKKE_FUNNET, dokumentVedlegg);
			valid = false;
		} else if (checkDuplicate(targetJournalpostId, sourceDokumentInfo)) {
			log.info(MDC.get(MDC_REQUEST_ID) + " kan ikke knytte dokumentinfo med dokumentInfoId={} til journalpost med journalpostId={} fordi den allerede er tilknyttet journalposten", dokumentVedlegg
					.getDokumentInfoId(), targetJournalpostId);
			valid = false;
		} else if (!tilknyttVedleggValidator.validateDokumentInfo(sourceDokumentInfo)) {
			addToFeiletDokumentList(feiledeDokumenterList, ArsakKode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			valid = false;
		}
		return valid;
	}

}
