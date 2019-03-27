package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.HashMap;

public class KopierJournalpostIT extends AbstractKopierJournalpostIT {

    @Test
    public void happyPathInngaaende() throws IOException {
        abacPermit();

        Journalpost journalpost = JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.FL)
                .tilleggsopplysninger(new HashMap<String, String>() {
                    {
                        put("nokkel1", "verdi1");
                        put("nokkel2", "verdi2");
                    }
                })
                .opprettetAvNavn("opprettetAvNavn")
                .opprettetKildeNavn("opprettetKildeNavn")
                .endretKildeNavn("endretKildeNavn")
                .endretAvNavn("endretAvNavn")
                .build();
        joarkRepository.save(journalpost);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        Long journalpostId = journalpost.getJournalpostId();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + KOPIERJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        TestTransaction.start();
        Journalpost kopiertJournalpost = joarkRepository.findById(Long.parseLong(response.getBody())).orElseThrow(RuntimeException::new);


        TestTransaction.end();
    }
}
