package unit.making;

/*
 * @author : Shi Ji
 * @date : 15:18 2022-10-25
 */

import basic.BasicObject;
import basic.Material;
import basic.StyledMesh;
import basic.StyledPolyLine;
import wblut.geom.*;
import wblut.hemesh.HEC_Box;
import wblut.hemesh.HEC_FromQuads;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class S_ExtrudeIn_2 extends BasicObject {

    WB_Point[] rectPts;

    public S_ExtrudeIn_2(WB_Point[] rectPts) {
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
    double borderWidth;
    double extrudeIn_up_height;

    @Override
    protected void initPara() {
        top_height = putPara(100, 100, 1000, "top_height").getValue();
        bottom_height = putPara(100, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(900, 100, 1500, "top_depth").getValue();
        glass_offset = putPara(900, 0, 1500, "glass_offset").getValue();
        borderWidth = putPara(50, 50, 300, "borderWidth").getValue();
        extrudeIn_up_height = putPara(300, 0, 900, "extrudeIn_up_height").getValue();
    }

    /**
     * ------------- data for display ------------
     */
    double height;
    double width;
    double paintArea;
    ArrayList<WB_Segment> divideLines;
    @Override
    protected void initData() {
        putData("height", "");
        putData("width", "");
        putData("paintArea", "");
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

        HE_Mesh topMesh =  new HEC_Box().setFromCorners(rectPts[3],rectPts[2].add(n.mul(this.top_depth).add(0,0,-this.top_height))).create();
        HE_Mesh bottomMesh =  new HEC_Box().setFromCorners(rectPts[0],rectPts[1].add(n.mul(this.top_depth).add(0,0,this.bottom_height))).create();
        HE_Mesh glassHemesh =  new HEC_FromQuads(new WB_Quad[]{new WB_Quad(
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

        paintArea = 0;
        for (HE_Face face : topMesh.getFaces()) {
            paintArea+= face.getFaceArea();
        }
        for (HE_Face face : bottomMesh.getFaces()) {
            paintArea+= face.getFaceArea();
        }
        paintArea = (int)(paintArea/1e6);

        StyledMesh whiteMesh = new StyledMesh(Material.Concrete).add(topMesh).add(bottomMesh);
        //StyledMesh glassMesh = new StyledMesh(Material.Glass).add(glassHemesh);
        StyledMesh glassMesh = new StyledMesh(Material.Glass);
        //StyledPolyLine styledPolyLine = new StyledPolyLine(0x00000000,10).add(frame);

        StyledPolyLine styledPolyLine = new StyledPolyLine(0x00000000,10);

        //*******************************************just for test
        //*****************************************************************************************************************
        //divide frames
        ArrayList<WB_PolyLine> dividedFrames = new ArrayList<>();
        ArrayList<WB_Segment> divideLines= new ArrayList<WB_Segment>();
        WB_Segment segment_start = frame.getSegment(3);
        segment_start.reverse();
        divideLines.add(segment_start);
        int divideNum = (int)(Math.random()*2) + 1; //divide the units in 1-2 times for 2-3 parts randomly
        ArrayList<Double> allDividePos = this.generateRandomInt(0,1,0.2,divideNum);//get two random positions with appropriate intervals
        System.out.println("allDividePos.size = " + allDividePos.size());
        for (double i : allDividePos)
        {
            WB_Point start = frame.getSegment(0).getParametricPoint(i);
            WB_Point end = WB_GeometryOp.getClosestPoint3D(start,frame.getSegment(2));
            System.out.println(" allDividePosPoint " + i + " = " + "start " + start);
            System.out.println(" allDividePosPoint " + i + " = " + " end  " + end);
            WB_Segment  p = new  WB_Segment(start,end);

            divideLines.add(p);
        }
        divideLines.add(frame.getSegment(1));


        for(int  i = 0 ; i < divideLines.size()-1; i++){

            WB_PolyLine f = new WB_PolyLine(
                    divideLines.get(i).getPoint(0),
                    divideLines.get(i+1).getPoint(0),
                    divideLines.get(i+1).getEndpoint(),
                    divideLines.get(i).getEndpoint(),
                    divideLines.get(i).getPoint(0)
            );

            System.out.println( "divideLines.get(i).getPoint(0)   = " +  divideLines.get(i).getPoint(0));
            System.out.println( "divideLines.get(i+1).getPoint(0) = " +  divideLines.get(i+1).getPoint(0));
            System.out.println( "divideLines.get(i+1).getPoint(1) = " +  divideLines.get(i+1).getEndpoint());
            System.out.println( "divideLines.get(i).getPoint(1)   = " +  divideLines.get(i).getEndpoint());
            dividedFrames.add(f);
        }

        System.out.println("dividedFrames.size AT FIRST = " + dividedFrames.size());
        styledPolyLine.addAll(dividedFrames);
        System.out.println("dividedFrames.size AT SECOND= " + dividedFrames.size());


        //*****************************************************************************************************************
        //addLREdges
        System.out.println("dividedFrames.size = " + dividedFrames.size());

        ArrayList<HE_Mesh> LREdges = new ArrayList<>();

        for(int k = 0 ; k < dividedFrames.size(); k++){
        //for (WB_PolyLine df: dividedFrames){
            WB_PolyLine df = dividedFrames.get(k);
            WB_Polygon polygon = new WB_Polygon(df.getPoints());

            WB_Point toRight = new WB_Point(df.getSegment(0).getDirection()).mul(borderWidth);
            HE_Mesh mesh_l =  new HEC_Box().setFromCorners(
                    df.getPoint(0),
                    df.getPoint(3).add(toRight).add(polygon.getNormal().mul(-glass_offset))
            ).create();
            LREdges.add(mesh_l);

            WB_Point toLeft = new WB_Point(df.getSegment(0).getDirection()).mul(borderWidth);
            HE_Mesh mesh_r =  new HEC_Box().setFromCorners(
                    df.getPoint(1),
                    df.getPoint(2).sub(toLeft).add(polygon.getNormal().mul(-glass_offset))
            ).create();

//            System.out.println("df[" + i + "]" + "SP" +  df.getPoint(1));
//            System.out.println("df[" + i + "]" + "EP" +  df.getPoint(2).sub(new WB_Point(df.getSegment(0).getDirection()).mul(borderWidth)).add(polygon.getNormal().mul(-glass_offset)));

            LREdges.add(mesh_r);

        }
        System.out.println("LREdges.size = " +  LREdges.size());
        whiteMesh.addAll(LREdges);


        //*****************************************************************************************************************
        //applyExtrudeIn();
        // whiteMesh.addAll(applyExtrudeIn(dividedFrames));
        ArrayList<HE_Mesh> glasses_updates = new ArrayList<>();
        ArrayList<HE_Mesh> mesh_extrudeIn = new ArrayList<>();
        for(int k = 0 ; k < dividedFrames.size(); k++){
            //for (WB_PolyLine df: dividedFrames){
            WB_PolyLine df = dividedFrames.get(k);
            // ArrayList<WB_Segment>  pl_forExtrudeIn = new ArrayList<>();
            WB_Polygon polygon = new WB_Polygon(df.getPoints());

            WB_Vector borderOffset = new WB_Vector(df.getSegment(0).getDirection()).mul(borderWidth);
            WB_Segment s0 = new WB_Segment(df.getPoint(0).add(borderOffset), df.getPoint(3).add(borderOffset));
            //pl_forExtrudeIn.add(s0);

            Random r=new Random();
            double rate_s1 = (1+ r.nextInt(3))*0.1;
            double rate_s2 = 1- (1+ r.nextInt(3))*0.1;
            System.out.println("rate_s1 = " + rate_s1);
            System.out.println("rate_s2 = " + rate_s2);

            WB_Segment s3 = new WB_Segment(df.getPoint(1).add((borderOffset).mul(-1)), df.getPoint(2).add((borderOffset).mul(-1)));
            System.out.println("S3 = " + s3.getPoint(0) + " - " +s3.getEndpoint());
            // pl_forExtrudeIn.add(s3);

            WB_Segment middleLine = new WB_Segment(s0.getPoint(0),s3.getPoint(0));

            WB_Segment s1 = new WB_Segment(middleLine.getParametricPoint(rate_s1),middleLine.getParametricPoint(rate_s1).add(new WB_Point(0,0,df.getSegment(1).getLength()-extrudeIn_up_height)));
            WB_Segment s2 = new WB_Segment(middleLine.getParametricPoint(rate_s2), middleLine.getParametricPoint(rate_s2).add(new WB_Point(0,0,df.getSegment(1).getLength()-extrudeIn_up_height)));

            WB_Point move_fore = new WB_Point(-glass_offset,0, 0);
            WB_Point[] f1 = new WB_Point[4];
            f1[0] = (s0.getPoint(0)).add(move_fore);
            f1[1] = (s1.getPoint(0));
            f1[2] = (WB_Point) (s1.getEndpoint());
            f1[3] =( (WB_Point) (s0.getEndpoint())).add(move_fore);

            WB_Point[] f3 = new WB_Point[4];
            f3[0] = (s2.getPoint(0));
            f3[1] = (s3.getPoint(0)).add(move_fore);
            f3[2] = ((WB_Point) (s3.getEndpoint())).add(move_fore);
            f3[3] = (WB_Point) (s2.getEndpoint());

            WB_Point[] f2 = new WB_Point[4];
            f2[0] = f1[2];
            f2[1] = f3[3];
            f2[2] = f3[2];
            f2[3] = f1[3];

            WB_Quad[] quads = new WB_Quad[3];
            quads[0] = new WB_Quad(f1[0], f1[1],f1[2],f1[3]);
            quads[1] = new WB_Quad(f2[0], f2[1],f2[2],f2[3]);
            quads[2] = new WB_Quad(f3[0], f3[1],f3[2],f3[3]);
            HEC_FromQuads creator=new HEC_FromQuads();
            creator.setQuads(quads);
            HE_Mesh mesh= new HE_Mesh(creator);
            mesh_extrudeIn.add(mesh);

            //updates glasses
            HE_Mesh glass_every = new HEC_FromQuads(new WB_Quad[]{new WB_Quad(
                    f1[1],
                    f3[0],
                    f3[3],
                    f1[2]
            )}).create();
            glasses_updates.add(glass_every);
        }


        whiteMesh.addAll(mesh_extrudeIn);

        //update glasses
        glassMesh.addAll(glasses_updates);

      //  styledPolyLine.addAll(dividedFrames);

        //*******************************************test end
        addGeometry(whiteMesh);
        addGeometry(glassMesh);
        addGeometry(styledPolyLine);
    }


/*------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }


/*    private ArrayList<HE_Mesh> applyExtrudeIn( ArrayList<WB_PolyLine>dividedFrames ){
        ArrayList<HE_Mesh> mesh_extrudeIn = new ArrayList<>();
        for (WB_PolyLine df: dividedFrames){
           // ArrayList<WB_Segment>  pl_forExtrudeIn = new ArrayList<>();
            WB_Polygon polygon = new WB_Polygon(df.getPoints());

            WB_Vector borderOffset = new WB_Vector(df.getSegment(0).getDirection()).mul(borderWidth);
            WB_Segment s0 = new WB_Segment(df.getPoint(0).add(borderOffset), df.getPoint(3).add(borderOffset));
            //pl_forExtrudeIn.add(s0);

            Random r=new Random();
            double rate_s1 = (1+ r.nextInt(3))*0.1;
            double rate_s2 = 1- (1+ r.nextInt(3))*0.1;
            System.out.println("rate_s1 = " + rate_s1);
            System.out.println("rate_s2 = " + rate_s2);

            WB_Segment s3 = new WB_Segment(df.getPoint(1).add((borderOffset).mul(-1)), df.getPoint(2).add((borderOffset).mul(-1)));
            System.out.println("S3 = " + s3.getPoint(0) + " - " +s3.getEndpoint());
           // pl_forExtrudeIn.add(s3);
            
            WB_Segment middleLine = new WB_Segment(s0.getPoint(0),s3.getPoint(0));
            
            WB_Segment s1 = new WB_Segment(middleLine.getParametricPoint(rate_s1),middleLine.getParametricPoint(rate_s1).add(new WB_Point(0,0,df.getSegment(1).getLength()-extrudeIn_up_height)));
            WB_Segment s2 = new WB_Segment(middleLine.getParametricPoint(rate_s2), middleLine.getParametricPoint(rate_s2).add(new WB_Point(0,0,df.getSegment(1).getLength()-extrudeIn_up_height)));

            WB_Point move_fore = new WB_Point(-glass_offset,0, 0);
            WB_Point[] f1 = new WB_Point[4];
            f1[0] = (s0.getPoint(0)).add(move_fore);
            f1[1] = (s1.getPoint(0));
            f1[2] = (WB_Point) (s1.getEndpoint());
            f1[3] =( (WB_Point) (s0.getEndpoint())).add(move_fore);

            WB_Point[] f3 = new WB_Point[4];
            f3[0] = (s2.getPoint(0));
            f3[1] = (s3.getPoint(0)).add(move_fore);
            f3[2] = ((WB_Point) (s3.getEndpoint())).add(move_fore);
            f3[3] = (WB_Point) (s2.getEndpoint());

            WB_Point[] f2 = new WB_Point[4];
            f2[0] = f1[2];
            f2[1] = f3[3];
            f2[2] = f3[2];
            f2[3] = f1[3];

            WB_Quad[] quads = new WB_Quad[3];
            quads[0] = new WB_Quad(f1[0], f1[1],f1[2],f1[3]);
            quads[1] = new WB_Quad(f2[0], f2[1],f2[2],f2[3]);
            quads[2] = new WB_Quad(f3[0], f3[1],f3[2],f3[3]);
            HEC_FromQuads creator=new HEC_FromQuads();
            creator.setQuads(quads);
            HE_Mesh mesh= new HE_Mesh(creator);
            mesh_extrudeIn.add(mesh);



            //updates glasses
            HE_Mesh glass_every = new HEC_FromQuads(new WB_Quad[]{new WB_Quad(
                   f1[1],
                    f3[0],
                    f3[3],
                    f1[2]
            )}).create();
            glasses_updates.add(glass_every);


        }
        return mesh_extrudeIn;
    }*/







    private ArrayList<Double> generateRandomInt(int startNum, int endNum , double interval, int divideNum){
        ArrayList<Double> mylist = new ArrayList(); //生成数据集，用来保存随即生成数，并用于判断
        Random rd = new Random();
        int  range = endNum - startNum;
        while(mylist.size() < divideNum) {

            int modulusNum = (int)(range/interval);
            double num = ((int)(1+rd.nextInt(modulusNum-1)))* interval +startNum;

            String str = String.format("%.2f",num);
            double two = Double.parseDouble(str);

            if (!mylist.contains(two)) {
                mylist.add(two); //往集合里面添加数据。
            }
        }
        for (double  d : mylist
             ) {
                System.out.println("myRandomList"  + d);
        }

        Collections.sort(mylist);
        return mylist;
    }

    
    private WB_Vector getUnitVector(WB_Vector p){
        return p.div(p.getLength());
    }
    
}