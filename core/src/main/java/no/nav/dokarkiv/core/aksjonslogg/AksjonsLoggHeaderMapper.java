package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObject;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggInfoException;

import java.io.IOException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggHeaderMapper {

	public AksjonsLoggHeader mapAksjonsLoggHeader(String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggInfoException {

		try {
			return jsonStringToObject(aksjonsLoggHeaderString, AksjonsLoggHeader.class);
		} catch (IOException e) {
			throw new UgyldigAksjonsLoggInfoException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", AKSJONS_LOGG_HEADER), e);
		}

	}
}
