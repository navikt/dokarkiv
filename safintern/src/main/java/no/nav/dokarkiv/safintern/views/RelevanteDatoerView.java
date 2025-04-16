package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.serializers.LocalDateTimeToOffsetDateTimeUTCSerializer;

import java.time.LocalDateTime;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityView(Journalpost.class)
public interface RelevanteDatoerView {

	@JsonSerialize(using = LocalDateTimeToOffsetDateTimeUTCSerializer.class)
	@Mapping("mottattDato")
	LocalDateTime getForsendelseMottatt();

	@JsonSerialize(using = LocalDateTimeToOffsetDateTimeUTCSerializer.class)
	@Mapping("dokumentDato")
	LocalDateTime getHoveddokument();

	@Mapping("journalDato")
	Date getJournalfoert();

	@Mapping("sendtPrintDato")
	Date getSendtPrint();

	@JsonSerialize(using = LocalDateTimeToOffsetDateTimeUTCSerializer.class)
	@Mapping("ekspedertDato")
	LocalDateTime getEkspedert();

	@Mapping("avsendtReturDato")
	Date getRetur();

	@JsonSerialize(using = LocalDateTimeToOffsetDateTimeUTCSerializer.class)
	@Mapping("lestDato")
	LocalDateTime getLest();

	@Mapping("changeStamp.createdDate")
	Date getOpprettet();
}
