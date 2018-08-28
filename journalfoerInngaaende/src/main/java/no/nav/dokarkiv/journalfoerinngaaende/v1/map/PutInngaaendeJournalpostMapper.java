package no.nav.dokarkiv.journalfoerinngaaende.v1.map;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PutInngaaendeJournalpostMapper extends AbstractInngaaendeJournalpostMapper {

	public void oppdaterJournalpost(Journalpost journalpost, PutJournalpostRequest putJournalpostRequest) {
		if (isNotBlank(putJournalpostRequest.getTittel())) {
			journalpost.setInnhold(putJournalpostRequest.getTittel());
		}
		if (isNotBlank(putJournalpostRequest.getTema())) {
			journalpost.setFagomrade(FagomradeCode.valueOf(putJournalpostRequest.getTema()));
		}
		if (putJournalpostRequest.getAvsender() != null) {
			if (isNotBlank(putJournalpostRequest.getAvsender().getNavn())) {
				journalpost.setAvsenderMottaker(putJournalpostRequest.getAvsender().getNavn());
			}
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putJournalpostRequest.getAvsender().getIdentifikator());
			}
		}
		updateSaksrelasjonFields(journalpost, putJournalpostRequest);

		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			Bruker bruker = new Bruker();
			bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
			bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
			bruker.setOpprettetKildeNavn("OpprettetAv"); // TODO: hent fra MDC
			journalpost.clearBrukere();
			journalpost.addBruker(bruker);
		} else {
			brukere.iterator().forEachRemaining(bruker -> {
				bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
				bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
			});
		}
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, PutJournalpostRequest request) {
		boolean newSak = false;
		if (request.getArkivSak() != null) {
			Saksrelasjon saksrelasjon;
			if (journalpost.getSaksrelasjon() == null) {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
			} else {
				saksrelasjon = journalpost.getSaksrelasjon();
			}
			saksrelasjon.setSakId(request.getArkivSak().getArkivSakId());
			saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getArkivSak().getArkivSakSystem()));
			saksrelasjon.setEndretAvNavn("Endret av"); // TODO: hent fra MDC
			saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
			}
		}
	}

}
