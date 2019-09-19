package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SafHentDokumentService {
	private final SafHentDokumentRepository safHentDokumentRepository;
	private final SafHentDokumentJoarkRepository safHentDokumentJoarkRepository;

	public SafHentDokumentService(SafHentDokumentRepository safHentDokumentRepository,
								  SafHentDokumentJoarkRepository safHentDokumentJoarkRepository) {
		this.safHentDokumentRepository = safHentDokumentRepository;
		this.safHentDokumentJoarkRepository = safHentDokumentJoarkRepository;
	}

	public SafHentDokumentResponse hentDokumentByDokumentinfoIdAndVariant(Long dokumentinfoId, VariantFormatCode variant) {
		return hentDokumentFromJoark(dokumentinfoId, variant);
	}

	private SafHentDokumentResponse hentDokumentFromJoark(Long dokumentinfoId, VariantFormatCode variant) {
		try {
			JoarkDokumentDto joarkDokumentDto = safHentDokumentRepository.hentDokumentFromJoark(dokumentinfoId, variant);
			if(joarkDokumentDto.isNormalDocument()) {
				return SafHentDokumentResponse.builder()
						.dokument(joarkDokumentDto.getDokument())
						.filtype(joarkDokumentDto.getFiltype())
						.build();
			} else if(joarkDokumentDto.isDlfDocument() || joarkDokumentDto.isOndemandDocument()) {
				byte[] ondemandDokument = safHentDokumentJoarkRepository.hentDokument(joarkDokumentDto);
				return SafHentDokumentResponse.builder()
						.dokument(ondemandDokument)
						.filtype(joarkDokumentDto.getFiltype())
						.build();
			} else {
				throw new DocumentNotFoundException("Fysisk dokument med dokumentinfoId=" + dokumentinfoId + " og variant=" + variant + " ikke funnet i Joark eller OnDemand.");
			}
		} catch (Exception e) {
			throw new DocumentNotFoundException("FilDetaljer med dokumentinfoId=" + dokumentinfoId + " og variant=" + variant + " ikke funnet.", e);
		}
	}
}