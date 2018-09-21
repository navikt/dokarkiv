package no.nav.dokarkiv.hentjournalinfo.dto.kode;

import static java.lang.String.format;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;

import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Getter
public enum Tema {

    @GraphQLEnumValue(description = "Bidrag")
    BID(FagomradeCode.BID),
    @GraphQLEnumValue(description = "Pensjon")
    PEN(FagomradeCode.PEN),
    @GraphQLEnumValue(description = "Øvrig")
    OVR(FagomradeCode.OVR),
    @GraphQLEnumValue(description = "Skanning")
    MOT(FagomradeCode.MOT),
    @GraphQLEnumValue(description = "Okonomi")
    OKO(FagomradeCode.OKO),
    @GraphQLEnumValue(description = "Bidrag innkreving")
    BII(FagomradeCode.BII),
    @GraphQLEnumValue(description = "FS22")
    FS22(FagomradeCode.FS22),
    @GraphQLEnumValue(description = "Bil")
    BIL(FagomradeCode.BIL),
    @GraphQLEnumValue(description = "Hjelpemidler")
    HJE(FagomradeCode.HJE),
    @GraphQLEnumValue(description = "Barnetrygd")
    BAR(FagomradeCode.BAR),
    @GraphQLEnumValue(description = "Foreldre- og svangerskapspenger")
    FOR(FagomradeCode.FOR),
    @GraphQLEnumValue(description = "Gravferdsstønad")
    GRA(FagomradeCode.GRA),
    @GraphQLEnumValue(description = "Grunn- og hjelpestønad")
    GRU(FagomradeCode.BID),
    @GraphQLEnumValue(description = "Kontantstøtte")
    KON(FagomradeCode.KON),
    @GraphQLEnumValue(description = "Omsorgspenger, Pleiepenger og opplæringspenger")
    OMS(FagomradeCode.OMS),
    @GraphQLEnumValue(description = "Supplerende stønad")
    SUP(FagomradeCode.SUP),
    @GraphQLEnumValue(description = "Yrkesskade / Menerstatning")
    YRK(FagomradeCode.YRK),
    @GraphQLEnumValue(description = "Enslig forsørger")
    ENF(FagomradeCode.ENF),
    @GraphQLEnumValue(description = "Stønadsregnskap")
    STO(FagomradeCode.STO),
    @GraphQLEnumValue(description = "Forsikring")
    FOS(FagomradeCode.FOS),
    @GraphQLEnumValue(description = "Erstatning")
    ERS(FagomradeCode.ERS),
    @GraphQLEnumValue(description = "Saksomkostning")
    SAK(FagomradeCode.SAK),
    @GraphQLEnumValue(description = "Dagpenger")
    DAG(FagomradeCode.DAG),
    @GraphQLEnumValue(description = "Individstønad")
    IND(FagomradeCode.IND),
    @GraphQLEnumValue(description = "Mob.stønad")
    MOB(FagomradeCode.MOB),
    @GraphQLEnumValue(description = "Oppfølging")
    OPP(FagomradeCode.OPP),
    @GraphQLEnumValue(description = "Ventelønn")
    VEN(FagomradeCode.VEN),
    @GraphQLEnumValue(description = "Yrkesrettet attføring")
    YRA(FagomradeCode.YRA),
    @GraphQLEnumValue(description = "Rehabilitering")
    REH(FagomradeCode.REH),
    @GraphQLEnumValue(description = "Uføreytelser")
    UFO(FagomradeCode.UFO),
    @GraphQLEnumValue(description = "Sykepenger")
    SYK(FagomradeCode.SYK),
    @GraphQLEnumValue(description = "Sykemelding")
    SYM(FagomradeCode.SYM),
    @GraphQLEnumValue(description = "Feilutbetaling (Arenaytelser)")
    FEI(FagomradeCode.FEI),
    @GraphQLEnumValue(description = "Generell")
    GEN(FagomradeCode.GEN),
    @GraphQLEnumValue(description = "Arbeidsavklaringspenger")
    AAP(FagomradeCode.AAP),
    @GraphQLEnumValue(description = "Fullmakt")
    FUL(FagomradeCode.FUL),
    @GraphQLEnumValue(description = "Helsetjenester og ort. Hjelpemidler")
    HEL(FagomradeCode.HEL),
    @GraphQLEnumValue(description = "Condictio indebiti")
    CON(FagomradeCode.CON),
    @GraphQLEnumValue(description = "Medlemskap")
    MED(FagomradeCode.MED),
    @GraphQLEnumValue(description = "Ukjent")
    UKJ(FagomradeCode.UKJ),
    @GraphQLEnumValue(description = "Tiltak")
    TIL(FagomradeCode.TIL),
    @GraphQLEnumValue(description = "Rekruttering og Stilling")
    REK(FagomradeCode.REK),
    @GraphQLEnumValue(description = "Inkluderende Arbeidsliv")
    IAR(FagomradeCode.IAR),
    @GraphQLEnumValue(description = "Ajourhold - Grunnopplysninger")
    AGR(FagomradeCode.AGR),
    @GraphQLEnumValue(description = "Trekk")
    TRK(FagomradeCode.TRK),
    @GraphQLEnumValue(description = "Kontroll")
    KTR(FagomradeCode.KTR),
    @GraphQLEnumValue(description = "Permittering og masseoppsigelser")
    PER(FagomradeCode.PER),
    @GraphQLEnumValue(description = "AA-registeret")
    AAR(FagomradeCode.AAR),
    @GraphQLEnumValue(description = "Trygdeavgift")
    TRY(FagomradeCode.TRY),
    @GraphQLEnumValue(description = "Sanksjon - Arbeidsgiver")
    SAA(FagomradeCode.SAA),
    @GraphQLEnumValue(description = "Sanksjon - Person")
    SAP(FagomradeCode.SAP),
    @GraphQLEnumValue(description = "Oppfølging")
    OPA(FagomradeCode.OPA),
    @GraphQLEnumValue(description = "Serviceklager")
    SER(FagomradeCode.SER),
    @GraphQLEnumValue(description = "Sikkerhetstiltak")
    SIK(FagomradeCode.SIK),
    @GraphQLEnumValue(description = "Unntak fra medlemskap")
    UFM(FagomradeCode.UFM),
    @GraphQLEnumValue(description = "Tilleggsstønad arbeidsøkere")
    TSR(FagomradeCode.TSR),
    @GraphQLEnumValue(description = "Tilleggsstønad")
    TSO(FagomradeCode.TSO),
    @GraphQLEnumValue(description = "Rettferdsvederlag")
    RVE(FagomradeCode.RVE),
    @GraphQLEnumValue(description = "Retting av personopplysninger")
    RPO(FagomradeCode.RPO),
    @GraphQLEnumValue(description = "Farskap")
    FAR(FagomradeCode.FAR);

    private final FagomradeCode mapFromValue;

    Tema(FagomradeCode mapFromValue) {
        this.mapFromValue = mapFromValue;
    }

    public static Tema mapFromFagomradeCode(FagomradeCode fagomradeCode) {
        Optional<Tema> tema = Optional.empty();
        for (Tema temaValue : Tema.values()) {
            if (fagomradeCode == temaValue.getMapFromValue()) {
                tema = Optional.of(temaValue);
            }
        }

        return tema.orElseThrow(() -> new IllegalArgumentException(format("Kunne ikke mappe FagomradeCode=%s til Tema", fagomradeCode)));
    }
}
