package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	@BeforeEach
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
		request.getJournalpost().setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpost(request),
				"Journalpost.fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfRequestIsMissingMetaforceInstanceId() throws Exception {
		request.getJournalpost().getDokumentInfo().getFildetaljer().setMetaForceInstanceId(0);

		assertThrows(ApplicationException.class,
				() -> response = arkiverDokumentproduksjonProvider.opprettJournalpost(request),
				"MetaforceInstanceId must be set");
	}
}
