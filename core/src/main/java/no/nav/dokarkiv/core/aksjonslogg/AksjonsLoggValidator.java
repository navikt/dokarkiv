package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.slf4j.MDC;

import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
class AksjonsLoggValidator {

	public void validateArkivElementToList(List<ArkivElementEndringTO> arkivElementEndringTOList) {
		for (ArkivElementEndringTO arkivElementEndringTo : arkivElementEndringTOList) {
			validateNotNullOrEmpty(arkivElementEndringTo.getArkivElement(), "AksjonsLogg.ArkivElementEndring mangler påkrevd parameter: arkivElement");

			if (Objects.isNull(arkivElementEndringTo.getFraVerdi()) && Objects.isNull(arkivElementEndringTo.getTilVerdi())) {
				throw new UgyldigAksjonsLoggException("Ugyldig AksjonsLogg.ArkivElementEndring: enten fraVerdi eller tilVerdi må bli satt");
			}
		}
	}

	public void validateAksjonslogg(AksjonsLoggTO aksjonsLoggTO) {
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();
		String userId = MDC.get(MDC_USER_ID);

		validateNotNullOrEmpty(componentId, "AksjonsLogg mangler påkrevd parameter: Applikasjon");

		if (isBlank(aksjonsLoggTO.getUtfoertAv()) && isBlank(userId)) {
			validateNotNullOrEmpty(aksjonsLoggTO.getUtfoertAv(), "AksjonsLogg mangler påkrevd parameter: utfoertAv. AksjonsLogg input må inneholde parameteren \"utfoertAv\" hvis kallet ikke inneholder sikkerhetstoken for saksbehandleren");
		}

		validateNotNullOrEmpty(aksjonsLoggTO.getAksjon(), "AksjonsLogg mangler påkrevd parameter: aksjon");

		if (Objects.isNull(aksjonsLoggTO.getJournalpostId()) && Objects.isNull(aksjonsLoggTO.getDokumentInfoId())) {
			throw new UgyldigAksjonsLoggException("AksjonsLogg mangler påkrevd parameter: enten journalpostId eller dokumentInfoId må bli satt.");
		}
	}

	private static void validateNotNullOrEmpty(Object value, String feilmelding) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigAksjonsLoggException(feilmelding);
		}
	}
}
