package many; /**
 * @author : author
 * @date : 15:28 2022-10-25
 */

import basic.BasicObject;
import basic.ControlPanel;
import basic.LayoutGenerator;
import basic.Output_Component;
import guo_cam.Camera;
import guo_cam.CameraController;
import guo_cam.Vec_Guo;
import processing.core.PApplet;
import unit.S_Corner_Component_Lib;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.Map;

public class S_Corner_Component_Lib_Many_TestGUI extends PApplet {
    CameraController cam;
    WB_Render3D render;
    ControlPanel panel;
    S_Corner_Component_Lib example;
    ArrayList<Map<String,ArrayList<Output_Component>>> output_components = new ArrayList<>();

    ArrayList<BasicObject> allExamples = new ArrayList<>();
    ArrayList<WB_Point> examplePostions = new ArrayList<>();
    int array_y_num = 5;
    int array_z_num = 4;
    Camera camera;
    WB_Point[] pts;

    LayoutGenerator layoutGenerator;


    public void settings() {
        size(1800, 600, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(S_Corner_Component_Lib_Many_TestGUI.class.getName());
    }

    public void setup() {
        cam = new CameraController(this, 200);

        cam.getCamera().setLookAt(new Vec_Guo(-500, (array_y_num * 8000) / 2.d, (array_z_num * 3000) / 2.d));
        cam.getCamera().setFovy(1);
        cam.getCamera().setPosition(new Vec_Guo(6500, (array_y_num * 8000) / 2.d, (array_z_num * 3000) / 2.d));

        camera = cam.getCamera();
        render = new WB_Render3D(this);
        textureMode(NORMAL);

        //传进来一套假的点
        //facade basic face
        double facade_width = 8000 * array_y_num;
        double facade_height = 3000 * array_z_num;
        WB_Point[] facade_pts = new WB_Point[]{
                new WB_Point(0, 0, 0),
                new WB_Point(0, facade_width, 0),
                new WB_Point(0, facade_width, facade_height),
                new WB_Point(0, 0, facade_height),
                new WB_Point(0, 0, 0)
        };

        WB_Polygon facade_poly = new WB_Polygon(facade_pts);
        System.out.println("facade_poly.getSignedArea() = " + facade_poly.getSignedArea());

        int real_y_num = (int) (facade_width / 8000.d);
        int real_z_num = (int) (facade_height / 3000.d);
        System.out.println("real_y_num = " + real_y_num);
        System.out.println("real_z_num = " + real_z_num);

        double y_interval = facade_width / real_y_num;
        double z_interval = facade_height / real_z_num;
        System.out.println("y_interval = " + y_interval);
        System.out.println("z_interval =" + z_interval);

        pts = new WB_Point[]{
                new WB_Point(0, 0, 0),
                new WB_Point(0, y_interval, 0),
                new WB_Point(0, y_interval, z_interval),
                new WB_Point(0, 0, z_interval)
        };

        for (int i = 0; i < real_y_num; i++) {
            for (int j = 0; j < real_z_num; j++) {
                examplePostions.add(new WB_Point(0, i * y_interval, j * z_interval));
            }
        }

        for (int i = 0; i < examplePostions.size(); i++) {
            S_Corner_Component_Lib example = new S_Corner_Component_Lib(pts);
           //example.setUnitNumberForOutputComponents(i);
            allExamples.add(example);
            output_components.add(example.getOutput_components(i));
        }
        panel = new ControlPanel(this, ControlPanel.Mode.Slider);
        for (int i = allExamples.size() - 1; i >= 0; i--) {
            panel.updatePanel(allExamples.get(i), " ");
        }
        layoutGenerator = new LayoutGenerator(841, 594, 20);
        layoutGenerator.setOutPutComponentsLayout(output_components);
    }


    public void draw() {
        background(255);
        cam.drawSystem(5000);

        for (int i = 0; i < examplePostions.size(); i++) {
            WB_Point pos = examplePostions.get(i);
            pushMatrix();
            pushStyle();

            translate(pos.xf(), pos.yf(), pos.zf());
            allExamples.get(i).draw(render, this);
            popStyle();
            popMatrix();
        }
        pushMatrix();
        translate(2000, 0, 0);
        layoutGenerator.draw(render, this);
        popMatrix();
        noFill();
        stroke(255, 0, 0);
        //render.drawPolygonEdges(poly);
        camera();
    }


    public void keyPressed() {
        if (key == 'F' || key == 'f') {
            int viewInt = cam.createView(
                    new Vec_Guo(6500, (array_y_num * 8000) / 2.d, (array_z_num * 3000) / 2.d), //position
                    new Vec_Guo(-500, (array_y_num * 8000) / 2.d, (array_z_num * 3000) / 2.d));//lookAt
            cam.changeCurrentView(viewInt);
            cam.getCamera().setFovy(1);
        }

        if (key == 'T' || key == 't') {
            int viewInt = cam.createView(
                    new Vec_Guo(6500, (array_y_num * 8000) / 2.d, 9000), //position
                    new Vec_Guo(6500, (array_y_num * 8000) / 2.d, -1000));//lookAt

            cam.getCamera().setFovy(1);
            cam.changeCurrentView(viewInt);
        }

        if (key == ' ') {
            cam.defaultView();
        }

    }




}