package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;

import no.nav.dokarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakTilleggsopplysningerConverter;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * JournalforInngaaendeForsendelseRequestMapper implementation
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 17.02.2017
 */
@Component
public class DefaultJournalforInngaaendeForsendelseRequestMapper {

	private ArkiverDokumentmottakTilleggsopplysningerConverter arkiverDokumentmottakTilleggsopplysningerConverter = new ArkiverDokumentmottakTilleggsopplysningerConverter();

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
				.saksrelasjon(journalpost.getSaksrelasjon() == null ? null :
						Saksrelasjon.builder()
								.fagsystem(stringToEnum(FagsystemCode.class, journalpost.getSaksrelasjon().getFagsystem()))
								.sakId(journalpost.getSaksrelasjon().getSaksnummer())
								.build())
				.tilleggsopplysninger(arkiverDokumentmottakTilleggsopplysningerConverter.convertTo(journalpost.getJournalpostTilleggsopplysninger()))
				.build();

		if (journalpost.getBruker() != null) {
			domainJournalpost.addBruker(Bruker.builder()
					.brukerType(stringToEnum(BrukerTypeCode.class, journalpost.getBruker().getBrukerType()))
					.brukerId(journalpost.getBruker().getBrukerId())
					.build());
		}


		journalpost.getJournalpostDokumentInfoRelasjon().forEach(relasjon ->
				domainJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
						.journalpost(domainJournalpost)
						.tilknyttetJournalpostSom(stringToEnum(TilknyttetJournalpostSomCode.class, relasjon.getTilknyttetJournalpostSom()
								.name()))
						.dokumentInfo(DokumentInfo.builder()
								.kategori(stringToEnum(DokumentKategoriCode.class, relasjon.getDokumentInfo().getKategori()))
								.sensitivt(relasjon.getDokumentInfo().isSensitivt())
								.tittel(relasjon.getDokumentInfo().getTittel())
								.brevkode(relasjon.getDokumentInfo().getBrevkode())
								.dokumenttypeId(relasjon.getDokumentInfo().getDokumentTypeId())
								.fildetaljerListe(relasjon.getDokumentInfo().getFildetaljerListe().stream()
										.map(fildetaljer -> FilDetaljer.builder()
												.fileContent(fildetaljer.getDokument())
												.filtype(stringToEnum(FilTypeCode.class, fildetaljer.getFiltype()))
												.filnavn(fildetaljer.getFilNavn())
												.variantFormat(stringToEnum(VariantFormatCode.class, fildetaljer.getVariantformat()))
												.build()).collect(Collectors.toSet())).build())
						.build()));

		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(domainJournalpost, RequestContextHolder.currentRequestContext()
				.getComponentId());

		return new JournalforInngaaendeForsendelseRequestTo(domainJournalpost);


	}


	private <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
		if (value == null) {
			return null;
		}
		return Enum.valueOf(clazz, value);
	}
}