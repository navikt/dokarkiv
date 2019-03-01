package no.nav.dokarkiv.oppdaterjournalpost.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.oppdaterjournalpost.v1.util.Utils.assertNotNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.oppdaterjournalpost.api.v1.Arkivsaksystem;
import no.nav.dok.oppdaterjournalpost.api.v1.BrukerIdType;
import no.nav.dok.oppdaterjournalpost.api.v1.PutOppdaterJournalpostRequest;
import no.nav.dok.oppdaterjournalpost.api.v1.Tilleggsopplysning;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JournalpostMapper {

	@Inject
	private BrukerRepository brukerRepository;

	public void oppdaterJournalpost(Journalpost journalpost, PutOppdaterJournalpostRequest putOppdaterJournalpostRequest) {

		boolean endret = false;

		if (isNotBlank(putOppdaterJournalpostRequest.getTittel())) {
			journalpost.setInnhold(putOppdaterJournalpostRequest.getTittel());
			endret = true;
		}
		if (isNotBlank(putOppdaterJournalpostRequest.getTema())) {
			journalpost.setFagomrade(FagomradeCode.valueOf(putOppdaterJournalpostRequest.getTema()));
			endret = true;
		}
		if (putOppdaterJournalpostRequest.getAvsenderMottaker() != null) {
			if (isNotBlank(putOppdaterJournalpostRequest.getAvsenderMottaker().getAvsenderMottakerNavn())) {
				journalpost.setAvsenderMottaker(putOppdaterJournalpostRequest.getAvsenderMottaker().getAvsenderMottakerNavn());
				endret = true;
			}
			if (isNotBlank(putOppdaterJournalpostRequest.getAvsenderMottaker().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putOppdaterJournalpostRequest.getAvsenderMottaker().getIdentifikator());
				endret = true;
			}
		}
        if (isNotBlank(putOppdaterJournalpostRequest.getBehandlingstema())) {
            journalpost.setBehandlingstema(Behandlingstema.valueOf(putOppdaterJournalpostRequest.getBehandlingstema()));
            endret = true;
        }
        if (isNotBlank(putOppdaterJournalpostRequest.getAvsenderMottakerLand())) {
            journalpost.setLand(putOppdaterJournalpostRequest.getAvsenderMottakerLand());
            endret = true;
        }

        if (putOppdaterJournalpostRequest.getTilleggsopplysninger() != null && !putOppdaterJournalpostRequest.getTilleggsopplysninger().isEmpty()) {
            journalpost.setTilleggsopplysninger(MapTilleggsopplysninger(putOppdaterJournalpostRequest.getTilleggsopplysninger()));
        }

		updateSaksrelasjonFields(journalpost, putOppdaterJournalpostRequest);

		boolean brukerEndret = updateBrukerFraRequest(journalpost, putOppdaterJournalpostRequest);
		if (brukerEndret) {
			endret = true;
		}

		if (endret) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
	}

    private Map<String, String> MapTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
	    return tilleggsopplysninger.stream().collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
    }

    private boolean updateBrukerFraRequest(Journalpost journalpost, PutOppdaterJournalpostRequest putOppdaterJournalpostRequest) {
		boolean endret = false;
		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			brukerRepository.deleteBrukerByJournalpostId(journalpost.getJournalpostId().toString());
			journalpost.clearBrukere();

			if (putOppdaterJournalpostRequest.getBruker() != null) {
				Bruker bruker = new Bruker();
				bruker.setBrukerId(putOppdaterJournalpostRequest.getBruker().getIdentifikator());
				assertNotNull(putOppdaterJournalpostRequest.getBruker().getBrukerIdType(), "bruker.brukerIdType");
				bruker.setBrukerType(BrukerIdType.ORGNR.equals(putOppdaterJournalpostRequest.getBruker().getBrukerIdType()) ?
						BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				bruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				journalpost.addBruker(bruker);

				endret = true;
			}
		} else {
			if (putOppdaterJournalpostRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					bruker.setBrukerId(putOppdaterJournalpostRequest.getBruker().getIdentifikator());
					assertNotNull(putOppdaterJournalpostRequest.getBruker().getBrukerIdType(), "bruker.brukerIdType");
					bruker.setBrukerType(BrukerIdType.ORGNR.equals(putOppdaterJournalpostRequest.getBruker().getBrukerIdType()) ?
							BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				});
				endret = true;
			}
		}
		return endret;
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, PutOppdaterJournalpostRequest request) {
		boolean endret = false;
		boolean newSak = false;
		if (request.getArkivsak() != null) {
			Saksrelasjon saksrelasjon;
			if (journalpost.getSaksrelasjon() == null) {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
			} else {
				saksrelasjon = journalpost.getSaksrelasjon();
			}
			if (isNotBlank(request.getArkivsak().getArkivsaksnummer())) {
				saksrelasjon.setSakId(request.getArkivsak().getArkivsaksnummer());
				endret = true;
			}
			if (request.getArkivsak().getArkivsaksystem() != null) {
				saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getArkivsak().getArkivsaksystem()));
				endret = true;
			}
			if (endret && !newSak) {
				saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_ID));
				saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			}
			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
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
