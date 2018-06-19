package no.nav.dokarkiv;

import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConfig;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.dokumentproduksjoninfo.DokumentproduksjonInfoConfig;
import no.nav.dokarkiv.nais.NaisContract;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Import({CoreConfig.class,
		DokumentproduksjonInfoConfig.class,
		ArkiverDokumentproduksjonConfig.class,
		NaisContract.class})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
