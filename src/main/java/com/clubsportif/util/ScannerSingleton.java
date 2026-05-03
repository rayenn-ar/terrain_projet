package com.clubsportif.util;

import java.util.Scanner;

/**
 * Singleton wrappant java.util.Scanner pour garantir une instance unique
 * et éviter les conflits entre nextLine() / nextInt().
 */
public class ScannerSingleton {

    private static ScannerSingleton instance;
    private final Scanner scanner;

    private ScannerSingleton() {
        this.scanner = new Scanner(System.in);
    }

    public static synchronized ScannerSingleton getInstance() {
        if (instance == null) {
            instance = new ScannerSingleton();
        }
        return instance;
    }

    /** Lit une ligne entière (sûr même après nextInt). */
    public String nextLine() {
        return scanner.nextLine().trim();
    }

    /**
     * Lit un entier.
     * Vide le buffer après la lecture pour éviter les artefacts de nextLine().
     *
     * @return L'entier saisi, ou -1 si la saisie est invalide
     */
    public int nextInt() {
        String line = scanner.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Lit un entier avec message de prompt.
     */
    public int nextInt(String prompt) {
        System.out.print(prompt);
        return nextInt();
    }

    /**
     * Lit une ligne avec message de prompt.
     */
    public String nextLine(String prompt) {
        System.out.print(prompt);
        return nextLine();
    }

    /**
     * Attend que l'utilisateur appuie sur Entrée.
     */
    public void attendreEntree() {
        System.out.print(ConsoleColors.info("  Appuyez sur Entrée pour continuer..."));
        scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
