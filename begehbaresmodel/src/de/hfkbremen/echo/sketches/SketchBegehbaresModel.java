

package de.hfkbremen.echo.sketches;


import data.Resource;
import java.util.Vector;
import mathematik.Vector3f;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.xml.XMLElement;
import remixlab.proscene.Scene;


public class SketchBegehbaresModel
        extends PApplet {

    private static final String PLANES_XML_NAME = "planes";

    private static final String SVG_PLANE_FILE_NAME = Resource.getPath("blg_forum_plan_skinned_1zu100.svg");

    private static final String SVG_ENVIRONMENT_FILE_NAME = Resource.getPath("blg_forum_plan_skinned_1zu100_env.svg");

    private static final float SVG_CONVERSION_RATIO_PIXEL_TO_CM = 28.3464901562882f;

    private static final Vector3f PLANE_DIMENSIONS = new Vector3f(3, 4, 0.01);

    private Scene mScene;

    private PShape s;

    private Vector<Plane> mPlanes;

    private Environment mEnvironment;

    private Vector3f mCenter;

    private static final float TOTAL_NUMBER_OF_PLANES = 18.0f;

    private boolean mMoveMouse = false;

    private Vector3f mMousePosition = new Vector3f();

    public void setup() {
        size(1024, 768, OPENGL);

        /* env */
        mEnvironment = getEnvironmentfromXML(SVG_ENVIRONMENT_FILE_NAME);
        mCenter = getCenterofMassFromEnvironment(mEnvironment);
        
        /* camera */
        mScene = new Scene(this);
        mScene.setGridIsDrawn(false);
        mScene.setAxisIsDrawn(false);

        mScene.center().set(mCenter.x, mCenter.y, mCenter.z);
        mScene.camera().centerScene();

        /* reconstruct plane positions from SVG */
        mPlanes = new Vector<Plane>();
        final XMLElement mXML = new XMLElement(this, SVG_PLANE_FILE_NAME);
        final XMLElement mPlanesXML = getPlanesXML(mXML);

        if (mPlanesXML != null) {
            int mChildren = mPlanesXML.getChildCount();
            println("+++ found " + mChildren + " objects.");
            int mInstanceCounter = 0;
            for (int i = 0; i < mChildren; i++) {
                XMLElement mChild = mPlanesXML.getChild(i);
                String mID = mChild.getString("id");
                if (mID != null && mID.startsWith("p")) {
                    String mPosition = mChild.getString("d");
                    println("    " + mID + " -> " + mPosition);
                    Plane mPlane = new Plane(mInstanceCounter++);
                    mPlane.position().set(getVector3f(mPosition));
                    mPlanes.add(mPlane);
                }
            }
        } else {
            println("+++ file contains no planes");
        }
        println("+++ created " + mPlanes.size() + " planes.");
    }

    private Environment getEnvironmentfromXML(String pFilename) {
        final Environment _mEnvironment = new Environment();
        final Vector<Vector3f> mEnvironmentVertices = _mEnvironment.vertices;

        final XMLElement mXML = new XMLElement(this, pFilename);
        XMLElement mPolygon = mXML.getChild("polygon");
        if (mPolygon != null) {
            String[] mRawPoints = mPolygon.getString("points").split(" ");
            for (int i = 0; i < mRawPoints.length; i++) {
                final String mPointStr = mRawPoints[i];
                if (mPointStr.length() > 0) {
                    Vector3f v = new Vector3f(mPointStr);
                    /* adjust to here */
                    v.z = v.y;
                    v.y = 0.0f;
                    v.scale(1.0f / SVG_CONVERSION_RATIO_PIXEL_TO_CM);
                    mEnvironmentVertices.add(v);
                }
            }
        }

        return _mEnvironment;
    }

    private XMLElement getPlanesXML(XMLElement mXML) {
        int mChildren = mXML.getChildCount();
        for (int i = 0; i < mChildren; i++) {
            XMLElement mChild = mXML.getChild(i);
            if (mChild.hasAttribute("id")) {
                if (mChild.getString("id").equalsIgnoreCase(PLANES_XML_NAME)) {
                    return mChild;
                }
            }
        }
        return null;
    }

    private Vector3f getVector3f(String pSVGPosition) {
        final Vector3f mPosition = new Vector3f(pSVGPosition.replace("M", ""));
        /*toggle from topview to side */
        mPosition.z = mPosition.y;
        mPosition.y = 0.0f;
        mPosition.scale(1.0f / SVG_CONVERSION_RATIO_PIXEL_TO_CM);
        return mPosition;
    }

    public void keyPressed() {
        if (keyPressed) {
            switch (key) {
                case 'm':
                    mMoveMouse = !mMoveMouse;
                    break;
                case '0':
                    switchBehavior(Plane.BEHAVIOR_NO);
                    break;
                case '1':
                    switchBehavior(Plane.BEHAVIOR_MOUSE_FOLLOWER);
                    break;
                case '2':
                    switchBehavior(Plane.BEHAVIOR_ROTATION);
                    break;
                case '3':
                    switchBehavior(Plane.BEHAVIOR_ROTATION_ID_OFFSET);
                    break;
                case '4':
                    switchBehavior(Plane.BEHAVIOR_RANDOM);
                    break;
            }
        }
    }

    public void draw() {
        for (final Plane mPlane : mPlanes) {
            mPlane.behavior(1.0f / frameRate);
        }

        background(255);

        pushMatrix();

        noFill();
        stroke(0);
        drawEnvironment(mEnvironment);

        stroke(255, 0, 0);
        pushMatrix();
        final Vector3f mMousePosition = getEnvironmentMousePosition();
        translate(mMousePosition.x, mMousePosition.y, mMousePosition.z);
        translate(0, -0.9f, 0);
        box(0.7f, 1.8f, 0.5f);
        popMatrix();

        for (final Plane mPlane : mPlanes) {
            fill(255, 164);
            stroke(0);
            mPlane.draw(g);
        }

        popMatrix();
    }

    private void switchBehavior(final int pBehaviorID) {
        for (final Plane mPlane : mPlanes) {
            mPlane.setBehavior(pBehaviorID);
        }
    }

    private Vector3f getEnvironmentMousePosition() {
        if (mMoveMouse) {
            mMousePosition.set(mouseX, 0, mouseY);
            mMousePosition.sub(new Vector3f(width / 2.0f, 0.0f, height / 2.0f));
            mMousePosition.scale(1.0f / SVG_CONVERSION_RATIO_PIXEL_TO_CM);
            mMousePosition.scale(3, 1, 5);
            mMousePosition.add(mCenter);
        }
        return new Vector3f(mMousePosition);
    }

    private void drawEnvironment(Environment pEnvironment) {
        final Vector<Vector3f> mEnvironmentVertices = pEnvironment.vertices;
        beginShape();
        for (Vector3f v : mEnvironmentVertices) {
            vertex(v.x, v.y, v.z);
        }
        endShape(CLOSE);
    }

    private Vector3f getCenterofMassFromEnvironment(Environment pEnvironment) {
        final Vector<Vector3f> mEnvironmentVertices = pEnvironment.vertices;
        final Vector3f mCoM = new Vector3f();
        for (Vector3f v : mEnvironmentVertices) {
            mCoM.add(v);
        }
        mCoM.scale(1.0f / mEnvironmentVertices.size());
        return mCoM;
    }

    public class Environment {

        Vector<Vector3f> vertices = new Vector<Vector3f>();
    }

    public final class Plane {

        private Vector3f mPosition = new Vector3f();

        private Behavior mBehavior;

        private float mRotation;

        private final int mID;

        public static final int BEHAVIOR_NO = 0;

        public static final int BEHAVIOR_MOUSE_FOLLOWER = 1;

        public static final int BEHAVIOR_ROTATION = 2;

        public static final int BEHAVIOR_ROTATION_ID_OFFSET = 3;

        public static final int BEHAVIOR_RANDOM = 4;

        public Plane(int pID) {
            mID = pID;
            mRotation = 0.0f;
            setBehavior(BEHAVIOR_NO);
        }

        public void setBehavior(int pBehaviorID) {
            if (mBehavior != null) {
                mBehavior.finish();
            }
            switch (pBehaviorID) {
                case BEHAVIOR_NO:
                    mBehavior = new NoBehavior();
                    break;
                case BEHAVIOR_MOUSE_FOLLOWER:
                    mBehavior = new MouseFollowerBehavior();
                    break;
                case BEHAVIOR_ROTATION:
                    mBehavior = new RotationBehavior();
                    break;
                case BEHAVIOR_ROTATION_ID_OFFSET:
                    mBehavior = new RotationIDOffsetBehavior();
                    break;
                case BEHAVIOR_RANDOM:
                    mBehavior = new RandomBehavior();
                    break;
            }
            if (mBehavior != null) {
                mBehavior.setup();
            }
        }

        public Vector3f position() {
            return mPosition;
        }

        public void behavior(float pDeltaTime) {
            mBehavior.update(pDeltaTime);
        }

        private void draw(PGraphics g) {
            g.pushMatrix();
            g.translate(position().x, position().y, position().z);
            g.translate(0, PLANE_DIMENSIONS.y * -0.5f, 0);
            g.rotateY(mRotation);
            g.box(PLANE_DIMENSIONS.x, PLANE_DIMENSIONS.y, PLANE_DIMENSIONS.z);
            g.popMatrix();
        }

        private class NoBehavior
                implements Behavior {

            public void setup() {
            }

            public void update(float pDeltaTime) {
            }

            public void finish() {
            }
        }

        private class MouseFollowerBehavior
                implements Behavior {

            public void setup() {
            }

            public void update(float pDeltaTime) {
                Vector3f mMousePosition = getEnvironmentMousePosition();
                Vector3f mPointAt = mathematik.Util.sub(mMousePosition, mPosition);
                mPointAt.normalize();
                mRotation = atan2(mPointAt.x, mPointAt.z);
                mPointAt.normalize();
            }

            public void finish() {
            }
        }

        private class RotationBehavior
                implements Behavior {

            public void setup() {
                mRotation = (mID / TOTAL_NUMBER_OF_PLANES) * PI;
            }

            public void update(float pDeltaTime) {
                mRotation += pDeltaTime * 0.2f;
            }

            public void finish() {
            }
        }

        private class RotationIDOffsetBehavior
                implements Behavior {

            public void setup() {
            }

            public void update(float pDeltaTime) {
                final float mIDOffset = mID / TOTAL_NUMBER_OF_PLANES;
                mRotation += pDeltaTime * (0.1f + 0.2f * mIDOffset) * ((mID < TOTAL_NUMBER_OF_PLANES / 2) ? -1 : 1);
            }

            public void finish() {
            }
        }

        private class RandomBehavior
                implements Behavior {

            public void setup() {
                mRotation = random(2.0f * PI);
            }

            public void update(float pDeltaTime) {
            }

            public void finish() {
            }
        }
    }

    private interface Behavior {

        void setup();

        void update(float pDeltaTime);

        public void finish();
    }

    public static void main(String[] args) {
        PApplet.main(new String[] {SketchBegehbaresModel.class.getName()});
    }
}
