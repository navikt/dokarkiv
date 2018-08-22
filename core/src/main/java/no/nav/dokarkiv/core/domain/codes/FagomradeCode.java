package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_FAGOMRADE.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public enum FagomradeCode implements CtiRelationship<FagomradeCode, String> {

	/** Bidrag */
	BID,
	/** Pensjon */
	PEN,
	/** Øvrig */
	OVR,
	/** Skanning */
	MOT,
	/** Okonomi */
	OKO,
	/** Bidrag innkreving */
	BII,
	/** FS22 */
	FS22,
	/** Bil */
	BIL,
	/** Hjelpemidler */
	HJE,
	/** Barnetrygd */
	BAR,
	/** Foreldre- og svangerskapspenger */
	FOR,
	/** Gravferdsstønad */
	GRA,
	/** Grunn- og hjelpestønad */
	GRU,
	/** Kontantstøtte */
	KON,
	/** Omsorgspenger, Pleiepenger og opplæringspenger */
	OMS,
	/** Supplerende stønad */
	SUP,
	/** Yrkesskade / Menerstatning */
	YRK,
	/** Enslig forsørger */
	ENF,
	/** Stønadsregnskap */
	STO,
	/** Forsikring */
	FOS,
	/** Erstatning */
	ERS,
	/** Saksomkostning */
	SAK,
	/** Dagpenger */
	DAG,
	/** Individstønad */
	IND,
	/** Mob.stønad */
	MOB,
	/** Oppfølging */
	OPP,
	/** Ventelønn */
	VEN,
	/** Yrkesrettet attføring */
	YRA,
	/** Rehabilitering */
	REH,
	/** Uføreytelser */
	UFO,
	/** Sykepenger */
	SYK,
	/** Sykemelding */
	SYM,
	/** Feilutbetaling (Arenaytelser) */
	FEI,
	/** Generell */
	GEN,
	/** Arbeidsavklaringspenger */
	AAP,
	/** Fullmakt */
	FUL,
	/** Helsetjenester og ort. Hjelpemidler */
	HEL,
	/** Condictio indebiti */
	CON,
	/** Medlemskap */
	MED,
	/** Ukjent */
	UKJ,
	/** Tiltak */
	TIL,
	/** Rekruttering og Stilling */
	REK,
	/** Inkluderende Arbeidsliv */
	IAR,
	/** Ajourhold - Grunnopplysninger */
	AGR,
	/** Trekk */
	TRK,
	/** Kontroll */
	KTR,
	/** Permittering og masseoppsigelser */
	PER,
	/** AA-registeret */
	AAR,
	/** Trygdeavgift */
	TRY,
	/** Sanksjon - Arbeidsgiver */
	SAA,
	/** Sanksjon - Person */
	SAP,
	/** Oppfølging */
	OPA,
	/** Serviceklager */
	SER,
	/** Sikkerhetstiltak */
	SIK,
	/** Unntak fra medlemskap */
	UFM,
	/** Tilleggsstønad arbeidsøkere */
	TSR,
	/** Tilleggsstønad */
	TSO,
	/** Rettferdsvederlag */
	RVE,
	/** Retting av personopplysninger */
	RPO,
	/** Farskap */
	FAR;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<FagomradeCode, String>> getCtiClass() {
		return FagomradeCti.class;
	}

}
