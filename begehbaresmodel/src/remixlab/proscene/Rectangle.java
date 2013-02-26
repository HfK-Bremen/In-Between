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
 * Rectangle class that provides a quick replacement for the java.awt.Rectangle.
 */
public class Rectangle {
	/**
	 * The X coordinate of the upper-left corner of the Rectangle.
	 */
	public int x;
	
	/**
	 * The Y coordinate of the upper-left corner of the Rectangle.
	 */
	public int y;
	
	/**
	 * The width of the Rectangle.
	 */
	public int width;
	
	/**
	 * The height of the Rectangle.
	 */
	public int height;

	/**
	 * Constructs a new Rectangle whose upper-left corner is at (0, 0) in the
	 * coordinate space, and whose width and height are both zero.
	 */
	public Rectangle() {
		this(0, 0, 0, 0);
	}

	/**
	 * Copy constructor
	 * 
	 * @param r
	 *          the rectangle to be copied
	 */
	public Rectangle(Rectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	/**
	 * Constructs a new Rectangle whose upper-left corner is specified as (x,y)
	 * and whose width and height are specified by the arguments of the same name.
	 */
	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the X coordinate of the center of the rectangle.
	 */
	public float getCenterX() {
		return (float) x + ((float) width / 2);
	}
	
	/**
	 * Returns the Y coordinate of the center of the rectangle.
	 */
	public float getCenterY() {
		return (float) y + ((float) height / 2);
	}
}
