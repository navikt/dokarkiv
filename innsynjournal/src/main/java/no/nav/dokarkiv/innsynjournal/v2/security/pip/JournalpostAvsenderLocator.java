package no.nav.dokarkiv.innsynjournal.v2.security.pip;

import static no.nav.dokarkiv.innsynjournal.v2.security.pip.LocatorCommon.createEmptyEvaluationResult;

import com.google.common.collect.Sets;
import no.nav.dokarkiv.innsynjournal.v2.security.AutowireUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.security.xacml.sunxacml.EvaluationCtx;
import org.jboss.security.xacml.sunxacml.attr.BagAttribute;
import org.jboss.security.xacml.sunxacml.attr.StringAttribute;
import org.jboss.security.xacml.sunxacml.cond.EvaluationResult;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.net.URI;

/**
 * A locator that binds journalpost.avsenderMottakerId to the security context
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class JournalpostAvsenderLocator extends AbstractJournalpostAttributeLocator {

	private static final String AVSENDER_FNR_HQL = "select j.avsenderMottakerId from Journalpost j " +
			"where j.journalpostId = :journalpostId";

	@Inject
	private EntityManager entityManager;

	public JournalpostAvsenderLocator() {
		AutowireUtil.autowireBean(this);
	}

	@Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, URI issuer, URI subjectCategory,
										  EvaluationCtx context, int designatorType) {
		if (!ids.contains(attributeId)) {
			return createEmptyEvaluationResult(attributeId, attributeType);
		}

		Long jpId = getSubstituteValue(attributeType, context);
		Query avsenderQuery = createAvsenderQuery(jpId);

		String avsenderFnr = (String) avsenderQuery.uniqueResult();

		if (avsenderFnr != null) {
			return new EvaluationResult(new BagAttribute(attributeType,
					Sets.newHashSet(new StringAttribute(avsenderFnr))));
		}

		return createEmptyEvaluationResult(attributeId, attributeType);
	}

	private Query createAvsenderQuery(Long jpId) {
		return entityManager.unwrap(Session.class)
				.createQuery(AVSENDER_FNR_HQL)
				.setParameter("journalpostId", jpId);
	}
}
