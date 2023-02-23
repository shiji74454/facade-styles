package basic;

import processing.core.PApplet;
import processing.core.PConstants;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform3D;
import wblut.processing.WB_Render3D;

import java.util.*;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:LayoutGenerator
 * @date : 22:27 2022-11-06
 */
public class LayoutGenerator {
    double scaleFactor;
    double layoutWidth;
    double layoutHeight;
    Map<String,ArrayList<Output_Component>> output_components_map_forLayout = new HashMap<>();
    StyledPolyLine layoutStyledPolyline = new StyledPolyLine(0xFF4682B4,2);;
    double componentsGap;//set gap
    double layoutGap;
    int layoutNum = 1;
    ArrayList<Output_Component> output_components;
    Map<String,WB_Polygon> material_layout_map = new HashMap();
    Map<WB_Point,String> pos_materialName = new HashMap();

    public LayoutGenerator(double layoutWidth,double layoutHeight,double scaleFactor) {
        this.scaleFactor =  scaleFactor;
        this.layoutWidth = layoutWidth * scaleFactor;
        this.layoutGap = this.layoutWidth*0.04;
        this.componentsGap = this.layoutWidth*0.01;
        this.layoutHeight = layoutHeight * scaleFactor;
    }

    private void setLayoutPolygon ( String material,int layoutNum,WB_Point transPoint) {
        WB_Transform3D trans = new WB_Transform3D();
        trans.addTranslate(transPoint);

        for(int i = 0; i < layoutNum; i++){
            WB_Polygon polygon = new WB_Polygon(
                    new WB_Point(layoutHeight,(layoutWidth + layoutGap) *i,0),
                    new WB_Point(layoutHeight,(layoutWidth +  layoutGap) *i + layoutWidth,0),
                    new WB_Point(0,(layoutWidth + layoutGap) *i + layoutWidth,0 ),
                    new WB_Point(0,(layoutWidth + layoutGap) *i,0),
                    new WB_Point(layoutHeight,(layoutWidth +  layoutGap) *i,0)
            );

            this.layoutStyledPolyline.add(polygon.apply(trans));
            this.material_layout_map.put(material,polygon);
        }





    }

    public void setOutPutComponentsLayout(ArrayList<Map<String,ArrayList<Output_Component>>> allOutput_components) {
        for(int i = 0 ; i <  allOutput_components.size() ;i++){
              output_components_map_forLayout = Output_Component.merge(output_components_map_forLayout,allOutput_components.get(i));
        }
        this.output_components = new ArrayList<>();
        System.out.println(" void setOutPutComponentsLayout(ArrayList<Map<String,ArrayList<Output_Component>>> allOutput_components)  -----------------------  is invoked. ");
        int key_num =  this.output_components_map_forLayout.keySet().size();
        System.out.println(" key_num = " +  key_num + " -------------------------- ");
        for (int i = 0; i < key_num; i++){
            WB_Point layout_byMaterial_trans = new WB_Point( i*(this.layoutHeight +layoutGap),0,0);
            String name = output_components_map_forLayout.keySet().toArray()[i].toString();
            System.out.println("String key name = " + name + " -------------------------- ");
            setOutComponentsPos("Simple",name ,output_components_map_forLayout.get(name),layout_byMaterial_trans);
        }
    }


    private void setOutComponentsPos(String layoutMode,String material, ArrayList<Output_Component> output_components,WB_Point trans_byMaterial) {
        System.out.println("setOutComponentsPos " +  " -------------------------- is invoked");
        if (layoutMode == "Simple") {
            int rows_num = 1;
            this.layoutNum = 1;
            WB_Point materialNamePos = trans_byMaterial;
            pos_materialName.put(materialNamePos,material);
            double temp_y = 0;
            double temp_x = 0;
            WB_Point pos_temp = new WB_Point(0, 0, 0);//定位到每一张图版
            for (int i = 0; i < output_components.size(); i++) {
                Output_Component oc = output_components.get(i);
                //如果不换行
                if (temp_y + oc.getSize()[0] <  layoutWidth) {
                    pos_temp.set(temp_x,temp_y+(layoutNum -1)  * (layoutWidth + layoutGap),0);
                    output_components.get(i).setPos(pos_temp.add(trans_byMaterial));
                    temp_y += oc.getSize()[0] +componentsGap;
                } else {
                    //如果换行
                    rows_num ++;
                    temp_x = (rows_num-1)*( oc.getSize()[1]+ componentsGap);
                    if(temp_x + oc.getSize()[1] <= layoutHeight){
                        temp_y = 0;
                        pos_temp.set(temp_x,temp_y+(layoutNum -1)  * (layoutWidth + layoutGap));
                        output_components.get(i).setPos(pos_temp.add(trans_byMaterial));
                        temp_y += oc.getSize()[0] + componentsGap;
                    }else{
                        //换图版
                        this.layoutNum++;
                        pos_temp.set(0,(layoutNum -1)  * (layoutWidth + layoutGap),0);
                        temp_x = 0;
                        temp_y = 0;
                        rows_num = 1;
                        output_components.get(i).setPos(pos_temp.add(trans_byMaterial));
                        temp_y += oc.getSize()[0] +componentsGap;
                        materialNamePos = pos_temp.add(trans_byMaterial);
                        pos_materialName.put(materialNamePos,material);

                    }
                }
                this.output_components.add(oc);
            }
            setLayoutPolygon(material,layoutNum,trans_byMaterial);
        }
    }

    public void draw(WB_Render3D wb_render,PApplet app){
        this.layoutStyledPolyline.draw(wb_render);

        for (int i = 0 ; i < pos_materialName.keySet().size();i++){
            app.pushMatrix();
            app.pushStyle();

            app.textSize(600);
            app.textAlign(PConstants.LEFT,PConstants.BOTTOM);
            WB_Point pos = (WB_Point) pos_materialName.keySet().toArray()[i];
            app.translate(pos.xf(),pos.yf(),pos.zf());
            app.rotateX(PApplet.PI);
            app.rotateZ(-PApplet.PI/2);
            String name =pos_materialName.get(pos);
            app.text( name,0,0,0);


            app.popStyle();
            app.popMatrix();
        }


        for (Output_Component output_components:this.output_components
             ) {
            output_components.draw(wb_render, app);
        }
    }
}
