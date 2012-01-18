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

/**
 * The InteractiveAvatarFrame class represents an InteractiveDrivableFrame that
 * can be tracked by a Camera, i.e., it implements the Trackable interface.
 * <p>
 * The {@link #cameraPosition()} of the camera that is to be tracking the frame
 * (see the documentation of the Trackable interface) is defined in spherical
 * coordinates ({@link #azimuth()}, {@link #inclination()} and
 * {@link #trackingDistance()}) respect to the {@link #position()} (which
 * defines its {@link #target()}) of the InteractiveAvatarFrame.
 */
public class InteractiveAvatarFrame extends InteractiveDrivableFrame implements
		Trackable {
	private Quaternion q;
	private float trackingDist;
	private PVector camRelPos;

	/**
	 * Constructs an InteractiveAvatarFrame and sets its
	 * {@link #trackingDistance()} to {@link remixlab.proscene.Scene#radius()}/5,
	 * {@link #azimuth()} to 0, and {@link #inclination()} to 0.
	 * 
	 * @see remixlab.proscene.Scene#setAvatar(Trackable)
	 * @see remixlab.proscene.Scene#setInteractiveFrame(InteractiveFrame)
	 */
	public InteractiveAvatarFrame(Scene scn) {
		super(scn);
		q = new Quaternion();
		q.fromTaitBryan(PApplet.QUARTER_PI, 0, 0);
		camRelPos = new PVector();
		setTrackingDistance(scene.radius() / 5);
	}

	/**
	 * Returns the distance between the frame and the tracking camera.
	 */
	public float trackingDistance() {
		return trackingDist;
	}

	/**
	 * Sets the distance between the frame and the tracking camera.
	 */
	public void setTrackingDistance(float d) {
		trackingDist = d;
		computeCameraPosition();
	}

	/**
	 * Returns the azimuth of the tracking camera measured respect to the frame's
	 * {@link #zAxis()}.
	 */
	public float azimuth() {
		// azimuth <-> pitch
		return q.taitBryanAngles().y;
	}

	/**
	 * Sets the {@link #azimuth()} of the tracking camera.
	 */
	public void setAzimuth(float a) {
		float roll = q.taitBryanAngles().x;
		q.fromTaitBryan(roll, a, 0);
		computeCameraPosition();
	}

	/**
	 * Returns the inclination of the tracking camera measured respect to the
	 * frame's {@link #yAxis()}.
	 */
	public float inclination() {
		// inclination <-> roll
		return q.taitBryanAngles().x;
	}

	/**
	 * Sets the {@link #inclination()} of the tracking camera.
	 */
	public void setInclination(float i) {
		float pitch = q.taitBryanAngles().y;
		q.fromTaitBryan(i, pitch, 0);
		computeCameraPosition();
	}

	// Interface implementation

	/**
	 * Overloading of {@link remixlab.proscene.Trackable#cameraPosition()}.
	 * Returns the world coordinates of the camera position computed in
	 * {@link #computeCameraPosition()}.
	 */
	public PVector cameraPosition() {
		return inverseCoordinatesOf(camRelPos);
	}

	/**
	 * Overloading of {@link remixlab.proscene.Trackable#upVector()}. Simply
	 * returns the frame {@link #yAxis()}.
	 */
	public PVector upVector() {
		return yAxis();
	}

	/**
	 * Overloading of {@link remixlab.proscene.Trackable#target()}. Simply returns
	 * the frame {@link #position()}.
	 */
	public PVector target() {
		return position();
	}

	/**
	 * Overloading of {@link remixlab.proscene.Trackable#computeCameraPosition()}.
	 * <p>
	 * The {@link #cameraPosition()} of the camera that is to be tracking the
	 * frame (see the documentation of the Trackable interface) is defined in
	 * spherical coordinates by means of the {@link #azimuth()}, the
	 * {@link #inclination()} and {@link #trackingDistance()}) respect to the
	 * {@link #position()}.
	 */
	public void computeCameraPosition() {
		camRelPos = q.rotate(new PVector(0, 0, 1));
		camRelPos.mult(trackingDistance());
	}
}
