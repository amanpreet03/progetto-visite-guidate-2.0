/*package model;

import java.io.Serializable;

/*
 * Tiene traccia della fase operativa corrente nel ciclo mensile del "giorno 16".
 *
 * Il flusso obbligato ogni mese è:
 *   RACCOLTA_DISPONIBILITA
 *     → (configuratore chiude la raccolta) →
 *   GENERAZIONE_PIANO
 *     → (configuratore lancia la pianificazione) →
 *   MODIFICHE_DATI
 *     → (configuratore inserisce aggiunte/rimozioni) →
 *   NUOVA_RACCOLTA_DISPONIBILITA  (= torna a RACCOLTA_DISPONIBILITA per il mese dopo)
 *
 * Nella V1 e V2 il sistema parte direttamente in RACCOLTA_DISPONIBILITA.
 * La V3 sblocca tutte le transizioni.
 *
public enum FaseOperativa implements Serializable {

     *
     * Fase normale: i volontari possono dichiarare disponibilità,
     * il configuratore può aggiungere date precluse.
     * Dura dal giorno 16 del mese i al giorno 15 del mese i+1.
     
    RACCOLTA_DISPONIBILITA

     *
     * Il configuratore ha chiuso la raccolta disponibilità e deve
     * avviare la generazione del piano delle visite proposte.
     
    GENERAZIONE_PIANO

     *
     * Il piano è stato generato. Il configuratore può ora inserire
     * richieste di aggiunta/rimozione di luoghi, tipi di visita e volontari.
     
    MODIFICHE_DATI

     *
     * Le modifiche sono terminate. Il configuratore apre la nuova raccolta
     * disponibilità (per il mese i+2) e il sistema torna a RACCOLTA_DISPONIBILITA.
     
    NUOVA_RACCOLTA

*/
