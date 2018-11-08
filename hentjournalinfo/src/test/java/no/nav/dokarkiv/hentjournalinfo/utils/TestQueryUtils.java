package no.nav.dokarkiv.hentjournalinfo.utils;

import static java.lang.String.format;
import static no.nav.dokarkiv.hentjournalinfo.QueryNames.DOKUMENT;

import no.nav.dokarkiv.hentjournalinfo.GraphQLRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestQueryUtils {

    public static String createJournalpostQuery() {

        return "journalpost(journalpostId: $journalpostId) {" +
                "    journalpostId" +
                "    journalpostStatus" +
                "    journalpostType" +
                "    tema" +
                "    tittel" +
                "    slettet" +
                "    brukere {" +
                "      brukerId" +
                "      brukerType" +
                "    }" +
                "    knyttetDokumentList {" +
                "      dokumentInfoId" +
                "      journalpostId" +
                "      tilknyttetJournalpostSom" +
                "      slettet" +
                "      dokumentInfo {" +
                "        dokumentInfoId" +
                "        dokumentStatus" +
                "        tilleggsopplysninger" +
                "        tittel" +
                "      }" +
                "    }" +
                "    " +
                "  }";
    }

    public static String createDokumentInfoQuery() {
        return "dokumentInfo(dokumentInfoId: $dokumentInfoId) {" +
                "    dokumentInfoId" +
                "    tittel" +
                "    slettet" +
                "    dokumentStatus" +
                "   filDetaljerList {" +
                "      fildetaljerId" +
                "      filtype" +
                "      variantFormat" +
                "    }" +
                "    tilleggsopplysninger" +
                "    knyttetJournalpostList {" +
                "      journalpost {" +
                "        tema" +
                "      }" +
                "      journalpostId" +
                "      slettet" +
                "      tilknyttetJournalpostSom" +
                "    }" +
                "    originalJournalpost {" +
                "      journalpostId" +
                "      journalpostStatus" +
                "      journalpostType" +
                "      tema" +
                "      tittel" +
                "      slettet" +
                "      brukere {" +
                "        brukerId" +
                "        brukerType" +
                "      }" +
                "    }" +
                "  }";
    }

    public static GraphQLRequest createJournalpostRequest(Long journalpostId) {

        Map<String, Object> variables = new HashMap<>();

        variables.put("journalpostId", journalpostId);
        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($journalpostId: Long!) " +
                        "{" + createJournalpostQuery() + "}")
                .build();

    }

    public static GraphQLRequest createDokumentInfoRequest(Long dokumentInfoId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("dokumentInfoId", dokumentInfoId);


        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($dokumentInfoId: Long!) " +
                        " {" + createDokumentInfoQuery() + "}")
                .build();
    }

    public static GraphQLRequest createJournalpostDokumentInfoRequest(Long journalpostId, Long dokumentInfoId) {
        Map<String, Object> variables = new HashMap<>();

        variables.put("journalpostId", journalpostId);
        variables.put("dokumentInfoId", dokumentInfoId);

        return GraphQLRequest.builder()
                .variables(variables)
                .query("query ($journalpostId: Long! $dokumentInfoId: Long!) " +
                        "{" + createJournalpostQuery() + createDokumentInfoQuery() + "}")
                .build();
    }

    public static GraphQLRequest createFilRequest(Long dokumentInfoId, Long journalpostId) {
        Map<String, Object> variables = new HashMap<>();

        variables.put("dokId", dokumentInfoId);
        variables.put("jpId", journalpostId);
        return GraphQLRequest.builder()
                .variables(variables)
                .query(format("query ($dokId: Long! $jpId: Long!) {  %s(dokumentInfoId:$dokId, journalpostId:$jpId )}", DOKUMENT))
                .build();
    }


}
