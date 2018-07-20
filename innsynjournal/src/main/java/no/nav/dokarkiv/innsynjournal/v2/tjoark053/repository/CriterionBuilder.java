package no.nav.dokarkiv.innsynjournal.v2.tjoark053.repository;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Abstract class containing common methods for building
 * criteria's.
 * 
 * @author Hans Olav Loftum, BEKK
 * @author Rune Romundstad, Sirius IT
 */
public abstract class CriterionBuilder {
	protected Criteria criteria;
	private Session session;
	
	/**
	 * Constructor.
	 * @param session a Hibernate session.
	 */
	protected CriterionBuilder(Session session) {
		this.session = session;
	}
	
	/**
	 * Creates criteria for a specified class, with a given alias.
	 * @param persistedClass the class.
	 * @param alias the alias.
	 * @return a Criteria instance.
	 */
	protected Criteria createCriteria(Class<?> persistedClass, String alias) {
		return session.createCriteria(persistedClass, alias);
	}

	/**
	 * Returns true if a value is null, false if not.
	 * @param value the value.
	 * @return Returns true if a value is null, false if not.
	 */
	protected boolean isNull(Object value) {
		if (value instanceof String) {
			return StringUtils.isBlank((String) value);
		}
		return value == null;
	}

}
