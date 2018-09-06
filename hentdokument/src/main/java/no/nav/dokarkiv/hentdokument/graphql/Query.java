package no.nav.dokarkiv.hentdokument.graphql;

import static java.lang.String.format;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
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

    @Transactional
    public DokumentInfo dokumentInfo(Long id) {
        return dokumentinfoRepository.findById(id).orElse(new DokumentInfo());
    }

    @Transactional
    public Journalpost journalpost(Long id) {
        return joarkRepository.findById(id).orElse(new Journalpost());
    }

    @Transactional
    public String fil(Long dokumentInfoId, String filtype) {
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
