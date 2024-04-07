package com.criffacademy.controller;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserController userController = new UserController();
        GroupController groupController = new GroupController();
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
    
            // Chiede all'utente se vuole effettuare il logout
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