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
 * An AxisPlaneConstraint defined in the world coordinate system.
 * <p>
 * The {@link #translationConstraintDirection()} and
 * {@link #rotationConstraintDirection()} are expressed in the world coordinate system.
 */
public class WorldConstraint extends AxisPlaneConstraint {
	/**
	 * Depending on {@link #translationConstraintType()}, {@code constrain}
	 * translation to be along an axis or limited to a plane defined in the Frame
	 * world coordinate system by {@link #translationConstraintDirection()}.
	 */
	@Override
	public PVector constrainTranslation(PVector translation, Frame frame) {
		PVector res = new PVector(translation.x, translation.y, translation.z);
		PVector proj;
		switch (translationConstraintType()) {
		case FREE:
			break;
		case PLANE:
			if (frame.referenceFrame() != null) {
				proj = frame.referenceFrame().transformOf(translationConstraintDirection());
				res = MathUtils.projectVectorOnPlane(translation, proj);
			} else
				res = MathUtils.projectVectorOnPlane(translation,	translationConstraintDirection());
			break;
		case AXIS:
			if (frame.referenceFrame() != null) {
				proj = frame.referenceFrame().transformOf(translationConstraintDirection());
				res = MathUtils.projectVectorOnAxis(translation, proj);
			} else
				res = MathUtils.projectVectorOnAxis(translation, translationConstraintDirection());
			break;
		case FORBIDDEN:
			res = new PVector(0.0f, 0.0f, 0.0f);
			break;
		}
		return res;
	}

	/**
	 * When {@link #rotationConstraintType()} is of type AXIS, constrain {@code
	 * rotation} to be a rotation around an axis whose direction is defined in the
	 * Frame world coordinate system by {@link #rotationConstraintDirection()}.
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
			PVector quat = new PVector(rotation.x, rotation.y, rotation.z);
			PVector axis = frame.transformOf(rotationConstraintDirection());
			quat = MathUtils.projectVectorOnAxis(quat, axis);
			res = new Quaternion(quat, 2.0f * PApplet.acos(rotation.w));
			break;
		}
		case FORBIDDEN:
			res = new Quaternion(); // identity
			break;
		}
		return res;
	}
}
