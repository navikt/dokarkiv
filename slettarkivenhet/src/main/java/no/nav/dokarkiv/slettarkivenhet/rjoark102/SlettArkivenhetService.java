package no.nav.dokarkiv.slettarkivenhet.rjoark102;

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettarkivenhet.exception.ArkivVariantkkeFunnetException;
import no.nav.dokarkiv.slettarkivenhet.exception.DokumentInfoKanIkkeSlettesException;
import no.nav.dokarkiv.slettarkivenhet.exception.JournalpostKanIkkeSlettesException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
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
	private final JoarkRepository joarkRepository;

	@Inject
	public SlettArkivenhetService(
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository,
			DokumentinfoRepository dokumentinfoRepository,
			DokumentFilRepository dokumentFilRepository,
			JoarkRepository joarkRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.joarkRepository = joarkRepository;
	}

	public List<ArkivElementEndringTO> slettJournalpost(SlettArkivenhetRequest request) {
		Journalpost journalpost = joarkRepository.findById(request.getJournalpostId())
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ingen journalpost med journalpostId=%s i databasen", request
						.getJournalpostId())));

		sjekkOmJournalpostKanSlettes(journalpost);

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(slettVedleggKnyttetTilJournalpost(journalpost));
		arkivElementEndringTOList.addAll(slettJournalpostRelasjonDokumentInfoOgFil(journalpost.findHoveddokumentDokumentInfoRelasjon()));
		arkivElementEndringTOList.addAll(fysiskSlettJournalpost(journalpost.getJournalpostId()));

		return arkivElementEndringTOList;
	}

	//TODO: Rename til slettJournalpostDokumentInfo, og få inn verdiet i arkivenhet? JournalpostDokumentInfoRelasjonKanIkkeSlettesException?
	public List<ArkivElementEndringTO> slettDokumentInfo(SlettArkivenhetRequest request) {

		JournalpostDokumentInfoRelasjon relasjon = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(request
				.getJournalpostId(), request.getDokumentInfoId())
				.orElseThrow(() -> new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format("Fant ingen JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", request
						.getJournalpostId(), request.getDokumentInfoId())));

		if (isFalse(relasjon.isVedlegg())) {
			throw new DokumentInfoKanIkkeSlettesException(String.format("DokumentInfo=%s er hoveddokument i journalpost=%s og kan derfor ikke slettes", relasjon
					.getDokumentInfo()
					.getDokumentInfoId(), relasjon.getJournalpost().getJournalpostId()));
		}

		return fysiskSlettEtVedlegg(relasjon);

	}

	public List<ArkivElementEndringTO> slettDokumentFil(SlettArkivenhetRequest request) {

		//Sjekk om dokumentInfo eksisterer
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(() -> new ArkivVariantkkeFunnetException(String.format("Fant ikke dokument med dokumentInfoId=%s i Joark databasen", request
						.getDokumentInfoId())));

		//Sjekk om fildetaljer eksisterer
		FilDetaljer filDetaljerSomSkalSlettes = dokumentInfo.findFilDetaljerByVariantFormat(request.getVariant());
		if (Objects.isNull(filDetaljerSomSkalSlettes)) {
			throw new IllegalArgumentException(String.format("Dokument med dokumentInfoId=%s har ingen fildetalj med variantFormat=%s", request
					.getDokumentInfoId(), request.getVariant()));
		}

		//Sjekk om dokumentFil eksisterer
		DokumentFil dokumentFilSomSkalSlettes = dokumentFilRepository.findByFilUuid(filDetaljerSomSkalSlettes.getFilUuid());
		if (Objects.isNull(dokumentFilSomSkalSlettes)) {
			throw new IllegalArgumentException(String.format("Fildetalj med variantFormat=%s og dokumentInfoId=%s mangler dokumentFil", request
					.getVariant(), request.getDokumentInfoId()));
		}

		return slettFilOgFildetaljer(request.getDokumentInfoId(), request.getVariant());
	}

	private void sjekkOmJournalpostKanSlettes(Journalpost journalpost) {
//		Kontroll av originalJournalpost, dokumenterMedJournalpostSattSomOriginalJournalpost
		List<DokumentInfo> dokumenterMedJournalpostSattSomOriginalJournalpost =
				dokumentinfoRepository.findByOriginalJournalpostJournalpostId(journalpost.getJournalpostId());
		if (dokumenterMedJournalpostSattSomOriginalJournalpost.size() > journalpost.getJournalpostDokumentInfoRelasjoner()
				.size()) {
			Integer diff = dokumenterMedJournalpostSattSomOriginalJournalpost.size() - journalpost.getJournalpostDokumentInfoRelasjoner()
					.size();
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost=%s kan ikke slettes: " +
							"Det finnes %s dokumentInfo(er) som har originalJournalpostId=%s men som ikke har relasjon med journalposten som skal slettes",
					journalpost.getJournalpostId(), diff, journalpost.getJournalpostId()));
		}

		Journalpost hoveddokOrigJp = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getOriginalJournalpost();

		if (Objects.nonNull(hoveddokOrigJp) &&
				isFalse(journalpost.getJournalpostId().equals(hoveddokOrigJp.getJournalpostId()))) {
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost kan ikke slettes: " +
							"Hoveddokument med dokumentInfoId=%s har originalJournalpostId=%s som er ulik journalpostIden til journalposten som skal slettes",
					journalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(), hoveddokOrigJp.getJournalpostId()));
		}
	}

	//HåndterSlettAvArkivenhet ------------------------------
	private List<ArkivElementEndringTO> slettVedleggKnyttetTilJournalpost(Journalpost journalpost) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)
				.forEach(relasjon -> arkivElementEndringTOList.addAll(fysiskSlettEtVedlegg(relasjon)));

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> fysiskSlettEtVedlegg(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		if (relasjonSomSkalSlettes.getDokumentInfo().isRelatedToMultipleJournalposts()) {
			return slettJournalpostDokumentInfoRelasjon(relasjonSomSkalSlettes);
		} else {
			return slettJournalpostRelasjonDokumentInfoOgFil(relasjonSomSkalSlettes);
		}
	}

	private List<ArkivElementEndringTO> slettJournalpostRelasjonDokumentInfoOgFil(JournalpostDokumentInfoRelasjon relasjonSomSkalSlettes) {
		slettJournalpostDokumentInfoRelasjonGittDokumentInfoId(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		slettFilOgFildetaljer(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
		return slettDokumentInfo(relasjonSomSkalSlettes.getDokumentInfo().getDokumentInfoId());
	}

	//FysiskSlettAvArkivenhet -------------------------------------------
	private List<ArkivElementEndringTO> slettJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjon) {
		deleteRepository.deleteJournalpostDokumentInfoRelasjonByJournalpostIdAndDokumentInfoId(
				relasjon.getJournalpost().getJournalpostId(),
				relasjon.getDokumentInfo().getDokumentInfoId());

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("JournalpostDokumentInfoRelasjon.tilknyttetJounalpostSom")
						.fraVerdi(VEDLEGG.name())
						.tilVerdi(null)
						.build()

		);
	}

	private List<ArkivElementEndringTO> fysiskSlettJournalpost(Long journalpostId) {
		deleteRepository.deleteJPTilleggByJournalpostId(journalpostId);
		deleteRepository.deleteSaksrelasjonByJournalpostId(journalpostId);
		deleteRepository.deleteBrukerByJournalpostId(journalpostId);
		deleteRepository.deleteJournalpostByJournalpostId(journalpostId);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalpostId")
						.fraVerdi(journalpostId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	private void slettJournalpostDokumentInfoRelasjonGittDokumentInfoId(Long dokumentInfoId) {
		deleteRepository.deleteDokInfoJPRelByDokumentInfoId(dokumentInfoId);
	}

	private List<ArkivElementEndringTO> slettDokumentInfo(Long dokumentInfoId) {
		deleteRepository.deleteSkannetInnholdByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoTilleggByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteDokInfoByDokumentInfoId(dokumentInfoId);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(dokumentInfoId.toString())
						.tilVerdi(null)
						.build()

		);
	}

	private List<ArkivElementEndringTO> slettFilOgFildetaljer(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode);
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode);

		return Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi(variantFormatCode.name())
						.tilVerdi(null)
						.build()

		);
	}

	private void slettFilOgFildetaljer(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
