package com.criffacademy.controller;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserController userController = new UserController();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Benvenuto! Inserisci il tuo username:");
        String username = scanner.nextLine();
        System.out.println("Inserisci la tua password:");
        String password = scanner.nextLine();

        try {
            // Inizialmente, il token JWT è vuoto
            String jwt = "";
            
            // Verifica se l'utente esiste già
            if (userController.userExists(username)) {
                // Se l'utente esiste, tenta il login
                jwt = userController.user_login(username, password,"192.168.1.1",8080);
                if ("InvalidCredentials".equals(jwt)) {
                    System.out.println("Credenziali non valide. Riprova.");
                } else {
                    System.out.println("Login effettuato con successo. Il tuo JWT è: " + jwt);
                }
            } else {
                // Se l'utente non esiste, procede con la registrazione
                String signUpResult = userController.user_signup(username, password);
                if ("Signed up successfully!".equals(signUpResult)) {
                    System.out.println("Registrazione effettuata con successo. Ora verrà effettuato il login.");
                    // Esegue automaticamente il login dopo la registrazione
                    jwt = userController.user_login(username, password,"192.168.1.1",8080);
                    System.out.println("Il tuo JWT è: " + jwt);
                } else {
                    System.out.println("Errore durante la registrazione: " + signUpResult);
                }
            }
            
            // Dopo il login/registrazione, chiedi se effettuare il logout
            if (!jwt.isEmpty() && !"InvalidCredentials".equals(jwt)) {
                System.out.println("Vuoi effettuare il logout? (s/n):");
                String logoutChoice = scanner.nextLine();
                if ("s".equalsIgnoreCase(logoutChoice)) {
                    userController.user_logout(jwt);
                    System.out.println("Logout effettuato con successo.");
                }
            }
        } catch (Exception e) {
            System.out.println("Si è verificato un errore: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
