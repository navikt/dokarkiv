package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import javafx.util.Pair;
import no.nav.dokarkiv.core.repository.SafRepository;

import java.util.Base64;

public class SafHentDokumentService {

	private final SafRepository safRepository;

	public SafHentDokumentService(SafRepository safRepository) {
		this.safRepository = safRepository;
	}

	public SafHentDokumentResponse hentDokumentByDokumentinfoIdAndVariant(String dokumentinfoId, String variant) {
		Pair<Base64, String> dokumentAndType = safRepository.hentDokumentAndType(dokumentinfoId, variant);

		return SafHentDokumentResponse.builder()
				.dokument(dokumentAndType.)
				.filtype(dokumentAndType.)
				.build();
	}

	// Den skal gjøre mappinga
	// samt utforme kallet, slik at selve getMappingen kan være så enkel som mulig.
}
