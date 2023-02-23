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

public class S_Quad_Hole extends BasicObject {

    WB_Point[] rectPts;

    public S_Quad_Hole(WB_Point[] rectPts) {
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
    boolean if_holes;
    double division_num;
    double holes_density;
    double holes_size;

    double thickness;

    boolean left_right;

    @Override
    protected void initPara() {
        top_height = putPara(600, 100, 1000, "top_height").getValue();
        bottom_height = putPara(0, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(600, 100, 1500, "top_depth").getValue();
        glass_offset = putPara(200, 0, 1500, "glass_offset").getValue();

        division_num = putPara(2, 2, 4, "division_num").getValue();
        thickness = putPara(20, 5, 200, "thickness").getValue();

        if_holes = putPara(true,"if_holes").getBoolean();
        left_right = putPara(false,"left_right").getBoolean();
        holes_density = putPara(4, 0, 8, "holes_density").getValue();
        holes_size = putPara(0.4, 0, 0.9, "holes_size").getValue();
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

        //HE_Mesh topMesh = new HEC_Box().setFromCorners(rectPts[3], rectPts[2].add(n.mul(this.top_depth).add(0, 0, -this.top_height))).create();
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
        StyledMesh whiteMesh_withEdges = new StyledMesh(Material.LightGray).add(bottomMesh);
        StyledMesh whiteMesh_withoutEdges = new StyledMesh(Material.LightGray, false);
        //StyledMesh glassMesh = new StyledMesh(Material.Glass).add(glassHemesh);
        StyledMesh glassMesh = new StyledMesh(Material.Glass);
        StyledPolygon whiteholePolygon = new StyledPolygon(0x00ffffff, 0x00000000,1);
        StyledPolyLine styledPolyLine = new StyledPolyLine(0x00000000, 4).add(frame);


        //*******************************************just for test
        ArrayList<WB_Polygon[]> panelsPolysWithHoles = new ArrayList<>();;
        ArrayList<WB_Polygon> allPanelPolysWithHoles = new ArrayList<>();;
        //getDividedFrames
        ArrayList<WB_PolyLine> dividedFrames = getDividedFrames(frame,(int)division_num);
        WB_PolyLine rect = new WB_PolyLine(rectPts[0],rectPts[1],rectPts[2],rectPts[3],rectPts[0]);
        ArrayList<WB_PolyLine> dividedRects = getDividedRects(rect,(int)division_num);
        styledPolyLine.addAll(dividedFrames);
        ArrayList<WB_PolyLine> glassesPolylines = getGlassesPolylines(dividedFrames);
        styledPolyLine.addAll(glassesPolylines);
        ArrayList<HE_Mesh> panelsMeshes = new ArrayList<>();
        ArrayList<WB_Quad[]> panelsQuads = getPanelMeshes(dividedRects,left_right);
        if (!if_holes) {
            for (WB_Quad[] panelQuads : panelsQuads) {
                HEC_FromQuads creator = new HEC_FromQuads();
                creator.setQuads(panelQuads);
                HE_Mesh mesh = new HE_Mesh(creator);
                HE_Mesh meshWithExtrude =  myExtrudeMesh(mesh, thickness);
                panelsMeshes.add(meshWithExtrude);
            }
            whiteMesh_withEdges.addAll(panelsMeshes);
        }else{
            panelsPolysWithHoles = getPanelPolysWithHoles(panelsQuads);
            for (WB_Polygon [] poly:panelsPolysWithHoles
                 ) {allPanelPolysWithHoles.add(poly[0]);
                allPanelPolysWithHoles.add(poly[1]);
            }

            ArrayList<HE_Mesh> panelsWithHoles = getPanelMeshesWithHolesFromPolygons(panelsPolysWithHoles);
            for (HE_Mesh ph :panelsWithHoles) {
                HE_Mesh phfe =  myExtrudeMesh(ph ,thickness);
                panelsMeshes.add(phfe);
            }
            whiteMesh_withoutEdges.addAll(panelsMeshes);
        }






        //getBeam
        ArrayList<HE_Mesh> beamsMeshes = new ArrayList<>();
        beamsMeshes = getBeamsMeshes(dividedRects,top_height,top_depth,glass_offset);
        whiteMesh_withEdges.addAll( beamsMeshes);
        //add glasses
        ArrayList<HE_Mesh> glasses = getGlasses(dividedFrames);
        glassMesh.addAll(glasses);



        whiteholePolygon.addAll(allPanelPolysWithHoles);


        panel_size = rect.getSegment(0).getLength()/(((int)division_num)*2) + "mm * "
                + rect.getSegment(1).getLength() + "mm";
        panel_num = division_num;


        glass_size = glassesPolylines.get(0).getSegment(0).getLength() + "mm * "
                + glassesPolylines.get(0).getSegment(1).getLength() + "mm";
        glass_num =  glassesPolylines.size();

        width = rect.getSegment(0).getLength();
        height = rect.getSegment(1).getLength();



        //*******************************************test end

        addGeometry(whiteMesh_withEdges);
        addGeometry(whiteMesh_withoutEdges);
        addGeometry(glassMesh);
        addGeometry(styledPolyLine);
        addGeometry(whiteholePolygon);
    }



    private ArrayList<WB_PolyLine> getDividedRects(WB_PolyLine rect, int division_num) {

        ArrayList<WB_PolyLine> dividedRects = new ArrayList<>();

        ArrayList<WB_Segment> divideLines = new ArrayList<WB_Segment>();
        WB_Segment segment_start =rect.getSegment(3);
        segment_start.reverse();
        divideLines.add(segment_start);
        ArrayList<Double> allDividePos = new ArrayList<>();
        for (int i = 1; i < division_num; i++) {
            allDividePos.add(i * (1.d / division_num));
        }

        //get two random positions with appropriate intervals
        for (double i : allDividePos) {
            WB_Point start = rect.getSegment(0).getParametricPoint(i);
            WB_Point end = WB_GeometryOp.getClosestPoint3D(start, rect.getSegment(2));
            WB_Segment p = new WB_Segment(start, end);
            divideLines.add(p);
        }
        divideLines.add(rect.getSegment(1));

        for (int i = 0; i < divideLines.size() - 1; i++) {
            WB_PolyLine f = new WB_PolyLine(
                    divideLines.get(i).getPoint(0),
                    divideLines.get(i + 1).getPoint(0),
                    divideLines.get(i + 1).getEndpoint(),
                    divideLines.get(i).getEndpoint(),
                    divideLines.get(i).getPoint(0)
            );
            dividedRects.add(f);
        }
        return dividedRects;
    }

    private ArrayList<HE_Mesh> getBeamsMeshes(ArrayList<WB_PolyLine> rects, double top_height, double top_depth, double glass_offset) {
        ArrayList<HE_Mesh> beamsMeshes = new ArrayList<>();
        for (WB_PolyLine rect: rects
             ) {

            WB_Point p0 = rect.getPoint(2);
            WB_Point p1 = p0.add(top_depth,0,0);
            WB_Point p2 = p1.sub(0,0,top_height);
            WB_Point p3 = p0.sub(0,0,top_height);
            p2 = p2.sub(0.4*(top_depth -glass_offset),0,0);
            WB_Polygon[] polygons = new WB_Polygon[]{
                    new WB_Polygon(p0,p1,p2,p3)
            };
            HE_Mesh beamMesh = new HE_Mesh(new HEC_FromPolygons().setPolygons(polygons));
            HEM_Extrude modifier=new HEM_Extrude();
            modifier.setDistance(-rect.getSegment(2).getLength());
            beamMesh.modify(modifier);
            beamsMeshes.add(beamMesh);
        }

        return  beamsMeshes;
    }

/*------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }

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

    private ArrayList<WB_Quad[]> getPanelMeshes(ArrayList<WB_PolyLine> dividedFrames,boolean left_right) {
        ArrayList<WB_PolyLine> dividedRects_Real = new ArrayList<>();
        for (WB_PolyLine df : dividedFrames) {
            ArrayList<WB_Point> ps = new ArrayList<>();
            for (WB_Coord p : df.getPoints().toList()){
                WB_Point newP = ((WB_Point) p).add(glass_offset,0,0);
                ps.add(newP);
            }
            WB_PolyLine pl = new WB_PolyLine(ps);
            dividedRects_Real.add(pl);
        }

        ArrayList< WB_Quad[]> panelsQuads = new ArrayList<>();
        for (WB_PolyLine df : dividedRects_Real) {
            WB_Polygon polygon = new WB_Polygon(df.getPoints());
            WB_Point[] f1 = new WB_Point[4];
            WB_Point[] f2 = new WB_Point[4];
            if(!left_right) {
                WB_Point toLeft = new WB_Point(df.getSegment(0).getDirection()).mul(-df.getSegment(0).getLength() / 2);

                f1[0] = df.getPoint(1).add(polygon.getNormal().mul(top_depth - glass_offset));
                f1[1] = df.getPoint(1);
                f1[2] = df.getPoint(2);
                f1[3] = df.getPoint(2).add(polygon.getNormal().mul(top_depth - glass_offset));

                f2[0] = f1[0].add(toLeft);
                f2[1] = f1[0];
                f2[2] = f1[3];
                f2[3] = f1[3].add(toLeft);
            }else{

                WB_Point toRight = new WB_Point(df.getSegment(0).getDirection()).mul(df.getSegment(0).getLength() / 2);
                f1[0] = df.getPoint(0);
                f1[1] = df.getPoint(0).add(polygon.getNormal().mul(top_depth - glass_offset));
                f1[2] = df.getPoint(3).add(polygon.getNormal().mul(top_depth - glass_offset));
                f1[3] = df.getPoint(3);


                f2[0] = f1[1];
                f2[1] = f1[1].add(toRight);
                f2[2] = f1[2].add(toRight);
                f2[3] = f1[2];

            }



            WB_Quad[] quads = new WB_Quad[2];
            quads[0] = new WB_Quad(f1[0], f1[1], f1[2], f1[3]);
            quads[1] = new WB_Quad(f2[0], f2[1], f2[2], f2[3]);



            panelsQuads.add(quads);
        }
        return panelsQuads;
    }

    private ArrayList<WB_Polygon[]> getPanelPolysWithHoles(ArrayList<WB_Quad[]> panelsQuads) {
        ArrayList<WB_Polygon[]> panelPolysWithHoles = new ArrayList<>();
        for (int i = 0; i < panelsQuads.size(); i++) {
            WB_Quad q = panelsQuads.get(i)[1];
            WB_Polygon panelPoly = quadToPoly(q);
            int u_num = (int) holes_density;
            double u_interval = (double)panelPoly.getSegment(0).getLength() / (double)u_num;
            int v_num = (int) (panelPoly.getSegment(1).getLength() / u_interval);
            double v_interval = panelPoly.getSegment(1).getLength() / v_num;

            System.out.println("u_num = " + u_num);
            System.out.println("v_num = " + v_num);
            System.out.println("u_interval = " + u_interval);
            System.out.println("v_interval = " + v_interval);
            System.out.println("panels.size() = " + panelsQuads.size());

            WB_Point stp = panelPoly.getPoint(0);

            ArrayList<WB_Circle> circles = new ArrayList<>();
            for (int u = 0; u < u_num ; u++) {
                for (int v = 0; v < v_num ; v++) {
                    WB_Point[] ws = new WB_Point[4];
                    ws[0] = new WB_Point(stp.xd(), stp.yd() + u * u_interval, stp.zd() + v * v_interval);
                    ws[1] = ws[0].add(new WB_Point(0, u_interval, 0));
                    ws[2] = ws[0].add(new WB_Point(0, u_interval, v_interval));
                    ws[3] = ws[0].add(new WB_Point(0, 0, v_interval));

                    WB_Polygon wp = new WB_Polygon(ws);

                    WB_Circle wc = new WB_Circle(wp.getCenter(), 0.5 * holes_size * u_interval);
                    circles.add(wc);
                }
            }

            WB_GeometryFactory gf = new WB_GeometryFactory();
            //convert circles into polygons
            ArrayList<WB_Point>[] holes = new ArrayList[circles.size()];
            for (int m = 0; m < holes.length; m++) {
                WB_Circle c = circles.get(m);
                ArrayList<WB_Point> cps = new ArrayList<>();
                int c_num = 12;
                for (int k = c_num -1; k >= 0 ; k--) {
                    cps.add(((WB_Point) (c.getCenter())).add(
                            new WB_Point(0,
                                    c.getRadius() * Math.cos(k * 2 * Math.PI / c_num),
                                    c.getRadius() * Math.sin(k * 2 * Math.PI / c_num)
                            ))
                    );
                }
                holes[m] = cps;
            }

            ArrayList<WB_Point> shell = new ArrayList<>();

            for (int m = 0; m < panelPoly.getPoints().size(); m++){

                shell.add(new WB_Point(panelPoly.getPoints().get(m)));
            }

            WB_Polygon newPoly = gf.createPolygonWithHoles(shell, holes);
            HEC_FromPolygons creator1 = new HEC_FromPolygons();
            WB_Polygon[] polygons = new WB_Polygon[2];
            polygons[0] = quadToPoly(panelsQuads.get(i)[0]);
            polygons[1] = newPoly;

            panelPolysWithHoles.add(polygons);

        }


        return  panelPolysWithHoles;
    }

    private ArrayList<HE_Mesh> getPanelMeshesWithHolesFromPolygons(ArrayList<WB_Polygon[]> panelsPolys) {
        ArrayList<HE_Mesh> panelsWithHoles = new ArrayList<>();
        for (  WB_Polygon[] polygons :panelsPolys
             ) {
            HEC_FromPolygons creator1 = new HEC_FromPolygons();
            creator1.setPolygons(polygons);
            HE_Mesh panelMeshWithHoles = new HE_Mesh(creator1);
            panelsWithHoles.add(panelMeshWithHoles);
        }
        return panelsWithHoles;
    }

//    private ArrayList<HE_Mesh> getPanelMeshesWithHoles(ArrayList<WB_Quad[]> panelsQuads) {
//        ArrayList<HE_Mesh> panelsWithHoles = new ArrayList<>();
//        for (int i = 0; i < panelsQuads.size(); i++) {
//            WB_Quad q = panelsQuads.get(i)[1];
//            WB_Polygon panelPoly = quadToPoly(q);
//            int u_num = (int) holes_density;
//            double u_interval = (double)panelPoly.getSegment(0).getLength() / (double)u_num;
//            int v_num = (int) (panelPoly.getSegment(1).getLength() / u_interval);
//            double v_interval = panelPoly.getSegment(1).getLength() / v_num;
//
//            System.out.println("u_num = " + u_num);
//            System.out.println("v_num = " + v_num);
//            System.out.println("u_interval = " + u_interval);
//            System.out.println("v_interval = " + v_interval);
//            System.out.println("panels.size() = " + panelsQuads.size());
//
//            WB_Point stp = panelPoly.getPoint(0);
//
//            ArrayList<WB_Circle> circles = new ArrayList<>();
//            for (int u = 0; u < u_num ; u++) {
//                for (int v = 0; v < v_num ; v++) {
//                    WB_Point[] ws = new WB_Point[4];
//                    ws[0] = new WB_Point(stp.xd(), stp.yd() + u * u_interval, stp.zd() + v * v_interval);
//                    ws[1] = ws[0].add(new WB_Point(0, u_interval, 0));
//                    ws[2] = ws[0].add(new WB_Point(0, u_interval, v_interval));
//                    ws[3] = ws[0].add(new WB_Point(0, 0, v_interval));
//
//                    WB_Polygon wp = new WB_Polygon(ws);
//
//                    WB_Circle wc = new WB_Circle(wp.getCenter(), 0.5 * holes_size * u_interval);
//                    circles.add(wc);
//                }
//            }
//
//            WB_GeometryFactory gf = new WB_GeometryFactory();
//            //convert circles into polygons
//            ArrayList<WB_Point>[] holes = new ArrayList[circles.size()];
//            for (int m = 0; m < holes.length; m++) {
//                WB_Circle c = circles.get(m);
//                ArrayList<WB_Point> cps = new ArrayList<>();
//                int c_num = 12;
//                for (int k = c_num -1; k >= 0 ; k--) {
//                    cps.add(((WB_Point) (c.getCenter())).add(
//                            new WB_Point(0,
//                                    c.getRadius() * Math.cos(k * 2 * Math.PI / c_num),
//                                    c.getRadius() * Math.sin(k * 2 * Math.PI / c_num)
//                            ))
//                    );
//                }
//                holes[m] = cps;
//            }
//
//            ArrayList<WB_Point> shell = new ArrayList<>();
//
//            for (int m = 0; m < panelPoly.getPoints().size(); m++){
//
//                shell.add(new WB_Point(panelPoly.getPoints().get(m)));
//            }
//
//            WB_Polygon newPoly = gf.createPolygonWithHoles(shell, holes);
//            HEC_FromPolygons creator1 = new HEC_FromPolygons();
//            WB_Polygon[] polygons = new WB_Polygon[2];
//            polygons[0] = quadToPoly(panelsQuads.get(i)[0]);
//            polygons[1] = newPoly;
//            creator1.setPolygons(polygons);
//
//            HE_Mesh panelMeshWithHoles = new HE_Mesh(creator1);
//            panelsWithHoles.add(panelMeshWithHoles);
//        }
//
//        return panelsWithHoles;
//    }

//    private ArrayList<HE_Mesh> getGlasses(ArrayList<WB_PolyLine> dividedFrames) {
//        ArrayList<HE_Mesh> glasses = new ArrayList<>();
//        for (WB_PolyLine dividedFrame : dividedFrames) {
//            ArrayList<WB_PolyLine> divided_glass = getDividedFrames(dividedFrame, 4);
//            for (WB_PolyLine pl : divided_glass
//            ) {
//                WB_Polygon poly = new WB_Polygon(pl.getPoints());
//                WB_Polygon[] polygons = new WB_Polygon[1];
//                polygons[0] = poly;
//                HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons().setPolygons(polygons));
//                glasses.add(mesh);
//            }
//        }
//        return glasses;
//    }

    private ArrayList<HE_Mesh> getGlasses(ArrayList<WB_PolyLine> dividedFrames) {
        ArrayList<HE_Mesh> glasses = new ArrayList<>();
        for (WB_PolyLine dividedFrame : dividedFrames) {
            ArrayList<WB_PolyLine> divided_glass = getDividedFrames(dividedFrame, 4);
            for (WB_PolyLine pl : divided_glass
            ) {
                WB_Polygon poly = new WB_Polygon(pl.getPoints());
                WB_Polygon[] polygons = new WB_Polygon[1];
                polygons[0] = poly;
                HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons().setPolygons(polygons));
                glasses.add(mesh);
            }
        }
        return glasses;
    }

    private ArrayList<WB_PolyLine> getGlassesPolylines(ArrayList<WB_PolyLine> dividedFrames) {
        ArrayList<WB_PolyLine> glassesPolylines = new ArrayList<>();
        for (WB_PolyLine dividedFrame : dividedFrames) {
            ArrayList<WB_PolyLine> divided_glass = getDividedFrames(dividedFrame, 4);
            glassesPolylines.addAll(divided_glass);
        }
        return  glassesPolylines;
    }





    private WB_Vector getUnitVector(WB_Vector p) {
        return p.div(p.getLength());
    }

    private  WB_Polygon quadToPoly(WB_Quad q){
        return new WB_Polygon(
                q.p1,
                q.p2,
                q.p3,
                q.p4
        );
    }

    private void  reverseList(ArrayList<WB_Point> shell){
        ArrayList<WB_Point> reversedList = new ArrayList<>();

        for (int i = shell.size() - 1; i >= 0; i--) {
            System.out.println("************list i = " + i);
            reversedList.add(shell.get(i));
        }

        shell = reversedList;
    };

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
