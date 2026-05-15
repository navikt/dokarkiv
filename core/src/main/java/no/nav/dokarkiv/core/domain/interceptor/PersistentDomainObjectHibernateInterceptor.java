package no.nav.dokarkiv.core.domain.interceptor;

import no.nav.dokarkiv.core.domain.AbstractPersistentDomainObject;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.time.LocalDateTime;

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
		return updateChangeStamp(entity, currentState, propertyNames, types);
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
		return updateChangeStamp(entity, state, propertyNames, types);
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
		boolean response = false;
		// Get the current user from the request context:
		String userId = RequestContextHolder.currentRequestContext().getUserId();
		if (userId == null) {
			userId = "DEFAULT_USER_ID";
		}
		String kildeNavn = RequestContextHolder.currentRequestContext().getComponentId();
		boolean isOpprettetNow = false;

		if (entity instanceof AbstractPersistentDomainObject) {
			for (int i = 0; i < currentState.length; i++) {
				Type type = types[i];
				if (type.getReturnedClass().equals(ChangeStamp.class)) {
					ChangeStamp current = (ChangeStamp) currentState[i];

					if (current != null && current.getCreatedBy() != null && current.getCreatedDate() != null) {
						current.updatedBy(userId);
					} else {
						//Only set created fields for new objects
						isOpprettetNow = true;
						currentState[i] = new ChangeStamp(userId, LocalDateTime.now(), null, null);
					}
					response = true;
					break;
				}
			}
		}

		if (isOpprettetNow && entity instanceof AbstractPersistentVersionedDomainObjectWithKilde) {
			response |= setPropertyValueForName(currentState, propertyNames, "opprettetKildeNavn", kildeNavn);
		}
		if (!isOpprettetNow && entity instanceof AbstractPersistentVersionedDomainObjectWithKilde) {
			response |= setPropertyValueForName(currentState, propertyNames, "endretKildeNavn", kildeNavn);
		}
		return response;
	}

	/**
	 * Searches for a field with the given name and sets it to the given value
	 *
	 * @param currentState  The values of the object's variables
	 * @param propertyNames The names of the object's variables
	 * @param propertyName  The name of the property to set to the given value
	 * @param value         The value to set the given propertyName to
	 * @return true if the change stamp is updated, false otherwise
	 */
	private static boolean setPropertyValueForName(Object[] currentState, String[] propertyNames, String propertyName, String value) {
		for (int i = 0; i < currentState.length; i++) {
			if (propertyName.equals(propertyNames[i])) {
				currentState[i] = value;
				return true;
			}
		}
		return false;
	}
}
