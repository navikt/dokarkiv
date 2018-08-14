package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static no.nav.dokarkiv.core.util.FilTypeMapper.mapFiltype;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OpprettJournalpostRequestMapper. Does the mapping of
 * webservice requst to domain request
 *
 * @author Stig Strøm
 */
@Component
public class OpprettJournalpostRequestMapper {
	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public OpprettJournalpostRequestTo map(OpprettJournalpostRequest wsRequest) {
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost journalpost = wsRequest
				.getJournalpost();
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.DokumentInfo dokumentInfo = journalpost
				.getDokumentInfo();

		Journalpost domainJournalpost = createDomainJournalpostBase(journalpost);
		addBruker(domainJournalpost, journalpost);
		setSaksrelasjon(domainJournalpost, journalpost);
		addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfo);
		addFildetaljer(domainJournalpost, dokumentInfo);

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return new OpprettJournalpostRequestTo(domainJournalpost);
	}

	Journalpost createDomainJournalpostBase(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost journalpost) {
		return Journalpost.builder()
				.fagomrade(journalpost.getFagomrade() == null ? null : FagomradeCode.valueOf(journalpost.getFagomrade()))
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.innhold(journalpost.getInnhold())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.land(journalpost.getLand())
				.build();
	}

	private void setSaksrelasjon(Journalpost domainJournalpost,
								 no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost journalpost) {
		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.sakId(journalpost.getSaksrelasjon().getSaksnummer())
				.fagsystem(journalpost.getSaksrelasjon()
						.getFagsystem() == null ? null : FagsystemCode.valueOf(journalpost.getSaksrelasjon()
						.getFagsystem()))
				.journalpost(domainJournalpost)
				.build());
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost,
													no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.DokumentInfo dokumentInfo) {
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
						.build())
				.build());
	}

	private void addFildetaljer(Journalpost domainJournalpost,
								no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.DokumentInfo dokumentInfo) {
		domainJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.addFilDetaljer(FilDetaljer
						.builder()
						.metaforceInstanceId(dokumentInfo.getFildetaljer().getMetaForceInstanceId())
						.filtype(mapFiltype(dokumentInfo.getFildetaljer()
								.getFiltype()) == null ? null : FilTypeCode.valueOf(mapFiltype(dokumentInfo.getFildetaljer()
								.getFiltype())))
						.variantFormat(dokumentInfo.getFildetaljer()
								.getVariantformat() == null ? null : VariantFormatCode.valueOf(dokumentInfo.getFildetaljer()
								.getVariantformat()))
						.filUuid(UUID.randomUUID().toString())
						.build());
	}

	private void addBruker(Journalpost domainJournalpost,
						   no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost journalpost) {
		domainJournalpost.addBruker(Bruker.builder()
				.brukerId(journalpost.getBruker() == null ? null : journalpost.getBruker().getBrukerId())
				.brukerType(journalpost.getBruker() == null || journalpost.getBruker()
						.getBrukerType() == null ? null : BrukerTypeCode.valueOf(journalpost.getBruker()
						.getBrukerType()))
				.build());
	}


}
