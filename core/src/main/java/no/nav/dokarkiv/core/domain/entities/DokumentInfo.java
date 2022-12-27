package no.nav.dokarkiv.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

/**
 * Holder rede på metadata for et dokument.
 */
@Entity
@Table(name = "T_DOKUMENT_INFO")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class DokumentInfo extends AbstractPersistentVersionedDomainObjectWithKilde {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -2981029229377469184L;

	/**
	 * DokumentInfo title for deleted documents. These documents point to a dummy dokument.
	 */
	public static final String DELETED_DOCUMENT_TITLE = "Slettet dokument";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentInfo_seq")
	@GenericGenerator(name = "dokumentInfo_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {@Parameter(name = "sequence_name", value = "T_DOKUMENT_INFO_SEQ"),
					@Parameter(name = "initial_value", value = "200000000")})
	@Column(name = "dokument_info_id", nullable = false)
	@Setter(AccessLevel.NONE)
	@ToString.Include
	private Long dokumentInfoId;

	@Column(name = "brev_kode", length = 50)
	private String brevkode;

	@Column(name = "brev_gruppe", length = 50)
	private String brevgruppe;

	@Column(name = "konvertert_system", length = 20)
	private String konvertertFraSystem;

	@Column(name = "sensitivt")
	private Boolean sensitivt;

	@Column(name = "endret_av_navn", length = 50)
	private String endretAvNavn;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_kategori_t", length = 20)
	private DokumentKategoriCode kategori;

	@Enumerated(EnumType.STRING)
	@Column(name = "k_dokument_s", length = 20)
	private DokumentStatusCode dokumentstatus;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dato_dok_ferdig")
	private Date dokumentFerdigDato;

	@Column(name = "tittel", length = 500)
	private String tittel;

	@Column(name = "innskr_partsinnsyn", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean innskrenketPartsinnsyn;

	@Column(name = "innskr_partsinnsyn_tredjepart", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean innskrenketPartsinnsynFraTredjepart;

	@Column(name = "organ_internt", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean organInternt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orig_journalpost_id")
	private Journalpost originalJournalpost;

	@Column(name = "dokumenttype_id", length = 20)
	private String dokumenttypeId;

	@Column(name = "dato_kassert")
	private LocalDateTime datoKassert;

	@Column(name = "kassert_av_navn", length = 20)
	private String kassertAvNavn;

	@Column(name = "kassert", length = 1)
	@Type(type = "org.hibernate.type.TrueFalseType")
	private Boolean kassert;

	@ElementCollection
	@JoinTable(name = "t_dok_info_tillegg", joinColumns = @JoinColumn(name = "dokument_info_id", nullable = false))
	@MapKeyColumn(name = "nokkel")
	@Column(name = "verdi", nullable = false)
	@Builder.Default
	private Map<String, String> tilleggsopplysninger = new HashMap<>();

	@OneToMany(mappedBy = "dokumentInfo", orphanRemoval = true)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
	@Builder.Default
	private Set<SkannetInnhold> skannetInnholdListe = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "dokumentInfo")
	@Builder.Default
	private Set<JournalpostDokumentInfoRelasjon> journalpostRelasjoner = new HashSet<>();

	@OneToMany(mappedBy = "dokumentInfo", orphanRemoval = true)
	@Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DETACH})
	@Builder.Default
	private Set<FilDetaljer> fildetaljerListe = new HashSet<>();


	/**
	 * Default constructor.
	 */
	public DokumentInfo() {
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 *
	 * @param dokumentInfoId DB-id for the instance.
	 * @param version        DB-version for the instance.
	 */
	public DokumentInfo(Long dokumentInfoId, long version) {
		this.dokumentInfoId = dokumentInfoId;
		setVersion(version);
		this.tilleggsopplysninger = new HashMap<>();
		this.fildetaljerListe = new HashSet<>();
		this.journalpostRelasjoner = new HashSet<>();
		this.tilleggsopplysninger = new HashMap<>();
		this.skannetInnholdListe = new HashSet<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return getDokumentInfoId();
	}

	/**
	 * Verify that all mandatory fields are set. Some fields are only required
	 * given certain journalStatuses and journalpostTypes.
	 *
	 * @param journalpost The journalpost which has a relation to this DokumentInfo
	 */
	public void verifyMandatoryFields(Journalpost journalpost) {
		if (dokumentInfoIdIsSet()) {
			verifyStringNotBlank(endretAvNavn, "endretAvNavn");
		}
		verifyDokumentStatus(journalpost);

		verifyFieldsForNonLenientJournalstatuses(journalpost);
		verifyFieldsForEndeligJournalforing(journalpost);
	}

	private boolean dokumentInfoIdIsSet() {
		return dokumentInfoId != null;
	}

	private void verifyDokumentStatus(Journalpost journalpost) {
		if (journalpost.isUtgaende() || journalpost.isNotat()) {
			verifyFieldNotNull(dokumentstatus, "dokumentstatus");
		}
	}

	private void verifyFieldsForNonLenientJournalstatuses(Journalpost journalpost) {
		if (!journalpost.hasLenientStatus()) {
			verifyFieldNotNull(kategori, "kategori");
			verifyStringNotBlank(tittel, "tittel");
		}
	}

	private void verifyFieldsForEndeligJournalforing(Journalpost journalpost) {
		if (journalpost.hasEndeligJournalforingStatus()) {
			verifyFilDetaljer();
		}
	}

	private void verifyFilDetaljer() {
		if (fildetaljerListe.isEmpty()) {
			throw new InvalidArgumentException("DokumentInfo must have at least one FilDetaljer");
		}
	}


	/**
	 * Checks that there are no duplicates among the varianter of documents
	 * represented by this DokumentInfos FilDetaljer list.
	 */
	public void verifyNoVariantDuplicates() {
		Map<VariantFormatCode, Integer> variantCounters = countVariants();
		for (Entry<VariantFormatCode, Integer> variantCount : variantCounters.entrySet()) {
			if (variantCount.getValue() > 1) {
				throw new InvalidJournalpostStructureException(this.getClass().getSimpleName()
						+ " cannot contain dokumentvariant duplicates, found " + variantCount.getValue()
						+ " " + variantCount.getKey() + " varianter");
			}
		}
	}

	/**
	 * Check if this DokumentInfo has a document with VariantFormatCode ARKIV.
	 *
	 * @return true if there is a document with variant ARKIV, false otherwise.
	 */
	public boolean hasArkivFormat() {
		for (FilDetaljer filDetaljer : getFildetaljerListe()) {
			if (filDetaljer.getVariantFormat() == VariantFormatCode.ARKIV) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if this DokumentInfo is under redigering.
	 *
	 * @return true if under redigering, false otherwise.
	 */
	public boolean isUnderRedigering() {
		return dokumentstatus == DokumentStatusCode.UNDER_REDIGERING;
	}

	/**
	 * Checks if this DokumentInfo is ferdigstilt.
	 *
	 * @return if ferdigstilt, false otherwise.
	 */
	public boolean isFerdigstilt() {
		return dokumentstatus == DokumentStatusCode.FERDIGSTILT;
	}

	/**
	 * Checks if this DokumentInfo is avbrutt.
	 *
	 * @return if avbrutt, false otherwise.
	 */
	public boolean isAvbrutt() {
		return dokumentstatus == DokumentStatusCode.AVBRUTT;
	}

	private Map<VariantFormatCode, Integer> countVariants() {
		Map<VariantFormatCode, Integer> variantCounters = getVariantCounters();
		for (FilDetaljer filDetaljer : getFildetaljerListe()) {
			VariantFormatCode variantFormat = filDetaljer.getVariantFormat();
			if (variantFormat != null) {
				variantCounters.put(variantFormat, variantCounters.get(variantFormat) + 1);
			}
		}
		return variantCounters;
	}

	private Map<VariantFormatCode, Integer> getVariantCounters() {
		Map<VariantFormatCode, Integer> variantCounters = new HashMap<>();
		for (VariantFormatCode variantFormat : VariantFormatCode.values()) {
			variantCounters.put(variantFormat, 0);
		}
		return variantCounters;
	}

	/**
	 * Finds a SkannetInnhold by Id.
	 *
	 * @param skannetInnholdId The Id.
	 * @return The SkannetInnhold.
	 */
	public SkannetInnhold findSkannetInnholdById(final Long skannetInnholdId) {
		return skannetInnholdListe.stream()
				.filter(skannetInnhold -> skannetInnholdId.equals(skannetInnhold.getId()))
				.findAny()
				.orElse(null);
	}

	/**
	 * Finds a FilDetaljer by Id.
	 *
	 * @param filDetaljerId The Id.
	 * @return The FilDetaljer.
	 */
	public FilDetaljer findFilDetaljerById(final Long filDetaljerId) {
		return fildetaljerListe.stream()
				.filter(filDetaljer -> filDetaljerId.equals(filDetaljer.getId()))
				.findAny()
				.orElse(null);
	}

	/**
	 * Finds a FilDetaljer by filUuid.
	 * <p>
	 * Returnerer null hvis fildetaljer skjermingType ikke er null og variant ikke er ARKIV
	 * Hvis Fildetaljer er ARKIV variant og SLADDET variant finnes og ikke er skjermet så vil SLADDET variant bli returnert
	 * Hvis ARKIV variant er skjermet og SLADDET variant ikke eksisterer eller er skjermet så vil ARKIV variant bli returnert
	 * -> Når ARKIV variant er skjermet så vil det bli returnert en dummy dokument i HentDokument kall. Sjekk DokumentFilSkjermetRepository
	 *
	 * @param filUuid The filUuid.
	 * @return The FilDetaljer.
	 */
	public FilDetaljer findFilDetaljerByFilUuid(final String filUuid) {
		Optional<FilDetaljer> filDetaljerIkkeSkjermet = fildetaljerListe.stream()
				.filter(f -> filUuid.equals(f.getFilUuid()))
				.findAny();

		return filterSkjermetFildetaljer(filDetaljerIkkeSkjermet);

	}

	/**
	 * Filterer ut fildetaljer som er skjermet
	 * Returnerer null hvis fildetaljer skjermingType ikke er null og variant ikke er ARKIV
	 * Hvis Fildetaljer er ARKIV variant og SLADDET variant finnes og ikke er skjermet så vil SLADDET variant bli returnert
	 * Hvis ARKIV variant er skjermet og SLADDET variant ikke eksisterer eller er skjermet så vil ARKIV variant bli returnert
	 * -> Når ARKIV variant er skjermet så vil det bli returnert en dummy dokument i HentDokument kall. Sjekk DokumentFilSkjermetRepository
	 */
	private FilDetaljer filterSkjermetFildetaljer(Optional<FilDetaljer> filDetaljer) {

		//Return SLADDET if ARKIV is skjermet
		//Return ARKIV if SLADDET doesn't exist or is skjermet. In case ARKIV variant is skjermet DokumentFilSkjermetRepository will return a dummy document
		if (filDetaljer.filter(FilDetaljer::isArkivVariant).filter(FilDetaljer::isSkjermet).isPresent()) {
			return Optional.ofNullable(findFilDetaljerByVariantFormatAdmin(SLADDET))
					.filter(f -> isFalse(f.isSkjermet()))
					.orElse(filDetaljer.get());
		}

		return filDetaljer.filter(f -> isFalse(f.isSkjermet())).orElse(null);
	}

	/**
	 * Finds all FilDetaljer with the given variantFormat.
	 * <p>
	 * Filterer ut fildetaljer som er skjermet
	 * Returnerer SLADDET variant hvis ARKIV variant er skjermet
	 * Hvis ARKIV variant er skjermet og SLADDET variant ikke eksisterer eller er skjermet så vil ARKIV variant bli returnert
	 * Når ARKIV variant er skjermet så vil det bli returnert en dummy dokument i HentDokument kall. SjekkDokumentFilSkjermetRepository
	 *
	 * @param variantFormat The VariantFormatCode.
	 * @return A list of Fildetaljer with the given VariantFormatCode.
	 */
	public FilDetaljer findFilDetaljerByVariantFormat(final VariantFormatCode variantFormat) {
		Optional<FilDetaljer> filDetaljer = fildetaljerListe.stream()
				.filter(fd -> variantFormat.equals(fd.getVariantFormat()))
				.findAny();

		return filterSkjermetFildetaljer(filDetaljer);
	}

	/**
	 * Returnerer fildetaljer med gitt variantFormat inkludert skjermet
	 */
	public FilDetaljer findFilDetaljerByVariantFormatAdmin(final VariantFormatCode variantFormat) {
		return fildetaljerListe.stream()
				.filter(filDetaljer -> variantFormat.equals(filDetaljer.getVariantFormat()))
				.findAny()
				.orElse(null);

	}

	/**
	 * Finds a JournalpostDokumentInfoRelasjon that has a Journalpost with the given journalpostId.
	 *
	 * @param journalpostId The journalpostId.
	 * @return The JournalpostDokumentInfoRelasjon.
	 */
	public JournalpostDokumentInfoRelasjon findJournalpostRelasjonByJournalpostId(final Long journalpostId) {
		for (JournalpostDokumentInfoRelasjon journalpostRelasjon : getJournalpostRelasjoner()) {
			if (journalpostRelasjon.getJournalpost() != null
					&& journalpostRelasjon.getJournalpost().getJournalpostId().equals(journalpostId)) {
				return journalpostRelasjon;
			}
		}
		return null;
	}

	/**
	 * Checks if the DokumentInfo is related to multiple journalposts.
	 *
	 * @return true if this dokumentInfo has more than one journalpostrelasjon, otherwise false
	 */
	public boolean isRelatedToMultipleJournalposts() {
		return getJournalpostRelasjoner().size() > 1;
	}

	public JournalpostDokumentInfoRelasjon findHoveddokumentJournalpostRelasjon() {
		return journalpostRelasjoner.stream()
				.filter(rel -> rel.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.findAny()
				.orElse(null);
	}

	/**
	 * Getter for the skannetInnhold property.
	 *
	 * @return the skannetInnhold
	 */
	public Set<SkannetInnhold> getSkannetInnholdListe() {
		return Collections.unmodifiableSet(skannetInnholdListe);
	}

	/**
	 * Add a SkannetInnhold to the skannetInnhold Set.
	 *
	 * @param skannetInnhold The SkannetInnhold to add.
	 */
	public void addSkannetInnhold(SkannetInnhold skannetInnhold) {
		if (skannetInnhold != null) {
			skannetInnhold.setDokumentInfo(this);
			this.skannetInnholdListe.add(skannetInnhold);
		}
	}

	public void setDokumentFerdigDato(Date dokumentFerdigDato) {
		if (dokumentFerdigDato != null) {
			this.dokumentFerdigDato = new Date(dokumentFerdigDato.getTime());
		} else {
			this.dokumentFerdigDato = null;
		}
	}

	/**
	 * Add a JournalpostDokumentInfoRelasjon to the journalpostRelasjon Set
	 *
	 * @param journalpostRelasjon The relasjon to add.
	 */
	void addJournalpostRelasjon(JournalpostDokumentInfoRelasjon journalpostRelasjon) {
		this.journalpostRelasjoner.add(journalpostRelasjon);
	}

	/**
	 * Getter for the journalpostRelasjoner property.
	 *
	 * @return the journalpostRelasjoner
	 */
	public Set<JournalpostDokumentInfoRelasjon> getJournalpostRelasjoner() {
		return Collections.unmodifiableSet(journalpostRelasjoner);
	}

	/**
	 * Add a FilDetaljer to the filDetaljer Set.
	 *
	 * @param filDetaljer The filDetaljer to add.
	 */
	public void addFilDetaljer(FilDetaljer filDetaljer) {
		if (filDetaljer != null) {
			this.fildetaljerListe.add(filDetaljer);
			filDetaljer.setDokumentInfo(this);
		}
	}

	/**
	 * Remove a FilDetaljer from the filDetaljer Set.
	 *
	 * @param filDetaljer The filDetaljer to remove.
	 */
	public void removeFilDetaljer(FilDetaljer filDetaljer) {
		if (filDetaljer != null) {
			this.fildetaljerListe.remove(filDetaljer);
		}
	}

	/**
	 * Getter for the fildetaljerListe property.
	 * Filterer ut fildetaljer som er skjermet og sladdet
	 *
	 * @return the fildetaljerListe
	 */
	public Set<FilDetaljer> getFildetaljerListe() {
		return Collections.unmodifiableSet(fildetaljerListe.stream()
				.filter(filDetaljer -> filDetaljer.getVariantFormat() == ARKIV || Objects.isNull(filDetaljer.getSkjermingType()))
				.collect(Collectors.toSet())
		);
	}

	/**
	 * Returnerer alle Fildetaljer inkludert skjermet
	 */
	public Set<FilDetaljer> getFildetaljerListeAdmin() {
		return Collections.unmodifiableSet(fildetaljerListe);
	}

	/**
	 * Empties the filDetaljer list.
	 */
	public void clearFildetaljerListe() {
		fildetaljerListe.clear();
	}

	/**
	 * Removes a JournalpostDokumentInfoRelasjon
	 *
	 * @return true if JournalpostDokumentInfoRelasjon was removed, otherwise
	 * false
	 */
	public boolean removeJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon relasjonToRemove) {
		return journalpostRelasjoner.remove(relasjonToRemove);
	}

	public void setKassert(boolean kassert) {
		this.kassert = kassert;
	}

	public boolean isKassert() {
		return this.kassert != null && this.kassert;
	}

}
