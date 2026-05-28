package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/*
 * Rappresenta una guida volontaria.
 * Il nickname è anche lo username per accedere all'app.
 * 
 * Invariante: nickname != null, passwordHash != null
 */
public class Volontario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nickname;
    private String passwordHash;
    private boolean primoAccesso; // true finché non cambia la password

    // disponibilità dichiarate: chiave = "YYYY-MM", valore = date in cui è libero
    private final Map<String, Set<LocalDate>> disponibilita = new HashMap<>();

    public Volontario(String nickname, String passwordIniziale) {
        this.nickname = nickname.trim();
        this.passwordHash = cifra(passwordIniziale);
        this.primoAccesso = true;
    }

    // ---- autenticazione ----

    public boolean passwordCorretta(String tentativo) {
        return passwordHash.equals(cifra(tentativo));
    }

    public void cambiaPassword(String nuova) {
        this.passwordHash = cifra(nuova);
        this.primoAccesso = false;
    }

    public boolean isPrimoAccesso() { return primoAccesso; }

    // ---- disponibilità ----

    // il volontario dichiara che è disponibile in questa data
    public void aggiungiDisponibilita(LocalDate data) {
        String chiave = chiave(data);
        disponibilita.computeIfAbsent(chiave, k -> new HashSet<>()).add(data);
    }

    // toglie una data di disponibilità (il volontario si è ripensato)
    public void rimuoviDisponibilita(LocalDate data) {
        Set<LocalDate> set = disponibilita.get(chiave(data));
        if (set != null) set.remove(data);
    }

    public boolean isDisponibileIn(LocalDate data) {
        Set<LocalDate> set = disponibilita.get(chiave(data));
        return set != null && set.contains(data);
    }

    // restituisce le date di disponibilità per un certo mese
    public Set<LocalDate> getDisponibilita(int anno, int mese) {
        return Collections.unmodifiableSet(
            disponibilita.getOrDefault(chiave(anno, mese), Collections.emptySet())
        );
    }

    // dopo che l'app ha usato le disponibilità per pianificare, non servono più
    public void cancellaDisponibilita(int anno, int mese) {
        disponibilita.remove(chiave(anno, mese));
    }

    // ---- utility ----

    private static String chiave(LocalDate d) { return chiave(d.getYear(), d.getMonthValue()); }
    private static String chiave(int anno, int mese) { return anno + "-" + String.format("%02d", mese); }

    // hash minimale, non crittografico — il progetto non richiede sicurezza reale
    private static String cifra(String s) { return Integer.toHexString(s.hashCode()); }

    public String getNickname() { return nickname; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Volontario v)) return false;
        return nickname.equalsIgnoreCase(v.nickname);
    }

    @Override
    public int hashCode() { return nickname.toLowerCase().hashCode(); }

    @Override
    public String toString() { return "Volontario(" + nickname + ")"; }
}
