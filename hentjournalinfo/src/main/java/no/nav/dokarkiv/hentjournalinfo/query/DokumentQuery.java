package no.nav.dokarkiv.hentjournalinfo.query;

import static java.lang.String.format;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.DOKUMENT;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentIkkeFunnetException;
import no.nav.freg.abac.core.annotation.Abac;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementasjon av query for henting av dokument.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
//@Component //TODO: Implementasjon av dette er skrudd av helt til det er klart om hvordan det skal brukes. Legg til @Component annotering for å skru det på igjen
@Slf4j
public class DokumentQuery implements Query {

    private final DokumentinfoRepository dokumentinfoRepository;

    private final DokumentFilRepository dokumentFilRepository;

    private final AbacSecurityService abacSecurityService;

    @Inject
    public DokumentQuery(DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, AbacSecurityService abacSecurityService, JoarkRepository joarkRepository) {
        this.dokumentinfoRepository = dokumentinfoRepository;
        this.dokumentFilRepository = dokumentFilRepository;
        this.abacSecurityService = abacSecurityService;
    }

    @GraphQLQuery(name = DOKUMENT, description = "Selve dokumentet i PDF/PDFA format")
    @Transactional(readOnly = true)
    @RestMetrics(value = "dok_graphql_request", extraTags = {"query", DOKUMENT}, percentiles = {0.5, 0.95}, logException = false)
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public byte[] hentDokument(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId, @GraphQLArgument(name = "journalpostId") @GraphQLNonNull Long journalpostId, @GraphQLArgument(name = "filtype") FilTypeCode filType) {
        log.info(format("GraphQL har mottatt %s query med dokumentInfoId=%s, journalpostId=%s", DOKUMENT, dokumentInfoId, journalpostId));
        abacSecurityService.assertAccessToDokument(dokumentInfoId);

        DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentInfoId)
                .orElse(new no.nav.dokarkiv.core.domain.entities.DokumentInfo());

        if (BooleanUtils.isTrue(dokumentInfo.getSlettet())) {
            throw new DokumentIkkeFunnetException(format("Dokument med dokumentInfoId=%s er satt som logisk slettet og kan derfor ikke hentes", dokumentInfoId));
        }

        List<FilDetaljer> fildetaljerListe = new ArrayList<>(dokumentInfo.getFildetaljerListe());
        FilDetaljer filDetaljer = fildetaljerListe.stream()
                .filter(detaljer -> detaljer.getFiltype() == FilTypeCode.PDFA || detaljer.getFiltype() == FilTypeCode.PDF)
                .findAny()
                .orElse(null);

        //TODO: Skal det hentes noe annet enn PDFA/PDF?

        if (filDetaljer != null) {
            String filUuid = filDetaljer.getFilUuid();
            DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
            if (dokumentFil == null) {
                throw new DokarkivTechnicalException(format("Finner ikke fil med filUuid=%s i databasen", filUuid));
            }
            return dokumentFil.getFil();
        }

        throw new DokarkivFunctionalException(format("Fant ingen fil med dokumentInfoId=%s og filtype=%s", dokumentInfoId, "PDF eller PDFA"));
    }


}
