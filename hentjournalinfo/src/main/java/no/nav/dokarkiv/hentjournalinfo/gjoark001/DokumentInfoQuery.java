package no.nav.dokarkiv.hentjournalinfo.gjoark001;

import static java.lang.String.format;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;
import static no.nav.dokarkiv.hentjournalinfo.QueryNames.DOKUMENTINFO;
import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapFildetaljer;
import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapJournalpost;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.metrics.GraphQLMetrics;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.hentjournalinfo.Query;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.exceptions.JournalpostIkkeFunnetException;
import no.nav.freg.abac.core.annotation.Abac;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class DokumentInfoQuery implements Query {

    private final DokumentinfoRepository dokumentinfoRepository;

    private final AbacSecurityService abacSecurityService;

    @Inject
    public DokumentInfoQuery(DokumentinfoRepository dokumentinfoRepository, AbacSecurityService abacSecurityService) {
        this.dokumentinfoRepository = dokumentinfoRepository;
        this.abacSecurityService = abacSecurityService;
    }

    @GraphQLQuery(name = DOKUMENTINFO)
    @Transactional(readOnly = true)
    @GraphQLMetrics(value = "dok_graphql_request", extraTags = {"process_code", "gjoark001", "query", DOKUMENTINFO})
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
            actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION))
    public DokumentInfo dokumentInfo(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId) {
        log.info(format("GraphQL har mottatt %s query med dokumentInfoId=%s", DOKUMENTINFO, dokumentInfoId));
        abacSecurityService.assertAccessToDokumentNotBegrenset(dokumentInfoId);

        //Om dokumentet eksiterer sjekkes i metoden over og kan derfor være sikker på dokumentInfo finnes i neste step
        no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentInfoId).get();

        return mapDokumentInfo(dokumentInfo);
    }

    @GraphQLQuery(name = "originalJournalpost")
    @Transactional(readOnly = true)
    public Journalpost originalJournalpost(@GraphQLContext DokumentInfo dokument) {
        no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokument.getDokumentInfoId())
                .orElse(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder().build());
        no.nav.dokarkiv.core.domain.entities.Journalpost originalJournalpost = dokumentInfo.getOriginalJournalpost();
        if (originalJournalpost == null) {
            throw new JournalpostIkkeFunnetException(format("Fant ingen tilhørende original journalpost for dokumentInfo med dokumentInfoId=%s", dokument
                    .getDokumentInfoId()));
        }
        return mapJournalpost(originalJournalpost);
    }

    @GraphQLQuery(name = "knyttetJournalpostList")
    @Transactional(readOnly = true)
    public List<JournalpostDokumentRelasjon> knyttetJournalpostList(@GraphQLContext DokumentInfo dokumentInfo) {
        Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder().build())
                .getJournalpostRelasjonerAlsoBegrenset();

        return DokumentInfoQueryMapper.mapKnyttetJournalpostList(journalpostDokumentInfoRelasjons, dokumentInfo.getDokumentInfoId());
    }

    @GraphQLQuery(name = "tilleggsopplysninger")
    @Transactional(readOnly = true)
    public Map<String, String> tilleggsopplysninger(@GraphQLContext DokumentInfo dokumentInfo) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Map<String, String> tilleggsopplysninger = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder().build())
                .getTilleggsopplysninger();

        return new HashMap<>(tilleggsopplysninger);
    }

    @GraphQLQuery(name = "filDetaljerList")
    @Transactional(readOnly = true)
    public List<DokumentInfo.Fildetaljer> filDetaljerList(@GraphQLContext DokumentInfo dokumentInfo) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Set<FilDetaljer> filDetaljerSet = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder().build())
                .getFildetaljerListe();

        return mapFildetaljer(filDetaljerSet);
    }


}
