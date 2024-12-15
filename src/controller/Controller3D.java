package controller;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import raster.RasterBufferedImage;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import render.CurveRenderer;
import render.WireRenderer;
import solid.*;
import transforms.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Controller3D {

    private JPanel panel;
    private RasterBufferedImage raster;
    private LineRasterizer lineRasterizer;
    private WireRenderer wireRenderer;
    private Solid cube;
    private Solid pyramid;
    private Solid cuboid;
    private Solid axisX;
    private Solid axisY;
    private Solid axisZ;
    private CurveRenderer curveRenderer;

    private Camera camera;
    private Mat4 proj;
    private Point prevPoint;
    private Point curPoint;
    private Solid pentagonalPrism;
    // Proměnné pro animaci
    private Timer animationTimer;
    private long animationStartTime;
    private double rotationAngle = 0; // Úhel rotace
    private double cubeRotationX = 0; // Rotace kolem osy X
    private double cubeRotationY = 0; // Rotace kolem osy Y
    private boolean movingUp = true;

    public Controller3D(int width, int height) {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Projekce: Perspektivní");
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        raster = new RasterBufferedImage(width, height);
        lineRasterizer = new LineRasterizerGraphics(raster);
        wireRenderer = new WireRenderer(lineRasterizer, raster);
        curveRenderer = new CurveRenderer(wireRenderer);

        panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        panel.requestFocus();
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> camera = camera.left(0.1);
                    case KeyEvent.VK_D -> camera = camera.right(0.1);
                    case KeyEvent.VK_W -> camera = camera.up(0.1);
                    case KeyEvent.VK_S -> camera = camera.down(0.1);
                    case KeyEvent.VK_Q -> camera = camera.forward(0.1);
                    case KeyEvent.VK_E -> camera = camera.backward(0.1);

                    case KeyEvent.VK_P -> {
                        proj = new Mat4PerspRH(
                                Math.PI / 3,
                                raster.getHeight() / (double) raster.getWidth(),
                                0.2,
                                20.0
                        );
                        wireRenderer.setProj(proj); // Nastavení projekce ve WireRenderer
                        frame.setTitle("Projekce: Perspektivní");
                    }
                    case KeyEvent.VK_O -> {
                        double aspectRatio = raster.getWidth() / (double) raster.getHeight();
                        double height = 4.0;
                        double width = height * aspectRatio;

                        proj = new Mat4OrthoRH(width, height, 0.2, 20.0);
                        wireRenderer.setProj(proj); // Nastavení projekce ve WireRenderer
                        frame.setTitle("Projekce: Paralelní");
                    }
                    case KeyEvent.VK_I -> { // Přepnutí na pravoúhlou projekci
                        double aspectRatio = raster.getWidth() / (double) raster.getHeight();
                        double height = 4.0; // Výška zobrazovacího prostoru
                        double width = height * aspectRatio;

                        proj = new Mat4OrthoRH(width, height, 0.1, 100.0); // Pravoúhlá projekce
                        wireRenderer.setProj(proj); // Aktualizace projekční matice v rendereru
                        frame.setTitle("Projekce: Pravoúhlá");
                        System.out.println("Přepnuto na pravoúhlou projekci.");
                    }
                    case KeyEvent.VK_UP -> cubeRotationX += Math.toRadians(5); // Rotace kolem osy X
                    case KeyEvent.VK_DOWN -> cubeRotationX -= Math.toRadians(5); // Opačná rotace kolem osy X
                    case KeyEvent.VK_LEFT -> cubeRotationY -= Math.toRadians(5); // Rotace kolem osy Y
                    case KeyEvent.VK_RIGHT -> cubeRotationY += Math.toRadians(5); // Opačná rotace kolem osy Y

                    case KeyEvent.VK_T -> {
                        rotationAngle += Math.toRadians(1); // Zvětšení úhlu rotace
                        drawScene();
                    }
                    case KeyEvent.VK_R -> {
                        resetView(); // Restart výhledu
                    }
                    case KeyEvent.VK_V -> startAnimation();
                }
                drawScene();
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevPoint = e.getPoint();
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                curPoint = e.getPoint();

                if (curPoint.x > prevPoint.x)
                    camera = camera.addAzimuth(-0.025);

                if (curPoint.x < prevPoint.x)
                    camera = camera.addAzimuth(0.025);

                if (curPoint.y > prevPoint.y && camera.getZenith() >= -90)
                    camera = camera.addZenith(-0.025);

                if (curPoint.y < prevPoint.y && camera.getZenith() <= 90)
                    camera = camera.addZenith(0.025);

                prevPoint = curPoint;
                drawScene();
            }
        });

        initScene();
    }

    private void resetView() {
        Vec3D cameraPosition = new Vec3D(0.9, 0.9, 1);
        Vec3D viewVector = new Vec3D(-0.6, -0.6, -0.5).normalized().orElse(new Vec3D());
        double azimuth = Math.atan2(viewVector.getX(), viewVector.getZ());
        double zenith = Math.asin(viewVector.getY() / viewVector.length());

        camera = new Camera(
                cameraPosition,
                azimuth,
                zenith,
                1,
                true
        );

        proj = new Mat4PerspRH(
                Math.PI / 3,
                raster.getHeight() / (double) raster.getWidth(),
                0.2,
                20.0
        );
        wireRenderer.setProj(proj); // Aktualizace projekce ve WireRenderer
        wireRenderer.setView(camera.getViewMatrix()); // Nastavení view ve WireRenderer
    }

    public void initScene() {
        resetView();

        axisX = new AxisX();
        axisY = new AxisY();
        axisZ = new AxisZ();
        cube = new Cube();
        cube.translate(-2, -2, 0);
        cube.zoom(0.2);
        pyramid = new Pyramid();
        pyramid.translate(-3, 2, -2);
        pyramid.zoom(0.2);
        pyramid.rotateY(Math.PI / 4);
        cuboid = new Cuboid();
        cuboid.translate(2, -2, 0);
        cuboid.rotateY(Math.PI / 3);
        cuboid.zoom(0.2);
        pentagonalPrism = new PentagonalPrism();
        pentagonalPrism.translate(3.5, 3.5, 0); // Posun hranolu
        pentagonalPrism.zoom(0.1); // Zmenšení objektu
    }

    public void drawScene() {
        clear(0xaaaaaa);

        wireRenderer.setProj(proj);
        wireRenderer.setView(camera.getViewMatrix());



        wireRenderer.render(axisX);
        wireRenderer.render(axisY);
        wireRenderer.render(axisZ);

        wireRenderer.render(cube);
        wireRenderer.render(pyramid);
        wireRenderer.render(cuboid);

        wireRenderer.setColor(Color.CYAN);
        curveRenderer.renderFergusonCurve(cuboid, 50);

        wireRenderer.setColor(Color.MAGENTA);
        curveRenderer.renderBezierCurve(cube, 50, rotationAngle);

        wireRenderer.setColor(Color.GREEN);
        curveRenderer.renderCoonsCurve(pyramid, 50); // Coonsova křivka pro pyramidu
// Vykreslení hranolu
        wireRenderer.render(pentagonalPrism);
        panel.repaint();
    }

    // Metoda pro spuštění animace
    private void startAnimation() {
        animationStartTime = System.currentTimeMillis(); // Uložení času startu

        // Timer se spouští každých 16 ms (přibližně 60 FPS)
        animationTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimation(); // Aktualizace animace
            }
        });

        animationTimer.start(); // Spuštění časovače
    }

    // Metoda pro aktualizaci animace
    private void updateAnimation() {
        long elapsedTime = System.currentTimeMillis() - animationStartTime; // Čas od spuštění

        // Získání aktuální modelovací matice
        Mat4 baseModelMatrix = pentagonalPrism.getModel();

        // Základní posun a zoom při inicializaci
        Mat4 initialTranslation = new Mat4Transl(3.5, 3.5, 0); // Počáteční posun
        Mat4 initialScale = new Mat4Scale(0.1); // Konstantní zoom

        Mat4 animationTranslation; // Animovaný posun

        // Posun nahoru (prvních 2 sekundy)
        if (elapsedTime <= 2000) {
            double progress = elapsedTime / 2000.0; // Progres animace (0 až 1)
            animationTranslation = new Mat4Transl(0, 2 * progress, 0); // Posun nahoru
        }
        // Posun dolů (dalších 2 sekundy)
        else if (elapsedTime <= 4000) {
            double progress = (elapsedTime - 2000) / 2000.0; // Progres animace (0 až 1)
            animationTranslation = new Mat4Transl(0, 2 * (1 - progress), 0); // Posun dolů
        }
        // Konec animace
        else {
            animationTimer.stop(); // Zastavení časovače
            animationTranslation = new Mat4Transl(0, 0, 0); // Žádný další posun
        }

        // Kombinace inicializačního posunu, animace a zoomu
        Mat4 modelMatrix = initialTranslation.mul(animationTranslation).mul(initialScale);
        pentagonalPrism.setModel(modelMatrix); // Nastavení modelovací matice

        drawScene(); // Aktualizace vykreslení
    }





    public void clear(int color) {
        raster.setClearColor(color);
        raster.clear();
    }

    public void present(Graphics graphics) {
        raster.repaint(graphics);
    }

    public void start() {
        drawScene();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Controller3D(1280, 720).start());
    }
}
