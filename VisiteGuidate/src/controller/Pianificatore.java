package controller;

import model.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/*
 * Genera l'elenco delle visite proponibili per un dato mese.
 *
 * Approccio: per ogni giorno del mese, scorre tutti i tipi di visita
 * programmabili in quel giorno e cerca un volontario disponibile
 * che non sia già stato assegnato quel giorno a un'altra visita.
 *
 * Vincoli rispettati (dalla spec):
 *   - al massimo una visita per tipo per giorno
 *   - al massimo una visita per volontario per giorno
 *   - la data non deve essere preclusa
 *   - il tipo di visita deve essere programmabile nel giorno della settimana
 *   - il tipo di visita deve essere nel suo periodo annuale
 *   - deve esserci almeno un volontario disponibile quel giorno per quel tipo
 *
 * L'obiettivo è ottenere un numero elevato di visite (non necessariamente
 * il massimo assoluto — la spec lo dice esplicitamente).
 * Un algoritmo greedy è sufficiente ai fini del progetto.
 */
public class Pianificatore {

    private Pianificatore() {}

    /*
     * Genera le visite per il mese indicato.
     *
     * Pre:  luoghi != null, anno/mese validi
     * Post: restituisce una lista (possibilmente vuota) di Visita in stato PROPOSTA
     */
    public static List<Visita> generaPiano(
            List<Luogo> luoghi,
            int anno, int mese,
            Set<LocalDate> datePrecluse) {

        List<Visita> piano = new ArrayList<>();
        YearMonth ym = YearMonth.of(anno, mese);

        // scorriamo giorno per giorno il mese
        for (int giorno = 1; giorno <= ym.lengthOfMonth(); giorno++) {
            LocalDate data = LocalDate.of(anno, mese, giorno);

            // salta le date precluse
            if (datePrecluse.contains(data)) continue;

            GiornoSettimana gdS = GiornoSettimana.da(data.getDayOfWeek());

            // volontari già impegnati in questa data (max una visita al giorno per volontario)
            Set<Volontario> impegnatiOggi = new HashSet<>();

            for (Luogo luogo : luoghi) {
                for (TipoVisita tv : luogo.getTipiVisita()) {

                    // il tipo deve essere attivo questo giorno della settimana
                    if (!tv.programmabileIl(gdS)) continue;

                    // e deve essere nel suo periodo annuale
                    if (!tv.nelPeriodo(data)) continue;

                    // cerca il primo volontario disponibile e non già impegnato oggi
                    Volontario guida = trovaGuida(tv, data, impegnatiOggi);
                    if (guida == null) continue;  // nessuno disponibile, saltiamo

                    piano.add(new Visita(tv, data, guida));
                    impegnatiOggi.add(guida);
                    // (al massimo una visita per tipo per giorno: il for sui tipi non torna indietro)
                }
            }
        }
        return piano;
    }

    // cerca un volontario disponibile per questo tipo di visita in questa data,
    // escludendo chi è già stato assegnato a un'altra visita nello stesso giorno
    private static Volontario trovaGuida(TipoVisita tv, LocalDate data, Set<Volontario> impegnati) {
        for (Volontario v : tv.getVolontari()) {
            if (!impegnati.contains(v) && v.isDisponibileIn(data)) return v;
        }
        return null;
    }
}
