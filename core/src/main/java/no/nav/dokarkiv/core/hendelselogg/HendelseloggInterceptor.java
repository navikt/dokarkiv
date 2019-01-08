package no.nav.dokarkiv.core.hendelselogg;

import static no.nav.dokarkiv.core.hendelselogg.HendelseLoggService.HENDELSE_INFO_HEADER;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Hendelselogg;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.repository.Hendelseloggrepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 *
 */
@Slf4j
@Component
public class HendelseloggInterceptor implements HandlerInterceptor {


	private final Hendelseloggrepository hendelseloggrepository;

	public HendelseloggInterceptor(Hendelseloggrepository hendelseloggrepository) {
		this.hendelseloggrepository = hendelseloggrepository;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
		RequestContextUtil.createAndSetUsername(MDC.get(MDCConstants.MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

		String hendelseInfo = request.getHeader(HENDELSE_INFO_HEADER);

		if (isBlank(hendelseInfo)) {
			return;
		}

		Hendelselogg hendelselogg = convertToHendelseLoggObject(hendelseInfo);
		if (Objects.isNull(hendelselogg)) {
			throw new DokarkivFunctionalException("Feil");
		}

		hendelseloggrepository.save(hendelselogg);

	}


	private Hendelselogg convertToHendelseLoggObject(String hendelseInfoHeader) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(hendelseInfoHeader, Hendelselogg.class);
		} catch (IOException e) {
			return null;
		}
	}
}