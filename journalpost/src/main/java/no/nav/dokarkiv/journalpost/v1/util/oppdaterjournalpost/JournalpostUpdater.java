package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import no.nav.dokarkiv.journalpost.v1.util.AksjonsLoggHelper;
import no.nav.dokarkiv.journalpost.v1.util.Endret;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JournalpostUpdater {

	private final BrukerRepository brukerRepository;

	@Inject
	public JournalpostUpdater(BrukerRepository brukerRepository) {
		this.brukerRepository = brukerRepository;
	}

	public void updateFields(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, AksjonsLoggHelper aksjonsLoggHelper) throws UgyldigAksjonsLoggException {

		Endret endret = new Endret();
		aksjonsLoggHelper.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA);

		updateTittel(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper, endret);
		updateTema(journalpost, oppdaterJournalpostRequest, aksjonsLoggHelper, endret);
		updateAvsenderMottaker(journalpost, oppdaterJournalpostRequest, endret);
		updateBehandlingstema(journalpost, oppdaterJournalpostRequest, endret);
		updateTilleggsopplysninger(journalpost, oppdaterJournalpostRequest, endret);
		updateJournalfoerendeEnhet(journalpost, oppdaterJournalpostRequest, endret);
		updateReturInfo(journalpost, oppdaterJournalpostRequest, endret);
		updateBruker(journalpost, oppdaterJournalpostRequest, endret);

		if (endret.isEndretFlagg()) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
	}

	private void updateJournalfoerendeEnhet(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getJournalfoerendeEnhet())) {
			journalpost.setJournalForendeEnhetId(oppdaterJournalpostRequest.getJournalfoerendeEnhet());
			endret.setEndretFlagg(true);
		}
	}

	private void updateReturInfo(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		if (oppdaterJournalpostRequest.getDatoRetur() != null) {
			journalpost.setAvsendtReturDato(oppdaterJournalpostRequest.getDatoRetur());
			journalpost.setAntallRetur(journalpost.getAntallRetur() == null ? 1 : (journalpost.getAntallRetur()+1));
			endret.setEndretFlagg(true);
		}
	}

	private void updateTilleggsopplysninger(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		if (oppdaterJournalpostRequest.getTilleggsopplysninger() != null && !oppdaterJournalpostRequest.getTilleggsopplysninger()
				.isEmpty()) {
			journalpost.setTilleggsopplysninger(mapTilleggsopplysninger(oppdaterJournalpostRequest.getTilleggsopplysninger()));
			endret.setEndretFlagg(true);
		}
	}

	private void updateBehandlingstema(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getBehandlingstema())) {
			journalpost.setBehandlingstema(Behandlingstema.valueOf(oppdaterJournalpostRequest.getBehandlingstema()));
			endret.setEndretFlagg(true);
		}
	}

	private void updateAvsenderMottaker(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		if (oppdaterJournalpostRequest.getAvsenderMottaker() != null) {
			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn())) {
				journalpost.setAvsenderMottaker(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn());
				endret.setEndretFlagg(true);
			}
			if ((oppdaterJournalpostRequest.getAvsenderMottaker().getId()) != null) {
				journalpost.setAvsenderMottakerId(oppdaterJournalpostRequest.getAvsenderMottaker().getId());
				if(oppdaterJournalpostRequest.getAvsenderMottaker().getId().trim().length() == 0) {
					journalpost.setAvsenderMottakerId(null);
				}
				endret.setEndretFlagg(true);
			}
			if (oppdaterJournalpostRequest.getAvsenderMottaker().getIdType() != null) {
				if (AvsenderMottakerIdType.FNR.equals(oppdaterJournalpostRequest.getAvsenderMottaker()
						.getIdType())) {
					journalpost.setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR);
				} else if (AvsenderMottakerIdType.ORGNR.equals(oppdaterJournalpostRequest.getAvsenderMottaker()
						.getIdType())) {
					journalpost.setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode.ORGNR);
				} else if (AvsenderMottakerIdType.HPRNR.equals(oppdaterJournalpostRequest.getAvsenderMottaker()
						.getIdType())) {
					journalpost.setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode.HPRNR);
				} else if (AvsenderMottakerIdType.UTL_ORG.equals(oppdaterJournalpostRequest.getAvsenderMottaker()
						.getIdType())) {
					journalpost.setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode.UTL_ORG);
				}
				endret.setEndretFlagg(true);

			}

			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getLand())) {
				journalpost.setLand(oppdaterJournalpostRequest.getAvsenderMottaker().getLand());
				endret.setEndretFlagg(true);
			}
		}
	}

	private void updateTema(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, AksjonsLoggHelper aksjonsLoggHelperMetadata, Endret endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getTema())) {
			aksjonsLoggHelperMetadata.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.fagomrade")
					.fraVerdi(journalpost.getFagomrade().name())
					.tilVerdi(oppdaterJournalpostRequest.getTema())
					.build());
			journalpost.setFagomrade(FagomradeCode.valueOf(oppdaterJournalpostRequest.getTema()));
			endret.setEndretFlagg(true);
		}
	}

	private Map<String, String> mapTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
		return tilleggsopplysninger.stream()
				.collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
	}

	private void updateTittel(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, AksjonsLoggHelper aksjonsLoggHelperMetadata, Endret endret) {
		if (isNotBlank(oppdaterJournalpostRequest.getTittel())) {
			aksjonsLoggHelperMetadata.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.innhold")
					.fraVerdi(journalpost.getInnhold())
					.tilVerdi(oppdaterJournalpostRequest.getTittel())
					.build());
			journalpost.setInnhold(oppdaterJournalpostRequest.getTittel());
			endret.setEndretFlagg(true);
		}

	}

	private void updateBruker(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest, Endret endret) {
		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			brukerRepository.deleteBrukerByJournalpostId(journalpost.getJournalpostId().toString());
			journalpost.clearBrukere();

			if (oppdaterJournalpostRequest.getBruker() != null) {
				Bruker bruker = new Bruker();
				bruker.setBrukerId(oppdaterJournalpostRequest.getBruker().getId());
				assertNotNull(oppdaterJournalpostRequest.getBruker().getIdType(), "Bruker.idType");
				bruker.setBrukerType(BrukerIdType.ORGNR.equals(oppdaterJournalpostRequest.getBruker().getIdType()) ?
						BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				bruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				journalpost.addBruker(bruker);
				endret.setEndretFlagg(true);
			}
		} else {
			if (oppdaterJournalpostRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					bruker.setBrukerId(oppdaterJournalpostRequest.getBruker().getId());
					assertNotNull(oppdaterJournalpostRequest.getBruker().getIdType(), "Bruker.idType");
					bruker.setBrukerType(BrukerIdType.ORGNR.equals(oppdaterJournalpostRequest.getBruker().getIdType()) ?
							BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				});
				endret.setEndretFlagg(true);
			}
		}
	}

	private void assertNotNull(Object object, String fieldName) {
		if (object == null) {
			throw new InputValideringFeiletException(String.format("%s kan ikke være null", fieldName));
		}
	}
}
