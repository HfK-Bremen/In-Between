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


/**
 * Interface for objects that grab mouse focus in a Scene.
 * <p>
 * MouseGrabber are objects which react to the mouse cursor, usually when it
 * hovers over them.
 * <p>
 * <h3>How does it work ?</h3>
 * All the created MouseGrabbers are grouped in a mouse grabber pool. The Scene
 * parses this pool, calling all the MouseGrabbers'
 * {@link #checkIfGrabsMouse(int, int, Camera)} methods that
 * {@link #setGrabsMouse(boolean)} if desired (method calls should actually be
 * performed on concrete class instances such as InteractiveFrame).
 * <p>
 * When a MouseGrabber {@link #grabsMouse()}, it becomes the
 * {@link remixlab.proscene.Scene#mouseGrabber()}. All the mouse events are then
 * transmitted to it instead of being normally processed. This continues while
 * {@link #grabsMouse()} (updated using
 * {@link #checkIfGrabsMouse(int, int, Camera)}) returns {@code true}.
 * <p>
 * If you want to (temporarily) disable a specific MouseGrabbers, you can remove
 * it from this pool using
 * {@link remixlab.proscene.Scene#removeFromMouseGrabberPool(MouseGrabbable)}.
 */
public interface MouseGrabbable {
	/**
	 * Called by the Scene before it tests if the MouseGrabber
	 * {@link #grabsMouse()}. Should {@link #setGrabsMouse(boolean)} according to
	 * the mouse position.
	 * <p>
	 * This is the core method of the MouseGrabber. Its goal is to update the
	 * {@link #grabsMouse()} flag according to the mouse and MouseGrabber current
	 * positions, using {@link #setGrabsMouse(boolean)}.
	 * <p>
	 * {@link #grabsMouse()} is usually set to {@code true} when the mouse cursor
	 * is close enough to the MouseGrabber position. It should also be set to
	 * {@code false} when the mouse cursor leaves this region in order to release
	 * the mouse focus.
	 * <p>
	 * {@code x} and {@code y} are the mouse cursor coordinates ((0,0) corresponds
	 * to the upper left corner).
	 * <p>
	 * A typical implementation will look like:
	 * <p>
	 * {@code // (posX,posY) is the position of the MouseGrabber on screen.} <br>
	 * {@code // Here, distance to mouse must be less than 10 pixels to activate
	 * the MouseGrabber.} <br>
	 * {@code setGrabsMouse( PApplet.sqrt((x-posX)*(x-posX) + (y-posY)*(y-posY)) <
	 * 10);} <br>
	 * <p>
	 * If the MouseGrabber position is defined in 3D, use the {@code camera}
	 * parameter, corresponding to the calling Scene Camera. Project on screen and
	 * then compare the projected coordinates:
	 * <p>
	 * {@code PVector proj = new PVector
	 * (camera.projectedCoordinatesOf(myMouseGrabber.frame().position());} <br>
	 * {@code setGrabsMouse((PApplet.abs(x-proj.x) < 5) && (PApplet.(y-proj.y) <
	 * 2)); // Rectangular region} <br>
	 */
	void checkIfGrabsMouse(int x, int y, Camera camera);

	/**
	 * Should return true when the MouseGrabbable grabs the Scene mouse events.
	 */
	boolean grabsMouse();
	
	/**
	 * Should sets the {@link #grabsMouse()} flag. Normally used by
	 * {@link #checkIfGrabsMouse(int, int, Camera)}.
	 *  
	 * @param grabs flag
	 */
	void setGrabsMouse(boolean grabs);
	
	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and a mouse button is clicked.
	 */
	void mouseClicked(/**Point eventPoint,*/ Scene.Button button, int numberOfClicks, Camera camera);

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and a
	 * mouse button is pressed.
	 * <p>
	 * The MouseGrabber will typically start an action or change its state when a
	 * mouse button is pressed. {@link #mouseDragged(Point, Camera)} (called at
	 * each mouse displacement) will then update the MouseGrabber accordingly and
	 * {@link #mouseReleased(Point, Camera)} (called when the mouse button is
	 * released) will terminate this action.
	 */
	void mousePressed(Point eventPoint, Camera camera);

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and the
	 * mouse is moved while a button is pressed.
	 * <p>
	 * This method will typically update the state of the MouseGrabber from the
	 * mouse displacement. See the {@link #mousePressed(Point, Camera)}
	 * documentation for details.
	 */
	void mouseDragged(Point eventPoint, Camera camera);

	/**
	 * Mouse release event callback method.
	 * 
	 * @see #mousePressed(Point, Camera)
	 */
	void mouseReleased(Point eventPoint, Camera camera);

	/**
	 * Callback method called when the MouseGrabber {@link #grabsMouse()} and the
	 * mouse wheel is used.
	 */
	void mouseWheelMoved(int rotation, Camera camera);
}
