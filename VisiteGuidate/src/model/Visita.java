package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/*
 * Una visita è un'istanza concreta di un TipoVisita su una data specifica.
 * Tiene traccia dello stato corrente e delle iscrizioni.
 *
 * Ciclo di vita:
 *   PROPOSTA <--> COMPLETA  (prima della chiusura iscrizioni)
 *   PROPOSTA/COMPLETA --> CONFERMATA/CANCELLATA  (alla chiusura, 3 gg prima)
 *   CONFERMATA --> EFFETTUATA  (giorno dopo lo svolgimento)
 *   CANCELLATA --> (rimossa dal sistema)
 */
public class Visita implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TipoVisita tipo;
    private final LocalDate data;
    private final Volontario guida;
    private StatoVisita stato;
    private final List<Iscrizione> iscrizioni = new ArrayList<>();

    public Visita(TipoVisita tipo, LocalDate data, Volontario guida) {
        this.tipo  = tipo;
        this.data  = data;
        this.guida = guida;
        this.stato = StatoVisita.PROPOSTA;
    }

    // ---- iscrizioni ----

    public int totaleIscritti() {
        return iscrizioni.stream().mapToInt(Iscrizione::getNumeroPersone).sum();
    }

    /*
     * Aggiunge un'iscrizione.
     * Pre:  stato == PROPOSTA e c'è abbastanza posto
     * Post: se il massimo è raggiunto, passa a COMPLETA
     */
    public void aggiungiIscrizione(Iscrizione i) {
        if (stato != StatoVisita.PROPOSTA)
            throw new IllegalStateException("Iscrizioni chiuse o visita non più disponibile.");
        if (totaleIscritti() + i.getNumeroPersone() > tipo.getMaxPartecipanti())
            throw new IllegalStateException("Posti insufficienti.");
        iscrizioni.add(i);
        if (totaleIscritti() >= tipo.getMaxPartecipanti())
            stato = StatoVisita.COMPLETA;
    }

    /*
     * Disdice una prenotazione tramite codice.
     * Se la visita era COMPLETA torna PROPOSTA (si è liberato un posto).
     */
    public Iscrizione rimuoviIscrizione(String codice) {
        Iscrizione trovata = iscrizioni.stream()
            .filter(i -> i.getCodice().equalsIgnoreCase(codice))
            .findFirst().orElse(null);
        if (trovata != null) {
            iscrizioni.remove(trovata);
            if (stato == StatoVisita.COMPLETA) stato = StatoVisita.PROPOSTA;
        }
        return trovata;
    }

    public Optional<Iscrizione> cercaIscrizione(String codice) {
        return iscrizioni.stream().filter(i -> i.getCodice().equalsIgnoreCase(codice)).findFirst();
    }

    // ---- transizioni di stato ----

    // chiamata 3 giorni prima della data di svolgimento
    public void chiudiIscrizioni() {
        if (totaleIscritti() >= tipo.getMinPartecipanti())
            stato = StatoVisita.CONFERMATA;
        else
            stato = StatoVisita.CANCELLATA;
    }

    // chiamata il giorno dopo lo svolgimento
    public void segnaEffettuata() {
        stato = StatoVisita.EFFETTUATA;
    }

    // ---- getter ----

    public TipoVisita getTipo()       { return tipo; }
    public LocalDate getData()        { return data; }
    public Volontario getGuida()      { return guida; }
    public StatoVisita getStato()     { return stato; }
    public int postiLiberi()          { return tipo.getMaxPartecipanti() - totaleIscritti(); }
    public List<Iscrizione> getIscrizioni() { return Collections.unmodifiableList(iscrizioni); }

    @Override
    public String toString() {
        return data + " – " + tipo.getTitolo() + " [" + stato + "]";
    }
}
