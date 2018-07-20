package no.nav.dokarkiv.core.repository.ondemand;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ibm.edms.od.ODConstant;
import com.ibm.edms.od.ODCriteria;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODHit;
import com.ibm.edms.od.ODServer;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import org.apache.commons.pool.ObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Tests DefaultOnDemandRepository.
 * 
 * @author Hans Olav Loftum, BEKK
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultOnDemandRepositoryTest {
	
	private static final byte[] PDF_BYTES = "PDF".getBytes();

	private static final String SOME_ON_DEMAND_ID = "PESYS***gammelt_fnr******gammelt_fnr******gammelt_fnr***";
	private static final String ONDEMAND_SEARCH_CRITERIA = "IDNR";
	
	private DefaultOnDemandRepository repository;
	private Map<OnDemandInstansCode, String> onDemandSearchCriterias;
	private Map<OnDemandInstansCode, ObjectPool<OnDemandConnection>> onDemandConnections;
	private OnDemandConnection onDemandConnection;
	@Mock
	private ObjectPool<OnDemandConnection> pesysConnectionPoolMock;
	@Mock
	private ObjectPool<OnDemandConnection> infotrygdConnectionPoolMock;
	@Mock
	private ODServer oDServerMock;
	@Mock
	private ODFolder odFolderMock;
	@Mock
	private ODCriteria odCriteriaMock;
	
	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		onDemandConnection = new OnDemandConnection(oDServerMock, odFolderMock);
		setupOnDemandSearchCriteria();
		setUpOnDemandConnections();
		setUpRepository();
	}
	
	@Test
	public void shouldRetrieveDocumentFromOnDemandInstancePesys() throws Exception {
		when(pesysConnectionPoolMock.borrowObject()).thenReturn(onDemandConnection);
		setUpOdFolderMockToReturn(PDF_BYTES);
		
		byte[] document = repository.getDocument(SOME_ON_DEMAND_ID, OnDemandInstansCode.PESYS);
		
		assertThat(document, is(PDF_BYTES));
	}
	
	@Test
	public void shouldRetrieveDocumentFromOnDemandInstanceInfotrygd() throws Exception {
		when(infotrygdConnectionPoolMock.borrowObject()).thenReturn(onDemandConnection);
		setUpOdFolderMockToReturn(PDF_BYTES);
		
		byte[] document = repository.getDocument(SOME_ON_DEMAND_ID, OnDemandInstansCode.INFOT_UT);
		
		assertThat(document, is(PDF_BYTES));
	}
	
	@Test
	public void shouldReturnConnectionToPoolAfterSuccessfulDocumentRetrieval() throws Exception {
		when(infotrygdConnectionPoolMock.borrowObject()).thenReturn(onDemandConnection);
		setUpOdFolderMockToReturn(PDF_BYTES);
		
		repository.getDocument(SOME_ON_DEMAND_ID, OnDemandInstansCode.INFOT_UT);
		
		verify(infotrygdConnectionPoolMock).returnObject(onDemandConnection);
	}
	
	@Test
	public void shouldInvalidateConnectionAfterExceptionFromOD() throws Exception {
		when(infotrygdConnectionPoolMock.borrowObject()).thenReturn(onDemandConnection);
		when(odFolderMock.search()).thenThrow(new Exception());
		
		try {
			repository.getDocument(SOME_ON_DEMAND_ID, OnDemandInstansCode.INFOT_UT);
			fail();
		} catch (OnDemandRepositoryException expected) {
		}
		
		verify(infotrygdConnectionPoolMock).invalidateObject(onDemandConnection);
	}
	
	@Test
	public void shouldThrowExceptionlWhenDocumentDoesNotExist() throws Exception {
		when(pesysConnectionPoolMock.borrowObject()).thenReturn(onDemandConnection);
		when(odFolderMock.search()).thenReturn(new Vector<ODHit>());
		try {
			repository.getDocument(SOME_ON_DEMAND_ID, OnDemandInstansCode.PESYS);
			fail("Should fail when search returns 0 hits");
		} catch (EmptyOnDemandSearchResultException expected) {
			assertThat(expected.getMessage(), containsString("OnDemand search for OnDemandId=" + SOME_ON_DEMAND_ID));
			assertThat(expected.getMessage(), containsString("returned 0 hits"));
		}
		verify(pesysConnectionPoolMock).returnObject(onDemandConnection);
	}

	private void setUpRepository() {
		repository = new DefaultOnDemandRepository();
		repository.setOnDemandSearchCriterias(onDemandSearchCriterias);
		repository.setOnDemandConnections(onDemandConnections);
	}

	private void setupOnDemandSearchCriteria() {
		onDemandSearchCriterias = new HashMap<OnDemandInstansCode, String>();
		onDemandSearchCriterias.put(OnDemandInstansCode.PESYS, ONDEMAND_SEARCH_CRITERIA);
		onDemandSearchCriterias.put(OnDemandInstansCode.INFOT_UT, ONDEMAND_SEARCH_CRITERIA);
		
		when(odFolderMock.getCriteria(ONDEMAND_SEARCH_CRITERIA)).thenReturn(odCriteriaMock);
	}
	
	private void setUpOnDemandConnections() {
		onDemandConnections = new HashMap<OnDemandInstansCode, ObjectPool<OnDemandConnection>>();
		onDemandConnections.put(OnDemandInstansCode.PESYS, pesysConnectionPoolMock);
		onDemandConnections.put(OnDemandInstansCode.INFOT_UT, infotrygdConnectionPoolMock);
	}
	
	private void setUpOdFolderMockToReturn(byte[]...dokuments) throws Exception {
		Vector<ODHit> hits = vectorWith(dokuments);
		when(odFolderMock.search()).thenReturn(hits);
	}

	@SuppressWarnings("unused")
	private static Vector<ODHit> vectorWith(byte[]... arrays) throws Exception {
		Vector<ODHit> vector = new Vector<ODHit>();
		for (byte[] array : arrays) {
			ODHit hitMock = mock(ODHit.class);
            when(hitMock.retrieve(ODConstant.PDF)).thenReturn(PDF_BYTES);
			vector.add(hitMock);
		}
		return vector;
	}
	
}
