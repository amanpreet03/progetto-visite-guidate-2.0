import controller.Controller;
import model.*;
import storage.GestoreStorage;
import ui.*;

import java.time.LocalDate;

/*
 * Punto di ingresso – Versioni 1, 2, 3 e 4.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║           SISTEMA VISITE GUIDATE         ║");
        System.out.println("╚══════════════════════════════════════════╝");

        Sistema sistema;
        try {
            sistema = GestoreStorage.carica();
            if (GestoreStorage.esisteFile())
                System.out.println("  Dati caricati.\n");
            else {
                System.out.println("  Primo avvio: sistema vuoto.\n");
                sistema.aggiungiConfiguratore(
                    new Configuratore(Sistema.CRED_USERNAME, Sistema.CRED_PASSWORD));
                GestoreStorage.salva(sistema);
            }
        } catch (Exception e) {
            System.out.println("  Errore caricamento: " + e.getMessage() + "  ripartenza da zero.");
            sistema = new Sistema();
            try {
                sistema.aggiungiConfiguratore(
                    new Configuratore(Sistema.CRED_USERNAME, Sistema.CRED_PASSWORD));
                GestoreStorage.salva(sistema);
            } catch (Exception ignored) {}
        }

        sistema.aggiornaStati(LocalDate.now());

        Controller       ctrl = new Controller(sistema);
        MenuConfiguratore mc  = new MenuConfiguratore(ctrl);
      // MenuVolontario    mv  = new MenuVolontario(ctrl);
      // MenuFruitore      mf  = new MenuFruitore(ctrl);

        boolean running = true;
        while (running) {
            System.out.println("\n  Chi sei?");
            System.out.println("  1. Configuratore");
        //    System.out.println("  2. Volontario");
        //    System.out.println("  3. Fruitore");
            System.out.println("  2. Registrati come configuratore");
        //    System.out.println("  5. Registrati come fruitore");
            System.out.println("  0. Esci");
            int scelta = Console.leggiInt("  Scelta: ", 0, 5);
            switch (scelta) {
                case 1 -> { Configuratore c = mc.login(); if (c != null) mc.menuPrincipale(c); }
            //    case 2 -> { Volontario v = mv.login();   if (v != null) mv.menuPrincipale(v); }
            //    case 3 -> { Fruitore f = mf.login();     if (f != null) mf.menuPrincipale(f); }
                case 2 -> mc.registrazione();
            //    case 5 -> mf.registrazione();
                case 0 -> running = false;
            }
        }
        System.out.println("  Arrivederci!");
    }
}
