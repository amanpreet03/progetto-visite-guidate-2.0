package storage;

import model.Sistema;

import java.io.*;
import java.nio.file.*;

/*
 * Salva e carica il sistema usando LA SERIALIZZAZIONE STANDARD DI JAVA.
 * Il file viene messo nella cartella da cui si lancia l'applicazione .
 */
public class GestoreStorage {

    private static final String FILE = "visite_guidate.dat";

    private GestoreStorage() {} // classe di utilità, non si istanzia

    public static void salva(Sistema sistema) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(FILE)))) {
            out.writeObject(sistema);
        }
    }

    public static Sistema carica() throws IOException, ClassNotFoundException {
        if (!Files.exists(Paths.get(FILE))) return new Sistema();
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(FILE)))) {
            return (Sistema) in.readObject();
        }
    }

    public static boolean esisteFile() {
        return Files.exists(Paths.get(FILE));
    }
}
