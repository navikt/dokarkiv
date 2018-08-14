package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static no.nav.dokarkiv.arkiverdokumentmottak.util.ConverterUtils.converTillegsopplysningerToMap;
import static no.nav.dokarkiv.arkiverdokumentmottak.util.ConverterUtils.stringToEnum;
import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.mapFiltype;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * JournalforInngaaendeForsendelseRequestMapper implementation
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 17.02.2017
 */
@Component
public class JournalforInngaaendeForsendelseRequestMapper {

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public JournalforInngaaendeForsendelseRequestTo map(JournalforInngaaendeForsendelseRequest request) {

		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost = request
				.getJournalpost();

		Journalpost domainJournalpost = mapJournalpost(journalpost);

		mapBruker(domainJournalpost, journalpost);
		mapSaksrelasjon(domainJournalpost, journalpost);
		mapJournalpostDokumentInfoRelasjon(domainJournalpost, journalpost);

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder.currentRequestContext()
				.getComponentId());

		return new JournalforInngaaendeForsendelseRequestTo(domainJournalpost);


	}

	private Journalpost mapJournalpost(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost) {
		return Journalpost.builder()
				.fagomrade(stringToEnum(FagomradeCode.class, journalpost.getTema()))
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalfortAvNavn(journalpost.getOpprettetAvNavn())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.innhold(journalpost.getInnhold())
				.mottattDato(journalpost.getDatoMottatt() == null ? null : journalpost.getDatoMottatt()
						.toGregorianCalendar()
						.getTime())
				.mottakskanal(stringToEnum(MottaksKanalCode.class, journalpost.getMottakskanal()))
				.dokumentDato(journalpost.getDatoDokument() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar()
						.getTime())
				.tilleggsopplysninger(converTillegsopplysningerToMap(journalpost.getJournalpostTilleggsopplysninger()))
				.build();

	}

	private void mapBruker(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost) {

		if (journalpost.getBruker() == null) {
			return;
		}

		domainJournalpost.addBruker(Bruker.builder()
				.brukerType(stringToEnum(BrukerTypeCode.class, journalpost.getBruker().getBrukerType()))
				.brukerId(journalpost.getBruker().getBrukerId())
				.build());

	}

	private void mapSaksrelasjon(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost) {

		if (journalpost.getSaksrelasjon() == null) {
			return;
		}

		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.fagsystem(stringToEnum(FagsystemCode.class, journalpost.getSaksrelasjon().getFagsystem()))
				.sakId(journalpost.getSaksrelasjon().getSaksnummer())
				.journalpost(domainJournalpost)
				.build());

	}

	private void mapJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost) {

		journalpost.getJournalpostDokumentInfoRelasjon().forEach(relasjon ->
				domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
						.journalpost(domainJournalpost)
						.tilknyttetJournalpostSom(stringToEnum(TilknyttetJournalpostSomCode.class, relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon
								.getTilknyttetJournalpostSom()
								.name()))
						.tilknyttetAvNavn(journalpost.getOpprettetAvNavn())
						.dokumentInfo(mapDokumentInfo(relasjon, domainJournalpost))
						.build()));

	}

	private DokumentInfo mapDokumentInfo(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon relasjon, Journalpost domainJournalpost) {
		if (relasjon == null || relasjon.getDokumentInfo() == null) {
			return null;
		}

		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.kategori(stringToEnum(DokumentKategoriCode.class, relasjon.getDokumentInfo().getKategori()))
				.sensitivt(relasjon.getDokumentInfo().isSensitivt())
				.tittel(relasjon.getDokumentInfo().getTittel())
				.brevkode(relasjon.getDokumentInfo().getBrevkode())
				.dokumenttypeId(relasjon.getDokumentInfo().getDokumentTypeId())
				.originalJournalpost(domainJournalpost).build();

		relasjon.getDokumentInfo()
				.getFildetaljerListe()
				.forEach(fildetaljer -> dokumentInfo.addFilDetaljer(FilDetaljer.builder()
						.fileContent(fildetaljer.getDokument())
						.filtype(stringToEnum(FilTypeCode.class, mapFiltype(fildetaljer.getFiltype())))
						.filnavn(fildetaljer.getFilNavn())
						.filUuid(FilDetaljer.generateUuid())
						.variantFormat(stringToEnum(VariantFormatCode.class, fildetaljer.getVariantformat()))
						.build()));

		return dokumentInfo;
	}


}