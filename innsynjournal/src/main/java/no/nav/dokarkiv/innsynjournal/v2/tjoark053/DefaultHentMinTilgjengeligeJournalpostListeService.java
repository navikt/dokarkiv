package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.journalpostliste.HentMinJPListeParameters;
import no.nav.dokarkiv.core.repository.journalpostliste.JournalpostListeRepository;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Torgeir Cook, Visma Consulting.
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Component
public class DefaultHentMinTilgjengeligeJournalpostListeService implements HentMinTilgjengeligeJournalpostListeService {

	@Inject
	private JournalpostListeRepository journalpostListeRepository;

	@Value("#{T(java.time.LocalDate).parse(\"${innsynjournal.v2.innsyn.earliest.date}\")}")
	private LocalDate earliestDateAllowed;

	private final JournalStatusCode[] journalStatusCodesAllowed =
			new JournalStatusCode[]{JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E};

	@Override
	public List<Journalpost> hentMineTilgjengeligeJournalposter(HentJournalpostListeToRequest hentJournalpostListeToRequest) {
		validateInput(hentJournalpostListeToRequest);
		HentMinJPListeParameters hentMinJPListeParameters = createHentMinJPListeParameters(hentJournalpostListeToRequest);
		List<Journalpost> journalposts = journalpostListeRepository.findJournalpostListe(hentMinJPListeParameters);
		filterJournalposts(journalposts);
		return journalposts;
	}

	private void validateInput(HentJournalpostListeToRequest hentJournalpostListeToRequest) {

		if (hentJournalpostListeToRequest.getSaksListe().isEmpty()) {
			throw new IllegalArgumentException("SaksListe can not be empty");
		}

		for (SakFagsystem sakFagsystem : hentJournalpostListeToRequest.getSaksListe()) {
			if (sakFagsystem.getSakId() == null) {
				throw new IllegalArgumentException("SaksId must be set");
			}
			if (sakFagsystem.getFagsystem() == null) {
				throw new IllegalArgumentException("FagsystemCode of sak with saksId " +
						sakFagsystem.getSakId() + ", must be set");
			}
		}
	}

	private void filterJournalposts(List<Journalpost> journalposts) {
		Iterator<Journalpost> iterator = journalposts.iterator();
		while (iterator.hasNext()) {
			Journalpost journalpost = iterator.next();
			JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon =
					journalpost.findHoveddokumentDokumentInfoRelasjon();
			if (hoveddokumentDokumentInfoRelasjon != null) {
				filterHoveddokument(iterator, journalpost, hoveddokumentDokumentInfoRelasjon);
			} else {
				iterator.remove();
			}
		}
	}

	private void filterHoveddokument(Iterator<Journalpost> iterator, Journalpost journalpost,
									 JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon) {
		DokumentInfo hovedDokumentInfo = hoveddokumentDokumentInfoRelasjon.getDokumentInfo();
		JournalpostTypeCode journalposttype = journalpost.getJournalposttype();

		if (journalposttype == N && hovedDokumentInfo.getKategori() != FORVALTNINGSNOTAT) {
			iterator.remove();
		} else if (isTrue(hovedDokumentInfo.getOrganInternt())) {
			iterator.remove();
		} else if (asList(U, N).contains(journalposttype) && hovedDokumentInfo.getDokumentstatus() != FERDIGSTILT) {
			iterator.remove();
		} else {
			filterVedlegg(journalpost);
			filterFildetaljer(journalpost);
		}
	}

	private void filterFildetaljer(Journalpost journalpost) {
		Set<JournalpostDokumentInfoRelasjon> infoRelasjoner = journalpost.getJournalpostDokumentInfoRelasjoner();

		for (JournalpostDokumentInfoRelasjon dokInfoRel : infoRelasjoner) {
			List<FilDetaljer> fdToRemove = Lists.newArrayList();
			for (FilDetaljer fd : dokInfoRel.getDokumentInfo().getFildetaljerListe()) {
				if (VariantFormatCode.ARKIV != fd.getVariantFormat()) {
					fdToRemove.add(fd);
				}
			}
			for (FilDetaljer fd : fdToRemove) {
				dokInfoRel.getDokumentInfo().removeFilDetaljer(fd);
			}
		}
	}

	private void filterVedlegg(Journalpost journalpost) {
		Set<JournalpostDokumentInfoRelasjon> vedleggs =
				journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG);
		List<JournalpostDokumentInfoRelasjon> doRemove = new ArrayList<>();
		for (JournalpostDokumentInfoRelasjon vedlegg : vedleggs) {
			JournalpostTypeCode journalposttype = vedlegg.getJournalpost().getJournalposttype();
			DokumentInfo dokumentInfo = vedlegg.getDokumentInfo();

			if (journalposttype == N && dokumentInfo.getKategori() != FORVALTNINGSNOTAT) {
				doRemove.add(vedlegg);
			} else if (isTrue(dokumentInfo.getOrganInternt())) {
				doRemove.add(vedlegg);
			} else if (asList(N, U).contains(journalposttype) && dokumentInfo.getDokumentstatus() != FERDIGSTILT) {
				doRemove.add(vedlegg);
			}
		}

		for (JournalpostDokumentInfoRelasjon removeVedlegg : doRemove) {
			journalpost.removeJournalpostDokumentInfoRelasjon(removeVedlegg);
		}
	}


	private HentMinJPListeParameters createHentMinJPListeParameters(HentJournalpostListeToRequest request) {
		HentMinJPListeParameters parameters = new HentMinJPListeParameters();
		for (SakFagsystem sakFagsystem : request.getSaksListe()) {
			parameters.addFagsystemSak(sakFagsystem);
		}
		parameters.setTidligstInnsynDato(Date.from(earliestDateAllowed.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		parameters.setTillattInnsynStatus(Lists.newArrayList(journalStatusCodesAllowed));
		parameters.setVisFeilRegistrert(false);
		List<FagomradeCode> skjulFagomraade = Lists.newArrayList();
		skjulFagomraade.add(FagomradeCode.KTR);
		parameters.setSkjulFagomraade(skjulFagomraade);
		return parameters;
	}

	public void setEarliestDateAllowed(LocalDate earliestDateAllowed) {
		this.earliestDateAllowed = earliestDateAllowed;
	}
}
