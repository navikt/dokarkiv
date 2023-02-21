package no.nav.dokarkiv.journalpost.v1.mappers;

import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.consumer.pdl.PersonIkkeFunnetException;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

@Component
public class OpprettJournalpostApiRequestMapper {

	private final IdentConsumer identConsumer;

	public OpprettJournalpostApiRequestMapper(IdentConsumer identConsumer) {
		this.identConsumer = identConsumer;
	}

	public Journalpost map(OpprettJournalpostRequest request, Long sakId) {
		Journalpost journalpost = Journalpost.builder()
				.journalposttype(mapJournalposttype(request.getJournalposttype()))
				.journalstatus(mapJournalstatus(request))
				.journalForendeEnhetId(request.getJournalfoerendeEnhet())
				.innhold(request.getTittel())
				.fagomrade(mapTema(request))
				.avsenderMottaker(hentNavn(request))
				.avsenderMottakerId(request.getAvsenderMottaker() == null ? null : trim(request.getAvsenderMottaker().getId()))
				.avsenderMottakerIdType(request.getAvsenderMottaker() == null ? null : mapAvsenderMottakerType(request.getAvsenderMottaker()
						.getIdType()))
				.land(request.getAvsenderMottaker() == null ? null : trim(request.getAvsenderMottaker().getLand()))
				.behandlingstema(request.getBehandlingstema())
				.tilleggsopplysninger(mapTilleggsopplysninger(request))
				.mottakskanal(mapMottakskanal(request))
				.utsendingskanal(mapUtsendingskanal(request))
				.kanalReferanseId(request.getEksternReferanseId())
				.mottattDato(mapMottattDato(request))
				.dokumentDato(mapDokumentDato(request))
				.innsyn(mapOverstyrInnsynsregler(request))
				.build();

		addSaksrelasjon(journalpost, request, sakId);
		addBruker(journalpost, request);
		addJournalpostDokumentInfoRelasjon(journalpost, request);

		return journalpost;
	}

	private static InnsynCode mapOverstyrInnsynsregler(OpprettJournalpostRequest request) {
		return request.getOverstyrInnsynsregler() == null ? null : InnsynCode.valueOf(request.getOverstyrInnsynsregler());
	}

	private String hentNavn(OpprettJournalpostRequest request) {
		final AvsenderMottaker avsenderMottaker = request.getAvsenderMottaker();
		if (avsenderMottaker != null && isNotBlank(avsenderMottaker.getNavn())) {
			return avsenderMottaker.getNavn();
		} else if (avsenderMottaker != null && isNotBlank(avsenderMottaker.getId()) &&
				(avsenderMottaker.getIdType() == null || avsenderMottaker.getIdType() == AvsenderMottakerIdType.FNR)) {
			return identConsumer.hentPersonIdent(avsenderMottaker.getId(), request.getTema());
		}
		return null;
	}

	private JournalpostTypeCode mapJournalposttype(JournalpostType request) {
		if (JournalpostType.INNGAAENDE.equals(request)) {
			return JournalpostTypeCode.I;
		} else if (JournalpostType.UTGAAENDE.equals(request)) {
			return JournalpostTypeCode.U;
		} else { // NOTAT
			return JournalpostTypeCode.N;
		}
	}

	private AvsenderMottakerIdTypeCode mapAvsenderMottakerType(AvsenderMottakerIdType request) {
		AvsenderMottakerIdTypeCode avsenderMottakerIdTypeCode = null;
		if (request != null) {
			switch (request) {
				case FNR:
					avsenderMottakerIdTypeCode = AvsenderMottakerIdTypeCode.FNR;
					break;
				case ORGNR:
					avsenderMottakerIdTypeCode = AvsenderMottakerIdTypeCode.ORGNR;
					break;
				case HPRNR:
					avsenderMottakerIdTypeCode = AvsenderMottakerIdTypeCode.HPRNR;
					break;
				case UTL_ORG:
					avsenderMottakerIdTypeCode = AvsenderMottakerIdTypeCode.UTL_ORG;
					break;
				default:
					throw new InputValideringFeiletException(String.format("AvsenderMottakerIdTypeCode validerer ikke mot kodeverk: %s.", request));

			}
		}
		return avsenderMottakerIdTypeCode;

	}

	private JournalStatusCode mapJournalstatus(OpprettJournalpostRequest request) {
		if (manglerDokumentvarianter(request)) {
			return JournalpostType.INNGAAENDE.equals(request.getJournalposttype()) ? JournalStatusCode.OD : JournalStatusCode.R;
		} else {
			return JournalpostType.INNGAAENDE.equals(request.getJournalposttype()) ? JournalStatusCode.M : JournalStatusCode.D;
		}
	}

	private boolean manglerDokumentvarianter(OpprettJournalpostRequest request) {
		// sjekker om ett eller flere dokumenter mangler dokumentvarianter
		return request.getDokumenter().stream().anyMatch(d -> isEmpty(d.getDokumentvarianter()));
	}

	private Map<String, String> mapTilleggsopplysninger(OpprettJournalpostRequest request) {
		return request.getTilleggsopplysninger() == null ? null :
				request.getTilleggsopplysninger()
						.stream()
						.collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
	}

	private MottaksKanalCode mapMottakskanal(OpprettJournalpostRequest request) {
		if (JournalpostType.INNGAAENDE.equals(request.getJournalposttype()) && isNotBlank(request.getKanal())) {
			return MottaksKanalCode.valueOf(request.getKanal());
		}
		return null;
	}

	private UtsendingsKanalCode mapUtsendingskanal(OpprettJournalpostRequest request) {
		if (!JournalpostType.INNGAAENDE.equals(request.getJournalposttype()) && isNotBlank(request.getKanal())) {
			final UtsendingsKanalCode utsendingsKanalCode = UtsendingsKanalCode.valueOf(request.getKanal());

			if (UtsendingsKanalCode.MIGRERING_L.equals(utsendingsKanalCode)) {
				return UtsendingsKanalCode.L;
			} else if (UtsendingsKanalCode.MIGRERING_S.equals(utsendingsKanalCode)) {
				return UtsendingsKanalCode.S;
			}
			return utsendingsKanalCode;
		}
		return null;
	}

	private java.util.Date mapMottattDato(OpprettJournalpostRequest request) {
		if (JournalpostType.INNGAAENDE.equals(request.getJournalposttype())) {
			return request.getDatoMottatt() == null ? Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()) : request.getDatoMottatt();
		}
		return null;
	}

	private java.util.Date mapDokumentDato(OpprettJournalpostRequest request) {
		return request.getDatoDokument() == null ?
				Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()) :	request.getDatoDokument();
	}

	private FagomradeCode mapTema(OpprettJournalpostRequest request) {
		return isBlank(request.getTema()) ? null : FagomradeCode.valueOf(request.getTema());
	}


	private void addSaksrelasjon(Journalpost journalpost, OpprettJournalpostRequest request, Long sakId) {
		if (request.getSak() != null) {
			journalpost.setSaksrelasjon(Saksrelasjon.builder()
					.sakId(mapSakId(request, sakId))
					.fagsystem(mapFagsystem(request))
					.journalpost(journalpost)
					.build());
		}
	}

	private Long mapSakId(OpprettJournalpostRequest request, Long sakId) {
		if (sakId != null) {
			return sakId;
		} else if (Sakstype.ARKIVSAK.equals(request.getSak().getSakstype()) || request.getSak().getSakstype() == null) {// Antas å være ARKIVSAK dersom feltet ikke er satt
			return parseLong(request.getSak().getArkivsaksnummer());
		} else if (Sakstype.FAGSAK.equals(request.getSak().getSakstype()) && Fagsaksystem.PP01.equals(request.getSak().getFagsaksystem())) {
			return parseLong(request.getSak().getFagsakId());
		} else {
			throw new UgyldigInputException("Kan ikke mappe sakId basert på input");
		}
	}

	private FagsystemCode mapFagsystem(OpprettJournalpostRequest request) {
		Sakstype sakstype = request.getSak().getSakstype();
		Fagsaksystem fagsaksystem = request.getSak().getFagsaksystem();
		Arkivsaksystem arkivsaksystem = request.getSak().getArkivsaksystem();
		if (Sakstype.ARKIVSAK.equals(sakstype) || request.getSak().getSakstype() == null) { // Antas å være ARKIVSAK dersom feltet ikke er satt
			return mapArkivsak(arkivsaksystem);
		} else {
			return mapFagsakEllerGenerellSak(sakstype, fagsaksystem);
		}
	}

	private FagsystemCode mapArkivsak(Arkivsaksystem arkivsaksystem) {
		if (Arkivsaksystem.PSAK.equals(arkivsaksystem)) {
			return FagsystemCode.PEN;
		} else if (Arkivsaksystem.GSAK.equals(arkivsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			throw new UgyldigInputException("Kan ikke mappe fagsystem basert på input");
		}
	}

	private FagsystemCode mapFagsakEllerGenerellSak(Sakstype sakstype, Fagsaksystem fagsaksystem) {

		if (isValidFagsaksystem(sakstype, fagsaksystem) && Fagsaksystem.PP01.equals(fagsaksystem)) {
			return FagsystemCode.PEN;
		} else if ((isValidFagsaksystem(sakstype, fagsaksystem) || Sakstype.GENERELL_SAK.equals(sakstype))
				&& !Fagsaksystem.PP01.equals(fagsaksystem)) {
			return FagsystemCode.FS22;
		} else {
			throw new UgyldigInputException("Kan ikke mappe fagsystem basert på input");
		}
	}

	private boolean isValidFagsaksystem(Sakstype sakstype, Fagsaksystem fagsaksystem) {
		return Arrays.stream(Fagsaksystem.values())
				.anyMatch(fagsak -> fagsak.equals(fagsaksystem) && Sakstype.FAGSAK.equals(sakstype));
	}

	private void addBruker(Journalpost jp, OpprettJournalpostRequest request) {
		if (request.getBruker() != null) {
			if (BrukerIdType.AKTOERID.equals(request.getBruker().getIdType())) {
				try {
					String fnr = identConsumer.hentFolkeregisterIdent(request.getBruker().getId());
					jp.addBruker(Bruker.builder()
							.brukerId(fnr)
							.brukerType(BrukerTypeCode.PERSON)
							.build());
				} catch (PersonIkkeFunnetException e) {
					// Fortsett uten å opprette bruker
				}
			} else {
				jp.addBruker(Bruker.builder()
						.brukerId(request.getBruker().getId())
						.brukerType(BrukerIdType.FNR.equals(request.getBruker()
								.getIdType()) ? BrukerTypeCode.PERSON : BrukerTypeCode.ORGANISASJON)
						.build());
			}
		}
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost jp, OpprettJournalpostRequest request) {
		if (!request.getDokumenter().isEmpty()) {
			createJournalpostDokumentInfoRelasjon(jp, request.getDokumenter().get(0), HOVEDDOKUMENT);

			if (request.getDokumenter().size() > 1) {
				request.getDokumenter()
						.stream()
						.skip(1)
						.forEach(dokument -> createJournalpostDokumentInfoRelasjon(jp, dokument, VEDLEGG));
			}
		}
	}

	//hack for sykepengeberegningsmodulen. ref MMA-6005
	private static final String BREVKODE_4936 = "4936";

	private void createJournalpostDokumentInfoRelasjon(Journalpost jp, Dokument dokument, TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.kategori(dokument.getDokumentKategori() != null ? DokumentKategoriCode.valueOf(dokument.getDokumentKategori()) : DokumentKategoriCode.IS)
				.tittel(dokument.getTittel())
				.dokumentstatus(Arrays.asList(JournalpostTypeCode.U, JournalpostTypeCode.N).contains(jp.getJournalposttype()) ?
						DokumentStatusCode.FERDIGSTILT : null)
				.brevkode(dokument.getBrevkode())
				.originalJournalpost(jp)
				//hack for sykepengeberegningsmodulen. ref MMA-6005
				.dokumenttypeId(BREVKODE_4936.equals(dokument.getBrevkode()) ? "I000067" : null)
				//FIXME: Kommentert ut pga feil i Gosys
				//.dokumentFerdigDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
				.build();

		if (dokument.getDokumentvarianter() != null) {
			dokument.getDokumentvarianter().forEach(
					dokumentVariant -> dokumentInfo.addFilDetaljer(FilDetaljer.builder()
							.filtype(mapFilType(dokumentVariant.getFiltype()))
							.variantFormat(mapVariantFormat(dokumentVariant.getVariantformat()))
							.filUuid(FilDetaljer.generateUuid())
							.fileContent(dokumentVariant.getFysiskDokument())
							.filnavn(dokumentVariant.getFilnavn())
							.dokumentInfo(dokumentInfo)
							.batchNavn(dokumentVariant.getBatchnavn())
							.build()));
		}

		JournalpostDokumentInfoRelasjon relasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.journalpost(jp)
				.dokumentInfo(dokumentInfo)
				.build();
		jp.addJournalpostDokumentInfoRelasjon(relasjon);
	}

	private FilTypeCode mapFilType(String filtype) {
		return FilTypeCode.valueOf(filtype);
	}

	private VariantFormatCode mapVariantFormat(String variantformat) {
		return VariantFormatCode.valueOf(variantformat);
	}
}