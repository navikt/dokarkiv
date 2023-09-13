package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(Bruker.class)
public interface BrukerView {
	@Mapping("brukerId")
	String getId();

	@Mapping("brukerType")
	BrukerTypeCode getType();
}
