package no.nav.dokarkiv.behandlejournal.v2;

import static org.apache.commons.lang.StringUtils.isBlank;

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
		if (!isBlank(personFornavn) && !isBlank(personEtternavn)) {
			return personFornavn + " " + personEtternavn;
		} else if (!isBlank(applikasjonsID)) {
			return applikasjonsID;
		} else {
			throw new ApplicationException("personFornavn, personEtternavn or applikasjonsID must be set.");
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
