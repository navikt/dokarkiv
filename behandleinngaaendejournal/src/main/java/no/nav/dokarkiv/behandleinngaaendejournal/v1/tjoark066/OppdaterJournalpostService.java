package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.AbstractBehandleInngaaendeJournalService;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.validator.BrukerValidator;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.ldap.NavUserLdapService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service for TJOARK066 OppdaterJournalpost
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 23.05.2017.
 */
@Slf4j
@Component
public class OppdaterJournalpostService extends AbstractBehandleInngaaendeJournalService {
	private final JoarkRepository repository;
	private final OppdaterJournalpostValidator validator;

	@Inject
	public OppdaterJournalpostService(JoarkRepository repository, OppdaterJournalpostValidator validator, NavUserLdapService navUserLdapService) {
		super(navUserLdapService);
		this.repository = repository;
		this.validator = validator;
	}

	public void oppdaterJournalpost(OppdaterJournalpostRequestTo request) {
		validator.validateInput(request);

		OppdaterJournalpostTo oppdaterJournalpostTo = request.getOppdaterJournalpostTo();
		Journalpost journalpost = getJournalpost(oppdaterJournalpostTo);

		validator.validateJournalpost(journalpost, oppdaterJournalpostTo);
		String userId = hentLdapBrukernavn(journalpost.getJournalpostId());
		updateAndPersist(journalpost, oppdaterJournalpostTo, userId);
	}

	private Journalpost getJournalpost(OppdaterJournalpostTo to) {
		Long journalpostId = Long.valueOf(to.getJournalpostId());
		return repository.findById(journalpostId).orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + to.getJournalpostId()));
	}

	private void updateAndPersist(Journalpost journalpost, OppdaterJournalpostTo input, String userId) {
		updateJournalpostFields(journalpost, input, userId);
		updateSaksrelasjonFields(journalpost, input, userId);
		updateBrukerFields(journalpost, input);
		updateHovedDokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon(), input, userId);
		updateVedleggDokumentInfo(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG), input, userId);
	}

	private void updateJournalpostFields(Journalpost journalpost, OppdaterJournalpostTo input, String userId) {
		boolean endret = false;
		String innhold = input.getInnhold();
		String avsendMottakerId = null;
		String avsendMottakerNavn = null;

		if (input.getAvsenderTo() != null) {
			avsendMottakerNavn = input.getAvsenderTo().getAvsenderNavn();
			avsendMottakerId = input.getAvsenderTo().getAvsenderId();
		}
		if (!Strings.isNullOrEmpty(innhold)) {
			journalpost.setInnhold(innhold);
			endret = true;
		}
		if (input.getTema() != null) {
			journalpost.setFagomrade(input.getTema());
			endret = true;
		}
		if (!Strings.isNullOrEmpty(avsendMottakerId)) {
			journalpost.setAvsenderMottakerId(avsendMottakerId);
			endret = true;
		}
		if (!Strings.isNullOrEmpty(avsendMottakerNavn)) {
			journalpost.setAvsenderMottaker(avsendMottakerNavn);
			endret = true;
		}
		if (endret) {
			journalpost.setEndretAvNavn(userId);
			journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
	}

	private void updateSaksrelasjonFields(Journalpost journalpost, OppdaterJournalpostTo input, String userId) {
		boolean newSak = false;
		if (input.getArkivSak() != null) {
			Saksrelasjon saksrelasjon;
			if (journalpost.getSaksrelasjon() == null) {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
			} else {
				saksrelasjon = journalpost.getSaksrelasjon();
			}
			saksrelasjon.setSakId(input.getArkivSak().getArkivSakId());
			saksrelasjon.setFagsystem(input.getArkivSak().getArkivSakSystem());
			saksrelasjon.setEndretAvNavn(userId);
			saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));

			if (newSak) {
				journalpost.setSaksrelasjon(saksrelasjon);
			}
		}
	}

	private void updateBrukerFields(Journalpost journalpost, OppdaterJournalpostTo input) {
		if (input.getAktoerTo() != null) {
			boolean endret = false;

			final boolean opprettet = addBruker(journalpost);
			Bruker bruker = getLatestBruker(journalpost);

			if (!input.getAktoerTo().getBrukerTypeCode().equals(bruker.getBrukerType())) {
				bruker.setBrukerType(input.getAktoerTo().getBrukerTypeCode());
				endret = true;
			}

			if (!input.getAktoerTo().getAktoerId().equals(bruker.getBrukerId())) {
				bruker.setBrukerId(input.getAktoerTo().getAktoerId());
				endret = true;
			}
			BrukerValidator.validate(bruker);

			if (!opprettet && endret) {
				bruker.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			}
		}
	}

	/**
	 * Gets the latest bruker
	 */
	private Bruker getLatestBruker(Journalpost journalpost) {
		assert (!journalpost.getBrukere().isEmpty());
		List<Bruker> sortedCopy = Ordering.from((Comparator<Bruker>) (o1, o2) ->
				LocalDateTime.ofInstant(o2.getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault())
						.compareTo(LocalDateTime.ofInstant(o1.getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault())))
				.sortedCopy(journalpost.getBrukere());
		return sortedCopy.get(0);
	}

	/**
	 * Create the bruker if it does not exist
	 */
	private boolean addBruker(Journalpost journalpost) {
		boolean opprettet = false;
		Set<Bruker> brukere = journalpost.getBrukere();
		if (brukere.isEmpty()) {
			Bruker newBruker = new Bruker();
			newBruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
			journalpost.addBruker(newBruker);
			opprettet = true;
		}
		return opprettet;
	}

	private void updateHovedDokumentInfo(JournalpostDokumentInfoRelasjon relasjon, OppdaterJournalpostTo input, String userId) {
		if (input.getHoveddokument() != null && relasjon != null) {
			DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
			boolean endret = false;
			if (dokumentInfo.getId().equals(input.getHoveddokument().getDokumentId())) {
				if (!Strings.isNullOrEmpty(input.getHoveddokument().getTittel())) {
					dokumentInfo.setTittel(input.getHoveddokument().getTittel());
				}
				if (input.getHoveddokument().getDokumentkategori() != null) {
					dokumentInfo.setKategori(input.getHoveddokument().getDokumentkategori());
				}
				endret = true;
			}
			if (endret) {
				dokumentInfo.setEndretAvNavn(userId);
				dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			}
		}
	}

	private void updateVedleggDokumentInfo(Set<JournalpostDokumentInfoRelasjon> vedlegg, OppdaterJournalpostTo input, String userId) {
		if (!input.getVedlegg().isEmpty()) {
			for (JournalpostDokumentInfoRelasjon relasjon : vedlegg) {
				updateDokumentInfo(input, relasjon, userId);
			}
		}
	}

	private void updateDokumentInfo(OppdaterJournalpostTo input, JournalpostDokumentInfoRelasjon relasjon, String userId) {
		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		boolean endret = false;
		for (DokumentInformasjonTo di : input.getVedlegg()) {
			if (dokumentInfo.getId().equals(di.getDokumentId())) {
				if (!Strings.isNullOrEmpty(di.getTittel())) {
					dokumentInfo.setTittel(di.getTittel());
				}
				if (di.getDokumentkategori() != null) {
					dokumentInfo.setKategori(di.getDokumentkategori());
				}
				endret = true;
			}
		}
		if (endret) {
			dokumentInfo.setEndretAvNavn(userId);
			dokumentInfo.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		}
	}
}