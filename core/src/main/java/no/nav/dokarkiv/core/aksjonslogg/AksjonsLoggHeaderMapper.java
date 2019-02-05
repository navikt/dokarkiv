package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObject;
import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObjectList;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggHeaderException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AksjonsLoggHeaderMapper {

	public List<AksjonsLoggHeader> mapAksjonsLoggHeader(String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggHeaderException {

		try {
			if (aksjonsLoggHeaderString.startsWith("[")) {
				return jsonStringToObjectList(aksjonsLoggHeaderString, AksjonsLoggHeader.class);
			} else {
				return Collections.singletonList(jsonStringToObject(aksjonsLoggHeaderString, AksjonsLoggHeader.class));
			}
		} catch (IOException e) {
			throw new UgyldigAksjonsLoggHeaderException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", AKSJONS_LOGG_HEADER), e);
		}

	}
}
