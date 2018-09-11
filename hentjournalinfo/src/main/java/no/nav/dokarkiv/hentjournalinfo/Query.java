package no.nav.dokarkiv.hentjournalinfo;

import static java.lang.String.format;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class Query implements GraphQLQueryResolver {

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Inject
    private JoarkRepository joarkRepository;

    @Inject
    private DokumentFilRepository dokumentFilRepository;

    @Inject
    private AbacSecurityService abacSecurityService;

    @Transactional
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark101g"}, percentiles = {0.5, 0.95})
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public DokumentInfo dokumentInfo(Long dokumentInfoId) {
        DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentInfoId)
                .orElseThrow(() -> new DokarkivFunctionalException(format("Fant ingen dokumentInfo med id=%s i databasen", dokumentInfoId)));

        //FIXME: Abac?
//        abacSecurityService.assertAccessToJournalpost(String.valueOf(dokumentInfo.getOriginalJournalpost().getJournalpostId()));
        return dokumentInfo;
    }

    @Transactional
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark102g"}, percentiles = {0.5, 0.95})
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public Journalpost journalpost(Long journalpostId) {
        abacSecurityService.assertAccessToJournalpost(String.valueOf(journalpostId));
        return joarkRepository.findById(journalpostId)
                .orElseThrow(() -> new DokarkivFunctionalException((format("Fant ingen journalpost med id=%s i databasen", journalpostId))));
    }

    @Transactional
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark103g"}, percentiles = {0.5, 0.95})
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public String fil(Long dokumentInfoId, String filtype) {

        //FIXME: Abac?
//        abacSecurityService.assertAccessToJournalpost(String.valueOf(dokumentInfo.getOriginalJournalpost().getJournalpostId()));


        List<FilDetaljer> fildetaljerListe = new ArrayList<>(dokumentinfoRepository.findById(dokumentInfoId)
                .orElse(new DokumentInfo())
                .getFildetaljerListe());
        FilDetaljer filDetaljer = fildetaljerListe.stream()
                .filter(detaljer -> detaljer.getFiltype() == FilTypeCode.valueOf(filtype))
                .findAny()
                .orElse(null);

        if (filDetaljer != null) {
            String filUuid = filDetaljer.getFilUuid();
            DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
            if (dokumentFil == null) {
                throw new DokarkivTechnicalException(format("Finner ikke fil med filUuid=%s i databasen", filUuid));
            }
            return new String(dokumentFil.getFil(), StandardCharsets.UTF_8);
        }

        throw new DokarkivFunctionalException(format("Fant ingen fil med dokumentInfoId=%s og filtype=%s", dokumentInfoId, filtype));
    }
}
