package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;

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
import no.nav.dokarkiv.journalpost.v1.api.ArsakFeilCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiletDokument;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.ShallowDokumentInfoCopier;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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
	private String tilKnyttetAvNavn;
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";
	private static final String OPPRETTET_KILDE_NAVN = "dokarkiv";

	@Inject
	public TilknyttVedleggService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.shallowDokumentInfoCopier = new ShallowDokumentInfoCopier();
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.tilknyttVedleggValidator = new TilknyttVedleggValidator();
	}

	public List<FeiletDokument> tilknyttVedlegg(Long targetJournalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		List<FeiletDokument> feiletDokumentList = new ArrayList<>();
		tilKnyttetAvNavn = tilknyttVedleggRequest.getTilknyttetAvNavn();

		Journalpost journalpost = joarkRepository.findById(targetJournalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", targetJournalpostId)));
		tilknyttVedleggValidator.validateJournalpostStatus(journalpost);

		for (DokumentVedlegg dokumentVedlegg : tilknyttVedleggRequest.getDokument()) {
			Journalpost sourceJournalpost = joarkRepository.findById(dokumentVedlegg.getKildeJournalpostId()).orElse(null);
			DokumentInfo sourceDokumentInfo = dokumentinfoRepository.findByDokumentInfoId(Long.parseLong(dokumentVedlegg.getDokumentInfoId()))
					.orElse(null);
			FilDetaljer filDetaljerSladdet = finnSladdetFildetaljer(sourceDokumentInfo);
			FilDetaljer filDetaljerArkiv = finnArkivFildetaljer(sourceDokumentInfo);

			if (!validateSourceJournalpost(sourceJournalpost, feiletDokumentList, dokumentVedlegg)) {
				break;
			}

			if (!validateSourceDokumentInfo(sourceDokumentInfo, targetJournalpostId, feiletDokumentList, dokumentVedlegg)) {
				break;
			}

			if (filDetaljerSladdet != null) {
				log.info(MDC.get(MDC_REQUEST_ID) + " dokumentId={} har fildetaljer med variant=SLADDET. Det vil bli lagt til en kopi av dokumentinfo på journalpostId={} med variant=ARKIV", dokumentVedlegg
						.getDokumentInfoId(), targetJournalpostId);
				DokumentInfo dokumentInfoCopy = createDokumentInfoCopy(sourceDokumentInfo);

				FilDetaljer fildetaljerCopy = createFildetaljerCopy(filDetaljerSladdet, dokumentInfoCopy);

				DokumentFil dokumentFilCopy = fildetaljerCopy.createDokumentFil();
				dokumentFilCopy.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
				dokumentInfoCopy.addFilDetaljer(fildetaljerCopy);

				dokumentFilRepository.save(dokumentFilCopy);
				dokumentinfoRepository.save(dokumentInfoCopy);

				tilknyttDokumentInfoSomVedleggPaaJournalpost(dokumentInfoCopy, dokumentVedlegg, journalpost, feiletDokumentList);

			} else if (filDetaljerArkiv != null) {
				tilknyttDokumentInfoSomVedleggPaaJournalpost(sourceDokumentInfo, dokumentVedlegg, journalpost, feiletDokumentList);
			} else {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			}

		}
		return feiletDokumentList;
	}


	private DokumentInfo createDokumentInfoCopy(DokumentInfo dokumentInfo) {
		DokumentInfo dokumentInfoCopy = shallowDokumentInfoCopier.copy(dokumentInfo);
		dokumentInfoCopy.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfoCopy.setEndretAvNavn(null);
		dokumentInfoCopy.setOriginalJournalpost(null);
		dokumentInfoCopy.getTilleggsopplysninger().put(TILLEGGOPPLYSNINGER_KEY, dokumentInfo.getDokumentInfoId().toString());

		return dokumentInfoCopy;
	}

	private FilDetaljer createFildetaljerCopy(FilDetaljer filDetaljer, DokumentInfo dokumentInfo) {
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


		filDetaljerCopy.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		byte[] fil = dokumentFilRepository.findByFilUuid(filDetaljer.getFilUuid()).getFil();
		filDetaljerCopy.setFileContent(fil);

		return filDetaljerCopy;
	}

	private List<FeiletDokument> addToFeiletDokumentList(List<FeiletDokument> feiletDokumentList, ArsakFeilCode arsakFeilCode, DokumentVedlegg dokumentVedlegg) {
		feiletDokumentList.add(FeiletDokument.builder()
				.kildeJournalpostId(dokumentVedlegg.getKildeJournalpostId())
				.dokumentInfoId(dokumentVedlegg.getDokumentInfoId())
				.arsakKode(arsakFeilCode)
				.build());
		return feiletDokumentList;
	}

	private void tilknyttDokumentInfoSomVedleggPaaJournalpost(DokumentInfo dokumentInfo, DokumentVedlegg dokumentVedlegg, Journalpost journalpost, List<FeiletDokument> feiletDokumentList) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon;
		try {
			journalpostDokumentInfoRelasjon = createJournalpostDokumentInfoRelasjon(dokumentInfo, journalpost);
			journalpost.addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
			joarkRepository.save(journalpost);
			log.info("Journalpost med journalpostId={} har fått tilknyttet dokument vedlegg fra DokumentInfoId={} ", journalpost
					.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		} catch (Exception e) {
			addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.TILKNYTNING_FEILET, dokumentVedlegg);
		}
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(DokumentInfo dokumentInfo, Journalpost journalpost) {
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
		if (checkDuplicateDokumentInfoRelasjon(targetJournalpostId, dokumentInfo)) {
			return true;
		} else if (checkDuplicateDokumentInfoCopy(targetJournalpostId, dokumentInfo)) {
			return true;
		}
		return false;
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

	private Boolean validateSourceJournalpost(Journalpost sourceJournalpost, List<FeiletDokument> feiletDokumentList, DokumentVedlegg dokumentVedlegg) {
		if (sourceJournalpost == null) {
			addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);
			return false;
		} else if (!tilknyttVedleggValidator.validateSourceJournalpostStatus(sourceJournalpost)) {
			addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.UGYLDIG_STATUS, dokumentVedlegg);
			return false;
		}
		return true;
	}

	private Boolean validateSourceDokumentInfo(DokumentInfo sourceDokumentInfo, Long targetJournalpostId, List<FeiletDokument> feiletDokumentList, DokumentVedlegg dokumentVedlegg) {
		if (sourceDokumentInfo == null) {
			addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);
			return false;
		} else if (checkDuplicate(targetJournalpostId, sourceDokumentInfo)) {
			log.info(MDC.get(MDC_REQUEST_ID) + " dokumentId={} er allerede tilknyttet journalpostId={}", dokumentVedlegg.getDokumentInfoId(), targetJournalpostId);
			return false;
		} else if (!tilknyttVedleggValidator.validateDokumentInfo(sourceDokumentInfo)) {
			addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
			return false;
		}
		return true;
	}
}
