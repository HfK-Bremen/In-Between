

package de.hfkbremen.echo.sketches;


import processing.core.PApplet;


public class SketchSendingReceiving
        extends PApplet {

    private Serial mSerial;

    public void setup() {
        Serial.listPorts();
        mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
//        mSerial.write("#*@A\r\n");
        for (int i = 0; i < 255; i++) {
            mSerial.write("#" + i + "@A\r");
        }
    }

    public void draw() {
    }

    public void keyPressed() {
        if (key == '1') {
            mSerial.write("#*@A\r\n");
        }
        if (key == '2') {
            mSerial.write("#*@A\n\r");
        }
        if (key == '3') {
            mSerial.write("#*@A\n");
        }
        if (key == ' ') {
            mSerial.write("#*@A\r");
        }
        if (key == '4') {
            mSerial.write("#*A\r");
        }
        if (key == '5') {
            mSerial.write("#*Z|\r");
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {SketchSendingReceiving.class.getName()});
    }
}
