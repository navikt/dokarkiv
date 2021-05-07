package no.nav.dokarkiv.behandlejournal;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Utility class for sporing in MOD
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public final class SporingUtil {

	/**
	 * Make this class uninstantiable
	 */
	private SporingUtil() {
	}

	/**
	 * Decides whether to use personFornavn + personEtternavn or applikasjonsID
	 * personFornavn + personEtternavn has precedence over applikasjonsID
	 *
	 * @param personFornavn
	 * @param personEtternavn
	 * @param applikasjonsID
	 * @return Either "personFornavn personEtternavn" or applikasjonsID
	 */
	public static String decideSporingNavn(String personFornavn, String personEtternavn, String applikasjonsID) {
		if (isBlank(personFornavn) && isBlank(personEtternavn) && isBlank(applikasjonsID)) {
			throw new ApplicationException("personFornavn, personEtternavn or applikasjonsID must be set.");
		}
		if (isNotBlank(personFornavn) && isNotBlank(personEtternavn)) {
			return personFornavn + " " + personEtternavn;
		} else {
			return applikasjonsID;
		}
	}

	/**
	 * Decides whether to use personFornavn + personEtternavn or applikasjonsID
	 * personFornavn + personEtternavn has precedence over applikasjonsID
	 *
	 * @param sporingsMetaData
	 * @return Either "personFornavn personEtternavn" or applikasjonsID
	 */
	public static String decideSporingNavn(SporingsMetaData sporingsMetaData) {
		if (sporingsMetaData == null) {
			throw new ApplicationException(
					"sporingsMetaData with personFornavn, personEtternavn or applikasjonsID must be set.");
		}
		return decideSporingNavn(sporingsMetaData.getPersonFornavn(), sporingsMetaData.getPersonEtternavn(),
				sporingsMetaData.getApplikasjonsID());
	}
}
