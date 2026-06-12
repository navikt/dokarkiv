package no.nav.dokarkiv.core.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrailingSlashRequestLoggingInterceptorTest {

	private final TrailingSlashRequestLoggingInterceptor interceptor = new TrailingSlashRequestLoggingInterceptor();

	@Test
	public void shouldLogWhenRequestPathHasTrailingSlash() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getMethod()).thenReturn("GET");
		when(request.getRequestURI()).thenReturn("/rest/journalpost/");

		List<ILoggingEvent> logs = withLogCapture(() -> interceptor.preHandle(request, response, new Object()));

		assertThat(logs, hasSize(1));
		assertThat(logs.getFirst().getLevel(), is(Level.INFO));
		assertThat(logs.getFirst().getFormattedMessage(), is("Mottok request med trailing slash. method=GET, path=/rest/journalpost/"));
	}

	@Test
	public void shouldNotLogWhenRequestPathDoesNotHaveTrailingSlash() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getMethod()).thenReturn("GET");
		when(request.getRequestURI()).thenReturn("/rest/journalpost");

		List<ILoggingEvent> logs = withLogCapture(() -> interceptor.preHandle(request, response, new Object()));

		assertThat(logs, hasSize(0));
	}

	private List<ILoggingEvent> withLogCapture(Runnable run) {
		Logger logger = (Logger) LoggerFactory.getLogger(TrailingSlashRequestLoggingInterceptor.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);
		try {
			run.run();
			return List.copyOf(listAppender.list);
		} finally {
			logger.detachAppender(listAppender);
			listAppender.stop();
		}
	}
}
