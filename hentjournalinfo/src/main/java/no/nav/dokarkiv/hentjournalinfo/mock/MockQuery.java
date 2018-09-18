package no.nav.dokarkiv.hentjournalinfo.mock;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.DOKUMENT;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.DOKUMENTINFO;
import static no.nav.dokarkiv.hentjournalinfo.query.QueryNames.JOURNALPOST;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.exceptions.DokumentIkkeFunnetException;
import org.springframework.stereotype.Component;

/**
 * Creates mock data for using on the GraphiQL interface. Should only be used for testing
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class MockQuery {

    private MockDataUtils mockDataUtils = new MockDataUtils();

    @GraphQLQuery(name = DOKUMENTINFO, description = "Returnerer mock data for alle input. Vil kaste feil ved input=(dokumentInfoId: 0) og kan brukes for å simulere feil. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public DokumentInfo dokumentInfo(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId) {
        if (dokumentInfoId.equals(0L)) {
            throw new DokumentIkkeFunnetException(format("Fant ingen dokument med dokumentInfoId=%s i JOARK Databasen", dokumentInfoId));
        }

        DokumentInfo dokumentInfo = mockDataUtils.createDokumentInfo(dokumentInfoId);
        dokumentInfo.getKnyttetJournalpostList()
                .forEach(relasjon -> relasjon.setJournalpost(mockDataUtils.createJournalpost(relasjon.getJournalpostId())));

        return dokumentInfo;
    }

    @GraphQLQuery(name = DOKUMENT, description = "Returnerer base64 encoded mock fil. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public byte[] hentDokument(@GraphQLArgument(name = "dokumentInfoId") @GraphQLNonNull Long dokumentInfoId, @GraphQLArgument(name = "journalpostId") @GraphQLNonNull String journalpostId, @GraphQLArgument(name = "filtype") FilTypeCode filType) {
        return new byte[123321];
    }

    @GraphQLQuery(name = JOURNALPOST, description = "Returnerer mock data for alle input. Er ment til å brukes for å teste ut graphql apiet uten å gå gjennom sikkerhet")
    public Journalpost journalpost(@GraphQLArgument(name = "journalpostId") Long journalpostId) {

        Journalpost journalpost = mockDataUtils.createJournalpost(journalpostId);
        journalpost.getKnyttetDokumentList().forEach(relasjon -> {
            relasjon.setDokumentInfo(mockDataUtils.createDokumentInfo(relasjon.getDokumentInfoId()));
        });

        return journalpost;
    }


}
