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

package noteLab.util.settings;

public class SettingsChangedEvent
{
   private String key;
   private Object oldValue;
   private Object newValue;
   
   public SettingsChangedEvent(String key, Object oldValue, Object newValue)
   {
      if (key == null)
         throw new NullPointerException();
      
      this.key = key;
      this.oldValue = oldValue;
      this.newValue = newValue;
   }

   public String getKey()
   {
      return this.key;
   }

   public Object getNewValue()
   {
      return this.newValue;
   }

   public Object getOldValue()
   {
      return this.oldValue;
   }
}
