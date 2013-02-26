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
 * An AxisPlaneConstraint defined in the Frame local coordinate system.
 * <p>
 * The {@link #translationConstraintDirection()} and
 * {@link #rotationConstraintDirection()} are expressed in the Frame local
 * coordinate system (see {@link remixlab.proscene.Frame#referenceFrame()}).
 */
public class LocalConstraint extends AxisPlaneConstraint {

	/**
	 * Depending on {@link #translationConstraintType()}, {@code constrain}
	 * translation to be along an axis or limited to a plane defined in the Frame
	 * local coordinate system by {@link #translationConstraintDirection()}.
	 */
	@Override
	public PVector constrainTranslation(PVector translation, Frame frame) {
		PVector res = new PVector(translation.x, translation.y, translation.z);
		PVector proj;
		switch (translationConstraintType()) {
		case FREE:
			break;
		case PLANE:
			proj = frame.rotation().rotate(translationConstraintDirection());
			res = MathUtils.projectVectorOnPlane(translation, proj);
			break;
		case AXIS:
			proj = frame.rotation().rotate(translationConstraintDirection());
			res = MathUtils.projectVectorOnAxis(translation, proj);
			break;
		case FORBIDDEN:
			res = new PVector(0.0f, 0.0f, 0.0f);
			break;
		}
		return res;
	}

	/**
	 * When {@link #rotationConstraintType()} is of Type AXIS, constrain {@code
	 * rotation} to be a rotation around an axis whose direction is defined in the
	 * Frame local coordinate system by {@link #rotationConstraintDirection()}.
	 */
	@Override
	public Quaternion constrainRotation(Quaternion rotation, Frame frame) {
		Quaternion res = new Quaternion(rotation);
		switch (rotationConstraintType()) {
		case FREE:
			break;
		case PLANE:
			break;
		case AXIS: {
			PVector axis = rotationConstraintDirection();
			PVector quat = new PVector(rotation.x, rotation.y, rotation.z);
			quat = MathUtils.projectVectorOnAxis(quat, axis);
			res = new Quaternion(quat, 2.0f * PApplet.acos(rotation.w));
		}
			break;
		case FORBIDDEN:
			res = new Quaternion(); // identity
			break;
		}
		return res;
	}
}
