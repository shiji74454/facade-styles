/**
 * @author : author
 * @date : 15:28 2022-10-25
 */

import basic.ControlPanel;
import guo_cam.Camera;
import guo_cam.CameraController;
import guo_cam.Vec_Guo;
import processing.core.PApplet;
import unit.S_ExtrudeIn;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

public class S_ExtrudeIn_TestGUI extends PApplet {
    CameraController cam;
    WB_Render3D render;
    ControlPanel panel;
    S_ExtrudeIn example;

    int array_y_num = 1;
    int array_z_num = 1;
    Camera camera;
    public void settings() {
        size(1800, 600, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(S_ExtrudeIn_TestGUI.class.getName());
    }

    public void setup() {
        cam = new CameraController(this, 200);

        cam.getCamera().setLookAt(new Vec_Guo(-500, (array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));
        cam.getCamera().setFovy(1);
        cam.getCamera().setPosition(new Vec_Guo(6500,(array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));

        camera = cam.getCamera();
        render = new WB_Render3D(this);
        WB_Point[] pts = new WB_Point[]{
                new WB_Point(0, 0, 0),
                new WB_Point(0, 8000, 0),
                new WB_Point(0, 8000, 3000),
                new WB_Point(0, 0, 3000)
        };
        example = new S_ExtrudeIn(pts);
        poly = new WB_Polygon(pts);
        panel = new ControlPanel(this, ControlPanel.Mode.Slider);
        panel.updatePanel(example,"S_Quad_Hole");
    }

    WB_Polygon poly;
    public void draw() {
        background(255);
        //cam.drawSystem(300);
        example.draw(render);
        noFill();
        stroke(255,0,0);
        //render.drawPolygonEdges(poly);
        camera();

    }
    public  void keyPressed() {
        if (key == 'F' || key == 'f') {
//            Camera ca = new Camera(camera);
//            ca.setLookAt(new Vec_Guo(-100, 0, 0));
//            ca.setFovy(0.01);
            int viewInt = cam.createView(
                    new Vec_Guo(6500,(array_y_num*8000)/2.d, (array_z_num * 3000)/2.d), //position
                   new Vec_Guo(-500, (array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));//lookAt
            //int viewInt = cam.createView(camera.getPosition(),camera.getLookAt());
            cam.changeCurrentView(viewInt);
            cam.getCamera().setFovy(1);

        }
        if (key == ' ') {
            cam.defaultView();

        }
    }
}