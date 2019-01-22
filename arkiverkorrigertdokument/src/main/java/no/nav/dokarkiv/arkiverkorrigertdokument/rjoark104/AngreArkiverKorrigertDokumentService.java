package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark104;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.exception.VariantFormatNotFoundException;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRespons;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class AngreArkiverKorrigertDokumentService {

	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final BegrensningService begrensningService;

	@Inject
	public AngreArkiverKorrigertDokumentService(
			DokumentinfoRepository dokumentinfoRepository,
			DokumentFilRepository dokumentFilRepository,
			BegrensningService begrensningService) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.begrensningService = begrensningService;
	}

	public ArkiverKorrigertDokumentRespons angreArkiverKorrigertDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						dokumentInfoId)));

		sjekkAtArkivVariantAvDokumentErSkjermet(dokumentInfo);
		sjekkAtSladdetVariantAvDokumentFinnes(dokumentInfo);

		slettBegrensning(dokumentInfo);
		slettSladdetFilOgFilDetaljer(dokumentInfo);

		return ArkiverKorrigertDokumentRespons.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalpostId(dokumentInfo.getOriginalJournalpost() == null ? null : dokumentInfo.getOriginalJournalpost()
						.getJournalpostId())
				.tittel(dokumentInfo.getTittel())
				.build();
	}

	private void sjekkAtSladdetVariantAvDokumentFinnes(DokumentInfo dokumentInfo) {
		if (isNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET))) {
			throw new VariantFormatNotFoundException(String.format("Kan ikke finne %s variantFormat for dokument med dokumentInfoId=%s",
					VariantFormatCode.SLADDET,
					dokumentInfo.getDokumentInfoId()));
		}
	}


	private void sjekkAtArkivVariantAvDokumentErSkjermet(DokumentInfo dokumentInfo) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (!BegrensningTypeCode.POL.equals(filDetaljer.getBegrensning())) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Korrigering av dokumentet kan ikke oppheves fordi dokument med dokumentInfoId=%s og variantFormat=%s " +
							"ikke er begrenset som et %s dokument.",
					dokumentInfo.getDokumentInfoId(),
					VariantFormatCode.ARKIV,
					SkjermingTypeCode.POL));
		}
	}

	private void slettBegrensning(DokumentInfo dokumentInfo) {
		begrensningService.setVariantSkjermet(dokumentInfo, VariantFormatCode.ARKIV, null);
	}

	private void slettSladdetFilOgFilDetaljer(DokumentInfo dokumentInfo) {
		FilDetaljer sladdetFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		dokumentInfo.removeFilDetaljer(sladdetFildetaljer);
		dokumentFilRepository.deleteByFilUuid(sladdetFildetaljer.getFilUuid());
	}

}
