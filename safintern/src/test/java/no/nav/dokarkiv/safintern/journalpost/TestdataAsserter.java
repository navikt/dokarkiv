package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.safintern.views.BrukerView;
import no.nav.dokarkiv.safintern.views.SaksrelasjonView;

import static no.nav.dokarkiv.core.util.TestDataGenerator.AKTOER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.GSAK_APPLIKASJON;
import static no.nav.dokarkiv.core.util.TestDataGenerator.GSAK_FAGSAKNR;
import static no.nav.dokarkiv.core.util.TestDataGenerator.GSAK_TEMA;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_ORGNR;
import static org.assertj.core.api.Assertions.assertThat;

public final class TestdataAsserter {

	static void assertSak(Long sakId, SaksrelasjonView saksrelasjon) {
		assertThat(saksrelasjon.getSakId()).isEqualTo(sakId);
		assertThat(saksrelasjon.getFagsystem()).isEqualTo(FagsystemCode.FS22);
		assertThat(saksrelasjon.getFeilregistrert()).isFalse();
		assertThat(saksrelasjon.getSak().getSakId()).isEqualTo(sakId);
		assertThat(saksrelasjon.getSak().getTema()).isEqualTo(GSAK_TEMA);
		assertThat(saksrelasjon.getSak().getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(saksrelasjon.getSak().getOrgNr()).isEqualTo(GSAK_ORGNR);
		assertThat(saksrelasjon.getSak().getFagsakNr()).isEqualTo(GSAK_FAGSAKNR);
		assertThat(saksrelasjon.getSak().getApplikasjon()).isEqualTo(GSAK_APPLIKASJON);
		assertThat(saksrelasjon.getSak().getOpprettetTid()).isNotNull();
	}

	static void assertBruker(BrukerView brukerView) {
		assertThat(brukerView.getId()).isEqualTo(BRUKER_ID);
		assertThat(brukerView.getType()).isEqualTo(BrukerTypeCode.PERSON);
	}
}
