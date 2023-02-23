package many; /**
 * @author : author
 * @date : 15:28 2022-10-25
 */

import basic.ControlPanel;
import guo_cam.Camera;
import guo_cam.CameraController;
import guo_cam.Vec_Guo;
import processing.core.PApplet;
import unit.S_Quad_Hole;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

public class S_Quad_Hole_Many_TestGUI extends PApplet {
    CameraController cam;
    WB_Render3D render;
    ControlPanel panel;
    ControlPanel panel2;
    S_Quad_Hole example;
    S_Quad_Hole example2;
    int array_y_num = 2;
    int array_z_num = 7;
    Camera camera;

    public void settings() {
        size(2000, 1200, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(S_Quad_Hole_Many_TestGUI.class.getName());
    }

    public void setup() {
        cam = new CameraController(this, 200);
        camera = cam.getCamera();
        cam.getCamera().setLookAt(new Vec_Guo(-500, (array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));
        cam.getCamera().setFovy(1);
        cam.getCamera().setPosition(new Vec_Guo(29500,(array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));
        render = new WB_Render3D(this);

        WB_Point[] pts = new WB_Point[]{
                new WB_Point(0, 0, 0),
                new WB_Point(0, 8000, 0),
                new WB_Point(0, 8000, 3000),
                new WB_Point(0, 0, 3000)
        };

        example = new S_Quad_Hole(pts);
        example2 = new S_Quad_Hole(pts);
        example2.reverseBooleanPara("left_right");

        poly = new WB_Polygon(pts);
        panel = new ControlPanel(this, ControlPanel.Mode.Slider);
        panel2 = new ControlPanel(this, ControlPanel.Mode.Slider);
        float[] pos2 = new float[]{
                panel.getPos()[0],
                panel.getPos()[1]+ 600
        };


        System.out.println("example2.getDataToDisplay().get(\"glass_num\")" + example2.getDataToDisplay().get("glass_num"));
        example2.putData("glass_num", String.valueOf(Double.parseDouble(example2.getDataToDisplay().get("glass_num"))*array_y_num*array_z_num));
        example2.putData("panel_num", String.valueOf(Double.parseDouble(example2.getDataToDisplay().get("panel_num"))*array_y_num*array_z_num));

        panel2.setPos(pos2 );
        panel.updatePanel(example,"S_ShaderArray");
        panel2.updatePanel(example2,"S_Quad_Hole");

    }

    WB_Polygon poly;

    public void draw() {
        background(255);
        //cam.drawSystem(300);

        for (int i = 0; i < array_y_num ; i++){
            for(int j = 0; j < array_z_num ; j++) {
                pushMatrix();
                pushStyle();
                translate(0, 8000 * i, 3000*j);
                if (j%2 == 0){
                    noStroke();
                    example.draw(render);
                }else {  example2.draw(render);}
                popStyle();
                popMatrix();
            }
        }

//        System.out.println("*********************************************************");
//        System.out.println("cam.pos    = " + cam.getCamera().getPosition());
//        System.out.println("cam.lookAt = " + cam.getCamera().getLookAt());
//        System.out.println("cam.Fovy   = " + cam.getCamera().getFovy());
        noFill();
        stroke(255,0,0);
        //render.drawPolygonEdges(poly);
        camera();
    }

    public  void keyPressed() {
        if (key == 'F' || key == 'f') {
            int viewInt = cam.createView(
                    new Vec_Guo(28000,(array_y_num*8000)/2.d, (array_z_num * 3000)/2.d), //position
                    new Vec_Guo(-500, (array_y_num*8000)/2.d, (array_z_num * 3000)/2.d));//lookAt
            cam.changeCurrentView(viewInt);
            cam.getCamera().setFovy(0.9);
        }

        if (key == ' ') {
            cam.defaultView();

        }

    }

}