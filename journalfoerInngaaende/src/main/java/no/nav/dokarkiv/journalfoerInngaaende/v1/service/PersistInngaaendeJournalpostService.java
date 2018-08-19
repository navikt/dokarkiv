package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse.AvsenderId.MANGLER;
import static no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse.AvsenderId.MANGLER_IKKE;
import static org.hibernate.annotations.common.util.StringHelper.isEmpty;

import no.nav.dok.tjenester.journalfoerinngaaende.response.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutJournalpostResponse;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalfoerInngaaende.v1.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Paul Magne Lunde, Visma Consulting
 */
@Service
public class PersistInngaaendeJournalpostService {

    private JoarkRepository joarkRepository;

    @Inject
    public PersistInngaaendeJournalpostService(JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public PutJournalpostResponse persist(String journalpostId, PutJournalpostRequest putJournalpostRequest) {
        Journalpost journalpost = getJournalpost(journalpostId);

        verifyMidlertidigJournalfoert(journalpost);

        if (!journalpost.isInngaende()) {
            throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngaaende", HttpStatus.BAD_REQUEST);
        }

        verifyDokumentInfos(journalpost);

        verifyHoveddokument(journalpost);

        verifyFildetaljer(journalpost);

        verifyRequiredFields(journalpost);

        journalpost.setInnhold(putJournalpostRequest.getTittel());
        journalpost.setFagomrade(FagomradeCode.valueOf(putJournalpostRequest.getTema()));
        journalpost.setAvsenderMottaker(putJournalpostRequest.getAvsender().getNavn());
        journalpost.setAvsenderMottakerId(putJournalpostRequest.getAvsender().getIdentifikator());

        if (putJournalpostRequest.getArkivSak() != null) {
            Saksrelasjon saksrelasjon = new Saksrelasjon();
            saksrelasjon.setSakId(putJournalpostRequest.getArkivSak().getArkivSakId());
            saksrelasjon.setFagsystem(FagsystemCode.valueOf(putJournalpostRequest.getArkivSak().getArkivSakSystem().name()));
            journalpost.setSaksrelasjon(saksrelasjon);
        }

        Set<Bruker> brukere = journalpost.getBrukere();
        if (brukere.isEmpty() || brukere.size() > 1) {
            Bruker bruker = new Bruker();
            bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
            bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
            journalpost.getBrukere().clear();
            journalpost.getBrukere().add(bruker);
        } else {
            brukere.iterator().forEachRemaining(bruker -> {
                bruker.setBrukerId(putJournalpostRequest.getBruker().getIdentifikator());
                bruker.setBrukerType(BrukerTypeCode.valueOf(putJournalpostRequest.getBruker().getBrukerType().name()));
            });
        }

        if (putJournalpostRequest.getForsoekEndeligJF()) {
            journalpost.setJournalstatus(JournalStatusCode.J);
            journalpost.setJournalForendeEnhetId(putJournalpostRequest.getJournalfEnhet());
            journalpost.setJournalDato(new Date());
            journalpost.setEndretAvNavn("dfd"); //TODO Fra MDC
            journalpost.setJournalfortAvNavn("dfdf"); //TODO
        }

        joarkRepository.save(journalpost);

        return createResponse(journalpost);
    }

    private Journalpost getJournalpost(String journalpostId) {
        try {
            return joarkRepository.findById(Utils.convertStringToLong(journalpostId, "journalpostId")).get();
        } catch (NoSuchElementException e) {
            throw new DokarkivRestFunctionalException(String.format("Oppgitt journalpostId %s eksisterer ikke", journalpostId), HttpStatus.NOT_FOUND);
        }
    }

    private void verifyMidlertidigJournalfoert(Journalpost jp) {
        if (!jp.hasMidlertidigInngaaendeJournalforingStatus()) {
            throw new DokarkivRestFunctionalException("Journalposten er ikke midlertidig journalført", HttpStatus.BAD_REQUEST);
        }
    }

    private void verifyDokumentInfos(Journalpost journalpost) {
        try {
            journalpost.verifyNoDokumentInfosUnderRedigering();
        } catch (InvalidJournalpostStructureException e) {
            throw new DokarkivRestFunctionalException("Ett eller flere av dokumentene som forsøkes oppdatert er ikke ferdigstilt", HttpStatus.BAD_REQUEST);
        }
    }

    private void verifyHoveddokument(Journalpost journalpost) {
        try {
            journalpost.verifyOnlyOneHoveddokument();
        } catch (InvalidJournalpostStructureException e) {
            throw new DokarkivRestFunctionalException("Journalpost inneholder ikke ett hoveddokument", HttpStatus.BAD_REQUEST);
        }
    }

    private void verifyFildetaljer(Journalpost journalpost) {
        try {
            journalpost.verifyArkivVariantOfAllDocuments();
        } catch (InvalidJournalpostStructureException e) {
            throw new DokarkivRestFunctionalException("Det mangler arkivvariant, dette er påkrevd for å ferdigstille journalposter", HttpStatus.BAD_REQUEST);
        }
        journalpost.getJournalpostDokumentInfoRelasjoner().forEach(dr -> {
            try {
                dr.getDokumentInfo().verifyNoVariantDuplicates();
            } catch (InvalidJournalpostStructureException e) {
                throw new DokarkivRestFunctionalException("Journalpost inneholder flere fildetaljer med samme variantformat", HttpStatus.BAD_REQUEST);
            }
        });
    }

    private void verifyRequiredFields(Journalpost journalpost) {
        try {
            journalpost.verifyMandatoryFieldsSkipJournalforendeEnhetId();
        } catch (Exception e) {
            throw new DokarkivRestFunctionalException("Journalpost mangler påkrevde felt for endelig journalføring", HttpStatus.BAD_REQUEST);
        }
    }

    private PutJournalpostResponse createResponse(Journalpost jp) {

        List<Dokument> dokumentList = new ArrayList<>();
        jp.getJournalpostDokumentInfoRelasjoner().forEach(d -> {
            DokumentInfo dokumentInfo = d.getDokumentInfo();
            if (dokumentInfo != null) {
                dokumentList.add(
                        new Dokument()
                        .withDokumentId(dokumentInfo.getId().toString())
                        .withTittel(isEmpty(dokumentInfo.getTittel()) ? MANGLER : MANGLER_IKKE)
                        .withDokumentKategori(isEmpty(dokumentInfo.getKategori().name()) ? MANGLER : MANGLER_IKKE)
                );
            }
        });

        return new PutJournalpostResponse()
                .withAvsenderId(isEmpty(jp.getAvsenderMottakerId()) ? MANGLER : MANGLER_IKKE)
                .withAvsenderNavn(isEmpty(jp.getAvsenderMottaker()) ? MANGLER : MANGLER_IKKE)
                .withArkivSak((jp.getSaksrelasjon() != null) ? MANGLER : MANGLER_IKKE)
                .withTittel(isEmpty(jp.getInnhold()) ? MANGLER : MANGLER_IKKE)
                .withTema((jp.getFagomrade() != null) ? MANGLER : MANGLER_IKKE)
                .withBruker((jp.getBrukere().isEmpty()) ? MANGLER : MANGLER_IKKE)
                .withDokumenter(dokumentList);
    }
}
