package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringBadMetadataException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.validators.MottaDokumentUtgaaendeSkanningValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;

@Service
@Slf4j
public class MottaDokumentUtgaaendeSkanningService {

	private final JournalpostRepository journalpostRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final MottaDokumentUtgaaendeSkanningValidator validator = new MottaDokumentUtgaaendeSkanningValidator();

	private final String KILDENAVN = "skanmotutgaaende";
	private final String JOURNALPOST = "journalpost";
	private final String REQUEST = "request";

	public MottaDokumentUtgaaendeSkanningService(JournalpostRepository journalpostRepository, DokumentFilRepository dokumentFilRepository) {
		this.journalpostRepository = journalpostRepository;
		this.dokumentFilRepository = dokumentFilRepository;
	}

	public void mottaDokumentUtgaaendeSkanning(Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request) throws DokarkivFunctionalException, DokarkivTechnicalException {
		try {
			validateRequest(journalpostId, request);

			Journalpost journalpost = journalpostRepository.fetchByIdWithJournalpostDokumentInfoRelasjoner(journalpostId)
					.orElseThrow(() -> new JournalpostIkkeFunnetException(get(MDC_REQUEST_ID) + " journalpostId=" + journalpostId + " ikke funnet i databasen"));
			validateJournalpost(journalpostId, request, journalpost);

			journalpost.setJournalstatus(JournalStatusCode.FL);

			Map<String, String> tilleggsopplysninger = journalpost.getTilleggsopplysninger();
			request.getTilleggsopplysninger().forEach(tilleggsopplysning -> tilleggsopplysninger.put(tilleggsopplysning.getNokkel(), tilleggsopplysning.getVerdi()));

			journalpost.setMottakskanal(MottaksKanalCode.valueOf(request.getMottakskanal()));

			journalpost.setEndretKildeNavn(KILDENAVN);
			if (request.getDatoMottatt() != null) {
				journalpost.setMottattDato(request.getDatoMottatt());
			}
			journalpost.setJournalDato(DateProvider.getToday());

			if (request.getEksternReferanseId() != null) {
				journalpost.setKanalReferanseId(request.getEksternReferanseId());
			}

			List<FilDetaljer> filDetaljerList = request.getDokumentvarianter()
					.stream()
					.map(dokumentVariant -> mapDokumentVariantToFildetaljer(dokumentVariant, request.getBatchnavn()))
					.toList();

			DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
			filDetaljerList.forEach(filDetaljer -> {
				dokumentInfo.addFilDetaljer(filDetaljer);
				DokumentFil dokumentFil = filDetaljer.createDokumentFil();
				dokumentFilRepository.persist(dokumentFil);
			});
		} catch (Exception e) {
			if (!(e instanceof DokarkivFunctionalException || e instanceof DokarkivTechnicalException)) {
				log.error("{} mottaDokumentUtgaaendeSkanning feilet med ukjent feil på journalpost. journalpostId={}, mottakskanal={}, batchnavn={}, feilmelding={}",
						get(MDC_REQUEST_ID), journalpostId, request.getMottakskanal(), request.getBatchnavn(), e.getMessage(), e);
			}
			throw e;
		}
	}

	private void validateRequest(Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request) throws InputValideringFeiletException {
		validator.validateRequest(request).ifPresent(errors -> {
			throw new InputValideringFeiletException(generateErrorMessage(errors, journalpostId, request, REQUEST));
		});
	}

	private void validateJournalpost(Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request, Journalpost journalpost) throws DokarkivFunctionalException {
		validator.validateJournalpostHasAllElements(journalpost).ifPresent(errors -> {
			throw new InputValideringFeiletException(generateErrorMessage(errors, journalpostId, request, JOURNALPOST));
		});

		validator.validateJournalpostMetadata(journalpost).ifPresent(errors -> {
			throw new InputValideringBadMetadataException(generateErrorMessage(errors, journalpostId, request, JOURNALPOST));
		});
	}

	private String generateErrorMessage(String errors, Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request, String valideringAv) {
		return String.format("%s feilet ved validering av %s. journalpostId=%d, mottakskanal=%s, batchnavn=%s, feilmelding=%s",
				get(MDC_REQUEST_ID), valideringAv, journalpostId, request.getMottakskanal(), request.getBatchnavn(), errors);
	}

	private FilDetaljer mapDokumentVariantToFildetaljer(DokumentVariant dokumentVariant, String batchnavn) {
		FilDetaljer filDetaljer = FilDetaljer
				.builder()
				.filtype(FilTypeCode.valueOf(dokumentVariant.getFiltype()))
				.filnavn(dokumentVariant.getFilnavn())
				.variantFormat(VariantFormatCode.valueOf(dokumentVariant.getVariantformat()))
				.fileContent(dokumentVariant.getFysiskDokument())
				.batchNavn(batchnavn)
				.filUuid(FilDetaljer.generateUuid())
				.build();
		filDetaljer.setOpprettetKildeNavn(KILDENAVN);
		return filDetaljer;
	}

}
