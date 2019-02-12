package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggTOMapper {

	public AksjonsLoggTO mapAksjonsLoggHeader(String aksjonsLoggHeaderString, AksjonsTypeCode aksjon, Long journalpostId, Long dokumentInfoId) throws UgyldigAksjonsLoggException {

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode aksjonsLoggHeader = objectMapper.readTree(aksjonsLoggHeaderString);
			return AksjonsLoggTO.builder()
					.aksjon(aksjon)
					.journalpostId(journalpostId)
					.dokumentInfoId(dokumentInfoId)
					.melding(getValueFromJsonNode(aksjonsLoggHeader, "melding"))
					.bruker(getValueFromJsonNode(aksjonsLoggHeader, "bruker"))
					.hjemmel(getValueFromJsonNode(aksjonsLoggHeader, "hjemmel"))
					.utfoertAv(getValueFromJsonNode(aksjonsLoggHeader, "utfoertAv"))
					.build();

		} catch (IOException e) {
			throw new UgyldigAksjonsLoggException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", AKSJONS_LOGG_HEADER), e);
		}

	}

	private String getValueFromJsonNode(JsonNode jsonNode, String fieldName) {
		JsonNode value = Objects.isNull(jsonNode.get(0))?jsonNode.get(fieldName):jsonNode.get(0).get(fieldName);
		if (value==null || "null".equals(value.asText())) {
			return null;
		}

		return value.asText();
	}
}
