package no.nav.dokarkiv.behandlejournal.v3.datautil;

import no.nav.dokarkiv.behandlejournal.v3.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.EksternPart;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Journaldistribusjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Kryssreferanse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NorskIdent;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Person;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Sak;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Signatur;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Variantformater;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * Util for creating common types in BehandleJournal
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class BehandleJournalCommonDataUtil {

	public static final String SPORING_FORNAVN = "Bjarne";
	public static final String SPORING_ETTERNAVN = "Betjent";
	public static final String SPORING_NAVN = SPORING_FORNAVN + " " + SPORING_ETTERNAVN;
	protected static final String TILLEGG_NOKKEL = "Testnøkkel";
	protected static final String TILLEGG_VERDI = "Testverdi";
	protected static final String FILNAVN = "filNavn";
	protected static final String FILTYPE = "PDF";
	protected static final String DOKUMENT_INNHOLD = "ustrukturertInnhold";
	protected static final String VARIANTFORMAT = "ARKIV";
	protected static final String PERSONIDENT = "***gammelt_fnr***";
	protected static final String EKSTERNPART_NAVN = "Jippi Hurra";
	protected static final String ORGNR = "954289600";
	protected static final boolean SIGNATUR = true;
	protected static final String REFERANSEID = "123";
	protected static final String REFERANSEKODE = ReferanseTypeCode.SPOERSMAAL.name();
	protected static final String FAGSYSTEMKODE = FagsystemCode.FS22.name();
	protected static final String SAKSID = "312";

	public static NoekkelVerdiSett createTilleggsopplysninger() {
		NoekkelVerdiSett noekkelVerdiSett = new NoekkelVerdiSett();
		NoekkelVerdiPar noekkelVerdiPar = new NoekkelVerdiPar();
		noekkelVerdiPar.setNoekkel(TILLEGG_NOKKEL);
		noekkelVerdiPar.setVerdi(TILLEGG_VERDI);
		noekkelVerdiSett.getInneholderNoekkelVerdiPar().add(noekkelVerdiPar);
		return noekkelVerdiSett;
	}

	public static UstrukturertInnhold createUstrukurertInnhold() {
		UstrukturertInnhold ustrukturertInnhold = new UstrukturertInnhold();
		ustrukturertInnhold.setFilnavn(FILNAVN);
		ustrukturertInnhold.setFiltype(KodeverdiHelper.kodeVerdi(FILTYPE, Arkivfiltyper.class));
		ustrukturertInnhold.setInnhold(DOKUMENT_INNHOLD.getBytes());
		ustrukturertInnhold.setVariantformat(KodeverdiHelper.kodeVerdi(VARIANTFORMAT, Variantformater.class));
		return ustrukturertInnhold;
	}

	public static Signatur createSignatur() {
		Signatur signatur = new Signatur();
		signatur.setSignert(SIGNATUR);
		return signatur;
	}

	public static Aktoer createPerson() {
		Person person = new Person();
		NorskIdent ident = new NorskIdent();
		ident.setIdent(PERSONIDENT);
		person.setIdent(ident);
		return person;
	}

	public static EksternPart createEksternPart() {
		EksternPart eksternPart = new EksternPart();
		eksternPart.setEksternAktoer(createPerson());
		eksternPart.setNavn(EKSTERNPART_NAVN);
		return eksternPart;
	}

	public static Aktoer createOrganisasjon() {
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setOrgnummer(ORGNR);
		return organisasjon;
	}

	public static Journaldistribusjon createJournaldistribusjon() {
		Journaldistribusjon journaldistribusjon = new Journaldistribusjon();
		journaldistribusjon.setSendtPrintDato(getToday());
		return journaldistribusjon;
	}

	protected static Kryssreferanse createKryssreferanse() {
		Kryssreferanse kryssreferanse = new Kryssreferanse();
		kryssreferanse.setReferanseId(REFERANSEID);
		kryssreferanse.setReferansekode(REFERANSEKODE);
		return kryssreferanse;
	}

	protected static Sak createSak() {
		Sak sak = new Sak();
		sak.setFagsystemkode(FAGSYSTEMKODE);
		sak.setSaksId(SAKSID);
		return sak;
	}

	protected static XMLGregorianCalendar getToday() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(DateProvider.getToday());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			return null;
		}
	}

}
