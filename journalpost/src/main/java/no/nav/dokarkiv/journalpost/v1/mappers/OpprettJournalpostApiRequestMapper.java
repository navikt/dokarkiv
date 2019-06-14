package no.nav.dokarkiv.journalpost.v1.mappers;

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
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
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OpprettJournalpostApiRequestMapper {

	public Journalpost map(OpprettJournalpostRequest request) {
		Journalpost journalpost = Journalpost.builder()
				.journalposttype(mapJournalposttype(request.getJournalpostType()))
				.journalstatus(mapJournalstatus(request))
				.innhold(request.getTittel())
				.fagomrade(FagomradeCode.valueOf(request.getTema()))
				.avsenderMottaker(request.getAvsenderMottaker() == null ? null : request.getAvsenderMottaker().getNavn())
				.avsenderMottakerId(request.getAvsenderMottaker() == null ? null : trim(request.getAvsenderMottaker().getId()))
				.avsenderMottakerIdType(request.getAvsenderMottaker() == null ? null : mapAvsenderMottakerType(request.getAvsenderMottaker()
						.getIdType()))
				.behandlingstema(mapBehandlingstema(request))
				.tilleggsopplysninger(mapTilleggsopplysninger(request))
				.mottakskanal(mapMottakskanal(request))
				.utsendingskanal(mapUtsendingskanal(request))
				.kanalReferanseId(request.getEksternReferanseId())
				.mottattDato(mapMottattDato(request))
				.dokumentDato(Date.valueOf(LocalDate.now()))
				.build();

		addSaksrelasjon(journalpost, request);
		addBruker(journalpost, request);
		addJournalpostDokumentInfoRelasjon(journalpost, request);

		return journalpost;
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
		if(request != null) {
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
					throw new InputValideringFeiletException(String.format("AvesenderMottakerIdTypeCode validerer ikke mot kodeverk: %s.", request));

			}
		}
		return avsenderMottakerIdTypeCode;

	}

	private JournalStatusCode mapJournalstatus(OpprettJournalpostRequest request) {
		if (request.getDokumenter().isEmpty()) {
			return JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) ? JournalStatusCode.OD : JournalStatusCode.R;
		} else {
			return JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) ? JournalStatusCode.M : JournalStatusCode.D;
		}
	}

	private Map<String, String> mapTilleggsopplysninger(OpprettJournalpostRequest request) {
		return request.getTilleggsopplysninger()
				.stream()
				.collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
	}

	private MottaksKanalCode mapMottakskanal(OpprettJournalpostRequest request) {
		if (JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) && isNotBlank(request.getKanal())) {
			return MottaksKanalCode.valueOf(request.getKanal());
		}
		return null;
	}

	private UtsendingsKanalCode mapUtsendingskanal(OpprettJournalpostRequest request) {
		if (!JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) && isNotBlank(request.getKanal())) {
			return UtsendingsKanalCode.valueOf(request.getKanal());
		}
		return null;
	}

	private Date mapMottattDato(OpprettJournalpostRequest request) {
		return JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) ? Date.valueOf(LocalDate.now()) : null;
	}

	private Behandlingstema mapBehandlingstema(OpprettJournalpostRequest request) {
		return isBlank(request.getBehandlingstema()) ? null : Behandlingstema.valueOf(request.getBehandlingstema());
	}


	private void addSaksrelasjon(Journalpost journalpost, OpprettJournalpostRequest request) {
		if (request.getSak() != null) {
			journalpost.setSaksrelasjon(Saksrelasjon.builder()
					.sakId(request.getSak().getArkivsaksnummer())
					.fagsystem(Arkivsaksystem.GSAK.equals(request.getSak()
							.getArkivsaksystem()) ? FagsystemCode.FS22 : FagsystemCode.PEN)
					.journalpost(journalpost)
					.build());
		}
	}

	private void addBruker(Journalpost jp, OpprettJournalpostRequest request) {
		if (request.getBruker() != null) {
			jp.addBruker(Bruker.builder()
					.brukerId(request.getBruker().getId())
					.brukerType(BrukerIdType.FNR.equals(request.getBruker()
							.getIdType()) ? BrukerTypeCode.PERSON : BrukerTypeCode.ORGANISASJON)
					.build());
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

	private void createJournalpostDokumentInfoRelasjon(Journalpost jp, Dokument dokument, TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.kategori(dokument.getDokumentKategori() != null ? DokumentKategoriCode.valueOf(dokument.getDokumentKategori()) : DokumentKategoriCode.IS)
				.tittel(dokument.getTittel())
				.dokumentstatus(Arrays.asList(JournalpostTypeCode.U, JournalpostTypeCode.N).contains(jp.getJournalposttype()) ?
						DokumentStatusCode.FERDIGSTILT : null)
				.brevkode(dokument.getBrevkode())
				.originalJournalpost(jp)
				.build();

		dokument.getDokumentvarianter().forEach(
				dokumentVariant -> dokumentInfo.addFilDetaljer(FilDetaljer.builder()
						.filtype(mapFilType(dokumentVariant.getFiltype()))
						.variantFormat(mapVariantFormat(dokumentVariant.getVariantformat()))
						.filUuid(FilDetaljer.generateUuid())
						.fileContent(dokumentVariant.getFysiskDokument())
						.dokumentInfo(dokumentInfo)
						.build()));

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