package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

import static org.apache.commons.io.IOUtils.toByteArray;

@Component
@Slf4j
public class SafHentDokumentService {
	private final SafHentDokumentRepository safHentDokumentRepository;

	public SafHentDokumentService(SafHentDokumentRepository safHentDokumentRepository) {
		this.safHentDokumentRepository = safHentDokumentRepository;
	}

	public SafHentDokumentResponse hentDokumentByDokumentinfoIdAndVariant(Long dokumentinfoId, VariantFormatCode variant) {
		try {
			return hentDokumentFromJoark(dokumentinfoId, variant);
		} catch (Exception e) {
			log.warn("Kunne ikke hente fysisk dokument med dokumentInfoId={}, variantformat={}", dokumentinfoId, variant, e);
			throw new DocumentNotFoundException("FilDetaljer med dokumentinfoId=" + dokumentinfoId + " og variant=" + variant + " ikke funnet.", e);
		}
	}

	private SafHentDokumentResponse hentDokumentFromJoark(Long dokumentinfoId, VariantFormatCode variant) {
		JoarkDokumentDto joarkDokumentDto = safHentDokumentRepository.hentDokumentFromJoark(dokumentinfoId, variant);
		if (joarkDokumentDto.isNormalDocument()) {
			try {
				return SafHentDokumentResponse.builder()
						.dokument(toByteArray(joarkDokumentDto.getDokument().getBinaryStream()))
						.filtype(joarkDokumentDto.getFiltype())
						.build();
			} catch (IOException | SQLException e) {
				throw new RuntimeException("Klarte ikke å lese dokument fra binaryStream. dokumentInfoId=" + dokumentinfoId + ", variantFormat=" + variant, e);
			}
		} else if (joarkDokumentDto.isOndemandDocument()) {
			log.warn("Dokument med dokumentInfoId={}, variantformat={} har OnDemandId={} og finnes ikke i fagarkivet", dokumentinfoId, variant, joarkDokumentDto.getOndemandId());
			throw new DocumentNotFoundException("Fysisk dokument med dokumentinfoId=" + dokumentinfoId + " og variant=" + variant + " ikke funnet i joark");
		} else {
			throw new DocumentNotFoundException("Fysisk dokument med dokumentinfoId=" + dokumentinfoId + " og variant=" + variant + " ikke funnet i joark");
		}
	}
}