package model;

import java.io.Serializable;
import java.util.UUID;

/*
 * Rappresenta la prenotazione di un fruitore per una visita.
 * Usata dalla versione 4, ma la classe è già definita qui per coerenza del modello.
 */
public class Iscrizione implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codice;          // codice univoco rilasciato al momento
    private final String usernameIscritto;
    private final int numeroPersone;      // quante persone include questa prenotazione

    public Iscrizione(String usernameIscritto, int numeroPersone) {
        // prendiamo solo i primi 8 caratteri dell'UUID per renderlo più gestibile
        this.codice = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.usernameIscritto = usernameIscritto;
        this.numeroPersone = numeroPersone;
    }

    public String getCodice()             { return codice; }
    public String getUsernameIscritto()   { return usernameIscritto; }
    public int getNumeroPersone()         { return numeroPersone; }

    @Override
    public String toString() {
        return "Prenotazione " + codice + " (" + usernameIscritto + ", " + numeroPersone + " pers.)";
    }
}
