package no.nav.dokarkiv.hentdokument.hentdokumenturlinfo;

import static no.nav.dokarkiv.core.domain.builder.DokumentUrlInfoBuilder.getDokumentUrlInfoBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.UrlNotValidException;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepositoryBegrenset;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.DefaultHentDokumentUrlInfo;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.hentdokument.dokumenturlinfo.HentDokumentUrlInfoResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Unit tests for DefaultHentDokumentUrlInfo.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultHentDokumentUrlInfoTest {

	private static final String DOC_TOKEN = "token";
	private static final long TIME_TO_LIVE = 1;

	private DefaultHentDokumentUrlInfo hentDokumentUrlInfo;

	@Mock
    private DokumentUrlInfoRepositoryBegrenset dokumentUrlInfoRepository;

	private HentDokumentUrlInfoRequest request;

	@Before
	public void before() {
		DateProvider.configure(false, null);

		MockitoAnnotations.initMocks(this);
		hentDokumentUrlInfo = new DefaultHentDokumentUrlInfo(TIME_TO_LIVE, dokumentUrlInfoRepository);
		request = new HentDokumentUrlInfoRequest(DOC_TOKEN);
	}

	@Test
	public void shouldThrowExceptionForNullRequest() {
		try {
			hentDokumentUrlInfo.hentDokumentUrlInfo(null);
		} catch (InvalidArgumentException e) {
			assertThat(e.getArgumentName(), is("hentDokumentUrlInfoRequest"));
		}
	}

	@Test
	public void shouldThrowExceptionForMissingDocTokenInInput() {
		request = new HentDokumentUrlInfoRequest(null);
		try {
			hentDokumentUrlInfo.hentDokumentUrlInfo(request);
		} catch (InvalidArgumentException e) {
			assertThat(e.getArgumentName(), is("docToken"));
		}
	}

	@Test
	public void shouldReturnValidDokumentUrlInfo() {
		DokumentUrlInfo dokumentUrlInfo = createDokumentUrlInfo(DOC_TOKEN, new Date());
		when(dokumentUrlInfoRepository.findByDoctoken(DOC_TOKEN)).thenReturn(Optional.of(dokumentUrlInfo));

		assertHentDokumentUrlInfoReturns(dokumentUrlInfo);
	}

	@Test
	public void shouldThrowExceptionForExpiredUrl() {
		Calendar calendar = createExpiredDefaultCalendar();

		DokumentUrlInfo dokumentUrlInfo = createDokumentUrlInfo(DOC_TOKEN, calendar.getTime());
		when(dokumentUrlInfoRepository.findByDoctoken(DOC_TOKEN)).thenReturn(Optional.of(dokumentUrlInfo));

		assertUrlNotValidExceptionThrow(dokumentUrlInfo);
	}

	@Test
	public void shouldValidateUrlAgainstSpecificTimeToLiveIfPresent() {
		Calendar calendar = createExpiredDefaultCalendar();

		DokumentUrlInfo dokumentUrlInfo = createDokumentUrlInfo(DOC_TOKEN, calendar.getTime(), 2L);
		when(dokumentUrlInfoRepository.findByDoctoken(DOC_TOKEN)).thenReturn(Optional.of(dokumentUrlInfo));

		assertHentDokumentUrlInfoReturns(dokumentUrlInfo);
	}

	@Test
	public void shouldThrowExceptionForExpiredUrlWithCustomTimeToLive() {
		Calendar calendar = createExpiredDefaultCalendar();
		calendar.add(Calendar.MINUTE, -1);

		DokumentUrlInfo dokumentUrlInfo = createDokumentUrlInfo(DOC_TOKEN, calendar.getTime(), 2L);
		when(dokumentUrlInfoRepository.findByDoctoken(DOC_TOKEN)).thenReturn(Optional.of(dokumentUrlInfo));

		assertUrlNotValidExceptionThrow(dokumentUrlInfo);
	}

	private void assertUrlNotValidExceptionThrow(DokumentUrlInfo dokumentUrlInfo) {
		try {
			hentDokumentUrlInfo.hentDokumentUrlInfo(request);
			fail("Expected exception");
		} catch (UrlNotValidException e) {
			assertThat(e.getDokumentUrlInfo(), is(dokumentUrlInfo));
		}
	}

	private void assertHentDokumentUrlInfoReturns(DokumentUrlInfo dokumentUrlInfo) {
		HentDokumentUrlInfoResponse response = hentDokumentUrlInfo.hentDokumentUrlInfo(request);

		assertThat(response.getDokumentUrl(), is(dokumentUrlInfo));
	}

	private Calendar createExpiredDefaultCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, (int) -TIME_TO_LIVE);
		calendar.add(Calendar.MILLISECOND, -1);
		return calendar;
	}

	private DokumentUrlInfo createDokumentUrlInfo(String docToken, Date timestamp) {
		return createDokumentUrlInfo(docToken, timestamp, null);
	}

	private DokumentUrlInfo createDokumentUrlInfo(String docToken, Date timestamp, Long timeToLive) {
		return getDokumentUrlInfoBuilder().docToken(docToken).tidspunkt(timestamp).timeToLiveMinutes(timeToLive).build();
	}

}
