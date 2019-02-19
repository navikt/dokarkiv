package no.nav.dokarkiv.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class SlettArkivenhetResponse {

	private Long journalpostId;
	private Long dokumentInfoId;
}
