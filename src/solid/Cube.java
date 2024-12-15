package solid;

import transforms.Mat4;
import transforms.Mat4Scale;
import transforms.Mat4Transl;
import transforms.Point3D;

import java.awt.*;

public class Cube extends Solid {

    public Cube() {

        setColor(Color.red);
        // vertex buffer
        vb.add(new Point3D(0, 0, 0));
        vb.add(new Point3D(1, 0, 0));
        vb.add(new Point3D(1, 1, 0));
        vb.add(new Point3D(0, 1, 0));
        vb.add(new Point3D(0, 0, 1));
        vb.add(new Point3D(1, 0, 1));
        vb.add(new Point3D(1, 1, 1));
        vb.add(new Point3D(0, 1, 1));

        // index buffer
        addIndices(
                0, 1,
                1, 2,
                2, 3,
                3, 0,
                0, 4,
                1, 5,
                2, 6,
                3, 7,
                4, 5,
                5, 6,
                6, 7,
                7, 4
        );

    }
}
