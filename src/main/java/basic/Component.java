package basic;

import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HEM_Extrude;
import wblut.hemesh.HE_Mesh;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:Component
 * @date : 19:53 2022-11-06
 */
public enum  Component {

    GratingPanel("GP500/1800/50/30");

    Component (String componentName) {
        this.componentName  = componentName;
    };

    private  String componentName  = "";

    public String getComponentName(){return this.componentName;};

    public HE_Mesh getMesh(){
        if (this.componentName == "GP500/1800/50/30"){
            return getGratingPanel(500,1800,50,30);
        }
        else return null;
    }

    private HE_Mesh getGratingPanel(double grating_width, double grating_height, double outter_bar_thickness, double inner_bar_thinkness){
        HE_Mesh myGratingPanel = new HE_Mesh();
        WB_Polygon rect = new WB_Polygon(
                new WB_Point(0,0,0),
                new WB_Point(0, grating_width,0),
                new WB_Point(0, grating_width,grating_height),
                new WB_Point(0, 0, grating_height),
                new WB_Point(0,0,0)
        );
        WB_Polygon inner_poly = new WB_Polygon(
                rect.getPoint(0).add(0,outter_bar_thickness,outter_bar_thickness),
                rect.getPoint(1).add(0,-outter_bar_thickness,outter_bar_thickness),
                rect.getPoint(2).add(0,-outter_bar_thickness,-outter_bar_thickness),
                rect.getPoint(3).add(0,outter_bar_thickness,-outter_bar_thickness),
                rect.getPoint(0).add(0,outter_bar_thickness,outter_bar_thickness)
        );

        WB_Polygon hole = myReversePolygon(inner_poly);
        WB_GeometryFactory gf = new WB_GeometryFactory();
        WB_Polygon outter_bar_poly  = gf.createPolygonWithHole(rect .getPoints().toArray(), hole.getPoints().toArray());
        HE_Mesh outter_bar = getMyExtrudedMesh(outter_bar_poly,outter_bar_thickness);
        myGratingPanel.add(outter_bar);

        int divided_bar_num = 18;
        double interval = inner_poly.getSegment(1).getLength()/divided_bar_num;
        WB_Polygon bar_poly = new WB_Polygon(
                inner_poly.getPoint(0).sub(0,0,inner_bar_thinkness/2),
                inner_poly.getPoint(1).sub(0,0,inner_bar_thinkness/2),
                inner_poly.getPoint(1).add(0,0,inner_bar_thinkness/2),
                inner_poly.getPoint(0).add(0,0,inner_bar_thinkness/2),
                inner_poly.getPoint(0).sub(0,0,inner_bar_thinkness/2)
        );
        HE_Mesh basicBarMesh = getMyExtrudedMesh(bar_poly,inner_bar_thinkness*0.5);

        WB_Transform3D move = new WB_Transform3D();
        for (int i = 1 ; i < divided_bar_num; i++){

            move.addTranslate(new WB_Point(0,0,interval));

            myGratingPanel.add(basicBarMesh.apply(move));
        }

        return myGratingPanel;
    };

    private WB_Polygon myReversePolygon(WB_Polygon poly) {

        WB_Coord[] wb_coords = new WB_Coord[poly.getNumberOfPoints()];
        for(int i = 0 ; i < poly.getNumberOfPoints(); i++){

            wb_coords[i] = poly.getPoint(poly.getNumberOfPoints()-i-1);

        }
        return new WB_Polygon(wb_coords);
    };
    private HE_Mesh getMyExtrudedMesh(WB_Polygon poly, double outter_bar_thickness){
        HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{poly}));
        HE_Mesh mesh1 = new HE_Mesh(mesh);
        HEM_Extrude extrude = new HEM_Extrude();
        extrude.setDistance(outter_bar_thickness);
        mesh.modify(extrude);
        mesh1.add(mesh);
        return  mesh1;

    };





}
