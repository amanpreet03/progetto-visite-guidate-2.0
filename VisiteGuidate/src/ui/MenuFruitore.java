package ui;

import controller.Controller;
import model.*;

import java.util.List;

/*
 * Menu testuale per il fruitore – Versione 4.
 * Il fruitore può vedere le visite, iscriversi e disdire.
*/
public class MenuFruitore {

    private final Controller ctrl;

    public MenuFruitore(Controller ctrl) {
        this.ctrl = ctrl;
    }

    // ---- accesso e registrazione ----

    public Fruitore login() {
        sep("ACCESSO FRUITORE");
        
        String usr = Console.leggiStringa("  Username: ");
        String pwd = Console.leggiStringa("  Password: ");
        
        try {
            Fruitore f = ctrl.loginFruitore(usr, pwd);
            System.out.println("  Benvenuto, " + f.getUsername() + "!");
            return f;
        } catch (Exception e) {
            System.out.println("  Accesso negato: " + e.getMessage());
            return null;
        }
    }

    public void registrazione() {
        sep("REGISTRAZIONE FRUITORE");
        
        System.out.println("  Scegli le tue credenziali per accedere al portale visite.");
        String usr = Console.leggiStringa("  Username: ");
        String pwd = Console.leggiStringa("  Password: ");
        String pwd2 = Console.leggiStringa("  Conferma password: ");
        
        if (!pwd.equals(pwd2)) {
            System.out.println("  Le password non coincidono. Riprova.");
            Console.pausa(); return;
        }
        try {
            ctrl.registraFruitore(usr, pwd);
            System.out.println("  Registrazione completata! Ora puoi accedere.");
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
        Console.pausa();
    }

    // ---- menu principale ----

    public void menuPrincipale(Fruitore f) {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n══════════════════════════════");
            System.out.println("  FRUITORE: " + f.getUsername());
            System.out.println("  1. Visualizza visite disponibili");
            System.out.println("  2. Iscriviti a una visita");
            System.out.println("  3. Le mie prenotazioni");
            System.out.println("  4. Disdici una prenotazione");
            System.out.println("  0. Esci");
            int s = Console.leggiInt("  Scelta: ", 0, 4);
            switch (s) {
                case 1 -> visualizzaVisite();
                case 2 -> iscrivitiAVisita(f);
                case 3 -> miePrenotazioni(f);
                case 4 -> disdiciPrenotazione(f);
                case 0 -> esci = true;
            }
        }
    }

    // ---- operazioni ----

    /*
     * Mostra tutte le visite visibili al fruitore:
     * proposte e confermate con dettaglio completo,
     * cancellate con solo titolo e data.
     */
    private void visualizzaVisite() {
        sep("VISITE DISPONIBILI");
        List<Visita> visite = ctrl.getVisiteFruitore();
        if (visite.isEmpty()) {
            System.out.println("  Nessuna visita disponibile al momento.");
            Console.pausa(); return;
        }
        for (Visita v : visite) {
            System.out.println();
            if (v.getStato() == StatoVisita.CANCELLATA) {
                // per le cancellate mostriamo solo titolo e data
                System.out.println("  ✗ [CANCELLATA] " + v.getData()
                    + " – " + v.getTipo().getTitolo());
            } else {
                String stato = v.getStato() == StatoVisita.CONFERMATA ? "✓ CONFERMATA" : "● PROPOSTA";
                System.out.println("  " + stato + " | " + v.getData()
                    + " – " + v.getTipo().getTitolo());
                System.out.println("    📍 Punto di incontro: " + v.getTipo().getPuntoIncontro());
                System.out.println("    🕐 Ora: " + v.getTipo().getOraInizio()
                    + " (durata " + v.getTipo().getDurata() + " min)");
                System.out.println("    📋 " + v.getTipo().getDescrizione());
                System.out.println("    🎫 Biglietto: " + (v.getTipo().vuoleBiglietto() ? "sì" : "no"));
                if (v.getStato() == StatoVisita.PROPOSTA)
                    System.out.println("    👥 Posti liberi: " + v.postiLiberi()
                        + "/" + v.getTipo().getMaxPartecipanti());
            }
        }
        Console.pausa();
    }

    /*
     * Il fruitore sceglie una visita proposta e indica quante persone vuole iscrivere.
     * Post: viene rilasciato il codice di prenotazione
     */ 
    private void iscrivitiAVisita(Fruitore f) {
        sep("ISCRIVITI A UNA VISITA");
        List<Visita> proposte = ctrl.getVisiteProposte();
        if (proposte.isEmpty()) {
            System.out.println("  Nessuna visita proposta al momento.");
            Console.pausa(); return;
        }

        // mostra elenco numerato
        for (int i = 0; i < proposte.size(); i++) {
            Visita v = proposte.get(i);
            System.out.println("  " + (i + 1) + ". " + v.getData()
                + " – " + v.getTipo().getTitolo()
                + " (posti: " + v.postiLiberi() + ")");
        }
        System.out.println("  0. Annulla");

        int idx = Console.leggiInt("  Scegli visita: ", 0, proposte.size());
        if (idx == 0) return;

        Visita scelta = proposte.get(idx - 1);
        System.out.println("\n  Visita scelta: " + scelta.getTipo().getTitolo());
        System.out.println("  Data: " + scelta.getData());
        System.out.println("  Max persone per iscrizione: " + ctrl.getMaxPersone());
        System.out.println("  Posti disponibili: " + scelta.postiLiberi());

        int persone = Console.leggiInt("  Quante persone (incluso te)? ", 1, ctrl.getMaxPersone());
        try {
            String codice = ctrl.iscriviAVisita(f, scelta, persone);
            System.out.println("\n  ✓ Iscrizione confermata!");
            System.out.println("  Il tuo codice di prenotazione è: " + codice);
            System.out.println("  Conservalo: ti servirà per disdire o per presentarti alla visita.");
        } catch (Exception e) {
            System.out.println("  Iscrizione non riuscita: " + e.getMessage());
        }
        Console.pausa();
    }

    
    // Mostra le prenotazioni attive del fruitore (proposte, confermate, cancellate).
     
    private void miePrenotazioni(Fruitore f) {
        sep("LE MIE PRENOTAZIONI");
        List<Visita> mie = ctrl.getMieIscrizioni(f);
        if (mie.isEmpty()) {
            System.out.println("  Nessuna prenotazione attiva.");
            Console.pausa(); return;
        }
        for (Visita v : mie) { // trovo le iscrizioni di questo fruitore in questa visita
            v.getIscrizioni().stream()
                .filter(i -> i.getUsernameIscritto().equalsIgnoreCase(f.getUsername()))
                .forEach(i -> {
                    System.out.println("\n  Codice: " + i.getCodice()
                        + " | " + v.getData() + " – " + v.getTipo().getTitolo()
                        + " | " + i.getNumeroPersone() + " pers."
                        + " | stato: " + v.getStato());
                    if (v.getStato() == StatoVisita.CONFERMATA || v.getStato() == StatoVisita.PROPOSTA) {
                        System.out.println("    📍 " + v.getTipo().getPuntoIncontro()
                            + " ore " + v.getTipo().getOraInizio());
                    }
                });
        }
        Console.pausa();
    }

    /*
     * Il fruitore disdice una prenotazione indicando il codice.
     * Si può disdire solo se la visita è ancora PROPOSTA (o COMPLETA).
    */
    private void disdiciPrenotazione(Fruitore f) {
        sep("DISDICI PRENOTAZIONE");
        List<Visita> mie = ctrl.getMieIscrizioni(f);

        // mostra solo le disdiribili (proposta o completa)
        List<Visita> disdiribili = mie.stream()
            .filter(v -> v.getStato() == StatoVisita.PROPOSTA || v.getStato() == StatoVisita.COMPLETA)
            .toList();

        if (disdiribili.isEmpty()) {
            System.out.println("  Non hai prenotazioni disdiribili al momento.");
            Console.pausa(); return;
        }

        System.out.println("  Prenotazioni disdiribili:");
        disdiribili.forEach(v ->
            v.getIscrizioni().stream()
                .filter(i -> i.getUsernameIscritto().equalsIgnoreCase(f.getUsername()))
                .forEach(i -> System.out.println("    " + i.getCodice()
                    + " – " + v.getData() + " – " + v.getTipo().getTitolo())));

        String codice = Console.leggiStringa("  Inserisci il codice da disdire (0 per annullare): ");
        if (codice.equals("0")) return;

        // trova la visita che contiene quel codice
        Visita target = disdiribili.stream()
            .filter(v -> v.cercaIscrizione(codice).isPresent())
            .findFirst().orElse(null);

        if (target == null) {
            System.out.println("  Codice non trovato tra le tue prenotazioni.");
            Console.pausa(); return;
        }

        try {
            ctrl.disdiciIscrizione(f, target, codice);
            System.out.println("  ✓ Prenotazione disdetta.");
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
        Console.pausa();
    }

    private static void sep(String t) {
        System.out.println("\n══ " + t + " " + "═".repeat(Math.max(0, 30 - t.length())));
    }
}
