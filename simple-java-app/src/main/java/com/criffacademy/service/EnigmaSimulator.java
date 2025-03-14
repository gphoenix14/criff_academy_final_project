package com.criffacademy.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Questa versione della macchina Enigma è solo una semplificazione
 * che non tiene conto del processo in toto.
 */
public class EnigmaSimulator {
    // Definizione dell'alfabeto della macchina Enigma
    private static final String DIZIONARIO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // Array per i rotori della macchina Enigma
    private final String[] rotori = new String[3];
    // Configurazione del plugboard
    private final String plug;

    // Costruttore della classe EnigmaSimulator
    public EnigmaSimulator() throws IOException {
        // Carica le proprietà dalla configurazione app.properties dal classpath
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/app.properties")) {
            if (is == null) {
                throw new IOException("Impossibile trovare 'app.properties' nel classpath.");
            }
            props.load(is);
        }

        // Inizializza i rotori prendendo i valori dal file di configurazione
        rotori[0] = props.getProperty("rotore1");
        rotori[1] = props.getProperty("rotore2");
        rotori[2] = props.getProperty("rotore3");

        // Inizializza la configurazione del plugboard prendendo il valore dal file di configurazione
        this.plug = props.getProperty("plug");
    }

    // Metodo per cifrare o decifrare un messaggio
    public String cifraDecifra(String messaggio, boolean cifra) {
        StringBuilder risultato = new StringBuilder();

        // Iterazione attraverso ogni carattere del messaggio
        for (char c : messaggio.toUpperCase().toCharArray()) {
            // Controlla se il carattere è presente nell'alfabeto della macchina Enigma
            if (DIZIONARIO.indexOf(c) != -1) {
                // Applica la configurazione del plugboard al carattere
                c = applicaPlugboard(c);

                // Cifra o decifra il carattere attraverso i rotori
                if (cifra) {
                    for (String rotore : rotori) {
                        c = transforma(c, rotore, true);
                    }
                } else {
                    for (int i = rotori.length - 1; i >= 0; i--) {
                        c = transforma(c, rotori[i], false);
                    }
                }

                // Applica di nuovo la configurazione del plugboard al carattere
                c = applicaPlugboard(c);

                // Aggiunge il carattere cifrato o decifrato al risultato
                risultato.append(c);
            } else {
                // Se il carattere non è nell'alfabeto, lo aggiunge direttamente
                risultato.append(c);
            }
        }
        return risultato.toString();
    }

    // Metodo privato per applicare la configurazione del plugboard
    private char applicaPlugboard(char c) {
        if (plug != null && !plug.isEmpty()) {
            // Se il plugboard è configurato, scambia i caratteri in base alla configurazione
            for (String swap : plug.split(",")) {
                if (swap.charAt(0) == c) return swap.charAt(2);
                if (swap.charAt(2) == c) return swap.charAt(0);
            }
        }
        // Restituisce il carattere originale se non ci sono configurazioni nel plugboard
        return c;
    }

    // Metodo privato per trasformare un carattere attraverso un rotore
    private char transforma(char c, String rotore, boolean cifra) {
        int idx = DIZIONARIO.indexOf(c);
        if (idx < 0) return c; // Carattere non trovato nell'alfabeto, restituisci invariato

        if (cifra) {
            // Se si sta cifrando, restituisce il carattere corrispondente nel rotore
            return rotore.charAt(idx);
        } else {
            // Se si sta decifrando, trova la posizione di c nel rotore e mappa su DIZIONARIO
            int pos = rotore.indexOf(c);
            return (pos >= 0) ? DIZIONARIO.charAt(pos) : c;
        }
    }

    // Metodo principale per eseguire la cifratura o decifratura da riga di comando
    public static void main(String[] args) throws IOException {
        // Controlla se ci sono due argomenti passati da riga di comando
        if (args.length != 2) {
            System.out.println("uso <messaggio> <1 per cifrare, 0 per decifrare>");
            return;
        }

        // Crea un'istanza di EnigmaSimulator
        EnigmaSimulator enigma = new EnigmaSimulator();
        // Ottiene il messaggio e la modalità (cifratura o decifratura) dagli argomenti
        String messaggio = args[0];
        boolean modalita = "1".equals(args[1]);

        // Esegue la cifratura/decifratura
        String risultato = enigma.cifraDecifra(messaggio, modalita);
        System.out.println("Risultato: " + risultato);
    }
}
