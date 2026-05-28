package model;

// ciclo di vita di una singola visita guidata
public enum StatoVisita {
    PROPOSTA,    // visibile e aperta alle iscrizioni
    COMPLETA,    // raggiunto il massimo iscritti, niente nuove iscrizioni
    CONFERMATA,  // chiuse le iscrizioni, soglia minima raggiunta
    CANCELLATA,  // chiuse le iscrizioni, sotto la soglia minima
    EFFETTUATA   // svolta, finisce nell'archivio storico
}
