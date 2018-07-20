package no.nav.dokarkiv.core.repository.ondemand;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for OnDemandConnectionFactory.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class OnDemandConnectionFactoryTest {

	private static final String APPLICATION = "test";
	private static final int PORT = 9000;
	private static final String SERVER = "localhost";
	private static final String USER = "testuser";
	private static final String ***passord=gammelt_passord***";
	private static final String FOLDER = "testfolder";
	
	private OnDemandConnectionFactory connectionFactory;
	
	private OnDemandConnectionParameters connectionParameters;
	
	@Mock
	private OdServerFactory odServerFactoryMock;
	@Mock
	private ODServer odServerMock;
	@Mock
	private ODFolder odFolderMock;

	@Before
	public void setUp() throws ODException {
		MockitoAnnotations.initMocks(this);
		connectionParameters = new OnDemandConnectionParameters(SERVER, PORT, USER, PASSWORD, APPLICATION, FOLDER);
		connectionFactory = new OnDemandConnectionFactory(odServerFactoryMock, connectionParameters);
		when(odServerFactoryMock.createOdServer()).thenReturn(odServerMock);
	}
	
	@Test
	public void shouldCreateOnDemandConnection() throws Exception {
		when(odServerMock.openFolder(FOLDER)).thenReturn(odFolderMock);
		
		OnDemandConnection connection = connectionFactory.makeObject();
		
		verify(odServerMock).initialize(APPLICATION);
		verify(odServerMock).setPort(PORT);
		verify(odServerMock).logon(SERVER, USER, PASSWORD);
		
		assertThat(connection.getOdFolder(), is(odFolderMock));
	}
	
	@Test
	public void shouldWrapOnDemandExceptionsWhenLoggingOnToOdServer() throws Exception {
		doThrow(new Exception()).when(odServerMock).logon(SERVER, USER, PASSWORD);
		try {
			connectionFactory.makeObject();
			fail();
		} catch (OnDemandRepositoryException e) {
			assertThat(e.getMessage(), containsString("Could not logon to OnDemand server " + SERVER + ":" + PORT));
		}
	}
	
	@Test
	public void shouldWrapOnDemandExceptionsWhenOpeningOdFolder() throws Exception {
		when(odServerMock.openFolder(FOLDER)).thenThrow(new Exception());
		try {
			connectionFactory.makeObject();
			fail();
		} catch (OnDemandRepositoryException e) {
			assertThat(e.getMessage(), containsString("Could not open OdFolder " + FOLDER));
		}
	}
	
	@Test
	public void shouldTerminateOdDemandConnection() throws Exception {
		OnDemandConnection onDemandConnection = new OnDemandConnection(odServerMock, odFolderMock);
		
		connectionFactory.destroyObject(onDemandConnection);
		
		verify(odFolderMock).close();
		verify(odServerMock).logoff();
		verify(odServerMock).terminate();
	}
	
	@Test
	public void shouldValidateOnDemandConnection() throws Exception {
		OnDemandConnection onDemandConnection = new OnDemandConnection(odServerMock, odFolderMock);
		
		connectionFactory.validateObject(onDemandConnection);
	}
}
