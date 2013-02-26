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
 * This class represents keyboard shortcuts.
 * <p>
 * Keyboard shortcuts can be of one out of three forms: 1. Characters (e.g., 'a');
 * 2. Virtual keys (e.g., right arrow key); or, 3. Key combinations (e.g., 'a' + CTRL key).
 */
public final class KeyboardShortcut {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((mask == null) ? 0 : mask.hashCode());
		result = prime * result + ((vKey == null) ? 0 : vKey.hashCode());
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
		KeyboardShortcut other = (KeyboardShortcut) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (mask == null) {
			if (other.mask != null)
				return false;
		} else if (!mask.equals(other.mask))
			return false;
		if (vKey == null) {
			if (other.vKey != null)
				return false;
		} else if (!vKey.equals(other.vKey))
			return false;
		return true;
	}

	/**
	 * Defines a keyboard shortcut from the given character.
	 *  
	 * @param k the character that defines the keyboard shortcut.
	 */
	public KeyboardShortcut(Character k) {
		this.key = k;
		this.mask = null;
		this.vKey = null;
	}
	
	/**
	 * Defines a keyboard shortcut from the given virtual key.
	 * 
	 * @param vk the virtual key that defines the keyboard shortcut.
	 */
	public KeyboardShortcut(Integer vk) {
		this(0, vk);
	}

	/**
	 * Defines a keyboard shortcut from the given modifier mask and virtual key combination.
	 * 
	 * @param m the mask 
	 * @param vk the virtual key that defines the keyboard shortcut.
	 */
	public KeyboardShortcut(Integer m, Integer vk) {
		this.mask = m;
		this.vKey = vk;
		this.key = null;
	}	
	
	/**
	 * Returns a textual description of this keyboard shortcut.
	 *  
	 * @return description
	 */
	public String description() {
		String description = new String();
		if(key != null)
			description = key.toString();
		else {
			if(mask == 0)
				description = DesktopEvents.getKeyText(vKey);
			else
				description = DesktopEvents.getModifiersExText(mask) + "+" + DesktopEvents.getKeyText(vKey);
		}			
		return description;
	}

	private final Integer mask;
	private final Integer vKey;
	private final Character key;		
}