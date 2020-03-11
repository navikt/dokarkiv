package no.nav.dokarkiv.core.stelvio;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.xml.ws.WebServiceContext;

import java.security.Principal;

import static org.mockito.Mockito.doReturn;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextUtilTest {
    private final String USER_KEY = "user";
    private final String USER_ID = "unitTestUser";

    @Mock
    WebServiceContext webServiceContext;

    @Mock
    Principal principal;

    @Before
    public void setup(){
        MDCOperations.resetMdcProperties();
    }

    @Test
    public void doSetUserInMDC() {
        doReturn(principal).when(webServiceContext).getUserPrincipal();
        doReturn(USER_ID).when(principal).getName();

        assertThat("MDC is cleared", MDC.get(USER_KEY) == null);

        RequestContextUtil.createAndSetRequestContext(webServiceContext, "unitTest");

        assertThat("MDC user is not masked", MDC.get(USER_KEY).equals(USER_ID));
    }

}
