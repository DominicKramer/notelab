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

package noteLab.gui.settings.panel.base;

import noteLab.util.settings.SettingsManager;

public abstract class ManagedSettingsPanel extends PrimitiveSettingsPanel
{
   private final String key;
   private final Object defaultVal;
   private final Object initVal;
   private Object curVal;
   
   public ManagedSettingsPanel(String title, String info, 
                               String key, Object defaultVal)
   {
      super(title, info);
      
      if (title == null || info == null || key == null || defaultVal == null)
         throw new NullPointerException();
      
      if (title == null || info == null || key == null || defaultVal == null)
         throw new NullPointerException();
      
      this.key = key;
      this.defaultVal = defaultVal;
      
      Object value = SettingsManager.getSharedInstance().getValue(key);
      this.initVal = (value != null)?value:this.defaultVal;
      
      this.curVal = this.initVal;
   }
   
   public String getKey()
   {
      return this.key;
   }
   
   public Object getCurrentValue()
   {
      return this.curVal;
   }
   
   public void setCurrentValue(Object curVal)
   {
      if (curVal == null)
         throw new NullPointerException();
      
      this.curVal = curVal;
   }
   
   public Object getDefaultValue()
   {
      return this.defaultVal;
   }
   
   public Object getInitialValue()
   {
      return this.initVal;
   }
   
   public void restoreDefaults()
   {
      this.curVal = this.defaultVal;
      updateDisplay(this.curVal);
   }
   
   public void revertToSaved()
   {
      this.curVal = this.initVal;
      updateDisplay(this.curVal);
   }
   
   public void apply()
   {
      if (this.key == null || this.curVal == null)
         return;
      
      SettingsManager.getSharedInstance().setValue(this.key, this.curVal);
   }
   
   public abstract void updateDisplay(Object curVal);
}
