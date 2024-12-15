package solid;

import transforms.Point3D;

import java.awt.*;

public class AxisY extends Solid{
    public AxisY() {
        setColor(Color.pink);

        vb.add(new Point3D(0, -0.5, 0));
        vb.add(new Point3D(0, 0.8, 0));

        addIndices(0, 1);
    }
}
