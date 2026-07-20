package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.util.Comparator;

@EntityView(SkannetInnhold.class)
public interface LogiskVedleggView {
	@IdMapping("skannetInnholdId")
	Long getVedleggId();

	@Mapping("vedleggInnhold")
	String getTittel();


	static class DefaultComparator implements Comparator<LogiskVedleggView> {

		@Override
		public int compare(LogiskVedleggView o1, LogiskVedleggView o2) {
			return Long.compare(o1.getVedleggId(), o2.getVedleggId());
		}
	}
}
