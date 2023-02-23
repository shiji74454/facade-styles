package basic;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render3D;

import java.util.Arrays;
import java.util.List;

public class StyledMesh extends StyledGeometry {
    HE_Mesh mesh;

    int strokeColor = 0;

    boolean if_drawEdges = true;


    public StyledMesh(Material material, boolean if_drawEdges) {
        this.material = material;
        if (material == Material.Glass)
            transparent = true;
        if (!if_drawEdges){
            this.if_drawEdges = false;

        }
    }

    public StyledMesh(Material material) {
        this.material = material;
        if (material == Material.Glass)
            transparent = true;
    }

    public StyledMesh(Material material, int strokeColor) {
        this.material = material;
        this.strokeColor = strokeColor;
        if (material == Material.Glass)
            transparent = true;
    }

    public StyledMesh add(HE_Mesh other) {
        if (this.mesh == null){
            this.mesh = other.copy();
        }
        this.mesh.add(other);
        this.mesh.setFaceColor(material.getColor());
        return this;
    }

    public StyledMesh addAll(List<HE_Mesh> meshList) {
        if (meshList.size() == 0)
            return this;
        if (this.mesh == null){
            this.mesh = meshList.get(0).copy();}
        else{
            this.mesh.add(meshList.get(0));}
        for (int i = 1; i < meshList.size(); i++) {
            this.mesh.add(meshList.get(i));
        }
        this.mesh.setFaceColor(material.getColor());
        return this;
    }

    public void setMesh(HE_Mesh mesh) {
        this.mesh = mesh;
    }

    public HE_Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    @Override
    public void draw(WB_Render3D render3D) {
        PGraphicsOpenGL home = render3D.getHome();
        home.pushStyle();
        home.fill(0);
        home.noStroke();
        render3D.drawFacesFC(mesh);
        if (strokeColor >= 0)
            home.stroke(strokeColor);
        home.noFill();
        /************************sjtest*/
        if(!if_drawEdges){
            home.noStroke();
        }

        //home.noStroke();
        /************************sjtest*/
        render3D.drawEdges(mesh);
        home.popStyle();
    }

    public void draw(WB_Render3D render3D, PApplet app) {
        PGraphicsOpenGL home = render3D.getHome();
        home.pushStyle();
        home.noStroke();
         app. noLights();
        PImage texture = app.loadImage(this.material.getTexture());
        PImage[] imgs = new PImage[mesh.getFaces().size()];
        Arrays.fill(imgs, texture);

        render3D.drawFaces(mesh,imgs);
        if (strokeColor >= 0)
            home.stroke(strokeColor);
        home.noFill();
        /************************sjtest*/
        if(!if_drawEdges){
            home.noStroke();
        }

        //home.noStroke();
        /************************sjtest*/
        render3D.drawEdges(mesh);
        home.popStyle();
    }

    public void draw(WB_Render3D render3D, boolean  if_drawEdges) {
        PGraphicsOpenGL home = render3D.getHome();
        home.pushStyle();
        home.fill(0);
        home.noStroke();
        render3D.drawFacesFC(mesh);
        if (strokeColor >= 0)
            home.stroke(strokeColor);
        home.noFill();
        render3D.drawEdges(mesh);
        home.popStyle();
    }
}
