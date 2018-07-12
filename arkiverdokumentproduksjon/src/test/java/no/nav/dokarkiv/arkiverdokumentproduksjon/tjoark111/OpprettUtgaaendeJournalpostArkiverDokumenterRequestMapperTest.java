package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertDokumentinfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertJournalpostFields;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertKryssReferanse;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.HOVEDDOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KANAL_REF_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.VEDLEGG;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.VEDLEGG_DOK_INFO_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.VEDLEGG_JP_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjonOnlyRequired;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpost;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpostOnlyRequiredValues;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createKryssReferanse;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createSaksrelasjon;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.DefaultKildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Vedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentRequest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpprettUtgaaendeJournalpostArkiverDokumenterRequestMapperTest {

	private OpprettUtgaaendeJournalpostArkiverDokumenterRequestMapper requestMapper = new OpprettUtgaaendeJournalpostArkiverDokumenterRequestMapper(new DefaultKildeNavnPopulator());

	@Before
	public void setUp() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	@Test
	public void shouldMap() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo to = requestMapper.map(createRequest());
		fillWithDummyValues(to);
		assertJournalpostFields(to.getJournalpost());
		assertDokumentinfoRelasjon(to.getJournalpost().getJournalpostDokumentInfoRelasjoner());
		assertBruker(to.getJournalpost().getBrukere());
		assertKryssReferanse(to.getJournalpost().getKryssreferanser());
		assertSaksrelasjon(to.getJournalpost().getSaksrelasjon());
		assertVedlegg(to);
	}

	@Test
	public void shouldMapWithOnlyRequiredValues() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo to = requestMapper.map(createRequestWihtOnlyRequiredValues());
		fillWithDummyValues(to);

		Journalpost journalpost = to.getJournalpost();
		assertThat(journalpost.getUtsendingskanal(), IsNull.nullValue());
		assertThat("JournalforendeEnhet", journalpost.getJournalForendeEnhetId(), IsNull.nullValue());
		assertThat(journalpost.getOpprettetAvNavn(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN));
		assertThat(journalpost.getInnhold(), IsNull.nullValue());
		assertThat(journalpost.getAvsenderMottaker(), IsNull.nullValue());
		assertThat(journalpost.getAvsenderMottakerId(), IsNull.nullValue());
		assertThat(journalpost.getKanalReferanseId(), Matchers.is(KANAL_REF_ID));
		assertThat(journalpost.getJournalposttype(), CoreMatchers.is(JournalpostTypeCode.U));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));

		DokumentInfo domainDokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo();
		assertThat(domainDokumentInfo.getDokumenttypeId(), CoreMatchers.is(DOKUMENT_TYPE_ID));
		assertThat(domainDokumentInfo.getDokumentstatus(), CoreMatchers.is(FERDIGSTILT));
		assertThat(domainDokumentInfo.getTittel(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori()
				.name(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), IsNull.nullValue());
		assertFildetaljer(domainDokumentInfo.getFildetaljerListe().iterator().next());

		assertThat(to.getVedleggList().size(), is(0));
	}

	private void fillWithDummyValues(OpprettUtgaaendeJournalpostArkiverDokumentRequestTo to) {
		//Using same assertion as in OpprettUtgaaendeJournalpostArkiverDokumentIT
		to.getJournalpost().getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> {
			relasjon.getDokumentInfo().getFildetaljerListe().iterator().next().setFilstorrelse("ads");
		});

	}

	private void assertVedlegg(OpprettUtgaaendeJournalpostArkiverDokumentRequestTo to) {
		assertThat(to.getVedleggList().size(), is(1));
		to.getVedleggList().forEach(vedlegg -> {
			assertThat(vedlegg.getDokumentInfoId(), is(Long.valueOf(VEDLEGG_DOK_INFO_ID)));
			assertThat(vedlegg.getKnyttesFraJournalpostId(), is(Long.valueOf(VEDLEGG_JP_ID)));
		});
	}


	private OpprettUtgaaendeJournalpostArkiverDokumentRequest createRequest() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = new OpprettUtgaaendeJournalpostArkiverDokumentRequest();
		request.setForsokFerdigstilling(true);
		request.setBruker(createBruker());
		request.setJournalpost(createJournalpost());
		request.setSaksrelasjon(createSaksrelasjon());
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjon(HOVEDDOKUMENT));
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjon(VEDLEGG));
		request.setKryssreferanse(createKryssReferanse());

		Vedlegg vedlegg = new Vedlegg();
		vedlegg.setDokumentInfoId(VEDLEGG_DOK_INFO_ID);
		vedlegg.setKnyttesFraJournalpostId(VEDLEGG_JP_ID);
		request.getVedlegg().add(vedlegg);
		return request;
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequest createRequestWihtOnlyRequiredValues() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = new OpprettUtgaaendeJournalpostArkiverDokumentRequest();

		request.setJournalpost(createJournalpostOnlyRequiredValues());
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjonOnlyRequired());
		return request;
	}


}