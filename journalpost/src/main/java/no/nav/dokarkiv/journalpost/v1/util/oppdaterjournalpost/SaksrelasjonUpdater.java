package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_APPLIKASJON;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_FAGSAKNR;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class SaksrelasjonUpdater {

	private static final String APPLIKASJON_FS22 = "FS22";

	public ChangeTracker updateFields(Journalpost journalpost, OppdaterJournalpostRequest request, Long sakId) {
		ChangeTracker endret = new ChangeTracker();
		boolean newSak = false;

		if (request.getSak() != null) {
			Saksrelasjon saksrelasjon;

			if (journalpost.getSaksrelasjon() == null) {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
			} else {
				saksrelasjon = journalpost.getSaksrelasjon();
			}

			updateSaksnummer(sakId, request, saksrelasjon, endret);
			updateArkivsaksystem(request, saksrelasjon, endret);

			endret.add(SAK_FAGSAKNR, null, request.getSak().getFagsakId());
			endret.add(SAK_APPLIKASJON, null, FAGSAK.equals(request.getSak().getSakstype()) ?
					request.getSak().getFagsaksystem().name() : APPLIKASJON_FS22);

			if (endret.isEndretFlagg() && !newSak) {
				saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_NAME));
				saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			} else if (!endret.isEndretFlagg() && !newSak) {
				MDC.put("mma-6992", "request.sak=samme_sak");
				vurderResetSaksrelasjon(journalpost, request, endret);
			}
			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
			}
		} else {
			MDC.put("mma-6992", "request.sak=null");
			vurderResetSaksrelasjon(journalpost, request, endret);
		}
		return endret;
	}

	private static void vurderResetSaksrelasjon(Journalpost journalpost, OppdaterJournalpostRequest request, ChangeTracker endret) {
		vurderResetSaksrelasjonTemaEndring(journalpost, request, endret);
		vurderResetSaksrelasjonBrukerEndring(journalpost, request, endret);
	}

	private static void vurderResetSaksrelasjonTemaEndring(Journalpost journalpost, OppdaterJournalpostRequest request, ChangeTracker endret) {
		String nyTema = request.getTema();
		if (isBlank(nyTema)) {
			return;
		}
		String tema = journalpost.getFagomrade().name();
		if (!tema.equals(nyTema)) {
			journalpost.setSaksrelasjon(null);
			endret.add(SAKSRELASJON_FAGSYSTEM,tema, "");
			//todo nuller ut saksrelasjon
			log.info("oppdaterJournalpost - Forsøker annen tema oppdatering der request.sak=null. tema={}, nyTema={}", tema, nyTema);
		}
	}

	private static void vurderResetSaksrelasjonBrukerEndring(Journalpost journalpost, OppdaterJournalpostRequest request, ChangeTracker endret) {
		if (request.getBruker() == null) {
			return;
		}
		String nyBrukerId = request.getBruker().getId();
		if (journalpost.getBrukere().isEmpty()) {
			return;
		}
		String brukerId = journalpost.getBrukere().iterator().next().getBrukerId();
		if (isBlank(brukerId)) {
			return;
		}
		if (!brukerId.equals(nyBrukerId)) {
			endret.add(SAKSRELASJON_FAGSYSTEM,journalpost.getSaksrelasjon().getFagsystem().toString(), null);
			journalpost.setSaksrelasjon(null);
			//todo nuller ut saksrelasjon
			log.info("oppdaterJournalpost - Forsøker annen bruker oppdatering der request.sak=null. brukerId er forskjellige");
		}
	}

	private static void nullUtSaksrelasjon(Journalpost journalpost) {
	}

	private void updateSaksnummer(Long sakId, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, ChangeTracker endret) {
		Long oldSakId = saksrelasjon.getSakId();
		if (sakId != null) { // arkivsak
			saksrelasjon.setSakId(sakId);
		} else if (Sakstype.ARKIVSAK.equals(request.getSak().getSakstype()) || request.getSak()
				.getSakstype() == null) {// Antas å være ARKIVSAK dersom feltet ikke er satt
			saksrelasjon.setSakId(parseLong(request.getSak().getArkivsaksnummer()));
		} else if (Sakstype.FAGSAK.equals(request.getSak()
				.getSakstype()) && Fagsaksystem.PP01.equals(request.getSak().getFagsaksystem())) {
			saksrelasjon.setSakId(parseLong(request.getSak().getFagsakId()));
		} else {
			throw new UgyldigInputException("Kan ikke oppdatere sakId basert på input");
		}
		if (oldSakId == null) {
			endret.add(SAKSRELASJON_SAKID, null, saksrelasjon.getSakId().toString());
		} else {
			endret.add(SAKSRELASJON_SAKID, oldSakId.toString(), saksrelasjon.getSakId().toString());
		}
	}

	private void updateArkivsaksystem(OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, ChangeTracker endret) {
		FagsystemCode oldFagsystem = saksrelasjon.getFagsystem();
		Sakstype sakstype = request.getSak().getSakstype();
		Arkivsaksystem arkivsaksystem = request.getSak().getArkivsaksystem();
		Fagsaksystem fagsaksystem = request.getSak().getFagsaksystem();
		if (Sakstype.ARKIVSAK.equals(sakstype) || request.getSak().getSakstype() == null) {
			saksrelasjon.setFagsystem(mapArkivsak(arkivsaksystem));
		} else {
			saksrelasjon.setFagsystem(mapFagsakEllerGenerellSak(sakstype, fagsaksystem));
		}
		endret.add(SAKSRELASJON_FAGSYSTEM, oldFagsystem == null ? null : oldFagsystem.name(), saksrelasjon.getFagsystem() == null ? null : saksrelasjon
				.getFagsystem()
				.name());
	}

	private FagsystemCode mapArkivsak(Arkivsaksystem arkivsaksystem) {
		if (Arkivsaksystem.PSAK.equals(arkivsaksystem)) {
			return FagsystemCode.PEN;
		} else if (Arkivsaksystem.GSAK.equals(arkivsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			throw new UgyldigInputException("Kan ikke mappe fagsystem basert på input");
		}
	}

	private FagsystemCode mapFagsakEllerGenerellSak(Sakstype sakstype, Fagsaksystem fagsaksystem) {
		if (isValidFagsaksystem(sakstype, fagsaksystem) && Fagsaksystem.PP01.equals(fagsaksystem)) {
			return FagsystemCode.PEN;
		} else if ((isValidFagsaksystem(sakstype, fagsaksystem) || Sakstype.GENERELL_SAK.equals(sakstype)) && !Fagsaksystem.PP01.equals(fagsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			throw new UgyldigInputException("Kan ikke mappe fagsystem basert på input");
		}
	}

	private boolean isValidFagsaksystem(Sakstype sakstype, Fagsaksystem fagsaksystem) {
		return Arrays.stream(Fagsaksystem.values())
				.filter(fagsak -> fagsak.equals(fagsaksystem) && Sakstype.FAGSAK.equals(sakstype))
				.findAny()
				.isPresent();
	}

	FagsystemCode mapArkivSakSystemToFagsystemCode(Arkivsaksystem arkivsaksystem) {
		assertNotNull(arkivsaksystem, "arkivsaksystem");
		if (Arkivsaksystem.GSAK.equals(arkivsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			return FagsystemCode.PEN;
		}
	}

	private void assertNotNull(Object object, String fieldName) {
		if (object == null) {
			throw new InputValideringFeiletException(String.format("%s kan ikke være null", fieldName));
		}
	}
}
