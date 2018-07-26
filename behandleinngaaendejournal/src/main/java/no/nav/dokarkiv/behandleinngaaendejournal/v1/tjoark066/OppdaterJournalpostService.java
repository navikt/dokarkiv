package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
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
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.ldap.BrukernavnLdapService;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.domain.IdentType;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Service for TJOARK066 OppdaterJournalpost
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 23.05.2017.
 */
public class OppdaterJournalpostService {
	private static final String UKJENT_BRUKER = "Ukjent";
	private static final Logger log = LoggerFactory.getLogger(OppdaterJournalpostService.class);
	private String userId = UKJENT_BRUKER;
	private JoarkRepository repository;
	private OppdaterJournalpostValidator validator;
	private final BrukernavnLdapService brukernavnLdapService;

	@Inject
	public OppdaterJournalpostService(JoarkRepository repository, OppdaterJournalpostValidator validator, BrukernavnLdapService brukernavnLdapService) {
		this.repository = repository;
		this.validator = validator;
		this.brukernavnLdapService = brukernavnLdapService;
	}

	public void oppdaterJournalpost(OppdaterJournalpostRequestTo request) {
		validator.validateInput(request);

		OppdaterJournalpostTo oppdaterJournalpostTo = request.getOppdaterJournalpostTo();
		Journalpost journalpost = getJournalpost(oppdaterJournalpostTo);

		validator.validateJournalpost(journalpost, oppdaterJournalpostTo);
		userId = hentLdapBrukernavn(journalpost.getJournalpostId());
		updateAndPersist(journalpost, oppdaterJournalpostTo);
	}

	private Journalpost getJournalpost(OppdaterJournalpostTo to) {
		Long journalpostId = Long.valueOf(to.getJournalpostId());
		return repository.findById(journalpostId).orElse(null);
	}

	private String hentLdapBrukernavn(Long journalpostId) {
		String userId = MDC.get(MDC_USER_ID);
		if (StringUtils.isEmpty(userId)) {
			log.warn(String.format("Kan ikke utlede brukerident på rett format fra SAML-token. journalpostId=%s", journalpostId.toString()));
			return UKJENT_BRUKER;
		}
		
		String ldapNavn = userId;
		IdentType type = SubjectHandler.getSubjectHandler().getIdentType();
		if (type.equals(IdentType.InternBruker)) {
			ldapNavn = brukernavnLdapService.searchWithRetry(userId);
			if (ldapNavn.trim().equals(userId.trim())) {
				log.warn(String.format("Feil ved søk mot LDAP. journalpostId=%s", journalpostId.toString()));
			}
		}
		return ldapNavn;
	}

	private void updateAndPersist(Journalpost journalpost, OppdaterJournalpostTo input) {
		updateJournalpostFields(journalpost, input);
		updateSaksrelasjonFields(journalpost, input);
		updateBrukerFields(journalpost, input);
		updateHovedDokumentInfo(journalpost.findHoveddokumentDokumentInfoRelasjon(), input);
		updateVedleggDokumentInfo(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG), input);
	}

	private void updateJournalpostFields(Journalpost journalpost, OppdaterJournalpostTo input) {
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

	private void updateSaksrelasjonFields(Journalpost journalpost, OppdaterJournalpostTo input) {
		boolean newSak = false;
		if (input.getArkivSak() != null) {
			Saksrelasjon saksrelasjon;
			if (journalpost.getSaksrelasjon() != null) {
				saksrelasjon = journalpost.getSaksrelasjon();
			} else {
				saksrelasjon = new Saksrelasjon();
				saksrelasjon.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				newSak = true;
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
			boolean opprettet = addBruker(journalpost);

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
		assert(!journalpost.getBrukere().isEmpty());
		List<Bruker> sortedCopy = Ordering.from(new Comparator<Bruker>() {
			@Override
			public int compare(Bruker o1, Bruker o2) {
				return LocalDateTime.fromDateFields(o2.getChangeStamp().getCreatedDate()).compareTo(LocalDateTime.fromDateFields(o1.getChangeStamp().getCreatedDate()));
			}
		}).sortedCopy(journalpost.getBrukere());
		return sortedCopy.get(0);
	}

	/**
	 * Create the bruker if it does not exist
	 */
	private boolean addBruker(Journalpost journalpost) {
		boolean opprettet = false;
		Set<Bruker> brukere = journalpost.getBrukere();
		if(brukere.isEmpty()) {
			Bruker newBruker = new Bruker();
			newBruker.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
			journalpost.addBruker(newBruker);
			opprettet = true;
		}
		return opprettet;
	}

	private void updateHovedDokumentInfo(JournalpostDokumentInfoRelasjon relasjon, OppdaterJournalpostTo input) {
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

	private void updateVedleggDokumentInfo(Set<JournalpostDokumentInfoRelasjon> vedlegg, OppdaterJournalpostTo input) {
		if (!input.getVedlegg().isEmpty()) {
			for (JournalpostDokumentInfoRelasjon relasjon : vedlegg) {
				updateDokumentInfo(input, relasjon);
			}
		}
	}

	private void updateDokumentInfo(OppdaterJournalpostTo input, JournalpostDokumentInfoRelasjon relasjon) {
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