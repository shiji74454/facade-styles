package unit;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:S_Quad_Hole
 * @date : 15:56 2022-10-26
 */

import basic.*;
import wblut.geom.*;
import wblut.hemesh.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class S_Arc_Stretch extends BasicObject {

    WB_Point[] rectPts;

    public S_Arc_Stretch(WB_Point[] rectPts) {
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

    //new para
    boolean[] if_holes;
    double division_num;
    double holes_density;
    double holes_size;

    double randomSeed;

    double border_width_ratio;
    @Override
    protected void initPara() {
        top_height = putPara(600, 100, 1000, "top_height").getValue();
        bottom_height = putPara(0, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(600, 100, 1500, "top_depth").getValue();
        glass_offset = putPara(0, 0, 1500, "glass_offset").getValue();

        division_num = putPara(6, 2, 8, "division_num").getValue();
        randomSeed = putPara(5, 0, 100, "randomSeed").getValue();
        border_width_ratio = putPara(0.1, 0.05, 0.2, "border_width_ratio").getValue();
    }

    /**
     * ------------- data for display ------------
     */
    double height;
    double width;
    String panel_size;
    double panel_num;
    String glass_size;
    double glass_num;

    double holeRatio;

    StyledPolygon whiteholePolygon;
    StyledPolyLine styledPolyLine;
    @Override
    protected void initData() {
        putData("height", "");
        putData("width", "");
        putData("panel_size", "");
        putData("panel_num", "");
        putData("glass_size", "");
        putData("glass_num", "");
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

        WB_PolyLine frame = new WB_PolyLine(
                rectPts[0].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                rectPts[1].add(v2.mul(bottom_height)).add(n.mul(glass_offset)),
                rectPts[2].sub(v2.mul(top_height)).add(n.mul(glass_offset)),
                rectPts[3].sub(v2.mul(top_height)).add(n.mul(glass_offset)),
                rectPts[0].add(v2.mul(bottom_height)).add(n.mul(glass_offset))
        );

        panel_size = "";
        glass_size = "";
        panel_num = 0;
        glass_num = 0;
        StyledMesh whiteMesh_withEdges = new StyledMesh(Material.Concrete).add(bottomMesh);
        StyledMesh whiteMesh_withoutEdges = new StyledMesh(Material.LightGray, false);
        //StyledMesh glassMesh = new StyledMesh(Material.Glass).add(glassHemesh);
        StyledMesh glassMesh = new StyledMesh(Material.Glass);
        whiteholePolygon = new StyledPolygon(0x00ffffff, 0x00000000,4);
        styledPolyLine = new StyledPolyLine(0x00000000, 4).add(frame);

        //*******************************************just for test
        //createRandomSeed as slider
        Random random = new Random();
        random.setSeed((int)randomSeed);
        System.out.println("(int)randomSeed = " +(int)randomSeed );
        Random random2 = new Random((int)randomSeed+1);
        //getDividedRects
        ArrayList<WB_PolyLine> dividedFrames = getDividedFrames(frame,(int)division_num);

        //set random if_holes
        int ran = random.nextInt(4);
       // ran = new Random().nextInt(4);
        System.out.println("ran = " + ran );
        boolean left_right =random2.nextBoolean();
        if_holes = new boolean[dividedFrames.size()];
        setIfHoles(dividedFrames,ran, left_right);

        //setHollowedOrFilledWithHoleRatio
        ArrayList<HE_Mesh> allMeshes = getAllMeshes(dividedFrames, if_holes,border_width_ratio);

        whiteMesh_withEdges.addAll(allMeshes);
        styledPolyLine.addAll(dividedFrames);
        //getAllMeshes

        //getTwoInterSectionPolylines

        //add to list##

        //*******************************************test end

        addGeometry(whiteMesh_withEdges);
        //addGeometry(whiteMesh_withoutEdges);
        addGeometry(glassMesh);
        addGeometry(styledPolyLine);
        addGeometry(whiteholePolygon);
    }


/*------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }

    private ArrayList<HE_Mesh> getAllMeshes(ArrayList<WB_PolyLine> dividedFrames, boolean[] if_holes, double border_width_ratio) {
        ArrayList<HE_Mesh> allMeshes = new ArrayList<>();
        for (int k = 0; k < dividedFrames.size(); k++){
            WB_PolyLine f = dividedFrames.get(k);
            HE_Mesh rectMesh = new HEC_Box().setFromCorners(
                    f.getPoint(0),
                    f.getPoint(2).add((top_depth - glass_offset)*2, 0)
            ).create();


            HE_Mesh rectMesh_real = new HEC_Box().setFromCorners(
                    f.getPoint(0).sub(0,0,height),
                    f.getPoint(2).add((top_depth - glass_offset)*2, 0,top_height)
            ).create();


            WB_Polygon shell = new WB_Polygon(
                    f.getPoint(0).sub(0,0,height).add((top_depth - glass_offset), 0,0),
                    f.getPoint(1).sub(0,0,height).add((top_depth - glass_offset), 0,0),
                    f.getPoint(2).add(0,0,top_height).add((top_depth - glass_offset), 0,0),
                    f.getPoint(3).add(0,0,top_height).add((top_depth - glass_offset), 0,0)
            );



            HEC_Cylinder creator_cylinder=new HEC_Cylinder();
            double R= f.getSegment(0).getLength()*(1-border_width_ratio*2)/2.d;

            double H = f.getSegment(1).getLength();



            creator_cylinder.setRadius(R); // upper and lower radius. If one is 0, HEC_Cone is called.
            creator_cylinder.setHeight(H);
            creator_cylinder.setFacets(32).setSteps(2);
            creator_cylinder.setCap(false,false);// cap top, cap bottom?
            HE_Mesh cylinder = new HE_Mesh( creator_cylinder);


            HEC_Hemisphere creator_hemisphere=new  HEC_Hemisphere();
            double r = R;
            double h=H*r/(R-r);
            double hp=R*r/h;
            double hpp=r*r/h;
            double Rtop=Math.sqrt(r*r+hpp*hpp);
            double Rbottom=Math.sqrt(R*R+hp*hp);

            creator_hemisphere.setRadius(Rtop);
            creator_hemisphere.setUFacets(32);
            creator_hemisphere.setVFacets(6);
            creator_hemisphere.setCenter(0,0,0);


            HE_Mesh hemisphere = new HE_Mesh(creator_hemisphere);
            double scaleZ = 0.6;
            hemisphere.scaleSelf(1,1,scaleZ);
            WB_Transform3D trans_0 = new WB_Transform3D();
            trans_0.addTranslate(new WB_Point(0,0, - hemisphere.getAllBoundaryVertices().get(0).zd()+H/2));
            HE_Mesh hemisphere_trans = hemisphere.apply(trans_0);

            HEM_Mirror modifier=new HEM_Mirror();
            WB_Plane P=new WB_Plane(0, 0, 0, 0, 0, 1);
            modifier.setPlane(P);
            modifier.setOffset(0);
            modifier.setReverse(false);
            hemisphere_trans.modify(modifier);
            cylinder.add(hemisphere_trans);
            cylinder.scaleSelf(0.6,1,1);

            WB_Transform3D trans = new WB_Transform3D();
            trans.addTranslate(rectMesh.getCenter().sub(0,0,R*scaleZ));
            HE_Mesh transCylinder =  cylinder.apply(trans);

            HEM_Slice  modifier_slice =new HEM_Slice();
            WB_Plane P2=new WB_Plane(rectMesh.getCenter().xd(),0,0,1,0,0);
            modifier_slice.setPlane(P2);
            modifier_slice.setOffset(0);// shift cut plane along normal
            modifier_slice.setCap(true);// cap holes
            modifier_slice.setReverse(true);// keep other side of plane
            transCylinder.modify(modifier_slice);

            List<HE_Halfedge> boundarySegements = transCylinder.getSelection("edges").getAllBoundaryHalfedges();
            //System.out.println("boundarySegements.size() = "+boundarySegements.size());
            int segements_num = boundarySegements.size();
            ArrayList<WB_Point> boundaryPoints = new ArrayList<>();
            HE_Halfedge startEdge = boundarySegements.get(0);
            boundaryPoints.add(startEdge.getStartPosition());
            boundaryPoints.add(startEdge.getEndPosition());
            boundarySegements.remove(0);
            double tolerance =0.5;

            while (boundaryPoints.size() < segements_num){
                for (HE_Halfedge halfedge: boundarySegements){
                    //System.out.println("halfedge.getStartPosition()" + halfedge.getStartPosition());
                    if (equalPos(boundaryPoints.get(boundaryPoints.size()-1),(halfedge.getStartPosition()),tolerance)){
                        boundaryPoints.add(halfedge.getEndPosition());
                    }
                    else if (equalPos(boundaryPoints.get(boundaryPoints.size()-1),(halfedge.getEndPosition()),tolerance)){
                        boundaryPoints.add(halfedge.getStartPosition());
                    }
                }
                //System.out.println("boundaryPoints.size()" + boundaryPoints.size());
            }

            boundaryPoints.add(boundaryPoints.get(0));
            WB_PolyLine polyLine = new WB_PolyLine(boundaryPoints);

            WB_Polygon poly = new WB_Polygon(polyLine.getPoints());

            List<WB_Point> allPoints = new ArrayList<>();
            for (int i = boundaryPoints.size()-1; i >= 0 ; i--){
                allPoints.add(boundaryPoints.get(i));
            }

            WB_GeometryFactory gf = new WB_GeometryFactory();
            WB_Polygon polyWithHole = gf.createPolygonWithHole(shell.getPoints().toList(),allPoints);
           // whiteholePolygon.add(polyWithHole);
            HE_Mesh polyMeshWithHole = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{polyWithHole}));

            HEM_Slice  modifier_slice_3 =new HEM_Slice();
            WB_Plane P3=new WB_Plane(0,0,bottom_height,0,0,1);
            modifier_slice_3.setPlane(P3);
            modifier_slice_3.setOffset(0);// shift cut plane along normal
            modifier_slice_3.setCap(false);// cap holes
            modifier_slice_3.setReverse(false);// keep other side of plane
            polyMeshWithHole.modify(modifier_slice_3);

            if(if_holes[k]){
                HE_Mesh polyMeshWithHole_copy = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{polyWithHole}));

                HEM_Slice  modifier_slice_4 =new HEM_Slice();
                WB_Plane P4=new WB_Plane(0,0,bottom_height,0,0,1);
                modifier_slice_4.setPlane(P4);
                modifier_slice_4.setOffset(0);// shift cut plane along normal
                modifier_slice_4.setCap(false);// cap holes
                modifier_slice_4.setReverse(false);// keep other side of plane
                polyMeshWithHole_copy.modify(modifier_slice_4);
                HEM_Extrude modifier_extrude=new HEM_Extrude();
                modifier_extrude.setDistance(-(top_depth-glass_offset));
                polyMeshWithHole_copy.modify( modifier_extrude);
                allMeshes.add(polyMeshWithHole_copy);
            }else {
                transCylinder.modify(modifier_slice_3);
                //rectMesh_real.remove(rectMesh_real.getFaceWithIndex(5));
                rectMesh_real.modify(modifier_slice_3);
                modifier_slice.setCap(false);
                rectMesh_real.modify(modifier_slice);
                allMeshes.add(rectMesh_real);
                allMeshes.add(transCylinder);
            }


            allMeshes.add(polyMeshWithHole);

        }

        return allMeshes;
    }


    private  boolean equalPos(WB_Point p1, WB_Point p2, double tolerance){
        boolean if_equal = false;
        if (
                Math.abs(p1.xd()-p2.xd()) < tolerance &&
                        Math.abs(p1.yd()-p2.yd()) < tolerance   &&       Math.abs(p1.zd()-p2.zd()) < tolerance
        ){if_equal = true;}

        return if_equal;
    }
    private  void  setIfHoles(ArrayList<WB_PolyLine> dividedFrames, int ran, boolean left_right){
        if(ran == 0) {holeRatio = 0;}
        else  if (ran == 1) {holeRatio = 0.4;}
        else if (ran == 2) { holeRatio = 0.6;}
        else{holeRatio = 1;}

        for(int i = 0 ; i < dividedFrames.size(); i++){


            if_holes[i] = !((i * 1.0 / dividedFrames.size()) < holeRatio);
        }

        if(! left_right) {
            for (int i = 0; i < if_holes.length; i++) {
                if_holes[i] = !if_holes[i];
            }
        }

        System.out.println();
    }

    private ArrayList<WB_PolyLine> getDividedFrames(WB_PolyLine frame, int division_num) {
        ArrayList<WB_PolyLine> dividedFrames = new ArrayList<>();
        ArrayList<WB_Segment> divideLines = new ArrayList<WB_Segment>();
        WB_Segment segment_start = frame.getSegment(3);
        segment_start.reverse();
        divideLines.add(segment_start);
        ArrayList<Double> allDividePos = new ArrayList<>();
        for (int i = 1; i < division_num; i++) {
            allDividePos.add(i * (1.d / division_num));
        }

        //get two random positions with appropriate intervals

        for (double i : allDividePos) {
            WB_Point start = frame.getSegment(0).getParametricPoint(i);
            WB_Point end = WB_GeometryOp.getClosestPoint3D(start, frame.getSegment(2));
            WB_Segment p = new WB_Segment(start, end);
            divideLines.add(p);
        }
        divideLines.add(frame.getSegment(1));

        for (int i = 0; i < divideLines.size() - 1; i++) {
            WB_PolyLine f = new WB_PolyLine(
                    divideLines.get(i).getPoint(0),
                    divideLines.get(i + 1).getPoint(0),
                    divideLines.get(i + 1).getEndpoint(),
                    divideLines.get(i).getEndpoint(),
                    divideLines.get(i).getPoint(0)
            );
            dividedFrames.add(f);
        }
        return dividedFrames;
    }
    private HE_Mesh myExtrudeMesh(HE_Mesh mesh1,double depth){
        HE_Mesh mesh3 = new HE_Mesh(mesh1);
        HE_Mesh mesh2 = new HE_Mesh(mesh1);

        HEM_Extrude modifier=new HEM_Extrude();
        modifier.setDistance(-depth);
        mesh3 .modify(modifier);
        mesh3 .add(mesh2);

        return  mesh3;
    }
}
