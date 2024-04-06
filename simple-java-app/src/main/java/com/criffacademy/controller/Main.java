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
                loginResponse = userController.user_login(username, password, "192.168.1.1", 8080);
                if (loginResponse == null || "InvalidCredentials".equals(loginResponse.getJwt())) {
                    System.out.println("Credenziali non valide. Riprova.");
                } else {
                    System.out.println("Login effettuato con successo. Il tuo JWT è: " + loginResponse.getJwt());
                }
            } else {
                // Gestione registrazione...
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
