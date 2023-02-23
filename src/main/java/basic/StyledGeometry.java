package basic;

import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

public abstract class StyledGeometry {
    boolean transparent = false;
    Material material = Material.DarkGray;
    public abstract void draw(WB_Render3D render3D);

    public static List<StyledGeometry>sortGlassLast(List<StyledGeometry>geometries){
        List<StyledGeometry>trans = new ArrayList<>();
        List<StyledGeometry>solid = new ArrayList<>();
        for (StyledGeometry geometry : geometries) {
            if (geometry.transparent)
                trans.add(geometry);
            else
                solid.add(geometry);
        }

        solid.addAll(trans);
        return solid;
    }
}
