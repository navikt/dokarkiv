package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;

import java.util.Date;

/**
 * Builder for {@link ChangeStamp}.
 *
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Deprecated // bruk lombok builder istedet
public class ChangeStampBuilder extends Builder<ChangeStamp> {

    private String createdBy;
    private Date createdDate;
    private String updatedBy;
    private Date updatedDate;

    private ChangeStampBuilder(){
	}

	public static ChangeStampBuilder aChangeStamp(){
		return new ChangeStampBuilder();
	}

    public ChangeStampBuilder but() {
        return aChangeStamp()
                .withCreatedBy(createdBy)
                .withCreatedDate(createdDate)
                .withUpdatedBy(updatedBy)
                .withUpdatedDate(updatedDate);
    }

	public ChangeStampBuilder withCreatedBy(String value){ this.createdBy = value; return this; }
	public ChangeStampBuilder withCreatedDate(Date value){ this.createdDate = value; return this; }
	public ChangeStampBuilder withUpdatedBy(String value){ this.updatedBy = value; return this; }
	public ChangeStampBuilder withUpdatedDate(Date value){ this.updatedDate = value; return this; }

	@Override
	public ChangeStamp build() {
		return new ChangeStamp(createdBy, createdDate, updatedBy, updatedDate);
	}
}
