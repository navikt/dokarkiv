package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface AksjonsLoggService {

	String AKSJONS_LOGG_HEADER = "dok_aksjonslogg";

	void validateAndSaveAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException;

}
