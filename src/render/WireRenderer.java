package render;

import raster.RasterBufferedImage;
import rasterize.LineRasterizer;
import solid.Solid;
import transforms.Mat4;
import transforms.Mat4Identity;
import transforms.Point3D;
import transforms.Vec3D;

import java.awt.*;
import java.util.ArrayList;

public class WireRenderer {

    private LineRasterizer lineRasterizer;
    private Mat4 view, proj;

    private int width, height;
    private RasterBufferedImage raster;
    private Color currentColor;

    public WireRenderer(LineRasterizer lineRasterizer, RasterBufferedImage raster) {
        this.lineRasterizer = lineRasterizer;
        this.view = new Mat4Identity();
        this.proj = new Mat4Identity();
        this.raster = raster;
    }

    public void render(Solid solid) {
        lineRasterizer.setColor(solid.getColor());
        for (int i = 0; i < solid.getIb().size(); i += 2) {
            int indexA = solid.getIb().get(i);
            int indexB = solid.getIb().get(i + 1);

            // Získání bodů
            Point3D a = solid.getVb().get(indexA);
            Point3D b = solid.getVb().get(indexB);

            // Transformace bodů pomocí modelové, pohledové a projekční matice
            a = a.mul(solid.getModel()).mul(view).mul(proj);
            b = b.mul(solid.getModel()).mul(view).mul(proj);

            // Ořezávání
            if (!(-a.getW() <= a.getX() && a.getX() <= a.getW() &&
                    -a.getW() <= a.getY() && a.getY() <= a.getW() &&
                    0 <= a.getZ() && a.getZ() <= a.getW())) {
                continue;
            }
            if (!(-b.getW() <= b.getX() && b.getX() <= b.getW() &&
                    -b.getW() <= b.getY() && b.getY() <= b.getW() &&
                    0 <= b.getZ() && b.getZ() <= b.getW())) {
                continue;
            }

            // Dehomogenizace
            var aDehomog = a.dehomog().get();
            var bDehomog = b.dehomog().get();

            // Transformace do okna
            Vec3D va = transformToWindow(aDehomog);
            Vec3D vb = transformToWindow(bDehomog);

            // Rasterizace čáry
            lineRasterizer.rasterize(
                    (int) Math.round(va.getX()), (int) Math.round(va.getY()),
                    (int) Math.round(vb.getX()), (int) Math.round(vb.getY()),
                    Color.YELLOW
            );
        }
    }

    public Vec3D transformToWindow(Vec3D v) {
        return v.mul(new Vec3D(1, -1, 1))
                .add(new Vec3D(1, 1, 0))
                .mul(new Vec3D((raster.getWidth() - 1) / 2., (raster.getHeight() - 1) / 2., 1));
    }

    public void renderScene(ArrayList<Solid> scene) {
        for (Solid solid : scene)
            render(solid);
    }

    public void renderLine(Point3D p1, Point3D p2) {
        // Transformace bodů pomocí projekční a pohledové matice
        Vec3D v1 = transformToWindow(p1.mul(view).mul(proj).ignoreW());
        Vec3D v2 = transformToWindow(p2.mul(view).mul(proj).ignoreW());

        // Vykreslení čáry mezi transformovanými body
        lineRasterizer.rasterize(
                (int) Math.round(v1.getX()), (int) Math.round(v1.getY()),
                (int) Math.round(v2.getX()), (int) Math.round(v2.getY()),
                Color.YELLOW
        );
    }
    public boolean isInsideFrustum(Point3D point) {
        double w = point.getW();
        return -w <= point.getX() && point.getX() <= w &&
                -w <= point.getY() && point.getY() <= w &&
                0 <= point.getZ() && point.getZ() <= w;
    }

    public void setView(Mat4 view) {
        this.view = view;
    }

    public void setProj(Mat4 proj) {
        this.proj = proj;
    }

    // Přidané gettery
    public Mat4 getView() {
        return this.view;
    }

    public Mat4 getProj() {
        return this.proj;
    }

    public void setColor(Color color) {
        lineRasterizer.setColor(color);
    }

    public Color getColor() {
        return currentColor; // Vrácení aktuální barvy
    }

    public LineRasterizer getLineRasterizer() {
        return lineRasterizer;
    }
}
