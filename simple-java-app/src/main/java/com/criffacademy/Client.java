package com.criffacademy;
import java.io.FileInputStream; // Importa la classe FileInputStream dal package java.io
import java.io.IOException; // Importa la classe IOException dal package java.io
import java.io.PrintWriter; // Importa la classe PrintWriter dal package java.io
import java.net.Socket; // Importa la classe Socket dal package java.net
import java.util.Properties; // Importa la classe Properties dal package java.util
import java.util.Scanner; // Importa la classe Scanner dal package java.util

import com.criffacademy.cryptoservice.CifrarioDiCesare;
import com.criffacademy.cryptoservice.CryptoUtils;
import com.criffacademy.cryptoservice.EnigmaSimulator;

public class Client { // Definisce la classe Client
    private static String sharedSecret = getPSK("app.properties"); // Variabile statica che contiene la chiave condivisa
    private static int cesar_shift = getCesarShift("app.properties");
    private static boolean isEnigmaOn = false; // Variabile statica per controllare lo stato di Enigma
    private static boolean isAesOn = false; // Variabile statica per controllare lo stato di AES
    private static boolean isCesarOn = false;

    private static EnigmaSimulator enigmaSimulator; // Variabile statica per l'istanza dell'EnigmaSimulator

    static {
        try {
            enigmaSimulator = new EnigmaSimulator(); // Istanzia l'EnigmaSimulator
        } catch (IOException e) {
            System.err.println("Errore nel caricamento: " + e.getMessage());
            System.exit(1);
        }
    }
    

    public static void main(String[] args) { // Metodo principale del programma
        if (args.length != 3) { // Controlla se sono stati passati tre argomenti da riga di comando
            System.err.println("Usage: java Client <server-ip> <port> <username>"); // Stampa il messaggio di utilizzo
            System.exit(1); // Esce dal programma con codice di errore 1
        }

        String serverIp = args[0]; // Ottiene l'indirizzo IP del server dalla riga di comando
        int port = Integer.parseInt(args[1]); // Ottiene la porta del server dalla riga di comando
        String username = args[2]; // Ottiene lo username dall'utente dalla riga di comando

        try (Socket socket = new Socket(serverIp, port); // Crea una nuova connessione Socket
             Scanner userInput = new Scanner(System.in); // Crea un nuovo oggetto Scanner per l'input utente
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) { // Crea un nuovo oggetto PrintWriter per scrivere sulla connessione

            out.println("[+1]" + username); // Invia un messaggio di connessione al server
            System.out.println("Connected to server. Start typing messages (type 'exit' to quit)."); // Stampa un messaggio di connessione riuscita

            Thread serverListener = new Thread(() -> { // Crea un nuovo thread per ascoltare i messaggi dal server
                try (Scanner in = new Scanner(socket.getInputStream())) { // Crea un nuovo oggetto Scanner per leggere i messaggi dal server
                    while (in.hasNextLine()) { // Continua a leggere i messaggi finché il server invia dati
                        String message = in.nextLine(); // Legge un nuovo messaggio dal server
                        if (message.startsWith("[+]") || message.startsWith("[-]") || message.startsWith("[!]")) { // Controlla se il messaggio è un messaggio di sistema
                            System.out.println(message); // Stampa il messaggio di sistema
                        } else { // Altrimenti, se è un messaggio normale
                            try { // Prova a decodificare il messaggio
                                String decryptedMessage = message; // Assume che il messaggio sia già decriptato
                                if (isAesOn) { // Se è attivo l'AES
                                    decryptedMessage = CryptoUtils.decrypt(message, sharedSecret); // Decifra il messaggio utilizzando AES
                                } else if (isEnigmaOn) { // Altrimenti, se è attivo Enigma
                                    decryptedMessage = enigmaSimulator.cifraDecifra(message, false); // Decifra il messaggio utilizzando Enigma
                                } else if (isCesarOn) {
                                    decryptedMessage = CifrarioDiCesare.decripta(message,cesar_shift);
                                }
                                System.out.println(decryptedMessage); // Stampa il messaggio decodificato
                            } catch (Exception e) { // Gestisce eventuali eccezioni
                                System.out.println("Ricevuto messaggio non decriptabile"); // Stampa un messaggio di errore
                            }
                        }
                    }
                } catch (IOException e) { // Gestisce eventuali eccezioni di IO
                    e.printStackTrace(); // Stampa lo stack trace dell'eccezione
                }
            });
            serverListener.start(); // Avvia il thread per ascoltare i messaggi dal server

            while (true) { // Loop infinito per gestire l'input dell'utente
                String inputMessage = userInput.nextLine(); // Legge l'input dell'utente

                // Gestione dei comandi speciali
                if (inputMessage.equalsIgnoreCase("/exit")) { // Se l'utente vuole uscire
                    out.println("[-1]" + username); // Invia un messaggio di disconnessione al server
                    break; // Esce dal loop
                } else if (inputMessage.equalsIgnoreCase("/enigma on")) { // Se l'utente vuole attivare Enigma
                    isEnigmaOn = true; // Imposta lo stato di Enigma su attivo
                    isAesOn = false; // Imposta lo stato di AES su disattivo
                    isCesarOn = false;
                    System.out.println("[+]Crittografia con enigma attivata");
                    continue; // Passa al prossimo ciclo del loop
                } else if (inputMessage.equalsIgnoreCase("/enigma off")) { // Se l'utente vuole disattivare Enigma
                    isEnigmaOn = false; // Imposta lo stato di Enigma su disattivo
                    System.out.println("[-]Crittografia con enigma disattivata");
                    continue; // Passa al prossimo ciclo del loop
                } else if (inputMessage.equalsIgnoreCase("/aes on")) { // Se l'utente vuole attivare AES
                    isAesOn = true; // Imposta lo stato di AES su attivo
                    isEnigmaOn = false; // Imposta lo stato di Enigma su disattivo
                    isCesarOn = false;
                    System.out.println("[+]Crittografia con AES attivata");
                    continue; // Passa al prossimo ciclo del loop
                } else if (inputMessage.equalsIgnoreCase("/aes off")) { // Se l'utente vuole disattivare AES
                    isAesOn = false; // Imposta lo stato di AES su disattivo
                    System.out.println("[-]Crittografia con AES disattivata");
                    continue; // Passa al prossimo ciclo del loop
                } else if (inputMessage.equalsIgnoreCase("/cesar on")) {
                    isCesarOn = true;
                    isAesOn = false; 
                    isEnigmaOn = false; // Imposta lo stato di Enigma su disattivo
                    System.out.println("[+]Crittografia con Cesar Cipher attivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/cesar off")) {
                    isCesarOn = false;
                    System.out.println("[-]Crittografia con Cesar Cipher disattivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/help")) {
                    System.out.println("[!]Crittografie disponibili: \n");
                    System.out.println("[!]Cesar Cipher (/cesar on , /cesar off): \n");
                    System.out.println("[!]Enigma (/enigma on , /enigma off): \n");
                    System.out.println("[!]AES256 (/aes on , /aes off): \n");
                    continue;
                }

                // Preparazione del messaggio con l'username
                String messageToSend = username + ": " + inputMessage; // Costruisce il messaggio da inviare al server

                // Applicazione della crittografia se necessario
                if (isAesOn) { // Se è attivo AES
                    try { // Prova a criptare il messaggio utilizzando AES
                        messageToSend = CryptoUtils.encrypt(messageToSend, sharedSecret); // Cripta il messaggio utilizzando AES
                    } catch (Exception e) { // Gestisce eventuali eccezioni
                        System.err.println("Errore nella crittografia del messaggio: " + e.getMessage()); // Stampa un messaggio di errore
                        continue; // Salta all'iterazione successiva del loop
                    }
                } else if (isEnigmaOn) { // Se è attivo Enigma
                    try { // Prova a cifrare il messaggio utilizzando Enigma
                        messageToSend = enigmaSimulator.cifraDecifra(messageToSend, true); // Cifra il messaggio utilizzando Enigma
                    } catch (Exception e) { // Gestisce eventuali eccezioni
                        System.err.println("Errore nella crittografia con Enigma: " + e.getMessage()); // Stampa un messaggio di errore
                        continue; // Salta all'iterazione successiva del loop
                    }
                 } else if (isCesarOn) { 
                        try { 
                            messageToSend = CifrarioDiCesare.cripta(messageToSend,cesar_shift);
                        } catch (Exception e) { 
                            System.err.println("Errore nella crittografia con Cesar Cihper: " + e.getMessage()); 
                            continue; 
                        }
                }

                out.println(messageToSend); // Invia il messaggio criptato al server
            }

        } catch (IOException e) { // Gestisce eventuali eccezioni di IO
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione
        }
    }

    private static String getPSK(String filename) { // Metodo privato per ottenere la chiave condivisa dal file di configurazione
        Properties prop = new Properties(); // Crea un nuovo oggetto Properties per gestire le proprietà
        try (FileInputStream fis = new FileInputStream(filename)) { // Apre un file di input stream per leggere le proprietà
            prop.load(fis); // Carica le proprietà dal file
            return prop.getProperty("sharedSecret"); // Restituisce il valore della chiave condivisa dal file di configurazione
        } catch (IOException e) { // Gestisce eventuali eccezioni di IO
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione
            return null; // Restituisce null in caso di errore
        }
    }

    private static int getCesarShift(String filename) { // Metodo corretto per ottenere lo shift del cifrario di Cesare dal file di configurazione
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(filename)) {
            prop.load(fis);
            String shiftString = prop.getProperty("cesar_shift");
            return Integer.parseInt(shiftString); // Converte la stringa in un intero
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0; // Restituisce 0 in caso di errore o se la conversione fallisce
        }
    }

    
}
