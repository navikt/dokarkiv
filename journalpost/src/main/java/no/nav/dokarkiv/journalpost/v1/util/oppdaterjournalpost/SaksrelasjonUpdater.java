package no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost;

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

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_FAGSYSTEM;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_APPLIKASJON;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAK_FAGSAKNR;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;

@Component
public class SaksrelasjonUpdater {

	private static final String APPLIKASJON_FS22 = "FS22";

	public ChangeTracker updateFields(Journalpost journalpost, OppdaterJournalpostRequest request, String sakId) {
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
			}
			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
			}
		}
		return endret;
	}

	private void updateSaksnummer(String sakId, OppdaterJournalpostRequest request, Saksrelasjon saksrelasjon, ChangeTracker endret) {
		String oldSaksnummer = saksrelasjon.getSakId();
		if (sakId != null) { // arkivsak
			saksrelasjon.setSakId(sakId);
		} else if (Sakstype.ARKIVSAK.equals(request.getSak().getSakstype()) || request.getSak()
				.getSakstype() == null) {// Antas å være ARKIVSAK dersom feltet ikke er satt
			saksrelasjon.setSakId(request.getSak().getArkivsaksnummer());
		} else if (Sakstype.FAGSAK.equals(request.getSak()
				.getSakstype()) && Fagsaksystem.PP01.equals(request.getSak().getFagsaksystem())) {
			saksrelasjon.setSakId(request.getSak().getFagsakId());
		} else {
			throw new UgyldigInputException("Kan ikke oppdatere sakId basert på input");
		}
		endret.add(SAKSRELASJON_SAKID, oldSaksnummer, saksrelasjon.getSakId());
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
