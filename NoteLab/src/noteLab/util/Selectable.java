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

package noteLab.util;

/**
 * A class that implements this interface has the ability to be selected 
 * as well as having its selected state accessed.
 * 
 * @author Dominic Kramer
 */
public interface Selectable
{
   /**
    * Used to determine if this object is selected or not.
    * 
    * @return <code>true</code> if the object is selected and 
    *         <code>false</code> if it's not.
    */
   public boolean isSelected();
   
   /**
    * Used to set this object's selected state.
    * 
    * @param selected <code>true</code> if this object is selected and 
    *                 <code>false</code> if it isn't.
    */
   public void setSelected(boolean selected);
}
