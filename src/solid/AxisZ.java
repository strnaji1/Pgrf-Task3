package solid;

import transforms.Point3D;

import java.awt.*;

public class AxisZ extends Solid{
    public AxisZ() {
        setColor(Color.blue);

        vb.add(new Point3D(0, 0, -0.5));
        vb.add(new Point3D(0, 0, 0.8));

        addIndices(0, 1);
    }
}
