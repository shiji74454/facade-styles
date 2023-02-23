package basic;

import controlP5.*;
import processing.core.PApplet;

import java.util.Map;

public class ControlPanel {

    final int barHeight = 15;
    final int barGap = 10;
    final int xEdge = 10;
    final int labelSize = 9;
    final int testFieldCapWidth = 100;
    final int sliderWidth = 150;

    public enum Mode {
        Slider, Text,
    }

    Mode mode;
    PApplet app;
    ControlP5 cp5;
    Accordion accordion;
    Textarea textarea;

    public ControlPanel(PApplet app, Mode mode) {
        this.app = app;
        cp5 = new ControlP5(app);
        cp5.setFont(cp5.papplet.createFont("Consolas", 12));
        if (mode == Mode.Slider)
            this.mode = Mode.Slider;
        else
            this.mode = Mode.Text;
        accordion = cp5.addAccordion("ControlPanel");
        accordion.setCollapseMode(Accordion.MULTI);
       this.setWidth(280).setPos(app.width - 500f, 40, 0);
//        this.setWidth(280).setPos(app.width, 10, 0);
    }

    public ControlPanel setPos(float... pos) {
        accordion = accordion.setPosition(pos);
        return this;
    }

    public float[] getPos(float... pos) {

        return accordion.getPosition();
    }


    public ControlPanel setWidth(int width) {
        accordion = accordion.setWidth(width);
        return this;
    }

    public void updatePanel(BasicObject obj, String groupName) {
        cp5.remove(groupName);

        Group g = cp5.addGroup(groupName).setBackgroundColor(Material.RGBtoInt(100, 100, 100, 150)).setWidth(accordion.getWidth())
                .setBarHeight(20).setTitle(" " + groupName);
        g.getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER);
        Map<String, BasicObject.Para> paras = obj.getParas();
        int count = 0;
        for (String s : paras.keySet()) {
            BasicObject.Para para = paras.get(s);
            if (para.isNumber) {
                if (mode == Mode.Slider) {
                    cp5.addSlider(para.name)
                            .setRange((float) para.min, (float) para.max)
                            .setValue((float) para.getValue())
                            .setPosition(xEdge, xEdge + count * (barHeight + barGap))
                            .setSize(sliderWidth, barHeight)
                            .setCaptionLabel(" " + para.name)
                            .plugTo(para, "updateValueFloat")
                            .onChange(new CallbackListener() {
                                @Override
                                public void controlEvent(CallbackEvent theEvent) {
                                    updateText(obj);
                                }
                            })
                            .moveTo(g);
                    count++;
                } else {
                    cp5.addTextfield(para.name)
                            .setAutoClear(true).setValue("" + para.getValue())
                            .setPosition(xEdge + testFieldCapWidth, xEdge + count * (barHeight + barGap))
                            .setSize(testFieldCapWidth, 20)
                            .plugTo(para, "updateValueString").moveTo(g);
//                            .getCaptionLabel().align(ControlP5.LEFT_OUTSIDE,ControlP5.CENTER);
                }
            } else {
                cp5.addToggle(para.name)
                        .setValue(para.getBoolean())
                        .setMode(ControlP5.SWITCH)
                        .setCaptionLabel(" " + para.name)
                        .setPosition(xEdge, xEdge + count * (barHeight + barGap))
                        .setSize(sliderWidth, barHeight)
                        .plugTo(para, "updateBoolean").moveTo(g).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER);
                ;

                count++;
            }
        }


        Map<String, String> dataToDisplay = obj.getDataToDisplay();

        int extra = 0;
        for (String s : dataToDisplay.keySet()) {

            if ( dataToDisplay.get(s).toString().contains("\n")){extra++;}
        }
        int textFieldHeight = (dataToDisplay.keySet().size()+extra) * (barHeight + 2) + barHeight;
        textarea = cp5.addTextarea("data")
                .setPosition(xEdge, xEdge + count * (barHeight + barGap))
                .setSize(accordion.getWidth() - 2 * xEdge, textFieldHeight)
                .setFont(app.createFont("arial", 12))
                .setLineHeight(barHeight)
                .moveTo(g)
        ;


        updateText(obj);

        int backgroundHeight = (count + 1) * (barHeight + barGap) + textFieldHeight + xEdge;
        accordion.setBackgroundHeight(backgroundHeight);
        g.setBackgroundHeight(backgroundHeight);
        accordion = accordion.addItem(g);
        accordion.open();
    }

    private void updateText(BasicObject obj) {
        Map<String, String> dataToDisplay = obj.getDataToDisplay();
        String text = "";
        for (String s : dataToDisplay.keySet()) {
            text = text + s + " : " + dataToDisplay.get(s) + "\n";
        }
        textarea.setText(text);
    }
}
