package basic;

import processing.core.PApplet;
import processing.core.PConstants;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:Output_Component
 * @date : 21:19 2022-11-06
 */
public class Output_Component {
    private String materialName;
    private ArrayList<StyledPolyLine> styledPolyLines;
    private double[] size;
    private WB_Point position;
    private String text;

    public Output_Component(String materialName) {
        this.styledPolyLines = new ArrayList<>();
        this.materialName = materialName;
    }

    public void setSize(double width, double height) {
        this.size = new double[2];
        size[0] = width;
        size[1] = height;
        setText();
    }

/*    public void setBoundingPoly() {
        this.boundingPoly = new WB_Polygon(
                new WB_Point(size[1], 0, 0),
                new WB_Point(size[1], size[0], 0),
                new WB_Point(0, size[0], 0),
                new WB_Point(0, 0, 0),
                new WB_Point(size[1], 0, 0)
        );
    }*/


    public double[] getSize() {
        return this.size;
    }

    public void addStylePolyline(StyledPolyLine styledPolyLine) {
        styledPolyLines.add(styledPolyLine);
    }

    private void setText() {
        text = this.materialName + "-" + (int) this.size[0] + "x" + (int) this.size[1];
    }

    public void setPos(WB_Point pos) {
        this.position = new WB_Point(pos.xd(), pos.yd(), pos.zd());
    }

    ;

    public void draw(WB_Render3D render3D, PApplet applet) {
        applet.pushMatrix();
        applet.translate(this.position.xf(), this.position.yf(), this.position.zf());
        for (StyledGeometry geometry : this.styledPolyLines) {
            geometry.draw(render3D);
        }
        applet.pushMatrix();
        applet.rotateX(PApplet.PI);
        applet.rotateZ(-PApplet.PI / 2);
        applet.textSize(100);
        applet.textAlign(PConstants.LEFT, PConstants.TOP);
        applet.text(this.text, 0, 0, 0);
        applet.popMatrix();
        applet.popMatrix();
    }

    public static Map<String, ArrayList<Output_Component>> merge(Map<String, ArrayList<Output_Component>> map1, Map<String, ArrayList<Output_Component>> map2) {
        Map<String, ArrayList<Output_Component>> map = new HashMap<>();
        map.putAll(map1);

        map2.forEach((key, value) -> {
            //Get the value for key in map.
            ArrayList<Output_Component> list = map.get(key);
            if (list == null) {
                map.put(key, value);
            } else {
                //Merge two list together
                ArrayList<Output_Component> mergedValue = new ArrayList<>(value);
                mergedValue.addAll(list);
                map.put(key, mergedValue);
            }
        });
        return map;
    }

    public void insertUnitNumber(int unitNumber) {
        char[] ca = this.text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] == '-') {
                sb.append(this.text).insert(i, "-" +Integer.toString(unitNumber));
                break;
            }
        }
        this.text = sb.toString();
    }
}
