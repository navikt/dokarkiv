package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.util.Comparator;

@EntityView(SkannetInnhold.class)
public interface LogiskVedleggView {
	@IdMapping("skannetInnholdId")
	Long getVedleggId();

	@Mapping("vedleggInnhold")
	String getTittel();

	@JsonIgnore
	@Mapping("vedleggNr")
	Integer getVedleggNr();

	class DefaultComparator implements Comparator<LogiskVedleggView> {

		@Override
		public int compare(LogiskVedleggView o1, LogiskVedleggView o2) {
			return Comparator.comparing(
							LogiskVedleggView::getVedleggNr,
							Comparator.nullsLast(Comparator.naturalOrder()))
					.thenComparing(LogiskVedleggView::getVedleggId)
					.compare(o1, o2);
		}
	}
}
