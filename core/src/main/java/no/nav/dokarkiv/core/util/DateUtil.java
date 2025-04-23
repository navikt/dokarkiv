package no.nav.dokarkiv.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Deprecated
public class DateUtil {


	public static Date getDateNow() {
		return java.sql.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
	}
}
