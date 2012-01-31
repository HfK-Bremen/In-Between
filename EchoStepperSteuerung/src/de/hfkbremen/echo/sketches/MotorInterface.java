

package de.hfkbremen.echo.sketches;


import processing.core.PApplet;
import controlP5.*;
import oscP5.*;
import netP5.*;
import processing.opengl.*;

import static de.hfkbremen.echo.sketches.MotorInterfaceProperties.*;


public class MotorInterface
        extends PApplet {

    private Serial mSerial;

    final int numberOfLeinwaende = 17;

    Leinwand[] leinwaende;

    ControlP5 controlP5;

    int margin = 10;

    int breiteSingleView;

    int hoeheSingleView;

    int behaviour;

    OscP5 oscObjekt;

    NetAddress oscEmpfaenger;

    String aktName;

    ControlTimer showDauer;

    public void setup() {
        //size(screen.width, screen.height);//beim skalieren verhältnis beachten ca 16/10
        size(1280, 700);
        background(0);
        ellipseMode(CENTER);
        smooth();
        frameRate(25);
        //Serial.listPorts();
        showDauer = new ControlTimer();

        showDauer.setSpeedOfTime(1);
        behaviour = 1;
        breiteSingleView = (width - 9 * margin) / 8;
        hoeheSingleView = breiteSingleView + breiteSingleView / 3;
        /* mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
         * mSerial.write("#*@A\r");
         * mSerial.write("#*p2\r");
         * mSerial.write("#*o60\r"); */
        controlP5 = new ControlP5(this);
        leinwaende = new Leinwand[numberOfLeinwaende];
        for (int i = 0; i < numberOfLeinwaende; i++) {
            if (i <= 7) {
                leinwaende[ i] = new Leinwand(margin + i * (breiteSingleView + margin), margin, i);
                controlP5.addSlider("steps" + i, 0, 400, 0, margin + i * (breiteSingleView + margin), margin + hoeheSingleView + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }


            if (i > 7 && i < 13) {
                leinwaende[ i] = new Leinwand(margin + (i - 8) * (breiteSingleView + margin), margin + hoeheSingleView + margin + margin + margin, i);
                controlP5.addSlider("steps" + i, 0, 400, 0, margin + (i - 8) * (breiteSingleView + margin), (margin + hoeheSingleView + margin) * 2 + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }

            if (i > 12) {
                leinwaende[ i] = new Leinwand(margin + (i - 13) * (breiteSingleView + margin), (margin + hoeheSingleView + margin + margin) * 2 + margin, i);
                controlP5.addSlider("steps" + i, 0, 400, 0, margin + (i - 13) * (breiteSingleView + margin), (margin + hoeheSingleView + margin) * 3 + margin + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }



        }
        //oscObjekt = new OscP5(this,3000);
        //oscEmpfaenger = new NetAddress("David.local",3000);
        controlP5.addButton("Start", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 2 * margin,
                            leinwaende[ 12].positionY + hoeheSingleView + 3 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(25);
        controlP5.controller("Start").moveTo("global");

        controlP5.addButton("anotherButton", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 2 * margin,
                            leinwaende[ 12].positionY + hoeheSingleView + 6 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(26);
        controlP5.controller("anotherButton").moveTo("global");

        controlP5.addButton("Randomize", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 2 * margin,
                            leinwaende[ 12].positionY + hoeheSingleView + 9 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(27);
        controlP5.controller("Randomize").moveTo("Impro II");

        controlP5.tab("default").activateEvent(true);
        controlP5.tab("default").setLabel("Xenakis");
        controlP5.tab("default").setId(18);

        controlP5.tab("Impro I").activateEvent(true);
        controlP5.tab("Impro I").setId(19);

        controlP5.tab("Kolongala").activateEvent(true);
        controlP5.tab("Kolongala").setId(20);

        controlP5.tab("Impro II").activateEvent(true);
        controlP5.tab("Impro II").setId(21);

        controlP5.tab("Tilde").activateEvent(true);
        controlP5.tab("Tilde").setId(22);

        controlP5.tab("Ende").activateEvent(true);
        controlP5.tab("Ende").setId(23);

        controlP5.tab("Manuell").activateEvent(true);
        controlP5.tab("Manuell").setId(24);

    }

    public void draw() {
        
        final float EPSILON = 0.1f;
        float mValue = 123.451f;
        float mPoint = 120.000f;
        final float mDiff = abs(mValue - mPoint);
        
        if (mDiff < EPSILON) {
            mValue = mPoint;
        }
        
        
        background(0);
        for (int i = 0; i < numberOfLeinwaende; i++) {
            leinwaende[ i].display();
        }
        drawControl();
    }

    private void drawControl() {
        strokeWeight(1);
        /* rect(leinwaende[ 12 ].positionX+breiteSingleView+2*margin,
         * leinwaende[ 12 ].positionY+5*margin,
         * (3*breiteSingleView)/2-margin/2,
         * breiteSingleView); */
        pushMatrix();
        translate(leinwaende[ 12].positionX + breiteSingleView + margin,
                  leinwaende[ 12].positionY);
        stroke(50);
        strokeWeight(1);
        noFill();
        rect(0,
             0,
             3 * breiteSingleView + 2 * margin,
             2 * hoeheSingleView + 5 * margin);
        stroke(100);
        rect(margin,
             5 * margin,
             (3 * breiteSingleView) / 2 - margin / 2,
             breiteSingleView);
        rect((3 * breiteSingleView) / 2 + margin + margin / 2,
             5 * margin,
             (3 * breiteSingleView) / 2 - margin / 2,
             breiteSingleView);
        fill(255);
        text("Uhrzeit: " + nullStunde() + " : " + nullMinute() + " : " + nullSekunde(), margin, margin * 2);
        text("Showdauer: " + showDauer.toString(), margin, margin * 4);
        text("Akt: " + aktName, (3 * breiteSingleView) / 2 + margin + margin / 2, margin * 2);
        text("Startposition", margin, margin * 7 + breiteSingleView);
        text("Endposition", (3 * breiteSingleView) / 2 + margin + margin / 2, margin * 7 + breiteSingleView);

        popMatrix();
    }

    public class Leinwand {

        int steps;

        float currentAngle;

        boolean simulationsDrehung;

        int id;

        int positionX;

        int positionY;

        int startTime;

        Leinwand(int X, int Y, int i) {

            steps = 0;
            currentAngle = 0;
            positionX = X;
            positionY = Y;
            simulationsDrehung = false;
            id = i;




        }

        void display() {
            switch (behaviour) {
                case (1):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (2):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (3):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (4):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (5):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (6):
                    text("Behaviour: " + behaviour, width / 2, height / 2);
                    break;
                case (7):
                    break;
            }
            pushMatrix();
            translate(positionX, positionY);
            fill(255);
            text("Motor " + id, margin / 2, margin + margin / 2);
            text("Angle: " + (steps * 0.9f) + "°", margin / 2, (margin + margin / 2) * 2);
            strokeWeight(1);
            stroke(50);
            noFill();
            rect(0, 0, breiteSingleView, hoeheSingleView);
            strokeWeight(1);
            stroke(100);
            ellipse(breiteSingleView / 2, hoeheSingleView / 2 + hoeheSingleView / 8, breiteSingleView - margin, breiteSingleView - margin);
            pushMatrix();
            translate(breiteSingleView / 2, hoeheSingleView / 2 + hoeheSingleView / 8);
            pushMatrix();
            noStroke();
            fill(0, 54, 82);
            if (radians(currentAngle) < radians((steps * 0.9f))) {
                arc(0, 0, breiteSingleView / 2, breiteSingleView / 2, radians(currentAngle), radians((steps * 0.9f)));
            } else {
                arc(0, 0, breiteSingleView / 2, breiteSingleView / 2, radians((steps * 0.9f)), radians(currentAngle));
            }

            strokeWeight(2);
            stroke(0, 105, 140);
            actualAngle();
            rotate(radians(actualAngle()));
            triangle(breiteSingleView / 3, 0,
                     (breiteSingleView / 3 - margin / 3), margin / 3,
                     (breiteSingleView / 3 - margin / 3), -margin / 3);
            line(-breiteSingleView / 3, 0, breiteSingleView / 3, 0);
            popMatrix();
            rotate(radians(steps * 0.9f));
            strokeWeight(3);
            stroke(255);
            triangle(breiteSingleView / 3, 0,
                     (breiteSingleView / 3 - margin / 3), margin / 3,
                     (breiteSingleView / 3 - margin / 3), -margin / 3);
            line(-breiteSingleView / 3, 0, breiteSingleView / 3, 0);
            popMatrix();
            ellipse(breiteSingleView / 2, hoeheSingleView / 2 + hoeheSingleView / 8, margin / 2, margin / 2);
            popMatrix();
        }

        float actualAngle() {
            float angle = currentAngle;
            if (currentAngle < steps * 0.9 && simulationsDrehung) {
                angle = angle + 1.2f;
            } else if (currentAngle > steps * 0.9 && simulationsDrehung) {
                angle = angle - 1.2f;
            } else if (currentAngle == steps * 0.9) {
                simulationsDrehung = false;
            }
            currentAngle = angle;
            return angle;
        }
    }

    /* void mouseClicked(){
     * if (mouseX>leinwaende[ 12 ].positionX+breiteSingleView+2*margin &&
     * mouseY>leinwaende[ 12 ].positionY+5*margin &&
     * mouseX){
     *
     * }
     * } */
    void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController()) {
            if (theEvent.controller().id() == 25) {
                GO();
            } else if (theEvent.controller().id() == 27) {
                randomize();
            } else {
                leinwaende[theEvent.controller().id()].steps = (int)theEvent.controller().value();
                //mSerial.write("#"+(theEvent.controller().id()+1)+"s"+leinwaende[theEvent.controller().id()].steps+"\r");
            }
        } else if (theEvent.isTab()) {
            aktName = theEvent.tab().name();
            switch (theEvent.tab().id()) {
                case (CHAPTER_XENAKIS):
                    controlP5.controller("steps0").setValue(100);
                    controlP5.controller("steps1").setValue(0);
                    controlP5.controller("steps2").setValue(0);
                    controlP5.controller("steps3").setValue(0);
                    controlP5.controller("steps4").setValue(0);
                    controlP5.controller("steps5").setValue(0);
                    controlP5.controller("steps6").setValue(0);
                    controlP5.controller("steps7").setValue(0);
                    controlP5.controller("steps8").setValue(100);
                    controlP5.controller("steps9").setValue(0);
                    controlP5.controller("steps10").setValue(0);
                    controlP5.controller("steps11").setValue(0);
                    controlP5.controller("steps12").setValue(0);
                    controlP5.controller("steps13").setValue(100);
                    controlP5.controller("steps14").setValue(0);
                    controlP5.controller("steps15").setValue(0);
                    controlP5.controller("steps16").setValue(0);
                    break;
                case (19):
                    behaviour = 2;
                    break;
                case (20):
                    behaviour = 3;
                    break;
                case (21):
                    behaviour = 4;
                    break;
                case (22):
                    behaviour = 5;
                    break;
                case (23):
                    behaviour = 6;
                    break;
                case (24):
                    behaviour = 7;
                    break;
            }

        }


    }

    public void GO() {
        //mSerial.write("#*A\r");
        //OscMessage myMessage = new OscMessage("Motorposition");
        //myMessage.add(180.8); /* add an int to the osc message */
        //oscObjekt.send(myMessage, oscEmpfaenger);
        /* for (int i=0; i<=numberOfLeinwaende;i++){
         * leinwaende[i].startTime = millis();
         *
         *
         * } */
        for (int i = 0; i < numberOfLeinwaende; i++) {
            if (leinwaende[i].currentAngle != leinwaende[i].steps * 0.9) {
                leinwaende[i].simulationsDrehung = true;
            }
        }
        println("und Go!");
    }

    void anotherButton() {
        for (int i = 0; i < numberOfLeinwaende; i++) {

            leinwaende[i].simulationsDrehung = false;
        }
    }

    void randomize() {
        for (int i = 0; i < numberOfLeinwaende; i++) {

            controlP5.controller("steps" + i).setValue(random(400));
        }
    }

    int nullStunde() {
        String h = "0";

        if (hour() < 10) {
            h = "0";
            h += hour();
        } else {
            h = Integer.toString(hour());
        }
        int i = Integer.parseInt(h);
        return i;
    }

    int nullMinute() {
        String m = "0";

        if (minute() < 10) {
            m = "0";
            m += minute();
        } else {
            m = Integer.toString(minute());
        }
        int i = Integer.parseInt(m);
        return i;
    }

    int nullSekunde() {
        String s = "0";

        if (second() < 10) {
            s = "0";
            s += second();
        } else {
            s = Integer.toString(second());
        }
        int i = Integer.parseInt(s);
        return i;
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {"--present", MotorInterface.class.getName()});
    }
}
