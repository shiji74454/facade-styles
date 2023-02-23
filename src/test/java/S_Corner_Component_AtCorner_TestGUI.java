/**
 * @author : author
 * @date : 15:28 2022-10-25
 */

import basic.ControlPanel;
import guo_cam.Camera;
import guo_cam.CameraController;
import guo_cam.Vec_Guo;
import processing.core.PApplet;
import unit.S_Corner_Component;
import unit.S_Corner_Component_AtCorner;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;

public class S_Corner_Component_AtCorner_TestGUI extends PApplet {
    CameraController cam;
    WB_Render3D render;
    ControlPanel panel;
    double length_a = 7000;
    double length_b = 5000;

    double unit_height = 3000;
    S_Corner_Component_AtCorner example;
    int array_z_num = 1;
    Camera camera;
    int standardUnitDividedNum = 16;
    WB_Polygon poly;

    ArrayList<Integer> rects_picked = new ArrayList<>();

    public void settings() {
        size(1800, 600, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(S_Corner_Component_AtCorner_TestGUI.class.getName());
    }

    public void setup() {
        cam = new CameraController(this, 200);

        cam.getCamera().setLookAt(new Vec_Guo(-500,  length_a/2.d, (array_z_num *unit_height)/2.d));
        cam.getCamera().setFovy(1);
        cam.getCamera().setPosition(new Vec_Guo(6500, length_a/2.d, (array_z_num * unit_height)/2.d));

        camera = cam.getCamera();
        render = new WB_Render3D(this);
        WB_Point[] pts = new WB_Point[]{
                new WB_Point(0,0,0),
                new WB_Point(0, length_a ,0),
                new WB_Point(-length_b,length_a ,0),
                new WB_Point(-length_b, length_a , unit_height ),
                new WB_Point(0, length_a , unit_height ),
                new WB_Point(0, 0, unit_height )
        };

        poly = new WB_Polygon(pts);
        example = new S_Corner_Component_AtCorner(pts,standardUnitDividedNum);
        panel = new ControlPanel(this, ControlPanel.Mode.Slider);
        panel.updatePanel(example,"S_Corner_Component_AtCorner");
    }

    public void draw() {
        background(255);
        cam.drawSystem(10000);
        example.draw(render,this);
        render.drawPolygonEdges(poly);
        noFill();
        stroke(255,0,0);
        camera();

    }
    public  void keyPressed() {
        if (key == 'F' || key == 'f') {
            int viewInt = cam.createView(
                    new Vec_Guo(6500,8000/2.d, array_z_num * 3000/2.d), //position
                   new Vec_Guo(-500, 8000/2.d, array_z_num * 3000/2.d));//lookAt
            //int viewInt = cam.createView(camera.getPosition(),camera.getLookAt());
            cam.changeCurrentView(viewInt);
            cam.getCamera().setFovy(1);
        }

        if (key == ' ') {
            cam.defaultView();
        }
    }
}