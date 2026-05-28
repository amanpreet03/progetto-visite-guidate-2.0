
import model.*;
import storage.GestoreStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Gestisce tutta la logica applicativa: configuratori.
 * La UI non tocca mai il modello direttamente.
 *
 * Invariante: sistema != null
 */
public class Controller {

    private final Sistema sistema;

    public Controller(Sistema sistema) {
        this.sistema = sistema;
    }

    // ================================================================
    // LOGIN / REGISTRAZIONE
    // ================================================================

    public Configuratore loginConfiguratore(String username, String password) {
        Configuratore c = sistema.trovaConfiguratore(username)
            .orElseThrow(() -> new IllegalArgumentException("Username non trovato."));
        if (!c.passwordCorretta(password))
            throw new IllegalArgumentException("Password errata.");
        return c;
    }

    public void registraConfiguratore(String credUsr, String credPwd, String nuovoUsr, String nuovaPwd) {
        if (!Sistema.CRED_USERNAME.equals(credUsr) || !Sistema.CRED_PASSWORD.equals(credPwd))
            throw new IllegalArgumentException("Credenziali predefinite errate.");
        if (sistema.trovaConfiguratore(nuovoUsr).isPresent())
            throw new IllegalArgumentException("Username già in uso: " + nuovoUsr);
        Configuratore c = new Configuratore(nuovoUsr, nuovaPwd);
        c.cambiaPassword(nuovaPwd);
        sistema.aggiungiConfiguratore(c);
        salva();
    }

    public void cambiaPasswordConfiguratore(Configuratore c, String nuova) {
        c.cambiaPassword(nuova);
        salva();
    }

    // ================================================================
    // INIZIALIZZAZIONE
    // ================================================================

    public void inizializza(String ambito, int maxPersone) {
        sistema.setAmbito(ambito);
        sistema.setMaxPersone(maxPersone);
        LocalDate prossimo = LocalDate.now().plusMonths(1);
        sistema.setMeseRaccolta(prossimo.getYear(), prossimo.getMonthValue());
        salva();
    }

    public boolean ambitoImpostato() { return sistema.ambitoImpostato(); }
    public String  getAmbito()       { return sistema.getAmbito(); }
    public int     getMaxPersone()   { return sistema.getMaxPersone(); }

    public void setMaxPersone(int max) {
        sistema.setMaxPersone(max);
        salva();
    }
    public int getAnnoRaccolta() { return sistema.getAnnoRaccolta(); }
    public int getMeseRaccolta() { return sistema.getMeseRaccolta(); }

   
    // ================================================================
    // LUOGHI
    // ================================================================

    public Luogo creaLuogo(String nome, String descr, String colloc) {
        if (sistema.trovaLuogo(nome).isPresent())
            throw new IllegalArgumentException("Esiste già un luogo con questo nome.");
        return new Luogo(nome, descr, colloc);
    }

    public void salvaLuogo(Luogo l) {
        if (!l.hasTipiVisita())
            throw new IllegalStateException("Il luogo deve avere almeno un tipo di visita.");
        sistema.aggiungiLuogo(l);
        salva();
    }

    public Luogo ottieniLuogo(String nome) {
        return sistema.trovaLuogo(nome)
            .orElseThrow(() -> new IllegalArgumentException("Luogo non trovato: " + nome));
    }

    public void aggiungiTipoVisita(Luogo l, TipoVisita tv) {
        if (!tv.hasVolontari())
            throw new IllegalStateException("Il tipo di visita deve avere almeno un volontario.");
        l.aggiungiTipoVisita(tv);
        salva();
    }

    public List<Luogo> getLuoghi() { return sistema.getLuoghi(); }

    // ================================================================
    // VOLONTARI
    // ================================================================

    public Volontario creaVolontario(String nickname, String password) {
        if (sistema.trovaVolontario(nickname).isPresent())
            throw new IllegalArgumentException("Nickname già in uso: " + nickname);
        Volontario v = new Volontario(nickname, password);
        sistema.aggiungiVolontario(v);
        salva();
        return v;
    }

    public void collegaVolontario(String nickname, TipoVisita tv) {
        Volontario v = sistema.trovaVolontario(nickname)
            .orElseThrow(() -> new IllegalArgumentException(
                "Volontario '" + nickname + "' non trovato. Crealo prima."));
        tv.aggiungiVolontario(v);
        salva();
    }

    public List<Volontario>  getVolontari()                      { return sistema.getVolontari(); }
    public List<TipoVisita>  getTipiVisitaDelVolontario(Volontario v) { return sistema.getTipiVisitaPerVolontario(v); }
        
    // ================================================================
    // VISITE
    // ================================================================

    public List<Visita> getVisitePerStato(StatoVisita s) {
        if (s == StatoVisita.EFFETTUATA) return sistema.getArchivio();
        return sistema.getVisitePerStato(s);
    }

    public List<Visita> getVisiteConfermate(Volontario v) {
        return sistema.getVisitePerStato(StatoVisita.CONFERMATA).stream()
            .filter(vis -> vis.getGuida().equals(v))
            .collect(Collectors.toList());
    }
   
    // solo le proposte, ordinate per data
    public List<Visita> getVisiteProposte() {
        return sistema.getVisitePerStato(StatoVisita.PROPOSTA).stream()
            .sorted(Comparator.comparing(Visita::getData))
            .collect(Collectors.toList());
    }

    
    // ================================================================
    // DATE PRECLUSE
    // ================================================================

    public void aggiungiDataPreclusa(LocalDate d) {
        sistema.aggiungiDataPreclusa(d);
        salva();
    }

    public Set<LocalDate> getDatePrecluse(int anno, int mese) {
        return sistema.getDatePrecluse(anno, mese);
    }

    // ================================================================
    // UTILITÀ INTERNE
    // ================================================================

    private void scollegaDaiVolontari(TipoVisita tv) {
        for (Volontario v : new ArrayList<>(tv.getVolontari())) tv.rimuoviVolontario(v);
    }

    // rimuove i tipi di visita rimasti senza volontari
    private void rimuoviTipiSenzaVolontari() {
        for (Luogo l : sistema.getLuoghi())
            for (TipoVisita tv : new ArrayList<>(l.getTipiVisita()))
                if (!tv.hasVolontari()) l.rimuoviTipoVisita(tv.getTitolo());
        }
    
    private void rimuoviLuoghiSenzaTipi() {
        sistema.getLuoghi().stream()
            .filter(l -> !l.hasTipiVisita())
            .collect(Collectors.toList())
            .forEach(sistema::rimuoviLuogo);
    }

    private void rimuoviVolontariSenzaTipi() {
        sistema.getVolontari().stream()
            .filter(v -> sistema.getTipiVisitaPerVolontario(v).isEmpty())
            .collect(Collectors.toList())
            .forEach(sistema::rimuoviVolontario);
    }

    private void salva() {
        try { GestoreStorage.salva(sistema); }
        catch (IOException e) { System.err.println("[ERRORE] Salvataggio: " + e.getMessage()); }
    }

    public Sistema getSistema() { return sistema; }
}
