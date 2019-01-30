package no.nav.dokarkiv.innsynjournal.v2;

import static java.util.Collections.singletonMap;
import static no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo.innsynJournalposts;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.ActionAttributeIds.READ_OPERATION;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.EnvironmentAttributeIds.ATTR_ENVIRONMENT_RECIEVER;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.EnvironmentAttributeIds.EXTERNAL;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.ResourceAttributeIds.ATTR_RESOURCE_TARGET;
import static no.nav.dokarkiv.innsynjournal.v2.security.urn.ResourceAttributeIds.JOURNALPOST_DOCUMENT;
import static no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds.ATTR_ACTION_ID;
import static no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds.ATTR_RESOURCE_ID;

import com.google.common.collect.Maps;
import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerService;
import no.nav.dokarkiv.core.consumer.aktoer.HentAktoerIdForIdentRequestTo;
import no.nav.dokarkiv.core.consumer.aktoer.HentAktoerIdForIdentResponseTo;
import no.nav.dokarkiv.core.consumer.aktoer.IdentDetaljerTo;
import no.nav.dokarkiv.core.consumer.aktoer.PersonIkkeFunnetException;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.JournalpostNotSupportedException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityTechnicalException;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentJournalpostListeToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligeJournalpostListeService;
import no.nav.dokarkiv.innsynjournal.v2.tjoark054.HentDokumentRequestTo;
import no.nav.dokarkiv.innsynjournal.v2.tjoark054.Tjoark054HentDokumentService;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostService;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostToRequest;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.security.tilgangskontroll.policy.pep.AccessControl;
import no.nav.modig.security.tilgangskontroll.policy.pep.AccessControlAttribute;
import no.nav.modig.security.tilgangskontroll.policy.pep.AttributeType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Adds security to the InnsynJournal-services
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Component
public class InnsynJournalV2SecurityFacade {

	private static final Logger log = LoggerFactory.getLogger(InnsynJournalV2SecurityFacade.class);

	@Value("#{T(java.time.LocalDate).parse(\"${innsynjournal.v2.innsyn.earliest.date}\")}")
	private LocalDate earliestAllowedDate;

	@Inject
	private Tjoark054HentDokumentService tjoark054HentDokumentService;
	@Inject
	private AktoerConsumerService aktoerConsumerService;
	@Inject
	private JoarkRepositorySkjermet joarkRepository;
	@Inject
	private HentMinTilgjengeligeJournalpostListeService hentMinTilgjengeligeJournalpostListeService;
	@Inject
	private IdentifiserJournalpostService identifiserJournalpostService;

	@AccessControl(attributes = {
			@AccessControlAttribute(name = ATTR_ACTION_ID, value = READ_OPERATION, type = AttributeType.ACTION),
			@AccessControlAttribute(name = ATTR_RESOURCE_TARGET, value = JOURNALPOST_DOCUMENT, type = AttributeType.RESOURCE),
			@AccessControlAttribute(name = ATTR_RESOURCE_ID, value = JOURNALPOST_DOCUMENT, type = AttributeType.RESOURCE),
			@AccessControlAttribute(name = ATTR_ENVIRONMENT_RECIEVER, value = EXTERNAL, type = AttributeType.SUBJECT)
			// Denne burde vært laget med type=environment, men en bug i
			// modig-security gjør at "environment"-variabler aldri blir sendt
			// inn som en del av XACML-requesten
	})
	public byte[] hentDokument(Long journalpostId, Long dokumentInfoId) throws NoJournalpostFoundException,
			DocumentNotFoundException {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElseThrow(() -> new NoJournalpostFoundException("Journalpost med id " + journalpostId + " eksisterer ikke", journalpostId));
		DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentInfoId);

		if (dokumentInfo == null) {
			throw new DocumentNotFoundException("DokumentInfo med dokumentInfoId=" + dokumentInfoId +
					" på Journalpost med journalpostId=" + journalpostId + " eksisterer ikke");
		}

		verifyAvsenderMottakerId(journalpost, dokumentInfoId);

		if (MottaksKanalCode.NAV_NO.equals(journalpost.getMottakskanal())) {
			return hentDokumentBytes(journalpostId, dokumentInfoId);
		}

		verifyJournalstatus(journalpost, dokumentInfoId);
		verifyCreatedAndJournalDate(journalpost, dokumentInfoId);
		verifySaksrelasjon(journalpost, dokumentInfoId);
		verifyFagomrade(journalpost, dokumentInfoId);
		verifyMottakskanal(journalpost, dokumentInfoId);
		verifyIfNotat(journalpost, dokumentInfo);
		verifyDokumentstatus(journalpost, dokumentInfo);
		verifyFildetaljer(journalpostId, dokumentInfo);
		verifyInnskrenketPartsinnsyn(journalpostId, dokumentInfo);

		return hentDokumentBytes(journalpostId, dokumentInfoId);
	}

	@AccessControl(attributes = {
			@AccessControlAttribute(name = ATTR_ACTION_ID, value = READ_OPERATION, type = AttributeType.ACTION),
			@AccessControlAttribute(name = ATTR_RESOURCE_TARGET, value = JOURNALPOST_DOCUMENT, type = AttributeType.RESOURCE),
			@AccessControlAttribute(name = ATTR_RESOURCE_ID, value = JOURNALPOST_DOCUMENT, type = AttributeType.RESOURCE),
			@AccessControlAttribute(name = ATTR_ENVIRONMENT_RECIEVER, value = EXTERNAL, type = AttributeType.SUBJECT)
			// Denne burde vært laget med type=environment, men en bug i
			// modig-security gjør at "environment"-variabler aldri blir sendt
			// inn som en del av XACML-requesten
	})
	public List<InnsynJournalpostTo> hentMineTilgjengeligeJournalpostListe(HentJournalpostListeToRequest request) {
		List<Journalpost> journalposts = hentMinTilgjengeligeJournalpostListeService
				.hentMineTilgjengeligeJournalposter(request);
		List<InnsynJournalpostTo> innsynJournalposts = innsynJournalposts(journalposts);

		for (InnsynJournalpostTo innsynJournalpost : innsynJournalposts) {
			decideAvsenderMottaker(innsynJournalpost);
			if (request.isMerkInnsynDokument()) {
				decideInnsynDokumentInfo(innsynJournalpost);
			}
		}
		return innsynJournalposts;
	}

	public InnsynJournalpostTo identifiserJournalpost(IdentifiserJournalpostToRequest request)
	throws JournalpostNotSupportedException, JournalpostIkkeFunnetException, UgyldigInputException, JournalpostIkkeInngaaendeException {
		Journalpost journalpost = identifiserJournalpostService
				.identifiserJournalpost(request);
		InnsynJournalpostTo innsynJournalpost = new InnsynJournalpostTo(journalpost);
		decideInnsynDokumentInfo(innsynJournalpost);

		return innsynJournalpost;
	}


	private void decideAvsenderMottaker(InnsynJournalpostTo innsynJournalpost) {
		try {
			if (!isInnsendtByBruker(innsynJournalpost.getJournalpost())) {
				innsynJournalpost.setAvsenderMottaker(InnsynJournalpostTo.AvsenderMottaker.NEI);
			}
		} catch (SecurityTechnicalException e) {
			if(log.isDebugEnabled()) {
				log.debug(e.toString());
			}
			innsynJournalpost.setAvsenderMottaker(InnsynJournalpostTo.AvsenderMottaker.KAN_IKKE_AVGJOERES);
		}
	}

	private void verifyAvsenderMottakerId(Journalpost journalpost, Long dokumentInfoId) {
		if (JournalpostTypeCode.N != journalpost.getJournalposttype()) {
			if (journalpost.getAvsenderMottakerId() == null || !isInnsendtByBruker(journalpost)) {
				throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
						dokumentInfoId,
						singletonMap("Journalpost.AvsenderMottakerId", journalpost.getAvsenderMottakerId()));
			}
		}
	}

	private boolean isInnsendtByBruker(Journalpost journalpost) {
		String loggedOnFnr = SubjectHandler.getSubjectHandler().getUid();
		return loggedOnFnr.equals(journalpost.getAvsenderMottakerId()) ||
				matchesHistoricalFnr(loggedOnFnr, journalpost);
	}

	private boolean matchesHistoricalFnr(String fnr, Journalpost journalpostTomatch) {
		HentAktoerIdForIdentResponseTo responseTo;
		try {
			responseTo = aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(fnr));
		} catch (PersonIkkeFunnetException e) {
			throw new SecurityTechnicalException("Kan ikke utføre tilgangskontroll for pålogget bruker med fnr=" + fnr + " " +
					"for journalpost med journalpostId=" + journalpostTomatch.getJournalpostId(), e);
		}

		for (IdentDetaljerTo identDetaljerTo : responseTo.getHistoriskeIdenter()) {
			if (identDetaljerTo.getFnr().equals(journalpostTomatch.getAvsenderMottakerId())) {
				return true;
			}
		}
		return false;
	}

	private void verifyJournalstatus(Journalpost journalpost, Long dokumentInfoId) {
		if (!(journalpost.hasEndeligJournalforingStatus() || JournalStatusCode.E.equals(journalpost.getJournalstatus()))) {
			throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
					dokumentInfoId,
					singletonMap("Journalpost.journalStatus", journalpost.getJournalstatus()));
		}
	}

	private void verifyCreatedAndJournalDate(Journalpost journalpost, Long dokumentInfoId) {
		if (isBeforeEarlieastAllowedDate(journalpost.getChangeStamp().getCreatedDate())
				|| isBeforeEarlieastAllowedDate(journalpost.getJournalDato())) {
			Map<String, Date> attributeMap = Maps.newHashMap();
			attributeMap.put("Journalpost.JournalDato", journalpost.getJournalDato());
			attributeMap.put("Journalpost.ChangeStamp.CreatedDate", journalpost.getChangeStamp().getCreatedDate());
			throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
					dokumentInfoId,
					attributeMap);
		}
	}

	private boolean isBeforeEarlieastAllowedDate(Date theDate) {
		if(theDate == null) {
			return false;
		}
		return theDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(earliestAllowedDate);
	}

	private void verifySaksrelasjon(Journalpost journalpost, Long dokumentInfoId) {
		if (journalpost.getSaksrelasjon() == null) {
			throw new IllegalStateException("Journalpost med journalpostId=" + journalpost.getJournalpostId() +
					" er ferdigstilt, men mangler saksrelasjon.");
		}

		if (BooleanUtils.isTrue(journalpost.getSaksrelasjon().getFeilregistrert())) {
			throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
					dokumentInfoId,
					singletonMap("Journalpost.Saksrelasjon.Feilregistrert", journalpost.getSaksrelasjon().getFeilregistrert()));
		}
	}

	private void verifyFagomrade(Journalpost journalpost, Long dokumentInfoId) {
		if (journalpost.getFagomrade() == FagomradeCode.KTR) {
			throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
					dokumentInfoId,
					singletonMap("Journalpost.Fagomrade", journalpost.getFagomrade()));
		}
	}

	private void verifyMottakskanal(Journalpost journalpost, Long dokumentInfoId) {
		if (journalpost.getMottakskanal() == MottaksKanalCode.SKAN_PEN ||
				journalpost.getMottakskanal() == MottaksKanalCode.SKAN_NETS) {
			throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
					dokumentInfoId,
					singletonMap("Journalpost.Mottakskanal", journalpost.getMottakskanal()));
		}
	}

	private void verifyIfNotat(Journalpost journalpost, DokumentInfo dokumentInfo) {
		if (journalpost.getJournalposttype() == JournalpostTypeCode.N) {
			if (dokumentInfo.getKategori() != DokumentKategoriCode.FORVALTNINGSNOTAT) {
				Map<String, Object> attributeMap = Maps.newHashMap();
				attributeMap.put("Journalpost.Journalposttype", journalpost.getJournalposttype());
				attributeMap.put("DokumentInfo.Kategori", dokumentInfo.getKategori());
				throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
						dokumentInfo.getDokumentInfoId(),
						attributeMap);
			}
			if (BooleanUtils.isTrue(dokumentInfo.getOrganInternt())) {
				Map<String, Object> attributeMap = Maps.newHashMap();
				attributeMap.put("Journalpost.Journalposttype", journalpost.getJournalposttype());
				attributeMap.put("DokumentInfo.OrganInternt", dokumentInfo.getOrganInternt());
				throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
						dokumentInfo.getDokumentInfoId(),
						attributeMap);
			}
		}
	}

	private void verifyDokumentstatus(Journalpost journalpost, DokumentInfo dokumentInfo) {
		if (journalpost.getJournalposttype() == JournalpostTypeCode.U ||
				journalpost.getJournalposttype() == JournalpostTypeCode.N) {
			if (dokumentInfo.getDokumentstatus() != DokumentStatusCode.FERDIGSTILT) {
				Map<String, Object> attributeMap = Maps.newHashMap();
				attributeMap.put("Journalpost.Journalposttype", journalpost.getJournalposttype());
				attributeMap.put("DokumentInfo.Dokumentstatus", dokumentInfo.getDokumentstatus());
				throw new SecurityLimitationAttributeException(journalpost.getJournalpostId(),
						dokumentInfo.getDokumentInfoId(),
						attributeMap);
			}
		}
	}

	private void verifyFildetaljer(Long journalpostId, DokumentInfo dokumentInfo) throws DocumentNotFoundException {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		if (filDetaljer == null) {
			throw new DocumentNotFoundException("DokumentInfo med dokumentinfoId=" + dokumentInfo.getDokumentInfoId()
					+ " på Journalpost med journalpostId=" + journalpostId
					+ " har ikke en fildetaljer med VariantFormat=ARKIV");
		}

		if (StringUtils.isNotEmpty(filDetaljer.getOnDemandId()) && filDetaljer.getOnDemandInstans() != null) {
			throw new SecurityLimitationAttributeException(journalpostId,
					dokumentInfo.getDokumentInfoId(),
					singletonMap("DokumentInfo.Fildetaljer.OnDemandId", filDetaljer.getOnDemandId()));
		}
	}

	private void verifyInnskrenketPartsinnsyn(Long journalpostId, DokumentInfo dokumentInfo) {
		if (BooleanUtils.isTrue(dokumentInfo.getInnskrenketPartsinnsyn())) {
			throw new SecurityLimitationAttributeException(journalpostId,
					dokumentInfo.getDokumentInfoId(),
					singletonMap("DokumentInfo.InnskrenketPartsinnsyn", dokumentInfo.getInnskrenketPartsinnsyn()));
		}
	}

	private byte[] hentDokumentBytes(Long journalpostId, Long dokumentInfoId) throws DocumentNotFoundException {
		return tjoark054HentDokumentService.hentDokument(new HentDokumentRequestTo(journalpostId,
				dokumentInfoId, VariantFormatCode.ARKIV));
	}

	void setEarliestAllowedDate(LocalDate earliestAllowedDate) {
		if (earliestAllowedDate == null) {
			this.earliestAllowedDate = null;
		} else {
			this.earliestAllowedDate = LocalDate.from(earliestAllowedDate);
		}
	}

	private void decideInnsynDokumentInfo(InnsynJournalpostTo innsynJournalpost) {
		Journalpost journalpost = innsynJournalpost.getJournalpost();
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {

			innsynJournalpost.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.JA, dokumentInfo.getDokumentInfoId());

			if (journalpost.getMottakskanal() == MottaksKanalCode.NAV_NO
					|| journalpost.getJournalposttype() != JournalpostTypeCode.N) {
				boolean innsendtByBruker;
				try {
					innsendtByBruker = isInnsendtByBruker(journalpost);
				} catch (SecurityTechnicalException e) {
					if(log.isDebugEnabled()) {
						log.debug(e.toString());
					}
					innsynJournalpost.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.KAN_IKKE_AVGJOERES,
							dokumentInfo.getDokumentInfoId());
					continue;
				}

				decideDokumentInnsynValue(innsynJournalpost, journalpost, dokumentInfo, innsendtByBruker);

				try {
					verifyMottakskanal(journalpost, dokumentInfo.getDokumentInfoId());
					verifyFildetaljer(journalpost.getJournalpostId(), dokumentInfo);
					verifyInnskrenketPartsinnsyn(journalpost.getJournalpostId(), dokumentInfo);
				} catch (DocumentNotFoundException | SecurityLimitationAttributeException e) {
					innsynJournalpost.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.NEI,
							dokumentInfo.getDokumentInfoId());
					if(log.isDebugEnabled()) {
						log.debug(e.toString());
					}
				}
			}
		}
	}

	private void decideDokumentInnsynValue(InnsynJournalpostTo innsynJournalpost, Journalpost journalpost,
										   DokumentInfo dokumentInfo, boolean innsendtByBruker) {
		if (!innsendtByBruker) {
			innsynJournalpost.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.NEI,
					dokumentInfo.getDokumentInfoId());
		} else if (MottaksKanalCode.NAV_NO == journalpost.getMottakskanal()) {
			innsynJournalpost.putDokumentInnsyn(InnsynJournalpostTo.DokumentInnsyn.JA,
					dokumentInfo.getDokumentInfoId());
		}
	}

}
