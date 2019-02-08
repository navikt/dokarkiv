package no.nav.dokarkiv.slettarkivenhet.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettarkivenhet.exception.DokumentInfoKanIkkeSlettesException;
import no.nav.dokarkiv.slettarkivenhet.exception.JournalpostKanIkkeSlettesException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
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
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ingen journalpost med journalpostId=%s i databasen", request
						.getJournalpostId())));

		sjekkAtJournalpostErSkjermet(request.getJournalpostId());
		sjekkOmJournalpostKanSlettes(journalpost);

		slettVedleggKnyttetTilJournalpost(journalpost);
		slettJournalpostRelasjonFilOgDokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon());
		slettJournalpost(journalpost.getJournalpostId());

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

		sjekkOmFildetaljerErSkjermet(dokumentInfo, request.getVariant());

		slettFilOgFildetaljer(request.getDokumentInfoId(), request.getVariant());
	}

	public void slettDokumentInfo(SlettArkivenhetRequest request) {

		JournalpostDokumentInfoRelasjon relasjon = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(request
				.getJournalpostId(), request.getDokumentInfoId())
				.orElseThrow(() -> new IllegalArgumentException(String.format("Fant ingen JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", request
						.getJournalpostId(), request.getDokumentInfoId())));

		sjekkAtDokumentErUtilgjengeliggjort(request.getJournalpostId(), request.getDokumentInfoId());

		if (isFalse(relasjon.isVedlegg())) {
			throw new DokumentInfoKanIkkeSlettesException(String.format("DokumentInfo=%s er hoveddokument i journalpost=%s og kan derfor ikke slettes", relasjon
					.getDokumentInfo()
					.getDokumentInfoId(), relasjon.getJournalpost().getJournalpostId()));
		}

		fysiskSlettEtVedlegg(relasjon);

	}

	private void sjekkOmJournalpostKanSlettes(Journalpost journalpost) {
		List<DokumentInfo> dokumentInfoList = dokumentinfoRepository.findByOriginalJournalpostJournalpostId(journalpost.getJournalpostId());
		if (dokumentInfoList.size() > journalpost.getJournalpostDokumentInfoRelasjoner().size()) {
			Integer diff = dokumentInfoList.size() - journalpost.getJournalpostDokumentInfoRelasjoner().size();
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost=%s kan ikke slettes: " +
							"Det finnes %s dokumentInfo(er) som har originalJournalpostId=%s men som ikke har relasjon med journalposten som skal slettes",
					journalpost.getJournalpostId(), diff, journalpost.getJournalpostId()));
		}

		Journalpost hoveddokOrigJp = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getOriginalJournalpost();

		if (Objects.nonNull(hoveddokOrigJp) && isFalse(journalpost.getJournalpostId()
				.equals(hoveddokOrigJp.getJournalpostId()))) {
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost kan ikke slettes: " +
							"Hoveddokument med dokumentInfoId=%s har originalJournalpostId=%s som er ulik journalpostIden til journalposten som skal slettes",
					journalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(), hoveddokOrigJp.getJournalpostId()));
		}
	}


	private void sjekkAtJournalpostErSkjermet(Long journalpostId) {
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

	private void sjekkOmFildetaljerErSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode) {
		if (isFalse(skjermingService.isFildetaljerSkjermet(dokumentInfo, variantFormatCode,
				SkjermingTypeCode.POL))) {
			throw new SkjermingIkkeFunnetException(String.format(
					"Fant ikke forventet skjerming for dokument med dokumentInfoId=%s og begrensningsType=%s.",
					dokumentInfo.getDokumentInfoId(),
					SkjermingTypeCode.POL.name()));
		}
	}

	private void slettJournalpostRelasjonFilOgDokumentInfo(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		slettJournalpostDokumentInfoRelasjonGittDokumentInfoId(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		slettFilOgFildetaljer(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		slettDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
	}

	private void slettVedleggKnyttetTilJournalpost(Journalpost journalpost) {

		journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.forEach(this::fysiskSlettEtVedlegg);
	}

	private void fysiskSlettEtVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			slettJournalpostRelasjonFilOgDokumentInfo(relasjonSomSkalSlettes);
		}
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

	private void slettJournalpostDokumentInfoRelasjonGittDokumentInfoId(Long dokumentInfoId) {
		deleteRepository.deleteDokInfoJPRelByDokumentInfoId(dokumentInfoId);
	}

	private void slettDokumentInfo(Long dokumentInfoId) {
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);
	}

	private void slettFilOgFildetaljer(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode);
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode);
	}

	private void slettFilOgFildetaljer(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
