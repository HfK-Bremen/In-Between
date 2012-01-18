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

import java.util.HashMap;

/**
 * A parameterized template class used to define shortcut bindings. This is
 * an internal class that should not be instantiated. 
 * <p>
 * Shortcuts are mapped to keyboard and mouse actions in different places:
 * 1. Scene global keyboard actions; 2. CameraProfile keyboard actions
 * and mouse (and mouse-click) actions.
 * <p>
 * Internally, this class is simply a parameterized hash-map wrap
 * (HashMap<K, A>). 
 */
public class Bindings<K, A> {
	protected Scene scene;
	protected HashMap<K, A> map;

	protected Bindings(Scene scn) {
		scene = scn;
		map = new HashMap<K, A>();
	}

	/**
	 * Returns the action associated to a given Keyboard shortcut {@code key}.
	 */
	protected A binding(K key) {
		return map.get(key);
	}
	
	/**
	 * Defines the shortcut that triggers a given action.
	 * 
	 * @param key shortcut.
	 * @param action action.
	 */
	protected void setBinding(K key, A action) {
		map.put(key, action);
	}
	
	/**
	 * Removes the shortcut binding.
	 * 
	 * @param key shortcut
	 */
	protected void removeBinding(K key) {
		map.remove(key);
	}
	
	/**
	 * Removes all the shortcuts from this object.
	 */
	protected void removeAllBindings() {
		map.clear();
	}

	/**
	 * Returns true if this object contains a binding for the specified shortcut.
	 * 
	 * @param key shortcut
	 * @return true if this object contains a binding for the specified shortcut.
	 */
	protected boolean isShortcutInUse(K key) {
		return map.containsKey(key);
	}

	/**
	 * Returns true if this object maps one or more shortcuts to the specified action.
	 * 
	 * @param action action whose presence in this object is to be tested
	 * @return true if this object maps one or more shortcuts to the specified action.
	 */
	protected boolean isActionMapped(A action) {
		return map.containsValue(action);
	}
}