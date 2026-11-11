package no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.FeiledeDokumenter;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.ShallowDokumentInfoCopier;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.journalpost.v1.services.tilknyttvedlegg.TilknyttVedleggService.TILLEGGOPPLYSNINGER_KEY;

public class TilknyttVedleggMapper {

	private static final ShallowDokumentInfoCopier DOKUMENT_INFO_COPIER = new ShallowDokumentInfoCopier();

	public static DokumentInfo mapDokumentInfoSladdet(DokumentInfo dokumentInfo, String consumerId) {

		DokumentInfo dokumentInfoCopy = DOKUMENT_INFO_COPIER.copy(dokumentInfo);
		dokumentInfoCopy.setOpprettetKildeNavn(consumerId);
		dokumentInfoCopy.setEndretAvNavn(null);
		dokumentInfoCopy.getTilleggsopplysninger().put(TILLEGGOPPLYSNINGER_KEY, dokumentInfo.getDokumentInfoId().toString());

		return dokumentInfoCopy;
	}

	public static FilDetaljer createFildetaljerCopy(FilDetaljer filDetaljer, DokumentInfo dokumentInfo, String consumerId) {
		FilDetaljer filDetaljerCopy = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(filDetaljer.getFileContent())
				.filUuid(FilDetaljer.generateUuid())
				.onDemandId(filDetaljer.getOnDemandId())
				.onDemandInstans(filDetaljer.getOnDemandInstans())
				.metaforceInstanceId(filDetaljer.getMetaforceInstanceId())
				.filtype(filDetaljer.getFiltype())
				.variantFormat(ARKIV)
				.batchNavn(filDetaljer.getBatchNavn())
				.filnavn(filDetaljer.getFilnavn())
				.filstorrelse(filDetaljer.getFilstorrelse())
				.build();

		filDetaljerCopy.setOpprettetKildeNavn(consumerId);

		return filDetaljerCopy;
	}

	public static void addToFeiletDokumentList(List<FeiledeDokumenter> feiledeDokumenterList, ArsakKode arsakKode, DokumentVedlegg dokumentVedlegg) {
		feiledeDokumenterList.add(FeiledeDokumenter.builder()
				.kildeJournalpostId(dokumentVedlegg.getKildeJournalpostId().toString())
				.dokumentInfoId(dokumentVedlegg.getDokumentInfoId())
				.arsakKode(arsakKode)
				.build());
	}

	public static FilDetaljer finnSladdetFildetaljer(DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			return dokumentInfo.getFildetaljerListe().stream()
					.filter(filDetaljer1 -> SLADDET.equals(filDetaljer1.getVariantFormat()))
					.findAny()
					.orElse(null);
		} else {
			return null;
		}
	}

	public static FilDetaljer finnArkivFildetaljer(DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			return dokumentInfo.getFildetaljerListe().stream()
					.filter(filDetaljer1 -> ARKIV.equals(filDetaljer1.getVariantFormat()))
					.findAny()
					.orElse(null);
		} else {
			return null;
		}
	}
}
