package no.nav.dokarkiv.core.aksjonslogg;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface AksjonsLoggService {

	String AKSJONS_LOGG_HEADER = "dok_aksjonslogg";
	String AKSJONS_LOGG_HJEMMEL_HEADER = "dok_aksjonslogg_hjemmel";
	String AKSJONS_LOGG_BRUKER_HEADER = "dok_aksjonslogg_bruker";
	String AKSJONS_LOGG_UTFOERT_AV_HEADER = "dok_aksjonslogg_utfoert_av";
	String AKSJONS_LOGG_MELDING_HEADER = "dok_aksjonslogg_melding";

	void validateAndSaveAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList);

}
