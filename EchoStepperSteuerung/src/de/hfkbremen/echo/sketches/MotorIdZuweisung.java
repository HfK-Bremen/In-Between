

package de.hfkbremen.echo.sketches;


import processing.core.PApplet;

import controlP5.*;



public class MotorIdZuweisung
        extends PApplet {

    private Serial mSerial;
    ControlP5 controlP5;

    String textValue = "";
    Textfield myTextfield;
    controlP5.Button b;
    int i;
    public void setup() {
        Serial.listPorts();
        size(300,300);
        mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
        mSerial.write("#*@A\r");
        controlP5 = new ControlP5(this);
        myTextfield = controlP5.addTextfield("Motoradresse",(width/2)-100,(height/2)-10,200,20);
        myTextfield.setFocus(true);
        controlP5.addButton("Test",0,(width/2)-100,(height/2)+55,200,20);
        controlP5.addButton("submit",0,(width/2)-100,(height/2)+30,200,20);
        
 
    }

    public void draw() {
        background(0);
        
    }
    
    public void controlEvent(ControlEvent theEvent) {
        String motorID = myTextfield.stringValue();
        i = Integer.parseInt(motorID);
        println(i);
        mSerial.write("#*m"+i+"\r");
        delay(10);
        mSerial.write("#*Zm\r");
    }
    public void Test() {
        mSerial.write("#"+i+"s200\r");
        delay(10);
        mSerial.write("#"+i+"A\r");
    }
    
    public void submit(int theValue) {
        myTextfield.submit();
    }


    public static void main(String[] args) {
        PApplet.main(new String[] {MotorIdZuweisung.class.getName()});
    }
}
