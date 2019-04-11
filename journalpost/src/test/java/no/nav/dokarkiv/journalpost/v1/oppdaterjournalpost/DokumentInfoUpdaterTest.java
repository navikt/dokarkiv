package no.nav.dokarkiv.journalpost.v1.oppdaterjournalpost;

import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.journalpost.v1.AksjonsLoggHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DokumentInfoUpdaterTest {
    private DokumentInfo dokumentInfo;
    private no.nav.dokarkiv.journalpost.v1.api.DokumentInfo dokumentRequest;

    @InjectMocks
    private DokumentInfoUpdater updater;

    @Test
    public void shouldUpdateDokumentInfo() throws UgyldigAksjonsLoggException {
        AksjonsLoggHelper aksjonsLoggHelper = new AksjonsLoggHelper();
        dokumentInfo = new DokumentInfo();
        dokumentRequest = no.nav.dokarkiv.journalpost.v1.api.DokumentInfo.builder()
                .brevkode(BREVKODE1)
                .tittel(DOKUMENT_TITTEL1)
                .build();

        updater.updateFields(dokumentInfo, dokumentRequest, aksjonsLoggHelper);

        assertThat(dokumentInfo.getBrevkode(), is(BREVKODE1));
        assertThat(dokumentInfo.getTittel(), is(DOKUMENT_TITTEL1));
    }

}
