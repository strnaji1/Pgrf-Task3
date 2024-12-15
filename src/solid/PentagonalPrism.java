package solid;

import transforms.Point3D;

import java.awt.*;

public class PentagonalPrism extends Solid {
    public PentagonalPrism() {
        setColor(Color.MAGENTA); // Barva hranolu

        int sides = 5; // Počet stran základny
        double radius = 1.0; // Poloměr základny
        double height = 2.0; // Výška hranolu

        // Generování vrcholů dolní základny
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            vb.add(new Point3D(
                    radius * Math.cos(angle), // X souřadnice
                    radius * Math.sin(angle), // Y souřadnice
                    0.0                       // Z souřadnice (dolní základna)
            ));
        }

        // Generování vrcholů horní základny
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            vb.add(new Point3D(
                    radius * Math.cos(angle), // X souřadnice
                    radius * Math.sin(angle), // Y souřadnice
                    height                    // Z souřadnice (horní základna)
            ));
        }

        // Indexy pro dolní základnu
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides; // Cyklování indexů
            addIndices(i, next); // Hrana mezi vrcholy dolní základny
        }

        // Indexy pro horní základnu
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides; // Cyklování indexů
            addIndices(sides + i, sides + next); // Hrana mezi vrcholy horní základny
        }

        // Boční stěny (bez úhlopříček)
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides; // Cyklování indexů

            // Boční plocha jako čtyřúhelník
            addIndices(i, next);       // Dolní hrana
            addIndices(next, sides + next); // Spoj horní základny
            addIndices(sides + next, sides + i); // Horní hrana
            addIndices(sides + i, i);  // Spoj dolní základny
        }
    }
}
