
import basic.Material;
import guo_cam.CameraController;
import processing.core.PApplet;
import wblut.hemesh.HEC_Beethoven;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render3D;


public class TestMaterial extends PApplet {
    CameraController cam;
    WB_Render3D render;

    public void settings() {
        size(1280, 960, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(TestMaterial.class.getName());
    }

    HE_Mesh mesh;

    public void setup() {
        cam = new CameraController(this, 200);
        render = new WB_Render3D(this);
        mesh = new HEC_Beethoven().create();
        mesh.setFaceColor(Material.Glass.getColor());
        System.out.println(Material.Glass.getColor());

    }

    public void draw() {
        background(255);
        cam.drawSystem(300);

        render.drawFacesFC(mesh);

    }

}

