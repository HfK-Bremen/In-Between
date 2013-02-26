

package de.hfkbremen.echo.app;


import controlP5.ControlEvent;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PVector;


public class AppSimpleController
        extends PApplet {

    private static final boolean RUN_WITH_SERIAL = true;

    private static final String SET_POWER = "#*i";

    private static final String END = "\r";

    private SerialAdapter mSerialA;

    private ControlP5 mControl;

    private Segel[][] mRows;

    private static final int ROW_A = 0;

    private static final int ROW_B = 1;

    private static final int ROW_C = 2;

    private static final float mBackground = 50;

    private int[][] mDeadList = {{0, 3, 7},
                                 {0, 1, 3},
                                 {0, 1, 2}};

    private static final String START_ALL = "start_all";

    private static final String STOP_ALL = "stop_all";

    private static final String CHANGE_ALL_RIGHT = "change_all_right";

    private static final String CHANGE_ALL_LEFT = "change_all_left";

    public void setup() {
        Serial.DEBUG = false;
        size(1024, 768);
        background(mBackground);
        smooth();

        mControl = new ControlP5(this);
        mSerialA = new SerialAdapter();

        /* control */
        final int mButtonOffset = 30;
        int mButtonPosition = 10;
        mControl.addButton(START_ALL, 0, 10, mButtonPosition += mButtonOffset, 150, 20);
        mControl.addButton(STOP_ALL, 0, 10, mButtonPosition += mButtonOffset, 150, 20);
        mControl.addButton(CHANGE_ALL_LEFT, 0, 10, mButtonPosition += mButtonOffset, 150, 20);
        mControl.addButton(CHANGE_ALL_RIGHT, 0, 10, mButtonPosition += mButtonOffset, 150, 20);

        /* segel */
        mRows = new Segel[3][];
        mRows[ROW_A] = new Segel[8];
        mRows[ROW_B] = new Segel[5];
        mRows[ROW_C] = new Segel[4];

        PVector mPositionOffset = new PVector(200, 100);
        for (int x = 0; x < mRows.length; x++) {
            for (int y = 0; y < mRows[x].length; y++) {
                final Segel mSegel = new Segel(x, y);
                mRows[x][y] = mSegel;
                final float mZwischenraum = 10;
                mSegel.position.y = (mSegel.width + mZwischenraum) * x + mPositionOffset.x;
                mSegel.position.x = (mSegel.width + mZwischenraum) * y + mPositionOffset.y;
            }
        }
    }

    public void draw() {
        background(mBackground);

        for (int x = 0; x < mRows.length; x++) {
            for (int y = 0; y < mRows[x].length; y++) {
                Segel mSegel = mRows[x][y];
                mSegel.isOver();
            }
        }

        for (int x = 0; x < mRows.length; x++) {
            for (int y = 0; y < mRows[x].length; y++) {
                Segel mSegel = mRows[x][y];
                mSegel.draw();
            }
        }
    }

    public void controlEvent(ControlEvent theEvent) {
        if (theEvent.controller().name().equals(START_ALL)) {
            println(START_ALL);
            mSerialA.write("#*A\r");
        }
        if (theEvent.controller().name().equals(STOP_ALL)) {
            println(STOP_ALL);
            mSerialA.write("#*S\r");
        }
        if (theEvent.controller().name().equals(CHANGE_ALL_LEFT)) {
            println(CHANGE_ALL_LEFT);
            mSerialA.write("#*d0\r");
        }
        if (theEvent.controller().name().equals(CHANGE_ALL_RIGHT)) {
            println(CHANGE_ALL_RIGHT);
            mSerialA.write("#*d1\r");
        }
    }

    public class Segel {

        public PVector position = new PVector();

        public float width = 100;

        public int state;

        private static final int NONE = 0;

        private static final int INNER = 1;

        private static final int LEFT = 2;

        private static final int RIGHT = 3;

        public boolean dead;

        private int MOTOR_ID;

        public Segel(int pRow, int ID) {
            dead = false;
            for (int y = 0; y < mDeadList[pRow].length; y++) {
                if (ID == mDeadList[pRow][y]) {
                    dead = true;
                }
            }

            for (int i = 0; i < pRow; i++) {
                MOTOR_ID += mRows[i].length;
            }
            MOTOR_ID += ID;
            MOTOR_ID += 1;
        }

        public void draw() {
            pushMatrix();
            translate(position.x, position.y);

            noStroke();
            fill(255);
            ellipse(0, 0, width, width);

            if (!dead) {

                switch (state) {
                    case LEFT:
                        fill(0, 255, 0);
                        arc(0, 0, width, width, PI / 2, PI * 1.5f);
                        break;
                    case RIGHT:
                        fill(255, 0, 0);
                        arc(0, 0, width, width, PI / -2, PI / 2);
                        break;
                }

                stroke(mBackground);
                line(0, -width / 2, 0, width / 2);

                /* inner */
                if (state == INNER) {
                    fill(0, 0, 255);
                } else {
                    fill(255);
                }

                stroke(mBackground);
                ellipse(0, 0, width / 2, width / 2);
            }

            popMatrix();
        }

        public void isOver() {
            PVector mMouse = new PVector(mouseX, mouseY);
            mMouse.sub(position);
            final float mDistanceToMouse = mMouse.mag();
            /* state */
            state = NONE;
            if (mDistanceToMouse < width / 4) {
                state = INNER;
            } else if (mDistanceToMouse < width / 2) {
                if (mMouse.x > 0.0) {
                    state = RIGHT;
                } else {
                    state = LEFT;
                }
            }
        }

        public void evaluateState() {
            if (mousePressed) {
                switch (state) {
                    case LEFT:
                        mSerialA.write("#" + MOTOR_ID + "d0\r");
                        break;
                    case RIGHT:
                        mSerialA.write("#" + MOTOR_ID + "d1\r");
                        break;
                    case INNER:
                        mSerialA.write("#" + MOTOR_ID + "S\r");
                        break;
                }
            }
        }
    }

    public void mousePressed() {
        for (int x = 0; x < mRows.length; x++) {
            for (int y = 0; y < mRows[x].length; y++) {
                Segel mSegel = mRows[x][y];
                mSegel.isOver();
                mSegel.evaluateState();
            }
        }
    }

    class SerialAdapter {

        private Serial mSerial;

        public SerialAdapter() {
            try {
                mSerial = Serial.open("/dev/tty.SLAB_USBtoUART");
            } catch (Exception e) {
            }
        }

        public void write(String pMessage) {
            if (mSerial != null) {
                mSerial.write(pMessage);
            }
            println("> " + pMessage);
        }
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {AppSimpleController.class.getName()});
    }
}
