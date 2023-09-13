package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(Journalpost.class)
public interface AvsenderMottakerView {
	@Mapping("avsenderMottakerId")
	String getId();

	@Mapping("avsenderMottakerIdType")
	AvsenderMottakerIdTypeCode getType();

	@Mapping("avsenderMottaker")
	String getNavn();

	@Mapping("land")
	String getLand();
}

