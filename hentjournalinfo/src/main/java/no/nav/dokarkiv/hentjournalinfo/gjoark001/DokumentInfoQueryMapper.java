package no.nav.dokarkiv.hentjournalinfo.gjoark001;

import static no.nav.dokarkiv.hentjournalinfo.gjoark002.JournalpostQueryMapper.mapJournalpost;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;
import no.nav.dokarkiv.hentjournalinfo.dto.JournalpostDokumentRelasjon;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.DokumentStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.FilType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;

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
                .dokumentStatus(DokumentStatus.mapFromDokumentStatusCode(dokumentInfo.getDokumentstatus()))
                .tittel(dokumentInfo.getTittel())
                .build();
    }

    public static List<JournalpostDokumentRelasjon> mapKnyttetJournalpostList(Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjonSet, Long dokumentInfoId, List<Long> begrensetJournalpostRelasjon, List<Long> begrensetJournalpost) {
        return journalpostDokumentInfoRelasjonSet.stream().map(relasjon -> JournalpostDokumentRelasjon.builder()
                .tilknyttetJournalpostSom(TilknyttetJournalpostSom.mapTilknyttetJournalpostSomCode(relasjon.getTilknyttetJournalpostSom()))
                .journalpostId(relasjon.getJournalpost().getJournalpostId())
                .slettet(begrensetJournalpostRelasjon.contains(relasjon.getJournalpost().getJournalpostId()))
                .journalpost(mapJournalpost(relasjon.getJournalpost(), begrensetJournalpost.contains(relasjon.getJournalpost()
                        .getJournalpostId()))) //Like greit å bare mappe journalpost når den må hentes opp fra DB for å hente jpId (ref: LazyFetching)
                .dokumentInfoId(dokumentInfoId).build()).collect(Collectors.toList());
    }

    public static List<DokumentInfo.Fildetaljer> mapFildetaljer(Set<FilDetaljer> filDetaljerSet) {
        return filDetaljerSet.stream()
                .map(fildetaljer -> DokumentInfo.Fildetaljer.builder()
                        .fildetaljerId(fildetaljer.getFildetaljerId())
                        .filtype(FilType.mapFromFilTypeCode(fildetaljer.getFiltype()))
                        .variantFormat(VariantFormat.mapFromVariantFormatCode(fildetaljer.getVariantFormat()))
                        .build()).collect(Collectors.toList());
    }


}
