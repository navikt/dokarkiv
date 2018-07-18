package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for the OpprettJournalpost operation in the ArkiverDokumentproduksjon webservice
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostIT extends AbstractArkiverdokumentproduksjonItest {

	private Journalpost wsJournalpost;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private OpprettJournalpostRequest request;
	private OpprettJournalpostResponse response;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-07-17T12:00");
		RequestContextSetter.setRequestContextForUnitTest();
		wsJournalpost = OpprettJournalpostDataUtil.createJournalpost();
		createRequest();

		response = arkiverDokumentproduksjonProvider.opprettJournalpost(request);
		persistedJournalpost = joarkRepository.findById(Long.valueOf(response.getJournalpostId())).get();
	}

	private void createRequest() throws Exception {
		request = new OpprettJournalpostRequest();
		request.setJournalpost(wsJournalpost);
	}

	@Test
	public void shouldVerfiyOpprettJournalpostResponseHasJournalpostAndDokumentId() throws Exception {
		assertThat(response.getJournalpostId(), is(notNullValue()));
		assertThat(response.getDokumentInfoId(), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		OpprettJournalpostAssertUtil.assertEqualJournalposts(persistedJournalpost);
	}

	@Test
	public void shouldVerifyStaticFieldsInJournalpost() throws Exception {
		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = persistedJournalpost.findHoveddokumentDokumentInfoRelasjon();
		DokumentInfo dokumentInfo = Iterables.getFirst(persistedJournalpost.getJournalpostDokumentInfoRelasjoner(), null)
				.getDokumentInfo();

		assertThat(persistedJournalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.D));
		assertThat(persistedJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
		assertThat(dokumentInfoRelasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT));
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.UNDER_REDIGERING));
		assertThat(dokumentInfo.getDokumentFerdigDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldThrowExceptionIfRequestDoesNotValidate() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Journalpost.fagomrade must be set");
		request.getJournalpost().setFagomrade(null);
		response = arkiverDokumentproduksjonProvider.opprettJournalpost(request);
	}

	@Test
	public void shouldThrowExceptionIfRequestIsMissingMetaforceInstanceId() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("MetaforceInstanceId must be set");
		request.getJournalpost().getDokumentInfo().getFildetaljer().setMetaForceInstanceId(0);
		response = arkiverDokumentproduksjonProvider.opprettJournalpost(request);
	}
}
