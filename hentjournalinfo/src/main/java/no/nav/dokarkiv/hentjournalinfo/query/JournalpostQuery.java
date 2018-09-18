package no.nav.dokarkiv.hentjournalinfo.query;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.map.DokumentInfoMapper.mapDokumentInfo;
import static no.nav.dokarkiv.hentjournalinfo.map.JournalpostMapper.mapJournalpost;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.JOURNALPOST;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.exceptions.JournalpostIkkeFunnetException;
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
public class JournalpostQuery implements Query {

    private final JoarkRepository joarkRepository;

    @Inject
    public JournalpostQuery(JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    @GraphQLQuery(name = JOURNALPOST)
    @Transactional(readOnly = true)
    @RestMetrics(value = "dok_graphql_request", extraTags = {"query", JOURNALPOST, "subquery", "root"}, percentiles = {0.5, 0.95})
    public Journalpost journalpost(@GraphQLArgument(name = "journalpostId") Long journalpostId) {
        no.nav.dokarkiv.core.domain.entities.Journalpost journalpost = joarkRepository.findById(journalpostId)
                .orElseThrow(() -> new JournalpostIkkeFunnetException(format("Fant ingen journalpost med journalpostId=%s i JOARK databasen", journalpostId)));
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


        return journalpostDokumentInfoRelasjons.stream().map(relasjon -> {
            if (isTrue(relasjon.getDokumentInfo().getSlettet())) {
                //Skip if slettet
                return null;
            }
            return JournalpostDokumentRelasjon.builder()
                    .tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon.getTilknyttetJournalpostSom()
                            .name())
                    .journalpostId(journalpost.getJournalpostId())
                    .dokumentInfo(mapDokumentInfo(relasjon.getDokumentInfo())) //Like greit å bare mappe dokumentinfo når den må hentes opp fra DB for å hente dokumentInfoId (ref: LazyFetching)
                    .dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId()).build();
        }).collect(Collectors.toList());

    }
}
