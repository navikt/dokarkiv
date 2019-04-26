package no.nav.dokarkiv.journalpost.v1;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.validators.CommonValidator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Aspect
@Component
@Slf4j
public class JournalpostApiHelper {

    @Inject
    private AbacSecurityService abacSecurityService;

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerRestController.*(..)) && args(journalpostId)")
    public void configureMDCAndLog(JoinPoint point, String journalpostId) {
        MDC.put(MDC_REQUEST_ID, "feilregistrer");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for feilregistrering av journalpost med journalpostId={}", journalpostId);
        CommonValidator.validateId(journalpostId, "journalpostId");
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
    }

    @After("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerRestController.*(..))")
    public void cleanUp() {
        MDC.clear();
    }

}
