package no.nav.dokarkiv.logiskslettdokument.common;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.exceptions.BegrensningIkkeFunnetException;
import org.slf4j.MDC;

import java.util.HashSet;
import java.util.Set;

public class SlettemeldingsFunksjoner {

	public static DokumentInfo setDokumentLogiskSlettet(Journalpost journalpost, DokumentInfo dokumentInfo, TilknyttetJournalpostSomCode tilknytning) {
		if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(tilknytning)) {
			Begrensning begrensning = Begrensning.builder().begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).journalpost(journalpost).build();
			begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
			dokumentInfo.addBegrensning(begrensning);
		} else {
			Begrensning begrensning = Begrensning.builder().begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT).dokumentInfo(dokumentInfo).build();
			begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
			dokumentInfo.addBegrensning(begrensning);
		}
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		return dokumentInfo;
	}

	public static DokumentInfo setAngreDokumentLogiskSlettet(Journalpost journalpost, DokumentInfo dokumentInfo, TilknyttetJournalpostSomCode tilknytning) {
		if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(tilknytning)) {
			Begrensning begrensning = journalpost.getBegrensningnerByJournalpostIdOnly(journalpost, BegrensningTypeCode.UTILGJENGELIGGJORT);
			if (begrensning == null) {
				throw new BegrensningIkkeFunnetException(String.format("Fant ikke forventet begrensning for dokumentInfoId %s og begreningstype %s.", dokumentInfo.getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT.name()));
			} else {
				Set<Begrensning> begrensninger = new HashSet<Begrensning>();
				begrensninger.add(begrensning);
				dokumentInfo.removeBegrensninger(begrensninger);
			}
		} else {
			Begrensning begrensning = dokumentInfo.getBegrensningnerByDokumentInfoAndJournalpost(dokumentInfo, BegrensningTypeCode.UTILGJENGELIGGJORT, journalpost);
			if (begrensning == null) {
				throw new BegrensningIkkeFunnetException(String.format("Fant ikke forventet begrensning for journalpostId %s, dokumentInfoId %s og begreningstype %s.", journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT.name()));
			} else {
				Set<Begrensning> begrensninger = new HashSet<Begrensning>();
				begrensninger.add(begrensning);
				dokumentInfo.removeBegrensninger(begrensninger);
			}
		}
		dokumentInfo.setEndretAvNavn(MDC.get(MDCConstants.MDC_USER_NAME));
		return dokumentInfo;
	}
}
