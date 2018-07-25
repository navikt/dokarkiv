package no.nav.dokarkiv.core.security.abac;

import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.util.Collections;
import java.util.List;

/**
 * Container for all resources needed by the ABAC policies for Joark
 *
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Data
public class AbacResources {
	String sakId;
	FagsystemCode fagsystem;
	List<String> brukerIds;
	FagomradeCode fagomrade;

	public AbacResources() {
		sakId = "";
		brukerIds = Collections.emptyList();
	}
}
