package unit;

import basic.BasicObject;
import basic.Material;
import basic.StyledMesh;
import basic.StyledPolyLine;
import wblut.geom.WB_Point;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Quad;
import wblut.geom.WB_Vector;
import wblut.hemesh.HEC_Box;
import wblut.hemesh.HEC_FromQuads;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;


public class F_Example extends BasicObject {

    WB_Point[] rectPts;

    public F_Example(WB_Point[] rectPts) {
        this.rectPts = rectPts;
        initPara();
        initData();
        calculate();
    }

    /**
     * ------------- parameters ------------
     */
    double top_height;
    double bottom_height;
    double top_depth;
    double glass_offset;
    boolean draw_frame;

    @Override
    protected void initPara() {
        top_height = putPara(200, 100, 1000, "top_height").getValue();
        bottom_height = putPara(200, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(400, 100, 1000, "top_depth").getValue();
        glass_offset = putPara(100, 0, 200, "glass_offset").getValue();
        draw_frame = putPara(false,"draw_frame").getBoolean();
    }


    /**
     * ------------- data for display ------------
     */
    double height;
    double width;
    double paintArea;
    double glassArea;

    @Override
    protected void initData() {
        putData("height", "");
        putData("width", "");
        putData("paintArea", "");
        putData("glassArea", "");
    }

    /**
     * main body of calculation: generate data and styled objects according to current parameters
     */
    @Override
    protected void calculate() {
        WB_Vector v1 = rectPts[1].subToVector3D(rectPts[0]);
        WB_Vector v2 = rectPts[3].subToVector3D(rectPts[0]);

        this.height = rectPts[2].getDistance3D(rectPts[1]);
        this.width = rectPts[1].getDistance3D(rectPts[0]);
        v1.normalizeSelf();
        v2.normalizeSelf();
        WB_Vector n = v1.cross(v2);
        n.normalizeSelf();

        HE_Mesh topMesh = new HEC_Box().setFromCorners(rectPts[3], rectPts[2].add(n.mul(this.top_depth).add(0, 0, -this.top_height))).create();
        HE_Mesh bottomMesh = new HEC_Box().setFromCorners(rectPts[0], rectPts[1].add(n.mul(this.top_depth).add(0, 0, this.bottom_height))).create();
        HE_Mesh glassHemesh = new HEC_FromQuads(new WB_Quad[]{new WB_Quad(
                rectPts[0].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                rectPts[1].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                rectPts[2].sub(v2.mul(top_height)).add(n.mul(glass_offset)),
                rectPts[3].sub(v2.mul(top_height)).add(n.mul(glass_offset))
        )}).create();



        paintArea = 0;
        for (HE_Face face : topMesh.getFaces()) {
            paintArea += face.getFaceArea();
        }
        for (HE_Face face : bottomMesh.getFaces()) {
            paintArea += face.getFaceArea();
        }
        paintArea = (int) (paintArea / 1e6);
        glassArea = 0;
        for (HE_Face face : glassHemesh.getFaces()) {
            glassArea += face.getFaceArea();
        }
        glassArea = (int) (glassArea / 1e6);
        
        StyledMesh whiteMesh = new StyledMesh(Material.LightGray).add(topMesh).add(bottomMesh);
        StyledMesh glassMesh = new StyledMesh(Material.Glass).add(glassHemesh);
        if(draw_frame){
            WB_PolyLine frame = new WB_PolyLine(
                    rectPts[0].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                    rectPts[1].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                    rectPts[2].sub(v2.mul(top_height)).add(n.mul(glass_offset)),
                    rectPts[3].sub(v2.mul(top_height)).add(n.mul(glass_offset)),
                    rectPts[0].add(v2.mul(bottom_height)).add(n.mul(glass_offset))
            );
            StyledPolyLine styledPolyLine = new StyledPolyLine(0x00000000, 10).add(frame);
            addGeometry(styledPolyLine);
        }

        addGeometry(whiteMesh);
        addGeometry(glassMesh);

    }


    /**------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }

}
