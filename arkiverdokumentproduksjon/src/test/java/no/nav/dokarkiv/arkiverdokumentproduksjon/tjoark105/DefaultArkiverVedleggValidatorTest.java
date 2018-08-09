package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDocumentUpdateException;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultArkiverVedleggValidatorTest {


	private static final long JOURNALPOST_ID = 11L;
	private static final String ENDRET_AV_NAVN = "Endre Tavnavn";
	private static final String DOKUMENT_TYPE_ID = "T01";
	private static final String TITTEL = "Tittel";
	private static final String BREVKODE = "BK";

	ArkiverVedleggValidator validator = new DefaultArkiverVedleggValidator();

	@Test
	public void shouldValidateOk() {
		ArkiverVedleggRequestTo request = createRequest();
		validator.validate(request);
	}

	@Test(expected = NoJournalpostFoundException.class)
	public void shouldNotValidateIfJournalpostIsNull() throws NoJournalpostFoundException {
		validator.validate(null, 1L);
	}

	@Test(expected = IllegalDocumentUpdateException.class)
	public void shouldNotValidateIfJournalpostIsFerdigstilt() throws NoJournalpostFoundException {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalstatus(JournalStatusCode.FL);
		validator.validate(journalpost, 1L);
	}

	@Test(expected = IllegalDocumentUpdateException.class)
	public void shouldNotValidateIfJournalpostIsNotUnderProduksjon() throws NoJournalpostFoundException {
		Journalpost journalpost = new Journalpost();
		journalpost.setJournalstatus(JournalStatusCode.A);
		validator.validate(journalpost, 1L);
	}

	@Test(expected = ApplicationException.class)
	public void shouldNotValidateIfMissingParameter() {
		ArkiverVedleggRequestTo request = createRequest();
		request.getDokumentInfo().setTittel("");
		validator.validate(request);
	}

	private ArkiverVedleggRequestTo createRequest() {
		DokumentInfo dokInfo = DokumentInfoBuilder.getDokumentInfoBuilder()
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.kategori(DokumentKategoriCode.B)
				.tittel(TITTEL)
				.brevkode(BREVKODE)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.sensitivt(false)
				.build();
		FilDetaljer filDetaljer = FilDetaljerBuilder.getFilDetaljerBuilder()
				.variantFormat(VariantFormatCode.ARKIV)
				.filtype(FilTypeCode.PDF)
				.fileContent(new byte[]{'1', '2', '3'})
				.build();
		dokInfo.addFilDetaljer(filDetaljer);

		ArkiverVedleggRequestTo request = new ArkiverVedleggRequestTo();
		request.setJournalpostId(JOURNALPOST_ID);
		request.setEndretAvNavn(ENDRET_AV_NAVN);
		request.setFerdigstillDokument(true);
		request.setDokumentInfo(dokInfo);
		return request;
	}


}