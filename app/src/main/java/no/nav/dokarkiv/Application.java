package no.nav.dokarkiv;

import no.nav.dokarkiv.core.CoreConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@Import(CoreConfig.class)
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
