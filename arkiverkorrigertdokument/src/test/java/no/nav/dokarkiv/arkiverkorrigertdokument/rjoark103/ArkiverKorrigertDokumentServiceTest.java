package no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103;

import static no.nav.dokarkiv.arkiverkorrigertdokument.util.TestUtils.createRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class ArkiverKorrigertDokumentServiceTest {

	@InjectMocks
	private ArkiverKorrigertDokumentService service;

	@InjectMocks
	private ArkiverKorrigertDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldSetBinaryFileToArkivVariantFormat() {
		when(service.arkiverKorrigertDokument(any(ArkiverKorrigertDokumentRequestTo.class))).thenReturn("Ok");

		ArkiverKorrigertDokumentRequestTo requestTo = createRequest();
		FilDetaljer produksjonFildetaljer = FilDetaljer.builder()
				.fildetaljerId(2L)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.filUuid(FilDetaljer.generateUuid())
				.fileContent(requestTo.getBinaerFil())
				.build();
		FilDetaljer arkivFildetaljer = FilDetaljer.builder()
				.fildetaljerId(1L)
				.variantFormat(VariantFormatCode.ARKIV)
				.filUuid(FilDetaljer.generateUuid())
				.fileContent("OriginalArkivBinaerFil".getBytes())
				.build();

		Set<FilDetaljer> filDetaljerSet = new HashSet<>();
		filDetaljerSet.add(arkivFildetaljer);
		filDetaljerSet.add(produksjonFildetaljer);

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.fildetaljerListe(filDetaljerSet)
				.build();

		String returStreng = service.arkiverKorrigertDokument(requestTo);
	}
}
