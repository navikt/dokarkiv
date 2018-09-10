package no.nav.dokarkiv.hentjournalinfo.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.ReturInfo;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.hentjournalinfo.objects.GraphQlMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class JournalpostResolver implements GraphQLResolver<Journalpost> {

    @Inject
    private JoarkRepository joarkRepository;

    @Transactional
    public List<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner(Journalpost journalpost) {
        return new ArrayList<>(joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new Journalpost())
                .getJournalpostDokumentInfoRelasjoner());
    }

    @Transactional
    public List<Kryssreferanse> kryssreferanser(Journalpost journalpost) {
        return new ArrayList<>(joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new Journalpost())
                .getKryssreferanser());
    }

    @Transactional
    public List<ReturInfo> returInfos(Journalpost journalpost) {
        return new ArrayList<>(joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new Journalpost())
                .getReturInfos());
    }

    @Transactional
    public List<Bruker> brukere(Journalpost journalpost) {
        return new ArrayList<>(joarkRepository.findById(journalpost.getJournalpostId())
                .orElse(new Journalpost())
                .getBrukere());
    }

    @Transactional
    public Saksrelasjon saksrelasjon(Journalpost journalpost) {
        return joarkRepository.findById(journalpost.getId()).orElse(new Journalpost()).getSaksrelasjon();
    }

    public FagomradeCode tema(Journalpost journalpost) {
        return journalpost.getFagomrade();
    }

    @Transactional
    public List<GraphQlMap> tilleggsopplysninger(Journalpost journalpost) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Map<String, String> tilleggsopplysninger = joarkRepository.findById(journalpost.getId())
                .orElse(new Journalpost())
                .getTilleggsopplysninger();
        return tilleggsopplysninger
                .keySet()
                .stream()
                .map(key -> GraphQlMap.builder().key(key).value(tilleggsopplysninger.get(key)).build())
                .collect(Collectors.toList());
    }
}
