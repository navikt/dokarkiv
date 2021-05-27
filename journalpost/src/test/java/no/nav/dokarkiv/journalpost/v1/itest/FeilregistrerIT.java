package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPHEV_FEILREGISTRERING;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UKJENT_BRUKER;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_STATUS_UTGÅR;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_UKJENT_BRUKER;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.OK;

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
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + FEILREGISTRER_SAKSTILKNYTNING, PATCH, requestEntity, String.class);

        assertEquals(OK, response.getStatusCode());

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
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING, PATCH, requestEntity, String.class);

        assertEquals(OK, response.getStatusCode());

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
        assertEquals(OPPHEV_FEILREGISTRERING, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
    }

    @Test
    public void happyPathUkjentBruker() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.setJournalstatus(U);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_UKJENT_BRUKER, PATCH, requestEntity, String.class);

        assertEquals(OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), UB);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(UKJENT_BRUKER, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
    }

    @Test
    public void shouldGet405WhenJournalPostHaveStatusUtgaaende() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.setJournalstatus(U);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_STATUS_UTGÅR, PATCH, requestEntity, String.class);

        assertEquals(METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    public void happyPathUkjentUtgaar() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        journalpost.setJournalstatus(OD);
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + FEILREGISTRER + SETT_STATUS_UTGÅR, PATCH, requestEntity, String.class);

        assertEquals(OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost oppdatertJournalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(oppdatertJournalpost.getJournalstatus(), U);

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());

        assertEquals(1, aksjonsLoggList.size());

        AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
        assertEquals(journalpostId, aksjonsLogg.getJournalpostId());
        assertEquals(SERVICE_USER_ID, aksjonsLogg.getUtfoertAv());
        assertEquals(UTGAAR, aksjonsLogg.getAksjon());
        assertEquals(HJEMMEL, aksjonsLogg.getHjemmel());
        assertEquals(1, aksjonsLogg.getArkivElementEndringer().size());
    }
}
