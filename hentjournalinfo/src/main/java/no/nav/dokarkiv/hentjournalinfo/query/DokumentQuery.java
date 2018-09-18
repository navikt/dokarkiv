package no.nav.dokarkiv.hentjournalinfo.query;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.DOKUMENT;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentLogiskSlettetException;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class DokumentQuery implements Query {

    private final DokumentinfoRepository dokumentinfoRepository;

    private final DokumentFilRepository dokumentFilRepository;

    private final AbacSecurityService abacSecurityService;

    @Inject
    public DokumentQuery(DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, AbacSecurityService abacSecurityService) {
        this.dokumentinfoRepository = dokumentinfoRepository;
        this.dokumentFilRepository = dokumentFilRepository;
        this.abacSecurityService = abacSecurityService;
    }

    @GraphQLQuery(name = DOKUMENT, description = "Selve dokumentet i PDF/PDFA format")
    @Transactional(readOnly = true)
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark002g"}, percentiles = {0.5, 0.95})
    public byte[] hentDokument(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId, @GraphQLArgument(name = "journalpostId") @GraphQLNonNull String journalpostId, @GraphQLArgument(name = "filtype") FilTypeCode filType) {

//        abacSecurityService.assertAccessToJournalpost(journalpostId);

        DokumentInfo dokumentInfo = dokumentinfoRepository.findById(dokumentInfoId)
                .orElse(new no.nav.dokarkiv.core.domain.entities.DokumentInfo());

        if (BooleanUtils.isTrue(dokumentInfo.getSlettet())) {
            throw new DokumentLogiskSlettetException(format("Dokument med dokumentInfoId=%s er satt som logisk slettet og kan derfor ikke hentes", dokumentInfoId));
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
