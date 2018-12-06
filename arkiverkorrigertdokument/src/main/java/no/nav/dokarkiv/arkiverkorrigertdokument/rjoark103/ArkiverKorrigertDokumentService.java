package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class ArkiverKorrigertDokumentService {

	private final DokumentinfoRepository dokumentinfoRepository;

	@Inject
	public ArkiverKorrigertDokumentService(DokumentinfoRepository dokumentinfoRepository) {
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public String arkiverKorrigertDokument(ArkiverKorrigertDokumentRequestTo requestTo) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(requestTo.getDokumentInfoId()).orElse(null);

		if (dokumentInfo == null) {
			throw new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
					requestTo.getDokumentInfoId()));
		}

//		byte[] decodedBody = decodeBodyInBase64(requestTo.getBody());
		lagreKorrigertDokumentSomSladdetVariantFormat(dokumentInfo, requestTo.getBody());

		log.info("{} har arkivert korrigert dokument med journalpostId={} og dokumentInfoId={}",
				MDC.get(MDCConstants.MDC_REQUEST_ID), requestTo.getJournalpostId(), requestTo.getDokumentInfoId());
		return "Success!";
	}

	private byte[] decodeBodyInBase64(byte[] body) {
		return Base64.getDecoder().decode(new String(body).getBytes(StandardCharsets.UTF_8));
	}

	private void lagreKorrigertDokumentSomSladdetVariantFormat(DokumentInfo dokumentInfo, byte[] body) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.filUuid(FilDetaljer.generateUuid())
				.filtype(FilTypeCode.PDF) // ELLER ER DET PDFA HER, for sladdet er vel alltid PDF
				.variantFormat(VariantFormatCode.SLADDET)
				.fileContent(body)
				.dokumentInfo(dokumentInfo)
				.build();
		filDetaljer.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_REQUEST_ID)); // Er dette system / funksjon som har opprettet dataposten?
//		filDetaljer.setFileContent(body);
		filDetaljer.createDokumentFil();
		dokumentInfo.addFilDetaljer(filDetaljer);

		dokumentinfoRepository.save(dokumentInfo);
	}
}
