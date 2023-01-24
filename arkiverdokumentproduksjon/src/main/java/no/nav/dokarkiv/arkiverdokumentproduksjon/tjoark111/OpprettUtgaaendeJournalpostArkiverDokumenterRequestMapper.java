package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
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
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Vedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentRequest;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.util.ConverterUtils.stringToEnum;
import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.convertFilType;
import static org.apache.commons.lang3.StringUtils.trim;

@Component
public class OpprettUtgaaendeJournalpostArkiverDokumenterRequestMapper {

	private final KildeNavnPopulator kildeNavnPopulator;

	public OpprettUtgaaendeJournalpostArkiverDokumenterRequestMapper(KildeNavnPopulator kildeNavnPopulator) {
		this.kildeNavnPopulator = kildeNavnPopulator;
	}

	public OpprettUtgaaendeJournalpostArkiverDokumentRequestTo map(OpprettUtgaaendeJournalpostArkiverDokumentRequest wsRequest) {

		Journalpost domainJournalpost = createDomainJournalpostBase(wsRequest.getJournalpost());
		addBruker(domainJournalpost, wsRequest.getBruker());
		setSaksrelasjon(domainJournalpost, wsRequest.getSaksrelasjon());
		addJournalpostDokumentInfoRelasjon(domainJournalpost, wsRequest.getJournalpostDokumentInfoRelasjon());

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder
				.currentRequestContext().getComponentId());

		return OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.builder()
				.journalpost(domainJournalpost)
				.forsokFerdigstilling(wsRequest.isForsokFerdigstilling())
				.journalforendeEnhet(wsRequest.getJournalpost() == null ? null : wsRequest.getJournalpost()
						.getJournalforendeEnhet())
				.vedleggList(mapVedlegg(wsRequest.getVedlegg()))
				.build();

	}

	private List<OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg> mapVedlegg(List<Vedlegg> vedleggList) {

		if (vedleggList == null || vedleggList.isEmpty()) {
			return new ArrayList<>();
		}

		return vedleggList.stream()
				.map(vedlegg -> OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg.builder()
						.knyttesFraJournalpostId(vedlegg.getKnyttesFraJournalpostId() == null ? null : Long.valueOf(vedlegg.getKnyttesFraJournalpostId()))
						.dokumentInfoId(vedlegg.getDokumentInfoId() == null ? null : Long.valueOf(vedlegg.getDokumentInfoId()))
						.build())
				.collect(Collectors.toList());

	}

	private Journalpost createDomainJournalpostBase(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Journalpost journalpost) {
		if (journalpost == null) {
			return new Journalpost();
		}

		return Journalpost.builder()
				.journalposttype(JournalpostTypeCode.U)
				.fagomrade(stringToEnum(FagomradeCode.class, journalpost.getTema()))
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.innhold(journalpost.getInnhold())
				.dokumentDato(journalpost.getDatoDokument() == null || journalpost.getDatoDokument()
						.toGregorianCalendar() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar()
						.getTime())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.utsendingskanal(journalpost.getUtsendingskanal() == null ? null : UtsendingsKanalCode.valueOf(journalpost
						.getUtsendingskanal()))
				.kanalReferanseId(journalpost.getKanalreferanseId())
				.tilleggsopplysninger(addTilleggsopplysningAsMap(journalpost.getTilleggsopplysninger()))
				.build();
	}

	private void addBruker(Journalpost domainJournalpost,
						   no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Bruker bruker) {

		if (bruker == null) {
			return;
		}

		domainJournalpost.addBruker(Bruker.builder()
				.brukerId(trim(bruker.getBrukerId()))
				.brukerType(stringToEnum(BrukerTypeCode.class, bruker.getBrukerType()))
				.build());
	}


	private void setSaksrelasjon(Journalpost domainJournalpost,
								 no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Saksrelasjon sakrelasjon) {

		if (sakrelasjon == null) {
			return;
		}

		domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
				.saknrfk(sakrelasjon.getSaksnummer())
				.fagsystem(stringToEnum(FagsystemCode.class, sakrelasjon.getFagsystem()))
				.build());
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost domainJournalpost,
													List<no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.JournalpostDokumentInfoRelasjon> dokumentInfoRelasjonList) {

		dokumentInfoRelasjonList.forEach(dokumentInfoRelasjon -> {
			no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.DokumentInfo dokumentInfo = dokumentInfoRelasjon
					.getDokumentInfo();
			JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
					.tilknyttetJournalpostSom(stringToEnum(TilknyttetJournalpostSomCode.class, dokumentInfoRelasjon.getTilknyttetJournalpostSom()))
					.journalpost(domainJournalpost)
					.tilknyttetAvNavn(domainJournalpost.getOpprettetAvNavn())
					.dokumentInfo(DokumentInfo.builder()
							.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
							.dokumentFerdigDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
							.kategori(stringToEnum(DokumentKategoriCode.class, dokumentInfo.getKategori()))
							.tittel(dokumentInfo.getTittel())
							.brevkode(dokumentInfo.getBrevkode())
							.dokumenttypeId(dokumentInfo.getDokumentTypeId())
							.originalJournalpost(domainJournalpost)
							.build()).build();

			addFildetaljer(domainDokumentInfoRelasjon, dokumentInfoRelasjon.getDokumentInfo().getFildetaljerListe());

			domainJournalpost.addJournalpostDokumentInfoRelasjon(domainDokumentInfoRelasjon);

		});
	}

	private void addFildetaljer(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon,
								List<no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Fildetaljer> fildetaljerList) {

		fildetaljerList.forEach(fildetaljer -> journalpostDokumentInfoRelasjon.getDokumentInfo()
				.addFilDetaljer(FilDetaljer.builder()
						.filtype(stringToEnum(FilTypeCode.class, convertFilType(fildetaljer.getFiltype())))
						.variantFormat(stringToEnum(VariantFormatCode.class, fildetaljer.getVariantformat()))
						.fileContent(fildetaljer.getIkkeRedigerbartdokument())
						.filUuid(FilDetaljer.generateUuid())
						.build()));
	}

	private Map<String, String> addTilleggsopplysningAsMap(Tilleggsopplysning tilleggsopplysning) {
		if (tilleggsopplysning == null) {
			return new HashMap<>();
		}
		Map<String, String> tilleggsopplysningMap = new HashMap<>();
		tilleggsopplysningMap.put(tilleggsopplysning.getOpplysningsnoekkel(), tilleggsopplysning.getOpplysningsverdi());
		return tilleggsopplysningMap;
	}

}
