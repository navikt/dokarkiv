package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.AvsenderMottaker;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Mapper for domain response to ws-reponse.
 *
 * @author Torgeir Cook, Visma Consulting.
 */
public class HentMinTilgjengeligJournalpostListeV2ResponseMapper {

//	private Mapper dozerMapper;

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
//				journalpost = dozerMapper.map(innsynJournalpostTo.getJournalpost(), Journalpost.class, "caseInngaende");
			} else {
//				journalpost = dozerMapper.map(innsynJournalpostTo.getJournalpost(), Journalpost.class, "caseUtgaaende");
			}
		} else {
//			journalpost = dozerMapper.map(innsynJournalpostTo.getJournalpost(), Journalpost.class, "caseUtgaaende"); FIXME
		}
		mapAvsenderMottaker(journalpost, innsynJournalpostTo);
		mapSendtDato(journalpost, innsynJournalpostTo.getJournalpost());
		mappedFerdigstiltDato(journalpost, innsynJournalpostTo.getJournalpost());
		mapInnsynDokument(journalpost, innsynJournalpostTo);
		Collections.sort(journalpost.getDokumentinfoRelasjonListe(), new JournalpostDokumentInfoRelasjonV2Comparator());
		return journalpost;
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
