package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;


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
import no.nav.dokarkiv.core.storage.BucketStorage;
import no.nav.dokarkiv.core.storage.DoksysDokument;
import no.nav.dokarkiv.core.util.JsonSerializer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.FILREFERANSE_ID_KEY;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
public class OpprettJournalpostArkiverDokumenterRequestMapper {

	private final KildeNavnPopulator kildeNavnPopulator;
	private final BucketStorage dokprodMellomlagerStorage;

	@Autowired
	public OpprettJournalpostArkiverDokumenterRequestMapper(KildeNavnPopulator kildeNavnPopulator, BucketStorage dokprodMellomlagerStorage) {
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
		final var bestillingsId = dokumentInfoHoveddokument.getTilleggsopplysninger()
				.stream()
				.filter(p -> BESTILLINGS_ID_KEY.equals(p.getOpplysningsnoekkel()))
				.findFirst()
				.orElseThrow(() -> new DokarkivTechnicalException("tjoark112 fant ingen opplysningsNoekkel=bestillingsId på hoveddokumentinfo"))
				.getOpplysningsverdi();
		addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfoHoveddokument, TilknyttetJournalpostSomCode.HOVEDDOKUMENT, bestillingsId);
		dokumentInfoVedleggList.forEach(dokumentInfo -> addJournalpostDokumentInfoRelasjon(domainJournalpost, dokumentInfo, TilknyttetJournalpostSomCode.VEDLEGG, bestillingsId));
		domainJournalpost.setKanalReferanseId(bestillingsId);
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
				.dokumentDato(mapDokumentDato(journalpost))
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.land(journalpost.getLand())
				.build();
	}

	private static LocalDateTime mapDokumentDato(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		return journalpost.getDatoDokument() == null || journalpost.getDatoDokument().toGregorianCalendar() == null ?
				null : journalpost.getDatoDokument().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}

	private void addBruker(Journalpost domainJournalpost,
						   no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		domainJournalpost.addBruker(Bruker.builder()
				.brukerId(journalpost.getBruker() == null ? null : trim(journalpost.getBruker().getBrukerId()))
				.brukerType(journalpost.getBruker() == null || journalpost.getBruker()
																	   .getBrukerType() == null ? null : BrukerTypeCode.valueOf(journalpost.getBruker()
						.getBrukerType()))
				.build());
	}


	private void setSaksrelasjon(Journalpost domainJournalpost,
								 no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost journalpost) {
		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.sakId(parseLong(journalpost.getSaksrelasjon().getSaksnummer()))
				.fagsystem(journalpost.getSaksrelasjon().getFagsystem() == null ? null : FagsystemCode.valueOf(journalpost.getSaksrelasjon()
						.getFagsystem()))
				.journalpost(domainJournalpost)
				.build());
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost,
													no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo dokumentInfo,
													TilknyttetJournalpostSomCode tilknyttetJournalpostSom,
													String bestillingsId) {

		DokumentInfo domainDokumentInfo = DokumentInfo.builder()
				.kategori(dokumentInfo.getKategori() == null ? null : DokumentKategoriCode.valueOf(dokumentInfo.getKategori()))
				.tittel(dokumentInfo.getTittel())
				.brevkode(dokumentInfo.getBrevkode())
				.dokumenttypeId(dokumentInfo.getDokumentTypeId())
				.sensitivt(dokumentInfo.isSensitivt())
				.tilleggsopplysninger(dokumentInfo.getTilleggsopplysninger() == null ? null :
						dokumentInfo.getTilleggsopplysninger()
								.stream()
								.collect(Collectors.toMap(Tilleggsopplysning::getOpplysningsnoekkel, Tilleggsopplysning::getOpplysningsverdi)))
				.build();
		addFildetaljer(domainDokumentInfo, bestillingsId);
		JournalpostDokumentInfoRelasjon relasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSom)
				.journalpost(domainJournalpost)
				.dokumentInfo(domainDokumentInfo)
				.build();

		domainJournalpost.addJournalpostDokumentInfoRelasjon(relasjon);
	}

	private void addFildetaljer(final DokumentInfo domainDokumentInfo, final String bestillingsId) {
		final String filreferanse = domainDokumentInfo.getTilleggsopplysninger().get(FILREFERANSE_ID_KEY);
		final DoksysDokument doksysDokument = createDokumentResultWithDocumentsFromGoogleCloudStorage(filreferanse, bestillingsId);

		domainDokumentInfo.addFilDetaljer(FilDetaljer.builder()
				.filtype(FilTypeCode.PDFA)
				.variantFormat(VariantFormatCode.ARKIV)
				.fileContent(doksysDokument.getPdf())
				.filUuid(UUID.randomUUID().toString())
				.dokumentInfo(domainDokumentInfo)
				.build());
		domainDokumentInfo.addFilDetaljer(FilDetaljer.builder()
				.filtype(FilTypeCode.AXML)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.fileContent(doksysDokument.getAxml())
				.filUuid(UUID.randomUUID().toString())
				.dokumentInfo(domainDokumentInfo)
				.build());
	}

	private DoksysDokument createDokumentResultWithDocumentsFromGoogleCloudStorage(String objectName, String bestillingsId) {
		log.info("tjoark112 henter dokument fra Google Cloud Storage. objectName={}", objectName);

		Optional<String> jsonPayload = dokprodMellomlagerStorage.downloadObject(objectName, bestillingsId);

		if (jsonPayload.isEmpty()) {
			throw new DokarkivTechnicalException(format("tjoark112 fant ingen dokument i Google Cloud Storage med objectName=%s ", objectName));
		}

		return JsonSerializer.deserialize(jsonPayload.get(), DoksysDokument.class);
	}
}
