package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;

public class HandterAvvikIT extends AbstractJournalpostIT {

    private static final String HANDTER_AVVIK = "/handterAvvik/";

    @Test
    public void happyPathFeilregistrer() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + HANDTER_AVVIK + AvvikstypeConstants.FEILREGISTRER_SAKSRELASJON, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost feilregistrertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(2, journalpost.getJournalpostDokumentInfoRelasjoner().size());

    }
}
