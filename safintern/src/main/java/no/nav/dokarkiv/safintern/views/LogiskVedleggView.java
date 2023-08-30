package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

@EntityView(SkannetInnhold.class)
public interface LogiskVedleggView {
	@IdMapping("skannetInnholdId")
	Long getVedleggId();

	@Mapping("vedleggInnhold")
	String getTittel();
}
