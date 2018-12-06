package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;


import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Objects;

@Slf4j
@Service
public class ArkiverKorrigertDokumentService {

	private final DokumentinfoRepository dokumentinfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public ArkiverKorrigertDokumentService(DokumentinfoRepository dokumentinfoRepository, DokumentFilRepository dokumentFilRepository, BegrensningRepository begrensningRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.begrensningRepository = begrensningRepository;
	}

	public ArkiverKorrigertDokumentRespons arkiverKorrigertDokument(ArkiverKorrigertDokumentRequest requestTo) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(requestTo.getDokumentInfoId())
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						requestTo.getDokumentInfoId())));


		kanskjeSlettEksisterendeSladdetFilOgFilDetaljer(dokumentInfo);

		byte[] decodedFil = decodeBodyInBase64(requestTo.getFil());
		lagreKorrigertDokumentSomSladdetVariantFormat(dokumentInfo, decodedFil);

		kanskjeOpprettBegrensingSkjermet(dokumentInfo);

		log.info("{} har arkivert korrigert dokument med dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getDokumentInfoId());
		return ArkiverKorrigertDokumentRespons.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.journalpostId(dokumentInfo.getOriginalJournalpost() == null ? null : dokumentInfo.getOriginalJournalpost()
						.getJournalpostId())
				.tittel(dokumentInfo.getTittel())
				.build();
	}

	private void kanskjeOpprettBegrensingSkjermet(DokumentInfo dokumentInfo) {
		boolean begrengsningExists = begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV, BegrensningTypeCode.SKJERMET)
				.isPresent();
		if (isFalse(begrengsningExists)) {
			Begrensning begrensning = Begrensning.builder()
					.journalpostId(dokumentInfo.getOriginalJournalpost().getJournalpostId())
					.dokumentInfoId(dokumentInfo.getDokumentInfoId())
					.begrensningType(BegrensningTypeCode.SKJERMET)
					.variantFormat(VariantFormatCode.ARKIV)
					.build();
			begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
			begrensningRepository.save(begrensning);
		}
	}

	private void kanskjeSlettEksisterendeSladdetFilOgFilDetaljer(DokumentInfo dokumentInfo) {
		FilDetaljer sladdetFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		if (Objects.nonNull(sladdetFildetaljer)) {
			dokumentFilRepository.deleteByFilUuid(sladdetFildetaljer.getFilUuid());
			dokumentInfo.removeFilDetaljer(sladdetFildetaljer);
		}
	}

	private byte[] decodeBodyInBase64(String dokumentFilBase64) {
		return Base64.decodeBase64(dokumentFilBase64);
	}

	private void lagreKorrigertDokumentSomSladdetVariantFormat(DokumentInfo dokumentInfo, byte[] fil) {
		FilDetaljer arkivFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.filUuid(FilDetaljer.generateUuid())
				.filnavn(arkivFildetaljer.getFilnavn())
				.filtype(arkivFildetaljer.getFiltype())
				.variantFormat(VariantFormatCode.SLADDET)
				.fileContent(fil)
				.dokumentInfo(dokumentInfo)
				.build();
		filDetaljer.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID)); // Er dette system / funksjon som har opprettet dataposten?

		dokumentInfo.addFilDetaljer(filDetaljer);

		dokumentFilRepository.save(filDetaljer.createDokumentFil());
		dokumentinfoRepository.save(dokumentInfo);
	}
}
