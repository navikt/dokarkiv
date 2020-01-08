package no.nav.dokarkiv.journalpost.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

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

/**
 * Metodene av samme Advice (f.eks @Before) kjøres i alfabetisk rekkefølge, vær obs på løsningsbeskrivelsen her!
 */
@Aspect
@Component
@Slf4j
public class JournalpostApiAspects {

    @Inject
    private AbacSecurityService abacSecurityService;

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.*(..)) && args(journalpostId)")
    public void aConfigureMDC(JoinPoint point, String journalpostId) {
        MDC.put(MDC_REQUEST_ID, "feilregistrer");
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
    }

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.JournalpostInternRestController.*(..))")
    public void aConfigureMDC(JoinPoint point) {
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));
    }

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.*(..)) && args(journalpostId)")
    public void bLog(JoinPoint point, String journalpostId) {
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for feilregistrering av journalpost med journalpostId={}", journalpostId);
    }

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.*(..)) && args(journalpostId)")
    public void cValiderInput(JoinPoint point, String journalpostId) {
        CommonValidator.validateId(journalpostId, "journalpostId");
    }

    @Before("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.*(..)) && args(journalpostId)")
    public void dPerformAccessControl(JoinPoint point, String journalpostId) {
        abacSecurityService.assertAccessToJournalpost(journalpostId);
    }

    @After("execution(* no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.*(..))")
    public void cleanUp() {
        MDC.clear();
    }

}
