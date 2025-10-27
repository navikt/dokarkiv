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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				.orElseThrow(() -> new JournalpostIkkeFunnetException(
						format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

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

		skrivTilAksjonslogg(originalJournalpost.getJournalpostId(), nyJournalpost, splittResultat.aksjoner());

		return nyJournalpost.getJournalpostId();
	}

	private void skrivTilAksjonslogg(Long originalJournalpostId,
									 Journalpost nyJournalpost,
									 List<AksjonsLoggTO> dokumentAksjoner) {

		AksjonsLoggTO splitt = AksjonsLoggTO.builder()
				.aksjon(SPLITT)
				.journalpostId(originalJournalpostId)
				.melding("Journalposten ble splittet til %s".formatted(nyJournalpost.getJournalpostId()))
				.build();

		AksjonsLoggTO utgaar = AksjonsLoggTO.builder()
				.aksjon(UTGAAR)
				.journalpostId(originalJournalpostId)
				.melding("Journalposten ble satt til %s etter splitt".formatted(UTGAAR.name()))
				.build();

		List<ArkivElementEndringTO> arkivElementEndringer = new ArrayList<>(List.of(
				ArkivElementEndringTO.arkivElementEndringNew("journalpost.fagomrade", nyJournalpost.getFagomrade().name()),
				ArkivElementEndringTO.arkivElementEndringNew("journalpost.innhold", nyJournalpost.getInnhold()),
				ArkivElementEndringTO.arkivElementEndringNew("journalpost.avsend_mottaker", nyJournalpost.getAvsenderMottaker()),
				ArkivElementEndringTO.arkivElementEndringNew("journalpost.avsend_mottaker_id", nyJournalpost.getAvsenderMottakerId()),
				ArkivElementEndringTO.arkivElementEndringNew("journalpost.journalf_enhet", nyJournalpost.getJournalForendeEnhetId())));

		nyJournalpost.getBrukere().stream().findFirst().ifPresent(bruker ->
				arkivElementEndringer.add(ArkivElementEndringTO.arkivElementEndringNew("bruker.bruker_id", bruker.getBrukerId())));

		AksjonsLoggTO opprettFraSplitt = AksjonsLoggTO.builder()
				.aksjon(OPPRETT_FRA_SPLITT)
				.journalpostId(nyJournalpost.getJournalpostId())
				.melding("Journalposten ble splittet fra journalpostId=%s".formatted(originalJournalpostId))
				.build();

		Map<AksjonsLoggTO, List<ArkivElementEndringTO>> aksjonsMap = new HashMap<>();
		aksjonsMap.put(splitt, List.of());
		aksjonsMap.put(utgaar, List.of());
		aksjonsMap.put(opprettFraSplitt, arkivElementEndringer);
		dokumentAksjoner.forEach(aksjon -> aksjonsMap.put(aksjon, List.of()));

		aksjonsMap.forEach(aksjonsLoggService::validateAndSaveAksjonsLogg);
	}
}
