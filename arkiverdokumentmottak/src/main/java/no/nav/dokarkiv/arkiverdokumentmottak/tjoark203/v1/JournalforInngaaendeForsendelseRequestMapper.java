package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JournalforInngaaendeForsendelseRequestMapper implementation
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 17.02.2017
 */
@Component
public class JournalforInngaaendeForsendelseRequestMapper {

	@Inject
	private KildeNavnPopulator kildeNavnPopulator;

	public JournalforInngaaendeForsendelseRequestTo map(JournalforInngaaendeForsendelseRequest request) {

		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpost = request
				.getJournalpost();
		Journalpost domainJournalpost = Journalpost.builder()
				.fagomrade(stringToEnum(FagomradeCode.class, journalpost.getTema()))
				.journalForendeEnhetId(journalpost.getJournalforendeEnhet())
				.opprettetAvNavn(journalpost.getOpprettetAvNavn())
				.journalfortAvNavn(journalpost.getOpprettetAvNavn())
				.avsenderMottaker(journalpost.getAvsenderMottakerNavn())
				.avsenderMottakerId(journalpost.getAvsenderMottakerId())
				.innhold(journalpost.getInnhold())
				.mottattDato(journalpost.getDatoMottatt() == null ? null : journalpost.getDatoMottatt()
						.toGregorianCalendar()
						.getTime())
				.mottakskanal(stringToEnum(MottaksKanalCode.class, journalpost.getMottakskanal()))
				.dokumentDato(journalpost.getDatoDokument() == null ? null : journalpost.getDatoDokument()
						.toGregorianCalendar()
						.getTime())
				.tilleggsopplysninger(converTillegsopplysningerToMap(journalpost.getJournalpostTilleggsopplysninger()))
				.build();

		if (journalpost.getBruker() != null) {
			domainJournalpost.addBruker(Bruker.builder()
					.brukerType(stringToEnum(BrukerTypeCode.class, journalpost.getBruker().getBrukerType()))
					.brukerId(journalpost.getBruker().getBrukerId())
					.build());
		}

		if (journalpost.getSaksrelasjon() != null) {
			domainJournalpost.setSaksrelasjon(Saksrelasjon.builder()
					.fagsystem(stringToEnum(FagsystemCode.class, journalpost.getSaksrelasjon().getFagsystem()))
					.sakId(journalpost.getSaksrelasjon().getSaksnummer())
					.journalpost(domainJournalpost)
					.build());
		}

		journalpost.getJournalpostDokumentInfoRelasjon().forEach(relasjon ->
				domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
						.journalpost(domainJournalpost)
						.tilknyttetJournalpostSom(stringToEnum(TilknyttetJournalpostSomCode.class, relasjon.getTilknyttetJournalpostSom() == null ? null : relasjon
								.getTilknyttetJournalpostSom()
								.name()))
						.dokumentInfo(createDokumentInfo(relasjon, domainJournalpost))
						.build()));

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder.currentRequestContext()
				.getComponentId());

		return new JournalforInngaaendeForsendelseRequestTo(domainJournalpost);


	}

	public DokumentInfo createDokumentInfo(no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon relasjon, Journalpost domainJournalpost) {
		if (relasjon.getDokumentInfo() == null) {
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
				.getFildetaljerListe()
				.forEach(fildetaljer -> dokumentInfo.addFilDetaljer(FilDetaljer.builder()
						.fileContent(fildetaljer.getDokument())
						.filtype(stringToEnum(FilTypeCode.class, fildetaljer.getFiltype()))
						.filnavn(fildetaljer.getFilNavn())
						.filUuid(FilDetaljer.generateUuid())
						.variantFormat(stringToEnum(VariantFormatCode.class, fildetaljer.getVariantformat()))
						.build()));
		return dokumentInfo;
	}

	public Map<String, String> converTillegsopplysningerToMap(List<Tilleggsopplysning> source) {
		if (CollectionUtils.isEmpty(source)) {
			return null;
		}

		Map<String, String> destination = new HashMap<>();
		for (Tilleggsopplysning tilleggsopplysning : source) {
			destination.put(tilleggsopplysning.getOpplysningsnoekkel(), tilleggsopplysning.getOpplysningsverdi());
		}

		return destination;
	}

	private <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
		if (value == null) {
			return null;
		}
		return Enum.valueOf(clazz, value);
	}
}