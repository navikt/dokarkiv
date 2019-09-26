package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HentTilgangJournalpostDtoMapperTest {

	@Test
	public void shouldMapHentTilgangDtoFromTuple() {

		LocalDateTime journalpostDatetime = LocalDateTime.now();
		String formattedSakrelasjonOpprettetTid = ZonedDateTime.of(journalpostDatetime ,ZoneId.systemDefault())
						.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		long journalpostId = 1234L;
		JournalStatusCode journalStatus = JournalStatusCode.J;
		JournalpostTypeCode journalType = JournalpostTypeCode.U;
		FagomradeCode journalFagomrade = FagomradeCode.PEN;
		Timestamp craetedDate = Timestamp.valueOf(journalpostDatetime);
		MottaksKanalCode mottakskanal = MottaksKanalCode.NAV_NO;
		SkjermingTypeCode jounralpostSkjermingType = SkjermingTypeCode.POL;
		String avsenderMottakerId = "avsenderId";
		String brukerId = "brukerId";
		BrukerTypeCode brukerType = BrukerTypeCode.PERSON;

		String sakrelasjonSakId = "12345";
		FagsystemCode sakrelasjonFagsystem = FagsystemCode.FS22;
		String sakrelasjonAktoerId = "aktoerId";
		String sakrelasjonTema = "BID";
		String sakrelasjonFagsakNr = "1234";
		String sakrelasjonOrgnr = "1234";
		String sakrelasjonApplikasjon = "FS22";
		String sakrelasjonOpprettetAv = "Z123456";
		Timestamp sakrelasjonOpprettetTid = Timestamp.valueOf(journalpostDatetime);

		long dokumentInfoId = 1234L;
		DokumentStatusCode dokumentInfoStatus = DokumentStatusCode.FERDIGSTILT;
		String dokumentInfoBrevkode = "kode";
		SkjermingTypeCode dokumentInfoRelasjonerSkjermingType = SkjermingTypeCode.FEIL;

		VariantFormatCode fildetaljerVariantFormat = VariantFormatCode.SLADDET;
		SkjermingTypeCode fildetaljerSkjermingType = SkjermingTypeCode.POL;

		Object[] tuple = new Object[]{
				journalpostId, journalStatus, journalType, journalFagomrade, craetedDate,
				mottakskanal, jounralpostSkjermingType, avsenderMottakerId,
				brukerId, brukerType,
				sakrelasjonSakId, sakrelasjonFagsystem, sakrelasjonAktoerId,
				sakrelasjonTema, sakrelasjonFagsakNr, sakrelasjonOrgnr,
				sakrelasjonApplikasjon, sakrelasjonOpprettetAv, sakrelasjonOpprettetTid,
				dokumentInfoId, dokumentInfoStatus, dokumentInfoBrevkode,
				dokumentInfoRelasjonerSkjermingType, fildetaljerVariantFormat, fildetaljerSkjermingType
		};

		TilgangJournalpostDto journalpostDto = HentTilgangJournalpostDtoMapper.mapTupleTilgangJournalPost(tuple);

		assertThat(journalpostDto.getJournalpostId(), is(String.valueOf(journalpostId)));
		assertThat(journalpostDto.getJournalStatus(), is(journalStatus));
		assertThat(journalpostDto.getJournalpostType(), is(journalType));
		assertThat(journalpostDto.getFagomrade(), is(journalFagomrade));
		assertThat(journalpostDto.getDatoOpprettet(), is(journalpostDatetime));
		assertThat(journalpostDto.getMottakskanal(), is(mottakskanal));
		assertThat(journalpostDto.getSkjerming(), is(jounralpostSkjermingType));
		assertThat(journalpostDto.getAvsenderMottakerId(), is(avsenderMottakerId));

		assertThat(journalpostDto.getBruker().getBrukerId(), is(brukerId));
		assertThat(journalpostDto.getBruker().getBrukerType(), is(brukerType));

		assertThat(journalpostDto.getSak().getSakId(), is(sakrelasjonSakId));
		assertThat(journalpostDto.getSak().getFagsystem(), is(sakrelasjonFagsystem));
		assertThat(journalpostDto.getSak().getAktoerId(), is(sakrelasjonAktoerId));
		assertThat(journalpostDto.getSak().getTema(), is(sakrelasjonTema));
		assertThat(journalpostDto.getSak().getFagsakNr(), is(sakrelasjonFagsakNr));
		assertThat(journalpostDto.getSak().getOrgnr(), is(sakrelasjonOrgnr));
		assertThat(journalpostDto.getSak().getApplikasjon(), is(sakrelasjonApplikasjon));
		assertThat(journalpostDto.getSak().getOpprettetAv(), is(sakrelasjonOpprettetAv));
		assertThat(journalpostDto.getSak().getOpprettetTidspunkt(), is(formattedSakrelasjonOpprettetTid));

		assertThat(journalpostDto.getDokument().getDokumentinfoId(), is(String.valueOf(dokumentInfoId)));
		assertThat(journalpostDto.getDokument().getDokumentstatus(), is(dokumentInfoStatus));
		assertThat(journalpostDto.getDokument().getBrevkode(), is(dokumentInfoBrevkode));
		assertThat(journalpostDto.getDokument().getSkjerming(), is(dokumentInfoRelasjonerSkjermingType));
		assertThat(journalpostDto.getDokument().getVariant().getVariantFormat(), is(fildetaljerVariantFormat));
		assertThat(journalpostDto.getDokument().getVariant().getSkjerming(), is(fildetaljerSkjermingType));
	}
}
