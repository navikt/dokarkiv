package no.nav.dokarkiv;

import no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConfig;
import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConfig;
import no.nav.dokarkiv.arkiverkorrigertdokument.ArkiverKorrigertDokumentConfig;
import no.nav.dokarkiv.arkivervariant.ArkiverVariantConfig;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalV1Config;
import no.nav.dokarkiv.behandlejournal.v2.BehandleJournalV2Config;
import no.nav.dokarkiv.behandlejournal.v3.BehandleJournalV3Config;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.dokumentproduksjoninfo.DokumentproduksjonInfoConfig;
import no.nav.dokarkiv.fysiskslettdokument.FysiskSlettDokumentConfig;
import no.nav.dokarkiv.fysisktidligkassasjon.FysiskTidligKassasjonConfig;
import no.nav.dokarkiv.hentdokument.HentDokumentConfig;
import no.nav.dokarkiv.hentjournalinfo.HentJournalInfoConfig;
import no.nav.dokarkiv.hentjournalsakinfo.HentJournalsakinfoConfig;
import no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalV1Config;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalV2Config;
import no.nav.dokarkiv.journal.v3.JournalV3Config;
import no.nav.dokarkiv.journalfoerinngaaende.v1.JournalfoerInngaaendeConfig;
import no.nav.dokarkiv.logiskslettdokument.LogiskSlettDokumentConfig;
import no.nav.dokarkiv.logisktidligkassasjon.LogiskTidligKassasjonConfig;
import no.nav.dokarkiv.nais.NaisContract;
import no.nav.dokarkiv.platform.TomcatConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Import({TomcatConfig.class,
		CoreConfig.class,
		DokumentproduksjonInfoConfig.class,
		ArkiverDokumentproduksjonConfig.class,
		ArkiverDokumentmottakConfig.class,
		BehandleJournalV2Config.class,
		BehandleJournalV3Config.class,
		InnsynJournalV2Config.class,
		InngaaendeJournalV1Config.class,
		BehandleInngaaendeJournalV1Config.class,
		JournalV3Config.class,
		JournalfoerInngaaendeConfig.class,
		HentDokumentConfig.class,
		HentJournalInfoConfig.class,
		HentJournalsakinfoConfig.class,
		LogiskSlettDokumentConfig.class,
		FysiskSlettDokumentConfig.class,
		ArkiverKorrigertDokumentConfig.class,
		LogiskTidligKassasjonConfig.class,
		FysiskTidligKassasjonConfig.class,
		ArkiverVariantConfig.class,
		NaisContract.class})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
