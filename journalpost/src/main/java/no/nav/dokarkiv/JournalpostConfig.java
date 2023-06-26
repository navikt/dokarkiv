package no.nav.dokarkiv;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"no.nav.dokarkiv.journalpost.v1", "no.nav.dokarkiv.sikkerhetsnivaa"})
public class JournalpostConfig {

}