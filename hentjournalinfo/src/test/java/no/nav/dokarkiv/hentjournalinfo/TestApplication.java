package no.nav.dokarkiv.hentjournalinfo;

import no.nav.dokarkiv.core.CoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Starts only this module. Reduces Application starttime
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@SpringBootApplication
@Import(value = {CoreConfig.class, HentJournalInfoConfig.class})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }


}
