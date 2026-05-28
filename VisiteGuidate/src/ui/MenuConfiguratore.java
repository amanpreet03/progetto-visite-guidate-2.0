package ui;

import controller.Controller;
import model.*;

import java.time.*;
import java.util.List;
import java.util.Set;

/*
 * Menu testuale per il configuratore – V1/V2/V3.
 * Tutta la logica è delegata al Controller.
 */
public class MenuConfiguratore {

    private final Controller ctrl;

    public MenuConfiguratore(Controller ctrl) {
        this.ctrl = ctrl;
    }

    // ---- accesso ----

    public Configuratore login() {
        sep("ACCESSO CONFIGURATORE");
        String usr = Console.leggiStringa("  Username: ");
        String pwd = Console.leggiStringa("  Password: ");
        try {
            Configuratore c = ctrl.loginConfiguratore(usr, pwd);
            System.out.println("  Benvenuto, " + c.getUsername() + "!");
            if (c.isPrimoAccesso()) forzaCambioPassword(c);
            return c;
        } catch (Exception e) {
            System.out.println("  Accesso negato: " + e.getMessage());
            return null;
        }
    }

    public void registrazione() {
        sep("REGISTRAZIONE NUOVO CONFIGURATORE");
        System.out.println("  Inserisci le credenziali predefinite ricevute dall'organizzazione.");
        String credUsr = Console.leggiStringa("  Username predefinito: ");
        String credPwd = Console.leggiStringa("  Password predefinita: ");
        System.out.println("\n  Ora scegli le tue credenziali personali:");
        String nuovoUsr = Console.leggiStringa("  Nuovo username: ");
        String nuovaPwd = Console.leggiStringa("  Nuova password: ");
        try {
            ctrl.registraConfiguratore(credUsr, credPwd, nuovoUsr, nuovaPwd);
            System.out.println("  Registrazione completata.");
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
        Console.pausa();
    }

    private void forzaCambioPassword(Configuratore c) {
        System.out.println("\n  [Primo accesso – scegli una nuova password]");
        while (true) {
            String p1 = Console.leggiStringa("  Nuova password: ");
            String p2 = Console.leggiStringa("  Conferma: ");
            if (!p1.equals(p2)) { System.out.println("  Non coincidono."); continue; }
            ctrl.cambiaPasswordConfiguratore(c, p1);
            System.out.println("  Password aggiornata.");
            break;
        }
    }

    // ---- menu principale ----

    public void menuPrincipale(Configuratore c) {
        boolean esci = false;
        while (!esci) {
            System.out.println("\n══════════════════════════════");
            System.out.println("  CONFIGURATORE: " + c.getUsername());

            if (!ctrl.ambitoImpostato()) {
                System.out.println("  [!] Sistema non ancora inizializzato");
                System.out.println("  1. Inizializza sistema   0. Esci");
                int s = Console.leggiInt("  Scelta: ", 0, 1);
                if (s == 1) inizializza(); else esci = true;
                continue;
            }

            System.out.println("  Ambito: " + ctrl.getAmbito());
            //System.out.println("  Fase:   " + ctrl.getFase());
            //if (ctrl.getMeseRaccolta() > 0)
            //    System.out.println("  Raccolta: " + ctrl.getMeseRaccolta() + "/" + ctrl.getAnnoRaccolta());
            System.out.println();
            System.out.println("  ── Dati ───────────────────────────────");
            System.out.println("  1. Aggiungi luogo");
            System.out.println("  2. Aggiungi tipo di visita a luogo esistente");
            System.out.println("  3. Aggiungi volontario a tipo di visita");
            System.out.println("  4. Inserisci nuovo volontario");
        //    System.out.println("  ── V3 – Rimozioni ─────────────────────");
        //    System.out.println("  5. Rimuovi luogo");
        //    System.out.println("  6. Rimuovi tipo di visita");
        //    System.out.println("  7. Rimuovi volontario");
        //    System.out.println("  ── V3 – Ciclo mensile ─────────────────");
        //    System.out.println("  8. Chiudi raccolta disponibilità");
        //    System.out.println("  9. Genera piano visite");
        //    System.out.println("  10. Apri nuova raccolta disponibilità");
            System.out.println("  ── Visualizzazione ────────────────────");
            System.out.println("  5. Visualizza luoghi");
            System.out.println("  6. Visualizza volontari");
            System.out.println("  7. Visualizza visite");
            System.out.println("  8. Date precluse");
            System.out.println("  9. Modifica max persone per iscrizione");
            System.out.println("  0. Esci");

            int s = Console.leggiInt("  Scelta: ", 0, 15);
            switch (s) {
                case 1  -> aggiungiLuogo();
                case 2  -> aggiungiTipoVisita();
                case 3  -> aggiungiVolontarioATipo();
                case 4  -> inserisciVolontario();
               // case 5  -> rimuoviLuogo();
               // case 6  -> rimuoviTipoVisita();
               // case 7  -> rimuoviVolontario();
               // case 8  -> chiudiRaccolta();
               // case 9  -> generaPiano();
               //case 10 -> apriRaccolta();
                case 5  -> mostraLuoghi();
                case 6  -> mostraVolontari();
                case 7  -> mostraVisite();
                case 8  -> gestionePrecluse();
                case 9  -> modificaMaxPersone();
                case 0  -> esci = true;
            }
        }
    }

    // ---- inizializzazione ----

    private void inizializza() {
        sep("INIZIALIZZAZIONE SISTEMA");
        String ambito = Console.leggiStringa("  Ambito territoriale: ");
        int max = Console.leggiInt("  Max persone per iscrizione: ", 1);
        try {
            ctrl.inizializza(ambito, max);
            System.out.println("  Sistema inizializzato.");
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
        Console.pausa();
    }

    // ---- dati ----

    private void aggiungiLuogo() {
        sep("AGGIUNGI LUOGO");
        String nome  = Console.leggiStringa("  Nome luogo: ");
        String descr = Console.leggiStringaOpt("  Descrizione (INVIO per saltare): ");
        String coll  = Console.leggiStringa("  Collocazione: ");
        try {
            Luogo l = ctrl.creaLuogo(nome, descr, coll);
            do {
                TipoVisita tv = raccogliTipoVisita();
                aggiungiVolontariATipo(tv);
                try { l.aggiungiTipoVisita(tv); System.out.println("  Tipo aggiunto."); }
                catch (Exception e) { System.out.println("  Tipo non aggiunto: " + e.getMessage()); }
            } while (Console.leggiSiNo("  Aggiungere un altro tipo di visita?"));
            ctrl.salvaLuogo(l);
            System.out.println("  Luogo salvato.");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void aggiungiTipoVisita() {
        sep("AGGIUNGI TIPO DI VISITA");
        mostraLuoghiBreve();
        String nomeLuogo = Console.leggiStringa("  Nome luogo: ");
        try {
            Luogo l = ctrl.ottieniLuogo(nomeLuogo);
            TipoVisita tv = raccogliTipoVisita();
            aggiungiVolontariATipo(tv);
            ctrl.aggiungiTipoVisita(l, tv);
            System.out.println("  Tipo di visita aggiunto.");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void aggiungiVolontarioATipo() {
        sep("AGGIUNGI VOLONTARIO A TIPO DI VISITA");
        mostraLuoghiBreve();
        String nomeLuogo = Console.leggiStringa("  Nome luogo: ");
        try {
            Luogo l = ctrl.ottieniLuogo(nomeLuogo);
            l.getTipiVisita().forEach(tv -> System.out.println("    – " + tv.getTitolo()));
            String titolo = Console.leggiStringa("  Titolo tipo: ");
            TipoVisita tv = l.getTipiVisita().stream()
                .filter(t -> t.getTitolo().equalsIgnoreCase(titolo)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo non trovato."));
            ctrl.collegaVolontario(Console.leggiStringa("  Nickname volontario: "), tv);
            System.out.println("  Volontario aggiunto.");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void inserisciVolontario() {
        sep("INSERISCI VOLONTARIO");
        try {
            ctrl.creaVolontario(Console.leggiStringa("  Nickname: "), Console.leggiStringa("  Password iniziale: "));
            System.out.println("  Volontario aggiunto.");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    /*  ---- rimozioni V3 ----

    private void rimuoviLuogo() {
        sep("RIMUOVI LUOGO");
        mostraLuoghiBreve();
        String nome = Console.leggiStringa("  Nome luogo da rimuovere: ");
        if (!Console.leggiSiNo("  Sicuro? Verranno rimossi anche tutti i tipi di visita associati")) {
            System.out.println("  Operazione annullata."); Console.pausa(); return;
        }
        try {
            ctrl.rimuoviLuogo(nome);
            System.out.println("  Luogo rimosso (con eventuali volontari rimasti senza tipi).");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void rimuoviTipoVisita() {
        sep("RIMUOVI TIPO DI VISITA");
        mostraLuoghiBreve();
        String nomeLuogo = Console.leggiStringa("  Nome luogo: ");
        try {
            Luogo l = ctrl.ottieniLuogo(nomeLuogo);
            l.getTipiVisita().forEach(tv -> System.out.println("    – " + tv.getTitolo()));
            String titolo = Console.leggiStringa("  Titolo tipo da rimuovere: ");
            if (!Console.leggiSiNo("  Confermi la rimozione?")) {
                System.out.println("  Annullato."); Console.pausa(); return;
            }
            ctrl.rimuoviTipoVisita(nomeLuogo, titolo);
            System.out.println("  Tipo rimosso (cascata applicata).");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void rimuoviVolontario() {
        sep("RIMUOVI VOLONTARIO");
        ctrl.getVolontari().forEach(v -> System.out.println("    – " + v.getNickname()));
        String nick = Console.leggiStringa("  Nickname da rimuovere: ");
        if (!Console.leggiSiNo("  Sicuro? Possono venire rimossi anche tipi di visita e luoghi")) {
            System.out.println("  Annullato."); Console.pausa(); return;
        }
        try {
            ctrl.rimuoviVolontario(nick);
            System.out.println("  Volontario rimosso (cascata applicata).");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    // ---- ciclo mensile V3 ----

    private void chiudiRaccolta() {
        sep("CHIUDI RACCOLTA DISPONIBILITÀ");
        try {
            ctrl.chiudiRaccoltaDisponibilita();
            System.out.println("  Raccolta chiusa. Ora puoi generare il piano.");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void generaPiano() {
        sep("GENERA PIANO VISITE");
        try {
            List<Visita> nuove = ctrl.generaPianoVisite();
            System.out.println("  Piano generato: " + nuove.size() + " visite proposte.");
            nuove.forEach(v -> System.out.println(
                "    " + v.getData() + " – " + v.getTipo().getTitolo()
                + " (guida: " + v.getGuida().getNickname() + ")"));
            System.out.println("\n  Ora puoi inserire modifiche ai dati (opzionale).");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }

    private void apriRaccolta() {
        sep("APRI NUOVA RACCOLTA DISPONIBILITÀ");
        try {
            ctrl.apriNuovaRaccolta();
            System.out.println("  Raccolta aperta per il mese "
                + ctrl.getMeseRaccolta() + "/" + ctrl.getAnnoRaccolta() + ".");
        } catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        Console.pausa();
    }
    */
    // ---- visualizzazione ----

    private void mostraLuoghi() {
        sep("LUOGHI VISITABILI");
        List<Luogo> lista = ctrl.getLuoghi();
        if (lista.isEmpty()) { System.out.println("  Nessun luogo."); Console.pausa(); return; }
        for (Luogo l : lista) {
            System.out.println("\n  📍 " + l.getNome() + "  " + l.getCollocazione());
            if (l.getDescrizione() != null) System.out.println("     " + l.getDescrizione());
            for (TipoVisita tv : l.getTipiVisita()) {
                System.out.println("     • " + tv.getTitolo()
                    + " [" + tv.getOraInizio() + ", " + tv.getDurata() + "min"
                    + ", min=" + tv.getMinPartecipanti() + " max=" + tv.getMaxPartecipanti()
                    + (tv.vuoleBiglietto() ? ", biglietto" : "") + "]");
                System.out.print("       Giorni: ");
                tv.getGiorni().forEach(g -> System.out.print(g + " "));
                System.out.println();
                System.out.print("       Volontari: ");
                tv.getVolontari().forEach(v -> System.out.print(v.getNickname() + " "));
                System.out.println();
            }
        }
        Console.pausa();
    }

    private void mostraVolontari() {
        sep("VOLONTARI");
        List<Volontario> lista = ctrl.getVolontari();
        if (lista.isEmpty()) { System.out.println("  Nessun volontario."); Console.pausa(); return; }
        for (Volontario v : lista) {
            System.out.println("\n  (•) " + v.getNickname());
            ctrl.getTipiVisitaDelVolontario(v)
                .forEach(tv -> System.out.println(" " + tv.getTitolo()));
        }
        Console.pausa();
    }

    private void mostraVisite() {
        sep("VISITE");
        System.out.println("  1.Proposte  2.Complete  3.Confermate  4.Cancellate  5.Archivio  0.Indietro");
        int s = Console.leggiInt("  Scelta: ", 0, 5);
        if (s == 0) return;
        StatoVisita stato = switch (s) {
            case 1 -> StatoVisita.PROPOSTA; case 2 -> StatoVisita.COMPLETA;
            case 3 -> StatoVisita.CONFERMATA; case 4 -> StatoVisita.CANCELLATA;
            default -> StatoVisita.EFFETTUATA;
        };
        List<Visita> visite = ctrl.getVisitePerStato(stato);
        if (visite.isEmpty()) System.out.println("  Nessuna visita.");
        else visite.forEach(v -> System.out.println("  " + v.getData()
            + "  " + v.getTipo().getTitolo()
            + " | guida: " + v.getGuida().getNickname()
            + " | iscritti: " + v.totaleIscritti() + "/" + v.getTipo().getMaxPartecipanti()));
        Console.pausa();
    }

    private void gestionePrecluse() {
        sep("DATE PRECLUSE");
        System.out.println("  1.Aggiungi  2.Visualizza  0.Indietro");
        int s = Console.leggiInt("  Scelta: ", 0, 2);
        if (s == 0) return;
        if (s == 1) {
            LocalDate d = Console.leggiData("  Data da escludere");
            ctrl.aggiungiDataPreclusa(d);
            System.out.println("  Preclusa: " + d);
        } else {
            LocalDate p = LocalDate.now().plusMonths(1);
            Set<LocalDate> pr = ctrl.getDatePrecluse(p.getYear(), p.getMonthValue());
            System.out.println("  Precluse per " + p.getMonth() + " " + p.getYear() + ":");
            if (pr.isEmpty()) System.out.println("  (nessuna)");
            else pr.stream().sorted().forEach(d -> System.out.println("    " + d));
        }
        Console.pausa();
    }

    private void modificaMaxPersone() {
        sep("MAX PERSONE PER ISCRIZIONE");
        System.out.println("  Attuale: " + ctrl.getMaxPersone());
        ctrl.setMaxPersone(Console.leggiInt("  Nuovo valore: ", 1));
        System.out.println("  Aggiornato.");
        Console.pausa();
    }

    // ---- helpers ----

    private TipoVisita raccogliTipoVisita() {
        System.out.println("\n  -- Dati tipo di visita --");
        String titolo     = Console.leggiStringa("  Titolo: ");
        String descr      = Console.leggiStringa("  Descrizione: ");
        String punto      = Console.leggiStringa("  Punto di incontro: ");
        MonthDay inizio   = Console.leggiGiornoMese("  Inizio periodo");
        MonthDay fine     = Console.leggiGiornoMese("  Fine periodo");
        var giorni        = Console.leggiGiorni("  Giorni programmabili:");
        LocalTime ora     = Console.leggiOra("  Ora di inizio");
        int durata        = Console.leggiInt("  Durata (minuti): ", 1);
        boolean big       = Console.leggiSiNo("  Biglietto richiesto?");
        int minP          = Console.leggiInt("  Min partecipanti: ", 1);
        int maxP          = Console.leggiInt("  Max partecipanti (>= " + minP + "): ", minP);
        return new TipoVisita(titolo, descr, punto, inizio, fine, giorni, ora, durata, big, minP, maxP);
    }

    private void aggiungiVolontariATipo(TipoVisita tv) {
        System.out.println("  Aggiungi almeno un volontario:");
        do {
            String nick = Console.leggiStringa("  Nickname: ");
            try { ctrl.collegaVolontario(nick, tv); System.out.println("  Aggiunto."); }
            catch (Exception e) { System.out.println("  Errore: " + e.getMessage()); }
        } while (!tv.hasVolontari() || Console.leggiSiNo("  Aggiungere un altro volontario?"));
    }

    private void mostraLuoghiBreve() {
        ctrl.getLuoghi().forEach(l -> System.out.println("     " + l.getNome()));
    }

    private static void sep(String t) {
        System.out.println("\n══ " + t + " " + "═".repeat(Math.max(0, 30 - t.length())));
    }
}
