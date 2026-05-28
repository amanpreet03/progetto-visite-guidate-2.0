package model;

import java.io.Serializable;
import java.util.*;

/*
 * Un luogo visitabile all'interno dell'ambito territoriale dell'organizzazione.
 * Si assume che l'organizzazione abbia già verificato l'assenza di barriere
 * architettoniche prima di inserire il luogo nel sistema.
 *
 * Invariante: nome != null, collocazione != null, tipiVisita != null
 */
public class Luogo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nome;         // identificatore univoco
    private final String descrizione;  // testo aggiuntivo, può essere null
    private final String collocazione; // indirizzo o coordinate

    private final List<TipoVisita> tipiVisita = new ArrayList<>();

    public Luogo(String nome, String descrizione, String collocazione) {
        this.nome = nome.trim();
        this.descrizione = (descrizione != null && !descrizione.isBlank()) ? descrizione.trim() : null;
        this.collocazione = collocazione.trim();
    }

    /*
     * Aggiunge un tipo di visita al luogo.
     * Lancia eccezione se c'è già un tipo con lo stesso titolo
     * o se l'orario si sovrappone con un tipo esistente.
     *
     * Pre:  tv != null
     * Post: tipiVisita contiene tv
     */
    public void aggiungiTipoVisita(TipoVisita tv) {
        for (TipoVisita esistente : tipiVisita) {
            if (esistente.getTitolo().equalsIgnoreCase(tv.getTitolo()))
                throw new IllegalArgumentException("Titolo già usato per questo luogo: " + tv.getTitolo());
            if (esistente.sovrapponeConAltro(tv))
                throw new IllegalArgumentException(
                    "Sovrapposizione orario con '" + esistente.getTitolo() + "'");
        }
        tipiVisita.add(tv);
    }

    public boolean rimuoviTipoVisita(String titolo) {
        return tipiVisita.removeIf(t -> t.getTitolo().equalsIgnoreCase(titolo));
    }

    public boolean hasTipiVisita() { return !tipiVisita.isEmpty(); }

    public String getNome()          { return nome; }
    public String getDescrizione()   { return descrizione; }
    public String getCollocazione()  { return collocazione; }
    public List<TipoVisita> getTipiVisita() { return Collections.unmodifiableList(tipiVisita); }

    @Override
    public String toString() { return nome; }
}
