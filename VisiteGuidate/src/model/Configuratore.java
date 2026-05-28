package model;

import java.io.Serializable;

/*
 * Un configuratore è il responsabile della pianificazione nell'organizzazione.
 * Si registra con credenziali predefinite (comuni a tutti i nuovi configuratori)
 * e deve cambiare la password al primo accesso.
 */
public class Configuratore implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private String passwordHash;
    private boolean primoAccesso;

    public Configuratore(String username, String passwordIniziale) {
        this.username = username.trim();
        this.passwordHash = cifra(passwordIniziale);
        this.primoAccesso = true;
    }

    public boolean passwordCorretta(String tentativo) {
        return passwordHash.equals(cifra(tentativo));
    }

    public void cambiaPassword(String nuova) {
        this.passwordHash = cifra(nuova);
        this.primoAccesso = false;
    }

    public boolean isPrimoAccesso()  { return primoAccesso; }
    public String getUsername()      { return username; }

    private static String cifra(String s) { return Integer.toHexString(s.hashCode()); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Configuratore c)) return false;
        return username.equalsIgnoreCase(c.username);
    }

    @Override
    public int hashCode() { return username.toLowerCase().hashCode(); }
}
