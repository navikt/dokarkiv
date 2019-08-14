package no.nav.dokarkiv.sak.validering;

import lombok.experimental.UtilityClass;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Arrays;

@UtilityClass
final class CountFieldsMatching {

	static Long count(Object o, String[] fields) {
		return Arrays.stream(fields).filter(field -> {
			try {
				return BeanUtils.getProperty(o, field) != null;
			} catch (Exception e) {
				throw new IllegalStateException("Kunne ikke telle antall felter");
			}
		}).count();
	}
}
