package basic;

import processing.core.PApplet;
import wblut.processing.WB_Render3D;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicObject {
    List<StyledGeometry> geometries;

    Map<String, String> dataToDisplay;
    Map<String, Para> paras;


    public BasicObject() {
        geometries = new ArrayList<>();
        dataToDisplay = new HashMap<>();
        paras = new HashMap<>();

    }

    protected abstract void initPara();

    protected abstract void initData();

    /**
     * main body of calculation: generate data and styled objects according to current parameters
     */
    protected abstract void calculate();

    protected void update() {
        /** step 1. update paras */
        for (String s : this.getParas().keySet()) {
            try {
                Field f = this.getClass().getDeclaredField(s);
                f.setAccessible(true);
                if (getPara(s).isNumber)
                    f.set(this, getPara(s).getValue());
                else
                    f.set(this, getPara(s).getBoolean());

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        geometries = new ArrayList<>();

        /** step 2. generate data and styled objects according to current parameters */
        calculate();

        /** step 3. update data */
        for (String s : this.dataToDisplay.keySet()) {
            try {
                Field f = this.getClass().getDeclaredField(s);
                f.setAccessible(true);
                putData(s, f.get(this).toString());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(WB_Render3D render3D) {
        for (StyledGeometry geometry : geometries) {
            geometry.draw(render3D);
        }
    }

    public void draw(WB_Render3D render3D, PApplet app) {

        for (StyledGeometry geometry : geometries) {
            if(geometry.material.getTexture() == " "){
                geometry.draw(render3D);
               // System.out.println("draw others*********************");
            }else { ((StyledMesh)geometry).draw(render3D,app);

                //System.out.println("draw texture *********************");
            }

        }
    }

    public void putData(String name, String value) {
        dataToDisplay.put(name, value);
    }


    public void  reverseBooleanPara(String name){
        paras.get(name).updateBoolean(!paras.get(name).getBoolean());
    }

    public void modifyParaBounds(String name, double min,double max){
        System.out.println("----------------------------------------------------------------");
        System.out.println("name " + name );
        System.out.println("min " + min );
        System.out.println("max " + max );

        System.out.println("----------------------------------------------------------------");
        paras.get(name).updateMax(max);
        paras.get(name).updateMin(min);
    }


    public Para putPara(double value, double min, double max, String name) {
        Para para = new Para(value, min, max, name, this);
        paras.put(name, para);
        return para;
    }

    public Para putPara(boolean bool, String name) {
        Para para = new Para(bool, name, this);
        paras.put(name, para);
        return para;
    }

    protected void addGeometry(StyledGeometry geometry) {
        this.geometries.add(geometry);
    }

    public Para getPara(String name) {
        return paras.get(name);
    }

    public Map<String, String> getDataToDisplay() {
        return dataToDisplay;
    }

    public Map<String, Para> getParas() {
        return paras;
    }

    public List<StyledGeometry> getGeometries() {
        return geometries;
    }

    public class Para {
        public double min, max, value;
        public String name;
        public boolean isNumber = true;
        public boolean bool = false;
        BasicObject parent;

        public Para(double value, double min, double max, String name, BasicObject parent) {
            this.min = min;
            this.max = max;
            this.value = value;
            this.name = name;
            this.parent = parent;
        }

        public Para(boolean bool, String name, BasicObject parent) {
            this.isNumber = false;
            this.bool = bool;
            this.name = name;
            this.parent = parent;
        }

        public void updateValueFloat(float value) {
            this.value = value;
            parent.update();
        }

        public void updateValueString(String value) {
            this.value = Double.valueOf(value);
            parent.update();
        }

        public void updateBoolean(boolean bool) {
            this.bool = bool;
            parent.update();
        }

        public void updateMax(double max){
            this.max = max;
            checkMinMax();

        }

        public void updateMin(double min){
            this.min = min;
            checkMinMax();

        }


        public double getValue() {
            return value;
        }

        public boolean getBoolean() {
            return bool;
        }

        private void checkMinMax() {
            if (this.value < min)
                this.value = min;
            if (this.value > max)
                this.value = max;
        }
    }
}
