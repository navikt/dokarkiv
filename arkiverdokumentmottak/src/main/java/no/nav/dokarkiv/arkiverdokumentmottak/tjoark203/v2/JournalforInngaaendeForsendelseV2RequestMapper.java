package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.arkiverdokumentmottak.util.ConverterUtils.converTillegsopplysningerToMapV2;
import static no.nav.dokarkiv.arkiverdokumentmottak.util.ConverterUtils.stringToEnum;
import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.mapFiltype;

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
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Mapper for TJOARK203 request
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class JournalforInngaaendeForsendelseV2RequestMapper {

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public JournalforInngaaendeForsendelseV2RequestTo map(JournalforInngaaendeForsendelseRequest request) {

		if (request == null || request.getJournalpost() == null) {
			return new JournalforInngaaendeForsendelseV2RequestTo(false, null);
		}

		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost = request
				.getJournalpost();

		Journalpost domainJournalpost = mapJournalpost(journalpost);

		mapKryssreferanse(domainJournalpost, journalpost);
		mapBruker(domainJournalpost, journalpost);
		mapSaksrelasjon(domainJournalpost, journalpost);
		mapJournalpostDokumentInfoRelasjon(domainJournalpost, journalpost);

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder.currentRequestContext()
				.getComponentId());


		return new JournalforInngaaendeForsendelseV2RequestTo(request.isForsokEndeligJF(), domainJournalpost);
	}

	private Journalpost mapJournalpost(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost) {
		return Journalpost.builder()
				.fagomrade(stringToEnum(FagomradeCode.class, journalpost.getTema()))
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalfortAvNavn(journalpost.getOpprettetAvNavn())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.innhold(journalpost.getInnhold())
				.kanalReferanseId(journalpost.getKanalReferanseId())
				.mottattDato(journalpost.getDatoMottatt() == null ? null : journalpost.getDatoMottatt()
						.toGregorianCalendar()
						.getTime())
				.mottakskanal(stringToEnum(MottaksKanalCode.class, journalpost.getMottakskanal()))
				.dokumentDato(journalpost.getDatoDokument() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar()
						.getTime())
				.tilleggsopplysninger(converTillegsopplysningerToMapV2(journalpost.getTilleggsopplysninger()))
				.build();

	}

	private void mapKryssreferanse(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost) {
		if (journalpost.getKryssreferanse() == null) {
			return;
		}

		domainJournalpost.addKryssReferanse(Kryssreferanse.builder()
				.referanseId(journalpost.getKryssreferanse().getReferanseId())
				.referanseType(stringToEnum(ReferanseTypeCode.class, journalpost.getKryssreferanse().getReferanseType()))
				.build());
	}

	private void mapBruker(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost) {

		if (journalpost.getBruker() == null) {
			return;
		}

		domainJournalpost.addBruker(Bruker.builder()
				.brukerType(stringToEnum(BrukerTypeCode.class, journalpost.getBruker().getBrukerType()))
				.brukerId(journalpost.getBruker().getBrukerId())
				.build());

	}

	private void mapSaksrelasjon(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost) {

		if (journalpost.getSaksrelasjon() == null) {
			return;
		}

		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.fagsystem(stringToEnum(FagsystemCode.class, journalpost.getSaksrelasjon().getFagsystem()))
				.sakId(journalpost.getSaksrelasjon().getSaksnummer())
				.build());

	}

	private void mapJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost journalpost) {

		journalpost.getJournalpostDokumentInfoRelasjon().forEach(relasjon ->
				domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(stringToEnum(TilknyttetJournalpostSomCode.class, relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon
								.getTilknyttetJournalpostSom()
								.name()))
						.tilknyttetAvNavn(journalpost.getOpprettetAvNavn())
						.dokumentInfo(mapDokumentInfo(relasjon, domainJournalpost))
						.build()));

	}

	public DokumentInfo mapDokumentInfo(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalpostDokumentInfoRelasjon relasjon, Journalpost domainJournalpost) {
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
				.getSkannetInnholdListe()
				.forEach(skannetInnhold -> dokumentInfo.addSkannetInnhold(SkannetInnhold.builder()
						.dokumenttypeid(skannetInnhold.getDokumentTypeId())
						.vedleggInnhold(skannetInnhold.getVedleggInnhold())
						.build()));

		relasjon.getDokumentInfo()
				.getFildetaljerListe()
				.forEach(fildetaljer -> dokumentInfo.addFilDetaljer(FilDetaljer.builder()
						.fileContent(fildetaljer.getDokument())
						.filtype(stringToEnum(FilTypeCode.class, mapFiltype(fildetaljer.getFiltype())))
						.filnavn(fildetaljer.getFilNavn())
						.filUuid(FilDetaljer.generateUuid())
						.batchNavn(fildetaljer.getBatchNavn())
						.variantFormat(stringToEnum(VariantFormatCode.class, fildetaljer.getVariantformat()))
						.build()));

		return dokumentInfo;
	}


}
