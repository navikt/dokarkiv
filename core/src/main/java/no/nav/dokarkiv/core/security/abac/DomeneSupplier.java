package no.nav.dokarkiv.core.security.abac;

import no.nav.freg.abac.core.annotation.attribute.AttributeSupplier;

public class DomeneSupplier implements AttributeSupplier {
    @Override
    public Object get() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("no.nav.dokarkiv.journalpost")) {
                return JoarkAbacAttributes.ARKIV_V2;
            }
        }

        return JoarkAbacAttributes.ARKIV;
    }

}
