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

import remixlab.proscene.Scene.Button;

/**
 * Base class for mouse grabbers objects that implements the MouseGrabbable interface.
 * <p>
 * If you want to implement your own MouseGrabber objects you should derive from this
 * class (instead of implementing the MouseGrabbable interface), and implement the
 * {@link #checkIfGrabsMouse(int, int, Camera)} method and some of the provided
 * callback methods, such {@link #mouseClicked(Scene.Button, int, Camera)} and/or
 * {@link #mouseWheelMoved(int, Camera)}. 
 * <p>
 * <b>Note:</b> The InteractiveFrame object implements the MouseGrabbable interface.
 */
public class MouseGrabber implements MouseGrabbable {
	protected Scene scene;
	protected boolean grabsMouse;
	protected boolean keepsGrabbingMouse;
	
	/**
	 * The constructor takes a scene instance and
	 * {@link remixlab.proscene.Scene#addInMouseGrabberPool(MouseGrabbable)} this MouseGrabber object.
	 * 
	 * @param scn Scene instance
	 */
	public MouseGrabber(Scene scn) {
		scene = scn;
		grabsMouse = false;
		keepsGrabbingMouse = false;
		scene.addInMouseGrabberPool(this);		
	}
	
	/**
	 * Main class method. Current implementation is empty.
	 * 
	 * @see remixlab.proscene.MouseGrabbable#checkIfGrabsMouse(int, int, Camera)
	 */
	public void checkIfGrabsMouse(int x, int y, Camera camera) { }

	/**
	 * Returns true when the MouseGrabbable grabs the Scene mouse events.
	 */
	public boolean grabsMouse() {
		return grabsMouse;
	}

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and a mouse button is clicked.
	 * <p>
	 * Current implementation is empty.
	 */
	public void mouseClicked(Button button, int numberOfClicks, Camera camera) { }

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and the
	 * mouse is moved while a button is pressed.
	 * <p>
	 * Current implementation is empty.
	 */
	public void mouseDragged(Point eventPoint, Camera camera) { }

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and a
	 * mouse button is pressed. Once a mouse grabber grabs the mouse and the mouse is
	 * pressed the default implementation will return that the mouse grabber
	 * keepsGrabbingMouse even if the mouse grabber loses focus.
	 * <p>
	 * The previous behavior is useful when you are planning to implement a mouse
	 * pressed event followed by mouse released event, e.g., 
	 * <p>
	 * The body of your {@code mousePressed(Point pnt, Camera cam)} method should look like: <br>
	 * {@code   super.mousePressed(pnt, cam); //sets the class variable keepsGrabbingMouse to true} <br>
	 * {@code   myMousePressedImplementation;} <br>
	 * {@code   ...} <br>
	 * <p>
	 * The body of your {@code mouseReleased(Point pnt, Camera cam)} method should look like: <br>
	 * {@code   super.mouseReleased(pnt, cam); //sets the class variable keepsGrabbingMouse to false} <br>
	 * {@code   myMouseReleasedImplementation;} <br>
	 * {@code   ...} <br>
	 * <p>
	 * Finally, the body of your {@code checkIfGrabsMouse(int x, int y, Camera camera)} method should look like: <br>
	 * {@code   setGrabsMouse( keepsGrabbingMouse	|| myCheckCondition); //note the use of the class variable keepsGrabbingMouse} <br>
	 * {@code   ...} <br>
	 * 
	 * @see #mouseReleased(Point, Camera)
	 */
	public void mousePressed(Point eventPoint, Camera camera) {
		if (grabsMouse())
			keepsGrabbingMouse = true;
	}

	/**
	 * Mouse release event callback method.
	 * <p>
	 */
	public void mouseReleased(Point eventPoint, Camera camera) {
		keepsGrabbingMouse = false;
	}

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and the
	 * mouse wheel is used.
	 * <p>
	 * Current implementation is empty.
	 */
	public void mouseWheelMoved(int rotation, Camera camera) { }

	/**
	 * Sets the {@link #grabsMouse()} flag. Normally used by
	 * {@link #checkIfGrabsMouse(int, int, Camera)}.
	 *  
	 * @param grabs flag
	 */
	public void setGrabsMouse(boolean grabs) {
		grabsMouse = grabs;
	}
}