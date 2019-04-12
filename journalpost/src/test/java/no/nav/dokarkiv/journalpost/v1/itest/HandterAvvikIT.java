package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
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

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getSaksrelasjon().getFeilregistrert(), true);
    }

    @Test
    public void happyPathOpphevFeilregistrering() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.getSaksrelasjon().setFeilregistrert(true);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + HANDTER_AVVIK + AvvikstypeConstants.OPPHEV_FEILREGISTRERING, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getSaksrelasjon().getFeilregistrert(), false);
    }

    @Test
    public void happyPathUkjentBruker() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.setJournalstatus(JournalStatusCode.U);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + HANDTER_AVVIK + AvvikstypeConstants.UKJENT_BRUKER, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), JournalStatusCode.UB);
    }

    @Test
    public void happyPathAvbryt() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.setJournalstatus(JournalStatusCode.OD);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + HANDTER_AVVIK + AvvikstypeConstants.AVBRYT, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), JournalStatusCode.U);
    }
}
