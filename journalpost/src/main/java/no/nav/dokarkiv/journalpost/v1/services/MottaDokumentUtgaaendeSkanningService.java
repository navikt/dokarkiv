package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringBadMetadataException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.validators.MottaDokumentUtgaaendeSkanningValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;

@Service
@Slf4j
public class MottaDokumentUtgaaendeSkanningService {

    private final JoarkRepository joarkRepository;
    private final DokumentFilRepository dokumentFilRepository;
    private final MottaDokumentUtgaaendeSkanningValidator validator = new MottaDokumentUtgaaendeSkanningValidator();

    private final String KILDENAVN = "skanmotutgaaende";
    private final String JOURNALPOST = "journalpost";
    private final String REQUEST = "request";

    public MottaDokumentUtgaaendeSkanningService(JoarkRepository joarkRepository, DokumentFilRepository dokumentFilRepository) {
        this.joarkRepository = joarkRepository;
        this.dokumentFilRepository = dokumentFilRepository;
    }

    public void mottaDokumentUtgaaendeSkanning(Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request) throws DokarkivFunctionalException, DokarkivTechnicalException {
        try {

            validateRequest(journalpostId, request);

            Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(() -> new JournalpostIkkeFunnetException(get(MDC_REQUEST_ID) + "\n" + "journalpost med id " + journalpostId + " ikke funnet"));
            validateJournalpost(journalpostId, request, journalpost);

            journalpost.setJournalstatus(JournalStatusCode.FL);

            request.getTilleggsopplysninger().forEach(tilleggsopplysning -> journalpost.getTilleggsopplysninger().put(tilleggsopplysning.getNokkel(), tilleggsopplysning.getVerdi()));

            journalpost.setMottakskanal(MottaksKanalCode.valueOf(request.getMottakskanal()));

            journalpost.setEndretKildeNavn(KILDENAVN);
            if (request.getDatoMottatt() != null) {
                journalpost.setMottattDato(request.getDatoMottatt());
            }
            journalpost.setJournalDato(DateProvider.getToday());

            log.info("Save1");
            List<FilDetaljer> filDetaljerList = request.getDokumentvarianter()
                    .stream()
                    .map(dokumentVariant -> mapDokumentVariantToFildetaljer(dokumentVariant, request.getBatchnavn()))
                    .collect(Collectors.toList());

            log.info("Save2");
            filDetaljerList.forEach(filDetaljer -> {
                log.info("lagrer");
                DokumentFil dokumentFil = filDetaljer.createDokumentFil();
                dokumentFilRepository.save(dokumentFil);
            });
            log.info("Save3");
            filDetaljerList.forEach(filDetaljer -> journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().addFilDetaljer(filDetaljer));

        } catch (Exception e) {
            if (!(e instanceof DokarkivFunctionalException || e instanceof DokarkivTechnicalException)) {
                log.error(
                        get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning feilet med ukjent feil på journalpost "
                                + "journalpostId=" + journalpostId + " "
                                + "mottakskanal=" + request.getMottakskanal() + " "
                                + "batchnavn=" + request.getBatchnavn() + " "
                                + "feilmedling=" + e.getMessage(), e
                );
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
        String errorMessage = String.format(get(MDC_REQUEST_ID) + " feilet ved validering av %s "
                + "journalpostId=" + journalpostId + " "
                + "mottakskanal=" + request.getMottakskanal() + " "
                + "batchnavn=" + request.getBatchnavn() + " "
                + "feilmedling=" + errors, valideringAv);
        return errorMessage;
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

    private boolean notNullOrEmpty(String string) {
        return string != null && !string.isBlank();
    }
}
