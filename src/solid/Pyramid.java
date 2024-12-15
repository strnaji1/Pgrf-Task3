package solid;

import transforms.*;

import java.awt.*;

public class Pyramid extends Solid {
    public Pyramid() {
        setColor(Color.CYAN);

        // vertex buffer
        vb.add(new Point3D(0, 0, 0));
        vb.add(new Point3D(1, 0, 0));
        vb.add(new Point3D(1, 1, 0));
        vb.add(new Point3D(0, 1, 0));
        vb.add(new Point3D(0.5, 0.5, 2));

        // index buffer
        addIndices(
                0, 1,
                1, 2,
                2, 3,
                0, 3,
                0, 4,
                1, 4,
                2, 4,
                3, 4
        );

    }
}