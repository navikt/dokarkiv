package no.nav.dokarkiv.core.domain.builder;

/**
 * Base class for builders.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @param <T> The object to build.
 */
@Deprecated // bruk lombok builder istedet
public abstract class Builder<T> {

	private String userId = "builderUserId";
	private String componentId = "builderComponentId";
	
	public abstract T build();
		
	public Builder<T> userId(String userId) { this.userId = userId; return this; }
	public Builder<T> componentId(String componentId) { this.componentId = componentId; return this; }
//
//	public T buildAndPersist(HibernateOperations hibernateOperations) {
//		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
//																		.userId(userId)
//																		.componentId(componentId)
//																		.build());
//		T objectToPersist = build();
//		hibernateOperations.persist(objectToPersist);
//		hibernateOperations.flush();
//		return objectToPersist;
//	}
//
//	public T buildAndPersist(Session session) {
//		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
//																		.userId(userId)
//																		.componentId(componentId)
//																		.build());
//		T objectToPersist = build();
//		session.persist(objectToPersist);
//		return objectToPersist;
//	}

}