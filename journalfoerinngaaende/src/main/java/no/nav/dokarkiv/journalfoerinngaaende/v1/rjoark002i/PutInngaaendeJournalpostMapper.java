package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark002i;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.assertNotNull;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSakWithArkivsakSystemEnum;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
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
import java.util.Set;

@Component
public class PutInngaaendeJournalpostMapper {

	@Inject
	private BrukerRepository brukerRepository;

	public void oppdaterJournalpost(Journalpost journalpost, PutJournalpostRequest putJournalpostRequest) {

		boolean endret = false;

		if (isNotBlank(putJournalpostRequest.getTittel())) {
			journalpost.setInnhold(putJournalpostRequest.getTittel());
			endret = true;
		}
		if (isNotBlank(putJournalpostRequest.getTema())) {
			journalpost.setFagomrade(FagomradeCode.valueOf(putJournalpostRequest.getTema()));
			endret = true;
		}
		if (putJournalpostRequest.getAvsender() != null) {
			if (isNotBlank(putJournalpostRequest.getAvsender().getNavn())) {
				journalpost.setAvsenderMottaker(putJournalpostRequest.getAvsender().getNavn());
				endret = true;
			}
			if (isNotBlank(putJournalpostRequest.getAvsender().getIdentifikator())) {
				journalpost.setAvsenderMottakerId(putJournalpostRequest.getAvsender().getIdentifikator());
				endret = true;
			}
		}

		updateSaksrelasjonFields(journalpost, putJournalpostRequest);

		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty() || brukere.size() > 1) {
			brukerRepository.deleteBrukerByJournalpostId(journalpost.getJournalpostId().toString());
			journalpost.clearBrukere();

			if (putJournalpostRequest.getBruker() != null) {
				Bruker bruker = new Bruker();
				bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
				assertNotNull(putJournalpostRequest.getBruker().getBrukerType(), "bruker.brukerType");
				bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
				bruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				journalpost.addBruker(bruker);

				endret = true;
			}
		} else {
			if (putJournalpostRequest.getBruker() != null) {
				brukere.iterator().forEachRemaining(bruker -> {
					bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
					assertNotNull(putJournalpostRequest.getBruker().getBrukerType(), "bruker.brukerType");
					bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
				});
				endret = true;
			}
		}

		if (endret) {
			journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, PutJournalpostRequest request) {
		boolean endret = false;
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
			if (isNotBlank(request.getArkivSak().getArkivSakId())) {
				saksrelasjon.setSakId(request.getArkivSak().getArkivSakId());
				endret = true;
			}
			if (request.getArkivSak().getArkivSakSystem() != null) {
				saksrelasjon.setFagsystem(mapArkivSakSystemToFagsystemCode(request.getArkivSak().getArkivSakSystem()));
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

	protected FagsystemCode mapArkivSakSystemToFagsystemCode(ArkivSakWithArkivsakSystemEnum.ArkivSakSystem arkivSakSystem) {
		assertNotNull(arkivSakSystem, "arkivsaksystem");
		if (ArkivsystemKode.GSAK.name().equals(arkivSakSystem.name())) {
			return FagsystemCode.FS22;
		} else {
			return FagsystemCode.PEN;
		}
	}

	private enum ArkivsystemKode {
		GSAK,
		PSAK
	}

}
