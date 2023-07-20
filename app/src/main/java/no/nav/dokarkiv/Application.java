package no.nav.dokarkiv;

import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConfig;
import no.nav.dokarkiv.arkivervariant.ArkiverVariantConfig;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.dokumentproduksjoninfo.DokumentproduksjonInfoConfig;
import no.nav.dokarkiv.hentjournalsakinfo.HentJournalsakinfoConfig;
import no.nav.dokarkiv.safintern.SafinternConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import({CoreConfig.class,
		DokumentproduksjonInfoConfig.class,
		ArkiverDokumentproduksjonConfig.class,
		HentJournalsakinfoConfig.class,
		ArkiverVariantConfig.class,
		AdminConfig.class,
		JournalpostConfig.class,
		SafinternConfig.class
})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
