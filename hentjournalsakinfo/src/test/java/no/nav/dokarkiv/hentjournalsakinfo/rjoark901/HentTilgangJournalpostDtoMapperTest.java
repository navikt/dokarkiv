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

import static org.junit.Assert.assertEquals;

public class HentTilgangJournalpostDtoMapperTest {

    @Test
    public void shouldMapHentTilgangDtoFromTuple() {

        LocalDateTime journalpostDatetime = LocalDateTime.now();
        String formattedSakrelasjonOpprettetTid = ZonedDateTime.of(journalpostDatetime, ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        long journalpostId = 1234L;
        JournalStatusCode journalStatus = JournalStatusCode.J;
        JournalpostTypeCode journalType = JournalpostTypeCode.U;
        FagomradeCode journalFagomrade = FagomradeCode.PEN;
        Timestamp createdDate = Timestamp.valueOf(journalpostDatetime);
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

        long dokumentInfoId = 1234L;
        DokumentStatusCode dokumentInfoStatus = DokumentStatusCode.FERDIGSTILT;
        String dokumentInfoBrevkode = "kode";
        SkjermingTypeCode dokumentInfoRelasjonerSkjermingType = SkjermingTypeCode.FEIL;

        VariantFormatCode fildetaljerVariantFormat = VariantFormatCode.SLADDET;
        SkjermingTypeCode fildetaljerSkjermingType = SkjermingTypeCode.POL;

        Object[] tuple = new Object[]{
                journalpostId, journalStatus, journalType, journalFagomrade, createdDate,
                mottakskanal, jounralpostSkjermingType, avsenderMottakerId,
                brukerId, brukerType,
                sakrelasjonSakId, sakrelasjonFagsystem, sakrelasjonAktoerId,
                sakrelasjonTema, sakrelasjonFagsakNr, sakrelasjonOrgnr,
                sakrelasjonApplikasjon, sakrelasjonOpprettetAv, journalpostDatetime,
                dokumentInfoId, dokumentInfoStatus, dokumentInfoBrevkode,
                dokumentInfoRelasjonerSkjermingType, fildetaljerVariantFormat, fildetaljerSkjermingType
        };

        TilgangJournalpostDto journalpostDto = HentTilgangJournalpostDtoMapper.mapTupleTilgangJournalPost(tuple);

        assertEquals(String.valueOf(journalpostId), journalpostDto.getJournalpostId());
        assertEquals(journalStatus, journalpostDto.getJournalStatus());
        assertEquals(journalType, journalpostDto.getJournalpostType());
        assertEquals(journalFagomrade, journalpostDto.getFagomrade());
        assertEquals(journalpostDatetime, journalpostDto.getDatoOpprettet());
        assertEquals(mottakskanal, journalpostDto.getMottakskanal());
        assertEquals(jounralpostSkjermingType, journalpostDto.getSkjerming());
        assertEquals(avsenderMottakerId, journalpostDto.getAvsenderMottakerId());

        assertEquals(brukerId, journalpostDto.getBruker().getBrukerId());
        assertEquals(brukerType, journalpostDto.getBruker().getBrukerType());

        assertEquals(sakrelasjonSakId, journalpostDto.getSak().getSakId());
        assertEquals(sakrelasjonFagsystem, journalpostDto.getSak().getFagsystem());
        assertEquals(sakrelasjonAktoerId, journalpostDto.getSak().getAktoerId());
        assertEquals(sakrelasjonTema, journalpostDto.getSak().getTema());
        assertEquals(sakrelasjonFagsakNr, journalpostDto.getSak().getFagsakNr());
        assertEquals(sakrelasjonOrgnr, journalpostDto.getSak().getOrgnr());
        assertEquals(sakrelasjonApplikasjon, journalpostDto.getSak().getApplikasjon());
        assertEquals(sakrelasjonOpprettetAv, journalpostDto.getSak().getOpprettetAv());
        assertEquals(formattedSakrelasjonOpprettetTid, journalpostDto.getSak().getOpprettetTidspunkt());

        assertEquals(String.valueOf(dokumentInfoId), journalpostDto.getDokument().getDokumentinfoId());
        assertEquals(dokumentInfoStatus, journalpostDto.getDokument().getDokumentstatus());
        assertEquals(dokumentInfoBrevkode, journalpostDto.getDokument().getBrevkode());
        assertEquals(dokumentInfoRelasjonerSkjermingType, journalpostDto.getDokument().getSkjerming());
        assertEquals(fildetaljerVariantFormat, journalpostDto.getDokument().getVariant().getVariantFormat());
        assertEquals(fildetaljerSkjermingType, journalpostDto.getDokument().getVariant().getSkjerming());
    }
}
