package no.nav.dokarkiv.core.domain.interceptor;

import no.nav.dokarkiv.core.domain.AbstractPersistentDomainObject;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.time.LocalDateTime;

import static no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde.KILDE_NAVN_LENGTH;

/**
 * Hibernate interceptor to update change stamp of persistent domain objects.
 */
@SuppressWarnings("unused")
public class PersistentDomainObjectHibernateInterceptor implements Interceptor {
	/**
	 * Updates the change stamp columns of the object that is dirty.
	 *
	 * @param entity
	 *            The object to update the change stamp for
	 * @param id
	 *            The id of the object
	 * @param currentState
	 *            The values of the object's variables
	 * @param previousState
	 *            The previous values of the object's variables
	 * @param propertyNames
	 *            The names of the object's variables
	 * @param types
	 *            The types of the object's variables
	 * @return true if the change stamp is updated, false otherwise
	 */
	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
		boolean response = updateChangeStamp(entity, currentState, propertyNames, types);
		response |= updateKildeNavn(entity, currentState, propertyNames, "endretKildeNavn");
		return response;
	}

	/**
	 * Updates the change stamp columns of the object that is about to be saved.
	 *
	 * @param entity
	 *            The object to update the change stamp for
	 * @param id
	 *            The id of the object
	 * @param state
	 *            The values of the object's variables
	 * @param propertyNames
	 *            The names of the object's variables
	 * @param types
	 *            The types of the object's variables
	 * @return true if the change stamp is updated, false otherwise
	 */
	@Override
	public boolean onPersist(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
		boolean response = updateChangeStamp(entity, state, propertyNames, types);
		response |= updateKildeNavn(entity, state, propertyNames, "opprettetKildeNavn");
		return response;
	}

	/**
	 * Updates the change stamp columns of the object that is dirty.
	 *
	 * Gets the user id from the RequestContext.
	 *
	 * @param entity
	 *            The object to update the change stamp for
	 * @param currentState
	 *            The values of the object's variables
	 * @param propertyNames
	 *            The names of the object's variables
	 * @param types
	 *            The types of the object's variables
	 * @return true if the change stamp is updated, false otherwise
	 */
	private boolean updateChangeStamp(Object entity, Object[] currentState, String[] propertyNames, Type[] types) {
		// Get the current user from the request context:
		String userId = RequestContextHolder.currentRequestContext().getUserId();
		if (userId == null) {
			userId = "DEFAULT_USER_ID";
		}

		if (entity instanceof AbstractPersistentDomainObject) {
			for (int i = 0; i < currentState.length; i++) {
				Type type = types[i];
				if (type.getReturnedClass().equals(ChangeStamp.class)) {
					ChangeStamp current = (ChangeStamp) currentState[i];

					if (current != null && current.getCreatedBy() != null && current.getCreatedDate() != null) {
						current.updatedBy(userId);
					} else {
						//Only set created fields for new objects
						currentState[i] = new ChangeStamp(userId, LocalDateTime.now(), null, null);
					}
					return true;
				}
			}
		}
		return false;
	}

	private static boolean updateKildeNavn(Object entity, Object[] state, String[] propertyNames, String kildeNavnFelt) {
		if (entity instanceof AbstractPersistentVersionedDomainObjectWithKilde) {
			String kildeNavn = RequestContextHolder.currentRequestContext().getComponentId();
			if (kildeNavn == null) {
				kildeNavn = "DEFAULT_KILDE_NAVN";
			}
			return setPropertyValueForKey(state, propertyNames, kildeNavnFelt, truncateToMaxSize(kildeNavn, KILDE_NAVN_LENGTH));
		} else {
			return false;
		}
	}

	/**
	 * Searches for a field with the given name and sets it to the given value
	 *
	 * @param currentState  The values of the object's variables
	 * @param propertyNames The names of the object's variables
	 * @param key  The name of the property to set to the given value
	 * @param value         The value to set the given key to
	 * @return true if the change stamp is updated, false otherwise
	 */
	private static boolean setPropertyValueForKey(Object[] currentState, String[] propertyNames, String key, String value) {
		for (int i = 0; i < currentState.length; i++) {
			if (key.equals(propertyNames[i])) {
				currentState[i] = value;
				return true;
			}
		}
		return false;
	}

	private static String truncateToMaxSize(String string, int size) {
		if (string != null && string.length() > size) {
			return string.substring(0, size - 1);
		}
		return string;
	}
}
