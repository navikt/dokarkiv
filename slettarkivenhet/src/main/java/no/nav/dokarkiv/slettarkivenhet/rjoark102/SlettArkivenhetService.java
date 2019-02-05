package no.nav.dokarkiv.slettarkivenhet.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;

@Slf4j
@Service
@SuppressWarnings("Duplicates")
public class SlettArkivenhetService {

	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final SkjermingService skjermingService;
	private final JoarkRepository joarkRepository;

	@Inject
	public SlettArkivenhetService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, SkjermingService skjermingService, JoarkRepository joarkRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.skjermingService = skjermingService;
		this.joarkRepository = joarkRepository;
	}

	public void slettJournalpost(SlettArkivenhetRequest request) {
		Journalpost journalpost = joarkRepository.findById(request.getJournalpostId())
				.orElseThrow(() -> new IllegalArgumentException("Fant ikke journalpost"));

		sjekkAtJournalpostErUtilgjengeliggjort(request.getJournalpostId());
		fysiskSlettJournalpost(journalpost);

	}

	public void slettDokumentFil(SlettArkivenhetRequest request) {

		//Sjekk om dokumentInfo eksisterer
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(() -> new IllegalArgumentException(String.format("Fant ikke dokument med dokumentInfoId=%s i Joark databasen", request
						.getDokumentInfoId())));

		//Sjekk om fildetaljer eksisterer
		FilDetaljer filDetaljerSomSkalSlettes = dokumentInfo.findFilDetaljerByVariantFormat(request.getVariant());
		if (Objects.isNull(filDetaljerSomSkalSlettes)) {
			throw new IllegalArgumentException(String.format("Dokument med dokumentInfoId=%s har ingen fildetalj med variantFormat=%s", request
					.getDokumentInfoId(), request.getVariant()));
		}

		//Sjekk om dokumentFil eksisterer
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filDetaljerSomSkalSlettes.getFilUuid());
		if (Objects.isNull(dokumentFil)) {
			throw new IllegalArgumentException(String.format("Fildetalj med variantFormat=%s og dokumentInfoId=%s mangler dokumentFil", request
					.getVariant(), request.getDokumentInfoId()));
		}

		slettFilOgFildetaljer(request.getDokumentInfoId());
	}

	public void slettDokumentInfo(SlettArkivenhetRequest request) {
		JournalpostDokumentInfoRelasjon relasjon = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(request
				.getJournalpostId(), request.getDokumentInfoId()).orElseThrow(()->new IllegalArgumentException("Fant ikke relasjon"));

		if (relasjon.isVedlegg()) {
			fysiskSlettEtVedlegg(relasjon);
		}


	}

	private void sjekkAtJournalpostErUtilgjengeliggjort(Long journalpostId) {
		if (isFalse(skjermingService.isJournalpostSkjermet(
				journalpostId,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
					journalpostId,
					SkjermingTypeCode.POL.name()));
		}
	}

	private void sjekkAtDokumentErUtilgjengeliggjort(Long journalpostId, Long dokumentInfoId) {
		if (isFalse(skjermingService.isJournalpostDokumentInfoRelasjonSkjermet(
				journalpostId,
				dokumentInfoId,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
					journalpostId,
					dokumentInfoId,
					SkjermingTypeCode.POL.name()));
		}
	}

	private void fysiskSlettJournalpost(Journalpost journalpost) {
		slettVedleggKnyttetTilJournalpost(journalpost);
		slettHoveddokumentKnyttetTiLJournalpost(journalpost.findHoveddokumentDokumentInfoRelasjon());
	}

	private void slettHoveddokumentKnyttetTiLJournalpost(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			slettJournalpostOgJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			slettJournalpostOgDokumentInfoOgJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		}
	}

	private void slettVedleggKnyttetTilJournalpost(Journalpost journalpost) {

		Long jpIdTilJpSomSkalSlettes = journalpost.getJournalpostId();

		for (JournalpostDokumentInfoRelasjon relasjon : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (relasjon.isVedlegg()) {
				Long originalJournalpostId = relasjon.getDokumentInfo().getOriginalJournalpost() ==
						null ? -1 : relasjon.getDokumentInfo().getOriginalJournalpost().getJournalpostId();
				if (relasjon.getDokumentInfo().isRelatedToMultipleJournalposts() &&
						originalJournalpostId.equals(jpIdTilJpSomSkalSlettes)) {
					endreOriginalJournalpostIDokumentInfo(relasjon.getDokumentInfo(), jpIdTilJpSomSkalSlettes);
				}
				fysiskSlettEtVedlegg(relasjon);
			}
		}
	}

	private void fysiskSlettEtVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			slettFilOgDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		}
	}

	private void slettJournalpostOgJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		endreOriginalJournalpostIDokumentInfo(relasjon.getDokumentInfo(), relasjon.getJournalpost().getJournalpostId());
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());
		slettJournalpost(relasjon.getJournalpost().getJournalpostId());
	}

	private void endreOriginalJournalpostIDokumentInfo(DokumentInfo dokInfoMedJpSomSkalSlettes, Long jpIdTilJpSomSkalSlettes) {
		Journalpost nyOriginalJournalpost = null;
		for (JournalpostDokumentInfoRelasjon relasjon : dokInfoMedJpSomSkalSlettes.getJournalpostRelasjoner()) {
			if (nyOriginalJournalpost == null &&
					isFalse(relasjon.getJournalpost().getJournalpostId().equals(jpIdTilJpSomSkalSlettes))) {
				nyOriginalJournalpost = relasjon.getJournalpost();
			}
		}
		dokInfoMedJpSomSkalSlettes.setOriginalJournalpost(nyOriginalJournalpost);
	}

	private void slettJournalpostOgDokumentInfoOgJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		slettFilOgDokumentInfo(relasjon.getDokumentInfo().getDokumentInfoId());
		slettJournalpost(relasjon.getJournalpost().getJournalpostId());
	}

	private void slettJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());
	}

	private void slettJournalpost(Long journalpostId) {
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukerByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);
	}

	private void slettFilOgDokumentInfo(Long dokumentInfoId) {
		slettFilOgFildetaljer(dokumentInfoId);
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoJPRelByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);
	}

	private void slettFilOgFildetaljer(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
