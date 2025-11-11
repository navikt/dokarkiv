package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.EksternReferanseIdFinnesException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.util.splittjournalpost.JournalpostSplitter;
import no.nav.dokarkiv.journalpost.v1.util.splittjournalpost.JournalpostSplitter.SplittResultat;
import no.nav.dokarkiv.journalpost.v1.validators.SplittJournalpostValidator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT_FRA_SPLITT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SPLITT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;

@Service
@Slf4j
public class SplittJournalpostService {

	private final JournalpostRepository journalpostRepository;
	private final AksjonsLoggService aksjonsLoggService;
	private final DokumentFilerDelegate dokumentFilerDelegate;

	public SplittJournalpostService(JournalpostRepository journalpostRepository,
									AksjonsLoggService aksjonsLoggService,
									DokumentFilerDelegate dokumentFilerDelegate) {
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.dokumentFilerDelegate = dokumentFilerDelegate;
	}

	public SplittJournalpostResponse splittJournalpost(long journalpostId, SplittJournalpostRequest request) {
		Journalpost journalpost = hentOgValiderJournalpost(journalpostId, request);

		SplittJournalpostValidator.valider(request, journalpost);

		long nyJournalpostId = splittJournalpost(journalpost, request);

		settJournalstatusUtgaatt(journalpost);

		return new SplittJournalpostResponse(nyJournalpostId);
	}

	private Journalpost hentOgValiderJournalpost(long journalpostId, SplittJournalpostRequest request) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=%s i joark".formatted(journalpostId)));

		validerJournalpostType(journalpost);
		validerEksternReferanseId(request.eksternReferanseId());

		return journalpost;
	}

	private void validerJournalpostType(Journalpost journalpost) {
		if (journalpost.getJournalposttype() != I) {
			throw new InputValideringFeiletException(
					format("Journalposten må være av type=%s, men er av type=%s", I.name(), journalpost.getJournalposttype().name()));
		}
	}

	private void validerEksternReferanseId(String eksternReferanseId) {
		if (journalpostRepository.existsByKanalReferanseId(eksternReferanseId)) {
			throw new EksternReferanseIdFinnesException(
					format("eksternReferanseId=%s finnes allerede i joark", eksternReferanseId));
		}
	}

	private void settJournalstatusUtgaatt(Journalpost journalpost) {
		journalpost.setJournalstatus(U);
		journalpostRepository.persist(journalpost);
	}

	private Long splittJournalpost(Journalpost originalJournalpost, SplittJournalpostRequest request) {
		SplittResultat splittResultat = JournalpostSplitter.splitt(originalJournalpost, request);
		Journalpost nyJournalpost = splittResultat.nyJournalpost();

		dokumentFilerDelegate.saveNewDokumentFiler(nyJournalpost);
		journalpostRepository.persist(nyJournalpost);

		skrivTilAksjonslogg(originalJournalpost, nyJournalpost, splittResultat.aksjoner());

		return nyJournalpost.getJournalpostId();
	}

	private void skrivTilAksjonslogg(Journalpost originalJournalpost,
									 Journalpost nyJournalpost,
									 List<AksjonsLoggTO> dokumentAksjoner) {

		AksjonsLoggTO splitt = AksjonsLoggTO.builder()
				.aksjon(SPLITT)
				.journalpostId(originalJournalpost.getJournalpostId())
				.melding("Journalposten ble splittet til %s".formatted(nyJournalpost.getJournalpostId()))
				.build();

		AksjonsLoggTO utgaar = AksjonsLoggTO.builder()
				.aksjon(UTGAAR)
				.journalpostId(originalJournalpost.getJournalpostId())
				.melding("Journalposten ble satt til %s etter splitt".formatted(UTGAAR.name()))
				.build();

		AksjonsLoggTO opprettFraSplitt = AksjonsLoggTO.builder()
				.aksjon(OPPRETT_FRA_SPLITT)
				.journalpostId(nyJournalpost.getJournalpostId())
				.melding("Journalposten ble splittet fra journalpostId=%s".formatted(originalJournalpost.getJournalpostId()))
				.build();

		Map<AksjonsLoggTO, List<ArkivElementEndringTO>> aksjonsMap = new HashMap<>();
		aksjonsMap.put(splitt, List.of());
		aksjonsMap.put(utgaar, List.of());
		aksjonsMap.put(opprettFraSplitt, arkivelementEndringer(originalJournalpost, nyJournalpost));
		dokumentAksjoner.forEach(aksjon -> aksjonsMap.put(aksjon, List.of()));

		aksjonsMap.forEach(aksjonsLoggService::validateAndSaveAksjonsLogg);
	}

	private List<ArkivElementEndringTO> arkivelementEndringer(Journalpost originalJournalpost, Journalpost nyJournalpost) {
		return Stream.of(arkivElementEndringTO("journalpost.fagomrade", originalJournalpost.getFagomrade().name(), nyJournalpost.getFagomrade().name()),
						arkivElementEndringTO("journalpost.innhold", originalJournalpost.getInnhold(), nyJournalpost.getInnhold()),
						arkivElementEndringTO("journalpost.avsend_mottaker", originalJournalpost.getAvsenderMottaker(), nyJournalpost.getAvsenderMottaker()),
						arkivElementEndringTO("journalpost.avsend_mottaker_id", originalJournalpost.getAvsenderMottakerId(), nyJournalpost.getAvsenderMottakerId()),
						arkivElementEndringTO("journalpost.journalf_enhet", originalJournalpost.getJournalForendeEnhetId(), nyJournalpost.getJournalForendeEnhetId()))
				.filter(Objects::nonNull)
				.toList();
	}

	private ArkivElementEndringTO arkivElementEndringTO(String arkivElement, String fraVerdi, String tilVerdi) {
		if (fraVerdi == null && tilVerdi == null) {
			return null;
		}

		return ArkivElementEndringTO.builder()
				.arkivElement(arkivElement)
				.fraVerdi(fraVerdi)
				.tilVerdi(tilVerdi)
				.build();
	}
}
