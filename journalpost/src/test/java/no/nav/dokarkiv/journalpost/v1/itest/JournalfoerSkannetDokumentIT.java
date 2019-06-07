package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggResponse;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class JournalfoerSkannetDokumentIT extends AbstractJournalpostIT {

    @Inject
    SkannetInnholdRepository skannetInnholdRepository;

    private static final String LOGISK_VEDLEGG = "/logiskVedlegg/";
    private static final String NY_TITTEL = "Ny tittel";

    @Test
    public void happyPathEndreLogiskVedlegg() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        joarkRepository.save(journalpost);
        Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();
        Long logiskVedleggId = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentInfoId).getSkannetInnholdListe().iterator().next().getSkannetInnholdId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        EndreLogiskVedleggRequest request = EndreLogiskVedleggRequest.builder()
                .tittel(NY_TITTEL)
                .build();
        HttpEntity<EndreLogiskVedleggRequest> requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG + logiskVedleggId, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        SkannetInnhold skannetInnhold = skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggId.toString(), dokumentInfoId.toString())
                .orElseThrow(RuntimeException::new);

        assertEquals(skannetInnhold.getVedleggInnhold(), NY_TITTEL);
    }

    @Test
    public void happyPathLeggTilLogiskVedlegg() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        joarkRepository.save(journalpost);
        Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        LeggTilLogiskVedleggRequest request = LeggTilLogiskVedleggRequest.builder()
                .tittel(NY_TITTEL)
                .build();
        HttpEntity<LeggTilLogiskVedleggRequest> requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
        ResponseEntity<LeggTilLogiskVedleggResponse> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG, HttpMethod.POST, requestEntity, LeggTilLogiskVedleggResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        SkannetInnhold skannetInnhold = skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(response.getBody().getLogiskVedleggId(), dokumentInfoId.toString())
                .orElseThrow(RuntimeException::new);

        assertEquals(skannetInnhold.getVedleggInnhold(), NY_TITTEL);
    }

    @Test
    public void happyPathSlettLogiskVedlegg() throws IOException {
        abacPermit();

        Journalpost journalpost = TestDataGenerator.createJournalpostWithHoveddokument();
        joarkRepository.save(journalpost);
        Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();
        Long logiskVedleggId = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentInfoId).getSkannetInnholdListe().iterator().next().getSkannetInnholdId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        List<SkannetInnhold> skannetInnholdList = IteratorUtils.toList(skannetInnholdRepository.findAll().iterator());
        assertEquals(skannetInnholdList.size(), 1);

        HttpEntity<String> requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_DOKUMENTINFO + dokumentInfoId + LOGISK_VEDLEGG + logiskVedleggId, HttpMethod.DELETE, requestEntity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        skannetInnholdList = IteratorUtils.toList(skannetInnholdRepository.findAll().iterator());
        assertEquals(skannetInnholdList.size(), 0);
    }
}
