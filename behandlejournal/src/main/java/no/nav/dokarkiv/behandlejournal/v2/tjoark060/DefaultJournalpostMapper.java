package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.convertFilType;
import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Kommunikasjonskanaler;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Person;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.StrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.UstrukturertInnhold;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of JournalpostMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultJournalpostMapper implements JournalpostMapper {

	public Journalpost map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost wsJournalpost) {
		if (wsJournalpost == null) {
			return null;
		}
		Journalpost domainJournalpost = Journalpost.builder()
				.dokumentDato(wsJournalpost.getDokumentDato().toGregorianCalendar().getTime())
				.mottattDato(wsJournalpost.getMottattDato().toGregorianCalendar().getTime())
				.signatur(wsJournalpost.getSignatur().isSignert())
				.journalForendeEnhetId(wsJournalpost.getJournalfoerendeEnhetREF())
				.fagomrade(FagomradeCode.valueOf(wsJournalpost.getArkivtema().getValue()))
				.avsenderMottaker(wsJournalpost.getEksternPart() == null ? null : wsJournalpost.getEksternPart().getNavn())
				.avsenderMottakerId(wsJournalpost.getEksternPart() == null ? null : convertAktoerToId(wsJournalpost.getEksternPart().getEksternAktoer()))
				.build();
		wsJournalpost.getForBruker().forEach(aktoer -> domainJournalpost.addBruker(convertAktoerToBruker(aktoer)));

		JournalfoertDokumentInfo journalfoertDokumentInfo = wsJournalpost.getJournalfoertDokument();
		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.innskrenketPartsinnsyn(journalfoertDokumentInfo.isBegrensetPartsInnsyn())
				.brevkode(journalfoertDokumentInfo.getDokumentType().getValue())
				.dokumenttypeId(journalfoertDokumentInfo.getDokumentType().getValue())
				.tilleggsopplysninger(convertNoekkelVerdiSettToMap(journalfoertDokumentInfo.getTilleggsopplysninger()))
				.build();
		journalfoertDokumentInfo.getBeskriverInnhold().forEach(dokumentInnhold -> dokumentInfo.addFilDetaljer(convertDokumentInnhold(dokumentInnhold)));

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

	private String convertAktoerToId(Aktoer source) {
		if (source == null) {
			return null;
		}

		if (source instanceof Person) {
			return ((Person) source).getIdent().getIdent();
		} else if (source instanceof Organisasjon) {
			return ((Organisasjon) source).getOrgnummer();
		} else {
			throw new ApplicationException("Aktoer must be a type or subtype of Person or Organisasjon.");
		}
	}

	private Bruker convertAktoerToBruker(Aktoer source) {
		if (source == null) {
			return null;
		}

		Bruker bruker = new Bruker();
		if (source instanceof Person) {
			bruker.setBrukerId(((Person) source).getIdent().getIdent());
			bruker.setBrukerType(BrukerTypeCode.PERSON);
		} else if (source instanceof Organisasjon) {
			bruker.setBrukerId(((Organisasjon) source).getOrgnummer());
			bruker.setBrukerType(BrukerTypeCode.ORGANISASJON);
		} else {
			throw new ApplicationException("Aktoer must be a type or subtype of Person or Organisasjon.");
		}
		return bruker;
	}

	private Map<String, String> convertNoekkelVerdiSettToMap(NoekkelVerdiSett source) {
		if (source == null) {
			return null;
		}
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		for (NoekkelVerdiPar noekkelVerdiPar : source.getInneholderNoekkelVerdiPar()) {
			tilleggsopplysninger.put(noekkelVerdiPar.getNoekkel(), noekkelVerdiPar.getVerdi());
		}
		return tilleggsopplysninger;
	}

	private FilDetaljer convertDokumentInnhold(DokumentInnhold dokumentInnhold) {
		FilDetaljer filDetaljer = new FilDetaljer();
		filDetaljer.setFiltype(FilTypeCode.valueOf(convertFilType(dokumentInnhold.getFiltype().getValue())));
		filDetaljer.setVariantFormat(VariantFormatCode.valueOf(dokumentInnhold.getVariantformat().getValue()));
		filDetaljer.setFilnavn(dokumentInnhold.getFilnavn());

		if (dokumentInnhold instanceof UstrukturertInnhold) {
			UstrukturertInnhold ustrukturertInnhold = (UstrukturertInnhold) dokumentInnhold;
			filDetaljer.setFileContent(ustrukturertInnhold.getInnhold());
		}

		if (dokumentInnhold instanceof StrukturertInnhold) {
			StrukturertInnhold strukturertInnhold = (StrukturertInnhold) dokumentInnhold;
			filDetaljer.setFileContent(strukturertInnhold.getInnhold());
		}
		return filDetaljer;
	}
}
