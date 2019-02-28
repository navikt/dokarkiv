package no.nav.dokarkiv.oppdatermetadata.v1.rjoark200;

import static no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.oppdatermetadata.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.repository.BrukerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DokumentInfoMapperTest {
        @Mock
        private BrukerRepository brukerRepositoryMock;

        private DokumentInfo dokumentInfo;

        @InjectMocks
        private DokumentInfoMapper mapper = new DokumentInfoMapper();


        @Test
        public void shouldUpdateDokumentInfo() {
            dokumentInfo = new DokumentInfo();

            mapper.oppdaterDokumentInfo(dokumentInfo, BREVKODE1, DOKUMENT_TITTEL1);

            assertThat(dokumentInfo.getBrevkode(), is(BREVKODE1));
            assertThat(dokumentInfo.getTittel(), is(DOKUMENT_TITTEL1));
        }

}
