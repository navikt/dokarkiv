package no.nav.dokarkiv.journalpost.v1.util.opprettjournalpost;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorResponse;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDFA;
import static org.verapdf.pdfa.flavours.PDFAFlavour.Specification.NO_STANDARD;

@Component
@Slf4j
public class OpprettJournalpostPDFAUtils {

	private final MeterRegistry meterRegistry;
	private static Pattern brukerPattern = Pattern.compile("[a-zA-Z]\\d{6}");

	@Inject
	public OpprettJournalpostPDFAUtils(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void safeValidateAndLogPDFA(Journalpost journalpost) {
		//denne tryen skal være veldig unødvendig, men beholder den inntil videre.
		//Det er ikke så farlig om det er et dokument vi ikke får validert, det er verre om opprettjournalpost slutter å funke..
		try {

			List<PDFAValidatorResponse> responses = journalpost.findAllFilDetaljer().stream()
					.filter(fildetaljer -> fildetaljer.getFiltype() == PDFA || fildetaljer.getFiltype() == PDF)
					.map(filDetaljer -> safeValidateDokumentFil(filDetaljer))
					.filter(result -> result.isPresent())
					.map(Optional::get)
					.collect(Collectors.toList());

			String arkivar = determineArkivar(MDC.get(MDC_CONSUMER_ID));

			incrementMetrics(responses, journalpost, arkivar);

			for (PDFAValidatorResponse response : responses) {
				log.info("Dokument {} tilhørende journalpost={} fra {} er en {} PDF/A på format {}. Eventuelle feilmeldinger:{}",
						response.getFilUuid(), journalpost.getJournalpostId(),
						arkivar, response.validPdfToString(),
						response.getPdfVersion(), response.getAssertionResults());
			}
		} catch (Exception e){
			log.warn("Feilet under PDF/A validering", e);
		}
	}


	private void incrementMetrics(List<PDFAValidatorResponse> validationResults, Journalpost journalpost, String userID) {
		for (PDFAValidatorResponse result : validationResults) {
			Optional<TilknyttetJournalpostSomCode> tilknyttetSom = journalpost.findTilknyttetSomByDokumentinfoId(result.getDokumentinfoId());
			String tilknyttetSomString = tilknyttetSom.isPresent() ? tilknyttetSom.get().toString() : "UKJENT_RELASJON";
			initOpprettJournalpostValidationCounter(meterRegistry, result, journalpost, tilknyttetSomString, userID);
		}
	}

	public Optional<PDFAValidatorResponse> safeValidateDokumentFil(FilDetaljer filDetaljer) {
		try {
			return Optional.of(PDFAValidatorUtil.validatePDFA(filDetaljer));
		} catch (Exception e) {
			log.warn("Kunne ikke validere dokumentfil", e);
			return Optional.empty();
		}
	}

	//Denne må nok tunes litt på når jeg starter på grafana
	private void initOpprettJournalpostValidationCounter(MeterRegistry meterRegistry, PDFAValidatorResponse validationResult,
														 Journalpost journalpost, String tilknyttetSom, String arkivar) {

		//registrer hvor mange PDF/A'er som faktisk er pdfa (konform eller ikke)
		Counter.builder("dok_faktisk_PDFA")
				.tag("faktiskPDFA", (NO_STANDARD.equals(validationResult.getPdfVersion()) || validationResult.getPdfVersion() == null) ? "Ikke_PDFA" : "PDF/A")
				.register(meterRegistry).increment();

		//counter for gyldige pdfa by arkivar
		Counter.builder("dok_gyldig_PDFA_arkiverer")
				.tag("arkiverer", arkivar)
				.tag("gyldigPDFA", validationResult.validPdfToString())
				.register(meterRegistry).increment();

		//counter for gyldig pdfa sortert på arkivvariant
		Counter.builder("dok_gyldig_PDFA_arkivvariant")
				.tag("gyldigPDFA", validationResult.validPdfToString())
				.tag("arkivvariant", tilknyttetSom)
				.register(meterRegistry).increment();

		//counter for gyldig pdfa sortert på journalposttype
		Counter.builder("dok_gyldig_PDFA_journalposttype")
				.tag("journalposttype", journalpost.getJournalposttype() == null ? "ukjent" : journalpost.getJournalposttype().toString())
				.tag("gyldigPDFA", validationResult.validPdfToString())
				.register(meterRegistry).increment();
	}


	//consumerID skal vel egentlig alltid være servicebruker, men just i case
	private String determineArkivar(String consumerId){
		return (consumerId == null || consumerId.length() == 0 || !isServiceuser(consumerId)) ? "Ukjent_servicebruker" : consumerId;
	}

	private boolean isServiceuser(String user) {
		return !brukerPattern.matcher(user).matches();
	}
}
