package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.Base64;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Rjoark920IT extends AbstractHentjournalsakinfoItest {
	private static final String HENTJOURNALSAKINFO_HENTDOKUMENT = "/hentjournalsakinfo/hentdokument/";
	private static final String UUID = FilDetaljer.generateUuid();
	private static final String TEST_FILE_CONTENT = "testfilecontent";

	// Happy path
	@Test
	public void shouldGetBase64Document() {
		Journalpost journalpost = buildAndPersistJournalpost();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0).getDokumentInfoId();
		VariantFormatCode variantFormat = VariantFormatCode.ARKIV;

		String uri = HENTJOURNALSAKINFO_HENTDOKUMENT + dokumentInfoId + "/" + variantFormat.toString();
		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertThat(new String(Base64.getDecoder().decode(responseEntity.getBody())), is(TEST_FILE_CONTENT));
		assertThat(responseEntity.getHeaders().getContentType(), is(MediaType.APPLICATION_PDF));
	}

	//  Unhappy path
	@Test
	public void shouldFailToGetDocument() throws Exception {
		String uri = HENTJOURNALSAKINFO_HENTDOKUMENT + "123456789/ARKIV";

		ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, createHeaderEntity(), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	private Journalpost buildAndPersistJournalpost() {
		Journalpost journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.fagomrade(FagomradeCode.FOR)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
										.opprettetKildeNavn("test")
										.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
												.filUuid(UUID)
												.filtype(FilTypeCode.PDF)
												.variantFormat(VariantFormatCode.ARKIV)
												.opprettetKildeNavn("test")
												.build())
										.build())
								.build())
				.build();
		joarkRepository.save(journalpost);

		DokumentFilTestRepository.persist(getDokumentFilBuilder()
				.filUuid(UUID)
				.fil(TEST_FILE_CONTENT.getBytes())
				.opprettetKildeNavn("test")
				.build());

		return journalpost;
	}
}

