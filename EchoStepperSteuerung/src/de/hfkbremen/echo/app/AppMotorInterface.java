

package de.hfkbremen.echo.app;


import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.ControlTimer;
import static de.hfkbremen.echo.app.Properties.*;
import processing.core.PApplet;
import processing.xml.XMLElement;


public class AppMotorInterface
        extends PApplet {

    private Serial mSerial;

    private Leinwand[] leinwaende;

    private ControlP5 controlP5;

    private int margin;

    private int breiteSingleView;

    private int hoeheSingleView;

    private String aktName;

    private String satzName;

    private int aktuellerAkt;

    private ControlTimer showDauer;

    private XMLElement performance;

    private AktWerte[] akte;

    private Button stop;

    private static final boolean RUN_WITH_SERIAL = false;

    private static final String TAB_NAME_MANUELL = "Manuell";

    private static final String XML_SCENE_DATA = "inbetween.xml";

    private Button[] mSatzButtons;

    private static final int MAX_NUMBER_OF_SATZ_BUTTONS = 3;

    public void setup() {
        Serial.DEBUG = false;

        size(screen.width - 100, screen.height - 100);// beim skalieren verhältnis beachten ca 16/10
        background(0);
        ellipseMode(CENTER);
        smooth();
        frameRate(25);
        showDauer = new ControlTimer();
        showDauer.setSpeedOfTime(1);
        margin = 10;
        breiteSingleView = (width - 9 * margin) / 8;
        hoeheSingleView = breiteSingleView + breiteSingleView / 3;

        if (RUN_WITH_SERIAL) {
            mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
        }

        xmlEinlesen();
        if (RUN_WITH_SERIAL) {
            saetzeSchreiben();
        }

        controlP5 = new ControlP5(this);
        mSatzButtons = new Button[MAX_NUMBER_OF_SATZ_BUTTONS];
        leinwandUndSliderInit();
        initTriggersButtonsAndTabs();

        /* intialize */
        aktuellerAkt = XENAKIS;
        handleTabGUI();
    }

    public void draw() {
        background(color(77, 77, 77));
        for (int i = 0; i < NUMBER_OF_LEINWAENDE; i++) {
            leinwaende[ i].display();
        }
        controlWindow();
    }

    public class Leinwand {

        public int steps;

        public boolean simulationsDrehung;

        public int positionX;

        public int positionY;

        public int akt;

        public int satz;

        private int mID;

        private float mCurrentAngle;

        public Leinwand(int X, int Y, int i) {
            steps = 0;
            mCurrentAngle = 0;
            positionX = X;
            positionY = Y;
            simulationsDrehung = false;
            mID = i;
            akt = 0;
            satz = 0;
        }

        public void display() {
            pushMatrix();
            translate(positionX, positionY);
            fill(255);
            text("Motor " + (mID + 1), margin / 2, margin + margin / 2);
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
            if (radians(mCurrentAngle) < radians((steps * 0.9f))) {
                arc(0, 0, breiteSingleView / 2, breiteSingleView / 2, radians(mCurrentAngle), radians((steps * 0.9f)));
            } else {
                arc(0, 0, breiteSingleView / 2, breiteSingleView / 2, radians((steps * 0.9f)), radians(mCurrentAngle));
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

        public float actualAngle() {
            float winkelProFrame = (akte[akt].saetze[satz].leinwaende[mID].speed / frameRate) * 0.9f;
            float differenz = (steps * 0.9f - mCurrentAngle);
            float angle = mCurrentAngle;
            if (mCurrentAngle < steps * 0.9 && simulationsDrehung) {
                if (abs(differenz) < winkelProFrame) {
                    simulationsDrehung = false;
                    mCurrentAngle = steps * 0.9f;
                } else {
                    angle = angle + winkelProFrame;
                }
            } else if (mCurrentAngle > steps * 0.9 && simulationsDrehung) {
                if (abs(differenz) < winkelProFrame) {
                    simulationsDrehung = false;
                    mCurrentAngle = steps * 0.9f;
                } else {
                    angle = angle - winkelProFrame;
                }
            }
            mCurrentAngle = angle;
            return angle;
        }
    }

    private void controlWindow() {
        pushMatrix();
        translate(leinwaende[ LETZTE_LEINWAND].positionX + breiteSingleView + margin,
                  leinwaende[ LETZTE_LEINWAND].positionY);
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

    private void leinwandUndSliderInit() {
        leinwaende = new Leinwand[NUMBER_OF_LEINWAENDE];
        for (int i = 0; i < NUMBER_OF_LEINWAENDE; i++) {
            if (i <= 7) {
                leinwaende[ i] = new Leinwand(margin + i * (breiteSingleView + margin), margin, i);
                controlP5.addSlider("steps" + i,
                                    -motorSchrittBereich,
                                    motorSchrittBereich,
                                    0,
                                    margin + i * (breiteSingleView + margin),
                                    margin + hoeheSingleView + margin,
                                    hoeheSingleView / 2 + margin,
                                    margin).setId(i);
                controlP5.controller("steps" + i).moveTo(TAB_NAME_MANUELL);
            }
            if (i > 7 && i < 13) {
                leinwaende[i] = new Leinwand(margin + (i - 8) * (breiteSingleView + margin), margin + hoeheSingleView + margin + margin + margin, i);
                controlP5.addSlider("steps" + i, -motorSchrittBereich, motorSchrittBereich, 0, margin + (i - 8) * (breiteSingleView + margin), (margin + hoeheSingleView + margin) * 2 + margin, hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo(TAB_NAME_MANUELL);
            }
            if (i > LETZTE_LEINWAND) {
                leinwaende[i] = new Leinwand(margin + (i - 13) * (breiteSingleView + margin), (margin + hoeheSingleView + margin + margin) * 2 + margin, i);
                controlP5.addSlider("steps" + i,
                                    -motorSchrittBereich,
                                    motorSchrittBereich,
                                    0,
                                    margin + (i - 13) * (breiteSingleView + margin),
                                    (margin + hoeheSingleView + margin) * 3 + margin + margin,
                                    hoeheSingleView / 2 + margin, margin).setId(i);
                controlP5.controller("steps" + i).moveTo(TAB_NAME_MANUELL);
            }
        }
    }

    private void initTriggersButtonsAndTabs() {
        controlP5.addButton("Start",
                            0,
                            leinwaende[LETZTE_LEINWAND].positionX + breiteSingleView + 2 * margin,
                            leinwaende[LETZTE_LEINWAND].positionY + hoeheSingleView + 3 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(START_BUTTON);
        controlP5.controller("Start").moveTo("global");

        stop = controlP5.addButton("Notfall Stop", 0,
                                   leinwaende[LETZTE_LEINWAND].positionX + breiteSingleView + 2 * margin,
                                   leinwaende[LETZTE_LEINWAND].positionY + hoeheSingleView + 6 * margin,
                                   (3 * breiteSingleView) / 2 - margin / 2,
                                   margin * 2);
        stop.setId(STOP_BUTTON);
        stop.setColorBackground(color(159, 17, 44));
        stop.setColorActive(color(244, 11, 55));
        stop.moveTo("global");

        for (int i = 0; i < MAX_NUMBER_OF_SATZ_BUTTONS; i++) {
            mSatzButtons[i] = controlP5.addButton("Satz" + (i + 1) + " Laden", 0,
                                                  leinwaende[LETZTE_LEINWAND].positionX + breiteSingleView + 3 * margin + (3 * breiteSingleView) / 2 - margin / 2,
                                                  leinwaende[LETZTE_LEINWAND].positionY + hoeheSingleView + (3 * (i + 1)) * margin,
                                                  (3 * breiteSingleView) / 2 - margin / 2,
                                                  margin * 2);
            mSatzButtons[i].setId(LADEN_SATZ_1 + i);
            mSatzButtons[i].moveTo("global");
        }

        /* saetze laden */
        controlP5.addButton("Saetze Schreiben", 0,
                            leinwaende[LETZTE_LEINWAND].positionX + breiteSingleView + 2 * margin,
                            leinwaende[LETZTE_LEINWAND].positionY + hoeheSingleView + 9 * margin,
                            (3 * breiteSingleView) / 2 - margin / 2,
                            margin * 2).setId(SAETZE_SCHREIBEN_BUTTON);
        controlP5.controller("Saetze Schreiben").moveTo("global");

        /* tab */
        controlP5.tab("default").activateEvent(true);
        controlP5.tab("default").setLabel("Xenakis");
        controlP5.tab("default").setId(TAB_XENAKIS);

        controlP5.tab("Impro I").activateEvent(true);
        controlP5.tab("Impro I").setId(TAB_IMPRO1);

        controlP5.tab("Kolongala").activateEvent(true);
        controlP5.tab("Kolongala").setId(TAB_KOLONGALA);

        controlP5.tab("Impro II").activateEvent(true);
        controlP5.tab("Impro II").setId(TAB_IMPRO2);

        controlP5.tab("Tilde").activateEvent(true);
        controlP5.tab("Tilde").setId(TAB_TILDE);

        controlP5.tab("Ende").activateEvent(true);
        controlP5.tab("Ende").setId(TAB_ENDE);

        controlP5.tab(TAB_NAME_MANUELL).activateEvent(true);
        controlP5.tab(TAB_NAME_MANUELL).setId(TAB_MANUELL);
    }

    private void ladeSatz(final int pAkt, final int pSatz) {
        if (pAkt >= 0 && pAkt < akte.length) {
            if (pSatz >= 0 && pSatz < akte[pAkt].saetze.length) {
                satzAufMotorAufrufen(pAkt, pSatz);
                leinwandSliderSetzen(pAkt, pSatz);
                satzName = akte[pAkt].saetze[pSatz].name;
            }
        }
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.isController() && theEvent.controller().id() == SAETZE_SCHREIBEN_BUTTON) {
            saetzeSchreiben();
        } else if (theEvent.isController() && theEvent.controller().id() > LAST_TAB_ID) {
            switch (theEvent.controller().id()) {
                case (START_BUTTON):
                    GO();
                    break;
                case (STOP_BUTTON):
                    if (RUN_WITH_SERIAL) {
                        mSerial.write("#*S\r");
                    }
                    break;
                default:
                    ladeSatz(aktuellerAkt, theEvent.controller().id() - LADEN_SATZ_1);
            }
        } else if (theEvent.isTab()) {
            switch (theEvent.tab().id()) {
                case (TAB_XENAKIS):
                    aktuellerAkt = XENAKIS;
                    break;
                case (TAB_IMPRO1):
                    aktuellerAkt = IMPRO1;
                    break;
                case (TAB_KOLONGALA):
                    aktuellerAkt = KOLONGALA;
                    break;
                case (TAB_IMPRO2):
                    aktuellerAkt = IMPRO2;
                    break;
                case (TAB_TILDE):
                    aktuellerAkt = TILDE;
                    break;
                case (TAB_ENDE):
                    aktuellerAkt = ENDE;
                    break;
                case (TAB_MANUELL):
                    aktuellerAkt = MANUELL;
                    break;
            }
            handleTabGUI();
        } else if (theEvent.isController()) {
            leinwaende[theEvent.controller().id()].steps = (int)theEvent.controller().value();
        }
    }

    private void handleTabGUI() {
        /* handle manuel tab */
        if (aktuellerAkt == MANUELL) {
            aktName = TAB_NAME_MANUELL;
            satzName = "";
            for (int i = 0; i < MAX_NUMBER_OF_SATZ_BUTTONS; i++) {
                mSatzButtons[i].hide();
            }
            return;
        }

        /* update control window */
        aktName = akte[aktuellerAkt].aktname;
        // todo restore satzName when switching back to current akt
        satzName = "";

        /* handle satz button visibility */
        for (int i = 0; i < MAX_NUMBER_OF_SATZ_BUTTONS; i++) {
            if (i < akte[aktuellerAkt].saetze.length) {
                mSatzButtons[i].show();
            } else {
                mSatzButtons[i].hide();
            }
        }
    }

    private void saetzeSchreiben() {
        if (!RUN_WITH_SERIAL) {
            return;
        }

        for (int i = 0; i < akte.length; i++) {
            for (int j = 0; j < akte[i].saetze.length; j++) {
                for (int x = 0; x < akte[i].saetze[j].leinwaende.length; x++) {
                    final int mID = x + 1;
                    mSerial.write("#" + mID + "p" + akte[i].saetze[j].drehmodus + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "t" + akte[i].saetze[j].richtungswechsel + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "W" + akte[i].saetze[j].wiederholungen + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "b" + akte[i].saetze[j].startrampe + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "B" + akte[i].saetze[j].bremsrampe + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "P" + akte[i].saetze[j].pause + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "o" + akte[i].saetze[j].leinwaende[x].speed + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "s" + akte[i].saetze[j].leinwaende[x].position + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + "d" + akte[i].saetze[j].leinwaende[x].drehrichtung + "\r");
                    delay(DELAY_BETWEEN_SERIAL_WRITES);
                    mSerial.write("#" + mID + ">" + akte[i].saetze[j].satzID + "\r");
                }
            }
        }
    }

    private void xmlEinlesen() {
        performance = new XMLElement(this, XML_SCENE_DATA);
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

    private void leinwandSliderSetzen(int akt, int satz) {
        for (int i = 0; i < NUMBER_OF_LEINWAENDE; i++) {
            controlP5.controller("steps" + i).setValue(akte[akt].saetze[satz].leinwaende[i].position);
            leinwaende[i].akt = akt;
            leinwaende[i].satz = satz;
            if (DEBUG) {
                println("Winkel Setzen auf:" + akte[akt].saetze[satz].leinwaende[i].position);
            }
        }
//        aktName = akte[akt].aktname;
//        satzName = akte[akt].saetze[satz].name;
    }

    private void satzAufMotorAufrufen(int akt, int satz) {
        if (RUN_WITH_SERIAL) {
            mSerial.write("#*y" + akte[akt].saetze[satz].satzID + "\r");
        }
        if (DEBUG) {
            println("lade Satz:" + akte[akt].saetze[satz].satzID);
        }
    }

    private void GO() {
        if (RUN_WITH_SERIAL) {
            mSerial.write("#*A\r");
        }
        for (int i = 0; i < NUMBER_OF_LEINWAENDE; i++) {
            if (leinwaende[i].mCurrentAngle != leinwaende[i].steps * 0.9) {
                leinwaende[i].simulationsDrehung = true;
            }
        }
    }

    private class AktWerte {

        public SatzWerte[] saetze;

        public String aktname;

        public int satzarraysize;

        AktWerte(int arraysize) {
            satzarraysize = arraysize;
            aktname = "name";
            saetze = new SatzWerte[satzarraysize];
        }
    }

    private class SatzWerte {

        public LeinwandWerte[] leinwaende;

        public int leinwandarraysize;

        public int satzID;

        public int drehmodus;

        public int richtungswechsel;

        public int wiederholungen;

        public int startrampe;

        public int bremsrampe;

        public int pause;

        public String name;

        public SatzWerte(int arraysize) {
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
        }
    }

    private class LeinwandWerte {

        public int position;

        public int speed;

        public int drehrichtung;

        public LeinwandWerte() {
            position = 0;
            speed = 5;
            drehrichtung = 0;
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {
                    //                    "--present",
                    AppMotorInterface.class.getName()});
    }
}
