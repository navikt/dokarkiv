package no.nav.dokarkiv.core.aksjonslogg;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
class AksjonsLoggValidator {

	public void validateArkivElementToList(List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {
		for (ArkivElementEndringTO arkivElementEndringTo : arkivElementEndringTOList) {
			assertNullOrEmpty(arkivElementEndringTo.getArkivElement(), "AksjonsLogg.ArkivElementEndring mangler påkrevd parameter: arkivElement");

			if (Objects.isNull(arkivElementEndringTo.getFraVerdi()) && Objects.isNull(arkivElementEndringTo.getTilVerdi())) {
				throw new UgyldigAksjonsLoggException("Ugyldig AksjonsLogg.ArkivElementEndring: enten fraVerdi eller tilVerdi må bli satt");
			}
		}
	}

	public void validateAksjonslogg(AksjonsLoggTO aksjonsLoggTO) throws UgyldigAksjonsLoggException {

		assertNullOrEmpty(aksjonsLoggTO.getAksjon(), "AksjonsLogg mangler påkrevd parameter: aksjon");
		assertNullOrEmpty(aksjonsLoggTO.getBruker(), "AksjonsLogg mangler påkrevd parameter: bruker");

		String userId = RequestContextHolder.currentRequestContext().getUserId();
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();

		if (Objects.nonNull(componentId) && componentId.equals(userId) || isBlank(userId)) {
			assertNullOrEmpty(aksjonsLoggTO.getUtfoertAv(), "AksjonsLogg mangler påkrevd parameter: utfoertAv. AksjonsLogg input må inneholde parameteren \"utfoertAv\" hvis kallet ikke inneholder sikkerhetstoken for saksbehandleren");
		}

		if (Objects.isNull(aksjonsLoggTO.getJournalpostId()) && Objects.isNull(aksjonsLoggTO.getDokumentInfoId())) {
			throw new UgyldigAksjonsLoggException("AksjonsLogg mangler påkrevd parameter: enten journalpostId eller dokumentInfoId må bli satt.");
		}
	}

	public void assertNullOrEmpty(Object value, String feilmelding) throws UgyldigAksjonsLoggException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigAksjonsLoggException(feilmelding);
		}
	}
}
