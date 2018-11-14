package no.nav.dokarkiv.core.domain.entities;

import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.contains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain entity class that represents document info.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Thomas Kåsene, Visma Consulting AS
 */
@Entity
@Table(name = "T_DOKUMENT_INFO")
@Builder
@AllArgsConstructor
public class DokumentInfo extends AbstractPersistentVersionedDomainObjectWithKilde {

    /**
     * ID used for serialization.
     */
    private static final long serialVersionUID = -***gammelt_fnr***77469184L;

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
    private Long dokumentInfoId;

    @Column(name = "brev_kode")
    private String brevkode;

    @Column(name = "brev_gruppe")
    private String brevgruppe;

    @Column(name = "konvertert_system")
    private String konvertertFraSystem;

    @Column(name = "sensitivt")
    private Boolean sensitivt;

    @Column(name = "slettet")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean slettet;

    @Column(name = "endret_av_navn")
    private String endretAvNavn;

    @Enumerated(EnumType.STRING)
    @Column(name = "k_kategori_t")
    private DokumentKategoriCode kategori;

    @Enumerated(EnumType.STRING)
    @Column(name = "k_dokument_s")
    private DokumentStatusCode dokumentstatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dato_dok_ferdig")
    private Date dokumentFerdigDato;

    @Column(name = "tittel")
    private String tittel;

    @Column(name = "konfidensialitet")
    private String konfidensialitet;

    @Column(name = "integritet")
    private String integritet;

    @Column(name = "tilgjengelighet")
    private String tilgjengelighet;

    @Column(name = "innskr_partsinnsyn")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean innskrenketPartsinnsyn;

    @Column(name = "innskr_partsinnsyn_tredjepart")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean innskrenketPartsinnsynFraTredjepart;

    @Column(name = "organ_internt")
    @Type(type = "org.hibernate.type.TrueFalseType")
    private Boolean organInternt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orig_journalpost_id")
    private Journalpost originalJournalpost;

    @Column(name = "dokumenttype_id")
    private String dokumenttypeId;

    @ElementCollection
    @JoinTable(name = "t_dok_info_tillegg", joinColumns = @JoinColumn(name = "dokument_info_id", nullable = false))
    @MapKeyColumn(name = "nokkel")
    @Column(name = "verdi", nullable = false)
    @Builder.Default
    private Map<String, String> tilleggsopplysninger = new HashMap<>();

    @OneToMany
    @JoinColumn(name = "dokument_info_id", nullable = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DETACH})
    @Builder.Default
    private Set<SkannetInnhold> skannetInnholdListe = new HashSet<>();

    @OneToMany(mappedBy = "dokumentInfo")
    @Builder.Default
    private Set<JournalpostDokumentInfoRelasjon> journalpostRelasjoner = new HashSet<>();

    @OneToMany(mappedBy = "dokumentInfo", orphanRemoval = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE, CascadeType.DETACH})
    @Builder.Default
    private Set<FilDetaljer> fildetaljerListe = new HashSet<>();

    @Transient
    private List<Long> begrensetRelasjonerJournalpostId = new ArrayList<>();

    /**
     * Default constructor.
     */
    public DokumentInfo() {
    }

    public void addAllbegrensetRelasjonJournalpostIds(List<Long> journalpostId) {
        if (begrensetRelasjonerJournalpostId == null) {
            begrensetRelasjonerJournalpostId = new ArrayList<>();
        }

        begrensetRelasjonerJournalpostId.addAll(journalpostId);
    }

    public List<Long> getBegrensetRelasjonerJournalpostId() {
        if (begrensetRelasjonerJournalpostId == null) {
            return new ArrayList<>();
        }
        return begrensetRelasjonerJournalpostId;
    }

    /**
     * Constructor that assigns immutable properties. Used for testing.
     *
     * @param dokumentInfoId DB-id for the instance.
     * @param version DB-version for the instance.
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

    /**
     * Verify that all mandatory fields are set except endretAvNavn. Some fields are only required
     * given certain journalStatuses and journalpostTypes.
     *
     * @param journalpost The journalpost which has a relation to this DokumentInfo
     */
    public void verifyMandatoryFieldsForInngaaendeJournal(Journalpost journalpost) {
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
     *
     * @param filUuid The filUuid.
     * @return The FilDetaljer.
     */
    public FilDetaljer findFilDetaljerByFilUuid(final String filUuid) {
        return fildetaljerListe.stream().filter(filDetaljer -> filUuid.equals(filDetaljer.getFilUuid())).findAny().orElse(null);
    }

    /**
     * Finds all FilDetaljer with the given variantFormat.
     *
     * @param variantFormat The VariantFormatCode.
     * @return A list of Fildetaljer with the given VariantFormatCode.
     */
    public FilDetaljer findFilDetaljerByVariantFormat(final VariantFormatCode variantFormat) {
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

    /**
     * <p>Checks if the document is deleted. A document is considered deleted if {@link #getSlettet()}
     * returns {@code true} or if the value returned by {@link #getTittel()} contains
     * &quot;{@value #DELETED_DOCUMENT_TITLE}&quot;.</p>
     * <p/>
     * <p>The old method of deleting a document included changing the title to contain the text
     * &quot;{@value #DELETED_DOCUMENT_TITLE}&quot;, which is why we check for that here.</p>
     *
     * @return {@code true} if the document has been marked as deleted, otherwise {@code false}.
     */
    public boolean isFunksjoneltSlettet() {
        return isTrue(slettet) || contains(getTittel(), DELETED_DOCUMENT_TITLE);
    }

    /**
     * Getter for the brevGruppe property.
     *
     * @return the brevGruppe
     */
    public String getBrevgruppe() {
        return brevgruppe;
    }

    /**
     * Setter for the brevGruppe property.
     *
     * @param brevgruppe the brevGruppe to set
     */
    public void setBrevgruppe(String brevgruppe) {
        this.brevgruppe = brevgruppe;
    }

    /**
     * Getter for the brevKode property.
     *
     * @return the brevKode
     */
    public String getBrevkode() {
        return brevkode;
    }

    /**
     * Setter for the brevKode property.
     *
     * @param brevkode the brevKode to set
     */
    public void setBrevkode(String brevkode) {
        this.brevkode = brevkode;
    }

    /**
     * Getter for the kategori property.
     *
     * @return the kategori
     */
    public DokumentKategoriCode getKategori() {
        return kategori;
    }

    /**
     * Setter for the kategori property.
     *
     * @param kategori the kategori to set
     */
    public void setKategori(DokumentKategoriCode kategori) {
        this.kategori = kategori;
    }

    /**
     * Getter for the konvertertSystem property.
     *
     * @return the konvertertSystem
     */
    public String getKonvertertFraSystem() {
        return konvertertFraSystem;
    }

    /**
     * Setter for the konvertertSystem property.
     *
     * @param konvertertFraSystem the konvertertSystem to set
     */
    public void setKonvertertFraSystem(String konvertertFraSystem) {
        this.konvertertFraSystem = konvertertFraSystem;
    }

    /**
     * Getter for the sensitivt property.
     *
     * @return the sensitivt
     */
    public Boolean getSensitivt() {
        return sensitivt;
    }

    /**
     * Setter for the sensitivt property.
     *
     * @param sensitivt the sensitivt to set
     */
    public void setSensitivt(Boolean sensitivt) {
        this.sensitivt = sensitivt;
    }

    /**
     * Getter for the {@link #slettet} property.
     *
     * @return The value of the {@link #slettet} property.
     * @see #isFunksjoneltSlettet()
     */
    public Boolean getSlettet() {
        return slettet;
    }

    /**
     * Sets whether or not this document should be marked as deleted.
     *
     * @param slettet The boolean value to which the slettet property should be set.
     */
    public void setSlettet(Boolean slettet) {
        this.slettet = slettet;
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
     * Getter for the skannetInnhold property.
     *
     * @return the skannetInnhold
     */
    public Set<SkannetInnhold> getSkannetInnholdListe() {
        return Collections.unmodifiableSet(skannetInnholdListe);
    }

    /**
     * Empties the skannetInnhold list.
     */
    public void clearSkannetInnholdListe() {
        skannetInnholdListe.clear();
    }

    /**
     * Add a SkannetInnhold to the skannetInnhold Set.
     *
     * @param skannetInnhold The SkannetInnhold to add.
     */
    public void addSkannetInnhold(SkannetInnhold skannetInnhold) {
        if (skannetInnhold != null) {
            this.skannetInnholdListe.add(skannetInnhold);
        }
    }

    /**
     * Getter for the dokumentInfoId property.
     *
     * @return the dokumentInfoId
     */
    public Long getDokumentInfoId() {
        return dokumentInfoId;
    }

    /**
     * Getter for the dokumentstatus property.
     *
     * @return the dokumentstatus
     */
    public DokumentStatusCode getDokumentstatus() {
        return dokumentstatus;
    }

    /**
     * Setter for the dokumentstatus property.
     *
     * @param dokumentstatus the dokumentstatus to set
     */
    public void setDokumentstatus(DokumentStatusCode dokumentstatus) {
        this.dokumentstatus = dokumentstatus;
    }

    /**
     * Getter for the dokumentFerdigDato property.
     *
     * @return the dokumentFerdigDato
     */
    public Date getDokumentFerdigDato() {
        if (dokumentFerdigDato != null) {
            return new Date(dokumentFerdigDato.getTime());
        }
        return null;
    }

    /**
     * Setter for the dokumentFerdigDato property.
     *
     * @param dokumentFerdigDato the dokumentFerdigDato to set
     */
    public void setDokumentFerdigDato(Date dokumentFerdigDato) {
        if (dokumentFerdigDato != null) {
            this.dokumentFerdigDato = new Date(dokumentFerdigDato.getTime());
        } else {
            this.dokumentFerdigDato = null;
        }
    }

    /**
     * Getter for the tittel property.
     *
     * @return the tittel
     */
    public String getTittel() {
        return tittel;
    }

    /**
     * Setter for the tittel property.
     *
     * @param tittel the tittel to set
     */
    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    /**
     * Getter for the konfidensialitet property.
     *
     * @return the konfidensialitet
     */
    public String getKonfidensialitet() {
        return konfidensialitet;
    }

    /**
     * Setter for the konfidensialitet property.
     *
     * @param konfidensialitet the konfidensialitet to set
     */
    public void setKonfidensialitet(String konfidensialitet) {
        this.konfidensialitet = konfidensialitet;
    }

    /**
     * Getter for the integritet property.
     *
     * @return the integritet
     */
    public String getIntegritet() {
        return integritet;
    }

    /**
     * Setter for the integritet property.
     *
     * @param integritet the integritet to set
     */
    public void setIntegritet(String integritet) {
        this.integritet = integritet;
    }

    /**
     * Getter for the tilgjengelighet property.
     *
     * @return the tilgjengelighet
     */
    public String getTilgjengelighet() {
        return tilgjengelighet;
    }

    /**
     * Setter for the tilgjengelighet property.
     *
     * @param tilgjengelighet the tilgjengelighet to set
     */
    public void setTilgjengelighet(String tilgjengelighet) {
        this.tilgjengelighet = tilgjengelighet;
    }

    /**
     * @return the innskrPartinnsyn
     */
    public Boolean getInnskrenketPartsinnsyn() {
        return innskrenketPartsinnsyn;
    }

    /**
     * @param innskrenketPartsinnsyn the innskrPartinnsyn to set
     */
    public void setInnskrenketPartsinnsyn(Boolean innskrenketPartsinnsyn) {
        this.innskrenketPartsinnsyn = innskrenketPartsinnsyn;
    }

    /**
     * Getter for the {@link #innskrenketPartsinnsynFraTredjepart} property.
     *
     * @return The value of the {@link #innskrenketPartsinnsynFraTredjepart} property.
     */
    public Boolean getInnskrenketPartsinnsynFraTredjepart() {
        return innskrenketPartsinnsynFraTredjepart;
    }

    /**
     * Sets whether or not a third-party source has marked this document as unviewable.
     *
     * @param innskrenketPartsinnsyn The boolean value to which the innskrenketPartsinnsynFraTredjepart
     * property should be set.
     */
    public void setInnskrenketPartsinnsynFraTredjepart(Boolean innskrenketPartsinnsyn) {
        this.innskrenketPartsinnsynFraTredjepart = innskrenketPartsinnsyn;
    }

    /**
     * Getter for the organInternt property.
     *
     * @return the organInternt
     */
    public Boolean getOrganInternt() {
        return organInternt;
    }

    /**
     * Setter for the organInternt property.
     *
     * @param organInternt the organInternt to set
     */
    public void setOrganInternt(Boolean organInternt) {
        this.organInternt = organInternt;
    }

    /**
     * Getter for the originalJournalpost property.
     *
     * @return the originalJournalpost
     */
    public Journalpost getOriginalJournalpost() {
        return originalJournalpost;
    }

    /**
     * Setter for the originalJournalpost property.
     *
     * @param originalJournalpost the originalJournalpost to set
     */
    public void setOriginalJournalpost(Journalpost originalJournalpost) {
        this.originalJournalpost = originalJournalpost;
    }

    /**
     * Getter for the dokumenttypeId property.
     *
     * @return the dokumenttypeId
     */
    public String getDokumenttypeId() {
        return dokumenttypeId;
    }

    /**
     * Setter for the dokumenttypeId property.
     *
     * @param dokumenttypeId the dokumenttypeId to set
     */
    public void setDokumenttypeId(String dokumenttypeId) {
        this.dokumenttypeId = dokumenttypeId;
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
        return Collections.unmodifiableSet(journalpostRelasjoner.stream()
                .filter(relasjon -> relasjon.getJournalpost() == null || isFalse(getBegrensetRelasjonerJournalpostId().contains(relasjon
                        .getJournalpost()
                        .getJournalpostId())))
                .collect(Collectors.toSet()));
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
     *
     * @return the fildetaljerListe
     */
    public Set<FilDetaljer> getFildetaljerListe() {
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

}
