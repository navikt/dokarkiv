package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Slf4j
@Service
public class ArkiverKorrigertDokumentService {

	private final ArkiverKorrigertDokumentValidator validator;
//	private final DokumentinfoRepository dokumentinfoRepository;
//	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public ArkiverKorrigertDokumentService(
			ArkiverKorrigertDokumentValidator validator) {
		this.validator = validator;
	}

	public String arkiverKorrigertDokument(ArkiverKorrigertDokumentRequestTo requestTo) {

		return "returString";
	}
}
