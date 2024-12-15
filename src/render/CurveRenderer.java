package render;

import solid.Cube;
import solid.Cuboid;
import solid.Pyramid;
import solid.Solid;
import transforms.*;

public class CurveRenderer {
    private final WireRenderer wireRenderer;

    public CurveRenderer(WireRenderer wireRenderer) {
        this.wireRenderer = wireRenderer;
    }

    public void renderBezierCurve(Solid solid, int samples, double rotationAngle) {
        // Kontrolní body
        Point3D p1 = solid.getTransformedVertex(0);
        Point3D p4 = solid.getTransformedVertex(6);

        Point3D p2 = new Point3D(
                (p1.getX() + p4.getX()) / 2,
                p1.getY() - 1.0,
                (p1.getZ() + p4.getZ()) / 2
        );
        Point3D p3 = new Point3D(
                (p1.getX() + p4.getX()) / 2,
                p4.getY() + 1.0,
                (p1.getZ() + p4.getZ()) / 2
        );

        // Výpočet středu křivky
        Point3D center = new Point3D(
                (p1.getX() + p2.getX() + p3.getX() + p4.getX()) / 4,
                (p1.getY() + p2.getY() + p3.getY() + p4.getY()) / 4,
                (p1.getZ() + p2.getZ() + p3.getZ() + p4.getZ()) / 4
        );

        // Matice rotace kolem středu křivky
        Mat4 rotationMatrix = new Mat4Transl(-center.getX(), -center.getY(), -center.getZ())
                .mul(new Mat4RotZ(rotationAngle)) // Rotace kolem osy Z
                .mul(new Mat4Transl(center.getX(), center.getY(), center.getZ()));

        // Transformace kontrolních bodů
        p1 = p1.mul(rotationMatrix);
        p2 = p2.mul(rotationMatrix);
        p3 = p3.mul(rotationMatrix);
        p4 = p4.mul(rotationMatrix);

        // Vytvoření Bézierovy křivky s transformovanými body
        Cubic bezierCurve = new Cubic(Cubic.BEZIER, p1, p2, p3, p4);

        Point3D prevPoint = bezierCurve.compute(0).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
        if (!prevPoint.dehomog().isPresent()) return;
        Vec3D prevDehomog = prevPoint.dehomog().get();
        Vec3D prevWindow = wireRenderer.transformToWindow(prevDehomog);

        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;

            Point3D curPoint = bezierCurve.compute(t).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
            if (!curPoint.dehomog().isPresent()) continue;

            Vec3D curDehomog = curPoint.dehomog().get();
            Vec3D curWindow = wireRenderer.transformToWindow(curDehomog);

            wireRenderer.getLineRasterizer().rasterize(
                    (int) Math.round(prevWindow.getX()), (int) Math.round(prevWindow.getY()),
                    (int) Math.round(curWindow.getX()), (int) Math.round(curWindow.getY()),
                    wireRenderer.getColor()
            );

            prevWindow = curWindow;
        }
    }


    public void renderFergusonCurve(Solid solid, int samples) {
        // Kontrola, zda solid je Cuboid
        if (!(solid instanceof Cuboid)) {
            System.out.println("Fergusonova křivka se aplikuje pouze na Cuboid.");
            return;
        }

        // Vrcholy kvádru
        Point3D start = solid.getTransformedVertex(0); // Dolní přední levý roh
        Point3D end = solid.getTransformedVertex(6);   // Horní zadní pravý roh

        // Výpočet tečných vektorů
        Vec3D tangentStart = end.ignoreW().sub(start.ignoreW()).mul(0.3);
        Vec3D tangentEnd = start.ignoreW().sub(end.ignoreW()).mul(0.3);

        // Kontrolní body
        Point3D control1 = start.add(new Point3D(tangentStart));
        Point3D control2 = end.add(new Point3D(tangentEnd));

        // Vytvoření Fergusonovy křivky
        Cubic fergusonCurve = new Cubic(Cubic.FERGUSON, start, end, control1, control2);

        Point3D prevPoint = fergusonCurve.compute(0).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
        if (!prevPoint.dehomog().isPresent() || !wireRenderer.isInsideFrustum(prevPoint)) return;
        Vec3D prevDehomog = prevPoint.dehomog().get();
        Vec3D prevWindow = wireRenderer.transformToWindow(prevDehomog);

        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;

            Point3D curPoint = fergusonCurve.compute(t).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
            if (!curPoint.dehomog().isPresent()) continue;
            Vec3D curDehomog = curPoint.dehomog().get();
            Vec3D curWindow = wireRenderer.transformToWindow(curDehomog);

            wireRenderer.getLineRasterizer().rasterize(
                    (int) Math.round(prevWindow.getX()), (int) Math.round(prevWindow.getY()),
                    (int) Math.round(curWindow.getX()), (int) Math.round(curWindow.getY()),
                    wireRenderer.getColor()
            );

            prevWindow = curWindow;
        }
    }
    public void renderCoonsCurve(Solid solid, int samples) {
        // Kontrola, zda solid je Pyramid
        if (!(solid instanceof Pyramid)) {
            System.out.println("Coonsova křivka se aplikuje pouze na Pyramid.");
            return;
        }

        // Koncové body
        Point3D start = solid.getTransformedVertex(4); // Špička pyramidy
        Point3D end = solid.getTransformedVertex(1);   // Jeden roh základny

        // Kontrolní body - Zvýšená délka a jemné zakřivení
        Point3D control1 = new Point3D(
                start.getX() + (end.getX() - start.getX()) * 0.5, // Posun blíže k end
                start.getY() - 1.0,                               // Jemné snížení
                start.getZ() + (end.getZ() - start.getZ()) * 1.5  // Prodloužení v Z směru
        );

        Point3D control2 = new Point3D(
                start.getX() + (end.getX() - start.getX()) * 1.5, // Posun blíže k start
                end.getY() + 1.0,                                 // Jemné zvýšení
                start.getZ() + (end.getZ() - start.getZ()) * 0.5  // Prodloužení v Z směru
        );

        // Vytvoření Coonsovy křivky
        Cubic coonsCurve = new Cubic(Cubic.COONS, start, control1, control2, end);

        // Počáteční bod
        Point3D prevPoint = coonsCurve.compute(0).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
        if (!prevPoint.dehomog().isPresent() || !wireRenderer.isInsideFrustum(prevPoint)) return;
        Vec3D prevDehomog = prevPoint.dehomog().get();
        Vec3D prevWindow = wireRenderer.transformToWindow(prevDehomog);

        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;

            // Výpočet aktuálního bodu na křivce
            Point3D curPoint = coonsCurve.compute(t).mul(wireRenderer.getView()).mul(wireRenderer.getProj());
            if (!curPoint.dehomog().isPresent()) continue;
            Vec3D curDehomog = curPoint.dehomog().get();
            Vec3D curWindow = wireRenderer.transformToWindow(curDehomog);

            // Vykreslení úsečky mezi předchozím a aktuálním bodem
            wireRenderer.getLineRasterizer().rasterize(
                    (int) Math.round(prevWindow.getX()), (int) Math.round(prevWindow.getY()),
                    (int) Math.round(curWindow.getX()), (int) Math.round(curWindow.getY()),
                    wireRenderer.getColor()
            );

            prevWindow = curWindow; // Posun na další bod
        }
    }









}
