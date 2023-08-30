package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;

@EntityView(Bruker.class)
public interface BrukerView {
	@Mapping("brukerId")
	String getId();

	@Mapping("brukerType")
	BrukerTypeCode getType();
}
