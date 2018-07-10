package no.nav.dokarkiv.behandlejournal.v2;

import static org.apache.commons.lang.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Kommunikasjonskanaler;
import org.springframework.stereotype.Component;

/**
 * Implementation of JournalpostMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultJournalpostMapper implements JournalpostMapper {

//	private Mapper dozerMapper;

	public Journalpost map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost wsJournalpost) {
		if (wsJournalpost == null) {
			return null;
		}
		//FIXME
		Journalpost domainJournalpost = null;// = dozerMapper.map(wsJournalpost, Journalpost.class);
		JournalfoertDokumentInfo journalfoertDokumentInfo = wsJournalpost.getJournalfoertDokument();

		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		//FIXME
		DokumentInfo dokumentInfo = null;//dozerMapper.map(journalfoertDokumentInfo, DokumentInfo.class);
		// We explicitly null out dokumentTypeId since JournalfoertDokumentInfo is the same type for
		// ArkiverUstrukturertKrav and JournalfoerInngaaendeHenvendelseMedHoveddokument. 
		// They use the same mapper that sets both brevkode and dokumentTypeId 
		dokumentInfo.setDokumenttypeId(null);
		dokumentInfoRelasjon.setDokumentInfo(dokumentInfo);
		domainJournalpost.addJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);

		setKanal(domainJournalpost, wsJournalpost.getKanal());

		return domainJournalpost;
	}

	private void setKanal(Journalpost journalpost, Kommunikasjonskanaler kanal) {
		if (kanal != null && !isBlank(kanal.getValue())) {
			journalpost.setMottakskanal(toMottaksKanalEnum(kanal.getValue()));
		}
	}

	private MottaksKanalCode toMottaksKanalEnum(String kanal) {
		return Enum.valueOf(MottaksKanalCode.class, kanal);
	}
//
//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
