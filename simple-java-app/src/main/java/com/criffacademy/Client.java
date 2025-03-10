package com.criffacademy;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket; 
import java.util.Properties;
import java.util.Scanner;

import com.criffacademy.service.CifrarioDiCesare;
import com.criffacademy.service.CryptoUtils;
import com.criffacademy.service.EnigmaSimulator;

public class Client {
    private static String sharedSecret = getPSK("app.properties");
    private static int cesar_shift     = getCesarShift("app.properties");
    private static boolean isEnigmaOn  = false;
    private static boolean isAesOn     = false;
    private static boolean isCesarOn   = false;

    private static EnigmaSimulator enigmaSimulator;

    static {
        try {
            enigmaSimulator = new EnigmaSimulator();
        } catch (IOException e) {
            System.err.println("Errore nel caricamento EnigmaSimulator: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Client <server-ip> <port> <username>");
            System.exit(1);
        }

        String serverIp = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];

        try (Socket socket = new Socket(serverIp, port);
             Scanner userInput = new Scanner(System.in);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("[+1]" + username);
            System.out.println("Connected to server. Start typing messages (type '/exit' to quit).");

            Thread serverListener = new Thread(() -> {
                try (Scanner in = new Scanner(socket.getInputStream())) {
                    while (in.hasNextLine()) {
                        String message = in.nextLine();
                        if (message.startsWith("[+]") || message.startsWith("[-]") || message.startsWith("[!]")) {
                            System.out.println(message);
                        } else {
                            try {
                                String decryptedMessage = message;
                                if (isAesOn) {
                                    decryptedMessage = CryptoUtils.decrypt(message, sharedSecret);
                                } else if (isEnigmaOn) {
                                    decryptedMessage = enigmaSimulator.cifraDecifra(message, false);
                                } else if (isCesarOn) {
                                    decryptedMessage = CifrarioDiCesare.decripta(message, cesar_shift);
                                }
                                System.out.println(decryptedMessage);
                            } catch (Exception e) {
                                System.out.println("Ricevuto messaggio non decriptabile");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverListener.start();

            while (true) {
                String inputMessage = userInput.nextLine();

                // Comandi speciali
                if (inputMessage.equalsIgnoreCase("/exit")) {
                    out.println("[-1]" + username);
                    break;
                } else if (inputMessage.equalsIgnoreCase("/enigma on")) {
                    isEnigmaOn = true;
                    isAesOn    = false;
                    isCesarOn  = false;
                    System.out.println("[+]Crittografia con enigma attivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/enigma off")) {
                    isEnigmaOn = false;
                    System.out.println("[-]Crittografia con enigma disattivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/aes on")) {
                    isAesOn    = true;
                    isEnigmaOn = false;
                    isCesarOn  = false;
                    System.out.println("[+]Crittografia con AES attivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/aes off")) {
                    isAesOn = false;
                    System.out.println("[-]Crittografia con AES disattivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/cesar on")) {
                    isCesarOn  = true;
                    isAesOn    = false;
                    isEnigmaOn = false;
                    System.out.println("[+]Crittografia con Cesar Cipher attivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/cesar off")) {
                    isCesarOn = false;
                    System.out.println("[-]Crittografia con Cesar Cipher disattivata");
                    continue;
                } else if (inputMessage.equalsIgnoreCase("/help")) {
                    System.out.println("[!]Crittografie disponibili:\n");
                    System.out.println("[!]Cesar Cipher (/cesar on, /cesar off)");
                    System.out.println("[!]Enigma (/enigma on, /enigma off)");
                    System.out.println("[!]AES256 (/aes on, /aes off)");
                    continue;
                }

                // Preparazione messaggio
                String messageToSend = username + ": " + inputMessage;

                // Eventuale crittografia
                if (isAesOn) {
                    try {
                        messageToSend = CryptoUtils.encrypt(messageToSend, sharedSecret);
                    } catch (Exception e) {
                        System.err.println("Errore nella crittografia AES: " + e.getMessage());
                        continue;
                    }
                } else if (isEnigmaOn) {
                    try {
                        messageToSend = enigmaSimulator.cifraDecifra(messageToSend, true);
                    } catch (Exception e) {
                        System.err.println("Errore nella crittografia Enigma: " + e.getMessage());
                        continue;
                    }
                } else if (isCesarOn) {
                    try {
                        messageToSend = CifrarioDiCesare.cripta(messageToSend, cesar_shift);
                    } catch (Exception e) {
                        System.err.println("Errore nella crittografia Cesar: " + e.getMessage());
                        continue;
                    }
                }

                out.println(messageToSend);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carica la sharedSecret da un file di properties nel classpath.
     */
    private static String getPSK(String resourceName) {
        Properties prop = new Properties();
        // Carichiamo app.properties come risorsa dal classpath
        try (InputStream is = Client.class.getResourceAsStream("/" + resourceName)) {
            if (is == null) {
                throw new IOException("Impossibile trovare il file nel classpath: " + resourceName);
            }
            prop.load(is);
            return prop.getProperty("sharedSecret");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Carica lo shift Cesare da un file di properties nel classpath.
     */
    private static int getCesarShift(String resourceName) {
        Properties prop = new Properties();
        try (InputStream is = Client.class.getResourceAsStream("/" + resourceName)) {
            if (is == null) {
                throw new IOException("Impossibile trovare il file nel classpath: " + resourceName);
            }
            prop.load(is);
            String shiftString = prop.getProperty("cesar_shift");
            return Integer.parseInt(shiftString.trim());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
