package no.nav.dokarkiv.core.repository.ondemand;

import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODServer;

/**
 * Factory for creating ODServer objects.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface OdServerFactory {

	/**
	 * Create a new ODServer.
	 * 
	 * @return A new ODServer
	 */
	ODServer createOdServer() throws ODException;
}
