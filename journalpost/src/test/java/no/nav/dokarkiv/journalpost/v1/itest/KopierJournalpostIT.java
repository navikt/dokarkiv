package no.nav.dokarkiv.journalpost.v1.itest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KopierJournalpostIT extends AbstractJournalpostIT {

    @Test
    public void happyPathInngaaende() throws IOException {
        abacPermit();

        Journalpost journalpost = createJournalpost();
        Long journalpostId = joarkRepository.save(journalpost).getJournalpostId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        HttpEntity requestEntity = new HttpEntity(createHeadersWithServiceUserToken());
        ResponseEntity<String> response = restTemplate.exchange(URL_JOURNALPOST + journalpostId + KOPIERJOURNALPOST, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Journalpost kopiertJournalpost = joarkRepository.findById(Long.parseLong(response.getBody())).orElseThrow(RuntimeException::new);
        journalpost = joarkRepository.findById(journalpostId).orElseThrow(RuntimeException::new);

        assertEquals(2, journalpost.getJournalpostDokumentInfoRelasjoner().size());
        assertEquals(2, kopiertJournalpost.getJournalpostDokumentInfoRelasjoner().size());

        assertEquals(kopiertJournalpost.getBrukere().size(), journalpost.getBrukere().size());

        Bruker kopiertBruker = kopiertJournalpost.getBrukere().iterator().next();
        Bruker originalBruker = journalpost.getBrukere().iterator().next();
        assertTrue(kopiertBruker.getChangeStamp().getCreatedDate().after(originalBruker.getChangeStamp().getCreatedDate()));
        assertEquals(SERVICE_USER_ID, kopiertBruker.getEndretKildeNavn());
        assertEquals(SERVICE_USER_ID, kopiertBruker.getOpprettetKildeNavn());

        assertTrue(brukereSetIsCorrectlyCopied(journalpost.getBrukere(), kopiertJournalpost.getBrukere()));
        assertTrue(kopiertJournalpost.getKryssreferanser().isEmpty());
        assertTrue(journalpostDokumentInfoRelasjonerAreCorrectlyCopied(
                journalpost.getJournalpostDokumentInfoRelasjoner(),
                kopiertJournalpost.getJournalpostDokumentInfoRelasjoner())
        );

        assertSaksrelasjon(journalpost.getSaksrelasjon(), kopiertJournalpost.getSaksrelasjon(), kopiertJournalpost);
        assertEquals(journalpost.getTilleggsopplysninger(), kopiertJournalpost.getTilleggsopplysninger());
        assertEquals(journalpost.getBehandlingstema(), kopiertJournalpost.getBehandlingstema());
        assertEquals(journalpost.getJournalposttype(), kopiertJournalpost.getJournalposttype());
        assertEquals(journalpost.getFagomrade(), kopiertJournalpost.getFagomrade());
        assertEquals(journalpost.getAvsenderMottakerId(), kopiertJournalpost.getAvsenderMottakerId());
        assertEquals(journalpost.getAvsenderMottaker(), kopiertJournalpost.getAvsenderMottaker());
        assertEquals(journalpost.getInnhold(), kopiertJournalpost.getInnhold());
        assertEquals(JournalStatusCode.M, kopiertJournalpost.getJournalstatus());

        List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
        assertEquals(1, aksjonsLoggList.size());
        assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
        assertEquals(AksjonsTypeCode.KOPIER_JOURNALPOST, aksjonsLoggList.get(0).getAksjon());

        Set<ArkivElementEndring> arkivElementEndringTOs = aksjonsLoggList.get(0).getArkivElementEndringer();
        assertEquals(1, arkivElementEndringTOs.size());

        ArkivElementEndring arkivElementEndring = arkivElementEndringTOs.iterator().next();
        assertEquals(arkivElementEndring.getFraVerdi(), Long.toString(journalpost.getJournalpostId()));
        assertEquals(arkivElementEndring.getTilVerdi(), Long.toString(kopiertJournalpost.getJournalpostId()));
    }

    @After
    public void closeTransaction() {
        TestTransaction.end();
    }

    private boolean journalpostDokumentInfoRelasjonerAreCorrectlyCopied(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner, Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonerCopy) {
        ArrayList<Map<String, String>> journalpostDokumentInfoRelasjonerList = journalpostDokumentInfoRelasjonerSetToArrayList(journalpostDokumentInfoRelasjoner);
        ArrayList<Map<String, String>> journalpostDokumentInfoRelasjonerCopyList = journalpostDokumentInfoRelasjonerSetToArrayList(journalpostDokumentInfoRelasjonerCopy);
        return journalpostDokumentInfoRelasjonerList.containsAll(journalpostDokumentInfoRelasjonerCopyList) && journalpostDokumentInfoRelasjonerCopyList.containsAll(journalpostDokumentInfoRelasjonerList);
    }

    private ArrayList<Map<String, String>> journalpostDokumentInfoRelasjonerSetToArrayList(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner) {
        ArrayList<Map<String, String>> journalpostDokumentInfoRelasjonerList = new ArrayList<>();

        journalpostDokumentInfoRelasjoner.forEach(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjonerList.add(new HashMap<String, String>()
          {{
              put("dokumentInfoId", journalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId().toString());
              put("tilknyttetJournalpostSom", journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom().name());
          }}
        ));

        return journalpostDokumentInfoRelasjonerList;
    }

    private void assertSaksrelasjon(Saksrelasjon saksrelasjon, Saksrelasjon kopiertSaksrelasjon, Journalpost kopiertJournalpost) {
        assertEquals(saksrelasjon.getSakId(), kopiertSaksrelasjon.getSakId());
        assertEquals(saksrelasjon.getFagsystem(), kopiertSaksrelasjon.getFagsystem());
        assertEquals(saksrelasjon.getFeilregistrert(), kopiertSaksrelasjon.getFeilregistrert());
        assertEquals(kopiertJournalpost.getSaksrelasjon().getJournalpost(), kopiertJournalpost);
    }

    private boolean brukereSetIsCorrectlyCopied(Set<Bruker> brukere, Set<Bruker> brukereCopy) {
        ArrayList<Map<String, String>> brukereList = brukereSetToArrayList(brukere);
        ArrayList<Map<String, String>> brukereCopyList = brukereSetToArrayList(brukereCopy);
        return brukereCopyList.containsAll(brukereList) && brukereList.containsAll(brukereCopyList);
    }

    private ArrayList<Map<String, String>> brukereSetToArrayList(Set<Bruker> brukere) {
        ArrayList<Map<String, String>> brukereList = new ArrayList<>();

        brukere.forEach(bruker -> brukereList.add(new HashMap<String, String>()
            {{
                put("brukerId", bruker.getBrukerId());
                put("brukerType", bruker.getBrukerType().name());
            }}
        ));

        return brukereList;
    }

    private Journalpost createJournalpost() {
        return JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.FL)
                .tilleggsopplysninger(new HashMap<String, String>() {{
                    put("nokkel1", "verdi1");
                    put("nokkel2", "verdi2"); }})
                .opprettetAvNavn("opprettetAvNavn")
                .opprettetKildeNavn("opprettetKildeNavn")
                .endretKildeNavn("endretKildeNavn")
                .endretAvNavn("endretAvNavn")
                .build();
    }
}
