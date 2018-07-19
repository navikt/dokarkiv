package no.nav.dokarkiv.core.repository.ondemand;

import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODServer;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating poolable OnDemand connections.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class OnDemandConnectionFactory extends BasePoolableObjectFactory<OnDemandConnection> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OdServerFactory odServerFactory;
	private OnDemandConnectionParameters connectionParameters;
	
	/**
	 * Constructs a new OnDemandConnectionFactory.
	 *
	 * @param odServerFactory The factory for obtaining ODServers.
	 * @param connectionParameters The connection parameters to use
	 */
	public OnDemandConnectionFactory(OdServerFactory odServerFactory, OnDemandConnectionParameters connectionParameters) {
		this.odServerFactory = odServerFactory;
		this.connectionParameters = connectionParameters;
	}

	/** {@inheritDoc} */
	@Override
	public OnDemandConnection makeObject() {
		ODServer odServer = logonToOdServer();
		ODFolder odFolder = openOdFolder(odServer);
		logger.debug("Adding OnDemandConnection to pool");
		return new OnDemandConnection(odServer, odFolder);
	}

	private ODServer logonToOdServer() {
		logger.debug("Creating new OdServer and logging on with connection parameters: {}", connectionParameters);
        ODServer odServer;
        try {
            odServer = odServerFactory.createOdServer();
            odServer.initialize(connectionParameters.getApplication());
			odServer.setPort(connectionParameters.getPort());
			odServer.logon(connectionParameters.getServer(), connectionParameters.getUsername(),
					connectionParameters.getPassword());
		} catch (Exception e) {
			throw new OnDemandRepositoryException("Could not logon to OnDemand server " + connectionParameters.getServer()
					+ ":" + connectionParameters.getPort() + " with username " + connectionParameters.getUsername(), e);
		}
		return odServer;
	}
	
	private ODFolder openOdFolder(ODServer odServer) {
		ODFolder odFolder = null;
		logger.debug("Opening OdFolder {}", connectionParameters.getFolder());
		try {
			odFolder = odServer.openFolder(connectionParameters.getFolder());
		} catch (Exception e) {
			throw new OnDemandRepositoryException("Could not open OdFolder " + connectionParameters.getFolder()
					+ " on OnDemand server " + connectionParameters.getServer() + ":" + connectionParameters.getPort(), e); 
		}
		return odFolder;
	}

	/** {@inheritDoc} */
	@Override
	public void destroyObject(OnDemandConnection onDemandConnection) {
		try {
			logger.debug("Closing OdFolder {}", connectionParameters.getFolder());
			onDemandConnection.getOdFolder().close();
			onDemandConnection.getOdServer().logoff();
		} catch (Exception e) {
			logger.error("Could not log off from OnDemand server " + connectionParameters.getServer()
					+ ":" + connectionParameters.getPort(), e);
		} finally {
			logger.debug("Closing connection to OdServer {}:{}", connectionParameters.getServer(),
					connectionParameters.getPort());
			onDemandConnection.getOdServer().terminate();
			logger.debug("Removing OnDemandConnection from pool");
		}
	}

    @Override
    public boolean validateObject(OnDemandConnection onDemandConnection) {
        boolean valid = onDemandConnection.getOdServer().isInitialized()
				&& !onDemandConnection.getOdServer().isServerTimedOut();
        if (!valid) {
            logger.warn("onDemandConnection {}:{} did not validate.", onDemandConnection.getOdServer().getServerName(),
					onDemandConnection.getOdFolder().getName());
        }
        return valid;
    }
}
