package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SafHentDokumentService {

	private final HentDokumentRepository hentDokumentRepository;

	public SafHentDokumentService(HentDokumentRepository hentDokumentRepository) {
		this.hentDokumentRepository = hentDokumentRepository;
	}

	public SafHentDokumentResponse hentDokumentByDokumentinfoIdAndVariant(Long dokumentinfoId, VariantFormatCode variant) {
		SafHentDokumentDto safHentDokumentDto = new SafHentDokumentDto(null, null);
		try {
			safHentDokumentDto = hentDokumentRepository.queryForDokumentAndDokumenttype(dokumentinfoId, variant);
		} catch (Exception e) {
			log.warn("Dokument med dokumentinfoId={}, variant={} ikke funnet.", dokumentinfoId, variant);
			throw new DocumentNotFoundException("Dokument med dokumentinfoId=" + dokumentinfoId.toString() + " og  variant=" + variant.toString() + " ikke funnet.", e);
		}

		return SafHentDokumentResponse.builder()
				.dokument(safHentDokumentDto.getDokument())
				.filtype(safHentDokumentDto.getVariantFormat())
				.build();


	}
}

