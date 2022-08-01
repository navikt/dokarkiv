package no.nav.dokarkiv.core.stelvio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.xml.ws.WebServiceContext;
import java.security.Principal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class RequestContextUtilTest {
	private final String USER_KEY = "user";
	private final String USER_ID = "unitTestUser";

	@Mock
	WebServiceContext webServiceContext;

	@Mock
	Principal principal;

	@BeforeEach
	public void setup() {
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
