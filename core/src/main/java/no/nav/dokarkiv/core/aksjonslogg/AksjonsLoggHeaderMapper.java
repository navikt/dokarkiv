package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObject;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObjectList;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;

import java.io.IOException;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggHeaderMapper {

	public List<AksjonsLoggHeader> mapAksjonsLoggHeader(String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggHeaderException {

		try {
			return jsonStringToObjectList(aksjonsLoggHeaderString, AksjonsLoggHeader.class);
		} catch (IOException e) {
			throw new UgyldigAksjonsLoggHeaderException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", AKSJONS_LOGG_HEADER), e);
		}

	}
}
