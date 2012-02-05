

package de.hfkbremen.echo.app;


import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlTimer;
import controlP5.Textarea;
import static de.hfkbremen.echo.sketches.MotorInterfaceProperties.*;
import processing.core.PApplet;
import processing.xml.XMLElement;


public class AppMotorInterface
        extends PApplet {

    private Serial mSerial;

    Leinwand[] leinwaende;

    ControlP5 controlP5;

    int margin;

    int breiteSingleView;

    int hoeheSingleView;

    String aktName;

    String satzName;

    int aktuellerAkt;

    int aktuellerSatz;

    int satzID;

    ControlTimer showDauer;

    XMLElement performance;

    AktWerte[] akte;

    Button stop;

    Textarea myTextarea;

    public void setup() {
        Serial.DEBUG = false;

        //size(screen.width, screen.height);//beim skalieren verhältnis beachten ca 16/10
        size(1280, 700);
        background(0);
        ellipseMode(CENTER);
        smooth();
        frameRate(25);
        showDauer = new ControlTimer();
        showDauer.setSpeedOfTime(1);
        margin = 10;
        breiteSingleView = (width - 9 * margin) / 8;
        hoeheSingleView = breiteSingleView + breiteSingleView / 3;
        //mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");


        xmlEinlesen();
        //saetzeSchreiben();
        leinwandUndSliderInit();
        triggerButtonUndTabsInit();
    }

    public void draw() {
        background(color(77, 77, 77));
        for (int i = 0; i < numberOfLeinwaende; i++) {
            leinwaende[ i].display();
        }
        controlWindow();

    }

    public class Leinwand {

        int steps;

        float currentAngle;

        boolean simulationsDrehung;

        int id;

        int positionX;

        int positionY;

        int akt;

        int satz;

        Leinwand(int X, int Y, int i) {
            steps = 0;
            currentAngle = 0;
            positionX = X;
            positionY = Y;
            simulationsDrehung = false;
            id = i;
            akt = 0;
            satz = 0;
        }

        void display() {
            pushMatrix();
            translate(positionX, positionY);
            fill(255);
            text("Motor " + (id + 1), margin / 2, margin + margin / 2);
            text("Angle: " + (steps * 0.9f) + "°", margin / 2, (margin + margin / 2) * 2);
            fill(0, 105, 140);
            text(actualAngle() + "°", margin / 2, (margin + margin / 2) * 3);
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
            triangle(-breiteSingleView / 3, 0,
                     -(breiteSingleView / 3 - margin / 3), margin / 3,
                     -(breiteSingleView / 3 - margin / 3), -margin / 3);
            line(-breiteSingleView / 3, 0, breiteSingleView / 3, 0);
            popMatrix();
            rotate(radians(steps * 0.9f));
            strokeWeight(3);
            stroke(255);
            triangle(-breiteSingleView / 3, 0,
                     -(breiteSingleView / 3 - margin / 3), margin / 3,
                     -(breiteSingleView / 3 - margin / 3), -margin / 3);
            line(-breiteSingleView / 3, 0, breiteSingleView / 3, 0);
            popMatrix();
            ellipse(breiteSingleView / 2, hoeheSingleView / 2 + hoeheSingleView / 8, margin / 2, margin / 2);
            popMatrix();
        }

        float actualAngle() {
            float winkelProFrame = (akte[akt].saetze[satz].leinwaende[id].speed / frameRate) * 0.9f;
            float differenz = (steps * 0.9f - currentAngle);
            float angle = currentAngle;
            if (currentAngle < steps * 0.9 && simulationsDrehung) {
                if (abs(differenz) < winkelProFrame) {
                    simulationsDrehung = false;
                    currentAngle = steps * 0.9f;
                } else {
                    angle = angle + winkelProFrame;
                }
            } else if (currentAngle > steps * 0.9 && simulationsDrehung) {
                if (abs(differenz) < winkelProFrame) {
                    simulationsDrehung = false;
                    currentAngle = steps * 0.9f;
                } else {
                    angle = angle - winkelProFrame;
                }
            }
            currentAngle = angle;
            return angle;
        }
    }

    void controlWindow() {
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
        text("Uhrzeit: " + nf(hour(), 2) + " : " + nf(minute(), 2) + " : " + nf(second(), 2), margin, margin * 2);
        text("Showdauer: " + showDauer.toString(), margin, margin * 4);
        text("Akt: " + aktName + " " + satzName, (3 * breiteSingleView) / 2 + margin + margin / 2, margin * 2);
        text("Startposition", margin, margin * 7 + breiteSingleView);
        text("Endposition", (3 * breiteSingleView) / 2 + margin + margin / 2, margin * 7 + breiteSingleView);
        popMatrix();
    }

    void leinwandUndSliderInit() {
        controlP5 = new ControlP5(this);
        leinwaende = new Leinwand[numberOfLeinwaende];
        for (int i = 0; i < numberOfLeinwaende; i++) {
            if (i <= 7) {
                leinwaende[ i] = new Leinwand(margin + i * (breiteSingleView + margin), margin, i);
                controlP5.addSlider("steps" + i, -motorSchrittBereich, motorSchrittBereich, 0, margin + i * (breiteSingleView + margin), margin + hoeheSingleView + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }
            if (i > 7 && i < 13) {
                leinwaende[ i] = new Leinwand(margin + (i - 8) * (breiteSingleView + margin), margin + hoeheSingleView + margin + margin + margin, i);
                controlP5.addSlider("steps" + i, -motorSchrittBereich, motorSchrittBereich, 0, margin + (i - 8) * (breiteSingleView + margin), (margin + hoeheSingleView + margin) * 2 + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }
            if (i > 12) {
                leinwaende[ i] = new Leinwand(margin + (i - 13) * (breiteSingleView + margin), (margin + hoeheSingleView + margin + margin) * 2 + margin, i);
                controlP5.addSlider("steps" + i, -motorSchrittBereich, motorSchrittBereich, 0, margin + (i - 13) * (breiteSingleView + margin), (margin + hoeheSingleView + margin) * 3 + margin + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo("Manuell");
            }
        }

    }

    void triggerButtonUndTabsInit() {
        controlP5.setColorForeground(255);
        controlP5.addButton("Start", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 2 * margin,
                            leinwaende[ 12].positionY + hoeheSingleView + 3 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(25);
        controlP5.controller("Start").moveTo("global");

        stop = controlP5.addButton("Notfall Stop", 0,
                                   leinwaende[ 12].positionX + breiteSingleView + 2 * margin,
                                   leinwaende[ 12].positionY + hoeheSingleView + 6 * margin,
                                   (3 * breiteSingleView) / 2 - margin / 2,
                                   margin * 2);
        stop.setId(26);
        stop.setColorBackground(color(159, 17, 44));
        stop.setColorActive(color(244, 11, 55));

        stop.moveTo("global");




        controlP5.addButton("Satz1 Laden", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 3 * margin + (3 * breiteSingleView) / 2 - margin / 2,
                            leinwaende[ 12].positionY + hoeheSingleView + 3 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(27);
        controlP5.controller("Satz1 Laden").moveTo("global");

        controlP5.addButton("Satz2 Laden", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 3 * margin + (3 * breiteSingleView) / 2 - margin / 2,
                            leinwaende[ 12].positionY + hoeheSingleView + 6 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(28);
        controlP5.controller("Satz2 Laden").moveTo("global");

        controlP5.addButton("Satz3 Laden", 0,
                            leinwaende[ 12].positionX + breiteSingleView + 3 * margin + (3 * breiteSingleView) / 2 - margin / 2,
                            leinwaende[ 12].positionY + hoeheSingleView + 9 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(29);
        controlP5.controller("Satz3 Laden").moveTo("global");





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

    void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController() && theEvent.controller().id() > 24) {
            switch (theEvent.controller().id()) {
                case (25):
                    GO();
                    break;
                case (26):
                    mSerial.write("#*S\r");
                    break;
                case (27):
                    if (akte[aktuellerAkt].saetze.length > 0) {
                        satzAufMotorAufrufen(aktuellerAkt, SATZ1);
                        leinwandSliderSetzen(aktuellerAkt, SATZ1);
                    }


                    break;
                case (28):
                    if (akte[aktuellerAkt].saetze.length > 0) {
                        satzAufMotorAufrufen(aktuellerAkt, SATZ2);
                        leinwandSliderSetzen(aktuellerAkt, SATZ2);
                    }
                    break;
                case (29):
                    if (akte[aktuellerAkt].saetze.length > 0) {
                        satzAufMotorAufrufen(aktuellerAkt, SATZ3);
                        leinwandSliderSetzen(aktuellerAkt, SATZ3);
                    }
                    break;

            }
        } else if (theEvent.isTab()) {

            switch (theEvent.tab().id()) {
                case (18):
                    saetzeSchreiben();
                    aktuellerAkt = XENAKIS;
                    break;
                case (19):

                    aktuellerAkt = IMPRO1;
                    break;
                case (20):

                    aktuellerAkt = KOLONGALA;
                    break;
                case (21):

                    aktuellerAkt = IMPRO2;
                    break;
                case (22):

                    aktuellerAkt = TILDE;
                    break;
                case (23):

                    aktuellerAkt = ENDE;
                    break;
                case (24):
                    //manuell
                    break;
            }

        } else if (theEvent.isController()) {
            leinwaende[theEvent.controller().id()].steps = (int)theEvent.controller().value();
        }
    }

    void saetzeSchreiben() {
        for (int i = 0; i < akte.length; i++) {
            for (int j = 0; j < akte[i].saetze.length; j++) {
                for (int x = 0; x < akte[i].saetze[j].leinwaende.length; x++) {
                    final int mID = x + 1;
                    mSerial.write("#" + mID + "p" + akte[i].saetze[j].drehmodus + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "t" + akte[i].saetze[j].richtungswechsel + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "W" + akte[i].saetze[j].wiederholungen + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "b" + akte[i].saetze[j].startrampe + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "B" + akte[i].saetze[j].bremsrampe + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "P" + akte[i].saetze[j].pause + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "o" + akte[i].saetze[j].leinwaende[x].speed + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "s" + akte[i].saetze[j].leinwaende[x].position + "\r");
                    delay(20);
                    mSerial.write("#" + mID + "d" + akte[i].saetze[j].leinwaende[x].drehrichtung + "\r");
                    delay(20);
                    mSerial.write("#" + mID + ">" + akte[i].saetze[j].satzID + "\r");
                }
            }
        }
    }

    void xmlEinlesen() {
        performance = new XMLElement(this, "inbetween.xml");
        int aktnum = performance.getChildCount();
        akte = new AktWerte[aktnum];
        for (int i = 0; i < aktnum; i++) {
            XMLElement aktliste = performance.getChild(i);
            int satznum = aktliste.getChildCount();
            akte[i] = new AktWerte(satznum);
            akte[i].aktname = aktliste.getString("name");
            for (int j = 0; j < satznum; j++) {
                XMLElement satzliste = aktliste.getChild(j);
                int leinwandnum = satzliste.getChildCount();
                println("### leinwandnum " + leinwandnum);
                akte[i].saetze[j] = new SatzWerte(leinwandnum);
                akte[i].saetze[j].satzID = satzliste.getInt("satzID_y");
                akte[i].saetze[j].drehmodus = satzliste.getInt("drehmodus_p");
                akte[i].saetze[j].richtungswechsel = satzliste.getInt("richtungswechsel_t");
                akte[i].saetze[j].wiederholungen = satzliste.getInt("wiederholungen_W");
                akte[i].saetze[j].startrampe = satzliste.getInt("startrampe_b");
                akte[i].saetze[j].bremsrampe = satzliste.getInt("bremsrampe_B");
                akte[i].saetze[j].pause = satzliste.getInt("pause_P");
                akte[i].saetze[j].name = satzliste.getString("name");
                for (int x = 0; x < leinwandnum; x++) {
                    XMLElement leinwandliste = satzliste.getChild(x);
                    akte[i].saetze[j].leinwaende[x] = new LeinwandWerte();
                    akte[i].saetze[j].leinwaende[x].speed = leinwandliste.getInt("speed_o");
                    akte[i].saetze[j].leinwaende[x].position = leinwandliste.getInt("position_s");
                    akte[i].saetze[j].leinwaende[x].drehrichtung = leinwandliste.getInt("drehrichtung_d");
                }
            }
        }
    }

    void leinwandSliderSetzen(int akt, int satz) {
        for (int i = 0; i < numberOfLeinwaende; i++) {
            controlP5.controller("steps" + i).setValue(akte[akt].saetze[satz].leinwaende[i].position);
            leinwaende[i].akt = akt;
            leinwaende[i].satz = satz;
            println("Winkel Setzen auf:" + akte[akt].saetze[satz].leinwaende[i].position);

        }
        aktName = akte[akt].aktname;
        satzName = akte[akt].saetze[satz].name;


    }

    void satzAufMotorAufrufen(int akt, int satz) {
        mSerial.write("#*y" + akte[akt].saetze[satz].satzID + "\r");
        println("lade Satz:" + akte[akt].saetze[satz].satzID);



    }

    public void GO() {

        //mSerial.write("#*A\r");
        for (int i = 0; i < numberOfLeinwaende; i++) {
            if (leinwaende[i].currentAngle != leinwaende[i].steps * 0.9) {
                leinwaende[i].simulationsDrehung = true;


            }
        }
    }

    class AktWerte {

        SatzWerte[] saetze;

        String aktname;

        int satzarraysize;

        AktWerte(int arraysize) {
            satzarraysize = arraysize;
            aktname = "name";
            saetze = new SatzWerte[satzarraysize];
        }
    }

    class SatzWerte {

        LeinwandWerte[] leinwaende;

        int leinwandarraysize;

        int satzID;

        int drehmodus;

        int richtungswechsel;

        int wiederholungen;

        int startrampe;

        int bremsrampe;

        int pause;

        String name;

        SatzWerte(int arraysize) {
            satzID = 0;
            drehmodus = 2;
            richtungswechsel = 0;
            wiederholungen = 1;
            startrampe = 4000;
            bremsrampe = 4000;
            pause = 0;
            name = "satz";
            leinwandarraysize = arraysize;
            leinwaende = new LeinwandWerte[leinwandarraysize];
            println("### leinwandarraysize " + leinwandarraysize);
        }
    }

    class LeinwandWerte {

        int position;

        int speed;

        int drehrichtung;

        LeinwandWerte() {
            position = 0;
            speed = 5;
            drehrichtung = 0;
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {
                    "--present",
                    AppMotorInterface.class.getName()});
    }
}
