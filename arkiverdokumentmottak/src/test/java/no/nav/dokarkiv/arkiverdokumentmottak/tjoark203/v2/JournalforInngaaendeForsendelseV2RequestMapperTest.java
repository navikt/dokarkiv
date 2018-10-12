package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.toXMLGregorianCalendar;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertFilDetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertKryssreferanser;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertSkannetInnhold;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.assertTilleggsopplysninger;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalforInngaaendeForsendelseV2RequestMapperTest {
	private static boolean SHOULD_ENDELIG_JOURNALFOERES = true;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private JournalforInngaaendeForsendelseV2RequestMapper mapper;

	private Journalpost journalpostRequest;
	private JournalforInngaaendeForsendelseRequest request;

	@Before
	public void setUp() throws Exception {
		RequestContextSetter.setRequestContextForUnitTest();
		createRequest();
	}

	@Test
	public void testMap() throws Exception {
		JournalforInngaaendeForsendelseV2RequestTo requestTo = mapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainResult = requestTo.getJournalpost();

		assertThat(domainResult, notNullValue());
		assertThat(domainResult.getFagomrade().name(), is(journalpostRequest.getTema()));
		assertThat(domainResult.getBehandlingstema().name(), is(journalpostRequest.getBehandlingstema()));
		assertThat(domainResult.getOpprettetAvNavn(), is(journalpostRequest.getOpprettetAvNavn()));
		assertThat(domainResult.getJournalfortAvNavn(), is(journalpostRequest.getOpprettetAvNavn()));
		assertThat(domainResult.getJournalForendeEnhetId(), is(journalpostRequest.getJournalforendeEnhet()));
		assertThat(domainResult.getInnhold(), is(journalpostRequest.getInnhold()));
		assertThat(toXMLGregorianCalendar(domainResult.getDokumentDato()), is(journalpostRequest.getDatoDokument()));
		assertThat(domainResult.getAvsenderMottaker(), is(journalpostRequest.getAvsenderMottakerNavn()));
		assertThat(domainResult.getAvsenderMottakerId(), is(journalpostRequest.getAvsenderMottakerId()));
		assertThat(toXMLGregorianCalendar(domainResult.getMottattDato()), is(journalpostRequest.getDatoMottatt()));
		assertThat(domainResult.getMottakskanal().name(), is(journalpostRequest.getMottakskanal()));

		assertSaksrelasjon(domainResult.getSaksrelasjon(), journalpostRequest.getSaksrelasjon());
		assertJournalpostDokumentInfoRelasjon(domainResult.getJournalpostDokumentInfoRelasjoner().iterator().next());
		assertBruker(domainResult.getBrukere().iterator().next(), journalpostRequest.getBruker());

		assertThat(requestTo.isForsokEndeligJf(), is(SHOULD_ENDELIG_JOURNALFOERES));

		assertTilleggsopplysninger(domainResult.getTilleggsopplysninger());
		assertKryssreferanser(domainResult.getKryssreferanser().iterator().next());
	}

	private void assertJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon dokumentInfoRelasjon) {
		assertDokumentInfo(dokumentInfoRelasjon.getDokumentInfo());
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom().name(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getTilknyttetJournalpostSom().name()));
		assertThat(dokumentInfoRelasjon.getTilknyttetAvNavn(), is(journalpostRequest.getOpprettetAvNavn()));

	}

	private void assertDokumentInfo(DokumentInfo dokumentInfo) {
		assertFilDetaljer(dokumentInfo.getFildetaljerListe().iterator().next());
		assertSkannetInnhold(dokumentInfo.getSkannetInnholdListe().iterator().next());
		assertThat(dokumentInfo.getKategori().name(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().getKategori()));
		assertThat(dokumentInfo.getTittel(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().getTittel()));
		assertThat(dokumentInfo.getBrevkode(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().getBrevkode()));
		assertThat(dokumentInfo.getDokumenttypeId(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon()
						.get(0)
						.getDokumentInfo()
						.getDokumentTypeId()));
		assertThat(dokumentInfo.getSensitivt(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().isSensitivt()));
	}

	private void createRequest() throws Exception {
		journalpostRequest = JournalforInngaaendeForsendelseV2RequestDataUtil.createJournalpost();
		request = new JournalforInngaaendeForsendelseRequest();
		request.setJournalpost(journalpostRequest);
		request.setForsokEndeligJF(SHOULD_ENDELIG_JOURNALFOERES);
	}
}