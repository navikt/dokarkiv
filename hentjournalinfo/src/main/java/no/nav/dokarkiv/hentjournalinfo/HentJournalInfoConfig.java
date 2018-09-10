package no.nav.dokarkiv.hentjournalinfo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Configuration
@ComponentScan
@PropertySource("classpath:graphql.properties")
public class HentJournalInfoConfig {
}
