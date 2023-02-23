import guo_cam.CameraController;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform3D;
import wblut.processing.WB_Render3D;

import java.util.Random;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:test
 * @date : 21:38 2022-11-04
 */
public class test02 extends PApplet {
    CameraController cam;
    int windowFrame_thickness = 10;
    WB_Render3D wb_render;
    WB_Polygon poly;
    WB_Polygon newPoly;
    WB_Polygon polygon_afterRotate;
    WB_Polygon polygon_scale_sy;
    public static void main(String[] args) {
        PApplet.main(test02.class.getName());
    }
    public void settings() {
        size(1280, 960, P3D);
    }
    public void setup() {
        cam = new CameraController(this, 200);
        wb_render = new WB_Render3D(this);
       poly  = new WB_Polygon(
                new WB_Point(-400,600,300),
                new WB_Point(-500,600,300),
                new WB_Point(-500,600,1200),
                new WB_Point(-400,600,1200)
                );
        WB_Transform3D transform3D = new WB_Transform3D();
        transform3D.addTranslate(new WB_Point(0,0,0).sub(poly.getCenter()));
        transform3D.addRotateZ(poly.getNormal().getAngle(new WB_Point(1,0,0)));
        polygon_afterRotate = poly.apply(transform3D);
        System.out.println("poly.getNormal().getAngle(new WB_Point(1,0,0)) = " + poly.getNormal().getAngle(new WB_Point(1,0,0)));
        double sy =  (poly.getSegment(0).getLength()-2*windowFrame_thickness)/poly.getSegment(0).getLength();
        double sz =  (poly.getSegment(1).getLength()-2*windowFrame_thickness)/poly.getSegment(1).getLength();
        System.out.println("poly.getSegment(0).getLength() = " + poly.getSegment(0).getLength());
        transform3D.addScale(1,sy,1);
        transform3D.addScale(1,1,sz);
        polygon_scale_sy = poly.apply(transform3D);
        transform3D.addRotateZ(-poly.getNormal().getAngle(new WB_Point(1,0,0)));
        transform3D.addTranslate(poly.getCenter()) ;
        newPoly = poly.apply(transform3D);

    }

    @Override
    public void draw() {
        background(255);
        cam.drawSystem(1200);
        stroke(255,0,0);
        strokeWeight(6);
        wb_render.drawPolygon(poly);
        stroke(0,255,0);
        wb_render.drawPolygon(newPoly);
        stroke(0,0,255);
        wb_render.drawPolygon(polygon_afterRotate);
        stroke(200,255,20);
        wb_render.drawPolygon(polygon_scale_sy);
    }
}
