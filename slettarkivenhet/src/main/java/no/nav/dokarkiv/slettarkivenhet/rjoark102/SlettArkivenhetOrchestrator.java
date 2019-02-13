package no.nav.dokarkiv.slettarkivenhet.rjoark102;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.slettarkivenhet.exception.ArkivVariantkkeFunnetException;
import no.nav.dokarkiv.slettarkivenhet.exception.DokumentFilIkkeFunnetException;
import no.nav.dokarkiv.slettarkivenhet.exception.DokumentInfoKanIkkeSlettesException;
import no.nav.dokarkiv.slettarkivenhet.exception.JournalpostKanIkkeSlettesException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class SlettArkivenhetOrchestrator {

	private final JoarkRepository joarkRepository;
	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final SlettArkivenhetService slettArkivenhetService;

	public SlettArkivenhetOrchestrator(JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, SlettArkivenhetService slettArkivenhetService) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.slettArkivenhetService = slettArkivenhetService;
	}

	public List<ArkivElementEndringTO> slettJournalpost(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Fant ingen journalpost med journalpostId=%s i databasen", journalpostId)));

		sjekkOmJournalpostErSplittet(journalpost);
		sjekkOmHoveddokumentHarFlereRelasjoner(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(slettArkivenhetService.slettVedleggKnyttetTilJournalpost(journalpostId));
		arkivElementEndringTOList.addAll(slettArkivenhetService.slettHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()));
		arkivElementEndringTOList.addAll(slettArkivenhetService.slettJournalpost(journalpost.getJournalpostId()));

		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> slettVedlegg(Long journalpostId, Long dokumentInfoId) {

		JournalpostDokumentInfoRelasjon relasjon = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpostId, dokumentInfoId)
				.orElseThrow(() ->
						new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format("Fant ingen JournalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", journalpostId, dokumentInfoId)));

		if (isFalse(relasjon.isVedlegg())) {
			throw new DokumentInfoKanIkkeSlettesException(String.format("DokumentInfo kan ikke slettes fordi dokumentInfo=%s er hoveddokument i journalpost=%s", relasjon
					.getDokumentInfo()
					.getDokumentInfoId(), relasjon.getJournalpost().getJournalpostId()));
		}

		return slettArkivenhetService.slettJournalpostDokumentInfoRelasjonVedlegg(relasjon);

	}

	public List<ArkivElementEndringTO> slettDokumentFil(Long dokumentInfoId, VariantFormatCode variant) {

		//Sjekk om dokumentInfo eksisterer
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Fant ikke dokument med dokumentInfoId=%s i Joark databasen", dokumentInfoId)));

		//Sjekk om fildetaljer eksisterer
		FilDetaljer filDetaljerSomSkalSlettes = dokumentInfo.findFilDetaljerByVariantFormat(variant);
		if (Objects.isNull(filDetaljerSomSkalSlettes)) {
			throw new ArkivVariantkkeFunnetException(String.format("Dokument med dokumentInfoId=%s har ingen fildetaljer med variantFormat=%s", dokumentInfoId, variant));
		}

		//Sjekk om dokumentFil eksisterer
		DokumentFil dokumentFilSomSkalSlettes = dokumentFilRepository.findByFilUuid(filDetaljerSomSkalSlettes.getFilUuid());
		if (Objects.isNull(dokumentFilSomSkalSlettes)) {
			throw new DokumentFilIkkeFunnetException(String.format("Fildetaljer med variantFormat=%s og dokumentInfoId=%s mangler dokumentFil", variant, dokumentInfoId));
		}

		return slettArkivenhetService.slettFilOgFildetaljer(dokumentInfoId, variant);
	}


	private void sjekkOmHoveddokumentHarFlereRelasjoner(DokumentInfo dokumentInfoHoveddokument) {
		if (dokumentInfoHoveddokument.getJournalpostRelasjoner().size() > 1) {
			throw new JournalpostKanIkkeSlettesException(String.format("Hoveddokument=%s er tilknyttet som vedlegg til andre journalposter. Alle tilknyttinger som vedlegg til hoveddokument må slettes før journalpost kan slettes",
					dokumentInfoHoveddokument.getDokumentInfoId()));
		}
	}

	private void sjekkOmJournalpostErSplittet(Journalpost journalpost) {

		List<DokumentInfo> dokumenterMedJournalpostSattSomOriginalJournalpost =
				dokumentinfoRepository.findByOriginalJournalpostJournalpostId(journalpost.getJournalpostId());
		if (dokumenterMedJournalpostSattSomOriginalJournalpost.size() > journalpost.getJournalpostDokumentInfoRelasjoner()
				.size()) {
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost=%s er splittet og kan ikke slettes før de splittete dokumentene er slettet",
					journalpost.getJournalpostId()));
		}

		//Hindrer sletting av splittet dokumenter. Vil støtte sletting på sikt.
		Journalpost hoveddokOrigJp = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getOriginalJournalpost();

		if (Objects.nonNull(hoveddokOrigJp) &&
				isFalse(journalpost.getJournalpostId().equals(hoveddokOrigJp.getJournalpostId()))) {
			throw new JournalpostKanIkkeSlettesException(String.format("Journalpost kan ikke slettes fordi " +
							"hoveddokument med dokumentInfoId=%s har originalJournalpostId=%s som er ulik journalposten som skal slettes",
					journalpost.findHoveddokumentDokumentInfoRelasjon()
							.getDokumentInfo()
							.getDokumentInfoId(), hoveddokOrigJp.getJournalpostId()));
		}
	}
}
