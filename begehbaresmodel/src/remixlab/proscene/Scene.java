/**
 *                     ProScene (version 1.1.0)      
 *    Copyright (c) 2010-2011 by National University of Colombia
 *                 @author Jean Pierre Charalambos      
 *           http://www.disi.unal.edu.co/grupos/remixlab/
 *                           
 * This java package provides classes to ease the creation of interactive 3D
 * scenes in Processing.
 * 
 * This source file is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * 
 * A copy of the GNU General Public License is available on the World Wide Web
 * at <http://www.gnu.org/copyleft/gpl.html>. You can also obtain it by
 * writing to the Free Software Foundation, 51 Franklin Street, Suite 500
 * Boston, MA 02110-1335, USA.
 */

package remixlab.proscene;

import processing.core.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A 3D interactive Processing scene.
 * <p>
 * A Scene has a full reach Camera, it can be used for on-screen or off-screen
 * rendering purposes (see the different constructors), and it has two means to
 * manipulate objects: an {@link #interactiveFrame()} single instance (which by
 * default is null) and a {@link #mouseGrabber()} pool.
 * <h3>Usage</h3>
 * To use a Scene you have three choices:
 * <ol>
 * <li><b>Direct instantiation</b>. In this case you should instantiate your own
 * Scene object at the {@code PApplet.setup()} function.
 * See the example <i>BasicUse</i>.
 * <li><b>Inheritance</b>. In this case, once you declare a Scene derived class,
 * you should implement {@link #proscenium()} which defines the objects in your
 * scene. Just make sure to define the {@code PApplet.draw()} method, even if
 * it's empty. See the example <i>AlternativeUse</i>.
 * <li><b>External draw handler registration</b>. You can even declare an
 * external drawing method and then register it at the Scene with
 * {@link #addDrawHandler(Object, String)}. That method should return {@code
 * void} and have one single {@code Scene} parameter. This strategy may be useful
 * when there are multiple viewers sharing the same drawing code. See the
 * example <i>StandardCamera</i>.
 * </ol>
 * <h3>Interactivity mechanisms</h3>
 * Proscene provides two interactivity mechanisms to manage your scene: global
 * keyboard shortcuts and camera profiles.
 * <ol>
 * <li><b>Global keyboard shortcuts</b> provide global configuration options
 * such as {@link #drawGrid()} or {@link #drawAxis()} that are common among
 * the different registered camera profiles. To define a global keyboard shortcut use
 * {@link #setShortcut(Character, KeyboardAction)} or one of its different forms.
 * Check {@link #setDefaultShortcuts()} to see the default global keyboard shortcuts.
 * <li><b>Camera profiles</b> represent a set of camera keyboard shortcuts, and camera and
 * frame mouse bindings which together represent a "camera mode". The scene provide
 * high-level methods to manage camera profiles such as
 * {@link #registerCameraProfile(CameraProfile)},
 * {@link #unregisterCameraProfile(CameraProfile)} or {@link #currentCameraProfile()}
 * among others. To perform the configuration of a camera profile see the CameraProfile
 * class documentation.
 * </ol>
 * <h3>Animation mechanisms</h3>
 * Proscene provides three animation mechanisms to define how your scene evolves
 * over time:
 * <ol>
 * <li><b>Overriding the {@link #animate()} method.</b>  In this case, once you
 * declare a Scene derived class, you should implement {@link #animate()} which
 * defines how your scene objects evolve over time. See the example <i>Animation</i>.
 * <li><b>External animation handler registration.</b> You can also declare an
 * external animation method and then register it at the Scene with
 * {@link #addAnimationHandler(Object, String)}. That method should return {@code
 * void} and have one single {@code Scene} parameter. See the example
 * <i>AnimationHandler</i>.
 * <li><b>By querying the state of the {@link #animatedFrameWasTriggered} variable.</b>
 * During the drawing loop, the variable {@link #animatedFrameWasTriggered} is set
 * to {@code true} each time an animated frame is triggered (and to {@code false}
 * otherwise), which is useful to notify the outside world when an animation event
 * occurs. See the example <i>Flock</i>.
 */
public class Scene implements PConstants {
	// proscene version
	public static final String version = "1.1.0";
	/**
	 * Returns the major release version number of proscene as an integer.
	 * <p>
	 * {@code Scene.version} will return the complete version (major+minor)
	 * number as a string. 
	 */
	public static int majorVersionNumber() {
		return Integer.parseInt(majorVersion());
	}
	
	/**
	 * Returns the major release version number of proscene as a string.
	 * <p>
	 * {@code Scene.version} will return the complete version (major+minor)
	 * number as a string.
	 */
	public static String majorVersion() {
		return version.substring(0, version.indexOf("."));
	}
	
	/**
	 * Returns the minor release version number of proscene as a float.
	 * <p>
	 * {@code Scene.version} will return the complete version (major+minor)
	 * number as a string.
	 */
	public static float minorVersionNumber() {
		return Float.parseFloat(minorVersion());
	}
	
	/**
	 * Returns the minor release version number of proscene as a string.
	 * <p>
	 * {@code Scene.version} will return the complete version (major+minor)
	 * number as a string.
	 */
	public static String minorVersion() {
		return version.substring(version.indexOf(".") + 1);
	}
	
	/**
	 * Defines the different actions that can be associated with a specific
	 * keyboard key.
	 */
	public enum KeyboardAction {
		/** Toggles the display of the world axis. */
		DRAW_AXIS("Toggles the display of the world axis"),
		/** Toggles the display of the XY grid. */
		DRAW_GRID("Toggles the display of the XY grid"),
		/** Cycles to the registered camera profiles. */
		CAMERA_PROFILE("Cycles to the registered camera profiles"),
		/** Toggles camera type (orthographic or perspective. */
		CAMERA_TYPE("Toggles camera type (orthographic or perspective)"),
		/** Toggles camera kind (proscene or standard). */
		CAMERA_KIND("Toggles camera kind (proscene or standard)"),
		/** Toggles animation. */
		ANIMATION("Toggles animation"),
		/** Set the arcball reference point from the pixel under the mouse. */
		ARP_FROM_PIXEL("Set the arcball reference point from the pixel under the mouse"),
		/** Reset the arcball reference point to the 3d frame world origin. */
		RESET_ARP("Reset the arcball reference point to the 3d frame world origin"),
		/** Displays the global help. */
		GLOBAL_HELP("Displays the global help"),
		/** Displays the current camera profile help. */
		CURRENT_CAMERA_PROFILE_HELP("Displays the current camera profile help"),
		/** Toggles the key frame camera paths (if any) for edition. */
		EDIT_CAMERA_PATH("Toggles the key frame camera paths (if any) for edition"),
		/** Toggle interactivity between camera and interactive frame (if any). */
		FOCUS_INTERACTIVE_FRAME("Toggle interactivity between camera and interactive frame (if any)"),
		/** Toggle interactive frame selection region drawing. */
		DRAW_FRAME_SELECTION_HINT("Toggle interactive frame selection region drawing"),
		/** Toggles on and off frame constraints (if any). */
		CONSTRAIN_FRAME("Toggles on and off frame constraints (if any)");
		
		private String description;
		
		KeyboardAction(String description) {
       this.description = description;
    }
    
    public String description() {
      return description;
    }
	}

	/**
	 * Defines the different camera actions that can be associated with a specific
	 * keyboard key. Actions are defined here, but bindings are defined at the CameraProfile level,
	 * i.e., the scene acts like a bridge between the CameraProfile and proscene low-level classes.
	 */
	public enum CameraKeyboardAction {
		/** Interpolate the camera to zoom on pixel. */
		INTERPOLATE_TO_ZOOM_ON_PIXEL("Interpolate the camera to zoom on pixel"),
		/** Interpolate the camera to fit the whole scene. */
		INTERPOLATE_TO_FIT_SCENE("Interpolate the camera to fit the whole scene"),
		/** Show the whole scene. */
		SHOW_ALL("Show the whole scene"),
		/** Move camera to the left. */
		MOVE_CAMERA_LEFT("Move camera to the left"),
		/** Move camera to the right. */
		MOVE_CAMERA_RIGHT("Move camera to the right"),
		/** Move camera up. */
		MOVE_CAMERA_UP("Move camera up"),
		/** Move camera down. */
		MOVE_CAMERA_DOWN("Move camera down"),
		/** Increase camera rotation sensitivity (only meaningful in arcball mode). */
		INCREASE_ROTATION_SENSITIVITY("Increase camera rotation sensitivity (only meaningful in arcball mode)"),
		/** Decrease camera rotation sensitivity (only meaningful in arcball mode). */
		DECREASE_ROTATION_SENSITIVITY("Decrease camera rotation sensitivity (only meaningful in arcball mode)"),
		/** Increase camera fly speed (only meaningful in first-person mode). */
		INCREASE_CAMERA_FLY_SPEED("Increase camera fly speed (only meaningful in first-person mode)"),
		/** Decrease camera fly speed (only meaningful in first-person mode). */
		DECREASE_CAMERA_FLY_SPEED("Decrease camera fly speed (only meaningful in first-person mode)"),
		/** Increase avatar fly speed (only meaningful in third-person mode). */
		INCREASE_AVATAR_FLY_SPEED("Increase avatar fly speed (only meaningful in third-person mode)"),
		/** Decrease avatar fly speed (only meaningful in third-person mode). */
		DECREASE_AVATAR_FLY_SPEED("Decrease avatar fly speed (only meaningful in third-person mode)"),
		/** Increase camera azymuth respect to the avatar (only meaningful in third-person mode). */
		INCREASE_AZYMUTH("Increase camera azymuth respect to the avatar (only meaningful in third-person mode)"),
		/** Decrease camera azymuth respect to the avatar (only meaningful in third-person mode). */
		DECREASE_AZYMUTH("Decrease camera azymuth respect to the avatar (only meaningful in third-person mode)"),
		/** Increase camera inclination respect to the avatar (only meaningful in third-person mode). */
		INCREASE_INCLINATION("Increase camera inclination respect to the avatar (only meaningful in third-person mode)"),
		/** Decrease camera inclination respect to the avatar (only meaningful in third-person mode). */
		DECREASE_INCLINATION("Decrease camera inclination respect to the avatar (only meaningful in third-person mode)"),
		/** Increase camera tracking distance respect to the avatar (only meaningful in third-person mode). */
		INCREASE_TRACKING_DISTANCE("Increase camera tracking distance respect to the avatar (only meaningful in third-person mode)"),
		/** Decrease camera tracking distance respect to the avatar (only meaningful in third-person mode). */
		DECREASE_TRACKING_DISTANCE("Decrease camera tracking distance respect to the avatar (only meaningful in third-person mode)");
		
		private String description;
		
		CameraKeyboardAction(String description) {
       this.description = description;
    }
		
    public String description() {
        return description;
    }
	}

	/**
	 * This enum defines mouse click actions to be binded to the mouse.
	 * Actions are defined here, but bindings are defined at the CameraProfile level,
	 * i.e., the scene acts like a bridge between the CameraProfile and proscene low-level classes.
	 */
	public enum ClickAction {
		/** No click action. */
		NO_CLICK_ACTION("No click action"),
		/** Zoom on pixel */
		ZOOM_ON_PIXEL("Zoom on pixel"),
		/** Zoom to fit the scene */
		ZOOM_TO_FIT("Zoom to fit the scene"),
		/** Set the arcball reference point from the pixel under the mouse */
		ARP_FROM_PIXEL("Set the arcball reference point from the pixel under the mouse"),
		/** Reset the arcball reference point to the 3d frame world origin */
		RESET_ARP("Reset the arcball reference point to the 3d frame world origin"),
		/** Center frame */
		CENTER_FRAME("Center frame"),
		/** Center scene */
		CENTER_SCENE("Center scene"),
		/** Show the whole scene */
		SHOW_ALL("Show the whole scene"),
		/** Align interactive frame (if any) with world */
		ALIGN_FRAME("Align interactive frame (if any) with world"),
		/** Align camera with world */
		ALIGN_CAMERA("Align camera with world");

		private String description;
		
		ClickAction(String description) {
       this.description = description;
    }
		
    public String description() {
        return description;
    }
	}

	/**
	 * This enum defines mouse actions (click + drag) to be binded to the mouse.
	 * Actions are defined here, but bindings are defined at the CameraProfile level,
	 * i.e., the scene acts like a bridge between the CameraProfile and proscene low-level classes.
	 */
	public enum MouseAction {
		/** No mouse action. */
		NO_MOUSE_ACTION("No mouse action"),
		/** Rotate frame (camera or interactive frame. */
		ROTATE("Rotate frame (camera or interactive frame)"),
		/** Zoom. */
		ZOOM("Zoom"),
		/** Translate frame (camera or interactive frame). */
		TRANSLATE("Translate frame (camera or interactive frame)"),
		/** Move forward frame (camera or interactive frame). */
		MOVE_FORWARD("Move forward frame (camera or interactive frame)"),
		/** move backward frame (camera or interactive frame). */
		MOVE_BACKWARD("move backward frame (camera or interactive frame)"),
		/** Look around with frame (camera or interactive drivable frame). */
		LOOK_AROUND("Look around with frame (camera or interactive drivable frame)"),
		/** Screen rotate (camera or interactive frame). */
		SCREEN_ROTATE("Screen rotate (camera or interactive frame)"),
		/** Roll frame (camera or interactive drivable frame). */
		ROLL("Roll frame (camera or interactive drivable frame)"),
		/** Drive (camera or interactive drivable frame). */
		DRIVE("Drive (camera or interactive drivable frame)"),
		/** Screen translate frame (camera or interactive frame). */
		SCREEN_TRANSLATE("Screen translate frame (camera or interactive frame)"),
		/** Zoom on region (camera or interactive drivable frame). */
		ZOOM_ON_REGION("Zoom on region (camera or interactive drivable frame)");		

		private String description;
		
		MouseAction(String description) {
       this.description = description;
    }
		
    public String description() {
        return description;
    }
	}

	/**
	 * Constants associated to the different mouse buttons which follow java conventions.
	 */
	public enum Button {
		// values correspond to: BUTTON1_DOWN_MASK, BUTTON2_DOWN_MASK and BUTTON3_DOWN_MASK
		// see: http://download-llnw.oracle.com/javase/6/docs/api/constant-values.html
		LEFT(1024), MIDDLE(2048), RIGHT(4096);
		public final int ID;
    Button(int code) {
    	this.ID = code;
    }    
    //The following code works but is considered overkill :)
    /**
    public int id() { return ID; }
    private static final Map<Integer,Button> lookup = new HashMap<Integer,Button>();
    static {
    	for(Button s : EnumSet.allOf(Button.class))
         lookup.put(s.id(), s);
    }
    public static Button get(int code) { 
      return lookup.get(code);
    }
    // */
	}

	/**
	 * Constants associated to the different arrow keys. Taken from Processing constants 
	 * (which follows java conventions). 
	 */	
	public enum Arrow {
		UP(PApplet.UP), DOWN(PApplet.DOWN), LEFT(PApplet.LEFT), RIGHT(PApplet.RIGHT);
		public final int ID;
    Arrow(int code) {
    	this.ID = code;
    }
    //The following code works but is considered overkill :)
    /**
    public int id() { return ID; }
    private static final Map<Integer,Arrow> lookup = new HashMap<Integer,Arrow>();
    static {
    	for(Arrow s : EnumSet.allOf(Arrow.class))
         lookup.put(s.id(), s);
    }
    public static Arrow get(int code) { 
      return lookup.get(code);
    }
    // */
	}

	/**
	 * Constants associated to the different modifier keys which follow java conventions.
	 */
	public enum Modifier {
		// values correspond to: ALT_DOWN_MASK, SHIFT_DOWN_MASK, CTRL_DOWN_MASK, META_DOWN_MASK, ALT_GRAPH_DOWN_MASK
		// see: http://download-llnw.oracle.com/javase/6/docs/api/constant-values.html
		ALT(512), SHIFT(64), CTRL(128), META(256), ALT_GRAPH(8192);
		public final int ID;
		Modifier(int code) {
      this.ID = code;
    }
    //The following code works but is considered overkill :)
    /**
    public int id() { return ID; }
    private static final Map<Integer,Modifier> lookup = new HashMap<Integer,Modifier>();
    static {
    	for(Modifier s : EnumSet.allOf(Modifier.class))
         lookup.put(s.id(), s);
    }
    public static Modifier get(int code) {
      return lookup.get(code);
    }
    // */
	}

	// K E Y F R A M E S
	protected Bindings<Integer, Integer> pathKeys;
	protected Modifier addKeyFrameKeyboardModifier;
	protected Modifier deleteKeyFrameKeyboardModifier;

	// S h o r t c u t k e y s
	protected Bindings<KeyboardShortcut, KeyboardAction> gProfile;

	// c a m e r a p r o f i l e s
	private HashMap<String, CameraProfile> cameraProfileMap;
	private ArrayList<String> cameraProfileNames;
	private CameraProfile currentCameraProfile;

	// mouse actions
	protected boolean arpFlag;
	protected boolean pupFlag;
	protected PVector pupVec;

	// P R O C E S S I N G   A P P L E T   A N D   O B J E C T S
	public PApplet parent;
	public PGraphics3D pg3d;
	protected int width, height;// size
	protected boolean offscreen;
	public Point upperLeftCorner;
	protected Frame tmpFrame;

	// O B J E C T S
	protected DesktopEvents dE;
	protected Camera cam;
	protected InteractiveFrame glIFrame;
	// boolean interactiveFrameIsAnAvatar;
	protected boolean iFrameIsDrwn;
	protected Trackable trck;
	protected boolean avatarIsInteractiveDrivableFrame;
	protected boolean avatarIsInteractiveAvatarFrame;

	// S C R E E N C O O R D I N A T E S	
	protected float zC;

	// E X C E P T I O N H A N D L I N G
	protected int startCoordCalls;
  protected int beginOffScreenDrawingCalls;

	// M o u s e G r a b b e r
	protected List<MouseGrabbable> MouseGrabberPool;
	protected MouseGrabbable mouseGrbbr;
	protected boolean mouseGrabberIsAnIFrame;	
	protected boolean mouseTrckn;

	// D I S P L A Y F L A G S
	private boolean axisIsDrwn; // world axis
	private boolean gridIsDrwn; // world XY grid
	private boolean frameSelectionHintIsDrwn;
	private boolean cameraPathsAreDrwn;

	// C O N S T R A I N T S
	private boolean withConstraint;

	// K E Y B O A R D A N D M O U S E
	protected boolean mouseHandling;
	protected boolean keyboardHandling;
	
	// A N I M A T I O N
	protected float targetFrameRate;
	protected float animationFrameRate;
	private long initialDrawingFrameWhenAnimationStarted;
	private long currentAnimationFrame;
	private float animationToFrameRateRatio;
	//private int framesInBetween;
	private boolean animationStarted;
	public boolean animatedFrameWasTriggered;
	private float animationPeriod;

	// R E G I S T E R   D R A W   A N D   A N I M A T I O N   M E T H O D S
	// Draw
	/** The object to handle the draw event */
	protected Object drawHandlerObject;
	/** The method in drawHandlerObject to execute */
	protected Method drawHandlerMethod;
	/** the name of the method to handle the event */
	protected String drawHandlerMethodName;
	// Animation
	/** The object to handle the animation */
	protected Object animateHandlerObject;
	/** The method in animateHandlerObject to execute */
	protected Method animateHandlerMethod;
	/** the name of the method to handle the animation */
	protected String animateHandlerMethodName;
	
	// D E V I C E S
	
	protected ArrayList<HIDevice> devices;

	/**
	 * Constructor that defines an on-screen Scene (the one that most likely
	 * would just fulfill all of your needs). All viewer parameters (display flags,
	 * scene parameters, associated objects...) are set to their default values.
	 * See the associated documentation. This is actually just a convenience
	 * function that simply calls {@code this(p, (PGraphics3D) p.g)}. Call any
	 * other constructor by yourself to possibly define an off-screen Scene.
	 * 
	 * @see #Scene(PApplet, PGraphics3D)
	 * @see #Scene(PApplet, PGraphics3D, int, int)
	 */	
	public Scene(PApplet p) {
		this(p, (PGraphics3D) p.g);
	}
	
	/**
	 * This constructor is typically used to define an off-screen Scene. This is
	 * accomplished simply by specifying a custom {@code renderer}, different
	 * from the PApplet's renderer. All viewer parameters (display flags, scene
	 * parameters, associated objects...) are set to their default values. This
	 * is actually just a convenience function that simply calls
	 * {@code this(p, renderer, 0, 0)}. If you plan to define an on-screen Scene,
	 * call {@link #Scene(PApplet)} instead.
	 * 
	 * @see #Scene(PApplet)
	 * @see #Scene(PApplet, PGraphics3D, int, int)
	 */
	public Scene(PApplet p, PGraphics3D renderer) {
		this(p, renderer, 0, 0);
	}

	/**
	 * This constructor is typically used to define an off-screen Scene. This is
	 * accomplished simply by specifying a custom {@code renderer}, different
	 * from the PApplet's renderer. All viewer parameters (display flags, scene
	 * parameters, associated objects...) are set to their default values. The
	 * {@code x} and {@code y} parameters define the position of the upper-left
	 * corner where the off-screen Scene is expected to be displayed, e.g., for
	 * instance with a call to the Processing built-in {@code image(img, x, y)}
	 * function. If {@link #isOffscreen()} returns {@code false} (i.e.,
	 * {@link #renderer()} equals the PApplet's renderer), the values of x and y
	 * are meaningless (both are set to 0 to be taken as dummy values). If you
	 * plan to define an on-screen Scene, call {@link #Scene(PApplet)} instead. 
	 * 
	 * @see #Scene(PApplet)
	 * @see #Scene(PApplet, PGraphics3D)
	 */
	public Scene(PApplet p, PGraphics3D renderer, int x, int y) {
		parent = p;
		pg3d = renderer;
		width = pg3d.width;
		height = pg3d.height;
		
		tmpFrame = new Frame();
		
		//event handler
		dE = new DesktopEvents(this);
		
		//mouse grabber pool
		MouseGrabberPool = new ArrayList<MouseGrabbable>();
		
		devices = new ArrayList<HIDevice>();

		gProfile = new Bindings<KeyboardShortcut, KeyboardAction>(this);
		pathKeys = new Bindings<Integer, Integer>(this);		
		setDefaultShortcuts();

		avatarIsInteractiveDrivableFrame = false;// also init in setAvatar, but we
		// need it here to properly init the camera
		avatarIsInteractiveAvatarFrame = false;// also init in setAvatar, but we
		// need it here to properly init the camera
		cam = new Camera(this);
		setCamera(camera());//calls showAll();
		setInteractiveFrame(null);
		setAvatar(null);
		
  	// This scene is offscreen if the provided renderer is
		// different from the main PApplet renderer.
		offscreen = renderer != p.g;
		if(offscreen)
			upperLeftCorner = new Point(x, y);
		else
			upperLeftCorner = new Point(0, 0);
		beginOffScreenDrawingCalls = 0;		
		setMouseTracking(true);
		setMouseGrabber(null);
		
		mouseGrabberIsAnIFrame = false;

		initDefaultCameraProfiles();

		//animation		
		animationStarted = false;
		setFrameRate(60, false);
		setAnimationPeriod(1000/60, false); // 60Hz
		stopAnimation();
		
		arpFlag = false;
		pupFlag = false;

		withConstraint = true;

		setAxisIsDrawn(true);
		setGridIsDrawn(true);
		setFrameSelectionHintIsDrawn(false);
		setCameraPathsAreDrawn(false);
		
		disableFrustumEquationsUpdate();

		// E X C E P T I O N H A N D L I N G
		startCoordCalls = 0;

		parent.registerPre(this);
		parent.registerDraw(this);
		// parent.registerPost(this);
		enableKeyboardHandling();
		enableMouseHandling();
		parseKeyXxxxMethods();
		parseMouseXxxxMethods();

		// register draw method
		removeDrawHandler();
	  // register animation method
		removeAnimationHandler();

		// called only once
		init();
	}
	
	// 1. Scene overloaded
	
	/**
	 * This method is called before the first drawing happen and should be overloaded to
	 * initialize stuff not initialized in {@code PApplet.setup()}. The default
	 * implementation is empty.
	 * <p>
	 * Typical usage include {@link #camera()} initialization ({@link #showAll()})
	 * and Scene state setup ({@link #setAxisIsDrawn(boolean)} and
	 * {@link #setGridIsDrawn(boolean)}.
	 */
	public void init() {}
	
	/**
	 * The method that actually defines the scene.
	 * <p>
	 * If you build a class that inherits from Scene, this is the method you
	 * should overload, but no if you instantiate your own Scene object (in this
	 * case you should just overload {@code PApplet.draw()} to define your scene).
	 * <p>
	 * The processing camera set in {@link #pre()} converts from the world to the
	 * camera coordinate systems. Vertices given in {@link #draw()} can then be
	 * considered as being given in the world coordinate system. The camera is
	 * moved in this world using the mouse. This representation is much more
	 * intuitive than a camera-centric system (which for instance is the standard
	 * in OpenGL).
	 */
	public void proscenium() {}

	// 2. Associated objects

	/**
	 * Returns a list containing references to all the active MouseGrabbers.
	 * <p>
	 * Used to parse all the MouseGrabbers and to check if any of them
	 * {@link remixlab.proscene.MouseGrabbable#grabsMouse()} using
	 * {@link remixlab.proscene.MouseGrabbable#checkIfGrabsMouse(int, int, Camera)}.
	 * <p>
	 * You should not have to directly use this list. Use
	 * {@link #removeFromMouseGrabberPool(MouseGrabbable)} and
	 * {@link #addInMouseGrabberPool(MouseGrabbable)} to modify this list.
	 */
	public List<MouseGrabbable> mouseGrabberPool() {
		return MouseGrabberPool;
	}

	/**
	 * Returns the associated Camera, never {@code null}.
	 */
	public Camera camera() {
		return cam;
	}

	/**
	 * Replaces the current {@link #camera()} with {@code camera}
	 */
	public void setCamera(Camera camera) {
		if (camera == null)
			return;

		camera.setSceneRadius(radius());
		camera.setSceneCenter(center());

		camera.setScreenWidthAndHeight(pg3d.width, pg3d.height);

		cam = camera;

		showAll();
	}

	/**
	 * Returns the InteractiveFrame associated to this Scene. It could be null if
	 * there's no InteractiveFrame associated to this Scene.
	 * 
	 * @see #setInteractiveFrame(InteractiveFrame)
	 */
	public InteractiveFrame interactiveFrame() {
		return glIFrame;
	}

	/**
	 * Returns the avatar object to be tracked by the Camera when
	 * {@link #currentCameraProfile()} is an instance of ThirdPersonCameraProfile.
	 * Simply returns {@code null} if no avatar has been set.
	 */
	public Trackable avatar() {
		return trck;
	}

	/**
	 * Sets the avatar object to be tracked by the Camera when
	 * {@link #currentCameraProfile()} is an instance of ThirdPersonCameraProfile.
	 * 
	 * @see #unsetAvatar()
	 */
	public void setAvatar(Trackable t) {
		trck = t;
		avatarIsInteractiveAvatarFrame = false;
		avatarIsInteractiveDrivableFrame = false;
		if (avatar() instanceof InteractiveAvatarFrame) {
			avatarIsInteractiveAvatarFrame = true;
			avatarIsInteractiveDrivableFrame = true;
			if (interactiveFrame() != null)
				((InteractiveDrivableFrame) interactiveFrame())
						.setFlySpeed(0.01f * radius());
		} else if (avatar() instanceof InteractiveDrivableFrame) {
			avatarIsInteractiveAvatarFrame = false;
			avatarIsInteractiveDrivableFrame = true;
			if (interactiveFrame() != null)
				((InteractiveDrivableFrame) interactiveFrame())
						.setFlySpeed(0.01f * radius());
		}
	}

	/**
	 * If there's a avatar unset it.
	 * 
	 * @see #setAvatar(Trackable)
	 */
	public void unsetAvatar() {
		trck = null;
		avatarIsInteractiveAvatarFrame = false;
		avatarIsInteractiveDrivableFrame = false;
	}

	/**
	 * Sets {@code frame} as the InteractiveFrame associated to this Scene. If
	 * {@code frame} is instance of Trackable it is also automatically set as the
	 * Scene {@link #avatar()} (by automatically calling {@code
	 * setAvatar((Trackable) frame)}).
	 * 
	 * @see #interactiveFrame()
	 * @see #setAvatar(Trackable)
	 */
	public void setInteractiveFrame(InteractiveFrame frame) {
		glIFrame = frame;		
		if (glIFrame == null)
			iFrameIsDrwn = false;
		else if (glIFrame instanceof Trackable)
			setAvatar((Trackable) glIFrame);
	}

	/**
	 * Returns the current MouseGrabber, or {@code null} if none currently grabs
	 * mouse events.
	 * <p>
	 * When {@link remixlab.proscene.MouseGrabbable#grabsMouse()}, the different
	 * mouse events are sent to it instead of their usual targets (
	 * {@link #camera()} or {@link #interactiveFrame()}).
	 */
	public MouseGrabbable mouseGrabber() {
		return mouseGrbbr;
	}

	/**
	 * Directly defines the {@link #mouseGrabber()}.
	 * <p>
	 * You should not call this method directly as it bypasses the
	 * {@link remixlab.proscene.MouseGrabbable#checkIfGrabsMouse(int, int, Camera)}
	 * test performed by {@link #mouseMoved(MouseEvent)}.
	 */
	protected void setMouseGrabber(MouseGrabbable mouseGrabber) {
		mouseGrbbr = mouseGrabber;

		mouseGrabberIsAnIFrame = mouseGrabber instanceof InteractiveFrame;
	}
	
	// 3. Mouse grabber handling
	
	/**
	 * Returns true if the mouseGrabber is currently in the {@link #mouseGrabberPool()} list.
	 * <p>
	 * When set to false using {@link #removeFromMouseGrabberPool(MouseGrabbable)}, the Scene no longer
	 * {@link remixlab.proscene.MouseGrabbable#checkIfGrabsMouse(int, int, Camera)} on this mouseGrabber.
	 * Use {@link #addInMouseGrabberPool(MouseGrabbable)} to insert it back.
	 */
	public boolean isInMouseGrabberPool(MouseGrabbable mouseGrabber) {
		return mouseGrabberPool().contains(mouseGrabber);
	}
	
	/**
	 * Adds the mouseGrabber in the {@link #mouseGrabberPool()}.
	 * <p>
	 * All created InteractiveFrames (which are MouseGrabbers) are automatically added in the
	 * {@link #mouseGrabberPool()} by their constructors. Trying to add a
	 * mouseGrabber that already {@link #isInMouseGrabberPool(MouseGrabbable)} has no effect.
	 * <p>
	 * Use {@link #removeFromMouseGrabberPool(MouseGrabbable)} to remove the mouseGrabber from
	 * the list, so that it is no longer tested with
	 * {@link remixlab.proscene.MouseGrabbable#checkIfGrabsMouse(int, int, Camera)}
	 * by the Scene, and hence can no longer grab mouse focus. Use
	 * {@link #isInMouseGrabberPool(MouseGrabbable)} to know the current state of the MouseGrabber.
	 */
	public void addInMouseGrabberPool(MouseGrabbable mouseGrabber) {
		if (!isInMouseGrabberPool(mouseGrabber))
			mouseGrabberPool().add(mouseGrabber);
	}

	/**
	 * Removes the mouseGrabber from the {@link #mouseGrabberPool()}.
	 * <p>
	 * See {@link #addInMouseGrabberPool(MouseGrabbable)} for details. Removing a mouseGrabber
	 * that is not in {@link #mouseGrabberPool()} has no effect.
	 */
	public void removeFromMouseGrabberPool(MouseGrabbable mouseGrabber) {
		mouseGrabberPool().remove(mouseGrabber);
	}

	/**
	 * Clears the {@link #mouseGrabberPool()}.
	 * <p>
	 * Use this method only if it is faster to clear the
	 * {@link #mouseGrabberPool()} and then to add back a few MouseGrabbers
	 * than to remove each one independently.
	 */
	public void clearMouseGrabberPool() {
		mouseGrabberPool().clear();
	}
	
	/**
	 * Returns {@code true}
	 * if {@link remixlab.proscene.DesktopEvents#mouseMoved(java.awt.event.MouseEvent)}
	 * is called even when no mouse button is pressed.
	 * <p>
	 * You need to setMouseTracking() to \c true in order to use MouseGrabber (see mouseGrabber()).
	 */
	public boolean hasMouseTracking() {
		return mouseTrckn;
	}
	
	/**
	 * Sets the {@link #hasMouseTracking()} value.
	 */
	public void setMouseTracking(boolean enable) {		
		if(!enable) {
			if( mouseGrabber() != null )
				mouseGrabber().setGrabsMouse(false);
			setMouseGrabber(null);
		}
		mouseTrckn = enable;
	}
	
	/**
	 * Calls {@link #setMouseTracking(boolean)} to toggle the {@link #hasMouseTracking()} value.
	 */
	public void toggleMouseTracking() {
		setMouseTracking(!hasMouseTracking());
	}

	// 4. State of the viewer

	/**
	 * Returns {@code true} if this Scene is associated to an offscreen 
	 * renderer and {@code false} otherwise.
	 * 
	 * @see #Scene(PApplet, PGraphics3D)
	 */
	
	public boolean isOffscreen() {
		return offscreen;
	}
	
	/**
	 * Returns {@code true} if automatic update of the camera frustum plane
	 * equations is enabled and {@code false} otherwise. Computation of the
	 * equations is expensive and hence is disabled by default.
	 * 
	 * @see #toggleFrustumEquationsUpdate()
	 * @see #disableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate(boolean)
	 * @see remixlab.proscene.Camera#updateFrustumEquations()
	 */
	public boolean frustumEquationsUpdateIsEnable() {
		return camera().frustumEquationsUpdateIsEnable();
	}

	/**
	 * Toggles automatic update of the camera frustum plane equations every frame.
	 * Computation of the equations is expensive and hence is disabled by default.
	 * 
	 * @see #frustumEquationsUpdateIsEnable()
	 * @see #disableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate(boolean)
	 * @see remixlab.proscene.Camera#updateFrustumEquations()
	 */
	public void toggleFrustumEquationsUpdate() {
		if ( frustumEquationsUpdateIsEnable() )
			disableFrustumEquationsUpdate();
		else
			enableFrustumEquationsUpdate();
	}

	/**
	 * Disables automatic update of the camera frustum plane equations every
	 * frame. Computation of the equations is expensive and hence is disabled by
	 * default.
	 * 
	 * @see #frustumEquationsUpdateIsEnable()
	 * @see #toggleFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate(boolean)
	 * @see remixlab.proscene.Camera#updateFrustumEquations()
	 */
	public void disableFrustumEquationsUpdate() {
		enableFrustumEquationsUpdate(false);
	}

	/**
	 * Enables automatic update of the camera frustum plane equations every frame.
	 * Computation of the equations is expensive and hence is disabled by default.
	 * 
	 * @see #frustumEquationsUpdateIsEnable()
	 * @see #toggleFrustumEquationsUpdate()
	 * @see #disableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate(boolean)
	 * @see remixlab.proscene.Camera#updateFrustumEquations()
	 */
	public void enableFrustumEquationsUpdate() {
		enableFrustumEquationsUpdate(true);
	}

	/**
	 * Enables or disables automatic update of the camera frustum plane equations
	 * every frame according to {@code flag}. Computation of the equations is
	 * expensive and hence is disabled by default.
	 * 
	 * @see #frustumEquationsUpdateIsEnable()
	 * @see #toggleFrustumEquationsUpdate()
	 * @see #disableFrustumEquationsUpdate()
	 * @see #enableFrustumEquationsUpdate()
	 * @see remixlab.proscene.Camera#updateFrustumEquations()
	 */
	public void enableFrustumEquationsUpdate(boolean flag) {
		camera().enableFrustumEquationsUpdate(flag);
	}

	/**
	 * Toggles the state of {@link #axisIsDrawn()}.
	 * 
	 * @see #axisIsDrawn()
	 * @see #setAxisIsDrawn(boolean)
	 */
	public void toggleAxisIsDrawn() {
		setAxisIsDrawn(!axisIsDrawn());
	}

	/**
	 * Toggles the state of {@link #gridIsDrawn()}.
	 * 
	 * @see #setGridIsDrawn(boolean)
	 */
	public void toggleGridIsDrawn() {
		setGridIsDrawn(!gridIsDrawn());
	}

	/**
	 * Toggles the state of {@link #frameSelectionHintIsDrawn()}.
	 * 
	 * @see #setFrameSelectionHintIsDrawn(boolean)
	 */
	public void toggleFrameSelectionHintIsDrawn() {
		setFrameSelectionHintIsDrawn(!frameSelectionHintIsDrawn());
	}

	/**
	 * Toggles the state of {@link #cameraPathsAreDrawn()}.
	 * 
	 * @see #setCameraPathsAreDrawn(boolean)
	 */
	public void toggleCameraPathsAreDrawn() {
		setCameraPathsAreDrawn(!cameraPathsAreDrawn());
	}

	/**
	 * Toggles the {@link #camera()} type between PERSPECTIVE and ORTHOGRAPHIC.
	 */
	public void toggleCameraType() {
		if (camera().type() == Camera.Type.PERSPECTIVE)
			setCameraType(Camera.Type.ORTHOGRAPHIC);
		else
			setCameraType(Camera.Type.PERSPECTIVE);
	}

	/**
	 * Toggles the {@link #camera()} kind between PROSCENE and STANDARD.
	 */
	public void toggleCameraKind() {
		if (camera().kind() == Camera.Kind.PROSCENE)
			setCameraKind(Camera.Kind.STANDARD);
		else
			setCameraKind(Camera.Kind.PROSCENE);
	}

	/**
	 * Toggles the {@link #interactiveFrame()} interactivity on and off.
	 */
	public void toggleDrawInteractiveFrame() {
		if (interactiveFrameIsDrawn())
			setDrawInteractiveFrame(false);
		else
			setDrawInteractiveFrame(true);
	}

	/**
	 * Toggles the draw with constraint on and off.
	 */
	public void toggleDrawWithConstraint() {
		if (drawIsConstrained())
			setDrawWithConstraint(false);
		else
			setDrawWithConstraint(true);
	}
	
	/**
	 * Returns the current {@link #camera()} type.
	 */
	public final Camera.Type cameraType() {
		return camera().type();
	}

	/**
	 * Sets the {@link #camera()} type.
	 */
	public void setCameraType(Camera.Type type) {
		if (type != camera().type())
			camera().setType(type);
	}

	/**
	 * Returns the current {@link #camera()} kind.
	 */
	public final Camera.Kind cameraKind() {
		return camera().kind();
	}

	/**
	 * Sets the {@link #camera()} kind.
	 */
	public void setCameraKind(Camera.Kind kind) {
		if (kind != camera().kind()) {
			camera().setKind(kind);
			if (kind == Camera.Kind.PROSCENE)
				PApplet.println("Changing camera kind to Proscene");
			else
				PApplet.println("Changing camera kind to Standard");
		}
	}
	
	/**
	 * Returns {@code true} if axis is currently being drawn and {@code false}
	 * otherwise.
	 */
	public boolean axisIsDrawn() {
		return axisIsDrwn;
	}

	/**
	 * Returns {@code true} if grid is currently being drawn and {@code false}
	 * otherwise.
	 */
	public boolean gridIsDrawn() {
		return gridIsDrwn;
	}

	/**
	 * Returns {@code true} if the frames selection visual hints are currently
	 * being drawn and {@code false} otherwise.
	 */
	public boolean frameSelectionHintIsDrawn() {
		return frameSelectionHintIsDrwn;
	}

	/**
	 * Returns {@code true} if the camera key frame paths are currently being
	 * drawn and {@code false} otherwise.
	 */
	public boolean cameraPathsAreDrawn() {
		return cameraPathsAreDrwn;
	}

	/**
	 * Returns {@code true} if axis is currently being drawn and {@code false}
	 * otherwise.
	 */
	public boolean interactiveFrameIsDrawn() {
		return iFrameIsDrwn;
	}

	/**
	 * Convenience function that simply calls {@code setAxisIsDrawn(true)}
	 */
	public void setAxisIsDrawn() {
		setAxisIsDrawn(true);
	}

	/**
	 * Sets the display of the axis according to {@code draw}
	 */
	public void setAxisIsDrawn(boolean draw) {
		axisIsDrwn = draw;
	}

	/**
	 * Convenience function that simply calls {@code setGridIsDrawn(true)}
	 */
	public void setGridIsDrawn() {
		setGridIsDrawn(true);
	}

	/**
	 * Sets the display of the grid according to {@code draw}
	 */
	public void setGridIsDrawn(boolean draw) {
		gridIsDrwn = draw;
	}

	/**
	 * Sets the display of the interactive frames' selection hints according to
	 * {@code draw}
	 */
	public void setFrameSelectionHintIsDrawn(boolean draw) {
		frameSelectionHintIsDrwn = draw;
	}

	/**
	 * Sets the display of the camera key frame paths according to {@code draw}
	 */
	public void setCameraPathsAreDrawn(boolean draw) {
		cameraPathsAreDrwn = draw;
	}

	/**
	 * Convenience function that simply calls {@code setDrawInteractiveFrame(true)}.
	 * 
	 * @see #setDrawInteractiveFrame(boolean)
	 */
	public void setDrawInteractiveFrame() {
		setDrawInteractiveFrame(true);
	}

	/**
	 * Sets the interactivity to the Scene {@link #interactiveFrame()} instance
	 * according to {@code draw}
	 */
	public void setDrawInteractiveFrame(boolean draw) {
		if (draw && (glIFrame == null))
			return;
		if (!draw && (currentCameraProfile().mode() == CameraProfile.Mode.THIRD_PERSON)
				&& interactiveFrame().equals(avatar()))// more natural than to bypass it
			return;
		iFrameIsDrwn = draw;
	}
	
	/**
	 * Returns {@code true} if drawn is currently being constrained and {@code
	 * false} otherwise.
	 */
	public boolean drawIsConstrained() {
		return withConstraint;
	}

	/**
	 * Constrain frame displacements according to {@code wConstraint}
	 */
	public void setDrawWithConstraint(boolean wConstraint) {
		withConstraint = wConstraint;
	}

	// 5. Drawing methods

	/**
	 * Internal use. Display various on-screen visual hints to be called from {@link #pre()}
	 * or {@link #draw()}.
	 */
	private void displayVisualHints() {		
		if (frameSelectionHintIsDrawn())
			drawSelectionHints();
		if (cameraPathsAreDrawn()) {
			camera().drawAllPaths();
			drawCameraPathSelectionHints();
		} else {
			camera().hideAllPaths();
		}
		if (dE.camMouseAction == MouseAction.ZOOM_ON_REGION)
			drawZoomWindowHint();
		if (dE.camMouseAction == MouseAction.SCREEN_ROTATE)
			drawScreenRotateLineHint();
		if (arpFlag)
			drawArcballReferencePointHint();
		if (pupFlag) {
			PVector v = camera().projectedCoordinatesOf(pupVec);
			drawCross(v.x, v.y);
		}
	}

	/**
	 * Paint method which is called just before your {@code PApplet.draw()}
	 * method. This method is registered at the PApplet and hence you don't need
	 * to call it.
	 * <p>
	 * Sets the processing camera parameters from {@link #camera()} and updates
	 * the frustum planes equations if {@link #enableFrustumEquationsUpdate(boolean)}
	 * has been set to {@code true}.
	 */
	public void pre() {
		if (isOffscreen()) return;
		
		// handle possible resize events
		// weird: we need to bypass the handling of a resize event when running the
		// applet from eclipse		
		if ((parent.frame != null) && (parent.frame.isResizable())) {
			if ((width != pg3d.width) || (height != pg3d.height)) {
				width = pg3d.width;
				height = pg3d.height;
				// weirdly enough we need to bypass what processing does
				// to the matrices when a resize event takes place
				camera().detachFromP5Camera();
				camera().setScreenWidthAndHeight(width, height);
				camera().attachToP5Camera();
			} else {
				if ((currentCameraProfile().mode() == CameraProfile.Mode.THIRD_PERSON)
						&& (!camera().anyInterpolationIsStarted())) {
					camera().setPosition(avatar().cameraPosition());
					camera().setUpVector(avatar().upVector());
					camera().lookAt(avatar().target());
				}
				// We set the processing camera matrices from our
				// remixlab.proscene.Camera
				setPProjectionMatrix();
				setPModelViewMatrix();
				// camera().computeProjectionMatrix();
				// camera().computeModelViewMatrix();
			}
		} else {
			if ((currentCameraProfile().mode() == CameraProfile.Mode.THIRD_PERSON)
					&& (!camera().anyInterpolationIsStarted())) {
				camera().setPosition(avatar().cameraPosition());
				camera().setUpVector(avatar().upVector());
				camera().lookAt(avatar().target());
			}
			// We set the processing camera matrices from our remixlab.proscene.Camera
			setPProjectionMatrix();
			setPModelViewMatrix();
			// same as the two previous lines:
			// WARNING: this can produce visual artifacts when using OPENGL and
			// GLGRAPHICS renderers because
			// processing will anyway set the matrices at the end of the rendering
			// loop.
			// camera().computeProjectionMatrix();
			// camera().computeModelViewMatrix();
		}

		if (frustumEquationsUpdateIsEnable())
			camera().updateFrustumEquations();
	}

	/**
	 * Paint method which is called just after your {@code PApplet.draw()} method.
	 * This method is registered at the PApplet and hence you don't need to call
	 * it. Calls {@link #drawCommon()}.
	 * 
	 * @see #drawCommon()
	 */
	public void draw() {
		if (isOffscreen()) return;
		drawCommon();
	}
	
	/**
	 * Internal method. Called by {@link #draw()} and {@link #beginDraw()}.
	 * <p>
	 * First performs any scheduled animation, then calls {@link #proscenium()}
	 * which is the main drawing method that could be overloaded. Then, if
	 * there's an additional drawing method registered at the Scene, calls it (see
	 * {@link #addDrawHandler(Object, String)}). Finally, displays the
	 * {@link #displayGlobalHelp()}, the axis, the grid, the interactive frames' selection
	 * hints and camera paths, and some visual hints (such {@link #drawZoomWindowHint()},
	 * {@link #drawScreenRotateLineHint()} and {@link #drawArcballReferencePointHint()})
	 * according to user interaction and flags.
	 * 
	 * @see #proscenium()
	 * @see #addDrawHandler(Object, String)
	 * @see #gridIsDrawn()
	 * @see #axisIsDrwn
	 * @see #addDrawHandler(Object, String)
	 * @see #addAnimationHandler(Object, String)
	 */
	protected void drawCommon() {
		// 1. Animation
		if( animationIsStarted() )
			performAnimation();
		
		// 2. Alternative use only
		proscenium();

		// 3. Draw external registered method
		if (drawHandlerObject != null) {
			try {
				drawHandlerMethod.invoke(drawHandlerObject, new Object[] { this });
			} catch (Exception e) {
				PApplet.println("Something went wrong when invoking your "	+ drawHandlerMethodName + " method");
				e.printStackTrace();
			}
		}
		
		// 4. HIDevices
		for (HIDevice device : devices)
			device.handle();
		
		// 5. Grid and axis drawing
		if (gridIsDrawn())
			drawGrid(camera().sceneRadius());
		if (axisIsDrawn())
			drawAxis(camera().sceneRadius());
		
		// 6. Display visual hints
		displayVisualHints();
	}
	
	/**
	 * Returns the renderer context linked to this scene. 
	 * 
	 * @return PGraphics3D renderer.
	 */
	public PGraphics3D renderer() {
		return pg3d;
	}

	/**
	 * This method should be called when using offscreen rendering 
	 * right after renderer.beginDraw().
   */	
	public void beginDraw() {
		if (isOffscreen()) {
			if (beginOffScreenDrawingCalls != 0)
				throw new RuntimeException(
						"There should be exactly one beginDraw() call followed by a "
								+ "endDraw() and they cannot be nested. Check your implementation!");
			
			beginOffScreenDrawingCalls++;
			if ((currentCameraProfile().mode() == CameraProfile.Mode.THIRD_PERSON)
					&& (!camera().anyInterpolationIsStarted())) {
				camera().setPosition(avatar().cameraPosition());
				camera().setUpVector(avatar().upVector());
				camera().lookAt(avatar().target());
			}
			// We set the processing camera matrices from our remixlab.proscene.Camera
			setPProjectionMatrix();
			setPModelViewMatrix();

			if (frustumEquationsUpdateIsEnable())
				camera().updateFrustumEquations();	
		}
	}

	/**
	 * This method should be called when using offscreen rendering 
	 * right before renderer.endDraw(). Calls {@link #drawCommon()}.
	 * 
	 * @see #drawCommon() 
   */		
	public void endDraw() {
		beginOffScreenDrawingCalls--;
		
		if (beginOffScreenDrawingCalls != 0)
			throw new RuntimeException(
					"There should be exactly one beginDraw() call followed by a "
							+ "endDraw() and they cannot be nested. Check your implementation!");
		
		drawCommon();
	}
	
	// 4. Scene dimensions

	/**
	 * Returns the scene radius.
	 * <p>
	 * Convenience wrapper function that simply calls {@code
	 * camera().sceneRadius()}
	 * 
	 * @see #setRadius(float)
	 * @see #center()
	 */
	public float radius() {
		return camera().sceneRadius();
	}

	/**
	 * Returns the scene center.
	 * <p>
	 * Convenience wrapper function that simply returns {@code
	 * camera().sceneCenter()}
	 * 
	 * @see #setCenter(PVector) {@link #radius()}
	 */
	public PVector center() {
		return camera().sceneCenter();
	}

	/**
	 * Returns the arcball reference point.
	 * <p>
	 * Convenience wrapper function that simply returns {@code
	 * camera().arcballReferencePoint()}
	 * 
	 * @see #setCenter(PVector) {@link #radius()}
	 */
	public PVector arcballReferencePoint() {
		return camera().arcballReferencePoint();
	}

	/**
	 * Sets the {@link #radius()} of the Scene.
	 * <p>
	 * Convenience wrapper function that simply returns {@code
	 * camera().setSceneRadius(radius)}
	 * 
	 * @see #setCenter(PVector)
	 */
	public void setRadius(float radius) {
		camera().setSceneRadius(radius);
	}

	/**
	 * Sets the {@link #center()} of the Scene.
	 * <p>
	 * Convenience wrapper function that simply calls {@code }
	 * 
	 * @see #setRadius(float)
	 */
	public void setCenter(PVector center) {
		camera().setSceneCenter(center);
	}

	/**
	 * Sets the {@link #center()} and {@link #radius()} of the Scene from the
	 * {@code min} and {@code max} PVectors.
	 * <p>
	 * Convenience wrapper function that simply calls {@code
	 * camera().setSceneBoundingBox(min,max)}
	 * 
	 * @see #setRadius(float)
	 * @see #setCenter(PVector)
	 */
	public void setBoundingBox(PVector min, PVector max) {
		camera().setSceneBoundingBox(min, max);
	}

	/**
	 * Convenience wrapper function that simply calls {@code
	 * camera().showEntireScene()}
	 * 
	 * @see remixlab.proscene.Camera#showEntireScene()
	 */
	public void showAll() {
		camera().showEntireScene();
	}

	/**
	 * Convenience wrapper function that simply returns {@code
	 * camera().setArcballReferencePointFromPixel(pixel)}.
	 * <p>
	 * Current implementation set no
	 * {@link remixlab.proscene.Camera#arcballReferencePoint()}. Override
	 * {@link remixlab.proscene.Camera#pointUnderPixel(Point)} in your openGL
	 * based camera for this to work.
	 * 
	 * @see remixlab.proscene.Camera#setArcballReferencePointFromPixel(Point)
	 * @see remixlab.proscene.Camera#pointUnderPixel(Point)
	 */
	public boolean setArcballReferencePointFromPixel(Point pixel) {
		return camera().setArcballReferencePointFromPixel(pixel);
	}

	/**
	 * Convenience wrapper function that simply returns {@code
	 * camera().interpolateToZoomOnPixel(pixel)}.
	 * <p>
	 * Current implementation does nothing. Override
	 * {@link remixlab.proscene.Camera#pointUnderPixel(Point)} in your openGL
	 * based camera for this to work.
	 * 
	 * @see remixlab.proscene.Camera#interpolateToZoomOnPixel(Point)
	 * @see remixlab.proscene.Camera#pointUnderPixel(Point)
	 */
	public Camera.WorldPoint interpolateToZoomOnPixel(Point pixel) {
		return camera().interpolateToZoomOnPixel(pixel);
	}

	/**
	 * Convenience wrapper function that simply returns {@code
	 * camera().setSceneCenterFromPixel(pixel)}
	 * <p>
	 * Current implementation set no
	 * {@link remixlab.proscene.Camera#sceneCenter()}. Override
	 * {@link remixlab.proscene.Camera#pointUnderPixel(Point)} in your openGL
	 * based camera for this to work.
	 * 
	 * @see remixlab.proscene.Camera#setSceneCenterFromPixel(Point)
	 * @see remixlab.proscene.Camera#pointUnderPixel(Point)
	 */
	public boolean setCenterFromPixel(Point pixel) {
		return camera().setSceneCenterFromPixel(pixel);
	}

	/**
	 * Returns the {@link PApplet#width} to {@link PApplet#height} aspect ratio of
	 * the processing display window.
	 */
	public float aspectRatio() {
		return (float) pg3d.width / (float) pg3d.height;
	}

	// 6. Display of visual hints and Display methods
	
	/**
	 * Draws a cylinder of width {@code w} and height {@code h}, along the {@link #renderer()} 
	 * positive {@code z} axis.
	 * <p>
	 * Code adapted from http://www.processingblogs.org/category/processing-java/
	 */
	public void cylinder(float w, float h) {
		float px, py;

		pg3d.beginShape(QUAD_STRIP);
		for (float i = 0; i < 13; i++) {
			px = PApplet.cos(PApplet.radians(i * 30)) * w;
			py = PApplet.sin(PApplet.radians(i * 30)) * w;
			pg3d.vertex(px, py, 0);
			pg3d.vertex(px, py, h);
		}
		pg3d.endShape();

		pg3d.beginShape(TRIANGLE_FAN);
		pg3d.vertex(0, 0, 0);
		for (float i = 12; i > -1; i--) {
			px = PApplet.cos(PApplet.radians(i * 30)) * w;
			py = PApplet.sin(PApplet.radians(i * 30)) * w;
			pg3d.vertex(px, py, 0);
		}
		pg3d.endShape();

		pg3d.beginShape(TRIANGLE_FAN);
		pg3d.vertex(0, 0, h);
		for (float i = 0; i < 13; i++) {
			px = PApplet.cos(PApplet.radians(i * 30)) * w;
			py = PApplet.sin(PApplet.radians(i * 30)) * w;
			pg3d.vertex(px, py, h);
		}
		pg3d.endShape();
	}	
	
	/**
	 * Same as {@code cone(det, 0, 0, r, h);}
	 * 
	 * @see #cone(int, float, float, float, float)
	 */
	public void cone(int det, float r, float h) {
		cone(det, 0, 0, r, h);
	}		
	
	/**
	 * Same as {@code cone(12, 0, 0, r, h);}
	 * 
	 * @see #cone(int, float, float, float, float)
	 */
	public void cone(float r, float h) {
		cone(12, 0, 0, r, h);
	}			
	
	/**
	 * Draws a cone along the {@link #renderer()} positive {@code z} axis, with its
	 * base centered at {@code (x,y)}, height {@code h}, and radius {@code r}.
	 * <p>
	 * The code of this function was adapted from
	 * http://processinghacks.com/hacks:cone Thanks to Tom Carden.
	 * 
	 * @see #cone(int, float, float, float, float, float)
	 */
	public void cone(int detail, float x, float y, float r, float h) {
		float unitConeX[] = new float[detail + 1];
		float unitConeY[] = new float[detail + 1];

		for (int i = 0; i <= detail; i++) {
			float a1 = TWO_PI * i / detail;
			unitConeX[i] = r * (float) Math.cos(a1);
			unitConeY[i] = r * (float) Math.sin(a1);
		}

		pg3d.pushMatrix();
		pg3d.translate(x, y);
		pg3d.beginShape(TRIANGLE_FAN);
		pg3d.vertex(0, 0, h);
		for (int i = 0; i <= detail; i++) {
			pg3d.vertex(unitConeX[i], unitConeY[i], 0.0f);
		}
		pg3d.endShape();
		pg3d.popMatrix();
	}
	
	/**
	 * Same as {@code cone(det, 0, 0, r1, r2, h);}
	 * 
	 * @see #cone(int, float, float, float, float, float)
	 */
	public void cone(int det, float r1, float r2, float h) {
		cone(det, 0, 0, r1, r2, h);
	}	
	
	/**
	 * Same as {@code cone(18, 0, 0, r1, r2, h);}
	 * 
	 * @see #cone(int, float, float, float, float, float)
	 */
	public void cone(float r1, float r2, float h) {
		cone(18, 0, 0, r1, r2, h);
	}

	/**
	 * Draws a truncated cone along the {@link #renderer()} positive {@code z} axis,
	 * with its base centered at {@code (x,y)}, height {@code h}, and radii
	 * {@code r1} and {@code r2} (basis and height respectively).
	 * 
	 * @see #cone(int, float, float, float, float)
	 */
	public void cone(int detail, float x, float y,	float r1, float r2, float h) {
		float firstCircleX[] = new float[detail + 1];
		float firstCircleY[] = new float[detail + 1];
		float secondCircleX[] = new float[detail + 1];
		float secondCircleY[] = new float[detail + 1];

		for (int i = 0; i <= detail; i++) {
			float a1 = TWO_PI * i / detail;
			firstCircleX[i] = r1 * (float) Math.cos(a1);
			firstCircleY[i] = r1 * (float) Math.sin(a1);
			secondCircleX[i] = r2 * (float) Math.cos(a1);
			secondCircleY[i] = r2 * (float) Math.sin(a1);
		}

		pg3d.pushMatrix();
		pg3d.translate(x, y);
		pg3d.beginShape(QUAD_STRIP);
		for (int i = 0; i <= detail; i++) {
			pg3d.vertex(firstCircleX[i], firstCircleY[i], 0);
			pg3d.vertex(secondCircleX[i], secondCircleY[i], h);
		}
		pg3d.endShape();
		pg3d.popMatrix();
	}
	
	/**
	 * Convenience function that simply calls {@code drawAxis(100)}.
	 */
	public void drawAxis() {
		drawAxis(100);
	}		
	
	/**
	 * Draws an axis of length {@code length} which origin correspond to the
	 * {@link #renderer()}'s world coordinate system origin.
	 * 
	 * @see #drawGrid(float, int)
	 */
	public void drawAxis(float length) {
		final float charWidth = length / 40.0f;
		final float charHeight = length / 30.0f;
		final float charShift = 1.04f * length;

		// pg3d.noLights();

		pg3d.pushStyle();
		
		pg3d.beginShape(LINES);		
		pg3d.strokeWeight(2);
		// The X
		pg3d.stroke(255, 178, 178);
		pg3d.vertex(charShift, charWidth, -charHeight);
		pg3d.vertex(charShift, -charWidth, charHeight);
		pg3d.vertex(charShift, -charWidth, -charHeight);
		pg3d.vertex(charShift, charWidth, charHeight);
		// The Y
		pg3d.stroke(178, 255, 178);
		pg3d.vertex(charWidth, charShift, charHeight);
		pg3d.vertex(0.0f, charShift, 0.0f);
		pg3d.vertex(-charWidth, charShift, charHeight);
		pg3d.vertex(0.0f, charShift, 0.0f);
		pg3d.vertex(0.0f, charShift, 0.0f);
		pg3d.vertex(0.0f, charShift, -charHeight);
		// The Z
		pg3d.stroke(178, 178, 255);
		
		//left_handed
		pg3d.vertex(-charWidth, -charHeight, charShift);
		pg3d.vertex(charWidth, -charHeight, charShift);
		pg3d.vertex(charWidth, -charHeight, charShift);
		pg3d.vertex(-charWidth, charHeight, charShift);
		pg3d.vertex(-charWidth, charHeight, charShift);
		pg3d.vertex(charWidth, charHeight, charShift);
	  //right_handed coordinate system should go like this:
		//pg3d.vertex(-charWidth, charHeight, charShift);
		//pg3d.vertex(charWidth, charHeight, charShift);
		//pg3d.vertex(charWidth, charHeight, charShift);
		//pg3d.vertex(-charWidth, -charHeight, charShift);
		//pg3d.vertex(-charWidth, -charHeight, charShift);
		//pg3d.vertex(charWidth, -charHeight, charShift);
		
		pg3d.endShape();

		// Z axis
		pg3d.noStroke();
		pg3d.fill(178, 178, 255);
		drawArrow(length, 0.01f * length);

		// X Axis
		pg3d.fill(255, 178, 178);
		pg3d.pushMatrix();
		pg3d.rotateY(HALF_PI);
		drawArrow(length, 0.01f * length);
		pg3d.popMatrix();

		// Y Axis
		pg3d.fill(178, 255, 178);
		pg3d.pushMatrix();
		pg3d.rotateX(-HALF_PI);
		drawArrow(length, 0.01f * length);
		pg3d.popMatrix();

		pg3d.popStyle();
	}			
	
	/**
	 * Simply calls {@code drawArrow(length, 0.05f * length)}
	 * 
	 * @see #drawArrow(float, float)
	 */
	public void drawArrow(float length) {
		drawArrow(length, 0.05f * length);
	}		
	
	/**
	 * Draws a 3D arrow along the {@link #renderer()} positive Z axis.
	 * <p>
	 * {@code length} and {@code radius} define its geometry.
	 * <p>
	 * Use {@link #drawArrow(PVector, PVector, float)} to place the arrow
	 * in 3D.
	 */
	public void drawArrow(float length, float radius) {
		float head = 2.5f * (radius / length) + 0.1f;
		float coneRadiusCoef = 4.0f - 5.0f * head;

		cylinder(radius, length * (1.0f - head / coneRadiusCoef));
		pg3d.translate(0.0f, 0.0f, length * (1.0f - head));
		cone(coneRadiusCoef * radius, head * length);
		pg3d.translate(0.0f, 0.0f, -length * (1.0f - head));
	}		
	
	/**
	 * Draws a 3D arrow between the 3D point {@code from} and the 3D point {@code
	 * to}, both defined in the current {@link #renderer()} ModelView coordinates
	 * system.
	 * 
	 * @see #drawArrow(float, float)
	 */
	public void drawArrow(PVector from, PVector to,	float radius) {
		pg3d.pushMatrix();
		pg3d.translate(from.x, from.y, from.z);
		pg3d.applyMatrix(new Quaternion(new PVector(0, 0, 1), PVector.sub(to,
				from)).matrix());
		drawArrow(PVector.sub(to, from).mag(), radius);
		pg3d.popMatrix();
	}			
	
	/**
	 * Convenience function that simply calls {@code drawGrid(100, 10)}
	 * 
	 * @see #drawGrid(float, int)
	 */
	public void drawGrid() {
		drawGrid(100, 10);
	}	
	
	/**
	 * Convenience function that simply calls {@code drawGrid(size, 10)}
	 * 
	 * @see #drawGrid(float, int)
	 */
	public void drawGrid(float size) {
		drawGrid(size, 10);
	}
	
	/**
	 * Convenience function that simply calls {@code drawGrid(100,
	 * nbSubdivisions)}
	 * 
	 * @see #drawGrid(float, int)
	 */
	public void drawGrid(int nbSubdivisions) {
		drawGrid(100, nbSubdivisions);
	}

	/**
	 * Draws a grid in the XY plane, centered on (0,0,0) (defined in the current
	 * coordinate system).
	 * <p>
	 * {@code size} (processing scene units) and {@code nbSubdivisions} define its
	 * geometry.
	 * 
	 * @see #drawAxis(float)
	 */
	public void drawGrid(float size, int nbSubdivisions) {
		pg3d.pushStyle();
		pg3d.stroke(170, 170, 170);
		pg3d.strokeWeight(1);
		pg3d.beginShape(LINES);
		for (int i = 0; i <= nbSubdivisions; ++i) {
			final float pos = size * (2.0f * i / nbSubdivisions - 1.0f);
			pg3d.vertex(pos, -size);
			pg3d.vertex(pos, +size);
			pg3d.vertex(-size, pos);
			pg3d.vertex(size, pos);
		}
		pg3d.endShape();
		pg3d.popStyle();
	}
	
	// 2. CAMERA

	/**
	 * Convenience function that simply calls {@code drawCamera(camera,
	 * 170, true, 1.0f)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera) {
		drawCamera(camera, 170, true, 1.0f);
	}

	/**
	 * Convenience function that simply calls {@code drawCamera(camera,
	 * 170, true, scale)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera, float scale) {
		drawCamera(camera, 170, true, scale);
	}
	
	/**
	 * Convenience function that simply calls {@code drawCamera(camera,
	 * color, true, 1.0f)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera, int color) {
		drawCamera(camera, color, true, 1.0f);
	}

	/**
	 * Convenience function that simply calls {@code drawCamera(camera,
	 * 170, drawFarPlane, 1.0f)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera,	boolean drawFarPlane) {
		drawCamera(camera, 170, drawFarPlane, 1.0f);
	}

	/**
	 * Convenience function that simply calls {@code drawCamera(camera, 170, drawFarPlane, scale)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera,	boolean drawFarPlane, float scale) {
		drawCamera(camera, 170, drawFarPlane, scale);
	}

	/**
	 * Convenience function that simply calls {@code drawCamera(camera, color, true, scale)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera, int color,	float scale) {
		drawCamera(camera, color, true, scale);
	}
	
	/**
	 * Convenience function that simply calls {@code drawCamera(camera,
	 * color, drawFarPlane, 1.0f)}
	 * 
	 * @see #drawCamera(Camera, int, boolean, float)
	 */
	public void drawCamera(Camera camera, int color,	boolean drawFarPlane) {
		drawCamera(camera, color, drawFarPlane, 1.0f);
	}

	/**
	 * Draws a representation of the {@code camera} in the {@link #renderer()} 3D
	 * virtual world using {@code color}.
	 * <p>
	 * The near and far planes are drawn as quads, the frustum is drawn using
	 * lines and the camera up vector is represented by an arrow to disambiguate
	 * the drawing.
	 * <p>
	 * When {@code drawFarPlane} is {@code false}, only the near plane is drawn.
	 * {@code scale} can be used to scale the drawing: a value of 1.0 (default)
	 * will draw the Camera's frustum at its actual size.
	 * <p>
	 * <b>Note:</b> The drawing of a Scene's own Scene.camera() should not be
	 * visible, but may create artifacts due to numerical imprecisions.
	 */
	public void drawCamera(Camera camera, int color, boolean drawFarPlane, float scale) {
		pg3d.pushMatrix();

		// pg3d.applyMatrix(camera.frame().worldMatrix());
		// same as the previous line, but maybe more efficient
		tmpFrame.fromMatrix(camera.frame().worldMatrix());
		tmpFrame.applyTransformation(pg3d);

		// 0 is the upper left coordinates of the near corner, 1 for the far one
		PVector[] points = new PVector[2];
		points[0] = new PVector();
		points[1] = new PVector();

		points[0].z = scale * camera.zNear();
		points[1].z = scale * camera.zFar();

		switch (camera.type()) {
		case PERSPECTIVE: {
			points[0].y = points[0].z * PApplet.tan(camera.fieldOfView() / 2.0f);
			points[0].x = points[0].y * camera.aspectRatio();
			float ratio = points[1].z / points[0].z;
			points[1].y = ratio * points[0].y;
			points[1].x = ratio * points[0].x;
			break;
		}
		case ORTHOGRAPHIC: {
			float[] wh = camera.getOrthoWidthHeight();
			points[0].x = points[1].x = scale * wh[0];
			points[0].y = points[1].y = scale * wh[1];
			break;
		}
		}

		int farIndex = drawFarPlane ? 1 : 0;

		// Near and (optionally) far plane(s)
		pg3d.pushStyle();
		pg3d.noStroke();
		pg3d.fill(color);
		pg3d.beginShape(PApplet.QUADS);
		for (int i = farIndex; i >= 0; --i) {
			pg3d.normal(0.0f, 0.0f, (i == 0) ? 1.0f : -1.0f);
			pg3d.vertex(points[i].x, points[i].y, -points[i].z);
			pg3d.vertex(-points[i].x, points[i].y, -points[i].z);
			pg3d.vertex(-points[i].x, -points[i].y, -points[i].z);
			pg3d.vertex(points[i].x, -points[i].y, -points[i].z);
		}
		pg3d.endShape();

		// Up arrow
		float arrowHeight = 1.5f * points[0].y;
		float baseHeight = 1.2f * points[0].y;
		float arrowHalfWidth = 0.5f * points[0].x;
		float baseHalfWidth = 0.3f * points[0].x;

		// pg3d.noStroke();
		pg3d.fill(color);
		// Base
		pg3d.beginShape(PApplet.QUADS);
		
		pg3d.vertex(-baseHalfWidth, -points[0].y, -points[0].z);
		pg3d.vertex(baseHalfWidth, -points[0].y, -points[0].z);
		pg3d.vertex(baseHalfWidth, -baseHeight, -points[0].z);
		pg3d.vertex(-baseHalfWidth, -baseHeight, -points[0].z);
  	//right_handed coordinate system should go like this:
		//pg3d.vertex(-baseHalfWidth, points[0].y, -points[0].z);
		//pg3d.vertex(baseHalfWidth, points[0].y, -points[0].z);
		//pg3d.vertex(baseHalfWidth, baseHeight, -points[0].z);
		//pg3d.vertex(-baseHalfWidth, baseHeight, -points[0].z);
		
		pg3d.endShape();

		// Arrow
		pg3d.fill(color);
		pg3d.beginShape(PApplet.TRIANGLES);
		
		pg3d.vertex(0.0f, -arrowHeight, -points[0].z);
		pg3d.vertex(-arrowHalfWidth, -baseHeight, -points[0].z);
		pg3d.vertex(arrowHalfWidth, -baseHeight, -points[0].z);
  	//right_handed coordinate system should go like this:
		//pg3d.vertex(0.0f, arrowHeight, -points[0].z);
		//pg3d.vertex(-arrowHalfWidth, baseHeight, -points[0].z);
		//pg3d.vertex(arrowHalfWidth, baseHeight, -points[0].z);
		
		pg3d.endShape();

		// Frustum lines
		pg3d.stroke(color);
		pg3d.strokeWeight(2);
		switch (camera.type()) {
		case PERSPECTIVE:
			pg3d.beginShape(PApplet.LINES);
			pg3d.vertex(0.0f, 0.0f, 0.0f);
			pg3d
					.vertex(points[farIndex].x, points[farIndex].y, -points[farIndex].z);
			pg3d.vertex(0.0f, 0.0f, 0.0f);
			pg3d.vertex(-points[farIndex].x, points[farIndex].y,
					-points[farIndex].z);
			pg3d.vertex(0.0f, 0.0f, 0.0f);
			pg3d.vertex(-points[farIndex].x, -points[farIndex].y,
					-points[farIndex].z);
			pg3d.vertex(0.0f, 0.0f, 0.0f);
			pg3d.vertex(points[farIndex].x, -points[farIndex].y,
					-points[farIndex].z);
			pg3d.endShape();
			break;
		case ORTHOGRAPHIC:
			if (drawFarPlane) {
				pg3d.beginShape(PApplet.LINES);
				pg3d.vertex(points[0].x, points[0].y, -points[0].z);
				pg3d.vertex(points[1].x, points[1].y, -points[1].z);
				pg3d.vertex(-points[0].x, points[0].y, -points[0].z);
				pg3d.vertex(-points[1].x, points[1].y, -points[1].z);
				pg3d.vertex(-points[0].x, -points[0].y, -points[0].z);
				pg3d.vertex(-points[1].x, -points[1].y, -points[1].z);
				pg3d.vertex(points[0].x, -points[0].y, -points[0].z);
				pg3d.vertex(points[1].x, -points[1].y, -points[1].z);
				pg3d.endShape();
			}
		}

		pg3d.popStyle();

		pg3d.popMatrix();
	}

	// 3. KEYFRAMEINTERPOLATOR CAMERA

	public void drawKFICamera(float scale) {
		drawKFICamera(170, scale);
	}

	public void drawKFICamera(int color, float scale) {
		float halfHeight = scale * 0.07f;
		float halfWidth = halfHeight * 1.3f;
		float dist = halfHeight / PApplet.tan(PApplet.PI / 8.0f);

		float arrowHeight = 1.5f * halfHeight;
		float baseHeight = 1.2f * halfHeight;
		float arrowHalfWidth = 0.5f * halfWidth;
		float baseHalfWidth = 0.3f * halfWidth;

		// Frustum outline
		pg3d.pushStyle();

		pg3d.noFill();
		pg3d.stroke(color);
		pg3d.beginShape();
		pg3d.vertex(-halfWidth, halfHeight, -dist);
		pg3d.vertex(-halfWidth, -halfHeight, -dist);
		pg3d.vertex(0.0f, 0.0f, 0.0f);
		pg3d.vertex(halfWidth, -halfHeight, -dist);
		pg3d.vertex(-halfWidth, -halfHeight, -dist);
		pg3d.endShape();
		pg3d.noFill();
		pg3d.beginShape();
		pg3d.vertex(halfWidth, -halfHeight, -dist);
		pg3d.vertex(halfWidth, halfHeight, -dist);
		pg3d.vertex(0.0f, 0.0f, 0.0f);
		pg3d.vertex(-halfWidth, halfHeight, -dist);
		pg3d.vertex(halfWidth, halfHeight, -dist);
		pg3d.endShape();

		// Up arrow
		pg3d.noStroke();
		pg3d.fill(color);
		// Base
		pg3d.beginShape(PApplet.QUADS);
		
		pg3d.vertex(baseHalfWidth, -halfHeight, -dist);
		pg3d.vertex(-baseHalfWidth, -halfHeight, -dist);
		pg3d.vertex(-baseHalfWidth, -baseHeight, -dist);
		pg3d.vertex(baseHalfWidth, -baseHeight, -dist);
  	//right_handed coordinate system should go like this:
		//pg3d.vertex(-baseHalfWidth, halfHeight, -dist);
		//pg3d.vertex(baseHalfWidth, halfHeight, -dist);
		//pg3d.vertex(baseHalfWidth, baseHeight, -dist);
		//pg3d.vertex(-baseHalfWidth, baseHeight, -dist);
		
		pg3d.endShape();
		// Arrow
		pg3d.beginShape(PApplet.TRIANGLES);
		
		pg3d.vertex(0.0f, -arrowHeight, -dist);
		pg3d.vertex(arrowHalfWidth, -baseHeight, -dist);
		pg3d.vertex(-arrowHalfWidth, -baseHeight, -dist);
	  //right_handed coordinate system should go like this:
		//pg3d.vertex(0.0f, arrowHeight, -dist);
		//pg3d.vertex(-arrowHalfWidth, baseHeight, -dist);
		//pg3d.vertex(arrowHalfWidth, baseHeight, -dist);
		
		pg3d.endShape();

		pg3d.popStyle();
	}

	/**
	 * Draws a rectangle on the screen showing the region where a zoom operation
	 * is taking place.
	 */
	protected void drawZoomWindowHint() {
		float p1x = (float) dE.fCorner.getX();
		float p1y = (float) dE.fCorner.getY();
		float p2x = (float) dE.lCorner.getX();
		float p2y = (float) dE.lCorner.getY();
		beginScreenDrawing();
		PVector p1 = coords(new Point(p1x, p1y));
		PVector p2 = coords(new Point(p2x, p2y));
		PVector p3 = coords(new Point(p2x, p1y));
		PVector p4 = coords(new Point(p1x, p2y));
		pg3d.pushStyle();
		pg3d.stroke(255, 255, 255);
		pg3d.strokeWeight(2);
		pg3d.noFill();
		pg3d.beginShape();
		pg3d.vertex(p1.x, p1.y, p1.z);
		pg3d.vertex(p3.x, p3.y, p3.z);//p3
		pg3d.vertex(p2.x, p2.y, p2.z);
		pg3d.vertex(p4.x, p4.y, p4.z);//p4
		pg3d.endShape(CLOSE);
		pg3d.popStyle();
		endScreenDrawing();
	}

	/**
	 * Draws visual hint (a line on the screen) when a screen rotation is taking
	 * place.
	 */
	protected void drawScreenRotateLineHint() {
		float p1x = (float) dE.fCorner.getX();
		float p1y = (float) dE.fCorner.getY();
		PVector p2 = camera().projectedCoordinatesOf(arcballReferencePoint());
		beginScreenDrawing();
		PVector p1s = coords(new Point(p1x, p1y));
		PVector p2s = coords(new Point(p2.x, p2.y));
		pg3d.pushStyle();
		pg3d.stroke(255, 255, 255);
		pg3d.strokeWeight(2);
		pg3d.noFill();
		pg3d.beginShape(LINE);
		pg3d.vertex(p1s.x, p1s.y, p1s.z);
		pg3d.vertex(p2s.x, p2s.y, p2s.z);
		pg3d.endShape();
		pg3d.popStyle();
		endScreenDrawing();
	}

	/**
	 * Draws visual hint (a cross on the screen) when the
	 * {@link #arcballReferencePoint()} is being set.
	 * <p>
	 * Simply calls {@link #drawCross(float, float)} on {@code
	 * camera().projectedCoordinatesOf(arcballReferencePoint())} {@code x} and
	 * {@code y} coordinates.
	 * 
	 * @see #drawCross(float, float)
	 */
	protected void drawArcballReferencePointHint() {
		PVector p = camera().projectedCoordinatesOf(arcballReferencePoint());
		drawCross(p.x, p.y);
	}

	/**
	 * Draws all InteractiveFrames' selection regions: a shooter target
	 * visual hint of {@link remixlab.proscene.InteractiveFrame#grabsMouseThreshold()} pixels size.
	 * 
	 * <b>Attention:</b> If the InteractiveFrame is part of a Camera path draws
	 * nothing.
	 * 
	 * @see #drawCameraPathSelectionHints()
	 */
	protected void drawSelectionHints() {
		for (MouseGrabbable mg : MouseGrabberPool) {
			if(mg instanceof InteractiveFrame) {
				InteractiveFrame iF = (InteractiveFrame) mg;// downcast needed
				if (!iF.isInCameraPath()) {
					PVector center = camera().projectedCoordinatesOf(iF.position());
					if (mg.grabsMouse())
						drawShooterTarget(pg3d.color(0, 255, 0), center, (iF.grabsMouseThreshold() + 1), 2);
					else
						drawShooterTarget(pg3d.color(240, 240, 240), center, iF.grabsMouseThreshold(), 1);
				}
			}
		}
	}

	/**
	 * Draws the selection regions (a shooter target visual hint of
	 * {@link remixlab.proscene.InteractiveFrame#grabsMouseThreshold()} pixels size) of all
	 * InteractiveFrames forming part of the Camera paths.
	 * 
	 * @see #drawSelectionHints()
	 */
	protected void drawCameraPathSelectionHints() {
		for (MouseGrabbable mg : MouseGrabberPool) {
			if(mg instanceof InteractiveFrame) {
				InteractiveFrame iF = (InteractiveFrame) mg;// downcast needed
				if (iF.isInCameraPath()) {
					PVector center = camera().projectedCoordinatesOf(iF.position());
					if (mg.grabsMouse())
						drawShooterTarget(pg3d.color(0, 255, 255), center, (iF.grabsMouseThreshold() + 1), 2);
					else
						drawShooterTarget(pg3d.color(255, 255, 0), center, iF.grabsMouseThreshold(), 1);
				}
			}
		}
	}

	/**
	 * Convenience function that simply calls {@code
	 * drawPointUnderPixelHint(pg3d.color(255,255,255),px,py,15,3)}.
	 */
	public void drawCross(float px, float py) {
		drawCross(pg3d.color(255, 255, 255), px, py, 15, 3);
	}

	/**
	 * Draws a cross on the screen centered under pixel {@code (px, py)}, and edge
	 * of size {@code size}. {@code strokeWeight} defined the weight of the
	 * stroke.
	 * 
	 * @see #drawArcballReferencePointHint()
	 */
	public void drawCross(int color, float px, float py, float size, int strokeWeight) {
		beginScreenDrawing();
		PVector p1 = coords(new Point(px - size, py));
		PVector p2 = coords(new Point(px + size, py));
		PVector p3 = coords(new Point(px, py - size));
		PVector p4 = coords(new Point(px, py + size));
		pg3d.pushStyle();
		pg3d.stroke(color);
		pg3d.strokeWeight(strokeWeight);
		pg3d.noFill();
		pg3d.beginShape(LINES);
		pg3d.vertex(p1.x, p1.y, p1.z);
		pg3d.vertex(p2.x, p2.y, p2.z);
		pg3d.vertex(p3.x, p3.y, p3.z);
		pg3d.vertex(p4.x, p4.y, p4.z);
		pg3d.endShape();
		pg3d.popStyle();
		endScreenDrawing();
	}
	
	/**
	 * Convenience function that simply calls
	 * {@code drawFilledCircle(40, color, center, radius)}.
	 * 
	 * @see #drawFilledCircle(int, int, PVector, float)
	 */
	public void drawFilledCircle(int color, PVector center, float radius) {
		drawFilledCircle(40, color, center, radius);
	}

	/**
	 * Draws a filled circle using screen coordinates.
	 * 
	 * @param subdivisions
	 *          Number of triangles aproximating the circle. 
	 * @param color
	 *          Color used to fill the circle.
	 * @param center
	 *          Circle screen center.
	 * @param radius
	 *          Circle screen radius.
	 * 
	 * @see #beginScreenDrawing()
	 * @see #endScreenDrawing()
	 */	
	public void drawFilledCircle(int subdivisions, int color, PVector center, float radius) {
		float precision = TWO_PI/subdivisions;
		float x = center.x;
		float y = center.y;
		float angle, x2, y2;
		beginScreenDrawing();
		pg3d.pushStyle();
		pg3d.noStroke();
		pg3d.fill(color);
		pg3d.beginShape(TRIANGLE_FAN);
		PVector c = coords(new Point(x, y));
		pg3d.vertex(c.x, c.y, c.z);
		PVector aux = new PVector();
		for (angle = 0.0f; angle <= TWO_PI + 1.1*precision; angle += precision) {			
			x2 = x + PApplet.sin(angle) * radius;
			y2 = y + PApplet.cos(angle) * radius;
			aux.set(coords(new Point(x2, y2)));
			pg3d.vertex(aux.x, aux.y, aux.z);
		}
		pg3d.endShape();
		pg3d.popStyle();
		endScreenDrawing();
	}

	/**
	 * Draws a filled square using screen coordinates.
	 * 
	 * @param color
	 *          Color used to fill the square.
	 * @param center
	 *          Square screen center.
	 * @param edge
	 *          Square edge length.
	 * 
	 * @see #beginScreenDrawing()
	 * @see #endScreenDrawing()
	 */
	public void drawFilledSquare(int color, PVector center, float edge) {
		float x = center.x;
		float y = center.y;
		beginScreenDrawing();
		PVector p1 = coords(new Point(x - edge, y + edge));
		PVector p2 = coords(new Point(x + edge, y + edge));
		PVector p3 = coords(new Point(x + edge, y - edge));
		PVector p4 = coords(new Point(x - edge, y - edge));
		pg3d.pushStyle();
		pg3d.noStroke();
		pg3d.fill(color);
		pg3d.beginShape(QUADS);
		pg3d.vertex(p1.x, p1.y, p1.z);
		pg3d.vertex(p2.x, p2.y, p2.z);
		pg3d.vertex(p3.x, p3.y, p3.z);
		pg3d.vertex(p4.x, p4.y, p4.z);
		pg3d.endShape();
		pg3d.popStyle();
		endScreenDrawing();
	}

	/**
	 * Draws the classical shooter target on the screen.
	 * 
	 * @param color
	 *          Color of the target
	 * @param center
	 *          Center of the target on the screen
	 * @param length
	 *          Length of the target in pixels
	 * @param strokeWeight
	 *          Stroke weight
	 */
	public void drawShooterTarget(int color, PVector center, float length, int strokeWeight) {
		float x = center.x;
		float y = center.y;
		beginScreenDrawing();
		PVector p1 = coords(new Point((x - length), (y - length) + (0.6f * length)));
		PVector p2 = coords(new Point((x - length), (y - length)));
		PVector p3 = coords(new Point((x - length) + (0.6f * length), (y - length)));
		PVector p4 = coords(new Point(((x + length) - (0.6f * length)), (y - length)));
		PVector p5 = coords(new Point((x + length), (y - length)));
		PVector p6 = coords(new Point((x + length), ((y - length) + (0.6f * length))));
		PVector p7 = coords(new Point((x + length), ((y + length) - (0.6f * length))));
		PVector p8 = coords(new Point((x + length), (y + length)));
		PVector p9 = coords(new Point(((x + length) - (0.6f * length)), (y + length)));
		PVector p10 = coords(new Point(((x - length) + (0.6f * length)), (y + length)));
		PVector p11 = coords(new Point((x - length), (y + length)));
		PVector p12 = coords(new Point((x - length), ((y + length) - (0.6f * length))));
		
		pg3d.pushStyle();

		pg3d.stroke(color);
		pg3d.strokeWeight(strokeWeight);
		pg3d.noFill();

		pg3d.beginShape();
		pg3d.vertex(p1.x, p1.y, p1.z);
		pg3d.vertex(p2.x, p2.y, p2.z);
		pg3d.vertex(p3.x, p3.y, p3.z);
		pg3d.endShape();

		pg3d.beginShape();
		pg3d.vertex(p4.x, p4.y, p4.z);
		pg3d.vertex(p5.x, p5.y, p5.z);
		pg3d.vertex(p6.x, p6.y, p6.z);
		pg3d.endShape();

		pg3d.beginShape();
		pg3d.vertex(p7.x, p7.y, p7.z);
		pg3d.vertex(p8.x, p8.y, p8.z);
		pg3d.vertex(p9.x, p9.y, p9.z);
		pg3d.endShape();

		pg3d.beginShape();
		pg3d.vertex(p10.x, p10.y, p10.z);
		pg3d.vertex(p11.x, p11.y, p11.z);
		pg3d.vertex(p12.x, p12.y, p12.z);
		pg3d.endShape();

		pg3d.popStyle();
		endScreenDrawing();

		drawCross(color, center.x, center.y, 0.6f * length, strokeWeight);
	}

	/**
	 * Computes the world coordinates of an screen object so that drawing can be
	 * done directly with 2D screen coordinates.
	 * <p>
	 * All screen drawing should be enclosed between {@link #beginScreenDrawing()}
	 * and {@link #endScreenDrawing()}. Then you can just begin drawing your
	 * screen shapes (defined between {@code PApplet.beginShape()} and {@code
	 * PApplet.endShape()}).
	 * <p>
	 * <b>Note:</b> To specify a {@code (x,y)} vertex screen coordinate you should 
	 * first call {@code PVector p = coords(new Point(x, y))} then do your
	 * drawing as {@code vertex(p.x, p.y, p.z)}.
	 * <p>
	 * <b>Attention:</b> If you want your screen drawing to appear on top of your
	 * 3d scene then draw first all your 3d before doing any call to a 
	 * {@link #beginScreenDrawing()} and {@link #endScreenDrawing()} pair.  
	 * 
	 * @see #endScreenDrawing()
	 * @see #coords(Point)
	 */
	public void beginScreenDrawing() {
		if (startCoordCalls != 0)
			throw new RuntimeException("There should be exactly one beginScreenDrawing() call followed by a "
							                 + "endScreenDrawing() and they cannot be nested. Check your implementation!");
		
		startCoordCalls++;
		
		if ( pg3d.getClass() == processing.core.PGraphics3D.class ) {
		//if ( pg3d instanceof processing.core.PGraphics3D ) {
			pg3d.hint(DISABLE_DEPTH_TEST);
			pg3d.matrixMode(PROJECTION);
			pg3d.pushMatrix();
			pg3d.ortho(-width/2, width/2, -height/2, height/2, -10, 10);
			pg3d.matrixMode(MODELVIEW);
			pg3d.pushMatrix();
		  // Camera needs to be reset!
			pg3d.camera();
			zC = 0.0f;
		}
		else {
			zC = 0.1f;
		}
	}

	/**
	 * Ends screen drawing. See {@link #beginScreenDrawing()} for details.
	 * 
	 * @see #beginScreenDrawing()
	 * @see #coords(Point)
	 */
	public void endScreenDrawing() {
		startCoordCalls--;
		if (startCoordCalls != 0)
			throw new RuntimeException("There should be exactly one beginScreenDrawing() call followed by a "
							                 + "endScreenDrawing() and they cannot be nested. Check your implementation!");

		if ( pg3d.getClass() == processing.core.PGraphics3D.class ) {
			pg3d.matrixMode(PROJECTION);
			pg3d.popMatrix();
			pg3d.matrixMode(MODELVIEW);  
			pg3d.popMatrix();		  
			pg3d.hint(ENABLE_DEPTH_TEST);
		}
	}
	
	/**
	 * Computes the world coordinates of the {@code p} screen Point.
	 * <p>
	 * This method is only useful when drawing directly on screen. It should be
	 * used in conjunction with {@link #beginScreenDrawing()} and
	 * {@link #endScreenDrawing()} (which may be consulted for details).
	 * <P>
	 * The method {@link #beginScreenDrawing()} should be called before, otherwise
	 * a runtime exception is thrown.
	 * 
	 * @see #beginScreenDrawing()
	 * @see #endScreenDrawing()
	 * @see #coords(Point)
	 */
	public PVector coords(Point p) {
		if (startCoordCalls != 1)
			throw new RuntimeException("beginScreenDrawing() should be called before this method!");
		if ( pg3d.getClass() == processing.core.PGraphics3D.class )
			return new PVector(p.x, p.y, zC);
		else
			return camera().unprojectedCoordinatesOf(new PVector(p.x,p.y,zC));
	}	

	/**
	 * Called from the timer to stop displaying the point under pixel and arcball
	 * reference point visual hints.
	 */
	protected void unSetTimerFlag() {
		arpFlag = false;
		pupFlag = false;
	}

	// 7. Camera profiles

	/**
	 * Internal method that defines the default camera profiles: WHEELED_ARCBALL
	 * and FIRST_PERSON.
	 */
	private void initDefaultCameraProfiles() {
		cameraProfileMap = new HashMap<String, CameraProfile>();
		cameraProfileNames = new ArrayList<String>();
		currentCameraProfile = null;
		// register here the default profiles
		//registerCameraProfile(new CameraProfile(this, "ARCBALL", CameraProfile.Mode.ARCBALL));
		registerCameraProfile( new CameraProfile(this, "WHEELED_ARCBALL", CameraProfile.Mode.WHEELED_ARCBALL) );
		registerCameraProfile( new CameraProfile(this, "FIRST_PERSON", CameraProfile.Mode.FIRST_PERSON) );
		//setCurrentCameraProfile("ARCBALL");
		setCurrentCameraProfile("WHEELED_ARCBALL");
	}

	/**
	 * Registers a camera profile. Returns true if succeeded. If there's a
	 * registered camera profile with the same name, registration will fail. 
	 * <p>
	 * <b>Attention:</b> This method doesn't make current {@code cp}. For that call
	 * {@link #setCurrentCameraProfile(CameraProfile)}.
	 * 
	 * @param cp camera profile
	 * 
	 * @see #setCurrentCameraProfile(CameraProfile)
	 * @see #unregisterCameraProfile(CameraProfile) 
	 */
	public boolean registerCameraProfile(CameraProfile cp) {
		// if(!isCameraProfileRegistered(cp)) {
		if (cp == null)
			return false;
		if (!isCameraProfileRegistered(cp)) {
			cameraProfileNames.add(cp.name());
			cameraProfileMap.put(cp.name(), cp);
			return true;
		}
		return false;
	}

	/**
	 * Convenience function that simply returns {@code unregisterCameraProfile(cp.name())}.
	 */
	public boolean unregisterCameraProfile(CameraProfile cp) {
		return unregisterCameraProfile(cp.name());
	}

	/**
	 * Unregisters the given camera profile by its name. Returns true if succeeded.
	 * Registration will fail in two cases: no camera profile is registered under
	 * the provided name, or the camera profile is the only registered camera profile which
	 * mode is different than THIRD_PERSON.
	 * <p>
	 * The last condition above guarantees that there should always be registered at least
	 * one camera profile which mode is different than THIRD_PERSON. 
	 * 
	 * @param cp camera profile
	 * @return true if succeeded
	 */
	public boolean unregisterCameraProfile(String cp) {
		if (!isCameraProfileRegistered(cp))
			return false;

		CameraProfile cProfile = cameraProfile(cp);
		int instancesDifferentThanThirdPerson = 0;

		for (CameraProfile camProfile : cameraProfileMap.values())
			if (camProfile.mode() != CameraProfile.Mode.THIRD_PERSON)
				instancesDifferentThanThirdPerson++;

		if ((cProfile.mode() != CameraProfile.Mode.THIRD_PERSON)
				&& (instancesDifferentThanThirdPerson == 1))
			return false;

		if (isCurrentCameraProfile(cp))
			nextCameraProfile();

		if (cameraProfileNames.remove(cp)) {
			cameraProfileMap.remove(cp);
			return true;
		}

		return false;
	}

	/**
	 * Returns the camera profile which name matches the one provided.
	 * Returns null if there's no camera profile registered by this name.
	 * 
	 * @param name camera profile name
	 * @return camera profile object
	 */
	public CameraProfile cameraProfile(String name) {
		return cameraProfileMap.get(name);
	}
	
	/**
	 * Returns an array of the camera profile objects that are currently
	 * registered at the Scene.
	 */
	public CameraProfile [] getCameraProfiles() {		
		return cameraProfileMap.values().toArray(new CameraProfile[0]);
	}

	/**
	 * Returns true the given camera profile is currently registered.
	 */
	public boolean isCameraProfileRegistered(CameraProfile cp) {
		return cameraProfileMap.containsValue(cp);
	}

	/**
	 * Returns true if currently there's a camera profile registered by
	 * the given name.
	 */
	public boolean isCameraProfileRegistered(String name) {
		return cameraProfileMap.containsKey(name);
	}

	/**
	 * Returns the current camera profile object. Never null.
	 */
	public CameraProfile currentCameraProfile() {
		return currentCameraProfile;
	}

	/**
	 * Returns true if the {@link #currentCameraProfile()} matches 
	 * the one by the given name.
	 */
	boolean isCurrentCameraProfile(String cp) {
		return isCurrentCameraProfile(cameraProfileMap.get(cp));
	}

	/**
	 * Returns true if the {@link #currentCameraProfile()} matches 
	 * the one given.
	 */
	boolean isCurrentCameraProfile(CameraProfile cp) {
		return currentCameraProfile() == cp;
	}

	/**
	 * Set current the given camera profile. Returns true if succeeded.
	 * <p>
	 * Registers first the given camera profile if it is not registered.
	 */
	public boolean setCurrentCameraProfile(CameraProfile cp) {
		if (cp == null) {
			return false;
		}
		if (!isCameraProfileRegistered(cp))
			if (!registerCameraProfile(cp))
				return false;

		return setCurrentCameraProfile(cp.name());
	}
	
	/**
	 * Set current the camera profile associated to the given name.
	 * Returns true if succeeded.
	 * <p>
	 * This method triggers smooth transition animations
	 * when switching between camera profile modes.
	 */
	public boolean setCurrentCameraProfile(String cp) {
		CameraProfile camProfile = cameraProfileMap.get(cp);
		if (camProfile == null)
			return false;
		if ((camProfile.mode() == CameraProfile.Mode.THIRD_PERSON) && (avatar() == null))
			return false;
		else {
			if (camProfile.mode() == CameraProfile.Mode.THIRD_PERSON) {
				setDrawInteractiveFrame();
				setCameraType(Camera.Type.PERSPECTIVE);
				if (avatarIsInteractiveDrivableFrame)
					((InteractiveDrivableFrame) avatar()).removeFromMouseGrabberPool();
				camera().frame().updateFlyUpVector();// ?
				camera().frame().stopSpinning();
				if (avatarIsInteractiveDrivableFrame) {
					((InteractiveDrivableFrame) (avatar())).updateFlyUpVector();
					((InteractiveDrivableFrame) (avatar())).stopSpinning();
				}
				// perform small animation ;)
				if (camera().anyInterpolationIsStarted())
					camera().stopAllInterpolations();
				Camera cm = camera().clone();
				cm.setPosition(avatar().cameraPosition());
				cm.setUpVector(avatar().upVector());
				cm.lookAt(avatar().target());
				camera().interpolateTo(cm.frame());
				currentCameraProfile = camProfile;
			} else {
				camera().frame().updateFlyUpVector();
				camera().frame().stopSpinning();
				
				if(currentCameraProfile != null)
					if (currentCameraProfile.mode() == CameraProfile.Mode.THIRD_PERSON)
						camera().interpolateToFitScene();
        
				currentCameraProfile = camProfile;        
				
				setDrawInteractiveFrame(false);
				if (avatarIsInteractiveDrivableFrame)
					((InteractiveDrivableFrame) avatar()).addInMouseGrabberPool();
			}
			return true;
		}
	}

	/**
	 * Sets the next registered camera profile as current.
	 * <p>
	 * Camera profiles are ordered by their registration order.
	 */
	public void nextCameraProfile() {
		int currentCameraProfileIndex = cameraProfileNames
				.indexOf(currentCameraProfile().name());
		nextCameraProfile(++currentCameraProfileIndex);
	}

	/**
	 * Internal use. Used by {@link #nextCameraProfile()}.
	 */
	private void nextCameraProfile(int index) {
		if (!cameraProfileNames.isEmpty()) {
			if (index == cameraProfileNames.size())
				index = 0;

			if (!setCurrentCameraProfile(cameraProfileNames.get(index)))
				nextCameraProfile(++index);
			// debug:
			else
				PApplet.println("Camera profile changed to: "
						+ cameraProfileNames.get(index));
		}
	}

	// 8. Keyboard customization

	/**
	 * Parses the sketch to find if any KeyXxxx method has been implemented. If
	 * this is the case, print a warning message telling the user what to do to
	 * avoid possible conflicts with proscene.
	 * <p>
	 * The methods sought are: {@code keyPressed}, {@code keyReleased}, and
	 * {@code keyTyped}.
	 */
	protected void parseKeyXxxxMethods() {
		boolean foundKP = true;
		boolean foundKR = true;
		boolean foundKT = true;

		try {
			parent.getClass().getDeclaredMethod("keyPressed");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundKP = false;
		} catch (NoSuchMethodException e) {
			foundKP = false;
		}

		try {
			parent.getClass().getDeclaredMethod("keyReleased");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundKR = false;
		} catch (NoSuchMethodException e) {
			foundKR = false;
		}

		try {
			parent.getClass().getDeclaredMethod("keyTyped");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundKT = false;
		} catch (NoSuchMethodException e) {
			foundKT = false;
		}

		if ( (foundKP || foundKR || foundKT) && keyboardIsHandled() ) {
			// if( (foundKP || foundKR || foundKT) &&
			// (!parent.getClass().getName().equals("remixlab.proscene.Viewer")) ) {
			PApplet.println("Warning: it seems that you have implemented some KeyXxxxMethod in your sketch. You may temporarily disable proscene " +
					"keyboard handling with Scene.disableKeyboardHandling() (you can re-enable it later with Scene.enableKeyboardHandling()).");
		}
	}

	/**
	 * Returns {@code true} if the keyboard is currently being handled by proscene
	 * and {@code false} otherwise. Set keyboard handling with
	 * {@link #enableMouseHandling(boolean)}.
	 * <p>
	 * Keyboard handling is enable by default.
	 */
	public boolean keyboardIsHandled() {
		return keyboardHandling;
	}

	/**
	 * Toggles the state of {@link #keyboardIsHandled()}
	 */
	public void toggleKeyboardHandling() {
		enableKeyboardHandling(!keyboardHandling);
	}

	/**
	 * Enables or disables proscene keyboard handling according to {@code enable}
	 * 
	 * @see #keyboardIsHandled()
	 */
	public void enableKeyboardHandling(boolean enable) {
		if (enable)
			enableKeyboardHandling();
		else
			disableKeyboardHandling();
	}

	/**
	 * Enables Proscene keyboard handling.
	 * 
	 * @see #keyboardIsHandled()
	 * @see #enableMouseHandling()
	 * @see #disableKeyboardHandling()
	 */
	public void enableKeyboardHandling() {
		if( keyboardIsHandled() )
			return;
		keyboardHandling = true;
		parent.registerKeyEvent(dE);
	}

	/**
	 * Disables Proscene keyboard handling.
	 * 
	 * @see #keyboardIsHandled()
	 */
	public void disableKeyboardHandling() {
		if( !keyboardIsHandled() )
			return;
		keyboardHandling = false;
		parent.unregisterKeyEvent(dE);
	}

	/**
	 * Sets global default keyboard shortcuts and the default key-frame shortcut keys.
	 * <p>
	 * Default global keyboard shortcuts are:
	 * <p>
	 * <ul>
	 * <li><b>'a'</b>: {@link remixlab.proscene.Scene.KeyboardAction#DRAW_AXIS}.
	 * <li><b>'e'</b>: {@link remixlab.proscene.Scene.KeyboardAction#CAMERA_TYPE}.
	 * <li><b>'g'</b>: {@link remixlab.proscene.Scene.KeyboardAction#DRAW_GRID}.
	 * <li><b>'h'</b>: {@link remixlab.proscene.Scene.KeyboardAction#GLOBAL_HELP}
	 * <li><b>'H'</b>: {@link remixlab.proscene.Scene.KeyboardAction#CURRENT_CAMERA_PROFILE_HELP}
	 * <li><b>'r'</b>: {@link remixlab.proscene.Scene.KeyboardAction#EDIT_CAMERA_PATH}.
	 * <li><b>space bar</b>: {@link remixlab.proscene.Scene.KeyboardAction#CAMERA_PROFILE}.
	 * </ul> 
	 * <p>
	 * Default key-frame shortcuts keys are:
	 * <ul>
	 * <li><b>'[1..5]'</b>: Play path [1..5]. 
	 * <li><b>'CTRL'+'[1..5]'</b>: Add key-frame to path [1..5].   
	 * <li><b>'ALT'+'[1..5]'</b>: Remove path [1..5].
	 * </ul> 
	 */
	public void setDefaultShortcuts() {
		// D e f a u l t s h o r t c u t s		
		setShortcut('a', KeyboardAction.DRAW_AXIS);
		setShortcut('g', KeyboardAction.DRAW_GRID);
		setShortcut(' ', KeyboardAction.CAMERA_PROFILE);
		setShortcut('e', KeyboardAction.CAMERA_TYPE);		
		setShortcut('h', KeyboardAction.GLOBAL_HELP);
		setShortcut('H', KeyboardAction.CURRENT_CAMERA_PROFILE_HELP);
		setShortcut('r', KeyboardAction.EDIT_CAMERA_PATH);

		// K e y f r a m e s s h o r t c u t k e y s
		setAddKeyFrameKeyboardModifier(Modifier.CTRL);
		setDeleteKeyFrameKeyboardModifier(Modifier.ALT);
		setPathKey('1', 1);
		setPathKey('2', 2);
		setPathKey('3', 3);
		setPathKey('4', 4);
		setPathKey('5', 5);
	}

	/**
	 * Associates key-frame interpolator path to key. High-level version
	 * of {@link #setPathKey(Integer, Integer)}.
	 *  
	 * @param key character (internally converted to a key coded) defining the shortcut
	 * @param path key-frame interpolator path
	 * 
	 * @see #setPathKey(Integer, Integer)
	 */
	public void setPathKey(Character key, Integer path) {
		setPathKey(DesktopEvents.getVKey(key), path);
	}
	
	/**
	 * Associates key-frame interpolator path to the given virtual key. Low-level version
	 * of {@link #setPathKey(Character, Integer)}.
	 * 
	 * @param vKey shortcut
	 * @param path key-frame interpolator path
	 * 
	 * @see #setPathKey(Character, Integer)
	 */
	public void setPathKey(Integer vKey, Integer path) {
		if ( isPathKeyInUse(vKey) ) {
			Integer p = path(vKey);
			PApplet.println("Warning: overwritting path key which was previously binded to path " + p);
		}
		pathKeys.setBinding(vKey, path);
	}

	/**
	 * Returns the key-frame interpolator path associated with key. High-level version
	 * of {@link #path(Integer)}.
	 * 
	 * @param key character (internally converted to a key coded) defining the shortcut
	 * 
	 * @see #path(Integer)
	 */
	public Integer path(Character key) {
		return path(DesktopEvents.getVKey(key));
	}
	
	/**
	 * Returns the key-frame interpolator path associated with key. Low-level version
	 * of {@link #path(Character)}.
	 * 
	 * @param vKey shortcut
	 * 
	 * @see #path(Character)
	 */
	public Integer path(Integer vKey) {
		return pathKeys.binding(vKey);
	}

	/**
	 * Removes the key-frame interpolator shortcut. High-level version
	 * of {@link #removePathKey(Integer)}.
	 * 
	 * @param key character (internally converted to a key coded) defining the shortcut
	 * 
	 * @see #removePathKey(Integer)
	 */
	public void removePathKey(Character key) {
		removePathKey(DesktopEvents.getVKey(key));
	}
	
	/**
	 * Removes the key-frame interpolator shortcut. Low-level version
	 * of {@link #removePathKey(Character)}.
	 * 
	 * @param vKey shortcut
	 * 
	 * @see #removePathKey(Character)
	 */
	public void removePathKey(Integer vKey) {
		pathKeys.removeBinding(vKey);
	}
	
	/**
	 * Returns true if the given key binds a key-frame interpolator path. High-level version
	 * of {@link #isPathKeyInUse(Integer)}.
	 * 
	 * @param key character (internally converted to a key coded) defining the shortcut
	 * 
	 * @see #isPathKeyInUse(Integer)
	 */
	public boolean isPathKeyInUse(Character key) {
		return isPathKeyInUse(DesktopEvents.getVKey(key));
	}
	
	/**
	 * Returns true if the given virtual key binds a key-frame interpolator path. Low-level version
	 * of {@link #isPathKeyInUse(Character)}.
	 * 
	 * @param vKey shortcut
	 * 
	 * @see #isPathKeyInUse(Character)
	 */
	public boolean isPathKeyInUse(Integer vKey) {
		return pathKeys.isShortcutInUse(vKey);
	}

	/**
	 * Sets the modifier key needed to play the key-frame interpolator paths.
	 * 
	 * @param modifier
	 */
	public void setAddKeyFrameKeyboardModifier(Modifier modifier) {
		addKeyFrameKeyboardModifier = modifier;
	}

	/**
	 * Sets the modifier key needed to delete the key-frame interpolator paths.
	 * 
	 * @param modifier
	 */
	public void setDeleteKeyFrameKeyboardModifier(Modifier modifier) {
		deleteKeyFrameKeyboardModifier = modifier;
	}

  /**
   * Defines a global keyboard shortcut to bind the given action.
   * 
   * @param key shortcut
   * @param action keyboard action
   */
	public void setShortcut(Character key, KeyboardAction action) {
		if ( isKeyInUse(key) ) {
			KeyboardAction a = shortcut(key);
			PApplet.println("Warning: overwritting shortcut which was previously binded to " + a);
		}
		gProfile.setBinding(new KeyboardShortcut(key), action);
	}
	
  /**
   * Defines a global keyboard shortcut to bind the given action. High-level version
   * of {@link #setShortcut(Integer, Integer, KeyboardAction)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param key character (internally converted to a coded key) defining the shortcut
   * @param action keyboard action
   * 
   * @see #setShortcut(Integer, Integer, KeyboardAction)
   */
	public void setShortcut(Integer mask, Character key, KeyboardAction action) {
		setShortcut(mask, DesktopEvents.getVKey(key), action);
	}
	
  /**
   * Defines a global keyboard shortcut to bind the given action. High-level version
   * of {@link #setShortcut(Integer, Character, KeyboardAction)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param vKey coded key defining the shortcut
   * @param action keyboard action
   * 
   * @see #setShortcut(Integer, Character, KeyboardAction)
   */
	public void setShortcut(Integer mask, Integer vKey, KeyboardAction action) {
		if ( isKeyInUse(mask, vKey) ) {
			KeyboardAction a = shortcut(mask, vKey);
			PApplet.println("Warning: overwritting shortcut which was previously binded to " + a);
		}
		gProfile.setBinding(new KeyboardShortcut(mask, vKey), action);
	}

	/**
	 * Defines a global keyboard shortcut to bind the given action.
	 * 
	 * @param vKey coded key defining the shortcut
	 * @param action keyboard action
	 */
	public void setShortcut(Integer vKey, KeyboardAction action) {
		if ( isKeyInUse(vKey) ) {
			KeyboardAction a = shortcut(vKey);
			PApplet.println("Warning: overwritting shortcut which was previously binded to " + a);
		}
		gProfile.setBinding(new KeyboardShortcut(vKey), action);
	}

	/**
	 * Removes all global keyboard shortcuts.
	 */
	public void removeAllShortcuts() {
		gProfile.removeAllBindings();
	}
	
	/**
	 * Removes the global keyboard shortcut.
	 * 
	 * @param key shortcut
	 */
	public void removeShortcut(Character key) {
		gProfile.removeBinding(new KeyboardShortcut(key));
	}
	
  /**
   * Removes the global keyboard shortcut. High-level version
   * of {@link #removeShortcut(Integer, Integer)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param key character (internally converted to a coded key) defining the shortcut
   * 
   * @see #removeShortcut(Integer, Integer)
   */
	public void removeShortcut(Integer mask, Character key) {
		removeShortcut(mask, DesktopEvents.getVKey(key));
	}

	/**
   * Removes the global keyboard shortcut. Low-level version
   * of {@link #removeShortcut(Integer, Character)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param vKey virtual coded-key defining the shortcut
   * 
   * @see #removeShortcut(Integer, Character)
   */
	public void removeShortcut(Integer mask, Integer vKey) {
		gProfile.removeBinding(new KeyboardShortcut(mask, vKey));
	}

	/**
	 * Removes the global keyboard shortcut.
	 * 
	 * @param vKey virtual coded-key defining the shortcut
	 */
	public void removeShortcut(Integer vKey) {
		gProfile.removeBinding(new KeyboardShortcut(vKey));
	}
	
	/**
	 * Returns the action that is binded to the given global keyboard shortcut.
	 * 
	 * @param key shortcut
	 */
	public KeyboardAction shortcut(Character key) {
		return gProfile.binding(new KeyboardShortcut(key));
	}
	
  /**
   * Returns the action that is binded to the given global keyboard shortcut.
   * High-level version of {@link #shortcut(Integer, Integer)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param key character (internally converted to a coded key) defining the shortcut
   * 
   * @see #shortcut(Integer, Integer)
   */
	public KeyboardAction shortcut(Integer mask, Character key) {
		return shortcut(mask, DesktopEvents.getVKey(key));
	}

	/**
   * Returns the action that is binded to the given global keyboard shortcut.
   * Low-level version of {@link #shortcut(Integer, Character)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param vKey virtual coded-key defining the shortcut
   * 
   * @see #shortcut(Integer, Character)
   */
	public KeyboardAction shortcut(Integer mask, Integer vKey) {
		return gProfile.binding(new KeyboardShortcut(mask, vKey));
	}

	/**
	 * Returns the action that is binded to the given global keyboard shortcut.
	 * 
	 * @param vKey virtual coded-key defining the shortcut
	 */
	public KeyboardAction shortcut(Integer vKey) {
		return gProfile.binding(new KeyboardShortcut(vKey));
	}

	/**
	 * Returns true if the given global keyboard shortcut binds an action.
	 * 
	 * @param key shortcut
	 */
	public boolean isKeyInUse(Character key) {
		return gProfile.isShortcutInUse(new KeyboardShortcut(key));
	}
	
  /**
   * Returns true if the given global keyboard shortcut binds an action.
   * High-level version of {@link #isKeyInUse(Integer, Integer)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param key character (internally converted to a coded key) defining the shortcut
   * 
   * @see #isKeyInUse(Integer, Integer)
   */
	public boolean isKeyInUse(Integer mask, Character key) {
		return isKeyInUse(mask, DesktopEvents.getVKey(key));
	}
	
	/**
   * Returns true if the given global keyboard shortcut binds an action.
   * Low-level version of {@link #isKeyInUse(Integer, Character)}.
   * 
   * @param mask modifier mask defining the shortcut
   * @param vKey virtual coded-key defining the shortcut
   * 
   * @see #isKeyInUse(Integer, Character)
   */
	public boolean isKeyInUse(Integer mask, Integer vKey) {
		return gProfile.isShortcutInUse(new KeyboardShortcut(mask, vKey));
	}
	
	/**
	 * Returns true if the given global keyboard shortcut binds an action.
	 * 
	 * @param vKey virtual coded-key defining the shortcut
	 */
	public boolean isKeyInUse(Integer vKey) {
		return gProfile.isShortcutInUse(new KeyboardShortcut(vKey));
	}

	/**
	 * Returns true if there is a global keyboard shortcut for the given action.
	 */
	public boolean isActionBinded(KeyboardAction action) {
		return gProfile.isActionMapped(action);
	}

	/**
	 * Internal method. Handles the different global keyboard actions.
	 */
	protected void handleKeyboardAction(KeyboardAction id) {
		if( !keyboardIsHandled() )
			return;
		switch (id) {
		case DRAW_AXIS:
			toggleAxisIsDrawn();
			break;
		case DRAW_GRID:
			toggleGridIsDrawn();
			break;
		case CAMERA_PROFILE:
			nextCameraProfile();
			break;
		case CAMERA_TYPE:
			toggleCameraType();
			break;
		case CAMERA_KIND:
			toggleCameraKind();
			break;
		case ANIMATION:
			toggleAnimation();
			break;
		case ARP_FROM_PIXEL:
			if (Camera.class == camera().getClass())
				PApplet.println("Override Camera.pointUnderPixel calling gl.glReadPixels() in your own OpenGL Camera derived class. "
								+ "See the Point Under Pixel example!");
			else if (setArcballReferencePointFromPixel(new Point(parent.mouseX, parent.mouseY))) {
				arpFlag = true;
				Timer timer=new Timer();
				TimerTask timerTask = new TimerTask() {
					public void run() {
						unSetTimerFlag();
					}
				};
				timer.schedule(timerTask, 1000);
			}
			break;
		case RESET_ARP:
			camera().setArcballReferencePoint(new PVector(0, 0, 0));
			arpFlag = true;
			Timer timer=new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					unSetTimerFlag();
				}
			};
			timer.schedule(timerTask, 1000);
			break;
		case GLOBAL_HELP:
			displayGlobalHelp();
			break;
		case CURRENT_CAMERA_PROFILE_HELP:
			displayCurrentCameraProfileHelp();
			break;
		case EDIT_CAMERA_PATH:
			toggleCameraPathsAreDrawn();
			break;
		case FOCUS_INTERACTIVE_FRAME:
			toggleDrawInteractiveFrame();
			break;
		case DRAW_FRAME_SELECTION_HINT:
			toggleFrameSelectionHintIsDrawn();
			break;
		case CONSTRAIN_FRAME:
			toggleDrawInteractiveFrame();
			break;
		}
	}

	/**
	 * Internal method. Handles the different camera keyboard actions.
	 */
	protected void handleCameraKeyboardAction(CameraKeyboardAction id) {
		if( !keyboardIsHandled() )
			return;
		switch (id) {
		case INTERPOLATE_TO_ZOOM_ON_PIXEL:
			if (Camera.class == camera().getClass())
				PApplet.println("Override Camera.pointUnderPixel calling gl.glReadPixels() in your own OpenGL Camera derived class. "
								+ "See the Point Under Pixel example!");
			else {
				Camera.WorldPoint wP = interpolateToZoomOnPixel(new Point(
						parent.mouseX, parent.mouseY));
				if (wP.found) {
					pupVec = wP.point;
					pupFlag = true;
					Timer timer=new Timer();
					TimerTask timerTask = new TimerTask() {
						public void run() {
							unSetTimerFlag();
						}
					};
					timer.schedule(timerTask, 1000);
				}
			}
			break;
		case INTERPOLATE_TO_FIT_SCENE:
			camera().interpolateToFitScene();
			break;
		case SHOW_ALL:
			showAll();
			break;
		case MOVE_CAMERA_LEFT:
			camera().frame().translate(
					camera().frame().inverseTransformOf(
							new PVector(-10.0f * camera().flySpeed(), 0.0f, 0.0f)));
			break;
		case MOVE_CAMERA_RIGHT:
			camera().frame().translate(
					camera().frame().inverseTransformOf(
							new PVector(10.0f * camera().flySpeed(), 0.0f, 0.0f)));
			break;
		case MOVE_CAMERA_UP:
			camera().frame().translate(
					camera().frame().inverseTransformOf(
							new PVector(0.0f, -10.0f * camera().flySpeed(), 0.0f)));
			break;
		case MOVE_CAMERA_DOWN:
			camera().frame().translate(
					camera().frame().inverseTransformOf(
							new PVector(0.0f, 10.0f * camera().flySpeed(), 0.0f)));
			break;
		case INCREASE_ROTATION_SENSITIVITY:
			camera().setRotationSensitivity(camera().rotationSensitivity() * 1.2f);
			break;
		case DECREASE_ROTATION_SENSITIVITY:
			camera().setRotationSensitivity(camera().rotationSensitivity() / 1.2f);
			break;
		case INCREASE_CAMERA_FLY_SPEED:
			camera().setFlySpeed(camera().flySpeed() * 1.2f);
			break;
		case DECREASE_CAMERA_FLY_SPEED:
			camera().setFlySpeed(camera().flySpeed() / 1.2f);
			break;
		case INCREASE_AVATAR_FLY_SPEED:
			if (avatar() != null)
				if (avatarIsInteractiveDrivableFrame)
					((InteractiveDrivableFrame) avatar())
							.setFlySpeed(((InteractiveDrivableFrame) avatar()).flySpeed() * 1.2f);
			break;
		case DECREASE_AVATAR_FLY_SPEED:
			if (avatar() != null)
				if (avatarIsInteractiveDrivableFrame)
					((InteractiveDrivableFrame) avatar())
							.setFlySpeed(((InteractiveDrivableFrame) avatar()).flySpeed() / 1.2f);
			break;
		case INCREASE_AZYMUTH:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setAzimuth(((InteractiveAvatarFrame) avatar()).azimuth()
									+ PApplet.PI / 64);
			break;
		case DECREASE_AZYMUTH:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setAzimuth(((InteractiveAvatarFrame) avatar()).azimuth()
									- PApplet.PI / 64);
			break;
		case INCREASE_INCLINATION:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setInclination(((InteractiveAvatarFrame) avatar()).inclination()
									+ PApplet.PI / 64);
			break;
		case DECREASE_INCLINATION:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setInclination(((InteractiveAvatarFrame) avatar()).inclination()
									- PApplet.PI / 64);
			break;
		case INCREASE_TRACKING_DISTANCE:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setTrackingDistance(((InteractiveAvatarFrame) avatar())
									.trackingDistance()
									+ radius() / 50);
			break;
		case DECREASE_TRACKING_DISTANCE:
			if (avatar() != null)
				if (avatarIsInteractiveAvatarFrame)
					((InteractiveAvatarFrame) avatar())
							.setTrackingDistance(((InteractiveAvatarFrame) avatar())
									.trackingDistance()
									- radius() / 50);
			break;
		}
	}

	/**
	 * Convenience funstion that simply calls {@code displayGlobalHelp(true)}.
	 * 
	 * @see #displayGlobalHelp(boolean)
	 */
	public void displayGlobalHelp() {
		displayGlobalHelp(true);
	}
	
	/**
	 * Displays global keyboard bindings.
	 * 
	 * @param onConsole if this flag is true displays the help on console.
	 * Otherwise displays it on the applet
	 * 
	 * @see #displayGlobalHelp()
	 */
	public void displayGlobalHelp(boolean onConsole) {
		if (onConsole)
			PApplet.println(globalHelp());
		else { //on applet
			pg3d.textFont(parent.createFont("Arial", 12));
			pg3d.textMode(SCREEN);
			pg3d.fill(0,255,0);
			pg3d.textLeading(20);
			pg3d.text(globalHelp(), 10, 10, (pg3d.width-20), (pg3d.height-20));
		}
	}
	
	/**
	 * Returns a String with the global keyboard bindings.
	 * 
	 * @see #displayGlobalHelp()
	 */
	public String globalHelp() {
		String description = new String();
		description += "GLOBAL keyboard shortcuts\n";
		for (Entry<KeyboardShortcut, Scene.KeyboardAction> entry : gProfile.map.entrySet()) {			
			Character space = ' ';
			if (!entry.getKey().description().equals(space.toString())) 
				description += entry.getKey().description() + " -> " + entry.getValue().description() + "\n";
			else
				description += "space_bar" + " -> " + entry.getValue().description() + "\n";
		}
		
		for (Entry<Integer, Integer> entry : pathKeys.map.entrySet())
			description += DesktopEvents.getKeyText(entry.getKey()) + " -> plays camera path " + entry.getValue().toString() + "\n";
		description += DesktopEvents.getModifiersExText(addKeyFrameKeyboardModifier.ID) + " + one of the above keys -> adds keyframe to the camera path \n";
		description += DesktopEvents.getModifiersExText(deleteKeyFrameKeyboardModifier.ID) + " + one of the above keys -> deletes the camera path \n";
		
		return description;		
	}
	
	/**
	 * Convenience function that simply calls {@code displayCurrentCameraProfileHelp(true)}.
	 * 
	 * @see #displayCurrentCameraProfileHelp(boolean)
	 */
	public void displayCurrentCameraProfileHelp() {
		displayCurrentCameraProfileHelp(true);
	}
	
	/**
	 * Displays the {@link #currentCameraProfile()} bindings.
	 * 
	 * @param onConsole if this flag is true displays the help on console.
	 * Otherwise displays it on the applet
	 * 
	 * @see #displayCurrentCameraProfileHelp()
	 */
	public void displayCurrentCameraProfileHelp(boolean onConsole) {
		if (onConsole)
			PApplet.println(currentCameraProfileHelp());
		else { //on applet
			pg3d.textFont(parent.createFont("Arial", 12));
			pg3d.textMode(SCREEN);
			pg3d.fill(0,255,0);
			pg3d.textLeading(20);
			pg3d.text(currentCameraProfileHelp(), 10, 10, (pg3d.width-20), (pg3d.height-20));			
		}
	}
	
	/**
	 * Returns a String with the {@link #currentCameraProfile()} keyboard and mouse bindings.
	 * 
	 * @see remixlab.proscene.CameraProfile#cameraMouseBindingsDescription()
	 * @see remixlab.proscene.CameraProfile#frameMouseBindingsDescription()
	 * @see remixlab.proscene.CameraProfile#mouseClickBindingsDescription()
	 * @see remixlab.proscene.CameraProfile#keyboardShortcutsDescription()
	 * @see remixlab.proscene.CameraProfile#cameraWheelBindingsDescription()
	 * @see remixlab.proscene.CameraProfile#frameWheelBindingsDescription()
	 */
	public String currentCameraProfileHelp() {
		String description = new String();
		description += currentCameraProfile().name() + " camera profile keyboard shortcuts and mouse bindings\n";
		int index = 1;
		if( currentCameraProfile().keyboardShortcutsDescription().length() != 0 ) {
			description += index + ". " + "Keyboard shortcuts\n";
			description += currentCameraProfile().keyboardShortcutsDescription();
			index++;
		}
		if( currentCameraProfile().cameraMouseBindingsDescription().length() != 0 ) {
			description += index + ". " + "Camera mouse bindings\n";
			description += currentCameraProfile().cameraMouseBindingsDescription();
			index++;
		}
		if( currentCameraProfile().mouseClickBindingsDescription().length() != 0 ) {
			description += index + ". " + "Mouse click bindings\n";
			description += currentCameraProfile().mouseClickBindingsDescription();
			index++;
		}
		if( currentCameraProfile().frameMouseBindingsDescription().length() != 0 ) {
			description += index + ". " + "Interactive frame mouse bindings\n";
			description += currentCameraProfile().frameMouseBindingsDescription();
			index++;
		}
		if( currentCameraProfile().cameraWheelBindingsDescription().length() != 0 ) {
			description += index + ". " + "Camera mouse wheel bindings\n";
			description += currentCameraProfile().cameraWheelBindingsDescription();
			index++;
		}
		if( currentCameraProfile().frameWheelBindingsDescription().length() != 0 ) {
			description += index + ". " + "Interactive frame mouse wheel bindings\n";
			description += currentCameraProfile().frameWheelBindingsDescription();
			index++;
		}
		return description;
	}

	// 9. Mouse customization

	/**
	 * Parses the sketch to find if any mouseXxxx method has been implemented. If
	 * this is the case, print a warning message telling the user what to do to
	 * avoid possible conflicts with proscene.
	 * <p>
	 * The methods sought are: {@code mouseDragged}, {@code mouseMoved}, {@code
	 * mouseReleased}, {@code mousePressed}, and {@code mouseClicked}.
	 */
	protected void parseMouseXxxxMethods() {
		boolean foundMD = true;
		boolean foundMM = true;
		boolean foundMR = true;
		boolean foundMP = true;
		boolean foundMC = true;

		try {
			parent.getClass().getDeclaredMethod("mouseDragged");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundMD = false;
		} catch (NoSuchMethodException e) {
			foundMD = false;
		}

		try {
			parent.getClass().getDeclaredMethod("mouseMoved");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundMM = false;
		} catch (NoSuchMethodException e) {
			foundMM = false;
		}

		try {
			parent.getClass().getDeclaredMethod("mouseReleased");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundMR = false;
		} catch (NoSuchMethodException e) {
			foundMR = false;
		}

		try {
			parent.getClass().getDeclaredMethod("mousePressed");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundMP = false;
		} catch (NoSuchMethodException e) {
			foundMP = false;
		}

		try {
			parent.getClass().getDeclaredMethod("mouseClicked");
		} catch (SecurityException e) {
			e.printStackTrace();
			foundMC = false;
		} catch (NoSuchMethodException e) {
			foundMC = false;
		}

		if ( (foundMD || foundMM || foundMR || foundMP || foundMC) && mouseIsHandled() ) {			
			PApplet.println("Warning: it seems that you have implemented some mouseXxxxMethod in your sketch. You may temporarily disable proscene " +
			"mouse handling with Scene.disableMouseHandling() (you can re-enable it later with Scene.enableMouseHandling()).");
		}
	}

	/**
	 * Returns {@code true} if the mouse is currently being handled by proscene and
	 * {@code false} otherwise. Set mouse handling with
	 * {@link #enableMouseHandling(boolean)}.
	 * <p>
	 * Mouse handling is enable by default.
	 */
	public boolean mouseIsHandled() {
		return mouseHandling;
	}

	/**
	 * Toggles the state of {@link #mouseIsHandled()}
	 */
	public void toggleMouseHandling() {
		enableMouseHandling(!mouseHandling);
	}

	/**
	 * Enables or disables proscene mouse handling according to {@code enable}
	 * 
	 * @see #mouseIsHandled()
	 */
	public void enableMouseHandling(boolean enable) {
		if (enable)
			enableMouseHandling();
		else
			disableMouseHandling();
	}

	/**
	 * Enables Proscene mouse handling.
	 * 
	 * @see #mouseIsHandled()
	 * @see #disableMouseHandling()
	 * @see #enableKeyboardHandling()
	 */
	public void enableMouseHandling() {
		if( mouseIsHandled() )
			return;
		mouseHandling = true;
		parent.registerMouseEvent(dE);
	}

	/**
	 * Disables Proscene mouse handling.
	 * 
	 * 
	 * @see #mouseIsHandled()
	 */
	public void disableMouseHandling() {
		if( !mouseIsHandled() )
			return;
		mouseHandling = false;
		parent.unregisterMouseEvent(dE);
	}

	/**
	 * Internal method. Handles the different mouse click actions.
	 */
	protected void handleClickAction(ClickAction action) {
		// public enum ClickAction { NO_CLICK_ACTION, ZOOM_ON_PIXEL, ZOOM_TO_FIT,
		// SELECT, ARP_FROM_PIXEL, RESET_ARP,
		// CENTER_FRAME, CENTER_SCENE, SHOW_ALL, ALIGN_FRAME, ALIGN_CAMERA }
		if( !mouseIsHandled() )
			return;
		switch (action) {
		case NO_CLICK_ACTION:
			break;
		case ZOOM_ON_PIXEL:
			if (Camera.class == camera().getClass())
				PApplet
						.println("Override Camera.pointUnderPixel calling gl.glReadPixels() in your own OpenGL Camera derived class. "
								+ "See the Point Under Pixel example!");
			else {
				Camera.WorldPoint wP = interpolateToZoomOnPixel(new Point(
						parent.mouseX, parent.mouseY));
				if (wP.found) {
					pupVec = wP.point;
					pupFlag = true;
					Timer timer=new Timer();
					TimerTask timerTask = new TimerTask() {
						public void run() {
							unSetTimerFlag();
						}
					};
					timer.schedule(timerTask, 1000);
				}
			}
			break;
		case ZOOM_TO_FIT:
			camera().interpolateToFitScene();
			break;
		case ARP_FROM_PIXEL:
			if (Camera.class == camera().getClass())
				PApplet.println("Override Camera.pointUnderPixel calling gl.glReadPixels() in your own OpenGL Camera derived class. "
								+ "See the Point Under Pixel example!");
			else if (setArcballReferencePointFromPixel(new Point(parent.mouseX, parent.mouseY))) {
				arpFlag = true;
				Timer timer=new Timer();
				TimerTask timerTask = new TimerTask() {
					public void run() {
						unSetTimerFlag();
					}
				};
				timer.schedule(timerTask, 1000);
			}
			break;
		case RESET_ARP:
			camera().setArcballReferencePoint(new PVector(0, 0, 0));
			arpFlag = true;
			Timer timer=new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					unSetTimerFlag();
				}
			};
			timer.schedule(timerTask, 1000);
			break;
		case CENTER_FRAME:
			if (interactiveFrame() != null)
				interactiveFrame().projectOnLine(camera().position(),
						camera().viewDirection());
			break;
		case CENTER_SCENE:
			camera().centerScene();
			break;
		case SHOW_ALL:
			camera().showEntireScene();
			break;
		case ALIGN_FRAME:
			if (interactiveFrame() != null)
				interactiveFrame().alignWithFrame(camera().frame());
			break;
		case ALIGN_CAMERA:
			camera().frame().alignWithFrame(null, true);
			break;
		}
	}
	
	// Device registration
	
	/**
	 * Adds an HIDevice to the scene.
	 * 
	 * @see #removeDevice(HIDevice)
	 * @see #removeAllDevices()
	 */
	public void addDevice(HIDevice device) {
		devices.add(device);
	}
	
	/**
	 * Removes the device from the scene.
	 * 
	 * @see #addDevice(HIDevice)
	 * @see #removeAllDevices()
	 */
	public void removeDevice(HIDevice device) {
		devices.remove(device);
	}
	
	/**
	 * Removes all registered devices from the scene.
	 * 
	 * @see #addDevice(HIDevice)
	 * @see #removeDevice(HIDevice)
	 */
	public void removeAllDevices() {
		devices.clear();
	}	

	// 10. Draw method registration

	/**
	 * Attempt to add a 'draw' handler method to the Scene. The default event
	 * handler is a method that returns void and has one single Scene parameter.
	 * 
	 * @param obj
	 *          the object to handle the event
	 * @param methodName
	 *          the method to execute in the object handler class
	 * 
	 * @see #removeDrawHandler()
	 */
	public void addDrawHandler(Object obj, String methodName) {
		try {
			drawHandlerMethod = obj.getClass().getMethod(methodName, new Class[] { Scene.class });
			drawHandlerObject = obj;
			drawHandlerMethodName = methodName;
		} catch (Exception e) {
			  PApplet.println("Something went wrong when registering your " + methodName + " method");
			  e.printStackTrace();
		}
	}

	/**
	 * Unregisters the 'draw' handler method (if any has previously been added to
	 * the Scene).
	 * 
	 * @see #addDrawHandler(Object, String)
	 */
	public void removeDrawHandler() {
		drawHandlerMethod = null;
		drawHandlerObject = null;
		drawHandlerMethodName = null;
	}

	/**
	 * Returns {@code true} if the user has registered a 'draw' handler method to
	 * the Scene and {@code false} otherwise.
	 */
	public boolean hasRegisteredDrawHandler() {
		if (drawHandlerMethodName == null)
			return false;
		return true;
	}
	
	// 11. Animation
	
	/**
	 * Returns the target frame rate.
	 * 
	 * @see #setFrameRate(float, boolean)
	 */
	public float frameRate() {
		return targetFrameRate;
	}
	
	/**
	 * Convenience function that simply calls {@code setFrameRate(fRate, true)}.
	 * 
	 * @see #setFrameRate(float, boolean)
	 */
	public void setFrameRate(float fRate) {
		setFrameRate(fRate, true);
	}
	
	/**
	 * Specifies the number of frames to be displayed every second. If the processor
	 * is not fast enough to maintain the specified rate, it will not be achieved.
	 * <p>
	 * For example, the function call setFrameRate(30) will attempt to refresh 30 times
	 * a second. It is recommended to set the frame rate within setup(). If restart is {@code true}
	 * and {@link #animationIsStarted()} then {@link #restartAnimation()} is called.
	 * <p>
	 * The default rate is 60 frames per second.
	 */
	public void setFrameRate(float fRate, boolean restart) {
		targetFrameRate = fRate;
		parent.frameRate(targetFrameRate);
		if(animationIsStarted() && restart)
			restartAnimation();
	}
	
	/**
	 * Return {@code true} when the animation loop is started.
	 * <p>
	 * Proscene animation loop relies on processing drawing loop. The {@link #draw()} function will
	 * check when {@link #animationIsStarted()} and then called the animation handler method
	 * (set with {@link #addAnimationHandler(Object, String)}) or {@link #animate()} (if no handler
	 * has been added to the scene) every {@link #animationPeriod()} milliseconds. In addition,
	 * During the drawing loop, the variable {@link #animatedFrameWasTriggered} is set
   * to {@code true} each time an animated frame is triggered (and {@code false} otherwise),
   * which is useful to notify to the outside world when an animation event occurs. 
	 * <p>
	 * Be sure to call {@code loop()} before an animation is started.
	 * <p>
	 * <b>Note:</b> The drawing frame rate may be modified when {@link #startAnimation()} is called,
	 * depending on the {@link #animationPeriod()}.   
	 * <p>
	 * Use {@link #startAnimation()}, {@link #stopAnimation()} or {@link #toggleAnimation()}
	 * to change this value.
	 * 
	 * @see #startAnimation()
	 * @see #addAnimationHandler(Object, String)
	 * @see #animate()
	 */
	public boolean animationIsStarted() {
		return animationStarted;
	}
	
	/**
	 * The animation loop period, in milliseconds. When {@link #animationIsStarted()}, this is
	 * the delay that takes place between two consecutive iterations of the animation loop.
	 * <p>
	 * This delay defines a target frame rate that will only be achieved if your
	 * {@link #animate()} and {@link #draw()} methods are fast enough. If you want to know
	 * the maximum possible frame rate of your machine on a given scene,
	 * {@link #setAnimationPeriod(float)} to {@code 1}, and {@link #startAnimation()}. The display
	 * will then be updated as often as possible, and the frame rate will be meaningful.  
	 * <p>
	 * Default value is 16.6666 milliseconds (60 Hz) which matches <b>processing</b> default
	 * frame rate.
	 * <p>
	 * <b>Note:</b> This value is taken into account only the next time you call
	 * {@link #startAnimation()}. If {@link #animationIsStarted()}, you should
	 * {@link #stopAnimation()} first. See {@link #restartAnimation()} and
	 * {@link #setAnimationPeriod(float, boolean)}.
	 * 
	 * @see #setAnimationPeriod(float, boolean)
	 */
	public float animationPeriod() {
		return animationPeriod;
	}
	
	/**
	 * Convenience function that simply calls {@code setAnimationPeriod(period, true)}.
	 * 
	 * @see #setAnimationPeriod(float, boolean)
	 */
	public void setAnimationPeriod(float period) {
		setAnimationPeriod(period, true);
	}
	
	/**
	 * Sets the {@link #animationPeriod()}, in milliseconds. If restart is {@code true}
	 * and {@link #animationIsStarted()} then {@link #restartAnimation()} is called.
	 * <p>
	 * <b>Note:</b> The drawing frame rate could be modified when {@link #startAnimation()} is called
	 * depending on the {@link #animationPeriod()}.
	 * 
	 * @see #startAnimation()
	 */
	public void setAnimationPeriod(float period, boolean restart) {
		if(period>0) {
			animationPeriod = period;
			animationFrameRate = 1000f/animationPeriod;
			if(animationIsStarted() && restart)				
				restartAnimation();
		}
	}
	
	/**
	 * Stops animation.
	 * <p>
	 * <b>Warning:</b> Restores the {@code PApplet} frame rate to its default value,
	 * i.e., calls {@code parent.frameRate(60)}. 
	 * 
	 * @see #animationIsStarted()
	 */
	public void stopAnimation()	{
		animationStarted = false;
		animatedFrameWasTriggered = false;
		setFrameRate(targetFrameRate, false);
	}
	
	/**
	 * Starts the animation loop.
	 * <p>
	 * Syncs the drawing frame rate according to {@link #animationPeriod()}: If the animation
	 * frame rate (which value depends on the {@link #animationPeriod()})
	 * is higher than the current {@link #frameRate()}, the frame rate is modified to match it,
	 * i.e., each drawing frame will trigger exactly one animation event. If the animation
	 * frame rate is lower than the {@link #frameRate()}, the frame rate is left unmodified,
	 * and the animation frames will be interleaved among the drawing frames in intervals
	 * needed to achieve the target {@link #animationPeriod()} (provided that your
	 * {@link #animate()} and {@link #draw()} methods are fast enough).
	 * 
	 * @see #animationIsStarted()
	 */
	public void startAnimation() {
		animationStarted = true;		
		//sync with processing drawing method:		
		currentAnimationFrame = -1;
		animatedFrameWasTriggered = false;
		if( (animationFrameRate > targetFrameRate) )
			parent.frameRate( animationFrameRate ); //bypass setFrameRate()
		else {
			parent.frameRate( targetFrameRate ); //same as setFrameRate(targetFrameRate, false)
			initialDrawingFrameWhenAnimationStarted = parent.frameCount;
			currentAnimationFrame = 0;
			animationToFrameRateRatio = animationFrameRate/targetFrameRate;
		}
	}
	
	/**
	 * Restart the animation.
	 * <p>
	 * Simply calls {@link #stopAnimation()} and then {@link #startAnimation()}.
	 */
  public void restartAnimation() {
  	stopAnimation();
  	startAnimation();
	}
  
  /**
	 * Internal use.
	 * <p>
	 * Calls the animation handler. Calls {@link #animate()} if there's no such a handler. Sets
	 * the value of {@link #animatedFrameWasTriggered} to {@code true} or {@code false}
	 * depending on whether or not an animation event was triggered during this drawing frame
	 * (useful to notify the outside world when an animation event occurs). 
	 * 
	 * @see #animationPeriod()
	 * @see #startAnimation()
	 */
	protected void performAnimation() {		
		if( currentAnimationFrame >= 0 ) {
			long previousAnimationFrame = currentAnimationFrame;
			currentAnimationFrame = PApplet.round( (parent.frameCount - initialDrawingFrameWhenAnimationStarted) * animationToFrameRateRatio );
			if(currentAnimationFrame == previousAnimationFrame) {
				animatedFrameWasTriggered = false;
				return;
			}				
		}		
		animatedFrameWasTriggered = true;		
		if (animateHandlerObject != null) {
			try {
				animateHandlerMethod.invoke(animateHandlerObject, new Object[] { this });
			} catch (Exception e) {
				PApplet.println("Something went wrong when invoking your "	+ animateHandlerMethodName + " method");
				e.printStackTrace();
			}
		}
		else
			animate();
	}
	
	/**
	 * Scene animation method.
	 * <p>
	 * When {@link #animationIsStarted()}, this method defines how your scene evolves over time.
	 * <p>
	 * Overload it as needed. Default implementation is empty. You may
	 * {@link #addAnimationHandler(Object, String)} instead.
	 * <p>
	 * <b>Note</b> that remixlab.proscene.KeyFrameInterpolator (which regularly updates a Frame)
	 * do not use this method.
	 * 
	 * @see #addAnimationHandler(Object, String).
	 */
	public void animate() {
	}
	
	/**
	 * Calls {@link #startAnimation()} or {@link #stopAnimation()}, depending on
	 * {@link #animationIsStarted()}.
	 */
	public void toggleAnimation() {
		if (animationIsStarted()) stopAnimation(); else startAnimation();
	}
	
	/**
	 * Attempt to add an 'animation' handler method to the Scene. The default event
	 * handler is a method that returns void and has one single Scene parameter.
	 * 
	 * @param obj
	 *          the object to handle the event
	 * @param methodName
	 *          the method to execute in the object handler class
	 * 
	 * @see #animate()
	 */
	public void addAnimationHandler(Object obj, String methodName) {
		try {
			animateHandlerMethod = obj.getClass().getMethod(methodName, new Class[] { Scene.class });
			animateHandlerObject = obj;
			animateHandlerMethodName = methodName;
		} catch (Exception e) {
			  PApplet.println("Something went wrong when registering your " + methodName + " method");
			  e.printStackTrace();
		}
	}

	/**
	 * Unregisters the 'animation' handler method (if any has previously been added to
	 * the Scene).
	 * 
	 * @see #addAnimationHandler(Object, String)
	 */
	public void removeAnimationHandler() {
		animateHandlerMethod = null;
		animateHandlerObject = null;
		animateHandlerMethodName = null;
	}

	/**
	 * Returns {@code true} if the user has registered an 'animation' handler method to
	 * the Scene and {@code false} otherwise.
	 */
	public boolean hasRegisteredAnimationHandler() {
		if (animateHandlerMethodName == null)
			return false;
		return true;
	}

	// 12. Processing objects

	/**
	 * Sets the processing camera projection matrix from {@link #camera()}. Calls
	 * {@code PApplet.perspective()} or {@code PApplet.orhto()} depending on the
	 * {@link remixlab.proscene.Camera#type()}.
	 */
	protected void setPProjectionMatrix() {
		// compute the processing camera projection matrix from our camera()
		// parameters
		switch (camera().type()) {
		case PERSPECTIVE:
			pg3d.perspective(camera().fieldOfView(), camera().aspectRatio(), camera().zNear(), camera().zFar());
			break;
		case ORTHOGRAPHIC:
			float[] wh = camera().getOrthoWidthHeight();
			pg3d.ortho(-wh[0], wh[0], -wh[1], wh[1], camera().zNear(), camera().zFar());
			break;
		}
		// if our camera() matrices are detached from the processing Camera
		// matrices,
		// we cache the processing camera projection matrix into our camera()
		camera().setProjectionMatrix(pg3d.projection);
		// camera().setProjectionMatrix(((PGraphics3D) parent.g).projection);
	}

	/**
	 * Sets the processing camera matrix from {@link #camera()}. Simply calls
	 * {@code PApplet.camera()}.
	 */
	protected void setPModelViewMatrix() {
		// compute the processing camera modelview matrix from our camera()
		// parameters
		pg3d.camera(camera().position().x, camera().position().y, camera().position().z,
				        camera().at().x, camera().at().y, camera().at().z,
				        camera().upVector().x, camera().upVector().y, camera().upVector().z);
		// if our camera() matrices are detached from the processing Camera
		// matrices,
		// we cache the processing camera modelview matrix into our camera()
		camera().setModelViewMatrix(pg3d.modelview);
		// camera().setProjectionMatrix(((PGraphics3D) parent.g).modelview);
	}
}