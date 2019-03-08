package no.nav.dokarkiv.journalpost.v1.rjoark202.util;

import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;

import java.util.Map;
import java.util.stream.Collectors;


public class JournalpostMapper {

	public Journalpost map(OpprettJournalpostRequest request) {

		Journalpost.JournalpostBuilder builder = Journalpost.builder()
				.journalposttype(mapJournalposttype(request.getJournalpostType()))
				.journalstatus(mapJournalstatus(request))
				.innhold(request.getTittel())
				.fagomrade(FagomradeCode.valueOf(request.getTema()))
				.avsenderMottaker(request.getAvsenderMottaker() == null ? null : request.getAvsenderMottaker().getAvsenderMottakerNavn())
				.avsenderMottakerId(request.getAvsenderMottaker() == null ? null : request.getAvsenderMottaker().getIdentifikator())
				.behandlingstema(Behandlingstema.valueOf(request.getBehandlingstema()))
				.tilleggsopplysninger(mapTilleggsopplysninger(request))
				.mottakskanal(mapMottakskanal(request))
				.utsendingskanal(mapUtsendingskanal(request))
				.kanalReferanseId(request.getEksternReferanseId());

		mapSaksrelasjon(builder, request);

		Journalpost journalpost = builder.build();

		addBruker(journalpost, request);
		addJournalpostDokumentInfoRelasjon(journalpost, request);

		return journalpost;
	}

	private JournalpostTypeCode mapJournalposttype(JournalpostType request) {
		if (JournalpostType.INNGAAENDE.equals(request)){
			return JournalpostTypeCode.I;
		} else if (JournalpostType.UTGAAENDE.equals(request)) {
			return JournalpostTypeCode.U;
		} else { // NOTAT
			return JournalpostTypeCode.N;
		}
	}

	private JournalStatusCode mapJournalstatus(OpprettJournalpostRequest request) {
		if (request.getDokumenter().isEmpty()){
			return JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) ? JournalStatusCode.OD : JournalStatusCode.R;
		} else {
			return JournalpostType.INNGAAENDE.equals(request.getJournalpostType()) ? JournalStatusCode.M : JournalStatusCode.D;
		}
	}

	private Map<String, String> mapTilleggsopplysninger(OpprettJournalpostRequest request) {
		return request.getTilleggsopplysninger().stream().collect(Collectors.toMap(Tilleggsopplysning::getNokkel, Tilleggsopplysning::getVerdi));
	}

	private MottaksKanalCode mapMottakskanal(OpprettJournalpostRequest request) {
		if (JournalpostType.INNGAAENDE.equals(request.getJournalpostType())) {
			return MottaksKanalCode.valueOf(request.getKanal());
		}
		return null;
	}

	private UtsendingsKanalCode mapUtsendingskanal(OpprettJournalpostRequest request) {
		if (! JournalpostType.INNGAAENDE.equals(request.getJournalpostType())) {
			return UtsendingsKanalCode.valueOf(request.getKanal());
		}
		return null;
	}

	private void mapSaksrelasjon(Journalpost.JournalpostBuilder builder, OpprettJournalpostRequest request) {
		if (request.getArkivSak() != null) {
			builder.saksrelasjon(Saksrelasjon.builder()
					.sakId(request.getArkivSak().getArkivsaksnummer())
					.fagsystem(Arkivsaksystem.GSAK.equals(request.getArkivSak().getArkivsaksystem()) ? FagsystemCode.FS22 : FagsystemCode.PEN)
					.build());
		}
	}

	private void addBruker(Journalpost jp, OpprettJournalpostRequest request) {
		if (request.getBruker() != null){
			jp.addBruker(Bruker.builder()
					.brukerId(request.getBruker().getIdentifikator())
					.brukerType(BrukerIdType.FNR.equals(request.getBruker().getBrukerIdType()) ? BrukerTypeCode.PERSON : BrukerTypeCode.ORGANISASJON)
					.build());
		}
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost jp, OpprettJournalpostRequest request) {
		if (!request.getDokumenter().isEmpty()) {
			addJournalpostDokumentInfoRelasjon(jp, request.getDokumenter().get(0), TilknyttetJournalpostSomCode.HOVEDDOKUMENT);

			if (request.getDokumenter().size() > 1){
				request.getDokumenter().stream().skip(1).forEach(dokument -> addJournalpostDokumentInfoRelasjon(jp, dokument, TilknyttetJournalpostSomCode.VEDLEGG));
			}
		}
	}

	private void addJournalpostDokumentInfoRelasjon(Journalpost jp, Dokument dokument, TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode){
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.kategori(DokumentKategoriCode.valueOf(dokument.getDokumentKategori()))
				.tittel(dokument.getTittel())
				.brevkode(dokument.getBrevkode())
				.originalJournalpost(jp)
				.build();
		// add fildetaljer


		JournalpostDokumentInfoRelasjon relasjon = JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
				.journalpost(jp)
				.dokumentInfo(dokumentInfo)
				.build();
		jp.addJournalpostDokumentInfoRelasjon(relasjon);
	}
}
