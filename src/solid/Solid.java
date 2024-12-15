package solid;

import transforms.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class Solid {
    protected ArrayList<Point3D> vb = new ArrayList<>();
    protected ArrayList<Integer> ib = new ArrayList<>();
    private Color color;
    private Mat4 model = new Mat4Identity();

    protected void addIndices(Integer... indices) {
        ib.addAll(Arrays.asList(indices));
    }

    public ArrayList<Point3D> getVb() {
        return vb;
    }

    public ArrayList<Integer> getIb() {
        return ib;
    }

    public Mat4 getModel() {
        return model;
    }

    public void setModel(Mat4 model) {
        this.model = model;
    }

    public Color getColor() {
        return this.color;
    }
    public void setColor(Color color){
        this.color = color;
    }

    public void translate(double x, double y, double z){
        Mat4 matTransl = new Mat4Transl(x, y, z);
        model = model.mul(matTransl);
    }
    public void zoom(double scale){
        Mat4 matZoom = new Mat4Scale(scale);
        model = model.mul(matZoom);
    }
    public void rotate(double alpha, double beta, double gamma){
        Mat4 matRot = new Mat4RotXYZ(alpha, beta, gamma);
        model = model.mul(matRot);
    }
    public void rotateX(double alpha){
        Mat4 matRotX = new Mat4RotX(alpha);
        model = model.mul(matRotX);
    }
    public void rotateY(double alpha){
        Mat4 matRotY = new Mat4RotY(alpha);
        model = model.mul(matRotY);
    }
    public void rotateZ(double alpha){
        Mat4 matRotZ = new Mat4RotZ(alpha);
        model = model.mul(matRotZ);
    }
    public Point3D getTransformedVertex(int index) {
        Point3D vertex = vb.get(index);
        return vertex.mul(model); // Aplikace transformační matice
    }
}
