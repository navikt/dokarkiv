package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.convertFilType;

import no.nav.dokarkiv.behandlejournal.v2.SporingMapper;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Person;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.StrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.JournalfoertDokumentInfo;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of
 * JournalfoerInngaaendeHenvendelseRequestMapper
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultJournalfoerInngaaendeHenvendelseRequestMapper implements
		JournalfoerInngaaendeHenvendelseRequestMapper {

	@Inject
	private SporingMapper sporingMapper;

	@Override
	public JournalfoerInngaaendeHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest wsRequest) {
		no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.Journalpost wsJournalpost = wsRequest.getJournalpost();
		Journalpost domainJournalpost = Journalpost.builder()
				.dokumentDato(wsJournalpost.getDokumentDato() == null ? null : wsJournalpost.getDokumentDato().toGregorianCalendar().getTime())
				.mottattDato(wsJournalpost.getMottattDato() == null ? null : wsJournalpost.getMottattDato().toGregorianCalendar().getTime())
				.mottakskanal(wsJournalpost.getKanal() == null ? null : MottaksKanalCode.valueOf(wsJournalpost.getKanal().getValue()))
				.signatur(wsJournalpost.getSignatur() == null ? null : wsJournalpost.getSignatur().isSignert())
				.journalForendeEnhetId(wsJournalpost.getJournalfoerendeEnhetREF())
				.journalfortAvNavn(wsJournalpost.getOpprettetAvNavn())
				.innhold(wsJournalpost.getInnhold())
				.fagomrade(wsJournalpost.getArkivtema() == null ? null : FagomradeCode.valueOf(wsJournalpost.getArkivtema().getValue()))
				.avsenderMottakerId(wsJournalpost.getEksternPart() == null || wsJournalpost.getEksternPart().getEksternAktoer() == null ?
						null : convertAktoerToId(wsJournalpost.getEksternPart().getEksternAktoer()))
				.avsenderMottaker(wsJournalpost.getEksternPart() == null ? null : wsJournalpost.getEksternPart().getNavn())
				.build();
		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.sakId(wsJournalpost.getGjelderSak() == null ? null : wsJournalpost.getGjelderSak().getSaksId())
				.fagsystem(wsJournalpost.getGjelderSak() == null ? null : FagsystemCode.valueOf(wsJournalpost.getGjelderSak().getFagsystemkode()))
				.build());
		wsJournalpost.getForBruker().forEach(aktoer -> domainJournalpost.addBruker(convertAktoerToBruker(aktoer)));
		wsJournalpost.getKryssreferanseListe().forEach(kryssreferanse -> domainJournalpost.addKryssReferanse(Kryssreferanse.builder()
				.referanseId(kryssreferanse.getReferanseId())
				.referanseType(kryssreferanse.getReferansekode() == null ? null : ReferanseTypeCode.valueOf(kryssreferanse.getReferansekode()))
				.build()));
		wsJournalpost.getDokumentinfoRelasjon().forEach(dokumentinfoRelasjon -> {
			JournalfoertDokumentInfo journalfoertDokumentInfo = dokumentinfoRelasjon.getJournalfoertDokument();
			DokumentInfo dokumentInfo = DokumentInfo.builder()
					.innskrenketPartsinnsyn(journalfoertDokumentInfo.isBegrensetPartsInnsyn())
					.sensitivt(journalfoertDokumentInfo.isSensitivitet())
					.tittel(journalfoertDokumentInfo.getTittel())
					.kategori(journalfoertDokumentInfo.getKategorikode() == null ? null : DokumentKategoriCode.valueOf(journalfoertDokumentInfo.getKategorikode()))
					.brevkode(journalfoertDokumentInfo.getDokumentType() == null ? null : journalfoertDokumentInfo.getDokumentType().getValue())
					.tilleggsopplysninger(convertNoekkelVerdiSettToMap(journalfoertDokumentInfo.getTilleggsopplysninger()))
					.build();
			journalfoertDokumentInfo.getBeskriverInnhold().forEach(dokumentInnhold -> dokumentInfo.addFilDetaljer(convertDokumentInnhold(dokumentInnhold)));
			domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
					.tilknyttetJournalpostSom(dokumentinfoRelasjon.getTillknyttetJournalpostSomKode() == null ?
							null : TilknyttetJournalpostSomCode.valueOf(dokumentinfoRelasjon.getTillknyttetJournalpostSomKode()))
					.dokumentInfo(dokumentInfo)
					.build());
		});
		sporingMapper.mapSporingsinfo(domainJournalpost, wsJournalpost.getOpprettetAvNavn());
		return JournalfoerInngaaendeHenvendelseRequest.builder()
				.journalpost(domainJournalpost)
				.build();
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
