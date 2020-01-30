package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
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

import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.BEHANDLINGSTEMA;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.BREVKODE;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.DATO_DOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.DATO_MOTTATT;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.EKSTERNPART_NAVN;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.FAGOMRADE;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.INNHOLD;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.JOURNALFOERENDE_ENHET_REF;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.KATEGORI;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.MOTTAKS_KANAL_CODE;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.SENSITIVITET;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.TITTEL;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertFilDetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertKryssreferanser;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertSkannetInnhold;
import static no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2RequestDataUtil.assertTilleggsopplysninger;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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

	@Before
	public void setUp() throws Exception {
		RequestContextSetter.setRequestContextForUnitTest();
		createRequest();
	}

	@Test
	public void testMap() throws Exception {
		JournalforInngaaendeForsendelseRequest request = createRequest();
		JournalforInngaaendeForsendelseV2RequestTo requestTo = mapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainResult = requestTo.getJournalpost();

		assertThat(domainResult, notNullValue());
		assertThat(domainResult.getFagomrade().name(), is(FAGOMRADE.name()));
		assertThat(domainResult.getBehandlingstema().name(), is(BEHANDLINGSTEMA));
		assertThat(domainResult.getOpprettetAvNavn(), is(OPPRETTET_AV_NAVN));
		assertThat(domainResult.getJournalfortAvNavn(), nullValue());
		assertThat(domainResult.getJournalDato(), nullValue());
		assertThat(domainResult.getJournalForendeEnhetId(), is(JOURNALFOERENDE_ENHET_REF));
		assertThat(domainResult.getInnhold(), is(INNHOLD));
		assertThat(domainResult.getDokumentDato(), is(DATO_DOKUMENT));
		assertThat(domainResult.getAvsenderMottaker(), is(EKSTERNPART_NAVN));
		assertThat(domainResult.getAvsenderMottakerId(), is(PERSONIDENT));
		assertThat(domainResult.getMottattDato(), is(DATO_MOTTATT));
		assertThat(domainResult.getMottakskanal(), is(MOTTAKS_KANAL_CODE));

		assertSaksrelasjon(domainResult.getSaksrelasjon());
		assertJournalpostDokumentInfoRelasjon(domainResult.getJournalpostDokumentInfoRelasjoner().iterator().next());
		assertBruker(domainResult.getBrukere().iterator().next());

		assertThat(requestTo.isForsokEndeligJf(), is(SHOULD_ENDELIG_JOURNALFOERES));

		assertTilleggsopplysninger(domainResult.getTilleggsopplysninger());
		assertKryssreferanser(domainResult.getKryssreferanser().iterator().next());
	}

	@Test
	public void shouldMapFagomradeUkjentWhenTemaNull() throws Exception {
		JournalforInngaaendeForsendelseRequest request = createRequest();
		request.getJournalpost().setTema(null);
		JournalforInngaaendeForsendelseV2RequestTo requestTo = mapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainResult = requestTo.getJournalpost();

		assertThat(domainResult.getFagomrade(), is(FagomradeCode.UKJ));
	}

	@Test
	public void shouldMapFagomradeUkjentWhenTemaBlank() throws Exception {
		JournalforInngaaendeForsendelseRequest request = createRequest();
		request.getJournalpost().setTema("");
		JournalforInngaaendeForsendelseV2RequestTo requestTo = mapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainResult = requestTo.getJournalpost();

		assertThat(domainResult.getFagomrade(), is(FagomradeCode.UKJ));
	}

	@Test
	public void shouldRunWhenBehandlingstemaIsNull() throws Exception {
		JournalforInngaaendeForsendelseRequest request = createRequest();
		request.getJournalpost().setBehandlingstema(null);
		JournalforInngaaendeForsendelseV2RequestTo requestTo = mapper.map(request);
		assertThat(requestTo.getJournalpost().getBehandlingstema(), nullValue());
	}

	private void assertJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon dokumentInfoRelasjon) {
		assertDokumentInfo(dokumentInfoRelasjon.getDokumentInfo());
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT));
		assertThat(dokumentInfoRelasjon.getTilknyttetAvNavn(), is(OPPRETTET_AV_NAVN));

	}

	private void assertDokumentInfo(DokumentInfo dokumentInfo) {
		assertFilDetaljer(dokumentInfo.getFildetaljerListe().iterator().next());
		assertSkannetInnhold(dokumentInfo.getSkannetInnholdListe().iterator().next());
		assertThat(dokumentInfo.getKategori().name(), is(KATEGORI));
		assertThat(dokumentInfo.getTittel(), is(TITTEL));
		assertThat(dokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(dokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(dokumentInfo.getSensitivt(), is(SENSITIVITET));
	}

	private JournalforInngaaendeForsendelseRequest createRequest() throws Exception {
		Journalpost wsJournalpost = JournalforInngaaendeForsendelseV2RequestDataUtil.createJournalpost();
		JournalforInngaaendeForsendelseRequest request = new JournalforInngaaendeForsendelseRequest();
		request.setJournalpost(wsJournalpost);
		request.setForsokEndeligJF(SHOULD_ENDELIG_JOURNALFOERES);
		return request;
	}
}