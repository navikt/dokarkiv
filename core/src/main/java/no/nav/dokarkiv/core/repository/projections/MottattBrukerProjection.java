package no.nav.dokarkiv.core.repository.projections;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;


public interface MottattBrukerProjection {

	String getBrukerId();

	String getBrukerType();

	@Value("#{target.changeStamp.createdDate}")
	Date getDatoOpprettet();

}