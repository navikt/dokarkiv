package no.nav.dokarkiv.core.repository.ondemand;

import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODServer;

/**
 * Wrapper used to pool an OdServer and an OdFolder.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class OnDemandConnection {

	private ODServer odServer;
	private ODFolder odFolder;
	
	/**
	 * Constructs a new OnDemandConnection.
	 *
	 * @param odServer The OdServer.
	 * @param odFolder The OdFolder.
	 */
	public OnDemandConnection(ODServer odServer, ODFolder odFolder) {
		this.odServer = odServer;
		this.odFolder = odFolder;
	}

	/**
	 * Getter for the odServer property.
	 *
	 * @return the odServer
	 */
	public ODServer getOdServer() {
		return odServer;
	}

	/**
	 * Getter for the odFolder property.
	 *
	 * @return the odFolder
	 */
	public ODFolder getOdFolder() {
		return odFolder;
	}
	
}
