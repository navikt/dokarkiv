package no.nav.dokarkiv.core.aksjonslogg;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggInfoException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AksjonsLoggService {

	public static final String AKSJONS_LOGG_HEADER = "dok_aksjonslogg";

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggMapper aksjonsLoggMapper;

	public AksjonsLoggService(AksjonsLoggRepository aksjonsLoggRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggMapper = new AksjonsLoggMapper();
	}

	public void validateAndSaveAksjon(AksjonsLoggHeader aksjonsLoggHeader) throws UgyldigAksjonsLoggInfoException {

		for (AksjonsLoggHeader.Aksjon aksjon: aksjonsLoggHeader.getAksjonListe()) {
			validateAndSaveAksjon(
					aksjon.getJournalpostId(),
					aksjon.getApplikasjon(),
					aksjon.getAksjon(),
					aksjon.getUtfoertAv(),
					aksjon.getDokumentInfoId(),
					aksjon.getHjemmel(),
					aksjon.getBruker(),
					aksjon.getArkivElement(),
					aksjon.getFraVerdi(),
					aksjon.getTilVerdi(),
					aksjon.getMelding()
			);
		}
	}

	public void validateAndSaveAksjon(@NotNull Long journalpostId,
									  @NotNull String applikasjon,
									  @NotNull String aksjon,
									  @NotNull String utfoertAv,
									  Long dokumentInfoId,
									  String hjemmel,
									  String bruker,
									  String arkivElement,
									  String fraVerdi,
									  String tilVerdi,
									  String melding) throws UgyldigAksjonsLoggInfoException {

		validateAksjonslogg(journalpostId, applikasjon, aksjon, utfoertAv);

		AksjonsLogg aksjonsLogg = aksjonsLoggMapper.mapToAksjonsLogg(
				journalpostId, applikasjon, aksjon, utfoertAv, dokumentInfoId, hjemmel, bruker, arkivElement, fraVerdi, tilVerdi, melding);

		aksjonsLoggRepository.save(aksjonsLogg);
	}

	private void validateAksjonslogg(Long journalpostId,
									 String applikasjon,
									 String aksjon,
									 String utfoertAv) throws UgyldigAksjonsLoggInfoException {

			assertNullOrEmpty(journalpostId, "journalpostId");
			assertNullOrEmpty(applikasjon, "applikasjon");
			assertNullOrEmpty(aksjon, "aksjon");
			assertNullOrEmpty(utfoertAv, "utfoertAv");

			assertInvalidEnum(aksjon, "aksjon", AksjonTypeCode.values());

	}

	private void assertNullOrEmpty(Object value, String parameter) throws UgyldigAksjonsLoggInfoException {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigAksjonsLoggInfoException("AksjonsLogg mangler påkrevd parameter: " + parameter);
		}
	}

	private void assertInvalidEnum(Object value, String parameter, Enum[] allowedValues) throws UgyldigAksjonsLoggInfoException {
		boolean invalid = true;
		for (Enum e : allowedValues) {
			if (e.name().equals(value)) {
				invalid = false;
				break;
			}
		}

		if (invalid) {
			throw new UgyldigAksjonsLoggInfoException(String.format("AksjonsLogg inneholder ugyldig verdi: %s er ikke en gyldig verdi for %s", value, parameter));
		}
	}
}
