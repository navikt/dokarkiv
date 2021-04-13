package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponseToGrafana;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static no.nav.dokarkiv.core.pdfValidation.PdfValidatorUtil.NOT_PDFA;

/**
 * Implementation of the OpprettJournalpostService
 *
 * @author Stig Strøm
 */
@Component
@Slf4j
public class DefaultOpprettJournalpostService implements OpprettJournalpostService {

	@Inject
    private JoarkRepositorySkjermet joarkRepository;
	@Inject
	private OpprettJournalpostValidator opprettJournalpostValidator;
	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;
	@Inject
	private MeterRegistry meterRegistry;

	@Override
	public OpprettJournalpostResponseTo opprettJournalpost(
			OpprettJournalpostRequestTo opprettJournalpostRequest) {
		validateRequest(opprettJournalpostRequest);

		Journalpost journalpost = opprettJournalpostRequest.getJournalpost();
		updateJournalpost(journalpost);
		opprettJournalpostValidator.validate(journalpost);
		List<PdfValidatorResponseToGrafana> responses = dokumentFilerDelegate.saveUpdateValidateDokumentFiler(journalpost);
		Journalpost storedJournalpost = joarkRepository.save(journalpost);
		count(meterRegistry, responses, journalpost);
		for(PdfValidatorResponseToGrafana response : responses) {
			log.info("Dokument {} tilhørende journalpost={} fra {} er en {} PDF/A på format {}",
					response.getId(), journalpost.getJournalpostId(),
					journalpost.getOpprettetAvNavn(), response.validPdfToString(),
					response.getPdfVersion());
		}
		return createResponse(storedJournalpost);
	}

	private void validateRequest(OpprettJournalpostRequestTo request) {
		if (request == null) {
			throw new ApplicationException("Missing parameter: request");
		}
		request.validate();
	}

	private OpprettJournalpostResponseTo createResponse(Journalpost journalpost) {
		return OpprettJournalpostResponseTo.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())
				.build();
	}

	private void updateJournalpost(Journalpost journalpost) {
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setJournalstatus(JournalStatusCode.D);
		journalpost.setJournalDato(DateProvider.getToday());
		journalpost.setJournalfortAvNavn(journalpost.getOpprettetAvNavn());
		JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hoveddokumentDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn());

		DokumentInfo dokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		dokumentInfo.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		dokumentInfo.setDokumentFerdigDato(DateProvider.getToday());
		dokumentInfo.setOriginalJournalpost(journalpost);
	}

	/*
antall dokumenter på ugyldig format
tema/fagområde
journalposttype (inngående, utgående, notat)
hvem (team/servicebruker) som arkiverer
Tenker vi kanskje kun bør samle statistikk på arkivvarianten (om det arkiveres flere), og kun de som har filtype "PDF" og "PDFA".
Hadde det gått an å lage grafana-board som også viser andel oppgitte PDFA som faktisk er pdfa, pluss det samme for oppgitte PDF?
Jeg tror også det er nyttig å skille på hoveddokument/vedlegg.
 */

	private static void count(MeterRegistry meterRegistry, List<PdfValidatorResponseToGrafana> validationResults, Journalpost journalpost){
		Set<JournalpostDokumentInfoRelasjon> relasjoner = journalpost.getJournalpostDokumentInfoRelasjoner();
		for(PdfValidatorResponseToGrafana validationresult: validationResults) {
		    for (JournalpostDokumentInfoRelasjon relasjon : relasjoner) {
				if (relasjon.getDokumentInfo().getId().toString() == validationresult.getId()){
					initOpprettJournalpostValidationCounter(meterRegistry, validationresult, journalpost, relasjon.getTilknyttetJournalpostSom().toString());
				}
			}
		}
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(journalpost.getOpprettetAvNavn()));
	}

	private static Counter initOpprettJournalpostValidationCounter(MeterRegistry meterRegistry, PdfValidatorResponseToGrafana validationResult, Journalpost journalpost, String tilknyttetSom) {
		//log.info()
		return Counter.builder("opprett_journalpost_PDFA_validering")
				.tag("tema", journalpost.getBehandlingstema() == null ? "ukjent" : journalpost.getBehandlingstema())
				.tag("journalposttype", journalpost.getJournalposttype() == null ? "ukjent" : journalpost.getJournalposttype().toString())
				.tag("arkiverer", journalpost.getOpprettetAvNavn() == null ? "ukjent" : journalpost.getOpprettetAvNavn())
				.tag("faktiskPDFA", validationResult.getPdfVersion() == NOT_PDFA ? NOT_PDFA : "PDF/A")
				.tag("gyldigPFA", validationResult.validPdfToString())
				.tag("arkivvariant", tilknyttetSom)
				.register(meterRegistry);
	}
}
