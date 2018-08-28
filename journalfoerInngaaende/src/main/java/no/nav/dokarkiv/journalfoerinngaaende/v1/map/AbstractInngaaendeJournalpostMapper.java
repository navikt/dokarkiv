package no.nav.dokarkiv.journalfoerinngaaende.v1.map;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

public abstract class AbstractInngaaendeJournalpostMapper {

	protected String mapFagsystemCodeToArkivSakSystem(FagsystemCode fagsystemCode) {
		if (fagsystemCode.equals(FagsystemCode.FS22)) {
			return AbstractInngaaendeJournalpostMapper.ArkivsystemKode.GSAK.name();
		} else if (fagsystemCode.equals(FagsystemCode.PEN)) {
			return AbstractInngaaendeJournalpostMapper.ArkivsystemKode.PSAK.name();
		} else {
			return fagsystemCode.name();
		}
	}

	private enum ArkivsystemKode {
		GSAK,
		PSAK
	}

	protected FagsystemCode mapArkivSakSystemToFagsystemCode(String arkivSakSystem){
		if (AbstractInngaaendeJournalpostMapper.ArkivsystemKode.GSAK.name().equals(arkivSakSystem)){
			return FagsystemCode.FS22;
		} else {
			return FagsystemCode.PEN;
		}
	}
}
