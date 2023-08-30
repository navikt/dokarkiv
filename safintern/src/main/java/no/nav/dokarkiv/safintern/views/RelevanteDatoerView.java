package no.nav.dokarkiv.safintern.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.Date;

@EntityView(Journalpost.class)
public interface RelevanteDatoerView {
	@Mapping("mottattDato")
	Date getForsendelseMottatt();

	@Mapping("dokumentDato")
	Date getHoveddokument();

	@Mapping("journalDato")
	Date getJournalfoert();

	@Mapping("sendtPrintDato")
	Date getSendtPrint();

	@Mapping("ekspedertDato")
	Date getEkspedert();

	@Mapping("avsendtReturDato")
	Date getRetur();

	@Mapping("lestDato")
	Date getLest();

	@Mapping("changeStamp.createdDate")
	Date getOpprettet();
}
