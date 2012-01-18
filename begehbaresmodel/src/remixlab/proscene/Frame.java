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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import processing.core.*;

/**
 * A Frame is a 3D coordinate system, represented by a {@link #position()} and
 * an {@link #orientation()}. The order of these transformations is important:
 * the Frame is first translated and then rotated around the new translated
 * origin.
 * <p>
 * In rare situations a frame can be {@link #linkTo(Frame)}, meaning that it
 * will share its {@link #translation()}, {@link #rotation()},
 * {@link #referenceFrame()}, and {@link #constraint()} with the other frame,
 * which can useful for some off-screen scenes.
 */
public class Frame implements Cloneable {
	/**
	 * Internal class that holds the main frame attributes. This class is useful
	 * to linking frames (i.e., to share these attributes).
	 */
	public class FrameKernel implements Cloneable {
		protected PVector trans;
		protected Quaternion rot;
		protected Frame refFrame;
		protected Constraint constr;
		
		public FrameKernel() {
			trans = new PVector(0, 0, 0);
			rot = new Quaternion();
			refFrame = null;
			constr = null;
		}
		
		public FrameKernel(PVector p, Quaternion r) {
			trans = new PVector(p.x, p.y, p.z);
			rot = new Quaternion(r);
			refFrame = null;
			constr = null;
		}
		
		protected FrameKernel(FrameKernel other) {
			trans = new PVector(other.translation().x, other.translation().y, other.translation().z);
			rot = new Quaternion(other.rotation());
			refFrame = other.referenceFrame();
			constr = other.constraint();
		}
		
		public FrameKernel copy() {
			return new FrameKernel(this);
		}
		
		public FrameKernel clone() {
			try {
				FrameKernel clonedFrameKernel = (FrameKernel) super.clone();
				clonedFrameKernel.trans = new PVector(translation().x, translation().y,	translation().z);
				clonedFrameKernel.rot = new Quaternion(rotation());				
				return clonedFrameKernel;
			} catch (CloneNotSupportedException e) {
				throw new Error("Something went wrong when cloning the FrameKernel");
			}
		}
		
		public final PVector translation() {
			return trans;
		}
		
		public final void setTranslation(PVector t) {
			trans = t;			
		}
		
		public final Quaternion rotation() {
			return rot;
		}
		
		public final void setRotation(Quaternion r) {
			rot = r;
		}
		
		public Constraint constraint() {
			return constr;
		}
		
		public final Frame referenceFrame() {
			return refFrame;
		}
		
		public final void setReferenceFrame(Frame rFrame) {
			refFrame = rFrame;
		}
		
		public void setConstraint(Constraint c) {
			constr = c;
		}
	}	

	protected FrameKernel krnl;
	protected List<KeyFrameInterpolator> list;
	protected List<Frame> linkedFramesList;
	protected Frame srcFrame;

	/**
	 * Creates a default Frame.
	 * <p>
	 * Its {@link #position()} is (0,0,0) and it has an identity
	 * {@link #orientation()} Quaternion. The {@link #referenceFrame()} and the
	 * {@link #constraint()} are {@code null}.
	 */
	public Frame() {
		krnl = new FrameKernel();
		list = new ArrayList<KeyFrameInterpolator>();
		linkedFramesList = new ArrayList<Frame>();
		srcFrame = null;
	}

	/**
	 * Creates a Frame with a {@link #position()} and an {@link #orientation()}.
	 * <p>
	 * See the PVector and Quaternion documentations for convenient constructors
	 * and methods.
	 * <p>
	 * The Frame is defined in the world coordinate system (its
	 * {@link #referenceFrame()} is {@code null}). It has a {@code null}
	 * associated {@link #constraint()}.
	 */
	public Frame(PVector p, Quaternion r) {
		krnl = new FrameKernel(p, r);
		list = new ArrayList<KeyFrameInterpolator>();
		linkedFramesList = new ArrayList<Frame>();
		srcFrame = null;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *          the Frame containing the object to be copied
	 */
	protected Frame(Frame other) {
		krnl = new FrameKernel( other.kernel() );
		list = new ArrayList<KeyFrameInterpolator>();
		Iterator<KeyFrameInterpolator> it = other.listeners().iterator();
		while (it.hasNext())
			list.add(it.next());
		linkedFramesList = new ArrayList<Frame>();
		Iterator<Frame> iterator = other.linkedFramesList.iterator();
		while (iterator.hasNext())
			linkedFramesList.add(iterator.next());
		srcFrame = other.srcFrame;
	}

	/**
	 * Calls {@link #Frame(Frame)} (which is private) and returns a copy of
	 * {@code this} object.
	 * 
	 * @see #Frame(Frame)
	 */
	public Frame copy() {
		return new Frame(this);
	}

	/**
	 * Implementation of the clone method.
	 * <p>
	 * The method performs a deep copy of the {@link #translation()} and
	 * {@link #rotation()} objects of the Frame, and a shallow copy of its
	 * {@link #referenceFrame()} and {@link #constraint()} objects.
	 * 
	 * @see #copy()
	 */
	// public Frame clone() throws CloneNotSupportedException {
	public Frame clone() {
		try {
			Frame clonedFrame = (Frame) super.clone();
			clonedFrame.krnl = new FrameKernel(kernel());			
			clonedFrame.list = new ArrayList<KeyFrameInterpolator>();
			Iterator<KeyFrameInterpolator> it = listeners().iterator();
			while (it.hasNext())
				clonedFrame.list.add(it.next());
			clonedFrame.linkedFramesList = new ArrayList<Frame>();
			Iterator<Frame> iterator = linkedFramesList.iterator();
			while (iterator.hasNext())
				clonedFrame.linkedFramesList.add(iterator.next());			
			return clonedFrame;
		} catch (CloneNotSupportedException e) {
			throw new Error("Something went wrong when cloning the Frame");
		}
	}
	
	public FrameKernel kernel() {
		return krnl;
	}
	
	public void setKernel(FrameKernel k) {
		krnl = k;
	}

	/**
	 * Returns the Frame translation, defined with respect to the
	 * {@link #referenceFrame()}.
	 * <p>
	 * Use {@link #position()} to get the result in the world coordinates. These
	 * two values are identical when the {@link #referenceFrame()} is {@code null}
	 * (default).
	 * 
	 * @see #setTranslation(PVector)
	 * @see #setTranslationWithConstraint(PVector)
	 */
	public final PVector translation() {
		return kernel().translation();
	}

	/**
	 * Returns the Frame rotation, defined with respect to the
	 * {@link #referenceFrame()} (i.e, the current Quaternion orientation).
	 * <p>
	 * Use {@link #orientation()} to get the result in the world coordinates.
	 * These two values are identical when the {@link #referenceFrame()} is
	 * {@code null} (default).
	 * 
	 * @see #setRotation(Quaternion)
	 * @see #setRotationWithConstraint(Quaternion)
	 */
	public final Quaternion rotation() {
		return kernel().rotation();
	}

	/**
	 * Returns the reference Frame, in which coordinates system the Frame is
	 * defined.
	 * <p>
	 * The {@link #translation()} {@link #rotation()} of the Frame are defined
	 * with respect to the reference Frame coordinate system. A {@code null}
	 * reference Frame (default value) means that the Frame is defined in the
	 * world coordinate system.
	 * <p>
	 * Use {@link #position()} and {@link #orientation()} to recursively convert
	 * values along the reference Frame chain and to get values expressed in the
	 * world coordinate system. The values match when the reference Frame {@code
	 * null}.
	 * <p>
	 * Use {@link #setReferenceFrame(Frame)} to set this value and create a Frame
	 * hierarchy. Convenient functions allow you to convert 3D coordinates from
	 * one Frame to another: see {@link #coordinatesOf(PVector)},
	 * {@link #localCoordinatesOf(PVector)} ,
	 * {@link #coordinatesOfIn(PVector, Frame)} and their inverse functions.
	 * <p>
	 * Vectors can also be converted using {@link #transformOf(PVector)},
	 * {@link #transformOfIn(PVector, Frame)}, {@link #localTransformOf(PVector)}
	 * and their inverse functions.
	 */
	public final Frame referenceFrame() {
		return kernel().referenceFrame();
	}

	/**
	 * Returns the current constraint applied to the Frame.
	 * <p>
	 * A {@code null} value (default) means that no Constraint is used to filter
	 * the Frame translation and rotation.
	 * <p>
	 * See the Constraint class documentation for details.
	 */
	public Constraint constraint() {
		return kernel().constraint();
	}

	/**
	 * Returns the list of KeyFrameInterpolators that are currently listening to
	 * this frame. Normally, you should not call this method as the
	 * KeyFrameInterpolator takes care of calling it.
	 * 
	 * @see remixlab.proscene.KeyFrameInterpolator#addKeyFrame(Frame, float,
	 *      boolean)
	 */
	public List<KeyFrameInterpolator> listeners() {
		return list;
	}

	/**
	 * Adds {@code kfi} to the list of KeyFrameInterpolators that are
	 * currently listening this frame.
	 */
	public void addListener(KeyFrameInterpolator kfi) {
		list.add(kfi);
	}

	/**
	 * Removes {@code kfi} from the list of KeyFrameInterpolators that are
	 * currently listening to this frame. Normally, you should not call this
	 * method, unless you have added {@code kfi} before (by calling
	 * {@link #addListener(KeyFrameInterpolator)}).
	 * 
	 * @see remixlab.proscene.KeyFrameInterpolator#addKeyFrame(Frame, float,
	 *      boolean)
	 */
	public void removeListener(KeyFrameInterpolator kfi) {
		list.remove(kfi);
	}

	/**
	 * Resets the cache of all KeyFrameInterpolators' associated with this Frame.
	 */
	protected void modified() {
		Iterator<KeyFrameInterpolator> it = list.iterator();
		while (it.hasNext()) {
			it.next().invalidateValues();
		}
	}
	
	/**
	 * Links this frame (referred to as the requested frame) to {@code sourceFrame},
	 * meaning that this frame will take (and share by reference) the {@link #translation()},
	 * {@link #rotation()}, {@link #referenceFrame()}, and {@link #constraint()} from the
	 * {@code sourceFrame}. This can useful for some off-screen scenes, e.g., to link a
	 * frame defined in one scene to the camera frame defined in other scene
	 * (see the CameraCrane example).
	 * <p>
	 * <b>Note:</b> Linking frames has the following properties:
	 * <ol>
   * <li>A frame can be linked only to another frame (referred to as the source
   * frame).</li> 
   * <li>A source frame can be linked by from many (requested) frames.</li>
   * <li>A source frame can't be linked to another (source) frame, i.e., it
   * can only receive links form other frames.</li>
   * </ol>
	 * 
	 * @param sourceFrame the frame to link this frame with.
	 * @return true if this frame can successfully being linked to the frame. False otherwise.
	 * 
	 * @see #linkFrom(Frame)
	 * @see #unlink()
	 * @see #unlinkFrom(Frame)
	 * @see #isLinked()
	 * @see #areLinkedTogether(Frame)
	 */
	public boolean linkTo(Frame sourceFrame) {
		// avoid loops		
		if( (!linkedFramesList.isEmpty()) || sourceFrame.linkedFramesList.contains(this) || (sourceFrame == this) )
			return false;		
		
		if(sourceFrame.linkedFramesList.add(this)) {
			srcFrame = sourceFrame;
			setKernel(srcFrame.kernel());
			return true;
		}
		
		return false;
	}	
	
	/**
	 * Attempts to link the {@code requestedFrame} to this frame.
	 * <p>
	 * See {@link #linkTo(Frame)} for the rules and terminology applying to the linking process.
	 * 
	 * @param requestedFrame the frame that is requesting a link to this frame.
	 * @return true if the requested frame can successfully being linked to this frame. False otherwise.
	 * 
	 * @see #linkTo(Frame)
	 * @see #unlink()
	 * @see #unlinkFrom(Frame)
	 * @see #isLinked()
	 * @see #areLinkedTogether(Frame)
	 */
	public boolean linkFrom(Frame requestedFrame) {
	  // avoid loops		
		if( (!requestedFrame.linkedFramesList.isEmpty()) || linkedFramesList.contains(this) || (requestedFrame == this) )
			return false;		
		
		if(linkedFramesList.add(requestedFrame)) {
			requestedFrame.srcFrame = this;
			requestedFrame.setKernel(kernel());
			return true;
		}
		
		return false;
	}
	
	/**
	 * Unlinks this frame from its source frame. Does nothing if this frame is not
	 * linked to another frame.
	 * <p>
	 * See {@link #linkTo(Frame)} for the rules and terminology applying to the linking process.
	 * 
	 * @return true if succeeded otherwise returns false.
	 * 
	 * @see #linkTo(Frame)
	 * @see #linkFrom(Frame) 
	 * @see #unlinkFrom(Frame)
	 * @see #isLinked()
	 * @see #areLinkedTogether(Frame)
	 */
	public boolean unlink() {
		boolean result = false;
		if(srcFrame != null) {
			result = srcFrame.linkedFramesList.remove(this);
			if(result) {
				setKernel(new FrameKernel(srcFrame.translation(), srcFrame.rotation()));
				srcFrame = null;
			}
		}
		return result;
	}
	
	/**
	 * Unlinks the requested frame from this frame. Does nothing if the frames are
	 * not linked together ({@link #areLinkedTogether(Frame)}).
	 * <p>
	 * See {@link #linkTo(Frame)} for the rules and terminology applying to the linking process.
	 * 
	 * @return true if succeeded otherwise returns false.
	 * 
	 * @see #linkTo(Frame)
	 * @see #linkFrom(Frame)
	 * @see #unlink()
	 * @see #isLinked()
	 * @see #areLinkedTogether(Frame)
	 */
	public boolean unlinkFrom(Frame requestedFrame) {
		boolean result = false;
		if ( (srcFrame == null) && (requestedFrame != this) ) {
			result = linkedFramesList.remove(requestedFrame);
			if (result) {
				requestedFrame.setKernel(new FrameKernel(translation(), rotation()));
				requestedFrame.srcFrame = null;
			}
		}
		return result;
	}
	
	/**
	 * Returns true if this frame is linked to a source frame or if this frame
	 * acts as the source frame of other frames. Otherwise returns false.
	 * <p>
	 * See {@link #linkTo(Frame)} for the rules and terminology applying to the linking process.
	 * 
	 * @see #linkTo(Frame)
	 * @see #linkFrom(Frame)
	 * @see #unlink()
	 * @see #unlinkFrom(Frame)
	 * @see #areLinkedTogether(Frame)
	 */
	public boolean isLinked() {
		if ((srcFrame != null) || (!linkedFramesList.isEmpty()) )
			return true;
		return false;
	}
	
	/**
	 * Returns true if this frame is linked with {@code sourceFrame}. Otherwise
	 * returns false.
	 * <p>
	 * See {@link #linkTo(Frame)} for the rules and terminology applying to the linking process.
	 * 
	 * @see #linkTo(Frame)
	 * @see #linkFrom(Frame)
	 * @see #unlink()
	 * @see #unlinkFrom(Frame)
	 * @see #isLinked() 
	 */
	public boolean areLinkedTogether(Frame sourceFrame) {
		if (sourceFrame == srcFrame)			
			return true;
		if (linkedFramesList.contains(sourceFrame))
			return true;
		return false;
	}
	
	/**
	 * Sets the {@link #translation()} of the frame, locally defined with respect
	 * to the {@link #referenceFrame()}. Calls {@link #modified()}.
	 * <p>
	 * Use {@link #setPosition(PVector)} to define the world coordinates
	 * {@link #position()}. Use {@link #setTranslationWithConstraint(PVector)} to
	 * take into account the potential {@link #constraint()} of the Frame.
	 */
	public final void setTranslation(PVector t) {
		kernel().setTranslation(t);
		modified();
	}

	/**
	 * Same as {@link #setTranslation(PVector)}, but with {@code float}
	 * parameters.
	 */
	public final void setTranslation(float x, float y, float z) {
		setTranslation(new PVector(x, y, z));
	}

	/**
	 * Same as {@link #setTranslation(PVector)}, but if there's a
	 * {@link #constraint()} it is satisfied (without modifying {@code
	 * translation}).
	 * 
	 * @see #setRotationWithConstraint(Quaternion)
	 * @see #setPositionWithConstraint(PVector)
	 */
	public final void setTranslationWithConstraint(PVector translation) {
		PVector deltaT = PVector.sub(translation, this.translation());
		if (constraint() != null)
			deltaT = constraint().constrainTranslation(deltaT, this);

		translation().add(deltaT);

		/**
		 * translation.x = this.translation().x; translation.y =
		 * this.translation().y; translation.z = this.translation().z;
		 */
	}

	/**
	 * Set the current rotation Quaternion and Calls {@link #modified()}. See the
	 * different Quaternion constructors.
	 * <p>
	 * Sets the {@link #rotation()} of the Frame, locally defined with respect to
	 * the {@link #referenceFrame()}.
	 * <p>
	 * Use {@link #setOrientation(Quaternion)} to define the world coordinates
	 * {@link #orientation()}. The potential {@link #constraint()} of the Frame is
	 * not taken into account, use {@link #setRotationWithConstraint(Quaternion)}
	 * instead.
	 * 
	 * @see #setRotationWithConstraint(Quaternion)
	 * @see #rotation()
	 * @see #setTranslation(PVector)
	 */
	public final void setRotation(Quaternion r) {
		kernel().setRotation(r);
		modified();
	}

	/**
	 * Same as {@link #setRotation(Quaternion)} but with {@code float} Quaternion
	 * parameters.
	 */
	public final void setRotation(float x, float y, float z, float w) {
		setRotation(new Quaternion(x, y, z, w));
	}

	/**
	 * Same as {@link #setRotation(Quaternion)}, if there's a
	 * {@link #constraint()} it's satisfied (without modifying {@code rotation}).
	 * 
	 * @see #setTranslationWithConstraint(PVector)
	 * @see #setOrientationWithConstraint(Quaternion)
	 */
	public final void setRotationWithConstraint(Quaternion rotation) {
		Quaternion deltaQ = Quaternion.multiply(this.rotation().inverse(), rotation);
		if (constraint() != null)
			deltaQ = constraint().constrainRotation(deltaQ, this);

		deltaQ.normalize(); // Prevent numerical drift

		rotation().multiply(deltaQ);
		rotation().normalize();
		// rotation.x = this.rotation().x;
		// rotation.y = this.rotation().y;
		// rotation.z = this.rotation().z;
		// rotation.w = this.rotation().w;
	}

	/**
	 * Sets the {@link #referenceFrame()} of the Frame and calls
	 * {@link #modified()}.
	 * <p>
	 * The Frame {@link #translation()} and {@link #rotation()} are then defined
	 * in the {@link #referenceFrame()} coordinate system.
	 * <p>
	 * Use {@link #position()} and {@link #orientation()} to express these in the
	 * world coordinate system.
	 * <p>
	 * Using this method, you can create a hierarchy of Frames. This hierarchy
	 * needs to be a tree, which root is the world coordinate system (i.e.,
	 * {@code null} {@link #referenceFrame()}). No action is performed if setting
	 * {@code refFrame} as the {@link #referenceFrame()} would create a loop in
	 * the Frame hierarchy.
	 * 
	 * @see #settingAsReferenceFrameWillCreateALoop(Frame)
	 */
	public final void setReferenceFrame(Frame rFrame) {
		if (settingAsReferenceFrameWillCreateALoop(rFrame))
			PApplet.println("Frame.setReferenceFrame would create a loop in Frame hierarchy");
		else {
			boolean identical = (kernel().referenceFrame() == rFrame);
			kernel().setReferenceFrame(rFrame);
			if (!identical)
				modified();
		}
	}

	/**
	 * Sets the {@link #constraint()} attached to the Frame.
	 * <p>
	 * A {@code null} value means no constraint.
	 */
	public void setConstraint(Constraint c) {
		kernel().setConstraint(c);
	}

	/**
	 * Returns {@code true} if setting {@code frame} as the Frame's
	 * {@link #referenceFrame()} would create a loop in the Frame hierarchy.
	 */
	public final boolean settingAsReferenceFrameWillCreateALoop(Frame frame) {
		Frame f = frame;
		while (f != null) {
			if (f == this)
				return true;
			f = f.referenceFrame();
		}
		return false;
	}

	/**
	 * Returns the orientation of the Frame, defined in the world coordinate
	 * system.
	 * 
	 * @see #position()
	 * @see #setOrientation(Quaternion)
	 * @see #rotation()
	 */
	public final Quaternion orientation() {
		Quaternion res = rotation();
		Frame fr = referenceFrame();
		while (fr != null) {
			res = Quaternion.multiply(fr.rotation(), res);
			fr = fr.referenceFrame();
		}
		return res;
	}

	/**
	 * Sets the {@link #position()} of the Frame, defined in the world coordinate
	 * system.
	 * <p>
	 * Use {@link #setTranslation(PVector)} to define the local Frame translation
	 * (with respect to the {@link #referenceFrame()}). The potential
	 * {@link #constraint()} of the Frame is not taken into account, use
	 * {@link #setPositionWithConstraint(PVector)} instead.
	 */
	public final void setPosition(PVector p) {
		if (referenceFrame() != null)
			setTranslation(referenceFrame().coordinatesOf(p));
		else
			setTranslation(p);
	}

	/**
	 * Same as {@link #setPosition(float, float, float)}, but with {@code float}
	 * parameters.
	 */
	public final void setPosition(float x, float y, float z) {
		setPosition(new PVector(x, y, z));
	}

	/**
	 * Same as {@link #setPosition(PVector)}, but if there's a
	 * {@link #constraint()} it is satisfied (without modifying {@code position}).
	 * 
	 * @see #setOrientationWithConstraint(Quaternion)
	 * @see #setTranslationWithConstraint(PVector)
	 */
	public final void setPositionWithConstraint(PVector position) {
		if (referenceFrame() != null)
			position = referenceFrame().coordinatesOf(position);

		setTranslationWithConstraint(position);
	}

	/**
	 * Sets the {@link #orientation()} of the Frame, defined in the world
	 * coordinate system.
	 * <p>
	 * Use {@link #setRotation(Quaternion)} to define the local frame rotation
	 * (with respect to the {@link #referenceFrame()}). The potential
	 * {@link #constraint()} of the Frame is not taken into account, use
	 * {@link #setOrientationWithConstraint(Quaternion)} instead.
	 */
	public final void setOrientation(Quaternion q) {
		if (referenceFrame() != null)
			setRotation(Quaternion.multiply(referenceFrame().orientation().inverse(),	q));
		else
			setRotation(q);
	}

	/**
	 * Same as {@link #setOrientation(Quaternion)}, but with {@code float}
	 * parameters.
	 */
	public final void setOrientation(float x, float y, float z, float w) {
		setOrientation(new Quaternion(x, y, z, w));
	}

	/**
	 * Same as {@link #setOrientation(Quaternion)}, but if there's a
	 * {@link #constraint()} it is satisfied (without modifying {@code
	 * orientation}).
	 * 
	 * @see #setPositionWithConstraint(PVector)
	 * @see #setRotationWithConstraint(Quaternion)
	 */
	public final void setOrientationWithConstraint(Quaternion orientation) {
		if (referenceFrame() != null)
			orientation = Quaternion.multiply(referenceFrame().orientation()
					.inverse(), orientation);

		setRotationWithConstraint(orientation);
	}

	/**
	 * Returns the position of the Frame, defined in the world coordinate system.
	 * 
	 * @see #orientation()
	 * @see #setPosition(PVector)
	 * @see #translation()
	 */
	public final PVector position() {
		return inverseCoordinatesOf(new PVector(0, 0, 0));
	}

	/**
	 * Same as {@code translate(t, true)}. Calls {@link #modified()}.
	 * 
	 * @see #translate(PVector, boolean)
	 * @see #rotate(Quaternion)
	 */
	public final void translate(PVector t) {
		if (constraint() != null)
			kernel().translation().add(constraint().constrainTranslation(t, this));
		else
			kernel().translation().add(t);
		modified();
	}

	/**
	 * Translates the Frame according to {@code t}, locally defined with respect
	 * to the {@link #referenceFrame()}. Calls {@link #modified()}.
	 * <p>
	 * If there's a {@link #constraint()} it is satisfied. Hence the translation
	 * actually applied to the Frame may differ from {@code t} (since it can be
	 * filtered by the {@link #constraint()}). Use {@code translate(t, false)} to
	 * retrieve the filtered translation value and {@code translate(t, true)} to
	 * keep the original value of {@code t}. Use {@link #setTranslation(PVector)}
	 * to directly translate the Frame without taking the {@link #constraint()}
	 * into account.
	 * 
	 * @see #rotate(Quaternion)
	 */
	public final void translate(PVector t, boolean keepArg) {
		PVector o = new PVector(t.x, t.y, t.z);
		if (constraint() != null) {
			o = constraint().constrainTranslation(t, this);
			if (!keepArg) {
				t.x = o.x;
				t.y = o.y;
				t.z = o.z;
			}
		}
		kernel().translation().add(o);
		modified();
	}

	/**
	 * Same as {@link #translate(PVector)} but with {@code float} parameters.
	 */
	public final void translate(float x, float y, float z) {
		translate(new PVector(x, y, z));
	}

	/**
	 * Same as {@code rotate(q, true)}. Calls {@link #modified()}.
	 * 
	 * @see #rotate(Quaternion, boolean)
	 * @see #translate(PVector)
	 */
	public final void rotate(Quaternion q) {
		if (constraint() != null)
			kernel().rotation().multiply(constraint().constrainRotation(q, this));
		else
			kernel().rotation().multiply(q);

		kernel().rotation().normalize(); // Prevents numerical drift
		modified();
	}

	/**
	 * Rotates the Frame by {@code q} (defined in the Frame coordinate system):
	 * {@code R = R*q}. Calls {@link #modified()}.
	 * <p>
	 * If there's a {@link #constraint()} it is satisfied. Hence the rotation
	 * actually applied to the Frame may differ from {@code q} (since it can be
	 * filtered by the {@link #constraint()}). Use {@code rotate(q, false)} to
	 * retrieve the filtered rotation value and {@code rotate(q, true)} to keep
	 * the original value of {@code q}. Use {@link #setRotation(Quaternion)} to
	 * directly rotate the Frame without taking the {@link #constraint()} into
	 * account.
	 * 
	 * @see #translate(PVector)
	 */
	public final void rotate(Quaternion q, boolean keepArg) {
		Quaternion o = new Quaternion(q);
		if (constraint() != null) {
			o = constraint().constrainRotation(q, this);
			if (!keepArg) {
				q.x = o.x;
				q.y = o.y;
				q.z = o.z;
				q.w = o.w;
			}
		}
		kernel().rotation().multiply(o);

		kernel().rotation().normalize(); // Prevents numerical drift
		modified();
	}

	/**
	 * Same as {@link #rotate(Quaternion)} but with {@code float} Quaternion
	 * parameters.
	 */
	public final void rotate(float x, float y, float z, float w) {
		rotate(new Quaternion(x, y, z, w));
	}

	/**
	 * Same as {@code rotateAroundPoint(rotation, point, true)}. Calls
	 * {@link #modified()}.
	 */
	public final void rotateAroundPoint(Quaternion rotation, PVector point) {
		if (constraint() != null)
			rotation = constraint().constrainRotation(rotation, this);

		this.kernel().rotation().multiply(rotation);
		this.kernel().rotation().normalize(); // Prevents numerical drift

		Quaternion q = new Quaternion(inverseTransformOf(rotation.axis()), rotation.angle());
		PVector t = PVector.add(point, q.rotate(PVector.sub(position(), point)));
		t.sub(kernel().translation());

		if (constraint() != null)
			kernel().translation().add(constraint().constrainTranslation(t, this));
		else
			kernel().translation().add(t);
		modified();
	}

	/**
	 * Same as {@link #rotateAroundPoint(Quaternion, PVector)} but if there's a
	 * {@link #constraint()} {@code rotation} is modified to satisfy it.
	 */

	/**
	 * Makes the Frame {@link #rotate(Quaternion)} by {@code rotation} around
	 * {@code point}. Calls {@link #modified()}.
	 * <p>
	 * {@code point} is defined in the world coordinate system, while the {@code
	 * rotation} axis is defined in the Frame coordinate system.
	 * <p>
	 * If the Frame has a {@link #constraint()}, {@code rotation} is first
	 * constrained using
	 * {@link remixlab.proscene.Constraint#constrainRotation(Quaternion, Frame)}.
	 * Hence the rotation actually applied to the Frame may differ from {@code
	 * rotation} (since it can be filtered by the {@link #constraint()}). Use
	 * {@code rotateAroundPoint(rotation, point, false)} to retrieve the filtered
	 * rotation value and {@code rotateAroundPoint(rotation, point, true)} to keep
	 * the original value of {@code rotation}.
	 * <p>
	 * The translation which results from the filtered rotation around {@code
	 * point} is then computed and filtered using
	 * {@link remixlab.proscene.Constraint#constrainTranslation(PVector, Frame)}.
	 */
	public final void rotateAroundPoint(Quaternion rotation, PVector point,
			boolean keepArg) {
		Quaternion q = new Quaternion(rotation);
		if (constraint() != null) {
			q = constraint().constrainRotation(rotation, this);
			if (!keepArg) {
				rotation.x = q.x;
				rotation.y = q.y;
				rotation.z = q.z;
				rotation.w = q.w;
			}
		}
		this.kernel().rotation().multiply(q);
		this.kernel().rotation().normalize(); // Prevents numerical drift

		q = new Quaternion(inverseTransformOf(rotation.axis()), rotation.angle());
		PVector t = PVector.add(point, q.rotate(PVector.sub(position(), point)));
		t.sub(kernel().translation());

		if (constraint() != null)
			kernel().translation().add(constraint().constrainTranslation(t, this));
		else
			kernel().translation().add(t);
		modified();
	}

	/**
	 * Convenience function that simply calls {@code alignWithFrame(frame, false,
	 * 0.85f)}
	 */
	public final void alignWithFrame(Frame frame) {
		alignWithFrame(frame, false, 0.85f);
	}

	/**
	 * Convenience function that simply calls {@code alignWithFrame(frame, move,
	 * 0.85f)}
	 */
	public final void alignWithFrame(Frame frame, boolean move) {
		alignWithFrame(frame, move, 0.85f);
	}

	/**
	 * Convenience function that simply calls {@code alignWithFrame(frame, false,
	 * threshold)}
	 */
	public final void alignWithFrame(Frame frame, float threshold) {
		alignWithFrame(frame, false, threshold);
	}

	/**
	 * Aligns the Frame with {@code frame}, so that two of their axis are
	 * parallel.
	 * <p>
	 * If one of the X, Y and Z axis of the Frame is almost parallel to any of the
	 * X, Y, or Z axis of {@code frame}, the Frame is rotated so that these two
	 * axis actually become parallel.
	 * <p>
	 * If, after this first rotation, two other axis are also almost parallel, a
	 * second alignment is performed. The two frames then have identical
	 * orientations, up to 90 degrees rotations.
	 * <p>
	 * {@code threshold} measures how close two axis must be to be considered
	 * parallel. It is compared with the absolute values of the dot product of the
	 * normalized axis.
	 * <p>
	 * When {@code move} is set to {@code true}, the Frame {@link #position()} is
	 * also affected by the alignment. The new Frame {@link #position()} is such
	 * that the {@code frame} frame position (computed with
	 * {@link #coordinatesOf(PVector)}, in the Frame coordinates system) does not
	 * change.
	 * <p>
	 * {@code frame} may be {@code null} and then represents the world coordinate
	 * system (same convention than for the {@link #referenceFrame()}).
	 */
	public final void alignWithFrame(Frame frame, boolean move, float threshold) {
		PVector[][] directions = new PVector[2][3];
		for (int d = 0; d < 3; ++d) {
			PVector dir = new PVector((d == 0) ? 1.0f : 0.0f, (d == 1) ? 1.0f : 0.0f,
					(d == 2) ? 1.0f : 0.0f);
			if (frame != null)
				directions[0][d] = frame.inverseTransformOf(dir);
			else
				directions[0][d] = dir;
			directions[1][d] = inverseTransformOf(dir);
		}

		float maxProj = 0.0f;
		float proj;
		short[] index = new short[2];
		index[0] = index[1] = 0;

		PVector vec = new PVector(0.0f, 0.0f, 0.0f);
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				vec.set(directions[0][i]);
				proj = PApplet.abs(vec.dot(directions[1][j]));
				if ((proj) >= maxProj) {
					index[0] = (short) i;
					index[1] = (short) j;
					maxProj = proj;
				}
			}
		}

		// Frame old = new Frame(this);
		Frame old = this.clone();

		vec.set(directions[0][index[0]]);
		float coef = vec.dot(directions[1][index[1]]);

		if (PApplet.abs(coef) >= threshold) {
			vec.set(directions[0][index[0]]);
			PVector axis = vec.cross(directions[1][index[1]]);
			float angle = PApplet.asin(axis.mag());
			if (coef >= 0.0)
				angle = -angle;
			// setOrientation(Quaternion(axis, angle) * orientation());
			Quaternion q = new Quaternion(axis, angle);
			q = Quaternion.multiply(rotation().inverse(), q);
			q = Quaternion.multiply(q, orientation());
			rotate(q);

			// Try to align an other axis direction
			short d = (short) ((index[1] + 1) % 3);
			PVector dir = new PVector((d == 0) ? 1.0f : 0.0f, (d == 1) ? 1.0f : 0.0f,
					(d == 2) ? 1.0f : 0.0f);
			dir = inverseTransformOf(dir);

			float max = 0.0f;
			for (int i = 0; i < 3; ++i) {
				vec.set(directions[0][i]);
				proj = PApplet.abs(vec.dot(dir));
				if (proj > max) {
					index[0] = (short) i;
					max = proj;
				}
			}

			if (max >= threshold) {
				vec.set(directions[0][index[0]]);
				axis = vec.cross(dir);
				angle = PApplet.asin(axis.mag());
				vec.set(directions[0][index[0]]);
				if (vec.dot(dir) >= 0.0)
					angle = -angle;
				// setOrientation(Quaternion(axis, angle) * orientation());
				q.fromAxisAngle(axis, angle);
				q = Quaternion.multiply(rotation().inverse(), q);
				q = Quaternion.multiply(q, orientation());
				rotate(q);
			}
		}

		if (move) {
			PVector center = new PVector(0.0f, 0.0f, 0.0f);
			if (frame != null)
				center = frame.position();

			vec = PVector
					.sub(center, orientation().rotate(old.coordinatesOf(center)));
			vec.sub(translation());
			translate(vec);
		}
	}

	/**
	 * Translates the Frame so that its {@link #position()} lies on the line
	 * defined by {@code origin} and {@code direction} (defined in the world
	 * coordinate system).
	 * <p>
	 * Simply uses an orthogonal projection. {@code direction} does not need to be
	 * normalized.
	 */
	public final void projectOnLine(PVector origin, PVector direction) {
		PVector shift = PVector.sub(origin, position());
		PVector proj = shift;
		// float directionSquaredNorm = (direction.x * direction.x) + (direction.y *
		// direction.y) + (direction.z * direction.z);
		// float modulation = proj.dot(direction) / directionSquaredNorm;
		// proj = PVector.mult(direction, modulation);
		proj = MathUtils.projectVectorOnAxis(proj, direction);
		translate(PVector.sub(shift, proj));
	}

	/**
	 * Returns the Frame coordinates of a point {@code src} defined in the world
	 * coordinate system (converts from world to Frame).
	 * <p>
	 * {@link #inverseCoordinatesOf(PVector)} performs the inverse conversion.
	 * {@link #transformOf(PVector)} converts 3D vectors instead of 3D
	 * coordinates.
	 */
	public final PVector coordinatesOf(PVector src) {
		if (referenceFrame() != null)
			return localCoordinatesOf(referenceFrame().coordinatesOf(src));
		else
			return localCoordinatesOf(src);
	}

	/**
	 * Returns the world coordinates of the point whose position in the Frame
	 * coordinate system is {@code src} (converts from Frame to world).
	 * <p>
	 * {@link #coordinatesOf(PVector)} performs the inverse conversion. Use
	 * {@link #inverseTransformOf(PVector)} to transform 3D vectors instead of 3D
	 * coordinates.
	 */
	public final PVector inverseCoordinatesOf(PVector src) {
		Frame fr = this;
		PVector res = src;
		while (fr != null) {
			res = fr.localInverseCoordinatesOf(res);
			fr = fr.referenceFrame();
		}
		return res;
	}

	/**
	 * Returns the Frame coordinates of a point {@code src} defined in the
	 * {@link #referenceFrame()} coordinate system (converts from
	 * {@link #referenceFrame()} to Frame).
	 * <p>
	 * {@link #localInverseCoordinatesOf(PVector)} performs the inverse
	 * conversion.
	 * 
	 * @see #localTransformOf(PVector)
	 */
	public final PVector localCoordinatesOf(PVector src) {
		return rotation().inverseRotate(PVector.sub(src, translation()));
	}

	/**
	 * Returns the {@link #referenceFrame()} coordinates of a point {@code src}
	 * defined in the Frame coordinate system (converts from Frame to
	 * {@link #referenceFrame()}).
	 * <p>
	 * {@link #localCoordinatesOf(PVector)} performs the inverse conversion.
	 * 
	 * @see #localInverseTransformOf(PVector)
	 */
	public final PVector localInverseCoordinatesOf(PVector src) {
		return PVector.add(rotation().rotate(src), translation());
	}

	/**
	 * Returns the Frame coordinates of the point whose position in the {@code
	 * from} coordinate system is {@code src} (converts from {@code from} to
	 * Frame).
	 * <p>
	 * {@link #coordinatesOfIn(PVector, Frame)} performs the inverse
	 * transformation.
	 */
	public final PVector coordinatesOfFrom(PVector src, Frame from) {
		if (this == from)
			return src;
		else if (referenceFrame() != null)
			return localCoordinatesOf(referenceFrame().coordinatesOfFrom(src, from));
		else
			return localCoordinatesOf(from.inverseCoordinatesOf(src));
	}

	/**
	 * Returns the {@code in} coordinates of the point whose position in the Frame
	 * coordinate system is {@code src} (converts from Frame to {@code in}).
	 * <p>
	 * {@link #coordinatesOfFrom(PVector, Frame)} performs the inverse
	 * transformation.
	 */
	public final PVector coordinatesOfIn(PVector src, Frame in) {
		Frame fr = this;
		PVector res = src;
		while ((fr != null) && (fr != in)) {
			res = fr.localInverseCoordinatesOf(res);
			fr = fr.referenceFrame();
		}

		if (fr != in)
			// in was not found in the branch of this, res is now expressed in
			// the world
			// coordinate system. Simply convert to in coordinate system.
			res = in.coordinatesOf(res);

		return res;
	}

	/**
	 * Returns the Frame transform of a vector {@code src} defined in the world
	 * coordinate system (converts vectors from world to Frame).
	 * <p>
	 * {@link #inverseTransformOf(PVector)} performs the inverse transformation.
	 * {@link #coordinatesOf(PVector)} converts 3D coordinates instead of 3D
	 * vectors (here only the rotational part of the transformation is taken into
	 * account).
	 */
	public final PVector transformOf(PVector src) {
		if (referenceFrame() != null)
			return localTransformOf(referenceFrame().transformOf(src));
		else
			return localTransformOf(src);

	}

	/**
	 * Returns the world transform of the vector whose coordinates in the Frame
	 * coordinate system is {@code src} (converts vectors from Frame to world).
	 * <p>
	 * {@link #transformOf(PVector)} performs the inverse transformation. Use
	 * {@link #inverseCoordinatesOf(PVector)} to transform 3D coordinates instead
	 * of 3D vectors.
	 */
	public final PVector inverseTransformOf(PVector src) {
		Frame fr = this;
		PVector res = src;
		while (fr != null) {
			res = fr.localInverseTransformOf(res);
			fr = fr.referenceFrame();
		}
		return res;
	}

	/**
	 * Rotates the frame so that its {@link #xAxis()} becomes {@code axis} defined
	 * in the world coordinate system.
	 * <p>
	 * <b>Attention:</b> this rotation is not uniquely defined. See
	 * {@link remixlab.proscene.Quaternion#fromTo(PVector, PVector)}.
	 * 
	 * @see #xAxis()
	 * @see #setYAxis(PVector)
	 * @see #setZAxis(PVector)
	 */
	public void setXAxis(PVector axis) {
		rotate(new Quaternion(new PVector(1.0f, 0.0f, 0.0f), transformOf(axis)));
	}

	/**
	 * Rotates the frame so that its {@link #yAxis()} becomes {@code axis} defined
	 * in the world coordinate system.
	 * <p>
	 * <b>Attention:</b> this rotation is not uniquely defined. See
	 * {@link remixlab.proscene.Quaternion#fromTo(PVector, PVector)}.
	 * 
	 * @see #yAxis()
	 * @see #setYAxis(PVector)
	 * @see #setZAxis(PVector)
	 */
	public void setYAxis(PVector axis) {
		rotate(new Quaternion(new PVector(0.0f, 1.0f, 0.0f), transformOf(axis)));
	}

	/**
	 * Rotates the frame so that its {@link #zAxis()} becomes {@code axis} defined
	 * in the world coordinate system.
	 * <p>
	 * <b>Attention:</b> this rotation is not uniquely defined. See
	 * {@link remixlab.proscene.Quaternion#fromTo(PVector, PVector)}.
	 * 
	 * @see #zAxis()
	 * @see #setYAxis(PVector)
	 * @see #setZAxis(PVector)
	 */
	public void setZAxis(PVector axis) {
		rotate(new Quaternion(new PVector(0.0f, 0.0f, 1.0f), transformOf(axis)));
	}

	/**
	 * Returns the x-axis of the frame, represented as a normalized vector defined
	 * in the world coordinate system.
	 * 
	 * @see #setXAxis(PVector)
	 * @see #yAxis()
	 * @see #zAxis()
	 */
	public PVector xAxis() {
		return inverseTransformOf(new PVector(1.0f, 0.0f, 0.0f));
	}

	/**
	 * Returns the y-axis of the frame, represented as a normalized vector defined
	 * in the world coordinate system.
	 * 
	 * @see #setYAxis(PVector)
	 * @see #xAxis()
	 * @see #zAxis()
	 */
	public PVector yAxis() {
		return inverseTransformOf(new PVector(0.0f, 1.0f, 0.0f));
	}

	/**
	 * Returns the z-axis of the frame, represented as a normalized vector defined
	 * in the world coordinate system.
	 * 
	 * @see #setZAxis(PVector)
	 * @see #xAxis()
	 * @see #yAxis()
	 */
	public PVector zAxis() {
		return inverseTransformOf(new PVector(0.0f, 0.0f, 1.0f));
	}

	/**
	 * Returns the Frame transform of a vector {@code src} defined in the
	 * {@link #referenceFrame()} coordinate system (converts vectors from
	 * {@link #referenceFrame()} to Frame).
	 * <p>
	 * {@link #localInverseTransformOf(PVector)} performs the inverse
	 * transformation.
	 * 
	 * @see #localCoordinatesOf(PVector)
	 */
	public final PVector localTransformOf(PVector src) {
		return rotation().inverseRotate(src);
	}

	/**
	 * Returns the {@link #referenceFrame()} transform of a vector {@code src}
	 * defined in the Frame coordinate system (converts vectors from Frame to
	 * {@link #referenceFrame()}).
	 * <p>
	 * {@link #localTransformOf(PVector)} performs the inverse transformation.
	 * 
	 * @see #localInverseCoordinatesOf(PVector)
	 */
	public final PVector localInverseTransformOf(PVector src) {
		return rotation().rotate(src);
	}

	/**
	 * Returns the Frame transform of the vector whose coordinates in the {@code
	 * from} coordinate system is {@code src} (converts vectors from {@code from}
	 * to Frame).
	 * <p>
	 * {@link #transformOfIn(PVector, Frame)} performs the inverse transformation.
	 */
	public final PVector transformOfFrom(PVector src, Frame from) {
		if (this == from)
			return src;
		else if (referenceFrame() != null)
			return localTransformOf(referenceFrame().transformOfFrom(src, from));
		else
			return localTransformOf(from.inverseTransformOf(src));
	}

	/**
	 * Returns the {@code in} transform of the vector whose coordinates in the
	 * Frame coordinate system is {@code src} (converts vectors from Frame to
	 * {@code in}).
	 * <p>
	 * {@link #transformOfFrom(PVector, Frame)} performs the inverse
	 * transformation.
	 */
	public final PVector transformOfIn(PVector src, Frame in) {
		Frame fr = this;
		PVector res = src;
		while ((fr != null) && (fr != in)) {
			res = fr.localInverseTransformOf(res);
			fr = fr.referenceFrame();
		}

		if (fr != in)
			// in was not found in the branch of this, res is now expressed in
			// the world
			// coordinate system. Simply convert to in coordinate system.
			res = in.transformOf(res);

		return res;
	}

	/**
	 * Returns the PMatrix3D associated with this Frame.
	 * <p>
	 * This method could be used in conjunction with {@code applyMatrix()} to
	 * modify the processing modelview matrix from a Frame hierarchy. For example,
	 * with this Frame hierarchy:
	 * <p>
	 * {@code Frame body = new Frame();} <br>
	 * {@code Frame leftArm = new Frame();} <br>
	 * {@code Frame rightArm = new Frame();} <br>
	 * {@code leftArm.setReferenceFrame(body);} <br>
	 * {@code rightArm.setReferenceFrame(body);} <br>
	 * <p>
	 * The associated processing drawing code should look like:
	 * <p>
	 * {@code p.pushMatrix();}//Supposing p is the PApplet instance <br>
	 * {@code p.applyMatrix(body.matrix());} <br>
	 * {@code drawBody();} <br>
	 * {@code p.pushMatrix();} <br>
	 * {@code p.applyMatrix(leftArm.matrix());} <br>
	 * {@code drawArm();} <br>
	 * {@code p.popMatrix();} <br>
	 * {@code p.pushMatrix();} <br>
	 * {@code p.applyMatrix(rightArm.matrix());} <br>
	 * {@code drawArm();} <br>
	 * {@code p.popMatrix();} <br>
	 * {@code p.popMatrix();} <br>
	 * <p>
	 * Note the use of nested {@code pushMatrix()} and {@code popMatrix()} blocks
	 * to represent the frame hierarchy: {@code leftArm} and {@code rightArm} are
	 * both correctly drawn with respect to the {@code body} coordinate system.
	 * <p>
	 * <b>Attention:</b> This technique is inefficient because {@code
	 * p.applyMatrix} will try to calculate the inverse of the transform. Avoid it
	 * whenever possible and instead use {@link #applyTransformation(PApplet)}
	 * which is very efficient.
	 * <p>
	 * This matrix only represents the local Frame transformation (i.e., with
	 * respect to the {@link #referenceFrame()}). Use {@link #worldMatrix()} to
	 * get the full Frame transformation matrix (i.e., from the world to the Frame
	 * coordinate system). These two match when the {@link #referenceFrame()} is
	 * {@code null}.
	 * <p>
	 * The result is only valid until the next call to {@code matrix()} or
	 * {@link #worldMatrix()}. Use it immediately (as above).
	 * <p>
	 * <b>Note:</b> The scaling factor of the 4x4 matrix is 1.0.
	 * 
	 * @see #applyTransformation(PApplet)
	 */
	public final PMatrix3D matrix() {
		PMatrix3D pM = new PMatrix3D();

		pM = kernel().rotation().matrix();

		pM.m03 = kernel().translation().x;
		pM.m13 = kernel().translation().y;
		pM.m23 = kernel().translation().z;

		return pM;
	}
	
	/**
	 * Convenience wrapper function that simply calls {@code applyTransformation( (PGraphics3D) p.g )}.
	 * 
	 * @see #applyTransformation(PGraphics3D)
	 */
	public void applyTransformation(PApplet p) {
		applyTransformation( (PGraphics3D) p.g );
	}
	
	/**
	 * Apply the transformation defined by this Frame to {@code p3d}. The Frame is
	 * first translated and then rotated around the new translated origin.
	 * <p>
	 * Same as:
	 * <p>
	 * {@code p3d.translate(translation().x, translation().y, translation().z);} <br>
	 * {@code p3d.rotate(rotation().angle(), rotation().axis().x,
	 * rotation().axis().y, rotation().axis().z);} <br>
	 * <p>
	 * This method should be used in conjunction with PApplet to modify the
	 * processing modelview matrix from a Frame hierarchy. For example, with this
	 * Frame hierarchy:
	 * <p>
	 * {@code Frame body = new Frame();} <br>
	 * {@code Frame leftArm = new Frame();} <br>
	 * {@code Frame rightArm = new Frame();} <br>
	 * {@code leftArm.setReferenceFrame(body);} <br>
	 * {@code rightArm.setReferenceFrame(body);} <br>
	 * <p>
	 * The associated processing drawing code should look like:
	 * <p>
	 * {@code p3d.pushMatrix();//p is the PApplet instance} <br>
	 * {@code body.applyTransformation(p);} <br>
	 * {@code drawBody();} <br>
	 * {@code p3d.pushMatrix();} <br>
	 * {@code leftArm.applyTransformation(p);} <br>
	 * {@code drawArm();} <br>
	 * {@code p3d.popMatrix();} <br>
	 * {@code p3d.pushMatrix();} <br>
	 * {@code rightArm.applyTransformation(p);} <br>
	 * {@code drawArm();} <br>
	 * {@code p3d.popMatrix();} <br>
	 * {@code p3d.popMatrix();} <br>
	 * <p>
	 * Note the use of nested {@code pushMatrix()} and {@code popMatrix()} blocks
	 * to represent the frame hierarchy: {@code leftArm} and {@code rightArm} are
	 * both correctly drawn with respect to the {@code body} coordinate system.
	 * <p>
	 * <b>Attention:</b> When drawing a frame hierarchy as above, this method
	 * should be used whenever possible (one can also use {@link #matrix()}
	 * instead).
	 * 
	 * @see #matrix()
	 */
	public void applyTransformation(PGraphics3D p3d) {
		p3d.translate(translation().x, translation().y, translation().z);
		p3d.rotate(rotation().angle(), rotation().axis().x, rotation().axis().y, rotation().axis().z);
	}

	/**
	 * Returns the processing transformation matrix represented by the Frame.
	 * <p>
	 * This method should be used in conjunction with {@code applyMatrix()} to
	 * modify the processing modelview matrix from a Frame:
	 * <p>
	 * {@code
	 * // Here the modelview matrix corresponds to the world coordinate system.} <br>
	 * {@code Frame fr = new Frame(pos, Quaternion(from, to));} <br>
	 * {@code pushMatrix();} <br>
	 * {@code applyMatrix(worldMatrix());} <br>
	 * {@code // draw object in the fr coordinate system.} <br>
	 * {@code popMatrix();} <br>
	 * <p>
	 * This matrix represents the global Frame transformation: the entire
	 * {@link #referenceFrame()} hierarchy is taken into account to define the
	 * Frame transformation from the world coordinate system. Use
	 * {@link #matrix()} to get the local Frame transformation matrix (i.e.
	 * defined with respect to the {@link #referenceFrame()}). These two match
	 * when the {@link #referenceFrame()} is {@code null}.
	 * <p>
	 * <b>Attention:</b> The result is only valid until the next call to
	 * {@link #matrix()} or {@code worldMatrix()}. Use it immediately (as above).
	 * <p>
	 * <b>Note:</b> The scaling factor of the 4x4 matrix is 1.0.
	 */
	public final PMatrix3D worldMatrix() {
		if (referenceFrame() != null) {
			final Frame fr = new Frame();
			fr.setTranslation(position());
			fr.setRotation(orientation());
			return fr.matrix();
		} else
			return matrix();
	}

	/**
	 * Sets the Frame from a PMatrix3D (processing matrix) representation
	 * (rotation in the upper left 3x3 matrix and translation on the last column).
	 * Calls {@link #modified()}.
	 * <p>
	 * Hence, if a code fragment looks like:
	 * <p>
	 * {@code float [] m = new float [16]; m[0]=...;} <br>
	 * {@code gl.glMultMatrixf(m);} <br>
	 * <p>
	 * It is equivalent to write:
	 * <p>
	 * {@code Frame fr = new Frame();} <br>
	 * {@code fr.fromMatrix(m);} <br>
	 * {@code applyMatrix(fr.matrix());} <br>
	 * <p>
	 * Using this conversion, you can benefit from the powerful Frame
	 * transformation methods to translate points and vectors to and from the
	 * Frame coordinate system to any other Frame coordinate system (including the
	 * world coordinate system). See {@link #coordinatesOf(PVector)} and
	 * {@link #transformOf(PVector)}.
	 * <p>
	 * <b>Attention:</b> A Frame does not contain a scale factor. The possible
	 * scaling in {@code m} will not be converted into the Frame by this method.
	 */
	public final void fromMatrix(PMatrix3D pM) {
		// m should be of size [4][4]
		if (PApplet.abs(pM.m33) < 1E-8) {
			PApplet.println("Doing nothing: pM.m33 should be non-zero!");
			return;
		}

		kernel().translation().x = pM.m03 / pM.m33;
		kernel().translation().y = pM.m13 / pM.m33;
		kernel().translation().z = pM.m23 / pM.m33;

		float[][] r = new float[3][3];

		r[0][0] = pM.m00 / pM.m33;
		r[0][1] = pM.m01 / pM.m33;
		r[0][2] = pM.m02 / pM.m33;
		r[1][0] = pM.m10 / pM.m33;
		r[1][1] = pM.m11 / pM.m33;
		r[1][2] = pM.m12 / pM.m33;
		r[2][0] = pM.m20 / pM.m33;
		r[2][1] = pM.m21 / pM.m33;
		r[2][2] = pM.m22 / pM.m33;

		kernel().rotation().fromRotationMatrix(r);
		modified();
	}

	/**
	 * Returns a Frame representing the inverse of the Frame space transformation.
	 * <p>
	 * The {@link #rotation()} the new Frame is the
	 * {@link remixlab.proscene.Quaternion#inverse()} of the original rotation.
	 * Its {@link #translation()} is the negated inverse rotated image of the
	 * original translation.
	 * <p>
	 * If a Frame is considered as a space rigid transformation (translation and
	 * rotation), the inverse() Frame performs the inverse transformation.
	 * <p>
	 * Only the local Frame transformation (i.e., defined with respect to the
	 * {@link #referenceFrame()}) is inverted. Use {@link #worldInverse()} for a
	 * global inverse.
	 * <p>
	 * The resulting Frame has the same {@link #referenceFrame()} as the Frame and
	 * a {@code null} {@link #constraint()}.
	 * <p>
	 * <b>Note:</b> The scaling factor of the 4x4 matrix is 1.0.
	 */
	public final Frame inverse() {
		Frame fr = new Frame(PVector.mult(kernel().rotation().inverseRotate(kernel().translation()), -1), kernel().rotation().inverse());
		fr.setReferenceFrame(referenceFrame());
		return fr;
	}

	/**
	 * 
	 * Returns the {@link #inverse()} of the Frame world transformation.
	 * <p>
	 * The {@link #orientation()} of the new Frame is the
	 * {@link remixlab.proscene.Quaternion#inverse()} of the original orientation.
	 * Its {@link #position()} is the negated and inverse rotated image of the
	 * original position.
	 * <p>
	 * The result Frame has a {@code null} {@link #referenceFrame()} and a {@code
	 * null} {@link #constraint()}.
	 * <p>
	 * Use {@link #inverse()} for a local (i.e., with respect to
	 * {@link #referenceFrame()}) transformation inverse.
	 */
	public final Frame worldInverse() {
		return (new Frame(
				PVector.mult(orientation().inverseRotate(position()), -1),
				orientation().inverse()));
	}
}
