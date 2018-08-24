package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Component
public class PutInngaaendeJournalpostMapper extends AbstractInngaaendeJournalpostMapper {

	public void map(Journalpost journalpost, PutJournalpostRequest putJournalpostRequest) {
		//TODO: Ikke sett felter hvis input er null?
		if (isNotBlank(putJournalpostRequest.getTittel())){
			journalpost.setInnhold(putJournalpostRequest.getTittel());
		}
		if (isNotBlank(putJournalpostRequest.getTema())) {
			journalpost.setFagomrade(FagomradeCode.valueOf(putJournalpostRequest.getTema()));
		}
		if (putJournalpostRequest.getAvsender() != null){
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())){
				journalpost.setAvsenderMottaker(putJournalpostRequest.getAvsender().getNavn());
			}
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putJournalpostRequest.getAvsender().getIdentifikator());
			}
		}
		if (putJournalpostRequest.getArkivSak() != null) {
			Saksrelasjon saksrelasjon = new Saksrelasjon();
			saksrelasjon.setSakId(putJournalpostRequest.getArkivSak().getArkivSakId());
			saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(putJournalpostRequest.getArkivSak().getArkivSakSystem()));
			saksrelasjon.setOpprettetKildeNavn("OpprettetAv"); //TODO: hent fra MDC
			journalpost.setSaksrelasjon(saksrelasjon);
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

//		// //TODO: endelig journalfør. !! kun dersom
//		if (putJournalpostRequest.getForsoekEndeligJF()) {
//			journalpost.setJournalstatus(JournalStatusCode.J);
//			journalpost.setJournalForendeEnhetId(putJournalpostRequest.getJournalfEnhet());
//			journalpost.setJournalDato(new Date());
//			journalpost.setEndretAvNavn("dfd"); //TODO Fra MDC
//			journalpost.setJournalfortAvNavn("dfdf"); //TODO
//		}
	}

}
