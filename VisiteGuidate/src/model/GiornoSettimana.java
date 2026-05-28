package model;

import java.time.DayOfWeek;

// giorni della settimana usati per capire in che giornate una visita è programmabile
public enum GiornoSettimana {
    LUNEDI, MARTEDI, MERCOLEDI, GIOVEDI, VENERDI, SABATO, DOMENICA;

    // converte dal tipo standard Java al nostro enum
    public static GiornoSettimana da(DayOfWeek d) {
        return switch (d) {
            case MONDAY    -> LUNEDI;
            case TUESDAY   -> MARTEDI;
            case WEDNESDAY -> MERCOLEDI;
            case THURSDAY  -> GIOVEDI;
            case FRIDAY    -> VENERDI;
            case SATURDAY  -> SABATO;
            case SUNDAY    -> DOMENICA;
        };
    }

    @Override
    public String toString() {
        // prima lettera maiuscola e resto minuscolo, più leggibile nei menu
        String s = name();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
