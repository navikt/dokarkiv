package no.nav.dokarkiv.core.repository.ondemand;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;

/**
 * Container for OnDemand connection parameters.
 * 
 * @author Stian Landsnes, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class OnDemandConnectionParameters {

	private String server;
	private int port;
	private String username;
	private String password;
	private String application;
	private String folder;

	/**
	 * Constructor with input parameters.
	 * 
	 * @param server The server
	 * @param port The port
	 * @param username The username
	 * @param password The password
	 * @param application The application
	 * @param folder The folder
	 */
	public OnDemandConnectionParameters(String server, int port, String username, String password, String application,
										String folder) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.***passord=gammelt_passord***;
		this.application = application;
		this.folder = folder;
	}

	/**
	 * Getter for the server property.
	 *
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Getter for the port property.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Getter for the username property.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getter for the password property.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Getter for the application property.
	 *
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * Getter for the folder property.
	 *
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
			protected boolean accept(Field f) {
				// Don't include password in toString
				return super.accept(f) && !f.getName().equals("password");
			}
		}).toString();
	}

}
