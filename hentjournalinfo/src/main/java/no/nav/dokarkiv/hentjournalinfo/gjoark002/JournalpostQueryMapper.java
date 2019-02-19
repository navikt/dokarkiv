package no.nav.dokarkiv.hentjournalinfo.gjoark002;

import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapDokumentInfo;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.BrukerType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.Tema;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostQueryMapper {

    public static Journalpost mapJournalpost(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost, boolean isBegrenset) {
        return Journalpost.builder()
                .journalpostId(journalpost.getJournalpostId())
                .tema(Tema.mapFromFagomradeCode(journalpost.getFagomrade()))
                .journalpostType(JournalpostType.mapFromJournalpostTypeCode(journalpost.getJournalposttype()))
                .journalpostStatus(JournalpostStatus.mapFromJournalStatusCode(journalpost.getJournalstatus()))
                .tittel(journalpost.getInnhold())
                .sakId(journalpost.getSaksrelasjon() == null ? null : journalpost.getSaksrelasjon().getSakId())
                .slettet(isBegrenset)
                .build();
    }

    public static List<JournalpostDokumentRelasjon> mapKnyttetDokumentList(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet, Long journalpostId, List<Long> begrensetDokumentInfoRelasjon) {
        return journalpostDokumentInfoRelasjonSet.stream()
                .map(relasjon -> JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.mapTilknyttetJournalpostSomCode(relasjon.getTilknyttetJournalpostSom()))
                        .journalpostId(journalpostId)
                        .dokumentInfo(mapDokumentInfo(relasjon.getDokumentInfo()))
                        .slettet(begrensetDokumentInfoRelasjon.contains(relasjon.getDokumentInfo().getDokumentInfoId()))
                        .dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId()).build())
                .collect(Collectors.toList());
    }

    public static List<Journalpost.Bruker> mapBrukere(Set<Bruker> brukere) {
        return brukere.stream().map(bruker -> Journalpost.Bruker.builder()
                .brukerId(bruker.getBrukerId())
                .brukerType(BrukerType.mapFromBrukerTypeCode(bruker.getBrukerType()))
                .build()).collect(Collectors.toList());
    }

}
