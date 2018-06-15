package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Response object for DokumentproduksjonInfo.hentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class HentJournalOgDokumentStatusResponseTo {

    private JournalStatusCode journalStatus;
    private DokumentStatusCode dokumentStatus;
    private Long metaforceInstanceId;

    /**
     * Constructs a new HentJournalOgDokumentStatusResponseTo.
     *
     * @param journalStatus       The journalStatus
     * @param dokumentStatus      The dokumentStatus
     * @param metaforceInstanceId The metaforceInstanceId
     */
    public HentJournalOgDokumentStatusResponseTo(JournalStatusCode journalStatus, DokumentStatusCode dokumentStatus,
                                                 Long metaforceInstanceId) {
        this.journalStatus = journalStatus;
        this.dokumentStatus = dokumentStatus;
        this.metaforceInstanceId = metaforceInstanceId;
    }

    public JournalStatusCode getJournalStatus() {
        return journalStatus;
    }

    public DokumentStatusCode getDokumentStatus() {
        return dokumentStatus;
    }

    public Long getMetaforceInstanceId() {
        return metaforceInstanceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("journalStatus", journalStatus)
                .append("dokumentStatus", dokumentStatus)
                .append("metaforceInstanceId", metaforceInstanceId)
                .toString();
    }

}
