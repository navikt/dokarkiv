package no.nav.dokarkiv.hentjournalinfo;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.BrukerType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.DokumentStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.FilType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostStatus;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.JournalpostType;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.Tema;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.TilknyttetJournalpostSom;
import no.nav.dokarkiv.hentjournalinfo.dto.kode.VariantFormat;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class EnumTest {


    @Test
    public void testDokumentStatus() {
        Arrays.stream(DokumentStatusCode.values()).forEach(dokumentStatusCode -> {
            DokumentStatus dokumentStatus = DokumentStatus.mapFromDokumentStatusCode(dokumentStatusCode);
            assertThat(dokumentStatus, notNullValue());
        });
    }

    @Test
    public void testBrukerType() {
        Arrays.stream(BrukerTypeCode.values()).forEach(brukerTypeCode -> {
            BrukerType brukerType = BrukerType.mapFromBrukerTypeCode(brukerTypeCode);
            assertThat(brukerType, notNullValue());
        });
    }

    @Test
    public void testTema() {
        Arrays.stream(FagomradeCode.values()).forEach(fagomradeCode -> {
            Tema tema = Tema.mapFromFagomradeCode(fagomradeCode);
            assertThat("Could not map for value=" + fagomradeCode.name(), tema, notNullValue());
        });
    }

    @Test
    public void testFiltype() {
        Arrays.stream(FilTypeCode.values()).forEach(filTypeCode -> {
            FilType filType = FilType.mapFromFilTypeCode(filTypeCode);
            assertThat("Could not map for value=" + filTypeCode.name(), filType, notNullValue());
        });
    }

    @Test
    public void testJournalpostStatus() {
        Arrays.stream(JournalStatusCode.values()).forEach(journalStatusCode -> {
            JournalpostStatus journalpostStatus = JournalpostStatus.mapFromJournalStatusCode(journalStatusCode);
            assertThat("Could not map for value=" + journalStatusCode.name(), journalpostStatus, notNullValue());
        });
    }

    @Test
    public void testJournalpostType() {
        Arrays.stream(JournalpostTypeCode.values()).forEach(journalpostTypeCode -> {
            JournalpostType journalpostType = JournalpostType.mapFromJournalpostTypeCode(journalpostTypeCode);
            assertThat("Could not map for value=" + journalpostTypeCode.name(), journalpostType, notNullValue());
        });
    }

    @Test
    public void testTilknyttetJournalpostSom() {
        Arrays.stream(TilknyttetJournalpostSomCode.values()).forEach(tilknyttetJournalpostSomCode -> {
            TilknyttetJournalpostSom tilknyttetJournalpostSom = TilknyttetJournalpostSom.mapTilknyttetJournalpostSomCode(tilknyttetJournalpostSomCode);
            assertThat("Could not map for value=" + tilknyttetJournalpostSomCode.name(), tilknyttetJournalpostSom, notNullValue());
        });
    }

    @Test
    public void testVariantFormat() {
        Arrays.stream(VariantFormatCode.values()).forEach(variantFormatCode -> {
            VariantFormat variantFormat = VariantFormat.mapFromVariantFormatCode(variantFormatCode);
            assertThat("Could not map for value=" + variantFormatCode.name(), variantFormat, notNullValue());
        });
    }
}
