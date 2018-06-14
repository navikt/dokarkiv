package no.nav.dokarkiv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@EnableAutoConfiguration
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
