package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSak;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.LogiskVedlegg;
import no.nav.dok.tjenester.journalfoerinngaaende.Variant;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class GetInngaaendeJournalpostMapper {

    private static final List<JournalStatusCode> MIDLERTIDIG_STATUS = Arrays.asList(JournalStatusCode.M, JournalStatusCode.MO, JournalStatusCode.UB, JournalStatusCode.OD);

    public GetJournalpostResponse map(Journalpost journalpost) {
        return new GetJournalpostResponse()
                .withJournalTilstand(GetJournalpostResponse.JournalTilstand.fromValue(mapJournaltilstand(journalpost)))
                .withAvsender(mapAvsender(journalpost))
                .withBrukerListe(mapBrukere(journalpost.getBrukere()))
                .withArkivSak(mapArkivsak(journalpost.getSaksrelasjon()))
                .withTema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
                .withTittel(journalpost.getInnhold())
                .withKanalReferanseId(journalpost.getKanalReferanseId())
                .withForsendelseMottatt(journalpost.getMottattDato())
                .withMottaksKanal(journalpost.getMottakskanal() == null ? null : journalpost.getMottakskanal().name())
                .withJournalfEnhet(journalpost.getJournalForendeEnhetId())
                .withDokumentListe(mapDokumenter(journalpost.getJournalpostDokumentInfoRelasjoner()));
    }

    private String mapJournaltilstand(Journalpost journalpost) {
        if (journalpost.isFeilregistrert()) {
            return JournaltilstandKode.UTGAAR.name();
        } else if (journalpost.hasEndeligJournalforingStatus()) {
            return JournaltilstandKode.ENDELIG.name();
        } else if (MIDLERTIDIG_STATUS.contains(journalpost.getJournalstatus())) {
            return JournaltilstandKode.MIDLERTIDIG.name();
        } else if (journalpost.hasUtgaattJournalforingStatus()) {
            return JournaltilstandKode.UTGAAR.name();
        } else {
            throw new DokarkivFunctionalException("Ugyldig journalstatus for inngående Journalpost. journalpostId=" + journalpost
                    .getJournalpostId());
        }
    }

    private enum JournaltilstandKode {
        MIDLERTIDIG,
        UTGAAR,
        ENDELIG
    }

    private ArkivSak mapArkivsak(Saksrelasjon saksrelasjon) {
        if (saksrelasjon == null) {
            return null;
        }
        //TODO Finn ut om dette skal valideres på
// else if (saksrelasjon.getFagsystem() != FagsystemCode.FS22 || saksrelasjon.getFagsystem() != FagsystemCode.PEN {
//			throw new DokArkivRestFunctionalException()
//		}
        else {
            return new ArkivSak()
                    .withArkivSakId(saksrelasjon.getSakId())
                    .withArkivSakSystem(ArkivSak.ArkivSakSystem.fromValue(mapFagsystemtoArkivsaksystem(saksrelasjon.getFagsystem())));
        }
    }

    private List<Bruker> mapBrukere(Set<no.nav.dokarkiv.core.domain.entities.Bruker> brukere) {
        if (brukere.isEmpty()) {
            return new ArrayList<>();
        } else {
            return brukere.stream().map(bruker -> new no.nav.dok.tjenester.journalfoerinngaaende.Bruker()
                    .withIdentifikator(bruker.getBrukerId())
                    .withBrukerType(Bruker.BrukerType.fromValue(bruker.getBrukerType().name())))
                    .collect(Collectors.toList());
        }
    }

    private Avsender mapAvsender(Journalpost journalpost) {
        if (journalpost.getAvsenderMottakerId() == null || journalpost.getAvsenderMottakerId().isEmpty()) {
            return null;
        } else {
            return new Avsender()
                    .withIdentifikator(journalpost.getAvsenderMottakerId())
                    .withAvsenderType(utledAvsenderType(journalpost.getAvsenderMottakerId()))
                    .withNavn(journalpost.getAvsenderMottaker());
        }
    }

    private List<Dokument> mapDokumenter(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner) {
        return journalpostDokumentInfoRelasjoner.stream()
                .map(relasjon -> new Dokument()
                        .withDokumentId(relasjon.getDokumentInfo().getDokumentInfoId().toString())
                        .withDokumentTypeId(relasjon.getDokumentInfo().getDokumenttypeId())
                        .withNavSkjemaId(relasjon.getDokumentInfo().getBrevkode())
                        .withTittel(relasjon.getDokumentInfo().getTittel())
                        .withDokumentKategori(relasjon.getDokumentInfo().getKategori() == null ? null : relasjon.getDokumentInfo()
                                .getKategori().name())
                        .withTilknyttetSom(Dokument.TilknyttetSom.fromValue(relasjon.getTilknyttetJournalpostSom().name()))
                        .withVariant(mapVarianter(relasjon.getDokumentInfo().getFildetaljerListe()))
                        .withLogiskVedleggListe(mapLogiskeVedlegg(relasjon.getDokumentInfo().getSkannetInnholdListe())))
                .collect(Collectors.toList());
    }

    private List<Variant> mapVarianter(Set<FilDetaljer> fildetaljer) {
        return fildetaljer.stream().map(filDetaljer -> new Variant()
                .withArkivFilType(Variant.ArkivFilType.fromValue(filDetaljer.getFiltype().name()))
                .withVariantFormat(Variant.VariantFormat.fromValue(filDetaljer.getVariantFormat().name())))
                .collect(Collectors.toList());

    }

    private List<LogiskVedlegg> mapLogiskeVedlegg(Set<SkannetInnhold> skannetInnholdSet) {
        return skannetInnholdSet.stream().map(skannetInnhold -> new LogiskVedlegg()
                .withLogiskVedleggId(skannetInnhold.getSkannetInnholdId() == null ? null : skannetInnhold.getSkannetInnholdId()
                        .toString())
                .withLogiskVedleggTittel(skannetInnhold.getVedleggInnhold()))
                .collect(Collectors.toList());
    }

    private Avsender.AvsenderType utledAvsenderType(String avsenderId) {
        if (avsenderId == null) {
            return null;
        } else if (avsenderId != null && avsenderId.length() == 11) {
            return Avsender.AvsenderType.PERSON;
        } else {
            return Avsender.AvsenderType.ORGANISASJON;
        }
    }

    private String mapFagsystemtoArkivsaksystem(FagsystemCode fagsystemCode) {
        if (fagsystemCode.equals(FagsystemCode.FS22)) {
            return ArkivsystemKode.GSAK.name();
        } else if (fagsystemCode.equals(FagsystemCode.PEN)) {
            return ArkivsystemKode.PSAK.name();
        } else {
            return fagsystemCode.name();
        }
    }

    private enum ArkivsystemKode {
        GSAK,
        PSAK
    }

}
