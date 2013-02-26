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

import processing.core.PVector;

/**
 * Interface for objects that are to be tracked by a proscene Camera when its
 * mode is THIRD_PERSON.
 * <p>
 * <h3>How does it work ?</h3>
 * All objects that are to be tracked by the
 * {@link remixlab.proscene.Scene#camera()} (known as avatars) should implement
 * this interface. To setup an avatar you should then call
 * {@link remixlab.proscene.Scene#setAvatar(Trackable)}. The avatar will be
 * tracked by the {@link remixlab.proscene.Scene#camera()} when
 * {@link remixlab.proscene.Scene#currentCameraProfile()} is an instance of
 * ThirdPersonCameraProfile.
 */

public interface Trackable {
	/**
	 * Returns the position of the tracking Camera in the world coordinate system.
	 * 
	 * @return PVector holding the camera position defined in the world coordinate
	 *         system.
	 */
	public PVector cameraPosition();

	/**
	 * Returns the vector to be set as the
	 * {@link remixlab.proscene.Camera#upVector()}.
	 * 
	 * @return PVector holding the camera up-vector defined in the world
	 *         coordinate system.
	 */
	public PVector upVector();

	/**
	 * Returns the target point to be set as the
	 * {@link remixlab.proscene.Camera#lookAt(PVector)}.
	 * 
	 * @return PVector holding the camera look-at vector defined in the world
	 *         coordinate system.
	 */
	public PVector target();

	/**
	 * Computes the camera position according to some specific InteractiveFrame
	 * parameters which depends on the type of interaction that is to be
	 * implemented.
	 * <p>
	 * It is responsibility of the object implementing this interface to update
	 * the camera position by properly calling this method.
	 */
	public void computeCameraPosition();
}
