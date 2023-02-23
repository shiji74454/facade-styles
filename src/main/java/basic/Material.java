package basic;

import processing.core.PApplet;
import processing.core.PImage;

import java.awt.*;

public enum Material{
    White(0xFFFFFFFF),
    Black(0xFF000000),
    LightGray(0xFFc3c3c3),
    MIDGray(0xFF969696),
    DarkGray(0xFF545454),
    Wheat1(0xFFFFE7BA),

    SeaGreen(0xFF2E8B57),
    Glass(0x507c99b3),


    Concrete("E:\\JavaProjects\\facade-styles\\facade-styles\\src\\main\\resources\\Concrete_H_Gloss.jpg"),
    Foam("E:\\JavaProjects\\facade-styles\\facade-styles\\src\\main\\resources\\Foam_B_Diff.jpg"),
    Brick("E:\\JavaProjects\\facade-styles\\facade-styles\\src\\main\\resources\\Bricks_A_Bump_01.jpg"),

    TestMaterial("E:\\JavaProjects\\facade-styles\\facade-styles\\src\\main\\resources\\texture_test.jpg")    ;




    private  String textureName  = " ";




    private int color;
    private Material(int color) {
        this.color = color;
    }

    private Material(String texture_name) {
       this. textureName = texture_name;
    }
    public int getColor() {
        return color;
    }

    public String getTexture(){return textureName;};

    public static int RGBtoInt(int... rgba) {
        switch (rgba.length) {
            case 1:
                return new Color(rgba[0]).getRGB();
            case 2:
                return new Color(rgba[0], rgba[0], rgba[0], rgba[1]).getRGB();
            case 3:
                return new Color(rgba[0], rgba[1], rgba[2]).getRGB();
            case 4:
                return new Color(rgba[0], rgba[1], rgba[2], rgba[3]).getRGB();
            default:
                return Color.RED.getRGB();
        }
    }
}

