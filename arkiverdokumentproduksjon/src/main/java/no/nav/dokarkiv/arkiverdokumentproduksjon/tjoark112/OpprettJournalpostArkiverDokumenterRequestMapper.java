package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;


import static no.nav.dokarkiv.core.storage.DokprodMellomlagerS3Storage.DOKPRODMELLOMLAGER_DIRECTORY_NAME;

import lombok.extern.slf4j.Slf4j;
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
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.dokarkiv.core.storage.DoksysDokument;
import no.nav.dokarkiv.core.storage.Storage;
import no.nav.dokarkiv.core.util.JsonSerializer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun
 */
@Slf4j
@Component
public class OpprettJournalpostArkiverDokumenterRequestMapper {

	private final KildeNavnPopulator kildeNavnPopulator;
	private final Storage dokprodMellomlagerStorage;

	@Inject
	public OpprettJournalpostArkiverDokumenterRequestMapper(KildeNavnPopulator kildeNavnPopulator, Storage dokprodMellomlagerStorage) {
		this.kildeNavnPopulator = kildeNavnPopulator;
		this.dokprodMellomlagerStorage = dokprodMellomlagerStorage;
	}

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
		dokumentInfoVedleggList.forEach(dokumentInfo -> addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfo, TilknyttetJournalpostSomCode.VEDLEGG));

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return new OpprettJournalpostArkiverDokumenterRequestTo(domainJournalpost);
	}

	private Journalpost createDomainJournalpostBase(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		return Journalpost.builder()
				.journalposttype(journalpost.getJournalpostType() == null ? null : JournalpostTypeCode.valueOf(journalpost
						.getJournalpostType().name()))
				.fagomrade(journalpost.getFagomrade() == null ? null : FagomradeCode.valueOf(journalpost.getFagomrade()))
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.innhold(journalpost.getInnhold())
				.dokumentDato(journalpost.getDatoDokument() == null || journalpost.getDatoDokument()
						.toGregorianCalendar() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar().getTime())
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

		DoksysDokument doksysDokument = createDokumentResultWithDocumentsFromS3(dokumentInfo.getFilreferanse());
		DokumentInfo domainDokumentInfo = relasjon.getDokumentInfo();

		domainDokumentInfo.addFilDetaljer(FilDetaljer.builder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.ARKIV)
				.fileContent(doksysDokument.getPdf())
				.filUuid(UUID.randomUUID().toString())
				.dokumentInfo(relasjon.getDokumentInfo())
				.build());
		domainDokumentInfo.addFilDetaljer(FilDetaljer.builder()
				.filtype(FilTypeCode.AXML)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.fileContent(doksysDokument.getAxml())
				.filUuid(UUID.randomUUID().toString())
				.dokumentInfo(relasjon.getDokumentInfo())
				.build());
	}

	private DoksysDokument createDokumentResultWithDocumentsFromS3(String s3ObjectId) {
		log.info("tjoark112 henter dokument fra S3. s3ObjectId={}", s3ObjectId);
		return fetchDocumentFromS3(s3ObjectId);
	}

	private DoksysDokument fetchDocumentFromS3(String key) {
		Optional<String> jsonPayload = dokprodMellomlagerStorage.get(DOKPRODMELLOMLAGER_DIRECTORY_NAME, key);

		if (!jsonPayload.isPresent()) {
			throw new DokarkivTechnicalException(String.format("qdok002 fant ingen dokument i S3 fra directory=%s med key=%s ", DOKPRODMELLOMLAGER_DIRECTORY_NAME, key));
		}

		return JsonSerializer.deserialize(jsonPayload.get(), DoksysDokument.class);
	}
}
