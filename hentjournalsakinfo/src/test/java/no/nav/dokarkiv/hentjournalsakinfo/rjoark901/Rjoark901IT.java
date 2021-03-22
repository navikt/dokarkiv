package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Objects;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createBruker;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Rjoark901IT extends AbstractHentjournalsakinfoItest {

    private static final String HENTTILGANGJOURNALPOST_URI = "/hentjournalsakinfo/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String EXPECTED_BRUKER_ID = "11111111111";

    @Test
    public void shouldGetTilgangJournalpost() {
        Journalpost storedJournalpost = persistJournalpost(createJournalpostWithHoveddokument());
        Long journalpostId = storedJournalpost.getJournalpostId();
        Long dokumentInfoId = storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
                journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

        TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
        assertEquals(responseJournalpost.getJournalpostId(), journalpostId.toString());
    }

    @Test
    public void shouldGetTilgangJournalpostNoBruker() {
        Journalpost journalpostNoBrukere = createJournalpostWithHoveddokument();
        journalpostNoBrukere.clearBrukere();

        Journalpost storedJournalpost = persistJournalpost(journalpostNoBrukere);
        Long journalpostId = storedJournalpost.getJournalpostId();
        Long dokumentInfoId = storedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
                journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

        TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
        assertNull(responseJournalpost.getBruker().getBrukerId());
        assertNull(responseJournalpost.getBruker().getBrukerType());
    }

    @Test
    public void shouldGetTilgangJournalpostMultipleBrukereUsingLatestBruker() {
        Journalpost baseStoredJournalpost = persistJournalpost(createJournalpostWithHoveddokument());
        Bruker actualBruker = createBruker();
        actualBruker.setBrukerId(EXPECTED_BRUKER_ID);
        baseStoredJournalpost.addBruker(actualBruker);

        TestTransaction.start();
        Journalpost storedJournalpostTwoBrukere = persistJournalpost(baseStoredJournalpost);
        Long journalpostId = storedJournalpostTwoBrukere.getJournalpostId();
        Long dokumentInfoId = storedJournalpostTwoBrukere.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

        ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
                journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

        TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
        assertEquals(EXPECTED_BRUKER_ID, responseJournalpost.getBruker().getBrukerId());
    }

    @Test
    public void shouldGetTilgangJournalpostWithMoreData() {
        Journalpost journalpost = createJournalpostWithHoveddokument();
        journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, createDokumentInfoWithMoreData()));
        persistJournalpost(journalpost);

        Long journalpostId = journalpost.getJournalpostId();
        Long dokumentInfoId = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG).stream().findFirst().get().getDokumentInfo().getDokumentInfoId();

        ResponseEntity<HentTilgangJournalpostResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), HentTilgangJournalpostResponse.class,
                journalpostId, dokumentInfoId, VariantFormatCode.ARKIV.name());

        TilgangJournalpostDto responseJournalpost = Objects.requireNonNull(responseEntity.getBody()).getTilgangJournalpostDto();
        TilgangDokumentInfoDto tilgangDokumentInfoDto = responseJournalpost.getDokument();

        assertTrue(tilgangDokumentInfoDto.getInnskrenketTredjepart());
        assertTrue(tilgangDokumentInfoDto.getInnskrenketPartsinnsyn());
        assertTrue(tilgangDokumentInfoDto.getOrganinternt());
        assertEquals(DokumentKategoriCode.B, tilgangDokumentInfoDto.getKategori());
    }

    @Test
    public void shouldReturn404WhenJournalpostDokumentInfoVariantTripletDoesNotExist() {
        ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(HENTTILGANGJOURNALPOST_URI, HttpMethod.GET, createHeaderEntity(), RestConsumerExceptionResponse.class,
                1L, 1L, VariantFormatCode.ARKIV.name());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("rjoark901 kunne ikke hente TilgangJournalpost. Ingen treff på journalpostId=1, dokumentInfoId=1 og variantFormat=ARKIV. Feilmelding: Ingen jornalpost funnet", Objects.requireNonNull(responseEntity.getBody()).getMessage());

    }

    private Journalpost persistJournalpost(Journalpost journalpost) {
        joarkRepository.save(journalpost);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        return journalpost;
    }
}
