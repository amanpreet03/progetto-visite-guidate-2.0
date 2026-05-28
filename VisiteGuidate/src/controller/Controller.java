package controller;

import model.*;
import storage.GestoreStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Gestisce tutta la logica applicativa: configuratori, volontari e fruitori.
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

    public Volontario loginVolontario(String nickname, String password) {
        Volontario v = sistema.trovaVolontario(nickname)
            .orElseThrow(() -> new IllegalArgumentException("Nickname non trovato."));
        if (!v.passwordCorretta(password))
            throw new IllegalArgumentException("Password errata.");
        return v;
    }

    public void cambiaPasswordVolontario(Volontario v, String nuova) {
        v.cambiaPassword(nuova);
        salva();
    }

   /*  public Fruitore loginFruitore(String username, String password) {
        Fruitore f = sistema.trovaFruitore(username)
            .orElseThrow(() -> new IllegalArgumentException("Username non trovato."));
        if (!f.passwordCorretta(password))
            throw new IllegalArgumentException("Password errata.");
        return f;
    }

    public void registraFruitore(String username, String password) {
        if (sistema.usernameOccupato(username))
            throw new IllegalArgumentException("Username già in uso: " + username);
        sistema.aggiungiFruitore(new Fruitore(username, password));
        salva();
    }
    */
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

    /*  ================================================================
    // FASE OPERATIVA (V3)
    // ================================================================

    public FaseOperativa getFase() { return sistema.getFase(); }

    /*
     * PASSO 1 – giorno 16: chiude la raccolta disponibilità.
     * Pre:  fase == RACCOLTA_DISPONIBILITA
     * Post: fase == GENERAZIONE_PIANO
     
    public void chiudiRaccoltaDisponibilita() {
        if (sistema.getFase() != FaseOperativa.RACCOLTA_DISPONIBILITA)
            throw new IllegalStateException("Non siamo in fase di raccolta disponibilità.");
        sistema.setFase(FaseOperativa.GENERAZIONE_PIANO);
        salva();
    }

    /*
     * PASSO 2 – genera il piano visite per il mese di raccolta.
     * Pre:  fase == GENERAZIONE_PIANO
     * Post: fase == MODIFICHE_DATI, visite aggiunte al sistema
     
    public List<Visita> generaPianoVisite() {
        if (sistema.getFase() != FaseOperativa.GENERAZIONE_PIANO)
            throw new IllegalStateException("Prima chiudi la raccolta disponibilità.");

        int anno = sistema.getAnnoRaccolta();
        int mese = sistema.getMeseRaccolta();

        List<Visita> nuove = Pianificatore.generaPiano(
            sistema.getLuoghi(), anno, mese, sistema.getDatePrecluse(anno, mese));

        for (Visita v : nuove) sistema.aggiungiVisita(v);

        // le disponibilità usate si possono eliminare
        for (Volontario v : sistema.getVolontari()) v.cancellaDisponibilita(anno, mese);
        sistema.cancellaDatePrecluse(anno, mese);

        sistema.setFase(FaseOperativa.MODIFICHE_DATI);
        salva();
        return nuove;
    }

    /*
     * PASSO 3 – apre la nuova raccolta disponibilità (mese i+2).
     * Pre:  fase == MODIFICHE_DATI
     * Post: fase == RACCOLTA_DISPONIBILITA, mese raccolta avanzato
     
    public void apriNuovaRaccolta() {
        if (sistema.getFase() != FaseOperativa.MODIFICHE_DATI)
            throw new IllegalStateException("Prima genera il piano e gestisci le eventuali modifiche.");
        LocalDate att = LocalDate.of(sistema.getAnnoRaccolta(), sistema.getMeseRaccolta(), 1);
        LocalDate prox = att.plusMonths(1);
        sistema.setMeseRaccolta(prox.getYear(), prox.getMonthValue());
        sistema.setFase(FaseOperativa.RACCOLTA_DISPONIBILITA);
        salva();
    }
    */
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

    /*
     * Rimuove un luogo con cascata (V3):
     *   - scollega tutti i tipi di visita dai volontari
     *   - rimuove i volontari rimasti senza alcun tipo
     
    public void rimuoviLuogo(String nomeLuogo) {
        verificaFaseModifiche();
        Luogo luogo = ottieniLuogo(nomeLuogo);
        for (TipoVisita tv : new ArrayList<>(luogo.getTipiVisita()))
            scollegaDaiVolontari(tv);
        rimuoviVolontariSenzaTipi();
        sistema.rimuoviLuogo(luogo);
        salva();
    }

    /*
     * Rimuove un tipo di visita con cascata (V3):
     *   - scollega dai volontari
     *   - se il luogo resta vuoto → rimuovi il luogo
     *   - se un volontario resta senza tipi → rimuovi il volontario
     
    public void rimuoviTipoVisita(String nomeLuogo, String titoloTV) {
        verificaFaseModifiche();
        Luogo luogo = ottieniLuogo(nomeLuogo);
        TipoVisita tv = luogo.getTipiVisita().stream()
            .filter(t -> t.getTitolo().equalsIgnoreCase(titoloTV))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Tipo di visita non trovato."));
        scollegaDaiVolontari(tv);
        luogo.rimuoviTipoVisita(titoloTV);
        if (!luogo.hasTipiVisita()) sistema.rimuoviLuogo(luogo);
        rimuoviVolontariSenzaTipi();
        salva();
    }

    /*
     * Rimuove un volontario con cascata (V3):
     *   - rimuovilo da ogni tipo di visita
     *   - se un tipo resta senza volontari → rimuovi quel tipo
     *   - se un luogo resta senza tipi → rimuovi quel luogo
     
    public void rimuoviVolontario(String nickname) {
        verificaFaseModifiche();
        Volontario v = sistema.trovaVolontario(nickname)
            .orElseThrow(() -> new IllegalArgumentException("Volontario non trovato: " + nickname));
        for (Luogo l : sistema.getLuoghi())
            for (TipoVisita tv : l.getTipiVisita())
                tv.rimuoviVolontario(v);
        rimuoviTipiSenzaVolontari();
        rimuoviLuoghiSenzaTipi();
        sistema.rimuoviVolontario(v);
        salva();
    }
    */
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

    // DISPONIBILITÀ (V2)
    // ================================================================

    public void aggiungiDisponibilita(Volontario v, LocalDate data) {
    //    if (sistema.getFase() != FaseOperativa.RACCOLTA_DISPONIBILITA)
    //        throw new IllegalStateException("Le disponibilità non si raccolgono in questa fase.");

    //    if (data.getYear() != sistema.getAnnoRaccolta() || data.getMonthValue() != sistema.getMeseRaccolta())
    //        throw new IllegalArgumentException(
    //            "Puoi dichiarare disponibilità solo per il mese " +
    //            sistema.getMeseRaccolta() + "/" + sistema.getAnnoRaccolta() + ".");
        if (v == null) {
        throw new IllegalArgumentException("Il volontario non può essere nullo.");
        }

        if (data == null) {
        throw new IllegalArgumentException("La data non può essere nulla.");
        }

        if (data.getYear() != sistema.getAnnoRaccolta() || data.getMonthValue() != sistema.getMeseRaccolta()) {
        throw new IllegalArgumentException(
                "Puoi dichiarare disponibilità solo per il mese "
                        + sistema.getMeseRaccolta() + "/" + sistema.getAnnoRaccolta() + "."
        );
    }
         
        if (sistema.isPreclusa(data))
            throw new IllegalArgumentException("Il " + data + " è precluso a ogni visita.");

        GiornoSettimana g = GiornoSettimana.da(data.getDayOfWeek());
        boolean haTipi = sistema.getTipiVisitaPerVolontario(v).stream()
            .anyMatch(tv -> tv.programmabileIl(g) && tv.nelPeriodo(data));
        if (!haTipi)
            throw new IllegalArgumentException("Nessun tuo tipo di visita è programmabile il " + g + ".");

        v.aggiungiDisponibilita(data);
        salva();
    }

    public void rimuoviDisponibilita(Volontario v, LocalDate data) {
        if (v == null) {
        throw new IllegalArgumentException("Il volontario non può essere nullo.");
        }
    
        if (data == null) {
        throw new IllegalArgumentException("La data non può essere nulla.");
        }
        
        v.rimuoviDisponibilita(data);
        salva();
    }

    public Set<LocalDate> getDisponibilita(Volontario v) {
    if (v == null) {
        throw new IllegalArgumentException("Il volontario non può essere nullo.");
    }
    return new TreeSet<>(v.getDisponibilita(getAnnoRaccolta(), getMeseRaccolta()));
}
        

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
    /*
    // tutte le visite visibili al fruitore, ordinate per data
    public List<Visita> getVisiteFruitore() {
        List<Visita> out = new ArrayList<>();
        out.addAll(sistema.getVisitePerStato(StatoVisita.PROPOSTA));
        out.addAll(sistema.getVisitePerStato(StatoVisita.CONFERMATA));
        out.addAll(sistema.getVisitePerStato(StatoVisita.CANCELLATA));
        out.sort(Comparator.comparing(Visita::getData));
        return out;
    }
    */
    // solo le proposte, ordinate per data
    public List<Visita> getVisiteProposte() {
        return sistema.getVisitePerStato(StatoVisita.PROPOSTA).stream()
            .sorted(Comparator.comparing(Visita::getData))
            .collect(Collectors.toList());
    }

    /*  visite a cui il fruitore risulta iscritto
    public List<Visita> getMieIscrizioni(Fruitore f) {
        return getVisiteFruitore().stream()
            .filter(v -> v.getIscrizioni().stream()
                .anyMatch(i -> i.getUsernameIscritto().equalsIgnoreCase(f.getUsername())))
            .collect(Collectors.toList());
    }

    // ================================================================
    // ISCRIZIONI FRUITORE (V4)
    // ================================================================

    
     * Iscrive il fruitore a una visita proposta.
     * Pre:  visita PROPOSTA, 1 <= persone <= maxPersone, posti sufficienti
     * Post: iscrizione aggiunta, restituisce il codice di prenotazione
     
    public String iscriviAVisita(Fruitore f, Visita visita, int persone) {
        int maxConsentito = sistema.getMaxPersone();
        if (persone < 1 || persone > maxConsentito)
            throw new IllegalArgumentException(
                "Persone: deve essere tra 1 e " + maxConsentito + ".");
        if (visita.getStato() != StatoVisita.PROPOSTA)
            throw new IllegalStateException("Iscrizioni chiuse per questa visita.");
        if (visita.totaleIscritti() + persone > visita.getTipo().getMaxPartecipanti())
            throw new IllegalStateException(
                "Posti disponibili: " + visita.postiLiberi() + ". Riduci il numero.");

        Iscrizione i = new Iscrizione(f.getUsername(), persone);
        visita.aggiungiIscrizione(i);
        salva();
        return i.getCodice();
    }

    /*
     * Disdice un'iscrizione tramite codice.
     * Pre:  visita PROPOSTA o COMPLETA, codice appartiene al fruitore f
     
    public void disdiciIscrizione(Fruitore f, Visita visita, String codice) {
        if (visita.getStato() != StatoVisita.PROPOSTA && visita.getStato() != StatoVisita.COMPLETA)
            throw new IllegalStateException("Non puoi disdire: visita già " + visita.getStato() + ".");
        Iscrizione trovata = visita.cercaIscrizione(codice)
            .orElseThrow(() -> new IllegalArgumentException("Codice prenotazione non trovato."));
        if (!trovata.getUsernameIscritto().equalsIgnoreCase(f.getUsername()))
            throw new IllegalArgumentException("Questo codice non appartiene al tuo account.");
        visita.rimuoviIscrizione(codice);
        salva();
    }

    public List<Fruitore> getFruitori() { return sistema.getFruitori(); }
    */
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
    /* 
    private void verificaFaseModifiche() {
        if (sistema.getFase() != FaseOperativa.MODIFICHE_DATI)
            throw new IllegalStateException(
                "Le modifiche ai dati sono permesse solo dopo la generazione del piano.");
    }*/

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
