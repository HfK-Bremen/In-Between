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

import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * An HIDevice represents a Human Interface Device with (<=) 6 degrees of freedom.
 * <p>
 * An HIDevice has a type which can be RELATIVE (default) or ABSOLUTE. A RELATIVE HIDevice
 * has a neutral position that the device holds when it is not being manipulated. An ABSOLUTE HIDevice
 * has no such neutral position. Examples of RELATIVE devices are the space navigator and the joystick,
 * examples of ABSOLUTE devices are the wii or the kinect.
 */
public class HIDevice {
	/**
	 * This enum holds the device type.
	 *
	 */
	public enum Mode {RELATIVE, ABSOLUTE}
	
	/**
	 * This enum holds the camera modes predefined by an HIDevice.
	 */	
	public enum CameraMode {
		FIRST_PERSON("FIRST_PERSON camera mode set"),
		GOOGLE_EARTH("GOOGLE_EARTH camera mode set"),
		/**WORLD("WORLD camera mode set"),*/
		CUSTOM("CUSTOM camera mode set");
    private String description;		
    CameraMode(String description) {
       this.description = description;
    }		
    public String description() {
        return description;
    }
	}
	
	/**
	 * This enum holds the interactive frame manipulation modes predefined by an HIDevice.
	 */
	public enum IFrameMode {
		FRAME("FRAME interactive frame control mode set"),
		CAMERA("CAMERA interactive frame control mode set"),
		WORLD("WORLD interactive frame control mode set"),
		CUSTOM("CUSTOM interactive frame control mode set");
		private String description;		
		IFrameMode(String description) {
       this.description = description;
    }		
    public String description() {
        return description;
    }
	}
	
	protected Mode mode;
	protected CameraMode camMode;
	protected IFrameMode iFrameMode;
	
	protected Object handlerObject;
	protected Method handlerMethod;	
	protected String handlerMethodName;
	
	protected Scene scene;
	protected Camera camera;
	protected InteractiveCameraFrame cameraFrame;
	protected InteractiveFrame iFrame;

	protected PVector rotation, rotSens;
	protected PVector translation, transSens;
	
	//absolute mode
	protected PVector prevRotation, prevTranslation;
	
	protected PVector t;
	protected Quaternion q;
	protected float tx;
  protected float ty;
  protected float tz;
	protected float roll;
  protected float pitch;
  protected float yaw;

	protected Quaternion quaternion;
	
	/**
	 * Convenience constructor that simply calls {@code this(scn, Mode.RELATIVE)}.
	 * 
	 * @param scn The Scene object this HIDevice belongs to.
	 * 
	 * @see #HIDevice(Scene, Mode)
	 */
	public HIDevice(Scene scn) {
		this(scn, Mode.RELATIVE);
	}

	/**
	 * Main constructor.
	 * 
	 * @param scn The Scene object this HIDevice belongs to.
	 * @param m The device {@link #mode()}.
	 */
	public HIDevice(Scene scn, Mode m) {
		scene = scn;
		camera = scene.camera();
		cameraFrame = camera.frame();
		iFrame = scene.interactiveFrame();
		translation = new PVector();
		transSens = new PVector(1, 1, 1);
		rotation = new PVector();
		rotSens = new PVector(1, 1, 1);		
		quaternion = new Quaternion();
		t = new PVector();
    q = new Quaternion();
    tx = translation.x * transSens.x;
    ty = translation.y * transSens.y;
    tz = translation.z * transSens.z;
  	roll = rotation.x * rotSens.x;
    pitch = rotation.y * rotSens.y;
    yaw = rotation.z * rotSens.z;
    
    setMode(m);
    setCameraMode(CameraMode.FIRST_PERSON);
    setIFrameMode(IFrameMode.CAMERA);    
	}
	
	/**
	 * Feed the HIDevice with hardware output using a controller. The controller should be implemented
	 * by the application. This method simply calls {@code feedTranslation(tx,ty,tz)} and
	 * {@code feedRotation(rx,ry,rz)}.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 * 
	 * @see #feedTranslation(float, float, float)
	 * @see #feedRotation(float, float, float)
	 */
	public void feed(float tx, float ty, float tz, float rx, float ry, float rz) {
		feedTranslation(tx,ty,tz);
		feedRotation(rx,ry,rz);
	}

	/**
	 * Defines the device translation feed.
	 * <p> 
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 * 
	 * @param x x-axis translation
	 * @param y y-axis translation
	 * @param z z-axis translation
	 * 
	 * @see #feed(float, float, float, float, float, float)
	 * @see #feedRotation(float, float, float)
	 */
	public void feedTranslation(float x, float y, float z) {
		if ( mode() == Mode.ABSOLUTE )
			prevTranslation.set(translation);
		translation.set(x, y, z);
	}
	
	/**
	 * Defines the device rotation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 * 
	 * @param x x-axis rotation
	 * @param y y-axis rotation
	 * @param z z-axis rotation
	 * 
	 * @see #feed(float, float, float, float, float, float)
	 * @see #feedTranslation(float, float, float)
	 */
	public void feedRotation(float x, float y, float z) {
		if ( mode() == Mode.ABSOLUTE )
			prevRotation.set(rotation);
		rotation.set(x, y, z);
	}
	
	/**
	 * Defines the device x-axis translation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 */
	public void feedXTranslation(float t) {
		if ( mode() == Mode.ABSOLUTE )
			prevTranslation.x = translation.x; 
		translation.x = t;
	}

	/**
	 * Defines the device y-axis translation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 */
	public void feedYTranslation(float t) {
		if ( mode() == Mode.ABSOLUTE ) {
			prevTranslation.y = translation.y; 
		}
		translation.y = t;
	}

	/**
	 * Defines the device z-axis translation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice.
	 */
	public void feedZTranslation(float t) {
		if ( mode() == Mode.ABSOLUTE )
			prevTranslation.z = translation.z; 
		translation.z = t;
	}	

	/**
	 * Defines the device x-axis rotation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice. 
	 */
	public void feedXRotation(float t) {
		if ( mode() == Mode.ABSOLUTE )
			prevRotation.x = rotation.x; 
		rotation.x = t;
	}

	/**
	 * Defines the device y-axis rotation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice. 
	 */
	public void feedYRotation(float t) {
		if ( mode() == Mode.ABSOLUTE )
			prevRotation.y = rotation.y;
		rotation.y = t;
	}

	/**
	 * Defines the device z-axis rotation feed.
	 * <p>
	 * Useful when {@link #addHandler(Object, String)} has been called on this HIDevice. 
	 */
	public void feedZRotation(float t) {
		if ( mode() == Mode.ABSOLUTE )
			prevRotation.z = rotation.z;
		rotation.z = t;
	}
	
	/**
	 * Overload this method to define the x-axis translation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedXTranslation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedXTranslation() {
		return 0;
	}

	/**
	 * Overload this method to define the y-axis translation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedYTranslation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedYTranslation() {
		return 0;
	}

	/**
	 * Overload this method to define the z-axis translation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedZTranslation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedZTranslation() {
		return 0;
	}

	/**
	 * Overload this method to define the x-axis rotation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedXRotation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedXRotation() {
		return 0;
	}

	/**
	 * Overload this method to define the y-axis rotation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedYRotation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedYRotation() {
		return 0;
	}

	/**
	 * Overload this method to define the z-axis rotation feed this method
	 * if you plan to implement your own HIDevice. Otherwise use {@link #feedZRotation(float)}
	 * and {@link #addHandler(Object, String)} to the HIDevice.
	 */
	public float feedZRotation() {
		return 0;
	}

	/**
	 * Sets the translation sensitivity.
	 * 
	 * @param sx x-axis translation sensitivity
	 * @param sy y-axis translation sensitivity
	 * @param sz z-axis translation sensitivity
	 */
	public void setTranslationSensitivity(float sx, float sy, float sz) {
		transSens.set(sx, sy, sz);
	}
	
	/**
	 * Sets the rotation sensitivity.
	 * 
	 * @param sx x-axis rotation sensitivity
	 * @param sy y-axis rotation sensitivity
	 * @param sz z-axis rotation sensitivity
	 */
	public void setRotationSensitivity(float sx, float sy, float sz) {
		rotSens.set(sx, sy, sz);
	}
	
	/**
	 * Sets the x-axis translation sensitivity.
	 */
	public void setXTranslationSensitivity(float sensitivity) {
		transSens.x = sensitivity;
	}

	/**
	 * Sets the y-axis translation sensitivity.
	 */
	public void setYTranslationSensitivity(float sensitivity) {
		transSens.y = sensitivity;
	}

	/**
	 * Sets the z-axis translation sensitivity.
	 */
	public void setZTranslationSensitivity(float sensitivity) {
		transSens.z = sensitivity;
	}	

	/**
	 * Sets the x-axis rotation sensitivity.
	 */
	public void setXRotationSensitivity(float sensitivity) {
		rotSens.x = sensitivity;
	}

	/**
	 * Sets the y-axis rotation sensitivity.
	 */
	public void setYRotationSensitivity(float sensitivity) {
		rotSens.y = sensitivity;
	}

	/**
	 * Sets the z-axis rotation sensitivity.
	 */
	public void setZRotationSensitivity(float sensitivity) {
		rotSens.z = sensitivity;
	}	
	
	/**
	 * Attempt to add a 'feed' handler method to the HIDevice. The default feed
	 * handler is a method that returns void and has one single HIDevice parameter.
	 * 
	 * @param obj the object to handle the feed
	 * @param methodName the method to execute the feed in the object handler class
	 * 
	 * @see #removeHandler()
	 */
	public void addHandler(Object obj, String methodName) {
		try {
			handlerMethod = obj.getClass().getMethod(methodName, new Class[] { HIDevice.class });
			handlerObject = obj;
			handlerMethodName = methodName;
		} catch (Exception e) {
			  PApplet.println("Something went wrong when registering your " + methodName + " method");
			  e.printStackTrace();
		}
	}
	
	/**
	 * Unregisters the 'feed' handler method (if any has previously been added to
	 * the HIDevice).
	 * 
	 * @see #addHandler(Object, String)
	 */
	public void removeHandler() {
		handlerMethod = null;
		handlerObject = null;
		handlerMethodName = null;
	}

	/**
	 * Handle the feed by properly calling {@link #handleCamera()} or {@link #handleIFrame()}.
	 */
	protected void handle() {		
		if (handlerObject != null) {
			try {
				handlerMethod.invoke(handlerObject, new Object[] { this });
			} catch (Exception e) {
				PApplet.println("Something went wrong when invoking your "	+ handlerMethodName + " method");
				e.printStackTrace();
			}
		}
		else {			
			feedXTranslation(feedXTranslation());
			feedYTranslation(feedYTranslation());
			feedZTranslation(feedZTranslation());
			feedXRotation(feedXRotation());
			feedYRotation(feedYRotation());
			feedZRotation(feedZRotation());
		}
		
		if ( mode() == Mode.ABSOLUTE ) {
			tx = (translation.x - prevTranslation.x) * transSens.x;
			ty = (translation.y - prevTranslation.y) * transSens.y;
			tz = (translation.z - prevTranslation.z) * transSens.z;
			roll = (rotation.x - prevRotation.x) * rotSens.x;
			pitch = (rotation.y - prevRotation.y) * rotSens.y;
			yaw = (rotation.z - prevRotation.z) * rotSens.z;
		}
		else {
			tx = translation.x * transSens.x;
			ty = translation.y * transSens.y;
			tz = translation.z * transSens.z;
			roll = rotation.x * rotSens.x;
			pitch = rotation.y * rotSens.y;
			yaw = rotation.z * rotSens.z;
		}
		
		if (scene.interactiveFrameIsDrawn() || (scene.mouseGrabber() != null && scene.mouseGrabber() instanceof InteractiveFrame) )
			handleIFrame();
		else
			handleCamera();
  }
	
	/**
	 * Handles the {@link remixlab.proscene.Scene#interactiveFrame()} with this HIDevice.
	 */
  protected void handleIFrame() {  	
  	switch (iFrameMode) {
		case FRAME:
			// A. Translate the iFrame      
      iFrame.translate(iFrame.inverseTransformOf(new PVector(tx,ty,-tz))); 
      // B. Rotate the iFrame 
      q.fromEulerAngles(-roll, -pitch, yaw);
      iFrame.rotate(q);
			break;
		case CAMERA:
		  // A. Translate the iFrame      
      // Transform to world coordinate system                     
      t = cameraFrame.inverseTransformOf(new PVector(tx,ty,-tz)); //same as: t = cameraFrame.orientation().rotate(new PVector(tx,ty,-tz));
      // And then down to frame
      if (iFrame.referenceFrame() != null)
        t = iFrame.referenceFrame().transformOf(t);
      iFrame.translate(t);
      // B. Rotate the iFrame
      t = camera.projectedCoordinatesOf(iFrame.position());    
      q.fromEulerAngles(roll, pitch, -yaw);
      t.set(-q.x, -q.y, -q.z);
      t = cameraFrame.orientation().rotate(t);
      t = iFrame.transformOf(t);
      q.x = t.x;
      q.y = t.y;
      q.z = t.z;
      iFrame.rotate(q);
			break;			
    case WORLD:
      // Transform to frame
    	t.set(tx,ty,-tz);
      if (iFrame.referenceFrame() != null)
        t = iFrame.referenceFrame().transformOf(t);
      iFrame.translate(t);        
      // B. Rotate the iFrame
      Quaternion qx = new Quaternion(iFrame.transformOf(new PVector(1, 0, 0)), -roll);
      Quaternion qy = new Quaternion(iFrame.transformOf(new PVector(0, 1, 0)), -pitch);     
      Quaternion qz = new Quaternion(iFrame.transformOf(new PVector(0, 0, 1)), yaw);      
      q.set(qy);
      q.multiply(qz);
      q.multiply(qx);
      iFrame.rotate(q);
			break;
    case CUSTOM:
			customIFrameHandle();
			break;
		}  	
	}

  /**
	 * Handles the {@link remixlab.proscene.Scene#camera()} with this HIDevice.
	 */
	protected void handleCamera() {
		switch (camMode) {
		case FIRST_PERSON:
   		// Translate      
      cameraFrame.translate(cameraFrame.localInverseTransformOf(new PVector(tx,ty,-tz)));
      // Rotate
      q.fromEulerAngles(-roll, -pitch, yaw);
      cameraFrame.rotate(q);
			break;
		case GOOGLE_EARTH:
			t = PVector.mult(cameraFrame.position(), -tz * ( rotSens.z/transSens.z ) );
      cameraFrame.translate(t);

      q.fromEulerAngles(-ty * ( rotSens.y/transSens.y ), tx * ( rotSens.x/transSens.x ), 0);
      cameraFrame.rotateAroundPoint(q, scene.camera().arcballReferencePoint());

      q.fromEulerAngles(0, 0, yaw);
      cameraFrame.rotateAroundPoint(q, scene.camera().arcballReferencePoint());

      q.fromEulerAngles(-roll, 0, 0);
      cameraFrame.rotate(q);
			break;
			/**
		case WORLD:
		  // Translate          
      cameraFrame.translate(new PVector(tx,ty,tz));      
      // Rotate (same as q.fromEulerAngles, but axes are expressed int the world coordinate system)            
      Quaternion qx = new Quaternion(cameraFrame.transformOf(new PVector(1, 0, 0)), -roll);
      Quaternion qy = new Quaternion(cameraFrame.transformOf(new PVector(0, 1, 0)), -pitch);     
      Quaternion qz = new Quaternion(cameraFrame.transformOf(new PVector(0, 0, 1)), yaw);      
      q.set(qy);
      q.multiply(qz);
      q.multiply(qx);
      cameraFrame.rotate(q);
			break;
			*/
		case CUSTOM:
			customCameraHandle();
			break;
		}
	}
	
	/**
	 * Sets the next camera mode.
	 */
	public void nextCameraMode() {
		switch (camMode) {
		case FIRST_PERSON:
			setCameraMode(CameraMode.GOOGLE_EARTH);
			break;
		case GOOGLE_EARTH:
			if (HIDevice.class == this.getClass())
				setCameraMode(CameraMode.FIRST_PERSON);
			else
				setCameraMode(CameraMode.CUSTOM);
			break;
		/**
		case GOOGLE_EARTH:
			setCameraMode(CameraMode.WORLD);
			break;
		case WORLD:
			if (HIDevice.class == this.getClass())
				setCameraMode(CameraMode.FIRST_PERSON);
			else
				setCameraMode(CameraMode.CUSTOM);
			break;
		*/
		case CUSTOM:
			setCameraMode(CameraMode.FIRST_PERSON);
			break;
		}
	}
	
	/**
	 * Sets the previous camera mode.
	 */
  public void previousCameraMode() {  	
  	switch (camMode) {
  	case FIRST_PERSON:
			if (HIDevice.class == this.getClass())
				setCameraMode(CameraMode.GOOGLE_EARTH);
			else
				setCameraMode(CameraMode.CUSTOM);
			break;
		case GOOGLE_EARTH:
			setCameraMode(CameraMode.FIRST_PERSON);
			break;
		case CUSTOM:
			setCameraMode(CameraMode.GOOGLE_EARTH);
			break;
		}
  	/**
		case FIRST_PERSON:
			if (HIDevice.class == this.getClass())
				setCameraMode(CameraMode.WORLD);
			else
				setCameraMode(CameraMode.CUSTOM);
			break;
		case GOOGLE_EARTH:
			setCameraMode(CameraMode.FIRST_PERSON);
			break;
		case WORLD:
			setCameraMode(CameraMode.GOOGLE_EARTH);
			break;
		case CUSTOM:
			setCameraMode(CameraMode.WORLD);
			break;
		}
		*/
	}
  
  /**
   * Returns the current camera mode.
   */
  public CameraMode cameraMode() {
  	return camMode;
  }
  
  /**
   * Sets the camera handle mode.
   */
  public void setCameraMode(CameraMode cMode) {
  	camMode = cMode;
  	if( camMode == CameraMode.GOOGLE_EARTH )
  		camera.interpolateToFitScene(); 
  	PApplet.println( camMode.description() );
  }
  
  /**
   * Sets the next interactive frame manipulation mode.
   */
  public void nextIFrameMode() {  	
  	switch (iFrameMode) {
		case FRAME:
			setIFrameMode(IFrameMode.CAMERA);
			break;
		case CAMERA:
			setIFrameMode(IFrameMode.WORLD);
			break;
		case WORLD:
			if (HIDevice.class == this.getClass())
				setIFrameMode(IFrameMode.FRAME);
			else
				setIFrameMode(IFrameMode.CUSTOM);
			break;
		case CUSTOM:
			setIFrameMode(IFrameMode.FRAME);
			break;
		}
  }
  
  /**
   * Sets the previous interactive frame manipulation mode.
   */
  public void previousIFrameMode() {  	
  	switch (iFrameMode) {
		case FRAME:
			if (HIDevice.class == this.getClass())
				setIFrameMode(IFrameMode.WORLD);
			else
				setIFrameMode(IFrameMode.CUSTOM);
			break;
		case CAMERA:
			setIFrameMode(IFrameMode.FRAME);
			break;
		case WORLD:
			setIFrameMode(IFrameMode.CAMERA);
			break;
		case CUSTOM:
			setIFrameMode(IFrameMode.WORLD);
			break;
		}
  }
  
  /**
   * Returns the current interactive frame manipulation mode.
   */
  public IFrameMode iFrameModeMode() {
  	return iFrameMode;
  }
  
  /**
   * Sets the interactive frame manipulation mode.
   */
  public void setIFrameMode(IFrameMode iMode) {
  	iFrameMode = iMode; 
  	PApplet.println( iFrameMode.description() );
  }
  
  /**
   * Sets the device type.
   */
  public void setMode(Mode m) {
  	if(m == Mode.ABSOLUTE) {
  		if(prevTranslation == null)
  			prevTranslation = new PVector();
  		if(prevRotation == null)
  			prevRotation = new PVector();
    }
  	mode = m;
  }
  
  /**
   * Return the device type.
   */
  public Mode mode() {
  	return mode;
  }
	
  /**
   * Overload this method in your HIDevice derived class if you plan to define your own camera handle method.
   * Default implementation is empty.
   */
	protected void customCameraHandle() {}
	
	/**
   * Overload this method in your HIDevice derived class if you plan to define your own
   * interactive frame handle method. Default implementation is empty.
   */
  protected void customIFrameHandle() {}
}