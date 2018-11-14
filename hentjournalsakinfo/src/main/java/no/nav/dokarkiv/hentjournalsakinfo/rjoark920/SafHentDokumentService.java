package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.repository.SafHentDokumentDto;
import no.nav.dokarkiv.core.repository.SafRepository;
import org.springframework.stereotype.Component;

@Component
public class SafHentDokumentService {

	private final SafRepository safRepository;

	public SafHentDokumentService(SafRepository safRepository) {
		this.safRepository = safRepository;
	}

	public SafHentDokumentResponse hentDokumentByDokumentinfoIdAndVariant(Long dokumentinfoId, VariantFormatCode variant) {
		SafHentDokumentDto safHentDokumentTo = safRepository.queryForDokumentAndDokumenttype(dokumentinfoId, variant);

		return SafHentDokumentResponse.builder()
				.dokument(safHentDokumentTo.getDokument())
				.filtype(safHentDokumentTo.getDokumentVariant())
				.build();
	}
}

