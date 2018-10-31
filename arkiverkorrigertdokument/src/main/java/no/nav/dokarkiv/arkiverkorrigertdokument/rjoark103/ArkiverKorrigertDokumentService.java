package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverkorrigertdokument.exception.VariantFormatCodeException;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;

@Slf4j
@Service
public class ArkiverKorrigertDokumentService {

	private final ArkiverKorrigertDokumentValidator validator;
	private final DokumentinfoRepository dokumentinfoRepository;
//	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public ArkiverKorrigertDokumentService(
			ArkiverKorrigertDokumentValidator validator,
			DokumentinfoRepository dokumentinfoRepository) {
		this.validator = validator;
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public String arkiverKorrigertDokument(ArkiverKorrigertDokumentRequestTo requestTo) {
		DokumentInfo dokumentInfo = dokumentinfoRepository.
				findDokumentInfoByJournalpostIdAndDokumentInfoId(requestTo.getJournalpostId(), requestTo.getDokumentInfoId())
				.orElse(null);

		Set<FilDetaljer> currentFilDetaljerSet = dokumentInfo.getFildetaljerListe();
		FilDetaljer skalBliOriginal = null;

		for (FilDetaljer filDetaljer : currentFilDetaljerSet) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.ARKIV)) {
				skalBliOriginal = filDetaljer;
			}
		}

		arkiverKorrigertDokumentSomVariantFormatArkiv(dokumentInfo, requestTo);

		validator.validerArkiverKorrigertDokument(dokumentInfo, requestTo);

		return "returString";
	}


	//TODO: Sett inn riktig logikk her. Original blir brukt for xmlinput
	private void arkiverKorrigertDokumentSomVariantFormatArkiv(DokumentInfo dokumentInfo, ArkiverKorrigertDokumentRequestTo requestTo) {
		FilDetaljer skalBliOriginal = hentFildetaljerForArkivVariant(dokumentInfo);
//		FilDetaljer skalBliArkiv = skalBliOriginal;

//		skalBliOriginal.
//
//
//		skalBliOriginal.setVariantFormat(VariantFormatCode.ORIGINAL);
//
	}

	private FilDetaljer hentFildetaljerForArkivVariant(DokumentInfo dokumentInfo) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat().equals(VariantFormatCode.ARKIV)) {
				return filDetaljer;
			}
		}
		throw new VariantFormatCodeException(
				String.format(MDC.get(MDCConstants.MDC_REQUEST_ID) + " kan ikke arkivere korrigert filinnehold på dokument som " +
								"mangler arkivert filinnehold. dokumentInfoId=%s",
						dokumentInfo.getDokumentInfoId()));
	}

	private FilDetaljer opprettFildetaljerForNyArkivVariant(DokumentInfo dokumentInfo, ArkiverKorrigertDokumentRequestTo requestTo) {
		return FilDetaljer.builder()

				.build();
	}

}
