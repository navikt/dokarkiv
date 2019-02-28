package no.nav.dokarkiv.oppdatermetadata.v1.rjoark200;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.Utils.assertNotNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.oppdatermetadata.api.v1.Arkivsaksystem;
import no.nav.dok.oppdatermetadata.api.v1.BrukerIdType;
import no.nav.dok.oppdatermetadata.api.v1.PutOppdatermetadataRequest;
import no.nav.dok.oppdatermetadata.api.v1.Tilleggsopplysning;
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

	public void oppdaterJournalpost(Journalpost journalpost, PutOppdatermetadataRequest putOppdatermetadataRequest) {

		boolean endret = false;

		if (isNotBlank(putOppdatermetadataRequest.getTittel())) {
			journalpost.setInnhold(putOppdatermetadataRequest.getTittel());
			endret = true;
		}
		if (isNotBlank(putOppdatermetadataRequest.getTema())) {
			journalpost.setFagomrade(FagomradeCode.valueOf(putOppdatermetadataRequest.getTema()));
			endret = true;
		}
		if (putOppdatermetadataRequest.getAvsenderMottaker() != null) {
			if (isNotBlank(putOppdatermetadataRequest.getAvsenderMottaker().getAvsenderMottakerNavn())) {
				journalpost.setAvsenderMottaker(putOppdatermetadataRequest.getAvsenderMottaker().getAvsenderMottakerNavn());
				endret = true;
			}
			if (isNotBlank(putOppdatermetadataRequest.getAvsenderMottaker().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putOppdatermetadataRequest.getAvsenderMottaker().getIdentifikator());
				endret = true;
			}
		}
        if (isNotBlank(putOppdatermetadataRequest.getBehandlingstema())) {
            journalpost.setBehandlingstema(Behandlingstema.valueOf(putOppdatermetadataRequest.getBehandlingstema()));
            endret = true;
        }
        if (isNotBlank(putOppdatermetadataRequest.getAvsenderMottakerLand())) {
            journalpost.setLand(putOppdatermetadataRequest.getAvsenderMottakerLand());
            endret = true;
        }

        if (putOppdatermetadataRequest.getTilleggsopplysninger() != null && !putOppdatermetadataRequest.getTilleggsopplysninger().isEmpty()) {
            journalpost.setTilleggsopplysninger(MapTilleggsopplysninger(putOppdatermetadataRequest.getTilleggsopplysninger()));
        }

		updateSaksrelasjonFields(journalpost, putOppdatermetadataRequest);

		boolean brukerEndret = updateBrukerFraRequest(journalpost, putOppdatermetadataRequest);
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

    private boolean updateBrukerFraRequest(Journalpost journalpost, PutOppdatermetadataRequest putOppdatermetadataRequest) {
		boolean endret = false;
		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			brukerRepository.deleteBrukerByJournalpostId(journalpost.getJournalpostId().toString());
			journalpost.clearBrukere();

			if (putOppdatermetadataRequest.getBruker() != null) {
				Bruker bruker = new Bruker();
				bruker.setBrukerId(putOppdatermetadataRequest.getBruker().getIdentifikator());
				assertNotNull(putOppdatermetadataRequest.getBruker().getBrukerIdType(), "bruker.brukerIdType");
				bruker.setBrukerType(BrukerIdType.ORGNR.equals(putOppdatermetadataRequest.getBruker().getBrukerIdType()) ?
						BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				bruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				journalpost.addBruker(bruker);

				endret = true;
			}
		} else {
			if (putOppdatermetadataRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					bruker.setBrukerId(putOppdatermetadataRequest.getBruker().getIdentifikator());
					assertNotNull(putOppdatermetadataRequest.getBruker().getBrukerIdType(), "bruker.brukerIdType");
					bruker.setBrukerType(BrukerIdType.ORGNR.equals(putOppdatermetadataRequest.getBruker().getBrukerIdType()) ?
							BrukerTypeCode.ORGANISASJON : BrukerTypeCode.PERSON);
				});
				endret = true;
			}
		}
		return endret;
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, PutOppdatermetadataRequest request) {
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
