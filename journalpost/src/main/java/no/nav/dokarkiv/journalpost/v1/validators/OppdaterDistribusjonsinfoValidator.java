package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.KanIkkeOppdatereDistribusjonsinfoException;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterDistribusjonsinfoRequest;
import no.nav.dokarkiv.journalpost.v1.api.WithUtsendingsKanal;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo.JournalpostWithDistribusjonsinfo;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateBoolean;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class OppdaterDistribusjonsinfoValidator {

	private final Validator springSuppliedValidator;

	private static final EnumSet<JournalStatusCode> ALLOWED_STATES_FOR_DISTRIBUTION = EnumSet.of(FS, FL);

	public OppdaterDistribusjonsinfoValidator(Validator validator) {
		this.springSuppliedValidator = validator;
	}

	public static void validateRequest(String journalpostId, OppdaterDistribusjonsinfoRequest request) {
		validateId(journalpostId, "journalpostId");
		validateBoolean(request.getSettStatusEkspedert(), "settStatusEkspedert");

		if (isNotBlank(request.getUtsendingsKanal())) {
			try {
				UtsendingsKanalCode.valueOf(request.getUtsendingsKanal());
			} catch (IllegalArgumentException e) {
				throw new KanIkkeOppdatereDistribusjonsinfoException(
						String.format("Utsendingskanalkode '%s' er ugyldig", request.getUtsendingsKanal()));
			}
		}
	}

	public JournalpostResponse validateRequest(JournalpostWithDistribusjonsinfo request) {
		try {
			validateNotNull(request.getJournalpostId(), "journalpostId");
			validateNotNull(request.getForsendelseId(), "forsendelseId");
			validateBoolean(request.getSettStatusEkspedert(), "settStatusEkspedert");

			if (request.getSettStatusEkspedert()) {
				validateNotNull(request.getEkspedertDato(), "ekspedertDato", "må være satt når settStatusEkspedert=true");
			}
		} catch (DokarkivFunctionalException e) {
			return JournalpostResponse.error(request.getJournalpostId(), e.getMessage());
		}

		try {
			UtsendingsKanalCode utsendingsKanal = UtsendingsKanalCode.valueOf(request.getUtsendingsKanal());

			String valideringsfeil = switch (utsendingsKanal) {
				case S -> validerFeltOgInnhold("postadresse", "må være satt når utsendingsKanal=S (sentralprint)", request.getPostadresse());
				case SDP -> validerFeltOgInnhold("digitalpostkasse", "må være satt når utsendingsKanal=SDP (digital post)", request.getDigitalpostkasse());
				case NAV_NO -> validerFeltOgInnhold("varsel", "må være satt når utsendingsKanal=NAV_NO", request.getVarsel());
				default -> null;
			};
			if (valideringsfeil != null) {
				return JournalpostResponse.error(request.getJournalpostId(), valideringsfeil);
			}
			return JournalpostResponse.ok(request.getJournalpostId());
		} catch (NullPointerException | IllegalArgumentException enumParseException) {
			return JournalpostResponse.error(request.getJournalpostId(), String.format("Utsendingskanalkode '%s' er ugyldig", request.getUtsendingsKanal()));
		} catch (DokarkivFunctionalException e) {
			return JournalpostResponse.error(request.getJournalpostId(), e.getMessage());
		}
	}

	private <T> String validerFeltOgInnhold(String feltnavn, String ekstraInformasjon, T feltinnhold) {
		validateNotNull(feltinnhold, feltnavn, ekstraInformasjon);
		Set<ConstraintViolation<T>> validationErrors = springSuppliedValidator.validate(feltinnhold);
		if (validationErrors.size() > 0) {
			return "%s er ugyldig: %s".formatted(
					feltnavn,
					validationErrors.stream()
							.map(ConstraintViolation::getMessage)
							.collect(Collectors.joining(",")));
		}
		return null;
	}

	public static void validateJournalpostKanSetteStatusEkspedert(Journalpost journalpost, WithUtsendingsKanal request) {
		if (!JournalpostTypeCode.U.equals(journalpost.getJournalposttype())) {
			throw new KanIkkeOppdatereDistribusjonsinfoException(
					String.format("Kan ikke ekspedere journalpost med journalposttype=%s", journalpost.getJournalposttype()));
		}
		if (!ALLOWED_STATES_FOR_DISTRIBUTION.contains(journalpost.getJournalstatus())) {
			throw new KanIkkeOppdatereDistribusjonsinfoException(
					String.format("Kan ikke ekspedere journalpost med status %s", journalpost.getJournalstatus()));
		}
		if (journalpost.isFeilregistrert()) {
			throw new KanIkkeOppdatereDistribusjonsinfoException(
					"Kan ikke ekspedere journalpost med tom/feilregistrert saksrelasjon");
		}
		if (journalpost.getUtsendingskanal() == null && request.getUtsendingsKanal() == null) {
			throw new KanIkkeOppdatereDistribusjonsinfoException(
					String.format("Utsendingskanal er ikke satt, hverken på input eller på journalpost med journalposttype=%s", journalpost.getJournalposttype()));
		}
	}
}
