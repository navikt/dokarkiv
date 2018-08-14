package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import static no.nav.dokarkiv.core.util.FilTypeMapper.mapFiltype;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of ArkiverVedleggRequestMapper
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
@Component
public class ArkiverVedleggRequestMapper {

	public ArkiverVedleggRequestTo map(ArkiverVedleggRequest arkiverVedleggRequest) {
		Journalpost journalpost = arkiverVedleggRequest.getJournalpost();
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkivervedlegg.DokumentInfo dokumentInfo = journalpost
				.getDokumentInfo();

		DokumentInfo domainDokumentInfo = DokumentInfo.builder()
				.kategori(dokumentInfo.getKategori() == null ? null : DokumentKategoriCode.valueOf(dokumentInfo.getKategori()))
				.tittel(dokumentInfo.getTittel())
				.brevkode(dokumentInfo.getBrevkode())
				.dokumenttypeId(dokumentInfo.getDokumentTypeId())
				.sensitivt(dokumentInfo.isSensitivt())
				.build();
		dokumentInfo.getFildetaljer().forEach(fildetaljer -> domainDokumentInfo.addFilDetaljer(FilDetaljer.builder()
				.filtype(fildetaljer.getFiltype() == null ? null : FilTypeCode.valueOf(mapFiltype(fildetaljer.getFiltype())))
				.variantFormat(fildetaljer.getVariantformat() == null ? null : VariantFormatCode.valueOf(fildetaljer
						.getVariantformat()))
				.fileContent(fildetaljer.getIkkeRedigerbartDokument())
				.filUuid(UUID.randomUUID().toString())
				.build()));
		return ArkiverVedleggRequestTo.builder()
				.journalpostId(journalpost.getJournalpostId() == null ? null : Long.valueOf(journalpost.getJournalpostId()))
				.endretAvNavn(journalpost.getEndretAvNavn())
				.ferdigstillDokument(arkiverVedleggRequest.isFerdigstillDokument())
				.dokumentInfo(domainDokumentInfo)
				.build();
	}
}
