package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class FeilregistrerIT extends AbstractJournalpostIT {

    private static final String FEILREGISTRER = "/feilregistrer/";
    private static final String HJEMMEL = "ARKL";

    @Test
    public void happyPathFeilregistrer() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getSaksrelasjon().getFeilregistrert(), true);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(AksjonsTypeCode.FEILREGISTRER_SAKSTILKNYTNING, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
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
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + AvvikstypeConstants.OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getSaksrelasjon().getFeilregistrert(), false);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(AksjonsTypeCode.OPPHEV_FEILREGISTRERING, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
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
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + AvvikstypeConstants.SETT_UKJENT_BRUKER, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), JournalStatusCode.UB);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(AksjonsTypeCode.UKJENT_BRUKER, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
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
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + AvvikstypeConstants.AVBRYT, HttpMethod.PATCH, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), JournalStatusCode.U);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(AksjonsTypeCode.AVBRYT, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
    }
}
