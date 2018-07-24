package no.nav.dokarkiv.innsynjournal.v2.security.pip;

import org.jboss.security.xacml.sunxacml.attr.BagAttribute;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;

import java.net.URI;

/**
 * Utility class with common operations for locators
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class LocatorCommon {

	/**
	 * Creates an empty EvaluationResult with type attributeType. If attributeType is null, attributeId is used as type.
	 *
	 * @param attributeId The attributeId
	 * @param attributeType The attributeType
	 * @return The empty evaluation result
	 */
	public static EvaluationResult createEmptyEvaluationResult(URI attributeId, URI attributeType) {
		if (attributeType != null) {
			return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
		}
		return new EvaluationResult(BagAttribute.createEmptyBag(attributeId));
	}

	private LocatorCommon() {

	}
}
