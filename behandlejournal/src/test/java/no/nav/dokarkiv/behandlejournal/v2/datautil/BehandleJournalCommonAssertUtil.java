package no.nav.dokarkiv.behandlejournal.v2.datautil;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Person;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Assert util for common BehandleJournal types
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class BehandleJournalCommonAssertUtil {

	protected static void assertBruker(Bruker bruker, Aktoer aktoer) {
		if (aktoer instanceof Person) {
			assertThat(bruker.getBrukerType(), is(BrukerTypeCode.PERSON));
			assertThat(bruker.getBrukerId(), is(BehandleJournalCommonDataUtil.PERSONIDENT));
		} else if (aktoer instanceof Organisasjon) {
			assertThat(bruker.getBrukerType(), is(BrukerTypeCode.ORGANISASJON));
			assertThat(bruker.getBrukerId(), is(BehandleJournalCommonDataUtil.ORGNR));
		}
	}

	public static void assertTilleggsopplysninger(Map<String, String> domainTilleggsopplysninger,
												  NoekkelVerdiSett wsTilleggsopplysninger) {
		assertThat(domainTilleggsopplysninger.size(), is(wsTilleggsopplysninger.getInneholderNoekkelVerdiPar().size()));
		for (NoekkelVerdiPar noekkelVerdiPar : wsTilleggsopplysninger.getInneholderNoekkelVerdiPar()) {
			assertThat(domainTilleggsopplysninger.get(noekkelVerdiPar.getNoekkel()), is(noekkelVerdiPar.getVerdi()));
		}
	}

	protected static void assertFildetaljer(FilDetaljer domainFildetaljer)
			throws Exception {
		assertThat(domainFildetaljer.getFilnavn(), is(BehandleJournalCommonDataUtil.FILNAVN));
		assertThat(domainFildetaljer.getFiltype().name(), is(BehandleJournalCommonDataUtil.FILTYPE));
		assertThat(domainFildetaljer.getVariantFormat().name(), is(BehandleJournalCommonDataUtil.VARIANTFORMAT));
		assertThat(domainFildetaljer.getFileContent(), is(BehandleJournalCommonDataUtil.DOKUMENT_INNHOLD.getBytes()));
	}
}
