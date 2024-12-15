package solid;
import transforms.Point3D;

import java.awt.*;

public class AxisX extends Solid {
    public AxisX() {
        setColor(Color.YELLOW);

        vb.add(new Point3D(-0.5, 0, 0));
        vb.add(new Point3D(0.8, 0, 0));

        addIndices(0, 1);
    }
}
