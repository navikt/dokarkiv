package no.nav.dokarkiv.core.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FaktiskDistribusjonskanalCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;

/**
 * Domain entity that represents journalposts.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Magnus Skuland, Sirius IT
 * @author Stian Landsnes, Sirius IT
 * @author Rune Romundstad, Sirius IT
 * @author Thao Thanh Nguyen, Visma Sirius
 * @author Lamisi Gurah Blackman, Accenture
 */
@Entity
@Table(name = "T_JOURNALPOST")
@Builder(toBuilder = true)
@AllArgsConstructor
public class Journalpost extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = 8744278542606158366L;
	private static final List<JournalStatusCode> ENDELIG_JOURNALFOERING_STATUS = Arrays.asList(J, JournalStatusCode.FS, JournalStatusCode.FL);
	private static final List<JournalStatusCode> MIDLERTIDIG_INNGAAENDE_JOURNALFOERING_STATUS = Arrays.asList(MO, M, UB);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "journalpost_seq")
	@GenericGenerator(name = "journalpost_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = "T_JOURNALPOST_SEQ"),
			@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "journalpost_id", nullable = false)
	private Long journalpostId;

	@Column(name = "journalf_enhet")
	private String journalForendeEnhetId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_journal")
	private Date journalDato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_sendt_print")
	private Date sendtPrintDato;

	@Column(name = "antall_retur")
	private Integer antallRetur;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_avs_retur")
	private Date avsendtReturDato;

	@Column(name = "innhold")
	private String innhold;

	@Column(name = "krav_type")
	private String kravtype;

	@Column(name = "merknad")
	private String merknad;

	@Column(name = "fordeling")
	private String fordeling;

	@Column(name = "original_bestilt")
	private Boolean originaltBestilt;

	@Column(name = "opprettet_av_navn")
	private String opprettetAvNavn;

	@Column(name = "endret_av_navn")
	private String endretAvNavn;

	@Column(name = "kanal_referanse_id")
	private String kanalReferanseId;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fagomrade", nullable = false)
	private FagomradeCode fagomrade;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_journal_s", nullable = false)
	private JournalStatusCode journalstatus;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_dokument")
	private Date dokumentDato;

	@Column(name = "avsend_mottaker", length = 200)
	private String avsenderMottaker;

	@Column(name = "avsend_mottak_id", length = 50)
	private String avsenderMottakerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_avsend_mottak_id_t", length = 20)
	private AvsenderMottakerIdTypeCode avsenderMottakerIdType;

	@Column(name = "journalfort_av_navn")
	private String journalfortAvNavn;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_mottatt")
	private Date mottattDato;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_mottaks_kanal")
	private MottaksKanalCode mottakskanal;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_utsendings_kanal")
	private UtsendingsKanalCode utsendingskanal;

	@Column(name = "land")
	private String land;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fakt_dis_kanal")
	private FaktiskDistribusjonskanalCode faktiskDistribusjonskanal;

	@Column(name = "elektronisk_distr")
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean elektroniskDistribusjon;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_ekspedert")
	private Date ekspedertDato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_lest")
	private Date lestDato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "mottatt_adressat")
	private Date mottattAdressatDato;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_journalpost_t", nullable = false)
	private JournalpostTypeCode journalposttype;

	@Column(name = "signatur")
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean signatur;

	@Column(name = "k_behandlingstema")
	private String behandlingstema;

	@Column(name = "k_skjerming_type")
	@Enumerated(EnumType.STRING)
	private SkjermingTypeCode skjermingType;

	@Column(name = "k_innsyn", length = 50)
	@Enumerated(EnumType.STRING)
	private InnsynCode innsyn;

	@OneToMany
	@JoinColumn(name = "journalpost_id", nullable = false)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private final Set<Bruker> brukere = new HashSet<>();

	@OneToOne(mappedBy = "journalpost", fetch = FetchType.LAZY)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private Saksrelasjon saksrelasjon;

	@OneToMany(mappedBy = "journalpost")
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private final Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner = new HashSet<>();

	@ElementCollection
	@JoinTable(name = "t_jp_tillegg", joinColumns = @JoinColumn(name = "journalpost_id", nullable = false))
	@MapKeyColumn(name = "nokkel")
	@Column(name = "verdi", nullable = false)
	private Map<String, String> tilleggsopplysninger = new HashMap<>();

	@OneToMany
	@JoinColumn(name = "journalpost_id", nullable = false)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private final Set<Kryssreferanse> kryssreferanser = new HashSet<>();

	/**
	 * Default constructor.
	 */
	public Journalpost() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param journalpostId DB-id for the instance.
	 * @param version DB-version for the instance.
	 */
	public Journalpost(Long journalpostId, long version) {
		this.journalpostId = journalpostId;
		setVersion(version);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getJournalpostId();
	}

	/**
	 * Checks if the Journalpost's status is lenient, i.e. if not all fields
	 * must be set.
	 *
	 * @return True if the journalstatus is lenient, false otherwise.
	 */
	public boolean hasLenientStatus() {
		List<JournalStatusCode> lenientStatuses = Arrays.asList(MO, M, UB, U, OD);
		return lenientStatuses.contains(journalstatus);
	}

	/**
	 * Checks if the Journalpost's status is one that indicates an inngaende
	 * journalpost.
	 *
	 * @return true if status is inngaende, false otherwise.
	 */
	public boolean hasInngaendeStatus() {
		List<JournalStatusCode> inngaendeStatuses = Arrays.asList(MO, M, UB, U, J);
		return inngaendeStatuses.contains(journalstatus);
	}

	/**
	 * Verify that all mandatory fields are set. Some fields are only required
	 * given certain journalStatuses and journalpostTypes.
	 */
	public void verifyMandatoryFields() {
		verifyAlwaysRequiredFields();
		verifyFieldsForNonLenientStatuses();
		verifyJournalforendeEnhetIdForJournalfortJournalforing();
		verifyFieldsForEndeligJournalforing();
	}

	/**
	 * Verify that all mandatory fields are set except for JournalforendeEnhetId. Some fields are only required
	 * given certain journalStatuses and journalpostTypes.
	 */
	public void verifyMandatoryFieldsSkipJournalforendeEnhetId() {
		verifyAlwaysRequiredFields();
		verifyFieldsForNonLenientStatuses();
		verifyFieldsForEndeligJournalforing();
	}

	/**
	 * Verify that all mandatory fields are set except endretAvNavn before ferdigstilling.
	 * Some fields are only required given certain journalStatuses and journalpostTypes.
	 */
	public void verifyMandatoryFieldsNotEndretAvNavn() {
		verifyMinimumAlwaysRequiredFields();
		verifyFieldsForNonLenientStatuses();
		verifyJournalforendeEnhetIdForJournalfortJournalforing();
		verifyFieldsForEndeligJournalforing();
	}

	private void verifyAlwaysRequiredFields() {
		verifyMinimumAlwaysRequiredFields();
		if (hasId()) {
			verifyStringNotBlank(endretAvNavn, "endretAvNavn");
		} else {
			verifyStringNotBlank(opprettetAvNavn, "opprettetAvNavn");
		}
	}

	private void verifyMinimumAlwaysRequiredFields() {
		verifyFieldNotNull(journalposttype, "journalposttype");
		verifyFieldNotNull(journalstatus, "journalstatus");
	}

	private void verifyFieldsForNonLenientStatuses() {
		if (!hasLenientStatus()) {
			verifyFieldNotNull(fagomrade, "fagomrade");
			verifyStringNotBlank(innhold, "innhold");
			if (journalposttype != JournalpostTypeCode.N) {
				verifyStringNotBlank(avsenderMottaker, "avsenderMottaker");
			}
			verifyFieldNotNull(saksrelasjon, "saksrelasjon");
			verifyBrukere();
		}
	}

	private void verifyBrukere() {
		if (brukere.isEmpty()) {
			throw new InvalidArgumentException("Journalpost must have at least one Bruker");
		}
	}

	private void verifyJournalforendeEnhetIdForJournalfortJournalforing() {
		if (journalstatus == J) {
			verifyStringNotBlank(journalForendeEnhetId, "journalForendeEnhetId");
		}
	}

	public void verifyJournalforendeEnhetIdForMidlertidigJournalforing() {
		if (journalstatus == M) {
			verifyStringNotBlank(journalForendeEnhetId, "journalForendeEnhetId");
		}
	}

	private void verifyFieldsForEndeligJournalforing() {
		if (hasEndeligJournalforingStatus() && getJournalpostDokumentInfoRelasjoner().isEmpty()) {
			throw new InvalidArgumentException("Journalpost must have at least one DokumentInfoRelasjon");
		}
	}

	/**
	 * Checks that none of this Journalposts documents contain duplicate
	 * varianter.
	 */
	public void verifyNoDokumentVariantDuplicates() {
		for (DokumentInfo dokumentInfo : findAllDokumentInfos()) {
			dokumentInfo.verifyNoVariantDuplicates();
		}
	}

	/**
	 * Checks that all DokumentInfoRelasjoner points to different DokumentInfos.
	 */
	public void verifyUniqueDokumentInfoRelasjoner() {
		Map<Long, Integer> dokumentInfoIdsCount = getDokumentInfoIdsCount();
		for (Entry<Long, Integer> dokumentInfoIdCount : dokumentInfoIdsCount.entrySet()) {
			if (dokumentInfoIdCount.getValue() > 1) {
				throw new InvalidJournalpostStructureException(this.getClass().getSimpleName() + " has "
						+ dokumentInfoIdCount.getValue() + " DokumentInfoRelasjoner pointing to the same "
						+ DokumentInfo.class.getSimpleName() + " with Id " + dokumentInfoIdCount.getKey());
			}
		}
	}

	private Map<Long, Integer> getDokumentInfoIdsCount() {
		Map<Long, Integer> dokumentInfoIdsCount = new HashMap<>();
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : getJournalpostDokumentInfoRelasjoner()) {
			DokumentInfo dokumentInfo = dokumentInfoRelasjon.getDokumentInfo();
			if (dokumentInfo != null && dokumentInfo.getDokumentInfoId() != null) {
				Integer count = dokumentInfoIdsCount.get(dokumentInfo.getDokumentInfoId());
				if (count == null) {
					dokumentInfoIdsCount.put(dokumentInfo.getDokumentInfoId(), 1);
				} else {
					dokumentInfoIdsCount.put(dokumentInfo.getDokumentInfoId(), count + 1);
				}
			}
		}
		return dokumentInfoIdsCount;
	}

	/**
	 * Checks that the journalpost structure is correct given a status that
	 * indicates endelig journalforing.
	 */
	public void verifyStructureForEndeligJournalforing() {
		if (!hasEndeligJournalforingStatus() && journalstatus != JournalStatusCode.E) {
			return;
		}
		verifyOnlyOneHoveddokument();
		verifyArkivVariantOfAllDocuments();
		verifyNoDokumentInfosUnderRedigering();
	}

	/**
	 * Checks if the Journalpost's status indicates endelig journalforing.
	 *
	 * @return true if the status means endelig journalforing, false otherwise.
	 */
	public boolean hasEndeligJournalforingStatus() {
		return ENDELIG_JOURNALFOERING_STATUS.contains(journalstatus);
	}

	/**
	 * Checks if the Journalpost status is midlertidig journalfï¿½rt for inngï¿½ende.
	 *
	 * @return true if the status is midlertidig for inngï¿½ende, false otherwise.
	 */
	public boolean hasMidlertidigInngaaendeJournalforingStatus() {
		return MIDLERTIDIG_INNGAAENDE_JOURNALFOERING_STATUS.contains(journalstatus);
	}

	/**
	 * Checks if the Journalpost status is utgï¿½tt.
	 *
	 * @return true if the status is midlertidig for inngï¿½ende, false otherwise.
	 */
	public boolean hasUtgaattJournalforingStatus() {
		return U == journalstatus;
	}

	/**
	 * Checks if JournalStatusCode is FS
	 *
	 * @return true is JournalStatusCode is FS
	 */
	public boolean hasFerdigOgSentralPrintJournalforingStatus() {
		return journalstatus == JournalStatusCode.FS;
	}

	public void verifyOnlyOneHoveddokument() {
		int hovedDokumentCount = getTilknyttetSomCount(getJournalpostDokumentInfoRelasjoner(),
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT);

		if (hovedDokumentCount == 0) {
			throwExceptionForFailedVerificationForEndeligJournalforing(
					"Journalpost must contain a hoveddokument");
		} else if (hovedDokumentCount > 1) {
			throwExceptionForFailedVerificationForEndeligJournalforing(
					"Journalpost cannot contain more than one hoveddokument");
		}
	}

	private int getTilknyttetSomCount(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjoner,
									  TilknyttetJournalpostSomCode codeToCount) {
		int count = 0;
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : dokumentInfoRelasjoner) {
			if (dokumentInfoRelasjon.getTilknyttetJournalpostSom() == codeToCount) {
				count++;
			}
		}
		return count;
	}

	public void verifyArkivVariantOfAllDocuments() {
		for (DokumentInfo dokumentInfo : findAllDokumentInfos()) {
			if (!dokumentInfo.hasArkivFormat()) {
				throwExceptionForFailedVerificationForEndeligJournalforing(
						"All the Journalpost's DokumentInfos must contain an arkiv variant");
			}
		}
	}

	public void verifyNoDokumentInfosUnderRedigering() {
		for (DokumentInfo dokumentInfo : findAllDokumentInfos()) {
			if (dokumentInfo.isUnderRedigering()) {
				throwExceptionForFailedVerificationForEndeligJournalforing(
						"Journalpost cannot contain DokumentInfos with status 'under redigering'");
			}
		}
	}

	private void throwExceptionForFailedVerificationForEndeligJournalforing(String message) {
		throw new InvalidJournalpostStructureException(message + " when endelig journalforing");
	}

	/**
	 * Finds a FilDetaljer that belongs to this Journalpost by filUuid.
	 *
	 * @param filUuid The UUID.
	 * @return The FilDetaljer.
	 */
	public FilDetaljer findFilDetaljerByFilUuid(String filUuid) {
		for (JournalpostDokumentInfoRelasjon relasjon : getJournalpostDokumentInfoRelasjoner()) {
			if (relasjon.getDokumentInfo() != null && relasjon.getDokumentInfo().findFilDetaljerByFilUuid(filUuid) != null) {
				return relasjon.getDokumentInfo().findFilDetaljerByFilUuid(filUuid);
			}
		}
		return null;
	}

	/**
	 * Finds a FilDetaljer that belongs to this Journalpost by FilDetaljerId.
	 *
	 * @param filDetaljerId The ID
	 * @return The FilDetaljer with the given Id
	 */
	public FilDetaljer findFilDetaljerByFilDetaljerId(Long filDetaljerId) {
		for (JournalpostDokumentInfoRelasjon relasjon : getJournalpostDokumentInfoRelasjoner()) {
			if (relasjon.getDokumentInfo() != null && relasjon.getDokumentInfo().findFilDetaljerById(filDetaljerId) != null) {
				return relasjon.getDokumentInfo().findFilDetaljerById(filDetaljerId);
			}
		}
		return null;
	}

	/**
	 * Finds all FilDetaljer that belongs to this Journalpost.
	 *
	 * @return A List with all FilDetaljer
	 */
	public List<FilDetaljer> findAllFilDetaljer() {
		List<FilDetaljer> allFilDetaljer = new ArrayList<>();
		for (JournalpostDokumentInfoRelasjon relasjon : getJournalpostDokumentInfoRelasjoner()) {
			if (relasjon.getDokumentInfo() != null) {
				allFilDetaljer.addAll(relasjon.getDokumentInfo().getFildetaljerListe());
			}
		}
		return allFilDetaljer;
	}

	/**
	 * Finds a KryssReferanse by Id.
	 *
	 * @param kryssReferanseId The Id.
	 * @return The KryssReferanse with kryssReferanseId.
	 */
	public Kryssreferanse findKryssreferanseById(final Long kryssReferanseId) {
		return kryssreferanser.stream()
				.filter(kryssreferanse -> kryssReferanseId.equals(kryssreferanse.getId()))
				.findAny()
				.orElse(null);
	}

	/**
	 * Finds a Bruker by Id (primary key)
	 *
	 * @param brukerInfoId The Id.
	 * @return The Bruker.
	 */
	public Bruker findBrukerById(final Long brukerInfoId) {
		return brukere.stream().filter(bruker -> brukerInfoId.equals(bruker.getId())).findAny().orElse(null);
	}

	/**
	 * Find a bruker by brukerId (e.g. fnr)
	 *
	 * @param brukerId The brukerId
	 * @return The Bruker.
	 */
	public Bruker findBrukerByBrukerId(final String brukerId) {
		return brukere.stream().filter(bruker -> brukerId.equals(bruker.getBrukerId())).findAny().orElse(null);
	}

	/**
	 * Finds a JournalpostDokumentInfoRelasjon by Id.
	 *
	 * @param journalpostDokumentInfoRelasjonId The Id.
	 * @return The JournalpostDokumentInfoRelasjon.
	 */
	public JournalpostDokumentInfoRelasjon findDokumentInfoRelasjonById(final Long journalpostDokumentInfoRelasjonId) {
		return getJournalpostDokumentInfoRelasjoner().stream()
				.filter(journalpostDokumentInfoRelasjon -> journalpostDokumentInfoRelasjonId.equals(journalpostDokumentInfoRelasjon
						.getId())).findAny().orElse(null);
	}

	/**
	 * Finds DokumentInfos by dokumentStatusCode
	 *
	 * @param dokumentStatusCode the code
	 * @return An Iterable of DokumentInfo
	 */
	public Iterable<DokumentInfo> findDokumentInfoByDokumentStatus(final DokumentStatusCode dokumentStatusCode) {
		return findAllDokumentInfos().stream()
				.filter(dokumentInfo -> dokumentStatusCode.equals(dokumentInfo.getDokumentstatus()))
				.collect(Collectors.toList());
	}

	/**
	 * Finds a JournalpostDokumentInfoRelasjon by TilknyttetJournalpostSomCode.
	 *
	 * @param tilknyttetJournalpostSom The code.
	 * @return The JournalpostDokumentInfoRelasjon.
	 */
	public Set<JournalpostDokumentInfoRelasjon> findDokumentInfoRelasjonByTilknyttetJournalpostSom(
			final TilknyttetJournalpostSomCode tilknyttetJournalpostSom) {
		return getJournalpostDokumentInfoRelasjoner().stream()
				.filter(journalpostDokumentInfoRelasjon -> tilknyttetJournalpostSom.equals(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom()))
				.collect(Collectors.toSet());
	}

	/**
	 * Finds a JournalpostDokumentInfoRelasjon that has
	 * TilknyttetJournalpostSomCode = HOVEDDOKUMENT.
	 *
	 * @return The JournalpostDokumentInfoRelasjon.
	 */
	public JournalpostDokumentInfoRelasjon findHoveddokumentDokumentInfoRelasjon() {
		Set<JournalpostDokumentInfoRelasjon> hoveddokumentList = findDokumentInfoRelasjonByTilknyttetJournalpostSom(
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT);
		return hoveddokumentList.isEmpty() ? null : hoveddokumentList.iterator().next();
	}

	/**
	 * Finds all DokumentInfos that belongs to this Journalpost.
	 *
	 * @return A List with all DokumentInfos
	 */
	public List<DokumentInfo> findAllDokumentInfos() {
		List<DokumentInfo> allDokumentInfos = new ArrayList<>();
		for (JournalpostDokumentInfoRelasjon relasjon : getJournalpostDokumentInfoRelasjoner()) {
			if (relasjon.getDokumentInfo() != null) {
				allDokumentInfos.add(relasjon.getDokumentInfo());
			}
		}
		return allDokumentInfos;
	}

	/**
	 * Finds a DokumentInfo that belongs to this Journalpost by dokumentInfoId.
	 *
	 * @param dokumentInfoId The Id.
	 * @return The DokumentInfo.
	 */
	public DokumentInfo findDokumentInfoById(final Long dokumentInfoId) {
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : getJournalpostDokumentInfoRelasjoner()) {
			DokumentInfo dokumentInfo = dokumentInfoRelasjon.getDokumentInfo();
			if (dokumentInfo != null && dokumentInfoId.equals(dokumentInfo.getDokumentInfoId())) {
				return dokumentInfo;
			}
		}
		return null;
	}

	/**
	 * Check if this Journalpost has not yet been persisted.
	 *
	 * @return true if this Journalpost is new, otherwise false.
	 */
	public boolean isNew() {
		return hasId();
	}

	/**
	 * Checks if this Journalpost's type is inngaende.
	 *
	 * @return true if inngaende, otherwise false
	 */
	public boolean isInngaende() {
		return journalposttype == JournalpostTypeCode.I;
	}

	/**
	 * Checks if this Journalpost's type is notat.
	 *
	 * @return true if notat, otherwise false
	 */
	public boolean isNotat() {
		return journalposttype == JournalpostTypeCode.N;
	}

	/**
	 * Checks if this Journalpost's type is utgaende.
	 *
	 * @return true if utgaende, otherwise false
	 */
	public boolean isUtgaende() {
		return journalposttype == JournalpostTypeCode.U;
	}

	/**
	 * Getter for the antallRetur property.
	 *
	 * @return the antallRetur
	 */
	public Integer getAntallRetur() {
		return antallRetur;
	}

	/**
	 * Setter for the antallRetur property.
	 *
	 * @param antallRetur the antallRetur to set
	 */
	public void setAntallRetur(Integer antallRetur) {
		this.antallRetur = antallRetur;
	}

	/**
	 * Getter for the datoAvsRetur property.
	 *
	 * @return the datoAvsRetur
	 */
	public Date getAvsendtReturDato() {
		if (avsendtReturDato != null) {
			return new Date(avsendtReturDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the datoAvsRetur property.
	 *
	 * @param avsendtReturDato the datoAvsRetur to set
	 */
	public void setAvsendtReturDato(Date avsendtReturDato) {
		if (avsendtReturDato != null) {
			this.avsendtReturDato = new Date(avsendtReturDato.getTime());
		} else {
			this.avsendtReturDato = null;
		}
	}

	/**
	 * Getter for the datoSendtPrint property.
	 *
	 * @return the datoSendtPrint
	 */
	public Date getSendtPrintDato() {
		if (sendtPrintDato != null) {
			return new Date(sendtPrintDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the datoSendtPrint property.
	 *
	 * @param sendtPrintDato the datoSendtPrint to set
	 */
	public void setSendtPrintDato(Date sendtPrintDato) {
		if (sendtPrintDato != null) {
			this.sendtPrintDato = new Date(sendtPrintDato.getTime());
		} else {
			this.sendtPrintDato = null;
		}
	}

	/**
	 * Getter for the journalDato property.
	 *
	 * @return the journalDato
	 */
	public Date getJournalDato() {
		if (journalDato != null) {
			return new Date(journalDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the journalDato property.
	 *
	 * @param journalDato the journalDato to set
	 */
	public void setJournalDato(Date journalDato) {
		if (journalDato != null) {
			this.journalDato = new Date(journalDato.getTime());
		} else {
			this.journalDato = null;
		}
	}

	/**
	 * Getter for the kanalReferanseId property.
	 *
	 * @return the kanalReferanseId
	 */
	public String getKanalReferanseId() {
		return kanalReferanseId;
	}

	/**
	 * Setter for the kanalReferanseId property.
	 *
	 * @param kanalReferanseId the kanalReferanseId to set
	 */
	public void setKanalReferanseId(String kanalReferanseId) {
		this.kanalReferanseId = kanalReferanseId;
	}

	/**
	 * Getter for the endretAvNavn property.
	 *
	 * @return the endretAvNavn
	 */
	public String getEndretAvNavn() {
		return endretAvNavn;
	}

	/**
	 * Setter for the endretAvNavn property.
	 *
	 * @param endretAvNavn the endretAvNavn to set
	 */
	public void setEndretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
	}

	/**
	 * Getter for the fagomrade property.
	 *
	 * @return the fagomrade
	 */
	public FagomradeCode getFagomrade() {
		return fagomrade;
	}

	/**
	 * Setter for the fagomrade property.
	 *
	 * @param fagomrade the fagomrade to set
	 */
	public void setFagomrade(FagomradeCode fagomrade) {
		this.fagomrade = fagomrade;
	}

	/**
	 * Getter for the fordeling property.
	 *
	 * @return the fordeling
	 */
	public String getFordeling() {
		return fordeling;
	}

	/**
	 * Setter for the fordeling property.
	 *
	 * @param fordeling the fordeling to set
	 */
	public void setFordeling(String fordeling) {
		this.fordeling = fordeling;
	}

	/**
	 * Getter for the innhold property.
	 *
	 * @return the innhold
	 */
	public String getInnhold() {
		return innhold;
	}

	/**
	 * Setter for the innhold property.
	 *
	 * @param innhold the innhold to set
	 */
	public void setInnhold(String innhold) {
		this.innhold = innhold;
	}

	/**
	 * Getter for the journalForendeEnhetId property.
	 *
	 * @return the journalForendeEnhetId
	 */
	public String getJournalForendeEnhetId() {
		return journalForendeEnhetId;
	}

	/**
	 * Setter for the journalForendeEnhetId property.
	 *
	 * @param journalForendeEnhetId the journalForendeEnhetId to set
	 */
	public void setJournalForendeEnhetId(String journalForendeEnhetId) {
		this.journalForendeEnhetId = journalForendeEnhetId;
	}

	/**
	 * Getter for the journalstatus property.
	 *
	 * @return the journalstatus
	 */
	public JournalStatusCode getJournalstatus() {
		return journalstatus;
	}

	/**
	 * Setter for the journalstatus property.
	 *
	 * @param journalstatus the journalstatus to set
	 */
	public void setJournalstatus(JournalStatusCode journalstatus) {
		this.journalstatus = journalstatus;
	}

	/**
	 * Getter for the kravType property.
	 *
	 * @return the kravType
	 */
	public String getKravtype() {
		return kravtype;
	}

	/**
	 * Setter for the kravType property.
	 *
	 * @param kravtype the kravType to set
	 */
	public void setKravtype(String kravtype) {
		this.kravtype = kravtype;
	}

	/**
	 * Getter for the merknad property.
	 *
	 * @return the merknad
	 */
	public String getMerknad() {
		return merknad;
	}

	/**
	 * Setter for the merknad property.
	 *
	 * @param merknad the merknad to set
	 */
	public void setMerknad(String merknad) {
		this.merknad = merknad;
	}

	/**
	 * Getter for the opprettetAvNavn property.
	 *
	 * @return the opprettetAvNavn
	 */
	public String getOpprettetAvNavn() {
		return opprettetAvNavn;
	}

	/**
	 * Setter for the opprettetAvNavn property.
	 *
	 * @param opprettetAvNavn the opprettetAvNavn to set
	 */
	public void setOpprettetAvNavn(String opprettetAvNavn) {
		this.opprettetAvNavn = opprettetAvNavn;
	}

	/**
	 * Getter for the originalBestilt property.
	 *
	 * @return the originalBestilt
	 */
	public Boolean getOriginaltBestilt() {
		return originaltBestilt;
	}

	/**
	 * Setter for the originalBestilt property.
	 *
	 * @param originaltBestilt the originalBestilt to set
	 */
	public void setOriginaltBestilt(Boolean originaltBestilt) {
		this.originaltBestilt = originaltBestilt;
	}

	/**
	 * Getter for the journalpostId property.
	 *
	 * @return the journalpostId
	 */
	public Long getJournalpostId() {
		return journalpostId;
	}

	/**
	 * Getter for the brukere property.
	 *
	 * @return the brukere
	 */
	public Set<Bruker> getBrukere() {
		return Collections.unmodifiableSet(brukere);
	}

	/**
	 * Removes a subset from brukere
	 *
	 * @return true if brukere was modified, otherwise false
	 */
	public boolean removeBrukere(Set<Bruker> brukereToRemove) {
		return brukere.removeAll(brukereToRemove);
	}

	/**
	 * Add a Bruker to the Bruker Set.
	 *
	 * @param bruker The Bruker to add,
	 */
	public void addBruker(Bruker bruker) {
		if (bruker != null) {
			brukere.add(bruker);
		}
	}

	/**
	 * Empties the Brukere set
	 */
	public void clearBrukere() {
		brukere.clear();
	}


	/**
	 * Getter for the saksrelasjon property.
	 *
	 * @return the saksrelasjon
	 */
	public Saksrelasjon getSaksrelasjon() {
		return saksrelasjon;
	}

	/**
	 * Whether this Journalpost is feilregistrert or not. Checks the attached sak, if any exists.
	 *
	 * @return true if Journalpost is feilregistrert, false if it is not or not applicable.
	 */
	public boolean isFeilregistrert() {
		return saksrelasjon != null && saksrelasjon.getFeilregistrert() != null && saksrelasjon.getFeilregistrert();
	}

	/**
	 * Setter for the saksrelasjon property.
	 *
	 * @param saksrelasjon the saksrelasjon to set
	 */
	public void setSaksrelasjon(Saksrelasjon saksrelasjon) {
		this.saksrelasjon = saksrelasjon;
		if (saksrelasjon != null) {
			saksrelasjon.setJournalpost(this);
		}
	}

	/**
	 * Getter for the dokumentDato property.
	 *
	 * @return the dokumentDato
	 */
	public Date getDokumentDato() {
		if (dokumentDato != null) {
			return new Date(dokumentDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the dokumentDato property.
	 *
	 * @param dokumentDato the dokumentDato to set
	 */
	public void setDokumentDato(Date dokumentDato) {
		if (dokumentDato != null) {
			this.dokumentDato = new Date(dokumentDato.getTime());
		} else {
			this.dokumentDato = null;
		}
	}

	/**
	 * Getter for the avsenderMottaker property.
	 *
	 * @return the avsenderMottaker
	 */
	public String getAvsenderMottaker() {
		return avsenderMottaker;
	}

	/**
	 * Setter for the avsenderMottaker property.
	 *
	 * @param avsenderMottaker the avsenderMottaker to set
	 */
	public void setAvsenderMottaker(String avsenderMottaker) {
		this.avsenderMottaker = avsenderMottaker;
	}

	/**
	 * Getter for the avsenderMottakerId property.
	 *
	 * @return the avsenderMottakerId
	 */
	public String getAvsenderMottakerId() {
		return avsenderMottakerId;
	}

	/**
	 * Setter for the avsenderMottakerId property.
	 *
	 * @param avsenderMottakerId the avsenderMottakerId to set
	 */
	public void setAvsenderMottakerId(String avsenderMottakerId) {
		this.avsenderMottakerId = avsenderMottakerId;
	}

	/**
	 * Getter for the avsenderMottakerIdType property.
	 *
	 * @return the avsenderMottakerIdType
	 */
	public AvsenderMottakerIdTypeCode getAvsenderMottakerIdType() {
		return avsenderMottakerIdType;
	}

	/**
	 * Setter for the avsenderMottakerIdType property.
	 *
	 * @param avsenderMottakerIdType the avsenderMottakerIdType to set
	 */
	public void setAvsenderMottakerIdType(AvsenderMottakerIdTypeCode avsenderMottakerIdType) {
		this.avsenderMottakerIdType = avsenderMottakerIdType;
	}

	/**
	 * Getter for the journalfortAvNavn property.
	 *
	 * @return the journalfortAvNavn
	 */
	public String getJournalfortAvNavn() {
		return journalfortAvNavn;
	}

	/**
	 * Setter for the journalfortAvNavn property.
	 *
	 * @param journalfortAvNavn the journalfortAvNavn to set
	 */
	public void setJournalfortAvNavn(String journalfortAvNavn) {
		this.journalfortAvNavn = journalfortAvNavn;
	}

	/**
	 * Getter for the mottattDato property.
	 *
	 * @return the mottattDato
	 */
	public Date getMottattDato() {
		if (mottattDato != null) {
			return new Date(mottattDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the mottattDato property.
	 *
	 * @param mottattDato the mottattDato to set
	 */
	public void setMottattDato(Date mottattDato) {
		if (mottattDato != null) {
			this.mottattDato = new Date(mottattDato.getTime());
		} else {
			this.mottattDato = null;
		}
	}

	/**
	 * Getter for the mottakskanal property.
	 *
	 * @return the mottakskanal
	 */
	public MottaksKanalCode getMottakskanal() {
		return mottakskanal;
	}

	/**
	 * Setter for the mottakskanal property.
	 *
	 * @param mottakskanal the mottakskanal to set
	 */
	public void setMottakskanal(MottaksKanalCode mottakskanal) {
		this.mottakskanal = mottakskanal;
	}

	/**
	 * Getter for the utsendingskanal property.
	 *
	 * @return the utsendingskanal
	 */
	public UtsendingsKanalCode getUtsendingskanal() {
		return utsendingskanal;
	}

	/**
	 * Setter for the utsendingskanal property.
	 *
	 * @param utsendingskanal the utsendingskanal to set
	 */
	public void setUtsendingskanal(UtsendingsKanalCode utsendingskanal) {
		this.utsendingskanal = utsendingskanal;
	}

	/**
	 * Getter for the land property.
	 *
	 * @return the land
	 */
	public String getLand() {
		return land;
	}

	/**
	 * Setter for the land property.
	 *
	 * @param land the land to set
	 */
	public void setLand(String land) {
		this.land = land;
	}

	/**
	 * Getter for the faktiskDistribusjonskanal property.
	 *
	 * @return the faktiskDistribusjonskanal
	 */
	public FaktiskDistribusjonskanalCode getFaktiskDistribusjonskanal() {
		return faktiskDistribusjonskanal;
	}

	/**
	 * Setter for the faktiskDistribusjonskanal property.
	 *
	 * @param faktiskDistribusjonskanal the faktiskDistribusjonskanal to set
	 */
	public void setFaktiskDistribusjonskanal(FaktiskDistribusjonskanalCode faktiskDistribusjonskanal) {
		this.faktiskDistribusjonskanal = faktiskDistribusjonskanal;
	}

	/**
	 * Getter for the elektroniskDistribusjon property.
	 *
	 * @return the elektroniskDistribusjon
	 */
	public Boolean getElektroniskDistribusjon() {
		return elektroniskDistribusjon;
	}

	/**
	 * Setter for the elektroniskDistribusjon property.
	 *
	 * @param elektroniskDistribusjon the elektroniskDistribusjon to set
	 */
	public void setElektroniskDistribusjon(Boolean elektroniskDistribusjon) {
		this.elektroniskDistribusjon = elektroniskDistribusjon;
	}

	/**
	 * Getter for the ekspedertDato property.
	 *
	 * @return the ekspedertDato
	 */
	public Date getEkspedertDato() {
		if (ekspedertDato != null) {
			return new Date(ekspedertDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the ekspedertDato property.
	 *
	 * @param ekspedertDato the ekspedertDato to set
	 */
	public void setEkspedertDato(Date ekspedertDato) {
		if (ekspedertDato != null) {
			this.ekspedertDato = new Date(ekspedertDato.getTime());
		} else {
			this.ekspedertDato = null;
		}
	}

	/**
	 * Getter for the lestDato property.
	 *
	 * @return the lestDato
	 */
	public Date getLestDato() {
		if (lestDato != null) {
			return new Date(lestDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the lestDato property.
	 *
	 * @param lestDato the lestDato to set
	 */
	public void setLestDato(Date lestDato) {
		if (lestDato != null) {
			this.lestDato = new Date(lestDato.getTime());
		} else {
			this.lestDato = null;
		}
	}

	/**
	 * Getter for the mottattAdressatDato property.
	 *
	 * @return the mottattAdressatDato
	 */
	public Date getMottattAdressatDato() {
		if (mottattAdressatDato != null) {
			return new Date(mottattAdressatDato.getTime());
		}
		return null;
	}

	/**
	 * Setter for the mottattAdressatDato property.
	 *
	 * @param mottattAdressatDato the mottattAdressatDato to set
	 */
	public void setMottattAdressatDato(Date mottattAdressatDato) {
		if (mottattAdressatDato != null) {
			this.mottattAdressatDato = new Date(mottattAdressatDato.getTime());
		} else {
			this.mottattAdressatDato = null;
		}
	}

	/**
	 * Getter for the journalposttype property.
	 *
	 * @return the journalposttype
	 */
	public JournalpostTypeCode getJournalposttype() {
		return journalposttype;
	}

	/**
	 * Setter for the journalposttype property.
	 *
	 * @param journalposttype the journalposttype to set
	 */
	public void setJournalposttype(JournalpostTypeCode journalposttype) {
		this.journalposttype = journalposttype;
	}

	/**
	 * Getter for the signatur property.
	 *
	 * @return the signartur
	 */
	public Boolean getSignatur() {
		return signatur;
	}

	/**
	 * Setter for the signatur property.
	 *
	 * @param signatur the signatur to set
	 */
	public void setSignatur(Boolean signatur) {
		this.signatur = signatur;
	}

	/**
	 * Getter for the behandlingstema property.
	 *
	 * @return the behandlingstema
	 */
	public String getBehandlingstema() {
		return behandlingstema;
	}

	/**
	 * Setter for the behandlingstema property.
	 *
	 * @param behandlingstema the behandlingstema to set
	 */
	public void setBehandlingstema(String behandlingstema) {
		this.behandlingstema = behandlingstema;
	}

	public SkjermingTypeCode getSkjermingType() {
		return skjermingType;
	}

	public void setSkjermingType(SkjermingTypeCode skjermingType) {
		throw new UnsupportedOperationException("Skjerming skal bare settes gjennom SkjermingService");
	}

	public InnsynCode getInnsyn() {
		return innsyn;
	}

	public void setInnsyn(InnsynCode innsyn) {
		this.innsyn = innsyn;
	}

	/**
	 * Add a JournalpostDokumentInfoRelasjon to relasjon Set.
	 *
	 * @param journalpostDokumentInfoRelasjon The relasjon to add.
	 */
	public void addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
		if (journalpostDokumentInfoRelasjon != null) {
			journalpostDokumentInfoRelasjoner.add(journalpostDokumentInfoRelasjon);
			journalpostDokumentInfoRelasjon.setJournalpost(this);
		}
	}

	/**
	 * Removes a JournalpostDokumentInfoRelasjon
	 *
	 * @return true if JournalpostDokumentInfoRelasjon was removed, otherwise
	 * false
	 */
	public boolean removeJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjonToRemove) {
		return journalpostDokumentInfoRelasjoner.remove(relasjonToRemove);
	}

	/**
	 * Getter for the journalpostDokumentInfoRelasjoner property.
	 * <p>
	 * Filterer ut JournalpostDokumentInfoRelasjoner som er skjermet
	 *
	 * @return the journalpostDokumentInfoRelasjoner
	 */
	public Set<JournalpostDokumentInfoRelasjon> getJournalpostDokumentInfoRelasjoner() {
		return Collections.unmodifiableSet(journalpostDokumentInfoRelasjoner.stream()
				.filter(relasjon -> Objects.isNull(relasjon.getSkjermingType()))
				.collect(Collectors.toSet()));
	}

	/**
	 * Returnerer alle journalpostDokumentInfoRelasjoner inkludert skjermet
	 */
	public Set<JournalpostDokumentInfoRelasjon> getJournalpostDokumentInfoRelasjonerAdmin() {
		return Collections.unmodifiableSet(journalpostDokumentInfoRelasjoner);
	}

	/**
	 * Getter for the journalpostDokumentInfoRelasjoner property.
	 *
	 * @return the journalpostDokumentInfoRelasjoner
	 */
	public DokumentInfo getDokumentInfoFromJpDokInfoRelasjoner(int nr) {
		JournalpostDokumentInfoRelasjon dokumentInfoRel;
		java.util.Iterator<JournalpostDokumentInfoRelasjon> dokInfoRelIterator = getJournalpostDokumentInfoRelasjoner().iterator();
		for (int i = 0; dokInfoRelIterator.hasNext(); i++) {
			dokumentInfoRel = dokInfoRelIterator.next();
			if (i == nr) {
				return dokumentInfoRel.getDokumentInfo();
			}
		}
		return null;
	}

	/**
	 * Get dokumentInfo by dokumentInfoId
	 *
	 * @param dokumentInfoId
	 * @return
	 */
	public DokumentInfo getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(Long dokumentInfoId) {
		for (JournalpostDokumentInfoRelasjon rel : getJournalpostDokumentInfoRelasjoner()) {
			DokumentInfo dokumentInfo = rel.getDokumentInfo();
			if (dokumentInfo.getDokumentInfoId().equals(dokumentInfoId)) {
				return dokumentInfo;
			}
		}
		return null;
	}

	/**
	 * Empties the journalpostDokumentInfoRelasjoner set.
	 */
	public void clearJournalpostDokumentInfoRelasjoner() {
		journalpostDokumentInfoRelasjoner.clear();
	}

	/**
	 * Getter for the tilleggsopplysninger property.
	 *
	 * @return the tilleggsopplysninger
	 */
	public Map<String, String> getTilleggsopplysninger() {
		return tilleggsopplysninger;
	}

	/**
	 * Setter for the tilleggsopplysninger property.
	 *
	 * @param tilleggsopplysninger the tilleggsopplysninger to set
	 */
	public void setTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		this.tilleggsopplysninger = tilleggsopplysninger;
	}

	/**
	 * Getter for the kryssreferanser property.
	 *
	 * @return the kryssreferanser
	 */
	public Set<Kryssreferanse> getKryssreferanser() {
		return Collections.unmodifiableSet(kryssreferanser);
	}

	/**
	 * Add a KryssReferanse to the KryssReferanse Set.
	 *
	 * @param kryssreferanse The KryssReferanse to add.
	 */
	public void addKryssReferanse(Kryssreferanse kryssreferanse) {
		if (kryssreferanse != null) {
			kryssreferanser.add(kryssreferanse);
		}
	}

	/**
	 * Updates a Journalpost with values from the Journalpost created by the
	 * scanning process (re-scanning, scenario 2b).
	 *
	 * @param journalpostFromScanning The journalpost created from scanning.
	 */
	public void updateJournalpostWithScanningValues(Journalpost journalpostFromScanning) {
		setMottattDato(journalpostFromScanning.getMottattDato());
		setMottakskanal(journalpostFromScanning.getMottakskanal());
		removeBrukere(getBrukere());
		Set<Bruker> brukereFromScanning = journalpostFromScanning.getBrukere();
		for (Bruker bruker : brukereFromScanning) {
			Bruker newBruker = new Bruker();
			newBruker.setBrukerId(bruker.getBrukerId());
			newBruker.setBrukerType(bruker.getBrukerType());
			addBruker(newBruker);
		}
		DokumentInfo tmpDokInfo = getDokumentInfoFromJpDokInfoRelasjoner(0);
		if (tmpDokInfo.getTittel() == null) {
			tmpDokInfo.setTittel(getInnhold());
		}
	}

	public List<FilDetaljer> findAllFilDetaljerByFilTypeCode(FilTypeCode type) {
		List<FilDetaljer> list = new ArrayList<>();
		for (JournalpostDokumentInfoRelasjon rel : getJournalpostDokumentInfoRelasjoner()) {
			for (FilDetaljer fd : rel.getDokumentInfo().getFildetaljerListe()) {
				if (fd.getFiltype().equals(type)) {
					list.add(fd);
				}
			}
		}
		return list;
	}

	public Optional<TilknyttetJournalpostSomCode> findTilknyttetSomByDokumentinfoId(long dokumentinfoId){
		for (JournalpostDokumentInfoRelasjon rel : getJournalpostDokumentInfoRelasjoner()) {
			if(rel.getDokumentInfo().getId() == dokumentinfoId)
				return Optional.of(rel.getTilknyttetJournalpostSom());
		}
		return Optional.empty();
	}

	public boolean hasHoveddokumentRelasjon() {
		return findHoveddokumentDokumentInfoRelasjon() != null;
	}

	public boolean hasAnyDokumentInfoRelasjoner() {
		return !getJournalpostDokumentInfoRelasjoner().isEmpty();
	}

	public boolean hasAnyDokumentInfoRelasjonerIncludingSkjermet() {
		return !journalpostDokumentInfoRelasjoner.isEmpty();
	}
}
