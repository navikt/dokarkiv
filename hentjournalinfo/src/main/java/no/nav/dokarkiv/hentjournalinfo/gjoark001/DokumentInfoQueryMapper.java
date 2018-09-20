package no.nav.dokarkiv.hentjournalinfo.gjoark001;

import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapJournalpost;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentInfoQueryMapper {

    public static DokumentInfo mapDokumentInfo(no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo) {
        return DokumentInfo.builder()
                .dokumentInfoId(dokumentInfo.getDokumentInfoId())
                .status(dokumentInfo.getDokumentstatus() == null ? null : dokumentInfo.getDokumentstatus().name())
                .tittel(dokumentInfo.getTittel())
                .build();
    }

    public static List<JournalpostDokumentRelasjon> mapJournalpostDokumentRelasjon(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet, Long dokumentInfoId) {
        return journalpostDokumentInfoRelasjonSet.stream().map(relasjon -> JournalpostDokumentRelasjon.builder()
                .tilknyttetJournalpostSom(relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon.getTilknyttetJournalpostSom()
                        .name())
                .journalpostId(relasjon.getJournalpost().getJournalpostId())
                .journalpost(mapJournalpost(relasjon.getJournalpost())) //Like greit å bare mappe journalpost når den må hentes opp fra DB for å hente jpId (ref: LazyFetching)
                .dokumentInfoId(dokumentInfoId).build()).collect(Collectors.toList());
    }

    public static List<DokumentInfo.Fildetaljer> mapFildetaljer(Set<FilDetaljer> filDetaljerSet) {
        return filDetaljerSet.stream()
                .map(fildetaljer -> DokumentInfo.Fildetaljer.builder()
                        .fildetaljerId(fildetaljer.getFildetaljerId())
                        .filtype(fildetaljer.getFiltype() == null ? null : fildetaljer.getFiltype().name())
                        .variantFormat(fildetaljer.getVariantFormat() == null ? null : fildetaljer.getVariantFormat().name())
                        .build()).collect(Collectors.toList());
    }
}
