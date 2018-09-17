package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.convertFilType;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OpprettJournalpostArkiverDokumentRequestMapper
 *
 * @author Sigurd Midttun
 */
@Component
public class OpprettJournalpostArkiverDokumenterRequestMapper {

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public OpprettJournalpostArkiverDokumenterRequestTo map(OpprettJournalpostArkiverDokumenterRequest wsRequest) {
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost = wsRequest
				.getJournalpost();
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo dokumentInfoHoveddokument = journalpost
				.getDokumentInfoHoveddokument();
		List<no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo> dokumentInfoVedleggList = journalpost
				.getDokumentInfoVedlegg();

		Journalpost domainJournalpost = createDomainJournalpostBase(journalpost);
		addBruker(domainJournalpost, journalpost);
		setSaksrelasjon(domainJournalpost, journalpost);
		addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfoHoveddokument, TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		dokumentInfoVedleggList.stream()
				.forEach(dokumentInfo -> addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfo, TilknyttetJournalpostSomCode.VEDLEGG));

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return new OpprettJournalpostArkiverDokumenterRequestTo(domainJournalpost);
	}

	private Journalpost createDomainJournalpostBase(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		return Journalpost.builder()
				.journalposttype(journalpost.getJournalpostType() == null ? null : JournalpostTypeCode.valueOf(journalpost
						.getJournalpostType()
						.name()))
				.fagomrade(journalpost.getFagomrade() == null ? null : FagomradeCode.valueOf(journalpost.getFagomrade()))
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.innhold(journalpost.getInnhold())
				.dokumentDato(journalpost.getDatoDokument() == null || journalpost.getDatoDokument()
						.toGregorianCalendar() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar()
						.getTime())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.land(journalpost.getLand())
				.build();
	}

	private void addBruker(Journalpost domainJournalpost,
						   no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		domainJournalpost.addBruker(Bruker.builder()
				.brukerId(journalpost.getBruker() == null ? null : journalpost.getBruker().getBrukerId())
				.brukerType(journalpost.getBruker() == null || journalpost.getBruker()
						.getBrukerType() == null ? null : BrukerTypeCode.valueOf(journalpost.getBruker()
						.getBrukerType()))
				.build());
	}


	private void setSaksrelasjon(Journalpost domainJournalpost,
								 no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.sakId(journalpost.getSaksrelasjon().getSaksnummer())
				.fagsystem(journalpost.getSaksrelasjon()
						.getFagsystem() == null ? null : FagsystemCode.valueOf(journalpost.getSaksrelasjon()
						.getFagsystem()))
				.journalpost(domainJournalpost)
				.build());
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost,
													no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo dokumentInfo,
													TilknyttetJournalpostSomCode tilknyttetJournalpostSom) {

		JournalpostDokumentInfoRelasjon relasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSom)
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
						.build())
				.build();

		addFildetaljer(relasjon, dokumentInfo);
		domainJournalpost.addJournalpostDokumentInfoRelasjon(relasjon);
	}

	private void addFildetaljer(JournalpostDokumentInfoRelasjon relasjon,
								no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo dokumentInfo) {
		dokumentInfo.getFildetaljerListe()
				.stream()
				.forEach(fildetaljer -> relasjon
						.getDokumentInfo()
						.addFilDetaljer(FilDetaljer.builder()
								.filtype(convertFilType(fildetaljer.getFiltype()) == null ? null : FilTypeCode.valueOf(convertFilType(fildetaljer
										.getFiltype())))
								.variantFormat(fildetaljer.getVariantformat() == null ? null : VariantFormatCode.valueOf(fildetaljer
										.getVariantformat()))
//								.fileContent(fildetaljer.get()) TODO Settes etter s3-oppslag
								.filUuid(UUID.randomUUID().toString())
								.dokumentInfo(relasjon.getDokumentInfo())
								.build()));
	}
}
