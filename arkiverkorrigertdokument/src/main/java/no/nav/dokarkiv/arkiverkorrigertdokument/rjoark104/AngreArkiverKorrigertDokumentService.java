package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark104;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.exception.VariantFormatCodeNotFoundException;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRequest;
import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRespons;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;

@Slf4j
@Service
public class AngreArkiverKorrigertDokumentService {

	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public AngreArkiverKorrigertDokumentService(
			DokumentinfoRepository dokumentinfoRepository,
			DokumentFilRepository dokumentFilRepository,
			BegrensningRepository begrensningRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.begrensningRepository = begrensningRepository;
	}

	public ArkiverKorrigertDokumentRespons angreArkiverKorrigertDokument(ArkiverKorrigertDokumentRequest request) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						request.getDokumentInfoId())));

		sjekkAtArkivVariantAvDokumentErSkjermet(dokumentInfo.getDokumentInfoId());
		sjekkAtSladdetVariantAvDokumentFinnes(dokumentInfo);

		slettBegrensning(dokumentInfo);
		slettSladdetFilOgFilDetaljer(dokumentInfo);
		log.info(MDC.get(MDCConstants.MDC_REQUEST_ID) +
				" har angret arkivering av korrigert dokument med dokumentInfoId={}", dokumentInfo.getDokumentInfoId());

		return ArkiverKorrigertDokumentRespons.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalpostId(dokumentInfo.getOriginalJournalpost() == null ? null : dokumentInfo.getOriginalJournalpost()
						.getJournalpostId())
				.tittel(dokumentInfo.getTittel())
				.build();
	}

	private void sjekkAtSladdetVariantAvDokumentFinnes(DokumentInfo dokumentInfo) {
		if (isNull(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET))) {
			throw new VariantFormatCodeNotFoundException(String.format("Kan ikke finne %s variantFormat for dokument med dokumentInfoId=%s",
					VariantFormatCode.SLADDET,
					dokumentInfo.getDokumentInfoId()));
		}
	}


	private void sjekkAtArkivVariantAvDokumentErSkjermet(Long dokumentInfoId) {
		if (isFalse(begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(
				dokumentInfoId, VariantFormatCode.ARKIV, BegrensningTypeCode.SKJERMET).isPresent())) {
			throw new BegrensningIkkeFunnetException(String.format(
					"Korrigering av dokumentet kan ikke oppheves fordi dokument med dokumentInfoId=%s og variantFormat=%s " +
							"ikke er begrenset som et %s dokument.",
					dokumentInfoId,
					VariantFormatCode.ARKIV,
					BegrensningTypeCode.SKJERMET));
		}
	}

	private void slettBegrensning(DokumentInfo dokumentInfo) {
		begrensningRepository.deleteByDokumentInfoIdAndBegrensningType(dokumentInfo.getDokumentInfoId(), BegrensningTypeCode.SKJERMET);
	}

	private void slettSladdetFilOgFilDetaljer(DokumentInfo dokumentInfo) {
		FilDetaljer sladdetFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		if (Objects.nonNull(sladdetFildetaljer)) {
			dokumentInfo.removeFilDetaljer(sladdetFildetaljer);
			dokumentFilRepository.deleteByFilUuid(sladdetFildetaljer.getFilUuid());
		}
	}

}
