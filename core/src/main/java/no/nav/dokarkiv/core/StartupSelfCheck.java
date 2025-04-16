package no.nav.dokarkiv.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;

@Component
@Profile("nais")
@Slf4j
public class StartupSelfCheck implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		checkTimezone();
	}

	private void checkTimezone() {
		ZoneId zoneIdSystemDefault = ZoneId.systemDefault();
		if (!ZONEID_NORGE.equals(zoneIdSystemDefault)) {
			throw new IllegalStateException("Starter ikke opp. ZoneId.systemDefault() må være " + ZONEID_NORGE + ". ZoneId.systemDefault()=" + zoneIdSystemDefault);
		}
	}
}
