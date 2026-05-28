/*package model;

import java.io.Serializable;


 * Rappresenta un utente fruitore: si registra autonomamente scegliendo
 * subito le sue credenziali (nessun primo accesso con password predefinita).
 *
 * Invariante: username != null, passwordHash != null
 
public class Fruitore implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private String passwordHash;

    public Fruitore(String username, String password) {
        this.username = username.trim();
        this.passwordHash = cifra(password);
    }

    public boolean passwordCorretta(String tentativo) {
        return passwordHash.equals(cifra(tentativo));
    }

    public void cambiaPassword(String nuova) {
        this.passwordHash = cifra(nuova);
    }

    public String getUsername() { return username; }

    private static String cifra(String s) { return Integer.toHexString(s.hashCode()); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Fruitore f)) return false;
        return username.equalsIgnoreCase(f.username);
    }

    @Override
    public int hashCode() { return username.toLowerCase().hashCode(); }

    @Override
    public String toString() { return "Fruitore(" + username + ")"; }
}
*/