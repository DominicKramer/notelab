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

package noteLab.gui.settings.state;

public interface SettingsStateCapable
{
   /**
    * Changes the display to show the settings currently 
    * stored on the drive.  The display of the settings is 
    * changed, but the settings are not actually applied.
    */
   public void revertToSaved();
   
   /**
    * Changes the display to show the default settings.  The 
    * display of the settings is changed, but the settings are 
    * not actually applied.
    */
   public void restoreDefaults();
   
   /** Actual applies the current settings. */
   public void apply();
}
