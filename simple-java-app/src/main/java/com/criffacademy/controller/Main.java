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
            LoginResponse loginResponse = null;
    
            if (userController.userExists(username)) {
                // Se l'utente esiste, tenta il login
                loginResponse = userController.user_login(username, password, "192.168.1.1", 8080);
                if (loginResponse == null || loginResponse.getJwt().equals("InvalidCredentials")) {
                    System.out.println("Credenziali non valide. Riprova.");
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
                }
            }
    
            if (loginResponse != null && loginResponse.getJwt() != null && !loginResponse.getJwt().isEmpty()) {
                System.out.println("Vuoi effettuare il logout? (s/n):");
                String logoutChoice = scanner.nextLine();
                if ("s".equalsIgnoreCase(logoutChoice)) {
                    userController.user_logout(loginResponse.getRefreshToken());
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
