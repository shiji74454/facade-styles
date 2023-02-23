package basic;

import processing.opengl.PGraphicsOpenGL;
import wblut.geom.WB_PolyLine;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

public class StyledPolyLine extends StyledGeometry{
    List<WB_PolyLine> lines;
    int strokeColor;
    float strokeWidth = 1;

    public StyledPolyLine(int strokeColor) {
        this.strokeColor = strokeColor;
        this.lines = new ArrayList<>();
    }

    public StyledPolyLine(int strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.lines = new ArrayList<>();
    }

    public StyledPolyLine add(WB_PolyLine line) {
        this.lines.add(line);
        return this;
    }

    public void addAll(List<WB_PolyLine> others) {
        this.lines.addAll(others);
    }

    public List<WB_PolyLine> getLines() {
        return lines;
    }

    public void setLines(List<WB_PolyLine> lines) {
        this.lines = lines;
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
        render3D.drawPolylineEdges(lines);
        home.popStyle();
    }


}
