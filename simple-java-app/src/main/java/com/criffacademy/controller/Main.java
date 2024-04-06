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
            // Verifica se l'utente esiste già
            if (userController.userExists(username)) {
                // Se l'utente esiste, tenta il login
                String jwt = userController.user_login(username, password);
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
                    String jwt = userController.user_login(username, password);
                    System.out.println("Il tuo JWT è: " + jwt);
                } else {
                    System.out.println("Errore durante la registrazione: " + signUpResult);
                }
            }
        } catch (Exception e) {
            System.out.println("Si è verificato un errore: " + e.getMessage());
        }

        scanner.close();
    }
}
