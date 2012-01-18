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
 * This class represents mouse click shortcuts.
 * <p>
 * Mouse click shortcuts are defined with a specific number of clicks
 * and can be of one out of two forms: 1. A mouse button; and, 2. A mouse
 * button plus a key-modifier (such as the CTRL key).
 */
public class ClickBinding {	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((button == null) ? 0 : button.hashCode());
		result = prime * result + ((mask == null) ? 0 : mask.hashCode());
		result = prime * result
				+ ((numberOfClicks == null) ? 0 : numberOfClicks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClickBinding other = (ClickBinding) obj;
		if (button == null) {
			if (other.button != null)
				return false;
		} else if (!button.equals(other.button))
			return false;
		if (mask == null) {
			if (other.mask != null)
				return false;
		} else if (!mask.equals(other.mask))
			return false;
		if (numberOfClicks == null) {
			if (other.numberOfClicks != null)
				return false;
		} else if (!numberOfClicks.equals(other.numberOfClicks))
			return false;
		return true;
	}

	/**
	 * Defines a mouse single click shortcut from the given mouse button. 
	 * 
	 * @param b mouse button
	 */
	public ClickBinding(Scene.Button b) {
		this(0, b, 1);
	}
	
	/**
	 * Defines a mouse single click shortcut from the given mouse button
	 * and modifier mask.
	 * 	
	 * @param m modifier mask
	 * @param b mouse button
	 */
	public ClickBinding(Integer m, Scene.Button b) {
		this(m, b, 1);
	}
	
	/**
	 * Defines a mouse click shortcut from the given mouse button and
	 * number of clicks. 
	 * 
	 * @param b mouse button
	 * @param c number of clicks
	 */
	public ClickBinding(Scene.Button b, Integer c) {
		this(0, b, c);
	}
	
	/**
	 * Defines a mouse click shortcut from the given mouse button,
	 * modifier mask, and number of clicks.
	 * 
	 * @param m modifier mask
	 * @param b mouse button
	 * @param c bumber of clicks
	 */
	public ClickBinding(Integer m, Scene.Button b, Integer c) {
		this.mask = m;
		this.button = b;
		if(c <= 0)
			this.numberOfClicks = 1;
		else
			this.numberOfClicks = c;
	}
	
	/**
	 * Returns a textual description of this click shortcut.
	 *  
	 * @return description
	 */
	public String description() {
		String description = new String();
		if(mask != 0)
			description += DesktopEvents.getModifiersExText(mask) + " + ";
		switch (button) {
		case LEFT :
			description += "Button1";
			break;
		case MIDDLE :
			description += "Button2";
			break;
		case RIGHT :
			description += "Button3";
			break;		
		}
		if(numberOfClicks==1)
		  description += " + " + numberOfClicks.toString() + " click";
		else
			description += " + " + numberOfClicks.toString() + " clicks";
		return description;
	}
	
	private final Integer mask;
	private final Integer numberOfClicks;
	private final Scene.Button button;
}