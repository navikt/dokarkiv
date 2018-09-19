package no.nav.dokarkiv.hentjournalinfo.query;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.map.DokumentInfoMapper.mapDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.map.JournalpostMapper.mapJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.DOKUMENTINFO;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentLogiskSlettetException;
import no.nav.dokarkiv.hentjournalinfo.exceptions.JournalpostIkkeFunnetException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @RestMetrics(value = "dok_graphql_request", extraTags = {"query", DOKUMENTINFO}, percentiles = {0.5, 0.95}, logException = false)
    public DokumentInfo dokumentInfo(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId) {
        log.info(format("GraphQL har mottatt %s query med dokumentInfoId=%s", DOKUMENTINFO, dokumentInfoId));
        abacSecurityService.assertAccessToKode6AndKode7();
        no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentInfoId)
                .orElseThrow(() -> new DokumentIkkeFunnetException(format("Fant ingen dokument med dokumentInfoId=%s i JOARK Databasen", dokumentInfoId)));

        if (isTrue(dokumentInfo.getSlettet())) {
            throw new DokumentLogiskSlettetException(format("Dokument med dokumentInfoId=%s er satt som logisk slettet og kan derfor ikke hentes", dokumentInfoId));
        }

        return mapDokumentInfo(dokumentInfo);
    }

    @GraphQLQuery(name = "originalJournalpost")
    @Transactional(readOnly = true)
    public Journalpost originalJournalpost(@GraphQLContext DokumentInfo dokument) {
        no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokument.getDokumentInfoId())
                .orElseThrow(() -> new DokumentIkkeFunnetException(format("Fant ingen dokument med dokumentInfoId=%s i joark Databasen", dokument
                        .getDokumentInfoId())));
        no.nav.dokarkiv.core.domain.entities.Journalpost originalJournalpost = dokumentInfo.getOriginalJournalpost();
        if (originalJournalpost == null) {
            throw new JournalpostIkkeFunnetException(format("Fant ingen tilhørende journalpost for dokument med dokumentInfoId=%s (Dette bør egentlig aldri være tilfelle)", dokument
                    .getDokumentInfoId()));
        }
        return mapJournalpost(originalJournalpost);

    }

    @GraphQLQuery(name = "knyttetJournalpostList")
    @Transactional(readOnly = true)
    public List<JournalpostDokumentRelasjon> knyttetJournalpostList(@GraphQLContext DokumentInfo dokumentInfo) {
        Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjons = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(new no.nav.dokarkiv.core.domain.entities.DokumentInfo())
                .getJournalpostRelasjoner();

        return journalpostDokumentInfoRelasjons.stream().map(relasjon -> JournalpostDokumentRelasjon.builder()
                .tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon.getTilknyttetJournalpostSom()
                        .name())
                .journalpostId(relasjon.getJournalpost().getJournalpostId())
                .journalpost(mapJournalpost(relasjon.getJournalpost())) //Like greit å bare mappe journalpost når den må hentes opp fra DB for å hente jpId (ref: LazyFetching)
                .dokumentInfoId(dokumentInfo.getDokumentInfoId()).build()).collect(Collectors.toList());

    }

    @GraphQLQuery(name = "tilleggsopplysninger")
    @Transactional(readOnly = true)
    public Map<String, String> tilleggsopplysninger(@GraphQLContext DokumentInfo dokumentInfo) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Map<String, String> tilleggsopplysninger = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(new no.nav.dokarkiv.core.domain.entities.DokumentInfo())
                .getTilleggsopplysninger();
        return new HashMap<>(tilleggsopplysninger);
    }

    @GraphQLQuery(name = "filDetaljerList")
    @Transactional(readOnly = true)
    public List<DokumentInfo.Fildetaljer> filDetaljerList(@GraphQLContext DokumentInfo dokumentInfo) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Set<FilDetaljer> filDetaljerSet = dokumentinfoRepository.findById(dokumentInfo.getDokumentInfoId())
                .orElse(new no.nav.dokarkiv.core.domain.entities.DokumentInfo())
                .getFildetaljerListe();
        return filDetaljerSet.stream()
                .map(fildetaljer -> DokumentInfo.Fildetaljer.builder()
                        .fildetaljerId(fildetaljer.getFildetaljerId())
                        .filtype(fildetaljer.getFiltype() == null ? null : fildetaljer.getFiltype().name())
                        .variantFormat(fildetaljer.getVariantFormat() == null ? null : fildetaljer.getVariantFormat().name())
                        .build()).collect(Collectors.toList());
    }


}
