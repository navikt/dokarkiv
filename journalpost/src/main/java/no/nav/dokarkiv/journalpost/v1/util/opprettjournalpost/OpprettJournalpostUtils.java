package no.nav.dokarkiv.journalpost.v1.util.opprettjournalpost;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorResponse;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorResponseToGrafana;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.Specification.NO_STANDARD;

@Component
@Slf4j
public class OpprettJournalpostUtils {

	private final MeterRegistry meterRegistry;

	@Inject
	public OpprettJournalpostUtils(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}


	public void validateAndLogPDFA(Journalpost journalpost) {
		List<PDFAValidatorResponseToGrafana> responses = journalpost.findAllFilDetaljer().stream().
				map(fil -> safeValidateDokumentFil(fil.createDokumentFil(), fil))
				.filter(result -> result.isPresent())
				.map(Optional::get)
				.collect(Collectors.toList());

		incrementMetrics(responses, journalpost);

		for (PDFAValidatorResponseToGrafana response : responses) {
			log.info("Dokument {} tilhørende journalpost={} fra {} er en {} PDF/A på format {}. Eventuelle feilmeldinger:{}",
					response.getFilUuid(), journalpost.getJournalpostId(),
					journalpost.getOpprettetAvNavn(), response.validPdfToString(),
					response.getPdfVersion(), response.getAssertionResults());
		}
	}


	private void incrementMetrics(List<PDFAValidatorResponseToGrafana> validationResults, Journalpost journalpost){
		for(PDFAValidatorResponseToGrafana result : validationResults){
			Optional<TilknyttetJournalpostSomCode> tilknyttetSom = journalpost.findTilknyttetSomByDokumentinfoId(result.getDokumentinfoId());
			String tilknyttetSomString = tilknyttetSom.isPresent() ? tilknyttetSom.get().toString() : "UKJENT_RELASJON";
			initOpprettJournalpostValidationCounter(meterRegistry, result, journalpost, tilknyttetSomString);
		}
	}

	public Optional<PDFAValidatorResponseToGrafana> safeValidateDokumentFil(DokumentFil dokumentFil, FilDetaljer filDetaljer){
		try {
			PDFAValidatorResponse response = PDFAValidatorUtil.validatePDFA(dokumentFil);
			return Optional.of(new PDFAValidatorResponseToGrafana(response, filDetaljer));
		}catch(Exception e){
			log.warn("Kunne ikke validere dokumentfil", e);
			return Optional.empty();
		}
	}

	//Denne må nok tunes litt på når jeg starter på grafana
	private void initOpprettJournalpostValidationCounter(MeterRegistry meterRegistry, PDFAValidatorResponseToGrafana validationResult, Journalpost journalpost, String tilknyttetSom) {

		//registrer hvor mange PDF/A'er som faktisk er pdfa (konform eller ikke)
		Counter.builder("dok_faktisk_PDFA")
				.tag("faktiskPDFA", validationResult.getPdfVersion().equals(NO_STANDARD) ? "Ikke_PDFA" : "PDF/A")
				.register(meterRegistry).increment();

		//counter for gyldige pdfa by arkiverer
		Counter.builder("dok_gyldig_PDFA_tema")
				.tag("arkiverer", journalpost.getOpprettetAvNavn() == null ? "ukjent" : journalpost.getOpprettetAvNavn())
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
}
