package no.nav.dokarkiv.core.repository.ondemand;

import com.ibm.edms.od.ODConfig;
import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Implementation of OdServerFactory.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultOdServerFactory implements OdServerFactory {
    private String odwekInstallDir;
    private int odwekTraceLevel;
    private String odwekTraceDir;
    private String odwekTempDir;
	@SuppressWarnings("unused")
	private String odwekResourceCacheDir;
    private static final Logger logger = LoggerFactory.getLogger(DefaultOdServerFactory.class);

	/** {@inheritDoc} */
	@Override
	public ODServer createOdServer() throws ODException {
        // Immutable config object
        ODConfig odConfig = new ODConfig() {
            {
                logger.debug("Configure ODWEK api for installation directory {}", odwekInstallDir);
                setODWEKInstallDir(odwekInstallDir);
                logger.debug("Configure AFP2PDF path and configuration in {}", odwekInstallDir + File.separator + "afp2pdf");
                setAfp2PdfInstallDir(odwekInstallDir + File.separator + "afp2pdf");
                setAfp2PdfConfigFile(odwekInstallDir + File.separator + "afp2pdf" + File.separator + "a2pxopts.cfg");
                if (odwekTraceLevel > 0) {
                    if (odwekTraceDir != null && !odwekTraceDir.isEmpty()) {
                        setTraceDirectory(odwekTraceDir);
                    } else {
                        setTraceDirectory(System.getProperty("java.io.tmpdir"));
                    }
                    setTraceLevel(odwekTraceLevel);
                    logger.debug("Odwek trace enabled level {}. Tracedirectory is {}", odwekTraceLevel, getTraceDirectory());
                    if (odwekTempDir != null && !odwekTempDir.isEmpty()) {
                        setTemporaryWorkingDirectory(odwekTempDir);
                    } else {
                        setTemporaryWorkingDirectory(System.getProperty("java.io.tmpdir"));
                    }
                    logger.debug("Odwek temporary work directory at {}", getTemporaryWorkingDirectory());
                }
            }
        };
        if (logger.isDebugEnabled()) {
            logger.debug("***** ODConfig printed to SystemOut.log ******");
            logger.debug("***** ODConfig start *****");
            odConfig.printConfig();
            logger.debug("***** ODConfig end ******");
        }
		return new ODServer(odConfig);
	}

    public void setOdwekInstallDir(String odwekInstallDir) {
        this.odwekInstallDir = odwekInstallDir;
    }

    public void setOdwekTraceLevel(int odwekTraceLevel) {
        this.odwekTraceLevel = odwekTraceLevel;
    }

    public void setOdwekTraceDir(String odwekTraceDir) {
        this.odwekTraceDir = odwekTraceDir;
    }

    public void setOdwekTempDir(String odwekTempDir) {
        this.odwekTempDir = odwekTempDir;
    }

    public void setOdwekResourceCacheDir(String odwekResourceCacheDir) {
		this.odwekResourceCacheDir = odwekResourceCacheDir;
	}
}
