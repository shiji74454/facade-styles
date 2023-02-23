package basic;

import processing.opengl.PGraphicsOpenGL;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

public class StyledPolygon extends StyledGeometry{
    List<WB_Polygon> polys;
    int strokeColor;

    int fillColor;
    float strokeWidth = 1;

    public StyledPolygon(int fillColor, int strokeColor,float strokeWidth) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.polys = new ArrayList<>();
    }



    public StyledPolygon(int strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.polys = new ArrayList<>();
    }

    public StyledPolygon add(WB_Polygon poly) {
        this.polys.add(poly);
        return this;
    }

    public void addAll(List<WB_Polygon> others) {
        this.polys.addAll(others);
    }

    public List<WB_Polygon> getLines() {
        return polys;
    }

    public void setLines(List<WB_Polygon> polys) {
        this.polys = polys;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public void draw(WB_Render3D render3D) {
        PGraphicsOpenGL home = render3D.getHome();
        home.pushStyle();
        home.stroke(strokeColor);
        home.strokeWeight(strokeWidth);
/*        render3D.drawSimplePolygon(polys);
        render3D.drawPolygoned*/
        render3D.drawPolygonEdges(polys);
        home.popStyle();
    }


}
