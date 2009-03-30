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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SettingsManager
{
   private static final SettingsManager SHARED_MANAGER = 
                                           new SettingsManager();
   
   private Hashtable<String, Object> settingsTable;
   private Vector<SettingsChangedListener> listenerVec;
   
   private SettingsManager()
   {
      this.settingsTable = new Hashtable<String, Object>();
      this.listenerVec = new Vector<SettingsChangedListener>();
   }
   
   public static SettingsManager getSharedInstance()
   {
      return SHARED_MANAGER;
   }
   
   public void addSettingsListener(SettingsChangedListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeSettingsListener(SettingsChangedListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public Object getValue(String key)
   {
      return this.settingsTable.get(key);
   }
   
   public void setValue(String key, Object value)
   {
      Object oldVal = getValue(key);
      
      this.settingsTable.put(key, value);
      
      SettingsChangedEvent event = 
                              new SettingsChangedEvent(key, oldVal, value);
      for (SettingsChangedListener listener : this.listenerVec)
         listener.settingsChanged(event);
   }
   
   public Enumeration<String> getKeys()
   {
      return this.settingsTable.keys();
   }
   
   public int getSize()
   {
      return this.settingsTable.size();
   }
   
   public void notifyOfChanges()
   {
      Enumeration<String> keys = this.settingsTable.keys();
      String key;
      Object value;
      while (keys.hasMoreElements())
      {
         key = keys.nextElement();
         value = getValue(key);
         
         for (SettingsChangedListener listener : this.listenerVec)
            listener.settingsChanged(
                     new SettingsChangedEvent(key, value, value));
      }
   }
}
