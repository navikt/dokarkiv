package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.springframework.stereotype.Component;

@Component
public class ArkiverKorrigertDokumentValidator {

	public void validerArkiverKorrigertDokument(DokumentInfo dokumentInfo, ArkiverKorrigertDokumentRequestTo requestTo) {

	}

	public void validerDokumentFil(DokumentFil dokumentFil, ArkiverKorrigertDokumentRequestTo requestTo) {

	}


	public static void validerAtVariantFormatCodeEndresFraArkivTilOriginal(DokumentInfo dokumentInfo, ArkiverKorrigertDokumentRequestTo requestTo) {


	}

	public void temp(FilDetaljer skalBliOriginal, FilDetaljer skalBliArkiv) {
		if (isTrue(skalBliOriginal.equals(skalBliArkiv))) {
			System.out.println("OK");
		}
	}

}
