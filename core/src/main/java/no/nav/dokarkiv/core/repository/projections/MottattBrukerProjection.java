package no.nav.dokarkiv.core.repository.projections;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;


public interface MottattBrukerProjection {

	String getBrukerId();

	String getBrukerType();

	@Value("#{target.changeStamp.createdDate}")
	LocalDateTime getDatoOpprettet();

}