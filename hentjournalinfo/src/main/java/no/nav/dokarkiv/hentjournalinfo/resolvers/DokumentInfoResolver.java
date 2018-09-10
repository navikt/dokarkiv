package no.nav.dokarkiv.hentjournalinfo.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
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
public class DokumentInfoResolver implements GraphQLResolver<DokumentInfo> {

    @Inject
    private DokumentinfoRepository dokumentinfoRepository;

    @Transactional
    public List<GraphQlMap> tilleggsopplysninger(DokumentInfo dokumentInfo) {
        //Må hente på nytt fra databasen pågrunn av lazy initialisering
        Map<String, String> tilleggsopplysninger = dokumentinfoRepository.findById(dokumentInfo.getId())
                .orElse(new DokumentInfo())
                .getTilleggsopplysninger();
        return tilleggsopplysninger
                .keySet()
                .stream()
                .map(key -> GraphQlMap.builder().key(key).value(tilleggsopplysninger.get(key)).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<FilDetaljer> fildetaljerListe(DokumentInfo dokumentInfo) {
        return new ArrayList<>(dokumentinfoRepository.findById(dokumentInfo.getId())
                .orElse(new DokumentInfo())
                .getFildetaljerListe());
    }

    @Transactional
    public List<JournalpostDokumentInfoRelasjon> journalpostRelasjoner(DokumentInfo dokumentInfo) {
        return new ArrayList<>(dokumentinfoRepository.findById(dokumentInfo.getId())
                .orElse(new DokumentInfo())
                .getJournalpostRelasjoner());
    }

    @Transactional
    public List<SkannetInnhold> skannetInnholdListe(DokumentInfo dokumentInfo) {
        return new ArrayList<>(dokumentinfoRepository.findById(dokumentInfo.getId())
                .orElse(new DokumentInfo())
                .getSkannetInnholdListe());
    }
}
