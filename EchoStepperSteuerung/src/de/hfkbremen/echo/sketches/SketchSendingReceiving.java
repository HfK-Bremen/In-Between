

package de.hfkbremen.echo.sketches;


import processing.core.PApplet;
import controlP5.*;



public class SketchSendingReceiving
        extends PApplet {
    ControlP5 controlP5;
    private Serial mSerial;
    boolean drehModus;
    public void setup() {
        size(120,310);//beim skalieren verhältnis beachten ca 16/10
        background(0);
        //Serial.listPorts();
        controlP5 = new ControlP5(this);
        controlP5.addButton("Start",0,10,10,100,20).setId(1);
        controlP5.addButton("Stop",0,10,40,100,20).setId(2);
        controlP5.addButton("DrehModus/PosModus",0,10,280,100,20).setId(3);
        controlP5.addButton("Pos0",0,10,70,100,20).setId(4);
        controlP5.addButton("Pos100",0,10,100,100,20).setId(5);
        controlP5.addButton("Pos200",0,10,130,100,20).setId(6);
        controlP5.addButton("Pos300",0,10,160,100,20).setId(7);
        controlP5.addButton("Pos400",0,10,190,100,20).setId(8);
        controlP5.addButton("DrehZahl+",0,10,220,100,20).setId(9);
        controlP5.addButton("DrehZahl-",0,10,250,100,20).setId(10);
        
        mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
        delay(10);
       
       
        mSerial.write("#*p5\r");
        drehModus=true;
        
 
    }

    public void draw() {
    }

    public void keyPressed() {
        if (key == '1') {
            mSerial.write("#*s100\r");
        }
        if (key == '2') {
            mSerial.write("#*s200\r");
        }
        if (key == '3') {
            mSerial.write("#*s300\r");
        }
        if (key == '4') {
            mSerial.write("#*s400\r");
        }
        if (key == '5') {
            mSerial.write("#*A\r");
        }
        if (key == 'm') {
            mSerial.write("#*!2\r");
        }
        if (key == '6') {
            mSerial.write("#*S\r");
        }
        if (key == '7') {
            mSerial.write("#*A\r");
        }
        if (key == '8') {
            mSerial.write("#*A\r");
        }
        if (key == '9') {
            mSerial.write("#*A\r");
        }
        if (key == '0') {
            mSerial.write("#*A\r");
        }
    }
    
    void controlEvent(ControlEvent theEvent) {    
            switch (theEvent.controller().id()) {
                case(1):
                    mSerial.write("#*A\r");
                    break;
                case(2):
                    mSerial.write("#*S\r");
                    break;
                case(3):
                    if (drehModus){
                        mSerial.write("#*p2\r");
                        drehModus=false;
                    }else {
                        mSerial.write("#*p5\r");
                        drehModus=true;
                    }
                    break;
                case(4):
                    mSerial.write("#*s0\r");
                    break;
                case(5):
                    mSerial.write("#*s100\r");
                    break;
                case(6):
                    mSerial.write("#*s200\r");
                    break;
                case(7):
                    mSerial.write("#*s300\r");
                    break;
                case(8):
                    mSerial.write("#*s400\r");
                    break;
                case(9):
                    mSerial.write("#*+\r");
                    break;
                case(10):
                    mSerial.write("#*-\r");
                    break;
                    
            }
                  
            
        }
    
    public static void main(String[] args) {
        PApplet.main(new String[] {SketchSendingReceiving.class.getName()});
    }
}
