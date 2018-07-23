package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Arkivtemaer;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.AvsenderMottaker;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Kommunikasjonsretninger;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Sak;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.SkannetInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.TilknyttetJournalpostSom;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mapper for domain response to ws-reponse.
 *
 * @author Torgeir Cook, Visma Consulting.
 */
@Component
public class HentMinTilgjengeligJournalpostListeV2ResponseMapper {

	/**
	 * Mapping of domain journalpost to ws-journalpost
	 *
	 * @param innsynJournalpostTo
	 * @return {@link Journalpost}
	 */
	public Journalpost map(InnsynJournalpostTo innsynJournalpostTo) {
		return mapJournalpost(innsynJournalpostTo);
	}

	/**
	 * Mapping of domain journalposts to ws-response
	 *
	 * @param innsynJournalpostTos
	 * @return {@link HentTilgjengeligJournalpostListeResponse}
	 */
	public HentTilgjengeligJournalpostListeResponse mapList(List<InnsynJournalpostTo> innsynJournalpostTos) {
		HentTilgjengeligJournalpostListeResponse response = new HentTilgjengeligJournalpostListeResponse();
		for (InnsynJournalpostTo innsynJournalpostTo : innsynJournalpostTos) {
			response.getJournalpostListe().add(mapJournalpost(innsynJournalpostTo));
		}
		return response;
	}

	private Journalpost mapJournalpost(InnsynJournalpostTo innsynJournalpostTo) {
		Journalpost journalpost = null;
		if (innsynJournalpostTo.getJournalpost().getJournalposttype() != null) {
			if (innsynJournalpostTo.getJournalpost().getJournalposttype().equals(JournalpostTypeCode.I)) {
				journalpost = mapInngaaende(innsynJournalpostTo.getJournalpost());
			} else {
				journalpost = mapUtgaaende(innsynJournalpostTo.getJournalpost());
			}
		} else {
			journalpost = mapUtgaaende(innsynJournalpostTo.getJournalpost());
		}
		mapAvsenderMottaker(journalpost, innsynJournalpostTo);
		mapSendtDato(journalpost, innsynJournalpostTo.getJournalpost());
		mappedFerdigstiltDato(journalpost, innsynJournalpostTo.getJournalpost());
		mapInnsynDokument(journalpost, innsynJournalpostTo);
		Collections.sort(journalpost.getDokumentinfoRelasjonListe(), new JournalpostDokumentInfoRelasjonV2Comparator());
		return journalpost;
	}

	private Journalpost mapUtgaaende(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		return mapBaseJournalpost(journalpost)
				.withKommunikasjonskanal(journalpost.getUtsendingskanal() == null ? null : journalpost.getUtsendingskanal().name());
	}

	private Journalpost mapInngaaende(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		return mapBaseJournalpost(journalpost)
				.withKommunikasjonskanal(journalpost.getMottakskanal() == null ? null : journalpost.getMottakskanal().name());
	}

	private Journalpost mapBaseJournalpost(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		Journalpost baseJournalpost = new Journalpost()
				.withJournalpostId(journalpost.getJournalpostId() == null ? null : journalpost.getJournalpostId().toString())
				.withKommunikasjonsretning(new Kommunikasjonsretninger().withValue(journalpost.getJournalposttype() == null ? null : journalpost.getJournalposttype().name()))
				.withArkivtema(new Arkivtemaer().withValue(journalpost.getFagomrade() == null ? null : journalpost.getFagomrade().name()))
				.withEksternPart(journalpost.getAvsenderMottaker())
				.withGjelderSak(mapSak(journalpost))
				.withMottatt(journalpost.getMottattDato() == null ? null : DateConverterUtil.convertDateToXMLGregorianCalendar(journalpost.getMottattDato()))
				.withOpprettet(journalpost.getChangeStamp() == null ? null : DateConverterUtil.convertDateToXMLGregorianCalendar(journalpost.getChangeStamp().getCreatedDate()))
				.withKanalReferanseId(journalpost.getKanalReferanseId());
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> {
			final DokumentInfo dokumentInfo = journalpostDokumentInfoRelasjon.getDokumentInfo();
			JournalfoertDokumentInfo journalfoertDokumentInfo = mapJournalfoertDokumentInfo(dokumentInfo);
			if(!dokumentInfo.getSkannetInnholdListe().isEmpty()) {
				dokumentInfo.getSkannetInnholdListe().forEach(skannetInnhold -> journalfoertDokumentInfo.getSkannetInnholdListe().add(
						new SkannetInnhold()
								.withSkannetInnholdId(skannetInnhold.getSkannetInnholdId().toString())
								.withVedleggInnhold(skannetInnhold.getVedleggInnhold())));
			}

			baseJournalpost.getDokumentinfoRelasjonListe().add(new DokumentinfoRelasjon()
					.withDokumentTilknyttetJournalpost(new TilknyttetJournalpostSom()
							.withValue(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom().name()))
					.withDokumentinfoRelasjonId(journalpostDokumentInfoRelasjon.getJournalpostDokumentInfoRelasjonId().toString())
					.withJournalfoertDokument(journalfoertDokumentInfo));
		});
		return baseJournalpost;
	}

	private JournalfoertDokumentInfo mapJournalfoertDokumentInfo(DokumentInfo dokumentInfo) {
		final Optional<FilDetaljer> filDetaljer = dokumentInfo.getFildetaljerListe().stream().findFirst();
		if(filDetaljer.isPresent()) {
			DokumentInnhold dokumentInnhold = mapDokumentInnhold(filDetaljer.get());
			return new JournalfoertDokumentInfo()
					.withDokumentId(dokumentInfo.getDokumentInfoId().toString())
					.withTittel(dokumentInfo.getTittel())
					.withBeskriverInnhold(dokumentInnhold);
		} else {
			return new JournalfoertDokumentInfo()
					.withDokumentId(dokumentInfo.getDokumentInfoId().toString())
					.withTittel(dokumentInfo.getTittel());
		}
	}

	private DokumentInnhold mapDokumentInnhold(FilDetaljer filDetaljer) {
		return new DokumentInnhold()
				.withFiltype(new Arkivfiltyper().withValue(filDetaljer.getFiltype().name()))
				.withVariantformat(new Variantformater().withValue(filDetaljer.getVariantFormat().name()));
	}

	private Sak mapSak(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		if (journalpost.getSaksrelasjon() == null) {
			return null;
		}
		return new Sak()
				.withSakId(journalpost.getSaksrelasjon().getSakId())
				.withFagsystem(new Fagsystemer().withValue(journalpost.getSaksrelasjon().getFagsystem().name()));
	}

	private void mapAvsenderMottaker(Journalpost journalpost, InnsynJournalpostTo innsynJournalpostTo) {
		InnsynJournalpostTo.AvsenderMottaker avsenderMottaker = innsynJournalpostTo.getAvsenderMottaker();
		switch (avsenderMottaker) {
			case JA:
				journalpost.setBrukerErAvsenderMottaker(AvsenderMottaker.JA);
				break;
			case NEI:
				journalpost.setBrukerErAvsenderMottaker(AvsenderMottaker.NEI);
				break;
			case KAN_IKKE_AVGJOERES:
				journalpost.setBrukerErAvsenderMottaker(AvsenderMottaker.KAN_IKKE_AVGJØRES);
				break;
			default:
				throw new IllegalArgumentException("Uknown AvsenderMottaker: " + avsenderMottaker);
		}
	}

	private void mapInnsynDokument(Journalpost journalpost, InnsynJournalpostTo innsynJournalpostTo) {
		for (DokumentinfoRelasjon dokumentinfoRelasjon : journalpost.getDokumentinfoRelasjonListe()) {
			JournalfoertDokumentInfo journalfoertDokument = dokumentinfoRelasjon.getJournalfoertDokument();
			Long dokumentId = Long.valueOf(journalfoertDokument.getDokumentId());
			Map<Long, InnsynJournalpostTo.DokumentInnsyn> dokumentInnsyn = innsynJournalpostTo.getDokumentInnsyn();
			if (dokumentInnsyn.containsKey(dokumentId)) {
				InnsynJournalpostTo.DokumentInnsyn innsyn = dokumentInnsyn.get(dokumentId);
				journalfoertDokument.setInnsynDokument(InnsynDokument.valueOf(innsyn.name()));
			}
		}
	}

	private void mapSendtDato(Journalpost mapped, no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		Date dato = journalpost.getJournalDato();
		if (journalpost.getSendtPrintDato() != null) {
			dato = journalpost.getSendtPrintDato();
		}
		if (journalpost.getEkspedertDato() != null) {
			dato = journalpost.getEkspedertDato();
		}
		mapped.setSendt(DateConverterUtil.convertDateToXMLGregorianCalendar(dato));
	}

	private void mappedFerdigstiltDato(Journalpost mapped, no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		Date newestDateOfDokumenInfosWithStatus = decideNewestDateOfDokumenInfosWithStatus(journalpost.findAllDokumentInfos(),
				DokumentStatusCode.FERDIGSTILT);
		Date journalDato = journalpost.getJournalDato();
		mapped.setFerdigstilt(DateConverterUtil
				.convertDateToXMLGregorianCalendar(journalDato != null ? journalDato : newestDateOfDokumenInfosWithStatus));
	}

	private Date decideNewestDateOfDokumenInfosWithStatus(List<DokumentInfo> dokumentInfos, DokumentStatusCode status) {
		Date newest = null;
		for (DokumentInfo dokumentInfo : dokumentInfos) {
			if (dokumentInfo.getDokumentstatus() == status) {
				Date dokumentFerdigDato = dokumentInfo.getDokumentFerdigDato();
				if (newest == null || dokumentFerdigDato != null && dokumentFerdigDato.after(newest)) {
					newest = dokumentFerdigDato;
				}
			}
		}
		return newest;
	}
}
