package no.nav.dokarkiv.core.aksjonslogg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AksjonsLoggRequest {

	List<Aksjon> aksjonListe;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Aksjon {
		private Long journalpostId;
		private Long dokumentInfoId;
		private String applikasjon;
		private String aksjon;
		private String hjemmel;
		private String bruker;
		private String arkivElement;
		private String fraVerdi;
		private String tilVerdi;
		private String melding;
		private String utfoertAv;
	}
}
