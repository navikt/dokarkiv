package no.nav.dokarkiv.core.repository.ondemand;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for DefaultOnDemandRepository. Tests integration with OnDemand,
 * requires ODWEK installed locally and added to PATH system variable
 * 
 * @author Carl-Henrik Wolf Lund, Bekk Consulting
 * @author Stian Landsnes, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { CFG_LAYER_TEST_CONTEXT_S, "/modules/rep-joark-onDemand-context.xml" })
@Ignore
public class DefaultOnDemandRepositoryTestOD {

	@Autowired
	private OnDemandRepository onDemandRepository;
	@Autowired
	@Qualifier("rep.joark.onDemand.infotrygdConnectionPool")
	private GenericObjectPool<OnDemandConnection> pool;

	
	@Test
	public void shouldRetrieveDocumentFromOnDemandInstancePESYS() throws Exception {
		String OnDemandId = "PESYS***gammelt_fnr******gammelt_fnr******gammelt_fnr***";
		byte[] document = onDemandRepository.getDocument(OnDemandId, OnDemandInstansCode.PESYS);
		assertNotNull(document);
		assertTrue("Size of document should be greater than zero", document.length > 0);
	}

	@Test
	public void shouldRetrieveDocumentFromOnDemandInstanceInfotrygd() throws Exception {
		String onDemandId = "ODIP***gammelt_fnr***0";
		byte[] document = onDemandRepository.getDocument(onDemandId, OnDemandInstansCode.INFOT_UT);
		assertNotNull(document);
		assertTrue("Size of document should be greater than zero", document.length > 0);
	}
	
	@Test
	public void shouldRetrieveDocumentFromOndemandInstanceSyfo() throws Exception {
		String onDemandId = "OD***gammelt_fnr***24";
		byte[] document = onDemandRepository.getDocument(onDemandId, OnDemandInstansCode.SYFO);
		assertNotNull(document);
		assertTrue("Size of document should be greater than zero", document.length > 0);
	}

	@Test(expected = EmptyOnDemandSearchResultException.class)
	public void retrieveDocumentThatDoesNotExist() {
		onDemandRepository.getDocument("9999", OnDemandInstansCode.PESYS);
	}
	
	@Test
	public void shouldInvalidateConnectionAfterException() throws Exception {
		pool.setMaxActive(1);
		OnDemandConnection connection = pool.borrowObject();
		connection.getOdServer().logoff(); // Will cause exception later
		pool.returnObject(connection);
		try {
			onDemandRepository.getDocument("ODIP***gammelt_fnr***0", OnDemandInstansCode.INFOT_UT);
		} catch (OnDemandRepositoryException e) {
		} finally {
			assertThat(pool.getNumActive(), is(0));
			assertThat(pool.getNumIdle(), is(0));
		}
	}
	
}
