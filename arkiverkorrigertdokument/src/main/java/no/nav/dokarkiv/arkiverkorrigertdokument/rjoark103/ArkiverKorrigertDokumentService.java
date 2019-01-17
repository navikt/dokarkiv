package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;


import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;


@Service
public class ArkiverKorrigertDokumentService {

	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final BegrensningService begrensningService;

	@Inject
	public ArkiverKorrigertDokumentService(DokumentinfoRepository dokumentinfoRepository,
										   DokumentFilRepository dokumentFilRepository,
										   BegrensningService begrensningService) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.begrensningService = begrensningService;
	}

	public ArkiverKorrigertDokumentRespons arkiverKorrigertDokument(ArkiverKorrigertDokumentRequest request) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						request.getDokumentInfoId())));

		kanskjeSlettEksisterendeSladdetFilOgFilDetaljer(dokumentInfo);

		byte[] decodedFil = base64ToByte(request.getFil());
		lagreKorrigertDokumentSomSladdetVariantFormat(dokumentInfo, decodedFil);

		kanskjeOpprettBegrensingSkjermet(dokumentInfo);

		return ArkiverKorrigertDokumentRespons.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalpostId(dokumentInfo.getOriginalJournalpost() == null ? null : dokumentInfo.getOriginalJournalpost()
						.getJournalpostId())
				.tittel(dokumentInfo.getTittel())
				.build();
	}

	private void kanskjeOpprettBegrensingSkjermet(DokumentInfo dokumentInfo) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (filDetaljer.getBegrensning() == null) {
			begrensningService.setVariantSkjermet(dokumentInfo, VariantFormatCode.ARKIV, BegrensningTypeCode.POL);
		}
	}

	private void kanskjeSlettEksisterendeSladdetFilOgFilDetaljer(DokumentInfo dokumentInfo) {
		FilDetaljer sladdetFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		if (Objects.nonNull(sladdetFildetaljer)) {
			dokumentFilRepository.deleteByFilUuid(sladdetFildetaljer.getFilUuid());
			dokumentInfo.removeFilDetaljer(sladdetFildetaljer);
		}

	}

	private byte[] base64ToByte(String dokumentFilBase64) {
		return Base64.decodeBase64(dokumentFilBase64);
	}

	private void lagreKorrigertDokumentSomSladdetVariantFormat(DokumentInfo dokumentInfo, byte[] fil) {
		FilDetaljer arkivFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.filUuid(FilDetaljer.generateUuid())
				.filnavn(arkivFildetaljer.getFilnavn())
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.SLADDET)
				.fileContent(fil)
				.dokumentInfo(dokumentInfo)
				.build();
		filDetaljer.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
		dokumentInfo.addFilDetaljer(filDetaljer);

		dokumentFilRepository.save(filDetaljer.createDokumentFil());
		dokumentinfoRepository.save(dokumentInfo);
	}
}
