package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;


import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.assertFilDetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.toXMLGregorianCalendar;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.core.config.DozerConfig;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.journabehandling.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for {@link DefaultJournalforInngaaendeForsendelseRequestMapper}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 21.02.2017
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class DefaultJournalforInngaaendeForsendelseRequestMapperTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private KildeNavnPopulator kildeNavnPopulator;

	@InjectMocks
	private DefaultJournalforInngaaendeForsendelseRequestMapper mapper;

	private Journalpost journalpostRequest;
	private JournalforInngaaendeForsendelseRequest request;

	@Before
	public void setUp() throws Exception {
		createRequest();
		mapper.setDozerMapper(new DozerConfig().dozerMapper());
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void testMap() throws Exception {
		JournalforInngaaendeForsendelseRequestTo requestTo = mapper.map(request);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainResult = requestTo.getJournalpost();
		assertThat(domainResult, notNullValue());
		assertThat(domainResult.getFagomrade().name(), is(journalpostRequest.getTema()));
		assertThat(domainResult.getOpprettetAvNavn(), is(journalpostRequest.getOpprettetAvNavn()));
		assertThat(domainResult.getJournalfortAvNavn(), is(journalpostRequest.getOpprettetAvNavn()));
		assertThat(domainResult.getJournalForendeEnhetId(), is(journalpostRequest.getJournalforendeEnhet()));
		assertThat(domainResult.getInnhold(), is(journalpostRequest.getInnhold()));
		assertThat(toXMLGregorianCalendar(domainResult.getDokumentDato()), is(journalpostRequest.getDatoDokument()));
		assertThat(domainResult.getAvsenderMottaker(), is(journalpostRequest.getAvsenderMottakerNavn()));
		assertThat(domainResult.getAvsenderMottakerId(), is(journalpostRequest.getAvsenderMottakerId()));
		assertThat(toXMLGregorianCalendar(domainResult.getMottattDato()), is(journalpostRequest.getDatoMottatt()));
		assertThat(domainResult.getMottakskanal().name(), is(journalpostRequest.getMottakskanal()));
		assertThat(domainResult.getTilleggsopplysninger(),
				hasEntry(
						request.getJournalpost().getJournalpostTilleggsopplysninger().get(0).getOpplysningsnoekkel(),
						request.getJournalpost().getJournalpostTilleggsopplysninger().get(0).getOpplysningsverdi()));

		assertSaksrelasjon(domainResult.getSaksrelasjon(), journalpostRequest.getSaksrelasjon());
		assertJournalpostDokumentInfoRelasjon(domainResult.getJournalpostDokumentInfoRelasjoner().iterator().next());
		assertBruker(domainResult.getBrukere().iterator().next(), journalpostRequest.getBruker());
	}

	private void assertJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon dokumentInfoRelasjon) {
		assertDokumentInfo(dokumentInfoRelasjon.getDokumentInfo());
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom().name(),
				is(journalpostRequest.getJournalpostDokumentInfoRelasjon().get(0).getTilknyttetJournalpostSom().name()));
	}

	private void assertDokumentInfo(DokumentInfo dokumentInfo) {
		assertFilDetaljer(dokumentInfo.getFildetaljerListe().iterator().next(), false);
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
		journalpostRequest = JournalforInngaaendeForsendelseRequestDataUtil.createJournalpost();
		request = new JournalforInngaaendeForsendelseRequest();
		request.setJournalpost(journalpostRequest);
	}
}