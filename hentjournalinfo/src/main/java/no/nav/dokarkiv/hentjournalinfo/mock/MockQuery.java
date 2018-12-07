package no.nav.dokarkiv.hentjournalinfo.mock;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.QueryNames.DOKUMENT;
import static no.nav.dokarkiv.hentjournalinfo.QueryNames.DOKUMENTINFO;
import static no.nav.dokarkiv.hentjournalinfo.QueryNames.JOURNALPOST;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentIkkeFunnetException;
import org.springframework.stereotype.Component;

/**
 * Creates mock data for using on the GraphiQL interface. Should only be used for testing
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class MockQuery {

    private MockDataUtils mockDataUtils = new MockDataUtils();

    @GraphQLQuery(name = DOKUMENTINFO, description = "Returnerer mock data for alle input. Vil kaste feil ved input=(dokumentInfoId: 0) som kan brukes for å simulere feil. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public DokumentInfo dokumentInfo(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId) {
        log.info(format("GraphQL har mottat %s mock query med dokumentInfoId=%s", DOKUMENTINFO, dokumentInfoId));

        if (dokumentInfoId.equals(0L)) {
            throw new DokumentIkkeFunnetException(format("Fant ingen dokument med dokumentInfoId=%s i JOARK Databasen", dokumentInfoId));
        }

        DokumentInfo dokumentInfo = mockDataUtils.createDokumentInfo(dokumentInfoId);
        dokumentInfo.getKnyttetJournalpostList()
                .forEach(relasjon -> relasjon.setJournalpost(mockDataUtils.createJournalpost(relasjon.getJournalpostId())));

        return dokumentInfo;
    }

    @GraphQLQuery(name = DOKUMENT, description = "Returnerer base64 encoded mock fil. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public byte[] dokumentFil(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId, @GraphQLArgument(name = "variantFormat") @GraphQLNonNull VariantFormat variantFormat) {
        log.info(format("GraphQL har mottat %s mock query med dokumentInfoId=%s, journalpostId=%s", DOKUMENT, dokumentInfoId, 1L));

        return new byte[123321];
    }

    @GraphQLQuery(name = JOURNALPOST, description = "Returnerer mock data for alle input. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public Journalpost journalpost(@GraphQLArgument(name = "journalpostId") Long journalpostId) {
        log.info(format("GraphQL har mottat %s mock query med journalpostId=%s", JOURNALPOST, journalpostId));

        Journalpost journalpost = mockDataUtils.createJournalpost(journalpostId);
        journalpost.getKnyttetDokumentList().forEach(relasjon -> {
            relasjon.setDokumentInfo(mockDataUtils.createDokumentInfo(relasjon.getDokumentInfoId()));
        });

        return journalpost;
    }


}
