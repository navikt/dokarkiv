package no.nav.dokarkiv.hentjournalinfo.gjoark001;

import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class DokumentInfoQueryTest {


    @Mock
    private DokumentinfoRepository dokumentinfoRepository;

    @Mock
    private AbacSecurityService abacSecurityService;

    @InjectMocks
    private DokumentInfoQuery dokumentInfoQuery;


//    @Test
//    public void test(){
//        dokumentInfoQuery.filDetaljerList()
//    }
//
//    public void createDokumentInfo(){
//        DokumentInfo.builder()
//
//                .filDetaljerList(Arrays.asList(
//                        DokumentInfo.Fildetaljer.builder()
//                                .variantFormat()
//                                .build()
//
//                ))
//    }

}