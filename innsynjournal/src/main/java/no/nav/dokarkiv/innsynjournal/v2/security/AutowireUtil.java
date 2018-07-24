package no.nav.dokarkiv.innsynjournal.v2.security;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * Workaround util som lar oss gjøre autowireing på objekter som ikke spring hånterer<br>
 * Dersom spring har opprettet denne beanen vil alle autowireBean kall føre til øyeblikkelig autowireing<br>
 * dersom det ikke er laget noen bean for denne klassen vil autowireing skje når spring lager denne beanen
 * <p/>
 * Copied from AA-Reg (Unknown author)
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class AutowireUtil {

	private static final List<Object> DELAYED_AUTOWIRE_OBJECTS = new LinkedList<>();
	private static AutowireCapableBeanFactory beanFactory;

	public static synchronized void autowireBean(Object object) {
		if (beanFactory != null) {
			// autowire now
			beanFactory.autowireBean(object);
		} else {
			// add to delay list and autowire when util bean is created
			DELAYED_AUTOWIRE_OBJECTS.add(object);
		}
	}

	private static void setAutowireCapableBeanFactory(AutowireCapableBeanFactory bean) {
		beanFactory = bean;
	}

	public AutowireUtil(final AutowireCapableBeanFactory beanFactory) {
		setAutowireCapableBeanFactory(beanFactory);
		delayedAutowiring();
	}

	private synchronized void delayedAutowiring() {
		for (Object o : DELAYED_AUTOWIRE_OBJECTS) {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(o);
			beanFactory.autowireBean(o);
		}
		DELAYED_AUTOWIRE_OBJECTS.clear();
	}
}

