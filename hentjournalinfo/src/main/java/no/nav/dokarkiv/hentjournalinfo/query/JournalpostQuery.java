package no.nav.dokarkiv.hentjournalinfo.query;

import static java.lang.String.format;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.hentjournalinfo.map.DokumentInfoMapper.mapDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.map.JournalpostMapper.mapJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.JOURNALPOST;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class JournalpostQuery implements Query {

    private final JoarkRepository joarkRepository;
    private final AbacSecurityService abacSecurityService;

    @Inject
    public JournalpostQuery(JoarkRepository joarkRepository, AbacSecurityService abacSecurityService) {
        this.joarkRepository = joarkRepository;
        this.abacSecurityService = abacSecurityService;
    }

    @GraphQLQuery(name = JOURNALPOST)
    @Transactional(readOnly = true)
    @RestMetrics(value = "dok_graphql_request", extraTags = {"query", JOURNALPOST}, percentiles = {0.5, 0.95}, logException = false)
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public Journalpost journalpost(@GraphQLArgument(name = "journalpostId") @GraphQLNonNull Long journalpostId) {
        log.info(format("GraphQL har mottatt %s query med journalpostId=%s", JOURNALPOST, journalpostId));
        abacSecurityService.assertAccessToJournalpost(journalpostId.toString());
        no.nav.dokarkiv.core.domain.entities.Journalpost journalpost = joarkRepository.findById(journalpostId).get();
        return mapJournalpost(journalpost);
    }

    @GraphQLQuery(name = "brukere")
    @Transactional(readOnly = true)
    public List<Journalpost.Bruker> brukere(@GraphQLContext Journalpost journalpost) {
        Set<Bruker> brukere = joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new no.nav.dokarkiv.core.domain.entities.Journalpost())
                .getBrukere();
        return brukere.stream().map(bruker -> Journalpost.Bruker.builder()
                .brukerId(bruker.getBrukerId())
                .brukerType(bruker.getBrukerType() == null ? null : bruker.getBrukerType().name())
                .build()).collect(Collectors.toList());

    }

    @GraphQLQuery(name = "knyttetDokumentList")
    @Transactional(readOnly = true)
    public List<JournalpostDokumentRelasjon> knyttetDokumentList(@GraphQLContext Journalpost journalpost) {
        Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new no.nav.dokarkiv.core.domain.entities.Journalpost())
                .getJournalpostDokumentInfoRelasjoner();


        return journalpostDokumentInfoRelasjons.stream()
                .filter(relasjon -> isNotTrue(relasjon.getDokumentInfo().getSlettet()))
                .map(relasjon -> JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon.getTilknyttetJournalpostSom()
                                .name())
                        .journalpostId(journalpost.getJournalpostId())
                        .dokumentInfo(mapDokumentInfo(relasjon.getDokumentInfo())) //Like greit å bare mappe dokumentinfo når den må hentes opp fra DB for å hente dokumentInfoId (ref: LazyFetching)
                        .dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId()).build())
                .collect(Collectors.toList());

    }
}
