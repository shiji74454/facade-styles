package making_test; /**
 * @author : author
 * @date : 15:28 2022-10-25
 */

import basic.ControlPanel;
import guo_cam.CameraController;
import processing.core.PApplet;
import unit.making.S_ExtrudeIn_3;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

public class S_3_TestGUI extends PApplet {
    CameraController cam;
    WB_Render3D render;
    ControlPanel panel;
    S_ExtrudeIn_3 example;

    public void settings() {
        size(1280, 960, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(S_3_TestGUI.class.getName());
    }

    public void setup() {
        cam = new CameraController(this, 200);
        render = new WB_Render3D(this);
        WB_Point[] pts = new WB_Point[]{
                new WB_Point(0, 0, 0),
                new WB_Point(0, 8000, 0),
                new WB_Point(0, 8000, 3000),
                new WB_Point(0, 0, 3000)
        };
        example = new S_ExtrudeIn_3(pts);
        poly = new WB_Polygon(pts);
        panel = new ControlPanel(this, ControlPanel.Mode.Slider);

        panel.updatePanel(example,"S_ShaderArray");
    }

    WB_Polygon poly;
    public void draw() {
        background(255);
        cam.drawSystem(300);
        example.draw(render);
        noFill();
        stroke(255,0,0);
        render.drawPolygonEdges(poly);
        camera();
    }

}