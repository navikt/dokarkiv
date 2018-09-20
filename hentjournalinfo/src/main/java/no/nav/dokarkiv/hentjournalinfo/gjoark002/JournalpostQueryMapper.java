package no.nav.dokarkiv.hentjournalinfo.gjoark002;

import static no.nav.dokarkiv.hentjournalinfo.gjoark001.DokumentInfoQueryMapper.mapDokumentInfo;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.Journalpost;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostQueryMapper {

    public static Journalpost mapJournalpost(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
        return Journalpost.builder()
                .journalpostId(journalpost.getJournalpostId())
                .tema(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name())
                .journalpostType(JournalpostType.mapFromJournalpostTypeCode(journalpost.getJournalposttype()))
                .journalpostStatus(JournalpostStatus.mapFromJournalStatusCode(journalpost.getJournalstatus()))
                .tittel(journalpost.getInnhold())
                .build();
    }

    public static List<JournalpostDokumentRelasjon> mapKnyttetDokumentList(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet, Long journalpostId) {
        return journalpostDokumentInfoRelasjonSet.stream()
                .filter(relasjon -> isNotTrue(relasjon.getDokumentInfo().getSlettet()))
                .map(relasjon -> JournalpostDokumentRelasjon.builder()
                        .tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon.getTilknyttetJournalpostSom()
                                .name())
                        .journalpostId(journalpostId)
                        .dokumentInfo(mapDokumentInfo(relasjon.getDokumentInfo()))
                        .dokumentInfoId(relasjon.getDokumentInfo().getDokumentInfoId()).build())
                .collect(Collectors.toList());
    }

    public static List<Journalpost.Bruker> mapBrukere(Set<Bruker> brukere) {
        return brukere.stream().map(bruker -> Journalpost.Bruker.builder()
                .brukerId(bruker.getBrukerId())
                .brukerType(bruker.getBrukerType() == null ? null : bruker.getBrukerType().name())
                .build()).collect(Collectors.toList());
    }
}
