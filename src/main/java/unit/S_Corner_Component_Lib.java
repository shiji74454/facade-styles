package unit;

import basic.*;
import wblut.geom.*;
import wblut.hemesh.*;

import java.util.*;

public class S_Corner_Component_Lib extends BasicObject {

    WB_Point[] rectPts;
    /**
     * ------------- parameters ------------
     */
    double top_height = 600;
    double bottom_height  = 500;
    double top_depth;
    double glass_offset = 0;
    //new para
    double division_num  = 16;//写死这里是16份的话就可以引用标准构件库文件
    double extended_distance;
    ArrayList<WB_Polygon> windowsInnerRects;
    double border_thickness;
    double  windowFrame_thickness;
    double windowNum;
    StyledPolygon whiteholePolygon;

    Map<String,ArrayList<Output_Component>> allOutput_components;
    /**
     * ------------- data for display ------------
     */
    double height;
    double width;
    String panel_size;
    double panel_num;
    String glass_size;
    double glass_num;
    ArrayList<HE_Mesh> guardBarGlasses;
    public S_Corner_Component_Lib(WB_Point[] rectPts) {
        this.rectPts = rectPts;
        initPara();
        initData();
        calculate();
    }

    @Override
    protected void initPara() {
        //top_height = putPara(600, 100, 1000, "top_height").getValue();
        //bottom_height = putPara(150, 100, 1000, "bottom_height").getValue();
        top_depth = putPara(400, 200, 400, "top_depth").getValue();
        glass_offset = putPara(120, 100, 200, "glass_offset").getValue();

        border_thickness = putPara(50, 50, 100, "border_thickness").getValue();
        windowFrame_thickness = putPara(50, 20, 100, "windowFrame_thickness").getValue();
        extended_distance = putPara(600, 400, 900, "extended_distance").getValue();
        windowNum = putPara(2,1,2,"window_num").getValue();
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

        StyledMesh woodMesh = new StyledMesh(Material.Wheat1);
       // StyledMesh brickMesh = new StyledMesh(Material.DarkGray).add(topMesh).add(bottomMesh);
        StyledMesh brickMesh = new StyledMesh(Material.DarkGray).add(topMesh).add(bottomMesh);;
        StyledMesh metalMesh = new StyledMesh(Material.LightGray);
        StyledMesh glassMesh = new StyledMesh(Material.Glass);
        whiteholePolygon = new StyledPolygon(0x00ffffff, 0x00000000, 3);
        StyledPolyLine styledPolyLine = new StyledPolyLine(0xFFFF0FFF, 4);

        //******************************************************************************************************************just for test
        //getdividedRects
        ArrayList<WB_Polygon> dividedRects = getDividedRects(frame, (int) division_num);

        //getWindowRangePolys
        ArrayList<ArrayList<WB_Polygon>> windowRangePolys = getWindowRangesPolys(dividedRects);

        //getWindowsRects
        ArrayList<ArrayList<WB_Polygon>> windowsRects = getWindowsRects(windowRangePolys);

        windowsInnerRects = new ArrayList<>();
        //getWindowsRangeOutFrames
        ArrayList<WB_Polygon> windowsOutterRects = new ArrayList<>();
        ArrayList<HE_Mesh> windowsRangeOutFrames = getWindowsRangeOutFramesAndSetInnerRect(windowsRects,border_thickness,windowsInnerRects,windowsOutterRects);
        metalMesh.addAll(windowsRangeOutFrames);

        ArrayList<HE_Mesh> brickWalls =  getBrickMeshes(windowsOutterRects,this.top_depth,frame);
        brickMesh.addAll(brickWalls);

        ArrayList<ArrayList<HE_Mesh>> windowsRealGlasses = new ArrayList<>();
        //getWindowsRealFrames
        ArrayList<ArrayList<HE_Mesh>>  windowsRealFrames = getWindowsRealFramesAndSetGlasses(windowsRects,border_thickness,windowFrame_thickness,windowsRealGlasses);
        for (ArrayList<HE_Mesh> windowRealFrame: windowsRealFrames) {
            woodMesh.addAll(windowRealFrame);
        }

        //getWindowsGlasses
        for (ArrayList<HE_Mesh> windowsRealGlass: windowsRealGlasses
        ) {
           glassMesh.addAll(windowsRealGlass);
        }

        //getGuardBarGlasses
        guardBarGlasses = getGuardBarGlasses(windowsInnerRects);
        glassMesh.addAll(guardBarGlasses);

        //getGratingPanels
        HE_Mesh gratingPanel = Component.GratingPanel.getMesh();
        //woodMesh.add(gratingPanel);

        Random random = new Random();
        ArrayList<ArrayList<HE_Mesh>> allGratingPanels = getAllGratingPanels(gratingPanel,windowsRects,random.nextBoolean());
        for (ArrayList<HE_Mesh> gratingPanels : allGratingPanels
        ) {
            woodMesh.addAll( gratingPanels );
        }

        //arrayGratingPanelsOn2D

        //addAllForDrawing
        //styledPolyLine.add(frame);

        //*************************************************************************************************************************test end
        addGeometry(metalMesh);
        addGeometry(brickMesh);
        addGeometry(woodMesh);
        addGeometry(glassMesh);
        addGeometry(styledPolyLine);
        addGeometry(whiteholePolygon);
    }

    private ArrayList<HE_Mesh> getBrickMeshes(ArrayList<WB_Polygon> windowsOutterRects, double top_depth, WB_PolyLine frame ) {
        ArrayList<HE_Mesh> brickMeshes = new ArrayList<>();

        ArrayList<WB_Point> points = new ArrayList<>();
        points.add(frame.getPoint(3));
        for (int i = 0 ; i< windowsOutterRects.size(); i++) {
            points.add(windowsOutterRects.get(i).getPoint(1));
            points.add(windowsOutterRects.get(i).getPoint(2));
        }
        points.add(frame.getPoint(2));


        ArrayList<WB_Point> toRemove = new ArrayList<>();
        for (int i = 0 ; i< points.size() - 1 ; i++) {
            if (Math.abs(points.get(i).yd() - points.get(i+1).yd())  < (border_thickness*3)){
                toRemove.add(points.get(i));
                toRemove.add(points.get(i+1));
            }
        };


        for(int i = 0 ; i < toRemove.size(); i++){
            points.remove(toRemove.get(i));
        }

        WB_Transform3D trans = new WB_Transform3D();
        trans.addTranslate(new WB_Point(-glass_offset,0,0));

        double middle_height = height - bottom_height - top_height;
        for(int i = 0 ; i < points.size()-1; i++){
            if(i % 2 == 0){
                WB_Polygon polygon = new WB_Polygon(
                        points.get(i).add(0,0,- middle_height),
                        points.get(i+1).add(0,0,-middle_height),
                        points.get(i+1),
                        points.get(i),
                        points.get(i).add(0,0,- middle_height)
                );

                WB_Polygon poly_trans = polygon.apply(trans);
                HE_Mesh mesh =  getMyExtrudedMesh(poly_trans,top_depth);
                brickMeshes.add(mesh);
            }
        }
        return  brickMeshes;
    }

    private ArrayList<ArrayList<HE_Mesh>> getAllGratingPanels(HE_Mesh gratingPanel, ArrayList<ArrayList<WB_Polygon>> windowsRects,Boolean if_open) {
        ArrayList<ArrayList<HE_Mesh>> allGratingPanels  = new ArrayList<>();
        for (ArrayList<WB_Polygon> windowsRects_everyRange : windowsRects){
            ArrayList<HE_Mesh> gratingPanels = new ArrayList<>();
            int windows_num = 0;
            for (WB_Polygon poly:windowsRects_everyRange
            ) {
                windows_num ++;
                if (poly.getSegment(0).getLength() > width/division_num + 0.1){
                    windows_num ++;
                }
            }

            if(if_open){
                    WB_Transform3D trans = new WB_Transform3D();
                    trans.addRotateAboutAxis(-Math.PI / 2, new WB_Point(0, 0, 0), new WB_Point(0, 0, 1));
                    trans.addTranslate(windowsRects_everyRange.get(0).getPoint(0).add(0, 0, border_thickness));
                    trans.addTranslate(new WB_Point(top_depth - glass_offset - gratingPanel.getAABB().getWidth(), 0, 0));

                    for (int i = 0; i <= windows_num / 2; i++) {
                        trans.addTranslate(new WB_Point(0, gratingPanel.getAABB().getWidth(), 0));
                        gratingPanels.add(gratingPanel.apply(trans));
                    }

                    WB_Transform3D trans_right = new WB_Transform3D();
                    trans_right.addRotateAboutAxis(-Math.PI / 2, new WB_Point(0, 0, 0), new WB_Point(0, 0, 1));
                    trans_right.addTranslate(windowsRects_everyRange.get(windowsRects_everyRange.size() - 1).getPoint(1).add(0, 0, border_thickness));
                    trans_right.addTranslate(new WB_Point(top_depth - glass_offset - gratingPanel.getAABB().getWidth(), 0, 0));
                    trans_right.addTranslate(new WB_Point(0, gratingPanel.getAABB().getWidth(), 0));

                    for (int i = windows_num - 1; i > windows_num / 2; i--) {
                        trans_right.addTranslate(new WB_Point(0, -gratingPanel.getAABB().getWidth(), 0));
                        gratingPanels.add(gratingPanel.apply(trans_right));
                    }

            }else{
                for (WB_Polygon poly:windowsRects_everyRange
                ) {
                    windows_num ++;
                    WB_Transform3D trans = new WB_Transform3D();
                    trans.addTranslate(poly.getPoint(0).add(0,0,border_thickness).add(top_depth - glass_offset - gratingPanel.getAABB().getWidth(),0,0));
                    gratingPanels.add(gratingPanel.apply(trans));
                    if (poly.getSegment(0).getLength() > width/division_num + 0.1){
                        WB_Transform3D trans_2 = new WB_Transform3D();
                        windows_num ++;
                        trans_2.addTranslate(poly.getSegment(0).getCenter().add(0,0,border_thickness).add(top_depth - glass_offset - gratingPanel.getAABB().getWidth(),0,0));
                        gratingPanels.add(gratingPanel.apply(trans_2));
                    }
                }
            }
            allGratingPanels.add(gratingPanels);
        }

        return  allGratingPanels;
    }
    /*------------- private detail generation methods ------------*/
//    private int shaderNum(){
//        return (int)(this.width/shaderUnitWidth);
//    }


    private void setAllOutPutComponents(ArrayList<WB_Polygon> windowsInnerRects) {
        //set metal board
        allOutput_components = new HashMap<>();

        ArrayList<Output_Component> output_components = new ArrayList<>();

        for (int i = 0; i < windowsInnerRects.size(); i++) {
            StyledPolyLine styledPolyLine_standard = new StyledPolyLine(0xFF458B00, 2);
            StyledPolyLine styledPolyLine_dash = new StyledPolyLine(0xFFFF0000, 1);
            Output_Component output_component = new Output_Component("Metal");

            WB_Polygon temp  = windowsInnerRects.get(i);
            double temp_length =  temp.getSegment(0).getLength();;
            for (int j = 0; j < temp.getNumberSegments() - 1; j++) {

                WB_PolyLine pl = new WB_PolyLine(
                        new WB_Point(extended_distance, temp_length, 0),
                        new WB_Point(0, temp_length, 0)
                );
                styledPolyLine_dash.add(pl);
                temp_length += temp.getSegment(j+1).getLength();
            }

            WB_PolyLine standardPolyline = new WB_PolyLine(
                    new WB_Point(extended_distance, 0, 0),
                    new WB_Point(extended_distance, temp_length, 0),
                    new WB_Point(0, temp_length, 0),
                    new WB_Point(0, 0, 0),
                    new WB_Point(extended_distance, 0, 0)
            );

            styledPolyLine_standard.add(standardPolyline);

            output_component.addStylePolyline(styledPolyLine_standard);
            output_component.addStylePolyline(styledPolyLine_dash);
            output_component.setSize(temp_length, extended_distance);
            //output_component.setBoundingPoly();

            output_components.add(output_component);
        }
        allOutput_components.put("Metal",output_components);

        ArrayList<Output_Component> output_components_glass = new ArrayList<>();
        for (HE_Mesh gb : guardBarGlasses) {
            StyledPolyLine styledPolyLine_standard= new StyledPolyLine(0xFF0000FF, 2);
            Output_Component output_component = new Output_Component("Glass");
            WB_AABB aabb = gb.getAABB();
            WB_Polygon temp = new WB_Polygon(
                    new WB_Point(aabb.getDepth(),0,0),
                    new WB_Point(aabb.getDepth(),aabb.getHeight(),0),
                    new WB_Point(0,aabb.getHeight(),0),
                    new WB_Point(0,0,0),
                    new WB_Point(aabb.getDepth(),0,0)
            );
            System.out.println("output_components_glass: temp.getSignedArea() " + temp.getSignedArea());
            System.out.println("output_components_glass: aabb.getHeight()" + aabb.getHeight());
            System.out.println("output_components_glass: aabb.getDepth()" + aabb.getDepth());
            styledPolyLine_standard.add(temp);
            output_component.addStylePolyline(styledPolyLine_standard);
            output_component.setSize(aabb.getHeight(), aabb.getDepth());
            //output_component.setBoundingPoly();
            output_components_glass.add(output_component);
        }
        allOutput_components.put("Glass",output_components_glass);
    }

    public  Map< String,ArrayList<Output_Component> > getOutput_components(){
        setAllOutPutComponents(windowsInnerRects);
        return this.allOutput_components;
    }
    public  Map< String,ArrayList<Output_Component> > getOutput_components(int unitNumber){
        setAllOutPutComponents(windowsInnerRects);
        setUnitNumberForOutputComponents(unitNumber);
        return this.allOutput_components;
    }

    private ArrayList<HE_Mesh> getGuardBarGlasses(ArrayList<WB_Polygon> windowsInnerRects) {

        ArrayList<HE_Mesh> guardBarGlasses = new ArrayList<>();
        double guardBarHeight = 300;
        for (WB_Polygon windowInnerRect: windowsInnerRects
             ) {
            WB_Polygon guardBar_poly = new WB_Polygon(
                    windowInnerRect.getPoint(0),
                    windowInnerRect.getPoint(1),
                    windowInnerRect.getPoint(1).add(0,0,guardBarHeight),
                    windowInnerRect.getPoint(0).add(0,0,guardBarHeight),
                    windowInnerRect.getPoint(0)
            );
            System.out.println("guardBar_poly.getSignedArea() = " + windowInnerRect.getSignedArea());
            WB_Transform3D wb_transform3D = new WB_Transform3D();
            wb_transform3D.addTranslate(new WB_Point(100,0,0));
            WB_Polygon guardBar_poly_new = guardBar_poly.apply(wb_transform3D);
            guardBarGlasses.add(new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{ guardBar_poly_new})));

        }
        System.out.println("guardBarGlasses.size() = " + guardBarGlasses.size());
        System.out.println("guardBarGlasses.area() = " + guardBarGlasses.get(0));
        return guardBarGlasses;
    }

    private  ArrayList<ArrayList<HE_Mesh>>  getWindowsRealFramesAndSetGlasses(
            ArrayList<ArrayList<WB_Polygon>> windowsRects, double border_thickness,double windowFrame_thickness,
            ArrayList<ArrayList<HE_Mesh>> windowsRealGlasses) {

        ArrayList<ArrayList<HE_Mesh>> windowsRealFrames = new ArrayList<>();


        for (ArrayList<WB_Polygon> windowsFrames_usingRects : windowsRects
        ) {
            ArrayList<HE_Mesh> windowsRealFrame = new ArrayList<>();

            ArrayList<HE_Mesh> windowsRealGlasses_everyRange  = new ArrayList<>();
            for(WB_Polygon polygon : windowsFrames_usingRects){
                WB_Polygon shell = new WB_Polygon(
                        polygon.getPoint(0).add(0, 0, border_thickness),
                        polygon.getPoint(1).add(0, 0, border_thickness),
                        polygon.getPoint(2).sub( 0, 0, border_thickness),
                        polygon.getPoint(3).sub( 0, 0, border_thickness),
                        polygon.getPoint(0).add(0, 0, border_thickness)
                );
                
                WB_Polygon hole = new WB_Polygon(
                        shell.getPoint(0).add(0,windowFrame_thickness,windowFrame_thickness),
                        shell.getPoint(3).add(0, windowFrame_thickness, -windowFrame_thickness),
                        shell.getPoint(2).add(0, -windowFrame_thickness,-windowFrame_thickness),
                        shell.getPoint(1).add(0,-windowFrame_thickness,windowFrame_thickness),
                        shell.getPoint(0).add(0,windowFrame_thickness,windowFrame_thickness)
                );


                HE_Mesh glass = new HE_Mesh(new HEC_FromPolygons().setPolygons(new WB_Polygon[]{hole}));

                windowsRealGlasses_everyRange.add(glass);

                WB_GeometryFactory gf = new WB_GeometryFactory();
                WB_Polygon border_poly = gf.createPolygonWithHole(shell.getPoints().toArray(), hole.getPoints().toArray());

                HE_Mesh  border_poly_mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{border_poly}));
                WB_Transform3D transform_moveback = new WB_Transform3D();
                transform_moveback.addTranslate(new WB_Point(-windowFrame_thickness/2.d,0,0));
                HE_Mesh border_poly_mesh_withTrans = border_poly_mesh.apply(transform_moveback);
                HE_Mesh border_poly_mesh_withTrans_copy = new HE_Mesh(border_poly_mesh_withTrans);
                HEM_Extrude modifier_extrude=new HEM_Extrude();
                modifier_extrude.setDistance((windowFrame_thickness));

                border_poly_mesh_withTrans.modify( modifier_extrude);
                border_poly_mesh_withTrans.add( border_poly_mesh_withTrans_copy);

                windowsRealFrame .add(border_poly_mesh_withTrans);
            }

            windowsRealFrames.add(windowsRealFrame);


            windowsRealGlasses.add(windowsRealGlasses_everyRange);
        }
        
        return  windowsRealFrames;
        
    }

    private ArrayList<HE_Mesh> getWindowsRangeOutFramesAndSetInnerRect(ArrayList<ArrayList<WB_Polygon>> windowsRects,
                                                                       double border_thickness,
                                                                       ArrayList<WB_Polygon> windowsInnerRects,
                                                                       ArrayList<WB_Polygon> windowsOutterRects) {
        ArrayList<HE_Mesh> windowsRangeOutFrames = new ArrayList<>();

        for (ArrayList<WB_Polygon> windowsRect_usingRects : windowsRects
        ) {
            WB_Polygon polygon = new WB_Polygon(
                    windowsRect_usingRects.get(0).getPoint(0),
                    windowsRect_usingRects.get(windowsRect_usingRects.size() - 1).getPoint(1),
                    windowsRect_usingRects.get(windowsRect_usingRects.size() - 1).getPoint(2),
                    windowsRect_usingRects.get(0).getPoint(3),
                    windowsRect_usingRects.get(0).getPoint(0)
            );
            WB_Polygon hole = new WB_Polygon(
                    polygon.getPoint(0).add(0, 0, border_thickness),
                    polygon.getPoint(1).add(0, 0, border_thickness),
                    polygon.getPoint(2).sub(0, 0, border_thickness),
                    polygon.getPoint(3).sub(0, 0, border_thickness),
                    polygon.getPoint(0).add(0, 0, border_thickness)
            );
            WB_Polygon shell = new WB_Polygon(
                    polygon.getPoint(0).sub(0, border_thickness, 0),
                    polygon.getPoint(3).sub(0, border_thickness, 0),
                    polygon.getPoint(2).add( 0, border_thickness, 0),
                    polygon.getPoint(1).add( 0, border_thickness, 0),
                    polygon.getPoint(0).sub(0, border_thickness, 0)
            );

            //windowsInnerRects.add(myReversePolygon(hole));
            windowsOutterRects.add(shell);
            windowsInnerRects.add( hole);
            WB_GeometryFactory gf = new WB_GeometryFactory();

            WB_Polygon border_poly = gf.createPolygonWithHole(shell.getPoints().toArray(), hole.getPoints().toArray());

            HE_Mesh  border_poly_mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{border_poly}));
            WB_Transform3D transform_moveback = new WB_Transform3D();
            transform_moveback.addTranslate(new WB_Point(-glass_offset,0,0));
            HE_Mesh border_poly_mesh_withTrans = border_poly_mesh.apply(transform_moveback);
            HE_Mesh border_poly_mesh_withTrans_copy = new HE_Mesh(border_poly_mesh_withTrans);
            HEM_Extrude modifier_extrude=new HEM_Extrude();
            modifier_extrude.setDistance(-(glass_offset + extended_distance));

            border_poly_mesh_withTrans.modify( modifier_extrude);
            border_poly_mesh_withTrans.add( border_poly_mesh_withTrans_copy);

            windowsRangeOutFrames.add(border_poly_mesh_withTrans);

        }
        return windowsRangeOutFrames;
    }


    private ArrayList<ArrayList<WB_Polygon>> getWindowsRects(ArrayList<ArrayList<WB_Polygon>> windowRangePolys) {

        ArrayList<ArrayList<WB_Polygon>> allWindowRectsInRange = new ArrayList<>();

        for (int i = 0; i < windowRangePolys.size(); i++) {
            ArrayList<WB_Polygon> windowFramesRects = new ArrayList<>();

            if (windowRangePolys.get(i).size() == 1) {
                windowFramesRects.add(windowRangePolys.get(i).get(0));
            } else {
                for (int k = 0; k < windowRangePolys.get(i).size() / 2; k++) {
                    WB_Polygon poly = new WB_Polygon(
                            windowRangePolys.get(i).get(2 * k).getPoint(0),
                            windowRangePolys.get(i).get(2 * k + 1).getPoint(1),
                            windowRangePolys.get(i).get(2 * k + 1).getPoint(2),
                            windowRangePolys.get(i).get(2 * k).getPoint(3),
                            windowRangePolys.get(i).get(2 * k).getPoint(0)
                    );

                    windowFramesRects.add(poly);
                }

                if (windowRangePolys.get(i).size() % 2 != 0) {
                    windowFramesRects.add(windowRangePolys.get(i).get(windowRangePolys.get(i).size() - 1));
                }


                allWindowRectsInRange.add(windowFramesRects);
            }

        }

        int total_size = 0;
        for (int i = 0 ; i < allWindowRectsInRange.size(); i++){
            total_size  += allWindowRectsInRange.get(i).size();
        }
        if (total_size >= division_num){


            allWindowRectsInRange.remove(allWindowRectsInRange.size()-1);
        }

        if(allWindowRectsInRange.get(0).get(0).getPoint(0).yd() - border_thickness < 0){
            for (int i = 0 ; i < allWindowRectsInRange.size(); i++){
                for(int j = 0; j < allWindowRectsInRange.get(i).size(); j++){

                    WB_Transform3D trans_temp = new WB_Transform3D();
                    trans_temp.addTranslate(new WB_Point(0,border_thickness,0));
                    WB_Polygon poly_temp = allWindowRectsInRange.get(i).get(j).apply(trans_temp);
                    allWindowRectsInRange.get(i).set(j,poly_temp);

                }
            }

            for (int i = 0 ; i < allWindowRectsInRange.size() - 1; i++) {
                double in = allWindowRectsInRange.get(i).get(allWindowRectsInRange.get(i).size()-1).getPoint(1).yd()+ border_thickness - allWindowRectsInRange.get(i+1).get(0).getPoint(0).yd();
                if(in >0) {
                    int j = i + 1;
                    for (int k = 0; k < allWindowRectsInRange.get(j).size(); k++) {
                       WB_Transform3D trans_temp = new WB_Transform3D();
                        trans_temp.addTranslate(new WB_Point(0,in,0));
                        WB_Polygon poly_temp =  allWindowRectsInRange.get(j).get(k).apply(trans_temp);
                        allWindowRectsInRange.get(j).set(k,poly_temp);
                    }
                }
            }
        }

        double v = allWindowRectsInRange.get(allWindowRectsInRange.size()-1).get(allWindowRectsInRange.get(allWindowRectsInRange.size()-1).size()-1).getPoint(1).yd() + border_thickness - rectPts[1].yd();
        if(v > 0 ){
            for (int i = 0 ; i < allWindowRectsInRange.size(); i++){
                for(int j = 0; j < allWindowRectsInRange.get(i).size(); j++){

                    WB_Transform3D trans_temp = new WB_Transform3D();
                    trans_temp.addTranslate(new WB_Point(0,-v,0));
                    WB_Polygon poly_temp  = allWindowRectsInRange.get(i).get(j).apply(trans_temp);
                    allWindowRectsInRange.get(i).set(j,poly_temp);
                }
            }

            for (int i = allWindowRectsInRange.size()-1; i > 0; i--) {
                double in = allWindowRectsInRange.get(i-1).get(allWindowRectsInRange.get(i-1).size()-1).getPoint(1).yd()+ border_thickness - allWindowRectsInRange.get(i).get(0).getPoint(0).yd();
                if(in >0) {
                    int j = i - 1;
                    for (int k = 0; k < allWindowRectsInRange.get(j).size(); k++) {

                        WB_Transform3D trans_temp = new WB_Transform3D();
                        trans_temp.addTranslate(new WB_Point(0,-in,0));
                        WB_Polygon poly_temp = allWindowRectsInRange.get(j).get(k).apply(trans_temp);
                        allWindowRectsInRange.get(j).set(k,poly_temp);
                    }
                }
            }

        }

        for(int i = 0 ; i <  allWindowRectsInRange.size()-1; i++){
            double in = allWindowRectsInRange.get(i).get(allWindowRectsInRange.get(i).size()-1).getPoint(1).yd()+ border_thickness - allWindowRectsInRange.get(i+1).get(0).getPoint(0).yd();
            if(in >0) {
                int j = i + 1;
                for (int k = 0; k < allWindowRectsInRange.get(j).size(); k++) {
                    WB_Transform3D trans_temp = new WB_Transform3D();
                    trans_temp.addTranslate(new WB_Point(0,2*in,0));
                    WB_Polygon poly_temp =  allWindowRectsInRange.get(j).get(k).apply(trans_temp);
                    allWindowRectsInRange.get(j).set(k,poly_temp);
                }
            }
        }
        return allWindowRectsInRange;
    }

    private ArrayList<ArrayList<WB_Polygon>> getWindowRangesPolys(ArrayList<WB_Polygon> dividedRects) {
        ArrayList<ArrayList<WB_Polygon>> windowRangesPolys = new ArrayList<>();
        int windowRangeNum = (int)this.windowNum + 1;
        ArrayList<Integer> randomRange = generateRandomInt(0, dividedRects.size()+ 1, 1, windowRangeNum + 1);
        for (int i = 0; i < windowRangeNum; i++) {
            if(i % 2 == 0) {
                ArrayList<WB_Polygon> windowRange_usingRects = new ArrayList<>();
                for (int j = randomRange.get(i); j <= randomRange.get(i + 1); j++) {
                    windowRange_usingRects.add(dividedRects.get(j));
                }
                windowRangesPolys.add(windowRange_usingRects);
            }

        }
        return windowRangesPolys;
    }

    private ArrayList<WB_Polygon> getDividedRects(WB_PolyLine rect, int division_num) {

        ArrayList<WB_Polygon> dividedRects = new ArrayList<>();

        ArrayList<WB_Segment> divideLines = new ArrayList<WB_Segment>();
        WB_Segment segment_start = rect.getSegment(3);
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
            WB_Polygon polygon = new WB_Polygon(
                    divideLines.get(i).getPoint(0),
                    divideLines.get(i + 1).getPoint(0),
                    divideLines.get(i + 1).getEndpoint(),
                    divideLines.get(i).getEndpoint(),
                    divideLines.get(i).getPoint(0)
            );
            dividedRects.add(polygon);
        }
        return dividedRects;
    }





    private WB_Polygon myReversePolygon(WB_Polygon poly) {

        WB_Coord[] wb_coords = new WB_Coord[poly.getNumberOfPoints()];
        for(int i = 0 ; i < poly.getNumberOfPoints(); i++){

            wb_coords[i] = poly.getPoint(poly.getNumberOfPoints()-i-1);

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

    private HE_Mesh getMyExtrudedMesh(WB_Polygon poly, double outter_bar_thickness){
        HE_Mesh mesh = new HE_Mesh(new HEC_FromPolygons(new WB_Polygon[]{poly}));
        HE_Mesh mesh1 = new HE_Mesh(mesh);
        HEM_Extrude extrude = new HEM_Extrude();
        extrude.setDistance(outter_bar_thickness);
        mesh.modify(extrude);
        mesh1.add(mesh);
        return  mesh1;

    };

    private void setUnitNumberForOutputComponents(int unitNumber) {
        for (int i = 0 ; i < this.allOutput_components.keySet().size(); i++){
            String materialName = this.allOutput_components.keySet().toArray()[i].toString();
            for (Output_Component oc:this.allOutput_components.get(materialName)
                 ) {
                oc.insertUnitNumber(unitNumber);
            }
        }
    }
}
