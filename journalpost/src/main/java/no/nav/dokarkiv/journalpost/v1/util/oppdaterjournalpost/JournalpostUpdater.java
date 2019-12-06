package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerService;
import no.nav.dokarkiv.core.consumer.aktoer.HentIdentForAktoerIdRequestTo;
import no.nav.dokarkiv.core.consumer.aktoer.PersonIkkeFunnetException;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_BRUKER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_FAGOMRADE;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_INNHOLD;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Component
public class JournalpostUpdater {

	private static final String DELETE_MARKER = " ";
	private final BrukerRepository brukerRepository;
	private final AktoerConsumerService aktoerConsumerService;

	@Inject
	public JournalpostUpdater(BrukerRepository brukerRepository, AktoerConsumerService aktoerConsumerService) {
		this.brukerRepository = brukerRepository;
		this.aktoerConsumerService = aktoerConsumerService;
	}

	public ChangeTracker updateFields(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest) {

		ChangeTracker tracker = new ChangeTracker();
		updateTittel(journalpost, oppdaterJournalpostRequest, tracker);
		updateTema(journalpost, oppdaterJournalpostRequest, tracker);
		updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, tracker);
		updateBehandlingstema(journalpost, oppdaterJournalpostRequest, tracker);
		updateTilleggsopplysninger(journalpost, oppdaterJournalpostRequest, tracker);
		updateJournalfoerendeEnhet(journalpost, oppdaterJournalpostRequest, tracker);
		updateReturInfo(journalpost, oppdaterJournalpostRequest, tracker);
		updateBruker(journalpost, oppdaterJournalpostRequest, tracker);
		updateDatoMottatt(journalpost, oppdaterJournalpostRequest, tracker);

		if (tracker.isEndretFlagg()) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
		return tracker;
	}

	public ChangeTracker updateFields(Journalpost journalpost, OppdaterDistribusjonsinfoRequest request) {
		ChangeTracker tracker = new ChangeTracker();

		if (request.getUtsendingsKanal() != null) {
			journalpost.setUtsendingskanal(UtsendingsKanalCode.valueOf(request.getUtsendingsKanal()));
			tracker.setEndretFlagg(true);
		}
		if (request.getSettStatusEkspedert()) {
			journalpost.setJournalstatus(JournalStatusCode.E);
			journalpost.setEkspedertDato(new Date());
			tracker.setEndretFlagg(true);
			tracker.add(JOURNALPOST_JOURNALSTATUS, journalpost.getJournalstatus().name(), JournalStatusCode.E.name());
		}

		if (tracker.isEndretFlagg()) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
		return tracker;
	}

	private void updateJournalfoerendeEnhet(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getJournalfoerendeEnhet())) {
			journalpost.setJournalForendeEnhetId(oppdaterJournalpostRequest.getJournalfoerendeEnhet());
			endret.setEndretFlagg(true);
		}
	}

	private void updateReturInfo(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (oppdaterJournalpostRequest.getDatoRetur() != null &&
				!oppdaterJournalpostRequest.getDatoRetur().equals(journalpost.getAvsendtReturDato())) {
			journalpost.setAvsendtReturDato(oppdaterJournalpostRequest.getDatoRetur());
			journalpost.setAntallRetur(journalpost.getAntallRetur() == null ? 1 : (journalpost.getAntallRetur() + 1));
			endret.setEndretFlagg(true);
		}
	}

	private void updateDatoMottatt(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {

		if (JournalpostTypeCode.I.equals(journalpost.getJournalposttype())) {
			if (oppdaterJournalpostRequest.getDatoMottatt() == null) {
				journalpost.setMottattDato(java.sql.Date.valueOf(LocalDate.now()));
			} else {
				journalpost.setMottattDato(oppdaterJournalpostRequest.getDatoMottatt());
			}
			endret.setEndretFlagg(true);
		}

	}

	private void updateTilleggsopplysninger(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (oppdaterJournalpostRequest.getTilleggsopplysninger() != null && !oppdaterJournalpostRequest.getTilleggsopplysninger()
				.isEmpty()) {
			journalpost.setTilleggsopplysninger(mapTilleggsopplysninger(oppdaterJournalpostRequest.getTilleggsopplysninger()));
			endret.setEndretFlagg(true);
		}
	}

	private void updateBehandlingstema(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getBehandlingstema())) {
			journalpost.setBehandlingstema(Behandlingstema.valueOf(oppdaterJournalpostRequest.getBehandlingstema()));
			endret.setEndretFlagg(true);
		}
	}

	private void updateAvsenderMottaker(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (oppdaterJournalpostRequest.getAvsenderMottaker() != null) {
			AvsenderMottaker ny = oppdaterJournalpostRequest.getAvsenderMottaker();
			if (ny.getId() != null) {
				if (ny.getIdType() != null &&
						oversettAvsenderMottakerIdType(ny.getIdType()) != journalpost.getAvsenderMottakerIdType()) {
					journalpost.setAvsenderMottakerIdType(oversettAvsenderMottakerIdType(ny.getIdType()));
					endret.setEndretFlagg(true);
				}
				if (!ny.getId().equalsIgnoreCase(journalpost.getAvsenderMottakerId())) {
					journalpost.setAvsenderMottakerId(ny.getId());
					endret.setEndretFlagg(true);
				}
				if (DELETE_MARKER.equalsIgnoreCase(ny.getId())) {
					journalpost.setAvsenderMottakerId(null);
					journalpost.setAvsenderMottakerIdType(null);
					endret.setEndretFlagg(true);
				}
			}

			if (isNotBlank(ny.getLand())) {
				journalpost.setLand(ny.getLand());
				endret.setEndretFlagg(true);
			}

			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn())) {
				journalpost.setAvsenderMottaker(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn());
				endret.setEndretFlagg(true);
			}
		}
	}

	private AvsenderMottakerIdTypeCode oversettAvsenderMottakerIdType(AvsenderMottakerIdType idType) {
		switch (idType) {
			case FNR:
				return AvsenderMottakerIdTypeCode.FNR;
			case HPRNR:
				return AvsenderMottakerIdTypeCode.HPRNR;
			case ORGNR:
				return AvsenderMottakerIdTypeCode.ORGNR;
			case UTL_ORG:
				return AvsenderMottakerIdTypeCode.UTL_ORG;
			default:
				return null;
		}
	}

	private void updateTema(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getTema()) && !oppdaterJournalpostRequest.getTema().equals(journalpost.getFagomrade().name())) {
			endret.add(JOURNALPOST_FAGOMRADE, journalpost.getFagomrade().name(), oppdaterJournalpostRequest.getTema());
			journalpost.setFagomrade(FagomradeCode.valueOf(oppdaterJournalpostRequest.getTema()));
		}
	}

	private Map<String, String> mapTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
		return tilleggsopplysninger.stream()
				.collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
	}

	private void updateTittel(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getTittel()) && !oppdaterJournalpostRequest.getTittel().equals(journalpost.getInnhold())) {
			endret.add(JOURNALPOST_INNHOLD, journalpost.getInnhold(), oppdaterJournalpostRequest.getTittel());
			journalpost.setInnhold(oppdaterJournalpostRequest.getTittel());
		}
	}

	private void updateBruker(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, ChangeTracker endret) {
		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			brukerRepository.deleteBrukerByJournalpostId(journalpost.getJournalpostId().toString());
			journalpost.clearBrukere();

			if (oppdaterJournalpostRequest.getBruker() != null) {
				Bruker bruker = new Bruker();
				assertNotNull(oppdaterJournalpostRequest.getBruker().getIdType(), "Bruker.idType");
				checkIfBrukerTypeIsAktoerId(null, bruker, oppdaterJournalpostRequest, journalpost, endret);

			}
		} else {
			if (oppdaterJournalpostRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					String oldBrukerId = bruker.getBrukerId();
					bruker.setBrukerId(oppdaterJournalpostRequest.getBruker().getId());
					assertNotNull(oppdaterJournalpostRequest.getBruker().getIdType(), "Bruker.idType");
					checkIfBrukerTypeIsAktoerId(oldBrukerId, bruker, oppdaterJournalpostRequest, journalpost, endret);
				});

			}
		}
	}

	private void assertNotNull(Object object, String fieldName) {
		if (object == null) {
			throw new InputValideringFeiletException(String.format("%s kan ikke være null", fieldName));
		}
	}

	private void checkIfBrukerTypeIsAktoerId(String oldBrukerId, Bruker bruker, OppdaterJournalpostRequest oppdaterJournalpostRequest, Journalpost journalpost, ChangeTracker endret) {
		if (BrukerIdType.AKTOERID.equals(oppdaterJournalpostRequest.getBruker().getIdType())) {
			try {
				String fnr = aktoerConsumerService.hentIdentForAktoerId(new HentIdentForAktoerIdRequestTo(oppdaterJournalpostRequest.getBruker().getId())).getIdent();
				bruker.setBrukerType(BrukerTypeCode.PERSON);
				bruker.setBrukerId(fnr);
				addBruker(oldBrukerId, bruker, journalpost, endret);


			} catch (PersonIkkeFunnetException e) {
				// Fortsett uten å oppdatere bruker
			}
		} else {
			bruker.setBrukerId(oppdaterJournalpostRequest.getBruker().getId());
			bruker.setBrukerType(BrukerIdType.ORGNR.equals(oppdaterJournalpostRequest.getBruker().getIdType()) ?
					BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
			addBruker(oldBrukerId, bruker, journalpost, endret);
		}
	}

	private void addBruker(String oldBrukerId, Bruker nyBruker, Journalpost journalpost, ChangeTracker endret) {
		nyBruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
		endret.add(JOURNALPOST_BRUKER, oldBrukerId, nyBruker.getBrukerId());
		journalpost.addBruker(nyBruker);

	}
}
