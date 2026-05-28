package ui;

import model.GiornoSettimana;

import java.time.*;
import java.util.*;

/*
 * Metodi di supporto per leggere input dalla console.
 * Li raccogliamo qui per non ripetere la stessa logica di validazione in ogni menu.
 */
public class Console {

    private static final Scanner sc = new Scanner(System.in);

    private Console() {}

    // legge una stringa non vuota, continua a chiedere se l'utente preme invio
    public static String leggiStringa(String prompt) {
        String s;
        do {
            System.out.print(prompt);
            s = sc.nextLine().trim();
            if (s.isEmpty()) System.out.println("  [campo obbligatorio, riprova]");
        } while (s.isEmpty());
        return s;
    }

    // stringa opzionale: se l'utente preme invio restituisce null
    public static String leggiStringaOpt(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        return s.isEmpty() ? null : s;
    }

    // intero con valore minimo
    public static int leggiInt(String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min) return v;
                System.out.println("  [valore minimo: " + min + "]");
            } catch (NumberFormatException e) {
                System.out.println("  [inserisci un numero intero]");
            }
        }
    }

    // intero con minimo e massimo
    public static int leggiInt(String prompt, int min, int max) {
        while (true) {
            int v = leggiInt(prompt, min);
            if (v <= max) return v;
            System.out.println("  [valore massimo: " + max + "]");
        }
    }

    public static boolean leggiSiNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n): ");
            String r = sc.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sì")) return true;
            if (r.equals("n") || r.equals("no"))                    return false;
            System.out.println("  [digita s oppure n]");
        }
    }

    // orario formato HH:MM
    public static LocalTime leggiOra(String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:MM): ");
            try { return LocalTime.parse(sc.nextLine().trim()); }
            catch (Exception e) { System.out.println("  [formato HH:MM, es. 15:30]"); }
        }
    }

    // giorno/mese nel formato GG/MM
    public static MonthDay leggiGiornoMese(String prompt) {
        while (true) {
            System.out.print(prompt + " (GG/MM): ");
            try {
                String[] p = sc.nextLine().trim().split("/");
                return MonthDay.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            } catch (Exception e) { System.out.println("  [formato GG/MM, es. 01/04]"); }
        }
    }

    // data completa GG/MM/AAAA
    public static LocalDate leggiData(String prompt) {
        while (true) {
            System.out.print(prompt + " (GG/MM/AAAA): ");
            try {
                String[] p = sc.nextLine().trim().split("/");
                return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            } catch (Exception e) { System.out.println("  [formato GG/MM/AAAA, es. 25/12/2025]"); }
        }
    }

    // seleziona uno o più giorni della settimana
    public static Set<GiornoSettimana> leggiGiorni(String prompt) {
        GiornoSettimana[] tutti = GiornoSettimana.values();
        System.out.println(prompt);
        for (int i = 0; i < tutti.length; i++)
            System.out.println("  " + (i + 1) + ". " + tutti[i]);
        Set<GiornoSettimana> scelti = new LinkedHashSet<>();
        while (scelti.isEmpty()) {
            System.out.print("  Numeri separati da spazio (es. 6 7): ");
            for (String tok : sc.nextLine().trim().split("\\s+")) {
                try {
                    int idx = Integer.parseInt(tok) - 1;
                    if (idx >= 0 && idx < tutti.length) scelti.add(tutti[idx]);
                } catch (NumberFormatException ignored) {}
            }
            if (scelti.isEmpty()) System.out.println("  [seleziona almeno un giorno]");
        }
        return scelti;
    }

    public static void pausa() {
        System.out.print("\n  [premi INVIO per continuare]");
        sc.nextLine();
    }

    public static Scanner getScanner() { return sc; }
}
