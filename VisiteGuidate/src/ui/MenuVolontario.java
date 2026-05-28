package ui;

import controller.Controller;
import model.*;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

/*
 * Menu testuale per il volontario – Versione 2.
 * Il volontario può:
 *   - vedere i tipi di visita a cui è associato
 *   - dichiarare le proprie disponibilità per il mese successivo
 *   - vedere le visite confermate in cui è designato come guida (V4, già preparato)
 */
public class MenuVolontario {

    private final Controller ctrl;

    public MenuVolontario(Controller ctrl) {
        this.ctrl = ctrl;
    }

    // ---- accesso ----

    public Volontario login() {
        sep("ACCESSO VOLONTARIO");
        String nick = Console.leggiStringa("  Nickname: ");
        String pwd  = Console.leggiStringa("  Password: ");
        try {
            Volontario v = ctrl.loginVolontario(nick, pwd);
            System.out.println("  Benvenuto, " + v.getNickname() + "!");
            if (v.isPrimoAccesso()) forzaCambioPassword(v);
            return v;
        } catch (Exception e) {
            System.out.println("  Accesso negato: " + e.getMessage());
            return null;
        }
    }

    // operazione obbligatoria di cambio password al primo accesso
    private void forzaCambioPassword(Volontario v) {
        System.out.println("\n  [Primo accesso: scegli una nuova password]");
        while (true) {
            String p1 = Console.leggiStringa("  Nuova password: ");
            String p2 = Console.leggiStringa("  Conferma: ");
            if (!p1.equals(p2)) { System.out.println("  Non coincidono."); continue; }
            ctrl.cambiaPasswordVolontario(v, p1);
            System.out.println("  Password aggiornata.");
            break;
        }
    }

    // ---- menu principale ----

    public void menuPrincipale(Volontario v) {
        boolean esci = false;
        while (!esci) {
            LocalDate prossimo = LocalDate.now().plusMonths(1);
            String nomeMese = prossimo.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + prossimo.getYear();

            System.out.println("\n══════════════════════════════");
            System.out.println("  VOLONTARIO: " + v.getNickname());
            System.out.println("  Mese di riferimento: " + nomeMese);
            System.out.println("  1. Visualizza i miei tipi di visita");
            System.out.println("    2. Dichiara/modifica disponibilità per " + nomeMese);
            System.out.println("  3. Visualizza le mie disponibilità attuali");
            System.out.println("  4. Rimuovi una disponibilità");
    //v4 System.out.println("  5. Visite confermate in cui sono guida");
            System.out.println("  0. Esci");
            int s = Console.leggiInt("  Scelta: ", 0, 4);
            switch (s) {
                case 1 -> mostraTipiVisita(v);
                case 2 -> dichiaraDisponibilita(v, nomeMese);
                case 3 -> mostraDisponibilita(v, nomeMese);
                case 4 -> rimuoviDisponibilita(v);
        // case 5 -> mostraVisiteConfermate(v);
                case 0 -> esci = true;
            }
        }
    }

    // ---- operazioni ----

    private void mostraTipiVisita(Volontario v) {
        sep("I MIEI TIPI DI VISITA");
        List<TipoVisita> tipi = ctrl.getTipiVisitaDelVolontario(v);
        if (tipi.isEmpty()) {
            System.out.println("  Non sei ancora associato a nessun tipo di visita.");
        } else {
            for (TipoVisita tv : tipi) {
                System.out.println("\n  • " + tv.getTitolo());
                System.out.println("    Periodo: " + tv.getInizioPeriodo() + " → " + tv.getFinePeriodo());
                System.out.print("    Giorni:  ");
                tv.getGiorni().forEach(g -> System.out.print(g + " "));
                System.out.println();
                System.out.println("    Ora: " + tv.getOraInizio() + ", durata: " + tv.getDurata() + " min");
                System.out.println("    Partecipanti: min " + tv.getMinPartecipanti()
                    + ", max " + tv.getMaxPartecipanti());
                System.out.println("    Biglietto: " + (tv.vuoleBiglietto() ? "sì" : "no"));
            }
        }
        Console.pausa();
    }

    private void dichiaraDisponibilita(Volontario v, String nomeMese) {
        sep("DISPONIBILITÀ PER " + nomeMese.toUpperCase());
        System.out.println("  Inserisci le date in cui sei disponibile.");
        System.out.println("  Il sistema accetta solo date in cui hai tipi di visita programmabili.");
        System.out.println("  Digita 'fine' per smettere.\n");

        while (true) {
            System.out.print("  Data (GG/MM/AAAA) oppure 'fine': ");
            String input = Console.getScanner().nextLine().trim();
            if (input.equalsIgnoreCase("fine")) break;
            try {
                String[] p = input.split("/");
                LocalDate data = LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                ctrl.aggiungiDisponibilita(v, data);
                System.out.println("  ✓ " + data + " aggiunta.");
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                System.out.println("  [formato non valido, usa GG/MM/AAAA]");
            } catch (Exception e) {
                System.out.println("  Non aggiunta: " + e.getMessage());
            }
        }
        mostraDisponibilita(v, nomeMese);
    }

    private void mostraDisponibilita(Volontario v, String nomeMese) {
        sep("LE MIE DISPONIBILITÀ: " + nomeMese.toUpperCase());
        Set<LocalDate> disp = ctrl.getDisponibilita(v);
        if (disp.isEmpty()) {
            System.out.println("  Nessuna disponibilità dichiarata per " + nomeMese + ".");
        } else {
            disp.stream().sorted().forEach(d ->
                System.out.println("  ✓ " + d + " (" + GiornoSettimana.da(d.getDayOfWeek()) + ")")
            );
        }
        Console.pausa();
    }

    private void rimuoviDisponibilita(Volontario v) {
        sep("RIMUOVI DISPONIBILITÀ");
        LocalDate data = Console.leggiData("  Data da rimuovere");
        ctrl.rimuoviDisponibilita(v, data);
        System.out.println("  Rimossa (se era presente).");
        Console.pausa();
    }

    private void mostraVisiteConfermate(Volontario v) {
        sep("VISITE CONFERMATE  SONO GUIDA");
        List<Visita> visite = ctrl.getVisiteConfermate(v);
        if (visite.isEmpty()) {
            System.out.println("  Nessuna visita confermata al momento.");
        } else {
            for (Visita vis : visite) {
                System.out.println("\n  " + vis.getData()
                    + "  " + vis.getTipo().getTitolo());
                System.out.println("     Ora: " + vis.getTipo().getOraInizio());
                System.out.println("     Punto di incontro: " + vis.getTipo().getPuntoIncontro());
                System.out.println("     Iscritti: " + vis.totaleIscritti()
                    + "/" + vis.getTipo().getMaxPartecipanti());
                if (!vis.getIscrizioni().isEmpty()) {
                    System.out.println("     Prenotazioni:");
                    vis.getIscrizioni().forEach(i ->
                        System.out.println("       " + i.getCodice() + "  " + i.getNumeroPersone() + " pers."));
                }
            }
        }
        Console.pausa();
    }

    private static void sep(String titolo) {
        System.out.println("\n══ " + titolo + " " + "═".repeat(Math.max(0, 30 - titolo.length())));
    }
}
