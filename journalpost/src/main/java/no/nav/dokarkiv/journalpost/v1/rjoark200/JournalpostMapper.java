package no.nav.dokarkiv.journalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalpost.v1.rjoark200.util.Utils.assertNotNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JournalpostMapper {

	private final BrukerRepository brukerRepository;
	private final AksjonsLoggService aksjonsLoggService;

	@Inject
	public JournalpostMapper(BrukerRepository brukerRepository, AksjonsLoggService aksjonsLoggService) {
		this.brukerRepository = brukerRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterJournalpost(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest) throws UgyldigAksjonsLoggException {

		boolean endret = false;
		AksjonsLoggHelper aksjonsLoggHelperMetadata = new AksjonsLoggHelper();
		aksjonsLoggHelperMetadata.setAksjonsLoggTO(AksjonsTypeCode.ENDRE_METADATA);

		if (isNotBlank(oppdaterJournalpostRequest.getTittel())) {
			aksjonsLoggHelperMetadata.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.innhold")
					.fraVerdi(journalpost.getInnhold())
					.tilVerdi(oppdaterJournalpostRequest.getTittel())
					.build());
			journalpost.setInnhold(oppdaterJournalpostRequest.getTittel());
			endret = true;
		}
		if (isNotBlank(oppdaterJournalpostRequest.getTema())) {
			aksjonsLoggHelperMetadata.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.fagomrade")
					.fraVerdi(journalpost.getFagomrade().name())
					.tilVerdi(oppdaterJournalpostRequest.getTema())
					.build());
			journalpost.setFagomrade(FagomradeCode.valueOf(oppdaterJournalpostRequest.getTema()));
			endret = true;
		}
		if (oppdaterJournalpostRequest.getAvsenderMottaker() != null) {
			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn())) {
				journalpost.setAvsenderMottaker(oppdaterJournalpostRequest.getAvsenderMottaker().getNavn());
				endret = true;
			}
			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getId())) {
				journalpost.setAvsenderMottakerId(oppdaterJournalpostRequest.getAvsenderMottaker().getId());
				endret = true;
			}
			if (isNotBlank(oppdaterJournalpostRequest.getAvsenderMottaker().getLand())) {
				journalpost.setLand(oppdaterJournalpostRequest.getAvsenderMottaker().getLand());
				endret = true;
			}
		}
        if (isNotBlank(oppdaterJournalpostRequest.getBehandlingstema())) {
            journalpost.setBehandlingstema(Behandlingstema.valueOf(oppdaterJournalpostRequest.getBehandlingstema()));
            endret = true;
        }

        if (oppdaterJournalpostRequest.getTilleggsopplysninger() != null && !oppdaterJournalpostRequest.getTilleggsopplysninger().isEmpty()) {
            journalpost.setTilleggsopplysninger(mapTilleggsopplysninger(oppdaterJournalpostRequest.getTilleggsopplysninger()));
        }

		updateSaksrelasjonFields(journalpost, oppdaterJournalpostRequest);

		boolean brukerEndret = updateBrukerFraRequest(journalpost, oppdaterJournalpostRequest);
		if (brukerEndret) {
			endret = true;
		}

		if (endret) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}

		if (!aksjonsLoggHelperMetadata.getArkivElementEndringTOs().isEmpty()) {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelperMetadata.getAksjonsLoggTO(), aksjonsLoggHelperMetadata
					.getArkivElementEndringTOs());
		}
	}

    private Map<String, String> mapTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
	    return tilleggsopplysninger.stream().collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
    }

    private boolean updateBrukerFraRequest(Journalpost journalpost, OppdaterJournalpostRequest oppdaterJournalpostRequest) {
		boolean endret = false;
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

				endret = true;
			}
		} else {
			if (oppdaterJournalpostRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					bruker.setBrukerId(oppdaterJournalpostRequest.getBruker().getId());
					assertNotNull(oppdaterJournalpostRequest.getBruker().getIdType(), "Bruker.idType");
					bruker.setBrukerType(BrukerIdType.ORGNR.equals(oppdaterJournalpostRequest.getBruker().getIdType()) ?
							BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				});
				endret = true;
			}
		}
		return endret;
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, OppdaterJournalpostRequest request) throws UgyldigAksjonsLoggException {
		boolean endret = false;
		boolean newSak = false;

		if (request.getSak() != null) {
			Saksrelasjon saksrelasjon;
			AksjonsLoggHelper aksjonsLoggHelperSakstilknytning = new AksjonsLoggHelper();
			aksjonsLoggHelperSakstilknytning.setAksjonsLoggTO(AksjonsTypeCode.SAKSTILKNYTNING);

			if (journalpost.getSaksrelasjon() == null) {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
			} else {
				saksrelasjon = journalpost.getSaksrelasjon();
			}
			if (isNotBlank(request.getSak().getArkivsaksnummer())) {
				aksjonsLoggHelperSakstilknytning.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
						.arkivElement("Saksrelasjon.sakId")
						.fraVerdi(journalpost.getSaksrelasjon().getSakId())
						.tilVerdi(request.getSak().getArkivsaksnummer())
						.build());
				saksrelasjon.setSakId(request.getSak().getArkivsaksnummer());
				endret = true;
			}
			if (request.getSak().getArkivsaksystem() != null) {
				aksjonsLoggHelperSakstilknytning.addToArkivElementEndringTOs(ArkivElementEndringTO.builder()
						.arkivElement("Saksrelasjon.fagsystem")
						.fraVerdi(journalpost.getSaksrelasjon().getFagsystem().name())
						.tilVerdi(request.getSak().getArkivsaksystem().name())
						.build());
				saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getSak().getArkivsaksystem()));
				endret = true;
			}
			if (endret && !newSak) {
				saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_ID));
				saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			}
			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
			}

			if (!aksjonsLoggHelperSakstilknytning.getArkivElementEndringTOs().isEmpty()) {
				aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggHelperSakstilknytning.getAksjonsLoggTO(), aksjonsLoggHelperSakstilknytning
						.getArkivElementEndringTOs());
			}
		}
	}

	protected FagsystemCode mapArkivSakSystemToFagsystemCode(Arkivsaksystem arkivsaksystem) {
		assertNotNull(arkivsaksystem, "arkivsaksystem");
		if (Arkivsaksystem.GSAK.equals(arkivsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			return FagsystemCode.PEN;
		}
	}
}
