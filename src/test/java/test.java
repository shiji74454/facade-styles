import processing.core.PApplet;
import java.util.Random;

/**
 * @author : Shi Ji
 * @project:facade-styles
 * @file:test
 * @date : 21:38 2022-11-04
 */
public class test extends PApplet {

    int randomSeed =1000000;
    public static void main(String[] args) {
        PApplet.main(test.class.getName());
    }
    public void setup() {
        Random random = new Random();
        random.setSeed((int) randomSeed);
        System.out.println("(int)randomSeed = " + (int) randomSeed);
        for(int i = 0 ; i < 10; i++){
            int ran = random.nextInt(4);
            System.out.print( ran);
        }


    }



}
