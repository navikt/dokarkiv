package no.nav.dokarkiv.core.domain.interceptor;

import no.nav.dokarkiv.core.domain.AbstractPersistentDomainObject;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Date;

/**
 * Hibernate interceptor to update change stamp of persistent domain objects.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class PersistentDomainObjectHibernateInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = ***gammelt_fnr***58677007L;

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
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
								String[] propertyNames, Type[] types) {
		return updateChangeStamp(entity, currentState, types);
	}

	/**
	 * Updates the change stamp columns of the object that is about to be saved.
	 * 
	 * @param entity
	 *            The object to update the change stamp for
	 * @param id
	 *            The id of the object
	 * @param currentState
	 *            The values of the object's variables
	 * @param propertyNames
	 *            The names of the object's variables
	 * @param types
	 *            The types of the object's variables
	 * @return true if the change stamp is updated, false otherwise
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] currentState, String[] propertyNames, Type[] types) {
		return updateChangeStamp(entity, currentState, types);
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
	 * @param types
	 *            The types of the object's variables
	 * @return true if the change stamp is updated, false otherwise
	 */
	private boolean updateChangeStamp(Object entity, Object[] currentState, Type[] types) {
		boolean response = false;
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
						currentState[i] = new ChangeStamp(userId, new Date(), null, null);
					}
					response = true;
					break;
				}
			}
		}
		return response;
	}
}
