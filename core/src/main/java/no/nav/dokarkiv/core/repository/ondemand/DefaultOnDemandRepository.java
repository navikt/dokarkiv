package no.nav.dokarkiv.core.repository.ondemand;

import com.ibm.edms.od.ODConstant;
import com.ibm.edms.od.ODCriteria;
import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODHit;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.stelvio.MissingPropertyException;
import org.apache.commons.pool.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

/**
 * Repository for accessing OnDemand
 *
 * @author Carl-Henrik Lund, Bekk
 * @author Stian Landsnes, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultOnDemandRepository implements OnDemandRepository {

	private Map<OnDemandInstansCode, ObjectPool<OnDemandConnection>> onDemandConnections;
	private Map<OnDemandInstansCode, String> onDemandSearchCriterias;
//	private ExceptionLogger exceptionLogger;
	private static final Logger infologger = LoggerFactory.getLogger(DefaultOnDemandRepository.class);

	/**
	 * {@inheritDoc}
	 */
	public byte[] getDocument(String onDemandId, OnDemandInstansCode onDemandInstans) {
		ObjectPool<OnDemandConnection> odConnectionPool = onDemandConnections.get(onDemandInstans);
		OnDemandConnection odConnection = null;
		try {
			odConnection = odConnectionPool.borrowObject();
			ODHit hit = doOnDemandSearch(onDemandId, onDemandInstans, odConnection.getOdFolder());
			byte[] transformedDoc = hit.retrieve(ODConstant.PDF);
			return transformedDoc;
		} catch (EmptyOnDemandSearchResultException emptySearchResultException) {
			throw emptySearchResultException;
		} catch (ODException odException) {
			String extraText = ". OnDemand error code: " + odException.getErrorId() + " msg: " + odException.getErrorMsg();
			OnDemandConnection tmpOdConnection = odConnection;
			odConnection = null;
			throw catchOdException(onDemandId, odConnectionPool, tmpOdConnection, odException, extraText);
		} catch (Exception exception) {
			OnDemandConnection tmpOdConnection = odConnection;
			odConnection = null;
			throw catchOdException(onDemandId, odConnectionPool, tmpOdConnection, exception, null);
		} finally {
			try {
				if (odConnection != null) {
					odConnectionPool.returnObject(odConnection);
				}
			} catch (Exception e) {
//				exceptionLogger.log("Could not return OdConnection to connectionpool", e);
			}
		}
	}

	private OnDemandRepositoryException catchOdException(
			String onDemandId, ObjectPool<OnDemandConnection> odConnectionPool,
			OnDemandConnection odConnection, Exception odException, String s) {
		String server = null;
		Integer port = null;
		if (odConnection != null) {
			server = odConnection.getOdServer().getServerName();
			port = odConnection.getOdServer().getPort();
			try {
				odConnectionPool.invalidateObject(odConnection);
			} catch (Exception poolException) {
//				exceptionLogger.log("Could not invalidate OdConnection", poolException);
			}
		}
		return new OnDemandRepositoryException("Failed to retrieve document with OnDemandId=" + onDemandId
				+ " from OnDemand server " + server + ":" + port
				+ (s != null ? s : ""), odException);
	}

	@SuppressWarnings("unchecked")
	private ODHit doOnDemandSearch(String onDemandId, OnDemandInstansCode onDemandInstans, ODFolder odFolder)
			throws Exception {
		ODCriteria odCrit = odFolder.getCriteria(onDemandSearchCriterias.get(onDemandInstans));
		odCrit.setOperator(ODConstant.OPEqual);
		odCrit.setSearchValue(onDemandId);
		Vector<ODHit> hits = odFolder.search();
		if (hits == null || hits.isEmpty()) {
			throw new EmptyOnDemandSearchResultException("OnDemand search for OnDemandId=" + onDemandId + " in folder "
					+ odFolder.getName() + " returned 0 hits");
		}
		if (hits.size() > 1) {
			infologger.warn("{} hits from ondemand on {} = {} in folder {}. Proceeding with first hit.", hits.size(), onDemandInstans, onDemandId, odFolder);
		}
		return hits.get(0);
	}

	/**
	 * Setter for the onDemandConnections property.
	 *
	 * @param onDemandConnections the onDemandConnections to set
	 */
	public void setOnDemandConnections(Map<OnDemandInstansCode, ObjectPool<OnDemandConnection>> onDemandConnections) {
		this.onDemandConnections = onDemandConnections;
	}

	/**
	 * Setter for the onDemandSearchCriterias property.
	 *
	 * @param onDemandSearchCriterias the onDemandSearchCriterias to set
	 */
	public void setOnDemandSearchCriterias(Map<OnDemandInstansCode, String> onDemandSearchCriterias) {
		this.onDemandSearchCriterias = onDemandSearchCriterias;
	}

	/**
	 * Setter for the exceptionLogger property.
	 *
	 * @param exceptionLogger the exceptionLogger to set
	 */
//	public void setExceptionLogger(ExceptionLogger exceptionLogger) {
//		this.exceptionLogger = exceptionLogger;
//	}

	/**
	 * Called by Spring to validate that the required dependency injection is
	 * properly configured.
	 *
	 * @throws MissingPropertyException If a dependency injection property is missing from the
	 *                                  configuration.
	 */
	public void performSanityCheck() throws MissingPropertyException {
		ArrayList<String> propertyList = new ArrayList<String>();
		if (onDemandConnections == null) {
			propertyList.add("onDemandConnections");
		}
		if (onDemandSearchCriterias == null) {
			propertyList.add("onDemandSearchCriterias");
		}
//		if (exceptionLogger == null) {
//			propertyList.add("exceptionLogger");
//		} FIXME
		if (!propertyList.isEmpty()) {
			throw new MissingPropertyException("Required property/properties was not set by configuration", propertyList);
		}
	}

}
