package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.validators.MottaDokumentUtgaaendeSkanningValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static org.slf4j.MDC.get;

@Service
@Slf4j
public class MottaDokumentUtgaaendeSkanningService {

    private final JoarkRepository joarkRepository;
    private final DokumentFilRepository dokumentFilRepository;
    private final MottaDokumentUtgaaendeSkanningValidator validator = new MottaDokumentUtgaaendeSkanningValidator();

    private final String KILDENAVN = "skanmot_1408";
    private final String MOTTATTI = "mottatti";
    private final String MOTTATTFRA = "mottattfra";
    private final String ENDORSERNR = "endorsernr";

    public MottaDokumentUtgaaendeSkanningService(JoarkRepository joarkRepository, DokumentFilRepository dokumentFilRepository) {
        this.joarkRepository = joarkRepository;
        this.dokumentFilRepository = dokumentFilRepository;
    }

    public void mottaDokumentUtgaaendeSkanning (Long journalpostId, MottaDokumentUtgaaendeSkanningRequest request) {
        try{
            Optional<String> requestValidationErrors = validator.validateRequest(request);
            if(requestValidationErrors.isPresent()) {
                InputValideringFeiletException inputValideringFeiletException = new InputValideringFeiletException(requestValidationErrors.get());
                log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning kunne ikke validere request: ", inputValideringFeiletException);
                throw inputValideringFeiletException;
            }

            Optional<Journalpost> optionalJournalpost = joarkRepository.findById(journalpostId);
            if(optionalJournalpost.isEmpty()) {
                JournalpostIkkeFunnetException journalpostIkkeFunnetException = new JournalpostIkkeFunnetException("journalpost med id " + journalpostId + " ikke funnet");
                log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning kunne ikke finne journalpost", journalpostIkkeFunnetException);
                throw journalpostIkkeFunnetException;
            }

            Journalpost journalpost = optionalJournalpost.get();

            Optional<String> journalpostValidationErrors = validator.validateJournalpost(journalpost);
            if(journalpostValidationErrors.isPresent()) {
                InputValideringFeiletException inputValideringFeiletException = new InputValideringFeiletException(journalpostValidationErrors.get());
                log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning kunne ikke validere journalpost:", inputValideringFeiletException);
                throw inputValideringFeiletException;
            }

            journalpost.setJournalstatus(JournalStatusCode.FL);
            if(!isNullOrEmpty(request.getMottatti())) {
                journalpost.getTilleggsopplysninger().put(MOTTATTI, request.getMottatti());
            }
            if(!isNullOrEmpty(request.getMottattfra())) {
                journalpost.getTilleggsopplysninger().put(MOTTATTFRA, request.getMottattfra());
            }
            if(!isNullOrEmpty(request.getEndorsernr())) {
                journalpost.getTilleggsopplysninger().put(ENDORSERNR, request.getEndorsernr());
            }
            journalpost.setMottakskanal(MottaksKanalCode.SKAN_NETS);

            //TODO disse er null i q1/2, kan ikke sjekke hva det er i prod
            //journalpost.setUtsendingskanal(UtsendingsKanalCode.?);
            //journalpost.setKanalReferanseId(?);

            journalpost.setEndretKildeNavn(KILDENAVN);
            if(request.getDatoMottatt() != null) {
                journalpost.setMottattDato(request.getDatoMottatt());
            }
            List<FilDetaljer> filDetaljerList = request.getDokumentvarianter()
                    .stream()
                    .map(dokumentVariant -> mapDokumentVariantToFildetaljer(dokumentVariant, request.getBatchnavn()))
                    .collect(Collectors.toList());

            filDetaljerList.stream().forEach(filDetaljer -> {
                DokumentFil dokumentFil = filDetaljer.createDokumentFil();
                dokumentFilRepository.save(dokumentFil);
            });
            filDetaljerList.forEach(filDetaljer -> journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().addFilDetaljer(filDetaljer));

        } catch(DokarkivFunctionalException e) {
            log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning feilet funksjonelt", e);
            throw e;
        } catch(DokarkivTechnicalException e) {
            log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning feilet teknisk", e);
            throw e;
        } catch(Exception e) {
            log.error(get(MDC_REQUEST_ID) + " mottaDokumentUtgaaendeSkanning feilet med ukjent feil", e);
            throw e;
        }
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

    private boolean isNullOrEmpty(String string){
        return string == null || string.isBlank();
    }
}
