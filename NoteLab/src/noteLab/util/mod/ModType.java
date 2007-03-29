/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2006, Dominic Kramer
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 *  For any questions or comments please contact:  
 *    Dominic Kramer
 *    kramerd@iastate.edu
 */

package noteLab.util.mod;

import noteLab.util.geom.Transformable;

/**
 * <p>
 *   When a <code>ModListener</code> is notified of a modification, an 
 *   object of this type specifies the type of modification done.
 * </p>
 * <p>
 *   To understand why this enum is constructed the way it is, an 
 *   example will be used.  A <code>Page</code> contains a collection of 
 *   <code>Strokes</code>.  The <code>Page</code> listens for changes done 
 *   to its <code>Strokes</code>.  When a modification is done to a 
 *   <code>Stroke</code>, the <code>Page</code> is notified and informs all 
 *   listeners that a modification has occured.  However, because 
 *   many classes implement <code>Transformable</code>, many classes 
 *   are modified when they are scaled or translated.  That means that when 
 *   a <code>Page</code> is scaled, all of its <code>Strokes</code> are 
 *   scaled.  In this case, listeners will be notified that a scale has 
 *   occured when the <code>Page</code> and all of its <code>Strokes</code> 
 *   are scaled.  It is unncessary to have all of these notifications done. 
 *   To fix this problem, a <code>Page</code> does not notify its listeners of 
 *   modifications from its <code>Strokes</code> if the <code>Strokes</code> 
 *   are scaled or translated.  Instead, the <code>Page</code> sends one 
 *   notification, that the entire <code>Page</code> has been modified.  
 *   This is more efficient.
 * </p>
 * 
 * @author Dominic Kramer
 */
public enum ModType
{
   /**
    * Specifies than the modified object has been scaled by an amount.  
    * This could occur when the {@link Transformable#scaleBy(float, float) 
    * Transformable.scaleBy(float, float)} method is invoked on the object.
    */
   ScaleBy, 
   
   /**
    * Specifies than the modified object has been scaled to an amount.  
    * This could occur when the {@link Transformable#scaleTo(float, float) 
    * Transformable.scaleTo(float, float)} method is invoked on the object.
    */
   ScaleTo, 
   
   /**
    * Specifies than the modified object has been translated by an amount.  
    * This could occur when the 
    * {@link Transformable#translateBy(float, float) 
    * Transformable.translateBy(float, float)} method is invoked on the 
    * object.
    */
   TranslateBy, 
   
   /**
    * Specifies than the modified object has been translated to an amount.  
    * This could occur when the 
    * {@link Transformable#translateTo(float, float) 
    * Transformable.translateTo(float, float)} method is invoked on the 
    * object.
    */
   TranslateTo, 
   
   /**
    * Invoked when any other modification is done to the modified object.
    */
   Other
}
