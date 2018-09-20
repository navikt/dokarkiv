package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.lang3.EnumUtils;
import org.junit.Test;

public class LogiskSlettDokumentResponseMapperTest {

	private static final String TITTEL = "Tittel";
	private static final Long DOKUMENTINFO_ID = 13L;
	private static final JournalStatusCode JOURNALSTATUS = JournalStatusCode.J;
	private static final Long JOURNALPOST_ID = 42L;
	private static final JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.U;
	private static final FagomradeCode TEMA = FagomradeCode.PEN;

	@Test
	public void shouldMap() {
		LogiskSlettDokumentResponse response = LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(
				createJournalpost(), createDokumentInfo());
		assertLogiskSLettDokumentResponse(response);
	}

	@Test
	public void shouldFailToMapBecauseJournalstatusIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(null);

		LogiskSlettDokumentResponse response = LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(
				journalpost, createDokumentInfo());

		assertThat("response.journalstatus", EnumUtils.isValidEnum(JournalStatusCode.class, response.getJournalStatus()), is(false));

	}

	@Test
	public void shouldFailToMapBecauseJournalpostTypeIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalposttype(null);

		LogiskSlettDokumentResponse response = LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(
				journalpost, createDokumentInfo());

		assertThat("response.journalpostType", EnumUtils.isValidEnum(JournalpostTypeCode.class, response.getJournalpostType()), is(false));

	}

	@Test
	public void shouldFailToMapBecauseTemaIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.setFagomrade(null);

		LogiskSlettDokumentResponse response = LogiskSlettDokumentResponseMapper.mapToSlettDokumentResponse(
				journalpost, createDokumentInfo());

		assertThat("response.tema", EnumUtils.isValidEnum(FagomradeCode.class, response.getTema()), is(false));

	}

	private void assertLogiskSLettDokumentResponse(LogiskSlettDokumentResponse response) {
		assertThat("response.tittel", response.getTittel(), is(TITTEL));
		assertThat("response.dokumentInfoId", response.getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat("response.journalstatus", EnumUtils.isValidEnum(JournalStatusCode.class, response.getJournalStatus()), is(true));
		assertThat("response.journalpostId", response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat("response.journalpostType", EnumUtils.isValidEnum(JournalpostTypeCode.class, response.getJournalpostType()), is(true));
		assertThat("response.tema", EnumUtils.isValidEnum(FagomradeCode.class, response.getTema()), is(true));
	}

	private Journalpost createJournalpost() {
		return Journalpost.builder()
				.journalstatus(JOURNALSTATUS)
				.journalpostId(JOURNALPOST_ID)
				.journalposttype(JOURNALPOST_TYPE)
				.fagomrade(TEMA)
				.build();
	}

	private DokumentInfo createDokumentInfo() {
		return DokumentInfo.builder()
				.tittel(TITTEL)
				.dokumentInfoId(DOKUMENTINFO_ID)
				.build();
	}
}
