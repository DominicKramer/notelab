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

package noteLab.util.arg;

public enum ArgResult
{
   /**
    * Indicates that even though the user specified this argument the 
    * graphical user interface should still be displayed.
    */
   SHOW_GUI, 
   
   /**
    * Indicates that no error has occured, but because the user 
    * specified this argument the graphical user interface should not 
    * be started.
    */
   NO_SHOW_GUI, 
   
   /**
    * Specifies that an error has occured and the execution of the argument 
    * cannot continue.
    */
   ERROR
}