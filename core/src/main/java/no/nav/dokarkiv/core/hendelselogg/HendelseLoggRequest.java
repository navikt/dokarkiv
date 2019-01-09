package no.nav.dokarkiv.core.hendelselogg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HendelseLoggRequest {

	private Long journalpostId;
	private Long dokumentInfoId;
	private String applikasjon;
	private String aksjon;
	private String hjemmel;
	private String sak;
	private String bruker;
	private String melding;
}
