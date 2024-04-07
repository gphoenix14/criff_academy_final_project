package com.criffacademy.controller;

import java.sql.Timestamp;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.criffacademy.general.LoginResponse;

public class Main {
    public static void main(String[] args) {
        UserController userController = new UserController();
        GroupController groupController = new GroupController();
        MessageController messageController = new MessageController();
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("Benvenuto! Inserisci il tuo username:");
        String username = scanner.nextLine();
        System.out.println("Inserisci la tua password:");
        String password = scanner.nextLine();
    
        try {
            LoginResponse loginResponse = null;
    
            if (userController.userExists(username)) {
                // Se l'utente esiste, tenta il login
                loginResponse = userController.user_login(username, password, "192.168.1.1", 8080);
                if (loginResponse == null || loginResponse.getJwt().equals("InvalidCredentials")) {
                    System.out.println("Credenziali non valide. Riprova.");
                    return; // Termina l'esecuzione se il login non è riuscito
                } else {
                    System.out.println("Login effettuato con successo. Il tuo JWT è: " + loginResponse.getJwt());
                }
            } else {
                // Se l'utente non esiste, invoca il metodo di sign up
                String signupResult = userController.user_signup(username, password);
                System.out.println(signupResult);
                
                // Dopo la registrazione, effettua il login per ottenere JWT e refresh token
                loginResponse = userController.user_login(username, password, "192.168.1.1", 8080);
                if (loginResponse != null && !loginResponse.getJwt().isEmpty()) {
                    System.out.println("Registrazione e login effettuati con successo. Il tuo JWT è: " + loginResponse.getJwt());
                } else {
                    System.out.println("Errore nel processo di login dopo la registrazione.");
                    return; // Termina l'esecuzione se il login dopo la registrazione non è riuscito
                }
            }
    
            // Chiede all'utente se vuole creare o accedere a un gruppo
            System.out.println("Vuoi creare o accedere a un gruppo? (crea/accedi):");
            String sceltaGruppo = scanner.nextLine();
            
            if ("crea".equalsIgnoreCase(sceltaGruppo)) {
                System.out.println("Inserisci il nome del gruppo:");
                String groupName = scanner.nextLine();
                System.out.println("Definisci una password per il gruppo:");
                String groupPassword = scanner.nextLine();
                System.out.println("Inserisci la chiave PSK per Enigma:");
                String enigmaPSK = scanner.nextLine();
                System.out.println("Inserisci la chiave PSK per AES:");
                String aesPSK = scanner.nextLine();
                System.out.println("Inserisci il valore di shift per Cesare:");
                int cesarShift = Integer.parseInt(scanner.nextLine());
                System.out.println("Scegli l'algoritmo di crittografia predefinito (1 per Enigma, 2 per AES, 3 per Cesare):");
                int defaultCrypto = Integer.parseInt(scanner.nextLine());
            
                groupController.createGroup(username, loginResponse.getJwt(), groupName, groupPassword, enigmaPSK, aesPSK, cesarShift, defaultCrypto);
                System.out.println("Gruppo creato con successo.");
            } else if ("accedi".equalsIgnoreCase(sceltaGruppo)) {
                System.out.println("Inserisci il nome del gruppo a cui vuoi accedere:");
                String groupName = scanner.nextLine();
                System.out.println("Inserisci la password del gruppo:");
                String groupPassword = scanner.nextLine();
                // Verifica l'accesso al gruppo
                if (groupController.verifyGroupPassword(groupName, groupPassword, loginResponse.getJwt())) {
                    System.out.println("Accesso al gruppo " + groupName + " effettuato con successo.");
                } else {
                    System.out.println("Password del gruppo non corretta. Accesso negato.");
                }
            }

            System.out.println("Vuoi inviare un messaggio? (s/n):");
            String sceltaInvioMessaggio = scanner.nextLine();
            
            if ("s".equalsIgnoreCase(sceltaInvioMessaggio)) {
                // Qui assumiamo che l'utente possa scegliere di inviare un messaggio a un gruppo o a un singolo utente.
                System.out.println("Il messaggio è per un gruppo o per un utente? (gruppo/utente):");
                String tipoDestinazione = scanner.nextLine();
                
                String groupDstName = null;
                String userDstUsername = null;
                if ("gruppo".equalsIgnoreCase(tipoDestinazione)) {
                    System.out.println("Inserisci il nome del gruppo destinatario del messaggio:");
                    groupDstName = scanner.nextLine();
                } else if ("utente".equalsIgnoreCase(tipoDestinazione)) {
                    System.out.println("Inserisci l'username dell'utente destinatario del messaggio:");
                    userDstUsername = scanner.nextLine();
                }
                
                // Chiede se il messaggio è unicast, multicast o broadcast
                System.out.println("Il messaggio è unicast, multicast o broadcast? (unicast/multicast/broadcast):");
                String tipoMessaggio = scanner.nextLine();
                boolean isUnicast = "unicast".equalsIgnoreCase(tipoMessaggio);
                boolean isMulticast = "multicast".equalsIgnoreCase(tipoMessaggio);
                boolean isBroadcast = "broadcast".equalsIgnoreCase(tipoMessaggio);
                
                // Chiede se il messaggio ha un allegato
                System.out.println("Il messaggio ha un allegato? (s/n):");
                boolean hasAttachment = "s".equalsIgnoreCase(scanner.nextLine());
                
                int attachmentId = 0;
                if (hasAttachment) {
                    System.out.println("Inserisci l'ID dell'allegato:");
                    attachmentId = Integer.parseInt(scanner.nextLine());
                }
                
                // Chiede il testo del messaggio
                System.out.println("Inserisci il testo del messaggio:");
                String msgText = scanner.nextLine();

                Timestamp now = new Timestamp(System.currentTimeMillis());

                // Invia il messaggio con il timestamp corrente
                try {
                    messageController.sendMessage(username, loginResponse.getJwt(), isUnicast, isMulticast, isBroadcast, groupDstName, userDstUsername, hasAttachment, attachmentId, msgText, now);
                } catch (Exception e) {
                    System.out.println("Si è verificato un errore durante l'invio del messaggio: " + e.getMessage());
                }
            }

            System.out.println("Vuoi leggere i nuovi messaggi? (s/n):");
String sceltaLetturaMessaggi = scanner.nextLine();

if ("s".equalsIgnoreCase(sceltaLetturaMessaggi)) {
    System.out.println("Inserisci il nome del gruppo da cui vuoi leggere i messaggi:");
    String groupName = scanner.nextLine();
    System.out.println("Inserisci la data e ora da cui vuoi iniziare a leggere i messaggi (formato YYYY-MM-DD HH:MM:SS):");
    String datetime = scanner.nextLine();

    try {
        messageController.readMessages(username, groupName, loginResponse.getJwt(), datetime);
    } catch (Exception e) {
        System.out.println("Si è verificato un errore durante la lettura dei messaggi: " + e.getMessage());
    }
}

System.out.println("Vuoi effettuare il logout? (s/n):");
    
            String logoutChoice = scanner.nextLine();
            if ("s".equalsIgnoreCase(logoutChoice)) {
                userController.user_logout(loginResponse.getRefreshToken());
                System.out.println("Logout effettuato con successo.");
            }
        } catch (Exception e) {
            System.out.println("Si è verificato un errore: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}