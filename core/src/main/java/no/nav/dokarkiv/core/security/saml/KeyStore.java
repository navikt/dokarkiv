//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package no.nav.dokarkiv.core.security.saml;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class KeyStore {
	private static final Logger log = LoggerFactory.getLogger(KeyStore.class);
	private final java.security.KeyStore keyStore;
	private final String privateKeyPassword;

	public KeyStore(String trustStore, String trustStorePassword, String privateKeyPassword) {
		this.privateKey***passord=gammelt_passord***;

		try {
			this.keyStore = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
			log.debug("Trying to load Truststore with classloader");
			InputStream classPathStream = KeyStore.class.getResourceAsStream(trustStore);

			try {
				if (classPathStream != null) {
					this.keyStore.load(classPathStream, trustStorePassword.toCharArray());
				} else {
					log.debug("Trying to load Truststore from filesystem");
					FileInputStream is = new FileInputStream(trustStore);

					try {
						this.keyStore.load(is, trustStorePassword.toCharArray());
					} catch (Throwable var10) {
						try {
							is.close();
						} catch (Throwable var9) {
							var10.addSuppressed(var9);
						}

						throw var10;
					}

					is.close();
				}
			} catch (Throwable var11) {
				if (classPathStream != null) {
					try {
						classPathStream.close();
					} catch (Throwable var8) {
						var11.addSuppressed(var8);
					}
				}

				throw var11;
			}

			if (classPathStream != null) {
				classPathStream.close();
			}

		} catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException var12) {
			throw new IllegalStateException("Failed while initiating Truststore", var12);
		}
	}

	java.security.KeyStore getKeyStore() {
		return this.keyStore;
	}

	public Collection<X509Certificate> getCertificates() {
		ArrayList certificates = new ArrayList();

		try {
			Enumeration aliases = this.getKeyStore().aliases();

			while (aliases.hasMoreElements()) {
				String alias = (String) aliases.nextElement();
				X509Certificate certificate = (X509Certificate) this.getKeyStore().getCertificate(alias);
				certificates.add(certificate);
			}

			return certificates;
		} catch (KeyStoreException var5) {
			throw new RuntimeException("Failed while reading certificates from truststore", var5);
		}
	}

	public PrivateKeyEntry getSSLCertificate() {
		if (StringUtils.isEmpty(this.privateKeyPassword)) {
			throw new IllegalStateException("Password for accessing privatekey entry in keystore was not provided");
		} else {
			PrivateKeyEntry privateKeyEntry = null;

			try {
				Enumeration aliases = this.getKeyStore().aliases();

				while (aliases.hasMoreElements()) {
					String alias = (String) aliases.nextElement();
					if (this.keyStore.isKeyEntry(alias)) {
						***passord=gammelt_passord***());
						privateKeyEntry = (PrivateKeyEntry) this.keyStore.getEntry(alias, protParam);
					}
				}

				return privateKeyEntry;
			} catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException var5) {
				throw new RuntimeException("Failed while reading certificates from truststore", var5);
			}
		}
	}
}
