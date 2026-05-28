package model;

import java.io.Serializable;
import java.time.*;
import java.util.*;

/*
 * Un tipo di visita è il "template" da cui si generano le visite reali su date specifiche.
 * Es: "Alla scoperta del Moretto" è un tipo di visita; ogni sabato in cui viene
 * proposta è una Visita distinta.
 *
 * Invariante:
 *   titolo != null, oraInizio != null
 *   durata > 0, minPartecipanti >= 1, maxPartecipanti >= minPartecipanti
 *   giorniSettimana non vuoto
 */
public class TipoVisita implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String titolo;
    private final String descrizione;
    private final String puntoIncontro;
    private final MonthDay inizioPeriodo;
    private final MonthDay finePeriodo;
    private final Set<GiornoSettimana> giorniSettimana;
    private final LocalTime oraInizio;
    private final int durata;           // minuti
    private final boolean bigliettoRichiesto;
    private final int minPartecipanti;
    private final int maxPartecipanti;

    // volontari abilitati a fare da guida in questo tipo di visita
    private final List<Volontario> volontari = new ArrayList<>();

    public TipoVisita(String titolo, String descrizione, String puntoIncontro,
                      MonthDay inizioPeriodo, MonthDay finePeriodo,
                      Set<GiornoSettimana> giorniSettimana, LocalTime oraInizio,
                      int durata, boolean bigliettoRichiesto,
                      int minPartecipanti, int maxPartecipanti) {

        this.titolo = titolo.trim();
        this.descrizione = descrizione.trim();
        this.puntoIncontro = puntoIncontro.trim();
        this.inizioPeriodo = inizioPeriodo;
        this.finePeriodo = finePeriodo;
        this.giorniSettimana = Collections.unmodifiableSet(new LinkedHashSet<>(giorniSettimana));
        this.oraInizio = oraInizio;
        this.durata = durata;
        this.bigliettoRichiesto = bigliettoRichiesto;
        this.minPartecipanti = minPartecipanti;
        this.maxPartecipanti = maxPartecipanti;
    }

    // ---- gestione volontari ----

    public void aggiungiVolontario(Volontario v) {
        if (!volontari.contains(v)) volontari.add(v);
    }

    public boolean rimuoviVolontario(Volontario v) {
        return volontari.remove(v);
    }

    public boolean hasVolontari() { return !volontari.isEmpty(); }

    // ---- logica di schedulazione ----

    // controlla se questa visita si può fare in questo giorno della settimana
    public boolean programmabileIl(GiornoSettimana g) {
        return giorniSettimana.contains(g);
    }

    // controlla se la data cade nel periodo annuale in cui il tipo è attivo
    // gestisce anche il caso che il periodo valichi l'anno (es. 1-nov → 15-gen)
    public boolean nelPeriodo(LocalDate data) {
        MonthDay md = MonthDay.from(data);
        if (!inizioPeriodo.isAfter(finePeriodo)) {
            return !md.isBefore(inizioPeriodo) && !md.isAfter(finePeriodo);
        } else {
            // periodo a cavallo d'anno
            return !md.isBefore(inizioPeriodo) || !md.isAfter(finePeriodo);
        }
    }

    /*
     * Due tipi di visita si sovrappongono se:
     *   1. condividono almeno un giorno della settimana E
     *   2. i loro slot temporali si sovrappongono (anche parzialmente)
     *
     * Questa verifica serve quando si aggiunge un nuovo tipo di visita a un luogo
     * che ne ha già altri, per evitare conflitti di orario.
     */
    public boolean sovrapponeConAltro(TipoVisita altro) {
        boolean giornoInComune = giorniSettimana.stream().anyMatch(altro.giorniSettimana::contains);
        if (!giornoInComune) return false;

        LocalTime mioFine   = oraInizio.plusMinutes(durata);
        LocalTime suoFine   = altro.oraInizio.plusMinutes(altro.durata);
        return oraInizio.isBefore(suoFine) && altro.oraInizio.isBefore(mioFine);
    }

    // ---- getter ----

    public String getTitolo()            { return titolo; }
    public String getDescrizione()       { return descrizione; }
    public String getPuntoIncontro()     { return puntoIncontro; }
    public MonthDay getInizioPeriodo()   { return inizioPeriodo; }
    public MonthDay getFinePeriodo()     { return finePeriodo; }
    public Set<GiornoSettimana> getGiorni() { return giorniSettimana; }
    public LocalTime getOraInizio()      { return oraInizio; }
    public int getDurata()               { return durata; }
    public boolean vuoleBiglietto()      { return bigliettoRichiesto; }
    public int getMinPartecipanti()      { return minPartecipanti; }
    public int getMaxPartecipanti()      { return maxPartecipanti; }
    public List<Volontario> getVolontari() { return Collections.unmodifiableList(volontari); }

    @Override
    public String toString() {
        return titolo + " [" + oraInizio + ", " + durata + "min]";
    }
}
