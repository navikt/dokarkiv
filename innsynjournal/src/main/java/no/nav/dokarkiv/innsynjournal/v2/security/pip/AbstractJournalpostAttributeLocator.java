package no.nav.dokarkiv.innsynjournal.v2.security.pip;

import no.nav.dokarkiv.innsynjournal.v2.security.XMLTypes;
import no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds;
import org.jboss.security.xacml.locators.attrib.StorageAttributeLocator;
import org.jboss.security.xacml.sunxacml.EvaluationCtx;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;

import java.net.URI;

/**
 * Abstract class for locators accessing information inferred from journalPostId
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public abstract class AbstractJournalpostAttributeLocator extends StorageAttributeLocator {

	static final URI JOURNALPOST_ID = URI.create(AttributeIds.ATTR_RESOURCE_ID);

	/**
	 * Finds the journalpostId from the EvaluationContext
	 *
	 * @param attributeType The type to find. Should always be string.
	 * @param context The context to inspect
	 * @return The journalpostId
	 */
	@Override
	protected Long getSubstituteValue(URI attributeType, EvaluationCtx context) {
		EvaluationResult result = context.getResourceAttribute(XMLTypes.STRING_TYPE_URI, JOURNALPOST_ID, null);
		return Long.valueOf((String) this.getAttributeValue(result, attributeType));
	}
}
