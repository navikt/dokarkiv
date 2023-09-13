package no.nav.dokarkiv.core.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FaktiskDistribusjonskanalCode;
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
import java.time.OffsetDateTime;
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
 * Inneholder metadata om en samling av dokumenter, hvilken bruker de gjelder og sakstilknytning.
 */
@Entity
@Table(name = "T_JOURNALPOST")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
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
	@ToString.Include
	private Long journalpostId;

	@Column(name = "journalf_enhet", length = 20)
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

	@Column(name = "innhold", length = 500)
	private String innhold;

	@Column(name = "krav_type", length = 20)
	private String kravtype;

	@Column(name = "original_bestilt", length = 1)
	private Boolean originaltBestilt;

	@Column(name = "opprettet_av_navn", length = 50)
	private String opprettetAvNavn;

	@Column(name = "endret_av_navn", length = 50)
	private String endretAvNavn;

	@Column(name = "kanal_referanse_id", unique = true, length = 200)
	private String kanalReferanseId;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fagomrade", nullable = false, length = 20)
	private FagomradeCode fagomrade;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_journal_s", nullable = false, length = 20)
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

	@Column(name = "journalfort_av_navn", length = 50)
	private String journalfortAvNavn;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_mottatt")
	private Date mottattDato;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_mottaks_kanal", length = 20)
	private MottaksKanalCode mottakskanal;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_utsendings_kanal", length = 20)
	private UtsendingsKanalCode utsendingskanal;

	@Column(name = "land", length = 50)
	private String land;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_fakt_dis_kanal", length = 20)
	private FaktiskDistribusjonskanalCode faktiskDistribusjonskanal;

	@Column(name = "elektronisk_distr", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean elektroniskDistribusjon;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_ekspedert")
	private Date ekspedertDato;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_lest")
	private Date lestDato;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_journalpost_t", nullable = false, length = 20)
	private JournalpostTypeCode journalposttype;

	@Column(name = "signatur", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean signatur;

	@Column(name = "k_behandlingstema", length = 20)
	private String behandlingstema;

	@Column(name = "k_skjerming_type", length = 50)
	@Enumerated(EnumType.STRING)
	@Setter(AccessLevel.NONE)
	private SkjermingTypeCode skjermingType;

	@Column(name = "k_innsyn", length = 50)
	@Enumerated(EnumType.STRING)
	private InnsynCode innsyn;

	@OneToMany(mappedBy = "journalpost", fetch = FetchType.LAZY)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private final Set<Bruker> brukere = new HashSet<>();

	// Bidireksjonelle OneToOne relasjoner blir eager fetched fra Journalpost
	@OneToOne(mappedBy = "journalpost", cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Saksrelasjon saksrelasjon;

	@OneToMany(mappedBy = "journalpost")
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	private final Set<JournalpostDokumentInfoRelasjon> journalpostDokumentInfoRelasjoner = new HashSet<>();

	@ElementCollection
	@JoinTable(name = "t_jp_tillegg", joinColumns = @JoinColumn(name = "journalpost_id", nullable = false))
	@MapKeyColumn(name = "nokkel")
	@Column(name = "verdi", nullable = false)
	private Map<String, String> tilleggsopplysninger = new HashMap<>();

	@OneToMany(mappedBy = "journalpost", fetch = FetchType.LAZY)
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
	 * @param version       DB-version for the instance.
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
	 * Getter for the brukere property.
	 *
	 * @return the brukere
	 */
	public Set<Bruker> getBrukere() {
		return Collections.unmodifiableSet(brukere);
	}

	/**
	 * Add a Bruker to the Bruker Set.
	 *
	 * @param bruker The Bruker to add,
	 */
	public void addBruker(Bruker bruker) {
		if (bruker != null) {
			brukere.add(bruker);
			bruker.setJournalpost(this);
		}
	}

	/**
	 * Empties the Brukere set
	 */
	public void clearBrukere() {
		brukere.clear();
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
		if(saksrelasjon == null) {
			if(this.saksrelasjon != null) {
				this.saksrelasjon.setJournalpost(null);
			}
		} else {
			saksrelasjon.setJournalpost(this);
		}
		this.saksrelasjon = saksrelasjon;
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
	 */
	public void removeJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjonToRemove) {
		journalpostDokumentInfoRelasjoner.remove(relasjonToRemove);
		relasjonToRemove.setJournalpost(this);
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
			kryssreferanse.setJournalpost(this);
		}
	}

	public void setEkspedertDato(OffsetDateTime ekspedertDato) {
		if (ekspedertDato != null) {
			this.ekspedertDato = Date.from(ekspedertDato.toInstant());
		} else {
			this.ekspedertDato = null;
		}
	}

	public void setLestDato(OffsetDateTime lestDato) {
		if (lestDato != null) {
			this.lestDato = Date.from(lestDato.toInstant());
		} else {
			this.lestDato = null;
		}
	}

	public void setAvsendtReturDato(Date avsendtReturDato) {
		if (avsendtReturDato != null) {
			this.avsendtReturDato = new Date(avsendtReturDato.getTime());
		} else {
			this.avsendtReturDato = null;
		}
	}

	public void setSendtPrintDato(Date sendtPrintDato) {
		if (sendtPrintDato != null) {
			this.sendtPrintDato = new Date(sendtPrintDato.getTime());
		} else {
			this.sendtPrintDato = null;
		}
	}

	public void setJournalDato(Date journalDato) {
		if (journalDato != null) {
			this.journalDato = new Date(journalDato.getTime());
		} else {
			this.journalDato = null;
		}
	}

	public void setDokumentDato(Date dokumentDato) {
		if (dokumentDato != null) {
			this.dokumentDato = new Date(dokumentDato.getTime());
		} else {
			this.dokumentDato = null;
		}
	}

	public void setMottattDato(Date mottattDato) {
		if (mottattDato != null) {
			this.mottattDato = new Date(mottattDato.getTime());
		} else {
			this.mottattDato = null;
		}
	}

	public Optional<TilknyttetJournalpostSomCode> findTilknyttetSomByDokumentinfoId(long dokumentinfoId) {
		for (JournalpostDokumentInfoRelasjon rel : getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getId() == dokumentinfoId)
				return Optional.of(rel.getTilknyttetJournalpostSom());
		}
		return Optional.empty();
	}

	public boolean hasHoveddokumentRelasjon() {
		return findHoveddokumentDokumentInfoRelasjon() != null;
	}

	public boolean hasAnyDokumentInfoRelasjonerIncludingSkjermet() {
		return !journalpostDokumentInfoRelasjoner.isEmpty();
	}
}
