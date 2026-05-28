package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Classe principale del modello: contiene tutti i dati del sistema.
 * È l'unico oggetto serializzato su disco.
 *
 * Invariante: tutte le liste non sono null.
 */
public class Sistema implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CRED_USERNAME = "admin";
    public static final String CRED_PASSWORD = "admin123";

    private String ambitoTerritoriale;
    private int maxPersonePerIscrizione;
    private final List<Configuratore> configuratori = new ArrayList<>();
    private final List<Volontario>    volontari      = new ArrayList<>();
    private final List<Luogo>         luoghi         = new ArrayList<>();
    private final List<Visita>        visite         = new ArrayList<>();
    private final List<Visita>        archivio       = new ArrayList<>();
    
    private final Map<String, Set<LocalDate>> datePrecluse = new HashMap<>();
    // date precluse per mese: "YYYY-MM" -> insieme di date
    
    /* fase del ciclo mensile (V3)
    private FaseOperativa fase = FaseOperativa.RACCOLTA_DISPONIBILITA;
    private int annoRaccolta;
    private int meseRaccolta;

    /*
    
    // private final List<Fruitore>      fruitori       = new ArrayList<>();


    // ---- fase operativa ----

    public FaseOperativa getFase()             { return fase; }
    public void setFase(FaseOperativa f)       { this.fase = f; }
    public int  getAnnoRaccolta()              { return annoRaccolta; }
    public int  getMeseRaccolta()              { return meseRaccolta; }
    public void setMeseRaccolta(int anno, int mese) {
        this.annoRaccolta = anno;
        this.meseRaccolta = mese;
    }
    */
    // ---- ambito ----

    public void setAmbito(String ambito) {
        if (ambitoTerritoriale != null)
            throw new IllegalStateException("L'ambito è già stato impostato.");
        this.ambitoTerritoriale = ambito.trim();
    }

    public String  getAmbito()        { return ambitoTerritoriale; }
    public boolean ambitoImpostato()  { return ambitoTerritoriale != null; }

    // ---- max persone ----

    public void setMaxPersone(int max) { this.maxPersonePerIscrizione = max; }
    public int  getMaxPersone()        { return maxPersonePerIscrizione; }

    // ---- configuratori ----

    public void aggiungiConfiguratore(Configuratore c) {
        if (trovaConfiguratore(c.getUsername()).isPresent())
            throw new IllegalArgumentException("Username già in uso: " + c.getUsername());
        configuratori.add(c);
    }

    public Optional<Configuratore> trovaConfiguratore(String username) {
        return configuratori.stream()
            .filter(c -> c.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public List<Configuratore> getConfiguratori() { return Collections.unmodifiableList(configuratori); }

    // ---- volontari ----

    public void aggiungiVolontario(Volontario v) {
        if (trovaVolontario(v.getNickname()).isPresent())
            throw new IllegalArgumentException("Nickname già in uso: " + v.getNickname());
        volontari.add(v);
    }

    public Optional<Volontario> trovaVolontario(String nickname) {
        return volontari.stream()
            .filter(v -> v.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    public boolean rimuoviVolontario(Volontario v)  { return volontari.remove(v); }
    public List<Volontario> getVolontari()           { return Collections.unmodifiableList(volontari); }
    List<Volontario> getVolontariInterni()           { return volontari; }

    /*  ---- fruitori (V4) ----

    public void aggiungiFruitore(Fruitore f) {
        if (usernameOccupato(f.getUsername()))
            throw new IllegalArgumentException("Username già in uso: " + f.getUsername());
        fruitori.add(f);
    }

    public Optional<Fruitore> trovaFruitore(String username) {
        return fruitori.stream()
            .filter(f -> f.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public List<Fruitore> getFruitori() { return Collections.unmodifiableList(fruitori); }
    */
    // ---- luoghi ----

    public void aggiungiLuogo(Luogo l) {
        if (trovaLuogo(l.getNome()).isPresent())
            throw new IllegalArgumentException("Luogo già presente: " + l.getNome());
        luoghi.add(l);
    }

    public Optional<Luogo> trovaLuogo(String nome) {
        return luoghi.stream().filter(l -> l.getNome().equalsIgnoreCase(nome)).findFirst();
    }

    public boolean rimuoviLuogo(Luogo l)  { return luoghi.remove(l); }
    public List<Luogo> getLuoghi()         { return Collections.unmodifiableList(luoghi); }
    List<Luogo> getLuoghiInterni()         { return luoghi; }

    // ---- visite ----

    public void aggiungiVisita(Visita v)  { visite.add(v); }
    public List<Visita> getVisite()       { return Collections.unmodifiableList(visite); }
    public List<Visita> getArchivio()     { return Collections.unmodifiableList(archivio); }

    public List<Visita> getVisitePerStato(StatoVisita s) {
        return visite.stream().filter(v -> v.getStato() == s).collect(Collectors.toList());
    }

    /*
     * Aggiorna gli stati delle visite in base alla data odierna.
     * Chiamato all'avvio dell'applicazione.
     */
    public void aggiornaStati(LocalDate oggi) {
        List<Visita> daRimuovere = new ArrayList<>();
        for (Visita v : visite) {
            LocalDate scadenza = v.getData().minusDays(3);
            if (!oggi.isBefore(scadenza)
                    && (v.getStato() == StatoVisita.PROPOSTA || v.getStato() == StatoVisita.COMPLETA))
                v.chiudiIscrizioni();
            if (oggi.isAfter(v.getData())) {
                if (v.getStato() == StatoVisita.CONFERMATA) {
                    v.segnaEffettuata();
                    archivio.add(v);
                    daRimuovere.add(v);
                } else if (v.getStato() == StatoVisita.CANCELLATA) {
                    daRimuovere.add(v);
                }
            }
        }
        visite.removeAll(daRimuovere);
    }

    // ---- date precluse ----

    public void aggiungiDataPreclusa(LocalDate d) {
        datePrecluse.computeIfAbsent(chiave(d), k -> new HashSet<>()).add(d);
    }

    public boolean isPreclusa(LocalDate d) {
        Set<LocalDate> s = datePrecluse.get(chiave(d));
        return s != null && s.contains(d);
    }

    public Set<LocalDate> getDatePrecluse(int anno, int mese) {
        return Collections.unmodifiableSet(
            datePrecluse.getOrDefault(anno + "-" + String.format("%02d", mese), Collections.emptySet()));
    }

    public void cancellaDatePrecluse(int anno, int mese) {
        datePrecluse.remove(anno + "-" + String.format("%02d", mese));
    }

    // ---- utilità ----

    // public boolean usernameOccupato(String username) {
    //    return trovaConfiguratore(username).isPresent()
    //        || trovaVolontario(username).isPresent()
    //        || trovaFruitore(username).isPresent();
    // }

    public List<TipoVisita> getTipiVisitaPerVolontario(Volontario v) {
        List<TipoVisita> out = new ArrayList<>();
        for (Luogo l : luoghi)
            for (TipoVisita tv : l.getTipiVisita())
                if (tv.getVolontari().contains(v)) out.add(tv);
        return out;
    }

    public Luogo getLuogoDi(TipoVisita tv) {
        for (Luogo l : luoghi)
            if (l.getTipiVisita().contains(tv)) return l;
        return null;
    }

    private static String chiave(LocalDate d) {
        return d.getYear() + "-" + String.format("%02d", d.getMonthValue());
    }
}
