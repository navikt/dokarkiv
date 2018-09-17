package no.nav.dokarkiv.core.document;

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
public class DoksysDokument {

	private byte[] axml;
	private byte[] pdf;
}
