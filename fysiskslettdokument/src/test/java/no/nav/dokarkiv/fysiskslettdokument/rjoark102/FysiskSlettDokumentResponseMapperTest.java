package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.lang3.EnumUtils;
import org.junit.Test;

public class FysiskSlettDokumentResponseMapperTest {

	private static final String TITTEL = "Tittel";
	private static final Long DOKUMENTINFO_ID = 13L;
	private static final JournalStatusCode JOURNALSTATUS = JournalStatusCode.J;
	private static final Long JOURNALPOST_ID = 42L;
	private static final JournalpostTypeCode JOURNALPOST_TYPE = JournalpostTypeCode.U;
	private static final FagomradeCode TEMA = FagomradeCode.PEN;

	@Test
	public void shouldMap() {
		FysiskSlettDokumentResponse response = FysiskSlettDokumentResponseMapper.mapToFysiskSlettDokumentResponse(
				createJournalpost(), createDokumentInfo());

		assertThat("response.tittel", response.getTittel(), is(TITTEL));
		assertThat("response.dokumentInfoId", response.getDokumentInfoId(), is(DOKUMENTINFO_ID));
		assertThat("response.slettet", response.getSlettet(), is(true));
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
				.slettet(true)
				.build();
	}

}
