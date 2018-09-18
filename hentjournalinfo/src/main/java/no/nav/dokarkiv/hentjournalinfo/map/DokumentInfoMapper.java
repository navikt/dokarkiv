package no.nav.dokarkiv.hentjournalinfo.map;

import no.nav.dokarkiv.hentjournalinfo.dto.DokumentInfo;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentInfoMapper {

    public static DokumentInfo mapDokumentInfo(no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo) {
        return DokumentInfo.builder()
                .dokumentInfoId(dokumentInfo.getDokumentInfoId())
                .status(dokumentInfo.getDokumentstatus() == null ? null : dokumentInfo.getDokumentstatus().name())
                .tittel(dokumentInfo.getTittel())
                .slettet(dokumentInfo.getSlettet())
                .build();
    }
}
