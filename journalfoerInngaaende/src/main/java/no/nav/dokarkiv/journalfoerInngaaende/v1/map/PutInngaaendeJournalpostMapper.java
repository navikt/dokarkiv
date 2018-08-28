package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
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
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())) {
				journalpost.setAvsenderMottaker(putJournalpostRequest.getAvsender().getNavn());
			}
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putJournalpostRequest.getAvsender().getIdentifikator());
			}
		}
		if (putJournalpostRequest.getArkivSak() != null) {
			if (journalpost.getSaksrelasjon() != null) {
				journalpost.getSaksrelasjon().setSakId(putJournalpostRequest.getArkivSak().getArkivSakId());
				journalpost.getSaksrelasjon().setFagsystem(mapArkivSakSystemToFagsystemCode(putJournalpostRequest.getArkivSak().getArkivSakSystem()));
				journalpost.getSaksrelasjon().setEndretKildeNavn("ENDRET_AV"); //TODO: hent fra MDC
			} else {
				Saksrelasjon saksrelasjon = new Saksrelasjon();
				saksrelasjon.setSakId(putJournalpostRequest.getArkivSak().getArkivSakId());
				saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(putJournalpostRequest.getArkivSak().getArkivSakSystem()));
				saksrelasjon.setOpprettetKildeNavn("OpprettetAv"); //TODO: hent fra MDC
				journalpost.setSaksrelasjon(saksrelasjon);
			}
		}

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

}
