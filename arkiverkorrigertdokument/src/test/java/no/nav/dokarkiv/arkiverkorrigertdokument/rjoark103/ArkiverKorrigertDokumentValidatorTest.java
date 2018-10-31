package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.createRequest;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ArkiverKorrigertDokumentValidatorTest {

	@InjectMocks
	private ArkiverKorrigertDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldValidateSetVariantArkivSomOriginal() {
		ArkiverKorrigertDokumentRequestTo requestTo = createRequest();
		FilDetaljer arkivFildetaljer = FilDetaljer.builder()
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.filUuid(FilDetaljer.generateUuid())
				.build();

		FilDetaljer originalFildetaljer = FilDetaljer.builder()
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ORIGINAL)
				.filUuid(FilDetaljer.generateUuid())
				.build();

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(arkivFildetaljer);
		filDetaljerSet.add(originalFildetaljer);

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.fildetaljerListe(filDetaljerSet)
				.build();


		validator.validerAtVariantFormatCodeEndresFraArkivTilOriginal(dokumentInfo, requestTo);
	}

	@Test
	public void shouldValidateSetInputBinaerFilSomVariantArkiv() {

	}


}
