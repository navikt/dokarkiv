package no.nav.dokarkiv.hentdokument.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class JournalpostDokumentInfoRelasjonResolver implements GraphQLResolver<JournalpostDokumentInfoRelasjon> {

    @Inject
    private JournalpostDokumentInfoRelasjonRepository repository;

    @Transactional
    public Journalpost journalpost(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
        return journalpostDokumentInfoRelasjon.getJournalpost();
    }

    @Transactional
    public DokumentInfo dokumentInfo(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
        return journalpostDokumentInfoRelasjon.getDokumentInfo();
    }

}
