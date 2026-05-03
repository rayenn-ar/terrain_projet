package com.clubsportif;

/**
 * Point d'entrée pour le fichier JAR exécutable (java -jar).
 *
 * Cette classe intermédiaire est nécessaire car JavaFX (depuis Java 9)
 * interdit le lancement direct d'une classe qui étend Application
 * via java -jar. Ce Launcher contourne cette restriction.
 *
 * Usage : java -jar terrain-sportif.jar
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
