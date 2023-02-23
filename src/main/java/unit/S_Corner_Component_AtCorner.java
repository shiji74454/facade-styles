package unit;

import basic.*;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HEC_Polygon;
import wblut.hemesh.HEM_Extrude;
import wblut.hemesh.HE_Mesh;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class S_Corner_Component_AtCorner extends BasicObject {

    WB_Point[] rectPts;
    /**
     * ------------- parameters ------------
     */

    ArrayList<Integer> rects_picked = new ArrayList<>();
    double top_height ;
    double bottom_height ;
    double top_depth;
    double glass_offset = 0;
    //new para
    double division_num = 16;//写死这里是16份的话就可以引用标准构件库文件
    double extended_distance;

    double border_thickness = 50;
    double windowFrame_thickness;
    StyledPolygon styledPolygon;

    Map<String, ArrayList<Output_Component>> allOutput_components;
    /**
     * ------------- data for display ------------
     */
    double height;
    double width;

    double a_width;
    double b_width;
    String panel_size;
    double panel_num;
    String glass_size;
    double glass_num;

    double sash_num;
    double divide_distance;
    double window_pos;

    Map<Integer, WB_Polygon> dividedRectsMap;
    WB_Vector a_v1;
    WB_Vector a_v2;
    WB_Vector b_v1;
    WB_Vector b_v2;
    WB_Vector a_n;
    WB_Vector b_n;
    double column_width;
    WB_PolyLine frame;
    AtomicInteger left_rects_num = new AtomicInteger();
    AtomicInteger right_rects_num = new AtomicInteger();

    StyledPolyLine styledPolyLine2;

    public S_Corner_Component_AtCorner(WB_Point[] rectPts, int standardUnitDividedNum) {
        this.rectPts = rectPts;
        this.divide_distance = 8000.f / standardUnitDividedNum;
        initPara();
        initData();
        calculate();
    }

    @Override
    protected void initPara() {
        top_height = putPara(600, 400, 1000, "top_height").getValue();
        bottom_height = putPara(400, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(400, 200, 400, "top_depth").getValue();
        glass_offset = putPara(300, 100, 400, "glass_offset").getValue();
        extended_distance = putPara(500, 400, 900, "extended_distance").getValue();

        a_v1 = rectPts[1].subToVector3D(rectPts[0]);
        a_v2 = rectPts[5].subToVector3D(rectPts[0]);
        b_v1 = rectPts[2].subToVector3D(rectPts[1]);
        b_v2 = rectPts[4].subToVector3D(rectPts[1]);

        this.height = rectPts[5].getDistance3D(rectPts[0]);
        this.a_width = rectPts[1].getDistance3D(rectPts[0]);
        this.b_width = rectPts[2].getDistance3D(rectPts[1]);


        a_v1.normalizeSelf();
        a_v2.normalizeSelf();
        a_n = a_v1.cross(a_v2);
        a_n.normalizeSelf();

        b_v1.normalizeSelf();
        b_v2.normalizeSelf();
        b_n = b_v1.cross(b_v2);
        b_n.normalizeSelf();





        frame = new WB_PolyLine(
                rectPts[0].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset)),
                rectPts[1].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset)).add(b_n.mul(glass_offset)),
                rectPts[2].add(b_v2.mul(bottom_height)).add(b_n.mul(glass_offset)),
                rectPts[3].sub(b_v2.mul(top_height)).add(b_n.mul(glass_offset)),
                rectPts[4].sub(b_v2.mul(top_height)).add(b_n.mul(glass_offset)).add(a_n.mul(glass_offset)),
                rectPts[5].sub(a_v2.mul(top_height)).add(a_n.mul(glass_offset)),
                rectPts[0].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset))
        );
        column_width = glass_offset;

        //getdividedRects
        dividedRectsMap = getDividedRects(frame, this.divide_distance, this.border_thickness, column_width);

        dividedRectsMap.forEach((key, value) -> {
            if (key > 0) {
                right_rects_num.getAndIncrement();
            } else {
                left_rects_num.getAndIncrement();
            }
        });

        System.out.println("dividedRectsMap.keySet() = " + dividedRectsMap.keySet());
        System.out.println("left_rects_num = " + left_rects_num);
        System.out.println("right_rects_num = " + right_rects_num);

        windowFrame_thickness = putPara(50, 20, 100, "windowFrame_thickness").getValue();
        sash_num = putPara(9, 0, left_rects_num.doubleValue() + right_rects_num.doubleValue(), "sash_num").getValue();
        window_pos = putPara(-4, -left_rects_num.doubleValue(), right_rects_num.doubleValue(), "window_pos").getValue();
    }

    @Override
    protected void initData() {
        putData("height", "");
        putData("width", "");
        putData("panel_size", "");
        putData("panel_num", "");
        putData("glass_size", "");
        putData("glass_num", "");
    }

    /*
     * main body of calculation: generate data and styled objects according to current parameters
     */

    @Override
    protected void calculate() {
        rects_picked.clear();
        panel_size = "";
        glass_size = "";
        panel_num = 0;
        glass_num = 0;


        frame = new WB_PolyLine(
                rectPts[0].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset)),
                rectPts[1].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset)).add(b_n.mul(glass_offset)),
                rectPts[2].add(b_v2.mul(bottom_height)).add(b_n.mul(glass_offset)),
                rectPts[3].sub(b_v2.mul(top_height)).add(b_n.mul(glass_offset)),
                rectPts[4].sub(b_v2.mul(top_height)).add(b_n.mul(glass_offset)).add(a_n.mul(glass_offset)),
                rectPts[5].sub(a_v2.mul(top_height)).add(a_n.mul(glass_offset)),
                rectPts[0].add(a_v2.mul(bottom_height)).add(a_n.mul(glass_offset))
        );
        column_width = glass_offset;

        //getdividedRects
        dividedRectsMap = getDividedRects(frame, this.divide_distance, this.border_thickness, column_width);

        dividedRectsMap.forEach((key, value) -> {
            if (key > 0) {
                right_rects_num.getAndIncrement();
            } else {
                left_rects_num.getAndIncrement();
            }
        });

        System.out.println("dividedRectsMap.keySet() = " + dividedRectsMap.keySet());
        System.out.println("left_rects_num = " + left_rects_num);
        System.out.println("right_rects_num = " + right_rects_num);

        System.out.println("----------------------rectPts.length = " + rectPts.length);
        WB_Polygon bottom_polygon = new WB_Polygon(
                rectPts[0].add(a_n.mul(top_depth)),
                rectPts[1].add(a_n.mul(top_depth)).add(b_n.mul(top_depth)),
                rectPts[2].add(b_n.mul(top_depth)),
                rectPts[2],
                rectPts[1],
                rectPts[0],
                rectPts[0].add(a_n.mul(top_depth))
        );

        HE_Mesh bottomMesh = getMyExtrudedMesh(bottom_polygon, bottom_height);

        WB_Polygon top_polygon = getOffsetPolygonFromThreePoints(rectPts[5], rectPts[4], rectPts[3], top_depth, a_n, b_n);

        HE_Mesh topMesh = getMyExtrudedMesh(top_polygon, -top_height);
        StyledMesh brickMesh = new StyledMesh(Material.MIDGray).add(topMesh).add(bottomMesh);
        StyledMesh metalMesh = new StyledMesh(Material.LightGray);
        StyledMesh woodMesh = new StyledMesh(Material.Wheat1);
        StyledMesh glassMesh = new StyledMesh(Material.Glass);
        styledPolygon = new StyledPolygon(0xFF2E8B57, 0xFF2E8B57, 10);
        StyledPolyLine styledPolyLine = new StyledPolyLine(0xFF5F9EA0, 4);
        styledPolyLine2 = new StyledPolyLine(0xFF8B2323, 4);
        //******************************************************************************************************************just for test

        dividedRectsMap.forEach((key, value) -> styledPolyLine.add(value));

        WB_Polygon columnPoly = getColumnPoly(frame, a_n, b_n, a_v1, b_v1, this.glass_offset, column_width);
        woodMesh.add(getMyExtrudedMesh(columnPoly, frame.getSegment(2).getLength()));

        for (int i = 0; i < (int) this.sash_num; i++) {
            int num = i + (int) this.window_pos;
            if (num >= 0) num++;
            if (!rects_picked.contains(num) && num >= -left_rects_num.doubleValue() && num <= right_rects_num.doubleValue()&& rects_picked.size() < sash_num) {
                rects_picked.add(num);
            }
        }

        Collections.sort(rects_picked);

        //getWindowRangePolys
        HE_Mesh windowsRangeOutFrame = getWindowsOutFrame(dividedRectsMap, rects_picked, a_n, b_n, a_v1, b_v1, columnPoly);

        HE_Mesh brickMeshes = getBrickMeshes(dividedRectsMap, rects_picked, a_n, b_n, a_v1, b_v1, frame);
        brickMesh.add(brickMeshes);

        //getWindowsRects
        ArrayList<WB_Polygon> windowsInnerRects = getWindowsInnerRects(dividedRectsMap, rects_picked);

        ArrayList<HE_Mesh> windowsFramesMesh = getWindowsRealFrames(windowsInnerRects);
        woodMesh.addAll(windowsFramesMesh);

        ArrayList<HE_Mesh> windowsRealGlasses = getWindowGlasses(windowsInnerRects);
        glassMesh.addAll(windowsRealGlasses);

        double grating_width = dividedRectsMap.get(rects_picked.get(0)).getSegment(0).getLength();
        double grating_height = dividedRectsMap.get(rects_picked.get(0)).getSegment(1).getLength() - 2 * border_thickness;
        HE_Mesh gratingPanel = getGratingPanel(grating_width, grating_height, 50, 50);


        ArrayList<HE_Mesh> allGratingPanels = getAllGratingPanels(gratingPanel,a_v1,b_v1,a_n,b_n);
        woodMesh.addAll(allGratingPanels);
        styledPolygon.addAll(windowsInnerRects);
        metalMesh.add(windowsRangeOutFrame);

        ArrayList<HE_Mesh> guardMeshes = getGuardBarGlasses(300);
        glassMesh.addAll(guardMeshes);

        //*************************************************************************************************************************test end
        addGeometry(metalMesh);
        addGeometry(brickMesh);
        addGeometry(woodMesh);
        addGeometry(glassMesh);
        addGeometry(styledPolyLine);
        addGeometry(styledPolygon);
        addGeometry(styledPolyLine2);
    }
    /*------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }
    private ArrayList<WB_Polygon> getWindowsOutterRects(ArrayList<WB_Polygon> windowsInnerRects) {

        ArrayList<WB_Polygon> windowsOutterRects = new ArrayList<>();
        for (WB_Polygon poly : windowsInnerRects) {
            WB_Vector poly_v1 = poly.getPoint(1).subToVector3D(poly.getPoint(0));
            WB_Vector poly_v2 = poly.getPoint(3).subToVector3D(poly.getPoint(0));
            poly_v1.normalizeSelf();
            poly_v2.normalizeSelf();
            WB_Polygon shell = new WB_Polygon(
                    poly.getPoint(0).add(poly_v1.mul(-windowFrame_thickness)).add(poly_v2.mul(-windowFrame_thickness)),
                    poly.getPoint(1).add(poly_v1.mul(windowFrame_thickness)).add(poly_v2.mul(-windowFrame_thickness)),
                    poly.getPoint(2).add(poly_v1.mul(windowFrame_thickness)).add(poly_v2.mul(windowFrame_thickness)),
                    poly.getPoint(3).add(poly_v1.mul(-windowFrame_thickness)).add(poly_v2.mul(windowFrame_thickness))
            );
            windowsOutterRects.add(shell);
        }
        return windowsOutterRects;
    }



    private HE_Mesh getGratingPanel(double grating_width, double grating_height, double outter_bar_thickness, double inner_bar_thinkness) {
        HE_Mesh myGratingPanel = new HE_Mesh();
        WB_Polygon rect = new WB_Polygon(
                new WB_Point(0, 0, 0),
                new WB_Point(0, grating_width, 0),
                new WB_Point(0, grating_width, grating_height),
                new WB_Point(0, 0, grating_height),
                new WB_Point(0, 0, 0)
        );

        WB_Polygon inner_poly = new WB_Polygon(
                rect.getPoint(0).add(0,  outter_bar_thickness,  outter_bar_thickness),
                rect.getPoint(1).add(0, -outter_bar_thickness,  outter_bar_thickness),
                rect.getPoint(2).add(0, -outter_bar_thickness, -outter_bar_thickness),
                rect.getPoint(3).add(0,  outter_bar_thickness, -outter_bar_thickness),
                rect.getPoint(0).add(0,  outter_bar_thickness,  outter_bar_thickness)
        );

        WB_Polygon hole = myReversePolygon(inner_poly);
        WB_GeometryFactory gf = new WB_GeometryFactory();
        WB_Polygon outter_bar_poly = gf.createPolygonWithHole(rect.getPoints().toArray(), hole.getPoints().toArray());
        HE_Mesh outter_bar = getMyExtrudedMesh(outter_bar_poly, outter_bar_thickness);
        myGratingPanel.add(outter_bar);

        int divided_bar_num = 18;
        double interval = inner_poly.getSegment(1).getLength() / divided_bar_num;
        WB_Polygon bar_poly = new WB_Polygon(
                inner_poly.getPoint(0).sub(0, 0, inner_bar_thinkness / 2),
                inner_poly.getPoint(1).sub(0, 0, inner_bar_thinkness / 2),
                inner_poly.getPoint(1).add(0, 0, inner_bar_thinkness / 2),
                inner_poly.getPoint(0).add(0, 0, inner_bar_thinkness / 2),
                inner_poly.getPoint(0).sub(0, 0, inner_bar_thinkness / 2)
        );
        HE_Mesh basicBarMesh = getMyExtrudedMesh(bar_poly, inner_bar_thinkness * 0.5);

        WB_Transform3D move = new WB_Transform3D();
        for (int i = 1; i < divided_bar_num; i++) {

            move.addTranslate(new WB_Point(0, 0, interval));

            myGratingPanel.add(basicBarMesh.apply(move));
        }


        WB_Transform3D moveToOriginal = new WB_Transform3D();
        moveToOriginal.addTranslate(new WB_Point(-outter_bar_thickness*0.5,-grating_width*0.5,-grating_height*0.5));

        return myGratingPanel.apply(moveToOriginal);
    }

    ;

    private ArrayList<HE_Mesh> getWindowGlasses(ArrayList<WB_Polygon> windowsInnerRects) {
        ArrayList<HE_Mesh> windowGlasses = new ArrayList<>();

        for (WB_Polygon w : windowsInnerRects
        ) {
            windowGlasses.add(new HE_Mesh(new HEC_Polygon().setPolygon(w)));

        }
        return windowGlasses;
    }

    private ArrayList<HE_Mesh> getWindowsRealFrames(ArrayList<WB_Polygon> windowsInnerRects) {

        ArrayList<HE_Mesh> windowsRealFrame = new ArrayList<>();
        for (WB_Polygon poly : windowsInnerRects) {
            WB_Vector poly_v1 = poly.getPoint(1).subToVector3D(poly.getPoint(0));
            WB_Vector poly_v2 = poly.getPoint(3).subToVector3D(poly.getPoint(0));
            poly_v1.normalizeSelf();
            poly_v2.normalizeSelf();
            WB_Polygon shell = new WB_Polygon(
                    poly.getPoint(0).add(poly_v1.mul(-windowFrame_thickness)).add(poly_v2.mul(-windowFrame_thickness)),
                    poly.getPoint(1).add(poly_v1.mul(windowFrame_thickness)).add(poly_v2.mul(-windowFrame_thickness)),
                    poly.getPoint(2).add(poly_v1.mul(windowFrame_thickness)).add(poly_v2.mul(windowFrame_thickness)),
                    poly.getPoint(3).add(poly_v1.mul(-windowFrame_thickness)).add(poly_v2.mul(windowFrame_thickness))

            );

            WB_Polygon hole = myReversePolygon(poly);
            styledPolygon.add(shell);

            WB_GeometryFactory gf = new WB_GeometryFactory();
            WB_Polygon border_poly = gf.createPolygonWithHole(shell.getPoints().toArray(), hole.getPoints().toArray());

            HE_Mesh border_poly_mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{border_poly}));
            WB_Transform3D transform_moveback = new WB_Transform3D();
            transform_moveback.addTranslate(poly.getNormal().mul(-windowFrame_thickness / 2.d));
            HE_Mesh border_poly_mesh_withTrans = border_poly_mesh.apply(transform_moveback);
            HE_Mesh border_poly_mesh_withTrans_copy = new HE_Mesh(border_poly_mesh_withTrans);
            HEM_Extrude modifier_extrude = new HEM_Extrude();
            modifier_extrude.setDistance((windowFrame_thickness));

            border_poly_mesh_withTrans.modify(modifier_extrude);
            border_poly_mesh_withTrans.add(border_poly_mesh_withTrans_copy);

            windowsRealFrame.add(border_poly_mesh_withTrans);
        }

        return windowsRealFrame;
    }

    private ArrayList<WB_Polygon> getWindowsInnerRects(Map<Integer, WB_Polygon> dividedRectsMap, ArrayList<Integer> rects_picked) {

        ArrayList<WB_Polygon> windowsRects = new ArrayList<>();
        int start = rects_picked.get(0);
        int end = rects_picked.get(rects_picked.size() - 1);
        int left_picked_size = 0;
        int right_picked_size = 0;
        for (int i : rects_picked) {
            if (i > 0) {
                right_picked_size++;
            } else if (i < 0) {
                left_picked_size++;
            }
        }

        System.out.println("ArrayList<Integer> rects_picked = " + rects_picked);
        System.out.println("right_picked_size = " + right_picked_size);
        int left_doubleLength_window_num = (int) (left_picked_size / 2);
        int right_doubleLength_window_num = (int) (right_picked_size / 2);
        System.out.println("left_doubleLength_window_num= " + left_doubleLength_window_num);
        System.out.println("right_doubleLength_window_num = " + right_doubleLength_window_num);
        for (int i = 0; i < left_doubleLength_window_num; i++) {
            WB_Polygon polygon;
            if (right_picked_size != 0) {
                polygon = new WB_Polygon(
                        dividedRectsMap.get(-2 * i - 2).getPoint(0),
                        dividedRectsMap.get(-2 * i - 1).getPoint(1),
                        dividedRectsMap.get(-2 * i - 1).getPoint(2),
                        dividedRectsMap.get(-2 * i - 2).getPoint(3)
                );
            } else {
                polygon = new WB_Polygon(
                        dividedRectsMap.get(-2 * i - 1 + end).getPoint(0),
                        dividedRectsMap.get(-2 * i + end).getPoint(1),
                        dividedRectsMap.get(-2 * i + end).getPoint(2),
                        dividedRectsMap.get(-2 * i - 1 + end).getPoint(3)
                );
            }
            windowsRects.add(polygon);
        }
        if (left_picked_size % 2 != 0) {
            windowsRects.add(dividedRectsMap.get(start));
        }

        for (int i = 0; i < right_doubleLength_window_num; i++) {
            if (left_picked_size == 0) {
                WB_Polygon polygon = new WB_Polygon(
                        dividedRectsMap.get(2 * i + start).getPoint(0),
                        dividedRectsMap.get(2 * i + 1 + start).getPoint(1),
                        dividedRectsMap.get(2 * i + 1 + start).getPoint(2),
                        dividedRectsMap.get(2 * i + start).getPoint(3)
                );
                windowsRects.add(polygon);
            } else {
                WB_Polygon polygon = new WB_Polygon(
                        dividedRectsMap.get(2 * i + 1).getPoint(0),
                        dividedRectsMap.get(2 * i + 2).getPoint(1),
                        dividedRectsMap.get(2 * i + 2).getPoint(2),
                        dividedRectsMap.get(2 * i + 1).getPoint(3)
                );
                windowsRects.add(polygon);
            }
        }

        if (right_picked_size % 2 != 0) {
            windowsRects.add(dividedRectsMap.get(end));
        }

        for (int i = 0; i < windowsRects.size(); i++
        ) {
            WB_Polygon poly = new WB_Polygon(
                    windowsRects.get(i).getPoint(0).add(0, 0, border_thickness),
                    windowsRects.get(i).getPoint(1).add(0, 0, border_thickness),
                    windowsRects.get(i).getPoint(2).sub(0, 0, border_thickness),
                    windowsRects.get(i).getPoint(3).sub(0, 0, border_thickness)
            );

            WB_Transform3D transform3D = new WB_Transform3D();
            transform3D.addTranslate(new WB_Point(0, 0, 0).sub(poly.getCenter()));
            transform3D.addRotateZ(poly.getNormal().getAngle(new WB_Point(1, 0, 0)));

            System.out.println("poly.getNormal().getAngle(new WB_Point(1,0,0)) = " + poly.getNormal().getAngle(new WB_Point(1, 0, 0)));
            double sy = (poly.getSegment(0).getLength() - 2 * windowFrame_thickness) / poly.getSegment(0).getLength();
            double sz = (poly.getSegment(1).getLength() - 2 * windowFrame_thickness) / poly.getSegment(1).getLength();
            System.out.println("poly.getSegment(0).getLength() = " + poly.getSegment(0).getLength());
            transform3D.addScale(1, sy, 1);
            transform3D.addScale(1, 1, sz);
            transform3D.addRotateZ(-poly.getNormal().getAngle(new WB_Point(1, 0, 0)));
            transform3D.addTranslate(poly.getCenter());

            WB_Polygon newPoly = poly.apply(transform3D);
            windowsRects.set(i, newPoly);
        }

        return windowsRects;
    }

    private HE_Mesh getBrickMeshes(Map<Integer, WB_Polygon> dividedRectsMap, ArrayList<Integer> rects_picked, WB_Vector a_n, WB_Vector b_n, WB_Vector a_v1, WB_Vector b_v1, WB_PolyLine frame) {


        int start = rects_picked.get(0);
        int end = rects_picked.get(rects_picked.size() - 1);

        if (rects_picked.contains(1) && rects_picked.contains(-1)) {
            WB_Point s1_right = dividedRectsMap.get(start).getPoint(0).sub(a_v1.mul(border_thickness));
            WB_Point s1_left = frame.getPoint(0);
            WB_Polygon poly1 = new WB_Polygon(
                    s1_left.add(a_n.mul(top_depth - glass_offset)),
                    s1_right.add(a_n.mul(top_depth - glass_offset)),
                    s1_right.sub(a_n.mul(top_depth - glass_offset)),
                    s1_left.sub(a_n.mul(top_depth - glass_offset)));

            WB_Point s2_left = dividedRectsMap.get(end).getPoint(1).add(b_v1.mul(border_thickness));
            WB_Point s2_right = frame.getPoint(2);

            WB_Polygon poly2 = new WB_Polygon(
                    s2_left.add(b_n.mul(top_depth - glass_offset)),
                    s2_right.add(b_n.mul(top_depth - glass_offset)),
                    s2_right.sub(b_n.mul(top_depth - glass_offset)),
                    s2_left.sub(b_n.mul(top_depth - glass_offset)));

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2};
            return getMyExtrudedMesh(polygons, frame.getSegment(2).getLength());

        } else if (rects_picked.get(0) > 0 && !rects_picked.contains(1)) {

            WB_Polygon poly1 = getOffsetPolygonFromThreePoints(
                    frame.getPoint(0).sub(a_n.mul(glass_offset)),
                    frame.getPoint(1).sub(a_n.mul(glass_offset)).sub(b_n.mul(glass_offset)),
                    dividedRectsMap.get(start).getPoint(0).sub(b_n.mul(glass_offset)), top_depth, a_n, b_n
            );

            WB_Polygon poly2 = getOffsetPolygonFromTwoPoints(
                    dividedRectsMap.get(end).getPoint(1).sub(b_n.mul(glass_offset)).add(b_v1.mul(border_thickness)),
                    frame.getPoint(2).sub(b_n.mul(glass_offset)),
                    top_depth, b_n);


            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2};
            return getMyExtrudedMesh(polygons, frame.getSegment(2).getLength());

        } else if (rects_picked.get(rects_picked.size() - 1) < 0 && !rects_picked.contains(-1)) {
            WB_Polygon poly1 = getOffsetPolygonFromTwoPoints(
                    frame.getPoint(0).sub(a_n.mul(glass_offset)),
                    dividedRectsMap.get(start).getPoint(0).sub(a_v1.mul(border_thickness)).sub(a_n.mul(glass_offset)), top_depth, a_n);

            WB_Polygon poly2 = getOffsetPolygonFromThreePoints(
                    dividedRectsMap.get(end).getPoint(1).add(a_v1.mul(border_thickness)).sub(a_n.mul(glass_offset)),
                    frame.getPoint(1).sub(a_n.mul(glass_offset)).sub(b_n.mul(glass_offset)),
                    frame.getPoint(2).sub(b_n.mul(glass_offset)),
                    top_depth, a_n, b_n
            );
            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2};
            return getMyExtrudedMesh(polygons, frame.getSegment(2).getLength());

        } else if (rects_picked.get(rects_picked.size() - 1) < 0 && rects_picked.contains(-1)) {
            WB_Polygon poly1 = getOffsetPolygonFromTwoPoints(
                    frame.getPoint(0).sub(a_n.mul(glass_offset)),
                    dividedRectsMap.get(start).getPoint(0).sub(a_v1.mul(border_thickness)).sub(a_n.mul(glass_offset)), top_depth, a_n);
            WB_Polygon poly2 = getOffsetPolygonFromTwoPoints(
                    frame.getPoint(1).sub(b_n.mul(glass_offset)),
                    frame.getPoint(2).sub(b_n.mul(glass_offset)),
                    top_depth, b_n);

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2};
            return getMyExtrudedMesh(polygons, frame.getSegment(2).getLength());
        } else if (rects_picked.get(0) > 0 && rects_picked.contains(1)) {

            WB_Polygon poly1 = getOffsetPolygonFromTwoPoints(
                    frame.getPoint(0).sub(a_n.mul(glass_offset)),
                    frame.getPoint(1).sub(a_n.mul(glass_offset)),
                    top_depth, a_n);
            WB_Polygon poly2 = getOffsetPolygonFromTwoPoints(
                    dividedRectsMap.get(end).getPoint(1).add(b_v1.mul(border_thickness)).sub(b_n.mul(glass_offset)),
                    frame.getPoint(2).sub(b_n.mul(glass_offset)),
                    top_depth, b_n
            );

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2};
            return getMyExtrudedMesh(polygons, frame.getSegment(2).getLength());

        } else {
            return new HE_Mesh();
        }

    }


    private WB_Polygon getColumnPoly(WB_PolyLine frame, WB_Vector a_n, WB_Vector b_n, WB_Vector a_v1, WB_Vector b_v1, double glass_offset, double column_width) {
        return new WB_Polygon(
                frame.getPoint(1).sub(a_v1.mul(column_width)),
                frame.getPoint(1),
                frame.getPoint(1).add(b_v1.mul(column_width)),
                frame.getPoint(1).add(b_v1.mul(column_width)).sub(b_n.mul(glass_offset)),
                frame.getPoint(1).sub(b_n.mul(glass_offset)).sub(a_n.mul(glass_offset)),
                frame.getPoint(1).sub(a_v1.mul(column_width)).sub(a_n.mul(glass_offset))
        );
    }

    private WB_Polygon getOffsetPolygonFromThreePoints(WB_Point rectPt, WB_Point rectPt1, WB_Point rectPt2, double top_depth, WB_Vector a_n, WB_Vector b_n) {

        return new WB_Polygon(
                rectPt.add(a_n.mul(top_depth)),
                rectPt1.add(a_n.mul(top_depth)).add(b_n.mul(top_depth)),
                rectPt2.add(b_n.mul(top_depth)),
                rectPt2,
                rectPt1,
                rectPt,
                rectPt.add(a_n.mul(top_depth))
        );
    }

    private WB_Polygon getOffsetPolygonFromTwoPoints(WB_Point rectPt, WB_Point rectPt1, double top_depth, WB_Vector a_n) {

        return new WB_Polygon(
                rectPt.add(a_n.mul(top_depth)),
                rectPt1.add(a_n.mul(top_depth)),
                rectPt1,
                rectPt,
                rectPt.add(a_n.mul(top_depth))
        );
    }

    //getWindowsOutFrame**********************************************************************************************
    private HE_Mesh getWindowsOutFrame(Map<Integer, WB_Polygon> dividedRectsMap, ArrayList<Integer> rects_picked, WB_Vector a_n, WB_Vector b_n, WB_Vector a_v1, WB_Vector b_v1, WB_Polygon columnPoly) {
        int start = rects_picked.get(0);
        int end = rects_picked.get(rects_picked.size() - 1);
        System.out.println("rects_picked.get(0) = " + rects_picked.get(0));
        System.out.println("rects_picked.get(end) = " + rects_picked.get(rects_picked.size() - 1));


        if (rects_picked.contains(1) && rects_picked.contains(-1)) {
            System.out.println("------------------rects_picked.contains(1) && rects_picked.contains(-1)--------------------------------------");
            WB_Polygon poly1 = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness)),//0
                    columnPoly.getPoint(1).add(a_n.mul(extended_distance)).add(b_n.mul(extended_distance)),//1
                    dividedRectsMap.get(end).getPoint(1).add(b_n.mul(extended_distance)).add(b_v1.mul(border_thickness)),//2
                    dividedRectsMap.get(end).getPoint(1).sub(b_n.mul(glass_offset)).add(b_v1.mul(border_thickness)),//3
                    columnPoly.getPoint(3),//4
                    columnPoly.getPoint(2),//5
                    columnPoly.getPoint(1),//6
                    columnPoly.getPoint(0),//7
                    columnPoly.getPoint(5),//8
                    dividedRectsMap.get(start).getPoint(0).sub(a_n.mul(glass_offset)).sub(a_v1.mul(border_thickness)),//9
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness))//0

            );
            WB_Polygon poly2 = new WB_Polygon(
                    poly1.getPoint(2).add(0, 0, border_thickness),
                    dividedRectsMap.get(end).getPoint(2).add(b_n.mul(extended_distance)).add(b_v1.mul(this.border_thickness)).sub(0, 0, border_thickness),
                    dividedRectsMap.get(end).getPoint(2).add(b_v1.mul(this.border_thickness)).sub(b_n.mul(glass_offset)).sub(0, 0, border_thickness),
                    poly1.getPoint(3).add(0, 0, this.border_thickness),
                    poly1.getPoint(2).add(0, 0, border_thickness)
            );

            WB_Transform3D transform3D_bottomToUp = new WB_Transform3D();
            transform3D_bottomToUp.addTranslate(new WB_Point(0, 0, dividedRectsMap.get(end).getSegment(1).getLength() - this.border_thickness));
            WB_Polygon poly3 = poly1.apply(transform3D_bottomToUp);

            WB_Polygon poly4 = new WB_Polygon(
                    poly1.getPoint(0).add(0, 0, this.border_thickness),
                    poly1.getPoint(9).add(0, 0, this.border_thickness),
                    poly3.getPoint(9),
                    poly3.getPoint(0),
                    poly1.getPoint(0).add(0, 0, this.border_thickness)
            );
            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2, poly3, poly4};
            return getMyExtrudedMesh(polygons, border_thickness);
        } else if (rects_picked.contains(1) && !rects_picked.contains(-1)) {
            System.out.println("--------------------------------------------------------");
            WB_Polygon poly1 = new WB_Polygon(

                    columnPoly.getPoint(1).sub(b_v1.mul(extended_distance)).add(b_n.mul(extended_distance)),//0
                    dividedRectsMap.get(end).getPoint(1).add(b_v1.mul(border_thickness)).add(b_n.mul(extended_distance)),//1
                    dividedRectsMap.get(end).getPoint(1).add(b_v1.mul(border_thickness)).sub(b_n.mul(glass_offset)),//2
                    columnPoly.getPoint(3),//3
                    columnPoly.getPoint(2),//4
                    columnPoly.getPoint(1).sub(b_v1.mul(extended_distance)),//5
                    columnPoly.getPoint(1).sub(b_v1.mul(extended_distance)).add(b_n.mul(extended_distance))//0
            );

            WB_Polygon poly2 = new WB_Polygon(
                    poly1.getPoint(1).add(0, 0, border_thickness),
                    dividedRectsMap.get(end).getPoint(2).add(b_n.mul(extended_distance)).add(b_v1.mul(this.border_thickness)).sub(0, 0, border_thickness),
                    dividedRectsMap.get(end).getPoint(2).add(b_v1.mul(this.border_thickness)).sub(b_n.mul(glass_offset)).sub(0, 0, border_thickness),
                    poly1.getPoint(2).add(0, 0, this.border_thickness),
                    poly1.getPoint(1).add(0, 0, border_thickness)
            );

            WB_Transform3D transform3D_bottomToUp = new WB_Transform3D();
            transform3D_bottomToUp.addTranslate(new WB_Point(0, 0, dividedRectsMap.get(end).getSegment(1).getLength() - this.border_thickness));
            WB_Polygon poly3 = poly1.apply(transform3D_bottomToUp);

            WB_Polygon poly4 = new WB_Polygon(
                    poly1.getPoint(5).add(0, 0, this.border_thickness),
                    poly1.getPoint(4).add(0, 0, this.border_thickness),
                    poly3.getPoint(4),
                    poly3.getPoint(5),
                    poly1.getPoint(5).add(0, 0, this.border_thickness)
            );
            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2, poly3, poly4};
            return getMyExtrudedMesh(polygons, border_thickness);
        } else if (!rects_picked.contains(1) && rects_picked.contains(-1)) {
            WB_Polygon poly1 = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness)),
                    columnPoly.getPoint(1).add(a_n.mul(extended_distance)).add(a_v1.mul(extended_distance)),
                    columnPoly.getPoint(1).add(a_v1.mul(extended_distance)),
                    columnPoly.getPoint(0),
                    columnPoly.getPoint(5),
                    dividedRectsMap.get(start).getPoint(0).sub(a_n.mul(glass_offset)).sub(a_v1.mul(border_thickness)),
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness))
            );
            WB_Transform3D transform3D_bottomToUp = new WB_Transform3D();
            transform3D_bottomToUp.addTranslate(new WB_Point(0, 0, dividedRectsMap.get(end).getSegment(1).getLength() - this.border_thickness));
            WB_Polygon poly3 = poly1.apply(transform3D_bottomToUp);

            WB_Polygon poly2 = new WB_Polygon(
                    poly1.getPoint(3).add(0, 0, this.border_thickness),
                    poly1.getPoint(2).add(0, 0, this.border_thickness),
                    poly3.getPoint(2),
                    poly3.getPoint(3),
                    poly1.getPoint(3).add(0, 0, this.border_thickness)
            );

            WB_Polygon poly4 = new WB_Polygon(
                    poly1.getPoint(0).add(0, 0, this.border_thickness),
                    poly1.getPoint(5).add(0, 0, this.border_thickness),
                    poly3.getPoint(5),
                    poly3.getPoint(0),
                    poly1.getPoint(0).add(0, 0, this.border_thickness)
            );

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2, poly3, poly4};
            return getMyExtrudedMesh(polygons, border_thickness);
        } else if (rects_picked.get(0) < 0 && !rects_picked.contains(-1)) {

            WB_Polygon poly1 = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness)),
                    dividedRectsMap.get(end).getPoint(1).add(a_n.mul(extended_distance)).add(a_v1.mul(border_thickness)),
                    dividedRectsMap.get(end).getPoint(1).sub(a_n.mul(glass_offset)).add(a_v1.mul(this.border_thickness)),
                    dividedRectsMap.get(start).getPoint(0).sub(a_n.mul(glass_offset)).sub(a_v1.mul(border_thickness)),
                    dividedRectsMap.get(start).getPoint(0).add(a_n.mul(extended_distance)).sub(a_v1.mul(border_thickness))
            );
            WB_Transform3D transform3D_bottomToUp = new WB_Transform3D();
            transform3D_bottomToUp.addTranslate(new WB_Point(0, 0, dividedRectsMap.get(end).getSegment(1).getLength() - this.border_thickness));
            WB_Polygon poly3 = poly1.apply(transform3D_bottomToUp);

            WB_Polygon poly2 = new WB_Polygon(
                    poly1.getPoint(2).add(0, 0, this.border_thickness),
                    poly1.getPoint(1).add(0, 0, this.border_thickness),
                    poly3.getPoint(1),
                    poly3.getPoint(2),
                    poly1.getPoint(2).add(0, 0, this.border_thickness)
            );

            WB_Polygon poly4 = new WB_Polygon(
                    poly1.getPoint(0).add(0, 0, this.border_thickness),
                    poly1.getPoint(3).add(0, 0, this.border_thickness),
                    poly3.getPoint(3),
                    poly3.getPoint(0),
                    poly1.getPoint(0).add(0, 0, this.border_thickness)
            );

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2, poly3, poly4};

            return getMyExtrudedMesh(polygons, border_thickness);
        } else if (rects_picked.get(0) > 0 && !rects_picked.contains(1)) {
            WB_Polygon poly1 = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0).add(b_n.mul(extended_distance)).sub(b_v1.mul(border_thickness)),
                    dividedRectsMap.get(end).getPoint(1).add(b_n.mul(extended_distance)).add(b_v1.mul(border_thickness)),
                    dividedRectsMap.get(end).getPoint(1).sub(b_n.mul(glass_offset)).add(b_v1.mul(this.border_thickness)),
                    dividedRectsMap.get(start).getPoint(0).sub(b_n.mul(glass_offset)).sub(b_v1.mul(border_thickness)),
                    dividedRectsMap.get(start).getPoint(0).add(b_n.mul(extended_distance)).sub(b_v1.mul(border_thickness))
            );
            WB_Transform3D transform3D_bottomToUp = new WB_Transform3D();
            transform3D_bottomToUp.addTranslate(new WB_Point(0, 0, dividedRectsMap.get(end).getSegment(1).getLength() - this.border_thickness));
            WB_Polygon poly3 = poly1.apply(transform3D_bottomToUp);

            WB_Polygon poly2 = new WB_Polygon(
                    poly1.getPoint(2).add(0, 0, this.border_thickness),
                    poly1.getPoint(1).add(0, 0, this.border_thickness),
                    poly3.getPoint(1),
                    poly3.getPoint(2),
                    poly1.getPoint(2).add(0, 0, this.border_thickness)
            );

            WB_Polygon poly4 = new WB_Polygon(
                    poly1.getPoint(0).add(0, 0, this.border_thickness),
                    poly1.getPoint(3).add(0, 0, this.border_thickness),
                    poly3.getPoint(3),
                    poly3.getPoint(0),
                    poly1.getPoint(0).add(0, 0, this.border_thickness)
            );

            WB_Polygon[] polygons = new WB_Polygon[]{poly1, poly2, poly3, poly4};

            return getMyExtrudedMesh(polygons, border_thickness);
        } else {
            return null;
        }
    }


    private ArrayList<HE_Mesh> getAllGratingPanels(HE_Mesh gratingPanel, WB_Vector a_v1, WB_Vector b_v1, WB_Vector a_n, WB_Vector b_n) {

        ArrayList<HE_Mesh> gratingPanels = new ArrayList<>();

        //得到最左和最右的窗户序号
        int start = rects_picked.get(0);
        int end = rects_picked.get(rects_picked.size() - 1);

        //随机两侧窗户是否打开
        Random random = new Random();
        boolean open = random.nextBoolean();

        ArrayList<WB_Polygon> allWindowPolys = new ArrayList<>();
        for (int i : rects_picked) {
            allWindowPolys.add(dividedRectsMap.get(i));
        }


        if (!open ) {//如果所有窗户都不打开
            for (WB_Polygon poly : allWindowPolys) {
                WB_Transform3D trans = new WB_Transform3D();
                double angle = poly.getNormal().getAngle(new WB_Point(1,0,0));
                trans.addRotateAboutAxis(angle,new WB_Point(0,0,0),new WB_Vector(0,0,1));
                HE_Mesh mesh1 = gratingPanel.apply(trans);
                WB_Transform3D transToPolyCenter = new WB_Transform3D();
                transToPolyCenter.addTranslate(poly.getCenter());
                HE_Mesh mesh2 = mesh1.apply(transToPolyCenter);
                gratingPanels.add(mesh2);
            }
        }else{//如果有窗户打开
            for (int i = 0 ; i < rects_picked.size(); i++) {
                WB_Polygon poly = dividedRectsMap.get(rects_picked.get(i));
                WB_Transform3D trans = new WB_Transform3D();
                double angle = poly.getNormal().getAngle(new WB_Point(1, 0, 0));
                trans.addRotateAboutAxis(angle + Math.PI * 0.5, new WB_Point(0, 0, 0), new WB_Vector(0, 0, 1));
                HE_Mesh mesh1 = gratingPanel.apply(trans);
                WB_Transform3D transToPolyCenter = new WB_Transform3D();
                transToPolyCenter.addTranslate(poly.getCenter());
                HE_Mesh mesh2 = mesh1.apply(transToPolyCenter);

                WB_Transform3D transToSide = new WB_Transform3D();
                if (start * end > 0 ){
                    if (i < rects_picked.size()/2){
                        transToSide.addTranslate((dividedRectsMap.get(start).getSegment(3).getCenter()).
                                add(new WB_Point(poly.getSegment(0).getDirection()).
                                        mul(Math.abs(start - rects_picked.get(i) + 0.5 ) *windowFrame_thickness) )
                                .subToVector3D(poly.getCenter()));
                    }else {
                        transToSide.addTranslate((dividedRectsMap.get(end).getSegment(1).getCenter()).
                            add(new WB_Point(poly.getSegment(0).getDirection()).
                                    mul(Math.abs(end- rects_picked.get(i) + 0.5 ) *windowFrame_thickness*(-1)) )
                            .subToVector3D(poly.getCenter()));
                    }
                    transToSide.addTranslate(poly.getNormal().mul(poly.getSegment(0).getLength()*0.5 + 2*windowFrame_thickness ));
                }else{

                    if (rects_picked.get(i) <  0){
                        transToSide.addTranslate((dividedRectsMap.get(start).getSegment(3).getCenter()).
                                add(new WB_Point(poly.getSegment(0).getDirection()).
                                        mul(Math.abs(start - rects_picked.get(i) + 0.5 ) *windowFrame_thickness) )
                                .subToVector3D(poly.getCenter()));
                    } else{
                        transToSide.addTranslate((dividedRectsMap.get(end).getSegment(1).getCenter()).
                                add(new WB_Point(poly.getSegment(0).getDirection()).
                                        mul(Math.abs(end- rects_picked.get(i) + 0.5 ) *windowFrame_thickness*(-1)) )
                                .subToVector3D(poly.getCenter()));

                    }
                    transToSide.addTranslate(poly.getNormal().mul(poly.getSegment(0).getLength()*0.5 + 2* windowFrame_thickness));
                }
                HE_Mesh mesh3 = mesh2.apply(transToSide);
                gratingPanels.add(mesh3);

            }
        }

        System.out.println("gratingPanels.size()" + gratingPanels.size());
        return gratingPanels;
    }


    private ArrayList<HE_Mesh> getGuardBarGlasses(double guardBarHeight) {

        ArrayList<HE_Mesh> guardBarGlasses = new ArrayList<>();

        //得到最左和最右的窗户序号
        int start = rects_picked.get(0);
        int end = rects_picked.get(rects_picked.size() - 1);

        if(start * end >0) //说明窗户在同一侧
        {
            WB_Polygon guardBar_poly = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0),
                    dividedRectsMap.get(end).getPoint(1),
                    dividedRectsMap.get(end).getPoint(1).add(0, 0, guardBarHeight),
                    dividedRectsMap.get(start).getPoint(0).add(0, 0, guardBarHeight)
            );
            WB_Transform3D wb_transform3D = new WB_Transform3D();
            wb_transform3D.addTranslate(new WB_Point(100, 0, 0));
            WB_Polygon guardBar_poly_new = guardBar_poly.apply(wb_transform3D);
            guardBarGlasses.add(new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{guardBar_poly_new})));
        }else {
            WB_Polygon guardBar_poly1 = new WB_Polygon(
                    dividedRectsMap.get(start).getPoint(0),
                    dividedRectsMap.get(-1).getPoint(1),
                    dividedRectsMap.get(-1).getPoint(1).add(0, 0, guardBarHeight),
                    dividedRectsMap.get(start).getPoint(0).add(0, 0, guardBarHeight)
            );
            WB_Transform3D trans_1 = new WB_Transform3D();
            trans_1.addTranslate(guardBar_poly1.getNormal().mul(windowFrame_thickness));
            trans_1.addTranslate(new WB_Point(0,0,border_thickness));
            WB_Polygon guardBar_poly2 = new WB_Polygon(
                    dividedRectsMap.get(1).getPoint(0),
                    dividedRectsMap.get(end).getPoint(1),
                    dividedRectsMap.get(end).getPoint(1).add(0, 0, guardBarHeight),
                    dividedRectsMap.get(1).getPoint(0).add(0, 0, guardBarHeight)
            );
            WB_Transform3D trans_2 = new WB_Transform3D();
            trans_2.addTranslate(guardBar_poly2.getNormal().mul(windowFrame_thickness));
            trans_2.addTranslate(new WB_Point(0,0,border_thickness));
            guardBarGlasses.add(new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{guardBar_poly1.apply(trans_1),guardBar_poly2.apply(trans_2)})));
        }




        return guardBarGlasses;
    }


    private Map<Integer, WB_Polygon> getDividedRects(WB_PolyLine frame, double divide_distance, double border_thickness, double column_width) {
        double frameHeight = frame.getSegment(2).getLength();
        System.out.println("frame.getPoints().size() = " + frame.getPoints().size());
        Map<Integer, WB_Polygon> divideRectsMap = new HashMap<>();

        int left_rect_num = 0;
        double left_rest = frame.getSegment(0).getLength() - divide_distance - column_width;
        while (left_rest > 0.01 + border_thickness) {
            WB_Polygon poly = new WB_Polygon(
                    frame.getPoint(0).add(0, left_rest, 0),
                    frame.getPoint(0).add(0, left_rest + divide_distance, 0),
                    frame.getPoint(0).add(0, left_rest + divide_distance, 0).add(0, 0, frameHeight),
                    frame.getPoint(0).add(0, left_rest, 0).add(0, 0, frameHeight)
            );
            left_rect_num++;
            divideRectsMap.put(-left_rect_num, poly);
            left_rest = frame.getSegment(0).getLength() - (left_rect_num + 1) * divide_distance - column_width;
        }

        System.out.println("frame.getSegment(1).getLength() = " + frame.getSegment(1).getLength());
        System.out.println("frame.getSegment(1).getPoint(0) = " + frame.getSegment(1).getPoint(0));
        System.out.println("frame.getPoint(1) = " + frame.getPoint(1));

        int right_rect_num = 1;
        double right_pos = column_width;
        while (frame.getSegment(1).getLength() - (right_pos + divide_distance + border_thickness) > 0) {
            WB_Polygon poly = new WB_Polygon(
                    frame.getPoint(1).sub(right_pos, 0, 0),
                    frame.getPoint(1).sub(right_pos + divide_distance, 0, 0),
                    frame.getPoint(1).sub(right_pos + divide_distance, 0, 0).add(0, 0, frameHeight),
                    frame.getPoint(1).sub(right_pos, 0, 0).add(0, 0, frameHeight)
                    //frame.getPoint(1).sub(right_pos, 0, 0)
            );
            divideRectsMap.put(right_rect_num, poly);
            right_pos = column_width + (right_rect_num) * divide_distance;
            right_rect_num++;
        }

        return divideRectsMap;
    }


    private WB_Polygon myReversePolygon(WB_Polygon poly) {
        WB_Coord[] wb_coords = new WB_Coord[poly.getNumberOfPoints()];
        for (int i = 0; i < poly.getNumberOfPoints(); i++) {
            wb_coords[i] = poly.getPoint(poly.getNumberOfPoints() - i - 1);
        }
        return new WB_Polygon(wb_coords);
    }


    private ArrayList<Integer> generateRandomInt(int startNum, int endNum, double interval, int divideNum) {
        ArrayList<Integer> mylist = new ArrayList(); //生成数据集，用来保存随即生成数，并用于判断
        Random rd = new Random();
        int range = endNum - startNum;
        while (mylist.size() < divideNum) {

            int modulusNum = (int) (range / interval);
            int num = (int) (rd.nextInt(modulusNum - 1) * interval + startNum);

   /*         String str = String.format("%.2f",num);
            double two = Double.parseDouble(str);*/
            if (!mylist.contains(num)) {
                mylist.add(num); //往集合里面添加数据。
            }
        }
        for (double d : mylist
        ) {
            System.out.println("myRandomList" + d);
        }

        Collections.sort(mylist);

        return mylist;
    }

    private HE_Mesh getMyExtrudedMesh(WB_Polygon poly, double outter_bar_thickness) {
        HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{poly}));
        HE_Mesh mesh1 = new HE_Mesh(mesh);
        HEM_Extrude extrude = new HEM_Extrude();
        extrude.setDistance(outter_bar_thickness);
        mesh.modify(extrude);
        mesh1.add(mesh);
        return mesh1;

    }

    private HE_Mesh getMyExtrudedMesh(WB_Polygon[] polygons, double outter_bar_thickness) {
        HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons(polygons));
        System.out.println("mesh.getVertexNormals().length(before clear) = " + mesh.getVertexNormals().length);
        HE_Mesh mesh1 = new HE_Mesh(mesh);
        HEM_Extrude extrude = new HEM_Extrude();
        extrude.setDistance(outter_bar_thickness);
        mesh.modify(extrude);
        mesh1.add(mesh);
        return mesh1;
    }

    /* **********************all about output components*****************************/
//    private void setUnitNumberForOutputComponents(int unitNumber) {
//        for (int i = 0; i < this.allOutput_components.keySet().size(); i++) {
//            String materialName = this.allOutput_components.keySet().toArray()[i].toString();
//            for (Output_Component oc : this.allOutput_components.get(materialName)
//            ) {
//                oc.insertUnitNumber(unitNumber);
//            }
//        }
//    }
//    public Map<String, ArrayList<Output_Component>> getOutput_components() {
//        setAllOutPutComponents(windowsInnerRects);
//        return this.allOutput_components;
//    }
//
//    public Map<String, ArrayList<Output_Component>> getOutput_components(int unitNumber) {
//        setAllOutPutComponents(windowsInnerRects);
//        setUnitNumberForOutputComponents(unitNumber);
//        return this.allOutput_components;
//    }
//    private void setAllOutPutComponents(ArrayList<WB_Polygon> windowsInnerRects) {
//        //set metal board
//        allOutput_components = new HashMap<>();
//
//        ArrayList<Output_Component> output_components = new ArrayList<>();
//
//        for (int i = 0; i < windowsInnerRects.size(); i++) {
//            StyledPolyLine styledPolyLine_standard = new StyledPolyLine(0xFF458B00, 2);
//            StyledPolyLine styledPolyLine_dash = new StyledPolyLine(0xFFFF0000, 1);
//            Output_Component output_component = new Output_Component("Metal");
//
//            WB_Polygon temp = windowsInnerRects.get(i);
//            double temp_length = temp.getSegment(0).getLength();
//            ;
//            for (int j = 0; j < temp.getNumberSegments() - 1; j++) {
//
//                WB_PolyLine pl = new WB_PolyLine(
//                        new WB_Point(extended_distance, temp_length, 0),
//                        new WB_Point(0, temp_length, 0)
//                );
//                styledPolyLine_dash.add(pl);
//                temp_length += temp.getSegment(j + 1).getLength();
//            }
//
//            WB_PolyLine standardPolyline = new WB_PolyLine(
//                    new WB_Point(extended_distance, 0, 0),
//                    new WB_Point(extended_distance, temp_length, 0),
//                    new WB_Point(0, temp_length, 0),
//                    new WB_Point(0, 0, 0),
//                    new WB_Point(extended_distance, 0, 0)
//            );
//
//            styledPolyLine_standard.add(standardPolyline);
//
//            output_component.addStylePolyline(styledPolyLine_standard);
//            output_component.addStylePolyline(styledPolyLine_dash);
//            output_component.setSize(temp_length, extended_distance);
//            //output_component.setBoundingPoly();
//
//            output_components.add(output_component);
//        }
//        allOutput_components.put("Metal", output_components);
//
//        ArrayList<Output_Component> output_components_glass = new ArrayList<>();
//        for (HE_Mesh gb : guardBarGlasses) {
//            StyledPolyLine styledPolyLine_standard = new StyledPolyLine(0xFF0000FF, 2);
//            Output_Component output_component = new Output_Component("Glass");
//            WB_AABB aabb = gb.getAABB();
//            WB_Polygon temp = new WB_Polygon(
//                    new WB_Point(aabb.getDepth(), 0, 0),
//                    new WB_Point(aabb.getDepth(), aabb.getHeight(), 0),
//                    new WB_Point(0, aabb.getHeight(), 0),
//                    new WB_Point(0, 0, 0),
//                    new WB_Point(aabb.getDepth(), 0, 0)
//            );
//            System.out.println("output_components_glass: temp.getSignedArea() " + temp.getSignedArea());
//            System.out.println("output_components_glass: aabb.getHeight()" + aabb.getHeight());
//            System.out.println("output_components_glass: aabb.getDepth()" + aabb.getDepth());
//            styledPolyLine_standard.add(temp);
//            output_component.addStylePolyline(styledPolyLine_standard);
//            output_component.setSize(aabb.getHeight(), aabb.getDepth());
//            //output_component.setBoundingPoly();
//            output_components_glass.add(output_component);
//        }
//        allOutput_components.put("Glass", output_components_glass);
//    }
}
