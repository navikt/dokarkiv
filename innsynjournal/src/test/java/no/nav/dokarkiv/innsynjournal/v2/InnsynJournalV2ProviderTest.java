package no.nav.dokarkiv.innsynjournal.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.AuthorizationException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentJournalpostListeToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligJournalpostListeV2ResponseMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligeJournalpostListeV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.SakFagsystem;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2RequestMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2ResponseMapper;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentTilgjengeligJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Dokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

/**
 * Unit tests for {@link InnsynJournalV2Provider}
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class InnsynJournalV2ProviderTest {

	private static final String JOURNALPOST_ID = "1";
	private static final String DOKUMENT_ID = "2";
	private static final byte[] FILE = "testfile".getBytes();
	private static final String KANAL_REFERANSE_ID = "kanalRef1";
	private static final MottaksKanalCode MOTTAK_KANAL = MottaksKanalCode.NAV_NO;
	private static final String HOVEDDOKUMENT1 = "Hoveddokument1";
	private static final String VEDLEGG1 = "Vedlegg1";
	private static final String VEDLEGG2 = "Vedlegg2";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private InnsynJournalV2SecurityFacade securityFacade;
	@Mock
	private HentMinTilgjengeligeJournalpostListeV2RequestMapper hentMinTilgjengeligeJournalpostListeV2RequestMapper;
	@Mock
	private HentMinTilgjengeligJournalpostListeV2ResponseMapper hentMinTilgjengeligJournalpostListeV2ResponseMapper;
	@Mock
	private IdentifiserJournalpostV2RequestMapper identifiserJournalpostV2RequestMapper;
	@Mock
	private IdentifiserJournalpostV2ResponseMapper identifiserJournalpostV2ResponseMapper;

	@InjectMocks
	private InnsynJournalV2Provider innsynJournalV2Provider;

	@Before
	public void setUp() throws Exception {
//		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
//		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
	}

	@Test
	public void shouldPing() throws Exception {
		innsynJournalV2Provider.ping();
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIdIsNull() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(innsynJournalV2Provider.JOURNALPOSTID_REQIRED);

		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(null);
		request.setDokumentId("1");

		innsynJournalV2Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIdIsEmpty() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(innsynJournalV2Provider.JOURNALPOSTID_REQIRED);

		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId("");
		request.setDokumentId("1");

		innsynJournalV2Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentIdIsNull() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(innsynJournalV2Provider.DOKUMENTID_REQUIRED);

		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId("1");
		request.setDokumentId(null);

		innsynJournalV2Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentIdIsEmpty() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(innsynJournalV2Provider.DOKUMENTID_REQUIRED);

		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId("1");
		request.setDokumentId("");

		innsynJournalV2Provider.hentDokument(request);
	}

	@Test
	public void shouldReturnResponse() throws Exception {
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID)))).thenReturn(FILE);
		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(createDefaultRequest());

		assertThat(response.getVariantFormat().getValue(), is(VariantFormatCode.ARKIV.name()));
		assertThat(response.getDokument(), is(FILE));
	}

	@Test
	public void shouldThrowHentDokumentDokumentIkkeFunnetWhenNoJournalpostFound() throws Exception {
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID))))
				.thenThrow(new NoJournalpostFoundException("not found", 1L));

		verifyHentDokumentIkkeFunnetExceptionThrown();
	}

	@Test
	public void shouldThrowHentDokumentDokumentIkkeFunnetWhenDokumentIkkeFunnet() throws Exception {
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID))))
				.thenThrow(new DocumentNotFoundException("not found"));

		verifyHentDokumentIkkeFunnetExceptionThrown();
	}

	@Test
	public void shouldPopulateFaultHentDokumentDokumentIkkeFunnet() throws Exception {
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID))))
				.thenThrow(new NoJournalpostFoundException("not found", 1L));

		verifyHentDokumentIkkeFunnetExceptionThrown();
	}

	@Test
	public void shouldThrowHentDokumentSikkerhetsbegrensningWhenXACMLDenies() throws Exception {
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID))))
				.thenThrow(new AuthorizationException("Access denied"));

		verifyHentDokumentSikkerhetsbegrensningThrown();
	}

	@Test
	public void shouldThrowHentDokumentSikkerhetsbegrensningWhenFacadeFilterDenies() throws Exception {
		SecurityLimitationAttributeException authException = new SecurityLimitationAttributeException(Long.valueOf(JOURNALPOST_ID), Long.valueOf(DOKUMENT_ID), null);
		when(securityFacade.hentDokument(eq(Long.valueOf(JOURNALPOST_ID)), eq(Long.valueOf(DOKUMENT_ID))))
				.thenThrow(authException);

		verifyHentDokumentSikkerhetsbegrensningThrown();
	}

	@Test
	public void shouldThrowHentTilgjengeligJournalpostListeSikkerhetsbegrensningWhenXACMLDenies() throws Exception {
		HentTilgjengeligJournalpostListeRequest wsRequest = new HentTilgjengeligJournalpostListeRequest();
		when(securityFacade.hentMineTilgjengeligeJournalpostListe(any(HentJournalpostListeToRequest.class)))
				.thenThrow(new AuthorizationException("Access denied"));
		when(hentMinTilgjengeligeJournalpostListeV2RequestMapper.map(wsRequest)).thenReturn(new HentJournalpostListeToRequest());

		try {
			innsynJournalV2Provider.hentTilgjengeligJournalpostListe(wsRequest);
			fail();
		} catch (HentTilgjengeligJournalpostListeSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), is("Access denied"));
		}
	}


	@Test
	public void shouldHentMinTilgjeneligeJournalpostListe() throws Exception {

		HentTilgjengeligJournalpostListeRequest wsRequest = new HentTilgjengeligJournalpostListeRequest();
		HentJournalpostListeToRequest request = new HentJournalpostListeToRequest();
		request.getSaksListe().add(new SakFagsystem());
		ArrayList<InnsynJournalpostTo> innsynList = Lists.newArrayList(new InnsynJournalpostTo(new Journalpost()));
		HentTilgjengeligJournalpostListeResponse response = createResponse();

		when(hentMinTilgjengeligeJournalpostListeV2RequestMapper.map(wsRequest)).thenReturn(request);
		when(securityFacade.hentMineTilgjengeligeJournalpostListe(request)).thenReturn(innsynList);
		when(hentMinTilgjengeligJournalpostListeV2ResponseMapper.mapList(innsynList)).thenReturn(response);

		innsynJournalV2Provider.hentTilgjengeligJournalpostListe(wsRequest);

		verify(hentMinTilgjengeligeJournalpostListeV2RequestMapper).map(wsRequest);
		verify(securityFacade).hentMineTilgjengeligeJournalpostListe(request);
		verify(hentMinTilgjengeligJournalpostListeV2ResponseMapper).mapList(innsynList);
	}

	@Test
	public void shouldRunIdentifiserJournalpost() throws Exception {

		IdentifiserJournalpostRequest wsRequest = new IdentifiserJournalpostRequest();
		IdentifiserJournalpostToRequest request = new IdentifiserJournalpostToRequest();
		request.setKanalReferanseId(KANAL_REFERANSE_ID);
		request.setMottaksKanal(MOTTAK_KANAL);
		InnsynJournalpostTo innsynJournalpostTo = new InnsynJournalpostTo(new Journalpost());
		IdentifiserJournalpostResponse response = createIdentifiserJournalpostResponse();

		when(identifiserJournalpostV2RequestMapper.map(wsRequest)).thenReturn(request);
		when(securityFacade.identifiserJournalpost(request)).thenReturn(innsynJournalpostTo);
		when(identifiserJournalpostV2ResponseMapper.map(innsynJournalpostTo)).thenReturn(response);

		innsynJournalV2Provider.identifiserJournalpost(wsRequest);

		verify(identifiserJournalpostV2RequestMapper).map(wsRequest);
		verify(securityFacade).identifiserJournalpost(request);
		verify(identifiserJournalpostV2ResponseMapper).map(innsynJournalpostTo);
	}

	private HentTilgjengeligJournalpostListeResponse createResponse() {
		HentTilgjengeligJournalpostListeResponse response = new HentTilgjengeligJournalpostListeResponse();
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost = new no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost();
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost2 = new no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost();
		journalpost.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon());
		journalpost.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon());
		journalpost2.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon());
		response.getJournalpostListe().add(journalpost);
		response.getJournalpostListe().add(journalpost2);
		return response;
	}

	private IdentifiserJournalpostResponse createIdentifiserJournalpostResponse() {
		IdentifiserJournalpostResponse response = new IdentifiserJournalpostResponse();
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost = new no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost();
		journalpost.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon());
		journalpost.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon());
		response.setJournalpostId(JOURNALPOST_ID);
		Dokument hovedDokument = new Dokument();
		hovedDokument.setDokumentId(HOVEDDOKUMENT1);
		response.setHoveddokument(hovedDokument);
		Dokument vedlegg1 = new Dokument();
		vedlegg1.setDokumentId(VEDLEGG1);
		response.getVedleggListe().add(vedlegg1);
		Dokument vedlegg2 = new Dokument();
		vedlegg2.setDokumentId(VEDLEGG2);
		response.getVedleggListe().add(vedlegg2);
		return response;
	}


	private void verifyHentDokumentSikkerhetsbegrensningThrown() throws HentDokumentDokumentIkkeFunnet {
		try {
			innsynJournalV2Provider.hentDokument(createDefaultRequest());
			fail();
		} catch (HentDokumentSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), is("Access denied"));
		}
	}

	private void verifyHentDokumentIkkeFunnetExceptionThrown() throws HentDokumentSikkerhetsbegrensning {
		try {
			innsynJournalV2Provider.hentDokument(createDefaultRequest());
			fail();
		} catch (HentDokumentDokumentIkkeFunnet e) {
			assertThat(e.getMessage(), is("not found"));
		}
	}


	private HentDokumentRequest createDefaultRequest() {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(JOURNALPOST_ID);
		request.setDokumentId(DOKUMENT_ID);
		return request;
	}
}