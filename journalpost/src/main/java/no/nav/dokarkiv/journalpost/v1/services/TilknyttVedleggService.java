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
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.DokumentInfoCopier;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Service(value = "tilknyttVedleggService")
@Slf4j
public class TilknyttVedleggService {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentInfoCopier dokumentInfoCopier;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final TilknyttVedleggValidator tilknyttVedleggValidator;
	private static final String TILLEGGOPPLYSNINGER_KEY = "Kopi dokumentInfoId";

	@Inject
	public TilknyttVedleggService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.dokumentInfoCopier = new DokumentInfoCopier();
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.tilknyttVedleggValidator = new TilknyttVedleggValidator();
	}

	public List<FeiletDokument> tilknyttVedlegg(Long journalpostId, TilknyttVedleggRequest tilknyttVedleggRequest) {
		List<FeiletDokument> feiletDokumentList = new ArrayList<>();

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		tilknyttVedleggValidator.validateJournalpostStatus(journalpost);

		for (DokumentVedlegg dokumentVedlegg : tilknyttVedleggRequest.getDokument()) {
			Journalpost journalpostOrigin = joarkRepository.findById(dokumentVedlegg.getKildeJournalpostId()).orElse(null);
			DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(Long.parseLong(dokumentVedlegg.getDokumentInfoId()))
					.orElse(null);
			FilDetaljer filDetaljerSladdet = finnSladdetFildetaljer(dokumentInfo);
			FilDetaljer filDetaljerArkiv = finnArkivFildetaljer(dokumentInfo);

			if (journalpostOrigin == null) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);

			} else if (!tilknyttVedleggValidator.validateOriginJournalpostStatus(journalpostOrigin)) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.UGYLDIG_STATUS, dokumentVedlegg);

			} else if (dokumentInfo == null) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);

			} else if (checkDuplicateDokumentInfoRelasjon(journalpostId, dokumentInfo)) {
				log.info(MDC.get(MDC_REQUEST_ID) + " dokumentId={} er allerede tilknyttet journalpostId={}", dokumentVedlegg.getDokumentInfoId(), journalpostOrigin);

			} else if (!tilknyttVedleggValidator.validateDokumentInfo(dokumentInfo)) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);

			} else {
				if (filDetaljerSladdet != null) {
					DokumentInfo dokumentInfoCopy = createDokumentInfoCopy(dokumentInfo);

					FilDetaljer filDetaljer = createFildetaljerCopy(filDetaljerSladdet, dokumentInfoCopy);

					DokumentFil dokumentFil = filDetaljer.createDokumentFil();
					dokumentFil.setOpprettetKildeNavn("Dokarkiv");
					dokumentInfoCopy.addFilDetaljer(filDetaljer);

					dokumentFilRepository.save(dokumentFil);
					dokumentinfoRepository.save(dokumentInfoCopy);

					saveDokumentInfoRelasjon(dokumentInfoCopy, dokumentVedlegg, journalpost, feiletDokumentList);

				} else if (filDetaljerArkiv != null) {
					saveDokumentInfoRelasjon(dokumentInfo, dokumentVedlegg, journalpost, feiletDokumentList);
				} else {
					addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);
				}
			}
		}
		return feiletDokumentList;
	}


	private DokumentInfo createDokumentInfoCopy(DokumentInfo dokumentInfo) {
		DokumentInfo dokumentInfoCopy = dokumentInfoCopier.copy(dokumentInfo);
		dokumentInfoCopy.setOpprettetKildeNavn("Dokarkiv");
		dokumentInfoCopy.setEndretAvNavn(null);
		dokumentInfoCopy.setOriginalJournalpost(null);
		dokumentInfoCopy.setTilleggsopplysninger(createTilleggsopplysninger(dokumentInfo.getDokumentInfoId()
				.toString()));

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


		filDetaljerCopy.setOpprettetKildeNavn("Dokarkiv");
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

	private void saveDokumentInfoRelasjon(DokumentInfo dokumentInfo, DokumentVedlegg dokumentVedlegg, Journalpost journalpost, List<FeiletDokument> feiletDokumentList) {
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
				.tilknyttetAvNavn("TilknyttVedlegg")
				.build();
		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn("TilknyttVedlegg");
		return journalpostDokumentInfoRelasjon;
	}

	private static Map<String, String> createTilleggsopplysninger(String dokumentInfoId) {
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY, dokumentInfoId);
		return tilleggsopplysninger;
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

	private Boolean checkDuplicateDokumentInfoRelasjon(Long journalpostId, DokumentInfo dokumentInfo) {
		List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostId);
		for (JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon : journalpostDokumentInfoRelasjons) {
			if (journalpostDokumentInfoRelasjon.getDokumentInfo()
					.getTilleggsopplysninger()
					.containsValue(dokumentInfo.getDokumentInfoId().toString())) {
				return true;
			}
		}
		if (joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.contains(journalpostId)) {
			return true;
		}
		return false;
	}


}
