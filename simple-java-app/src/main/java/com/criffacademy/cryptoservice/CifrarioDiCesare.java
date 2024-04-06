package com.criffacademy.cryptoservice;
public class CifrarioDiCesare {
    // Dizionario utilizzato per il cifrario di Cesare
    private static final String DIZIONARIO = "abcdefghijklmnopqrstuvwxyz";

    // Metodo principale del programma
    public static void main(String[] args) {
        // Verifica se sono stati passati abbastanza argomenti
        if (args.length < 3) {
            System.out.println("Uso <messaggio> <shift> <modalità(0/1)>");
            return;
        }

        // Estrae i parametri dalla riga di comando
        String messaggio = args[0].toLowerCase(); // Converti tutti i caratteri del messaggio in minuscolo per semplicità
        int shift = Integer.parseInt(args[1]);     // Numero di spostamenti per il cifrario
        int modalità = Integer.parseInt(args[2]);  // Modalità 1 per crittografia, 0 per decrittografia

        // Stringa per contenere il risultato
        String risultato = "";

        // Determina se criptare o decriptare in base alla modalità specificata
        if (modalità == 1) {
            risultato = cripta(messaggio, shift);
        } else if (modalità == 0) {
            risultato = decripta(messaggio, shift);
        } else {
            System.out.println("Modalità non valida. Usa 1 per Crittografia e 0 per decrittografia");
            return;
        }

        // Stampare il risultato
        System.out.println("Risultato : " + risultato);
    }

    // Metodo per criptare il testo
    public static String cripta(String testo, int shift) {
        return trasforma(testo, shift); // Richiama il metodo trasforma con il parametro di shift
    }

    // Metodo per decriptare il testo
    public static String decripta(String testo, int shift) {
        return trasforma(testo, -shift); // Richiama il metodo trasforma con il parametro di shift negativo
    }

    // Metodo che effettua la trasformazione del testo in base al parametro di shift
    private static String trasforma(String testo, int shift) {
        StringBuilder risultato = new StringBuilder(); // Creazione di un oggetto StringBuilder per costruire la stringa risultante

        // Iterazione attraverso ogni carattere del testo
        for (char carattere : testo.toCharArray()) {
            // Verifica se il carattere è presente nel dizionario
            if (DIZIONARIO.indexOf(carattere) != -1) {
                int posizioneOriginale = DIZIONARIO.indexOf(carattere); // Ottiene la posizione originale del carattere nel dizionario
                int nuovaPosizione = (DIZIONARIO.length() + posizioneOriginale + shift) % DIZIONARIO.length(); // Calcola la nuova posizione applicando lo shift
                risultato.append(DIZIONARIO.charAt(nuovaPosizione)); // Aggiunge il carattere trasformato al risultato
            } else {
                risultato.append(carattere); // Se il carattere non è presente nel dizionario, lo aggiunge al risultato senza modificarlo
            }
        }
        return risultato.toString(); // Restituisce il risultato come stringa
    }
}