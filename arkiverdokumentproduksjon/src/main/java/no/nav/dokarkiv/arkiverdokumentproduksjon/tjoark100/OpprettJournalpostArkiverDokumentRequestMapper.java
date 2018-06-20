package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.journalbehandling.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * Implementation of OpprettJournalpostArkiverDokumentRequestMapper
 *
 * @author Stig Str?m
 */
public class OpprettJournalpostArkiverDokumentRequestMapper {

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public OpprettJournalpostArkiverDokumentRequestTo map(OpprettJournalpostArkiverDokumentRequest wsRequest) {
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokument.Journalpost journalpost = wsRequest
				.getJournalpost();
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokument.DokumentInfo dokumentInfo = journalpost
				.getDokumentInfo();

		Journalpost domainJournalpost = Journalpost.builder()
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(journalpost.getSaksrelasjon().getSaksnummer())
						.fagsystem(journalpost.getSaksrelasjon()
								.getFagsystem() == null ? null : FagsystemCode.valueOf(journalpost.getSaksrelasjon()
								.getFagsystem()))
						.build())
				.journalposttype(journalpost.getJournalpostType() == null ? null : JournalpostTypeCode.valueOf(journalpost.getJournalpostType()
						.name()))
				.fagomrade(journalpost.getFagomrade() == null ? null : FagomradeCode.valueOf(journalpost.getFagomrade()))
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.innhold(journalpost.getInnhold())
				.dokumentDato(journalpost.getDatoDokument() == null || journalpost.getDatoDokument()
						.toGregorianCalendar() == null ? null : journalpost.getDatoDokument().toGregorianCalendar().getTime())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.utsendingskanal(journalpost.getUtsendingskanal() == null ? null : UtsendingsKanalCode.valueOf(journalpost.getUtsendingskanal()))
				.land(journalpost.getLand())
				.build();

		domainJournalpost.addBruker(Bruker.builder()
				.brukerId(journalpost.getBruker() == null ? null : journalpost.getBruker().getBrukerId())
				.brukerType(journalpost.getBruker() == null || journalpost.getBruker()
						.getBrukerType() == null ? null : BrukerTypeCode.valueOf(journalpost.getBruker()
						.getBrukerType()))
				.build());

		domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.journalpost(domainJournalpost)
				.dokumentInfo(DokumentInfo.builder()
						.kategori(dokumentInfo.getKategori() == null ? null : DokumentKategoriCode.valueOf(dokumentInfo.getKategori()))
						.tittel(dokumentInfo.getTittel())
						.brevkode(dokumentInfo.getBrevkode())
						.dokumenttypeId(dokumentInfo.getDokumentTypeId())
						.sensitivt(dokumentInfo.isSensitivt())
						.tilleggsopplysninger(dokumentInfo.getTilleggsopplysninger() == null ? null :
								dokumentInfo.getTilleggsopplysninger()
										.stream()
										.collect(Collectors.toMap(Tilleggsopplysning::getOpplysningsnoekkel, Tilleggsopplysning::getOpplysningsverdi)))
						.fildetaljerListe(dokumentInfo.getFildetaljerListe() == null ? null : dokumentInfo.getFildetaljerListe()
								.stream()
								.map(fildetaljer -> FilDetaljer.builder()
										.filtype(fildetaljer.getFiltype() == null ? null : FilTypeCode.valueOf(fildetaljer.getFiltype()))
										.variantFormat(fildetaljer.getVariantformat() == null ? null : VariantFormatCode.valueOf(fildetaljer
												.getVariantformat()))
										.fileContent(fildetaljer.getIkkeRedigerbartdokument())
										.build())
								.collect(Collectors.toSet()))
						.build())
				.build());

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return new OpprettJournalpostArkiverDokumentRequestTo(domainJournalpost, wsRequest.isFerdigstillJournalpost());
	}

}
