package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.journalpost.v1.api.ArsakFeilCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiletDokument;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.validators.TilknyttVedleggValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Service(value = "tilknyttVedleggService")
@Slf4j
public class TilknyttVedleggService {

	private final JoarkRepositorySkjermet joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final TilknyttVedleggValidator tilknyttVedleggValidator;

	@Inject
	public TilknyttVedleggService(JoarkRepositorySkjermet joarkRepository, DokumentinfoRepository dokumentinfoRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
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
			FilDetaljer filDetaljerSladdet = tilknyttVedleggValidator.finnSladdetFildetaljer(dokumentInfo);
			FilDetaljer filDetaljerArkiv = tilknyttVedleggValidator.finnArkivFildetaljer(dokumentInfo);
			Set<FilDetaljer> filDetaljerListCopy = new HashSet<>();


			if (journalpostOrigin == null) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);

			} else if (!tilknyttVedleggValidator.validateOriginJournalpostStatus(journalpostOrigin)) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.UGYLDIG_STATUS, dokumentVedlegg);

			} else if (dokumentInfo == null) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.IKKE_FUNNET, dokumentVedlegg);

			} else if (joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfo.getDokumentInfoId())
					.contains(journalpostId)) {
				log.info(MDC.get(MDC_REQUEST_ID) + " dokumentId={} er allerede tilknyttet journalpostId={}", dokumentVedlegg.getDokumentInfoId(), journalpostOrigin);

			} else if (!tilknyttVedleggValidator.validateDokumentInfo(dokumentInfo)) {
				addToFeiletDokumentList(feiletDokumentList, ArsakFeilCode.DOKUMENT_TILLATES_IKKE_GJENBRUKT, dokumentVedlegg);

			} else {
				if (filDetaljerSladdet != null) {
					DokumentInfo dokumentInfoCopy = createDokumentInfoCopy(dokumentInfo);
					filDetaljerListCopy.add(createFildetaljerCopy(filDetaljerSladdet, dokumentInfoCopy));
					dokumentInfoCopy.setOpprettetKildeNavn("Test");
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

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(DokumentInfo dokumentInfo, Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.tilknyttetAvNavn("Testus testesen")
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn("test");
		return journalpostDokumentInfoRelasjon;
	}

	private FilDetaljer createFildetaljerCopy(FilDetaljer filDetaljer, DokumentInfo dokumentInfo) {
		return FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(filDetaljer.getFileContent())
				.filUuid(filDetaljer.getFilUuid())
				.onDemandId(filDetaljer.getOnDemandId())
				.onDemandInstans(filDetaljer.getOnDemandInstans())
				.metaforceInstanceId(filDetaljer.getMetaforceInstanceId())
				.filtype(filDetaljer.getFiltype())
				.variantFormat(VariantFormatCode.ARKIV)
				.batchNavn(filDetaljer.getBatchNavn())
				.filnavn(filDetaljer.getFilnavn())
				.filstorrelse(filDetaljer.getFilstorrelse())
				.build();
	}

	private DokumentInfo createDokumentInfoCopy(DokumentInfo dokumentInfo) {
		return DokumentInfo.builder()
				.brevkode(dokumentInfo.getBrevkode())
				.tittel(dokumentInfo.getTittel())
				.kategori(dokumentInfo.getKategori())
				.tilleggsopplysninger(dokumentInfo.getTilleggsopplysninger())
				.innskrenketPartsinnsyn(dokumentInfo.getInnskrenketPartsinnsyn())
				.sensitivt(dokumentInfo.getSensitivt())
				.innskrenketPartsinnsyn(dokumentInfo.getInnskrenketPartsinnsyn())
				.brevkode(dokumentInfo.getBrevkode())
				.dokumenttypeId(dokumentInfo.getDokumenttypeId())
				.dokumentstatus(dokumentInfo.getDokumentstatus())
				.organInternt(dokumentInfo.getOrganInternt())
				.originalJournalpost(dokumentInfo.getOriginalJournalpost())
				.dokumentFerdigDato(dokumentInfo.getDokumentFerdigDato())
				.brevgruppe(dokumentInfo.getBrevgruppe())
				.datoKassert(dokumentInfo.getDatoKassert())
				.endretAvNavn("tilKnyttVedlegg")
				.kassert(dokumentInfo.isKassert())
				.kassertAvNavn(dokumentInfo.getKassertAvNavn())
				.konvertertFraSystem(dokumentInfo.getKonvertertFraSystem())
				.build();
	}

	private List<FeiletDokument> addToFeiletDokumentList(List<FeiletDokument> feiletDokumentList, ArsakFeilCode arsakFeilCode, DokumentVedlegg dokumentVedlegg) {
		feiletDokumentList.add(FeiletDokument.builder()
				.kildeJournalpostId(dokumentVedlegg.getKildeJournalpostId())
				.dokumentInfoId(dokumentVedlegg.getDokumentInfoId())
				.arsakKode(arsakFeilCode)
				.build());
		return feiletDokumentList;
	}

	private void saveDokumentInfoRelasjon(DokumentInfo dokumentInfo, DokumentVedlegg dokumentVedlegg, Journalpost journalpost, List<FeiletDokument> feiletDokumentList){
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
}
