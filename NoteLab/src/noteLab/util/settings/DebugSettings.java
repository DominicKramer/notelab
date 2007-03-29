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

public class DebugSettings implements SettingsChangedListener, SettingsKeys
{
   private static final DebugSettings SHARED_INSTANCE = new DebugSettings();
   
   private boolean displayBoundingBox;
   private boolean displayUpdateBox;
   private boolean disablePaper;
   private boolean notifyOfRepaints;
   private boolean useCache;
   private boolean forceGlobalRepaints;
   
   private DebugSettings()
   {
      this.displayBoundingBox = false;
      this.displayUpdateBox = false;
      this.disablePaper = false;
      this.notifyOfRepaints = false;
      this.useCache = false;
      this.forceGlobalRepaints = false;
   }
   
   public static DebugSettings getSharedInstance()
   {
      return SHARED_INSTANCE;
   }
   
   public boolean displayBoundingBox()
   {
      return this.displayBoundingBox;
   }
   
   public boolean displayUpdateBox()
   {
      return this.displayUpdateBox;
   }
   
   public boolean disablePaper()
   {
      return this.disablePaper;
   }
   
   public boolean notifyOfRepaints()
   {
      return this.notifyOfRepaints;
   }
   
   public boolean useCache()
   {
      return this.useCache;
   }
   
   public boolean forceGlobalRepaints()
   {
      return this.forceGlobalRepaints;
   }
   
   public void settingsChanged(SettingsChangedEvent event)
   {
      String key = event.getKey();
      if (key.equals(DISPLAY_BOUNDING_BOX))
         this.displayBoundingBox = true;
      else if (key.equals(DISPLAY_UPDATE_BOX))
         this.displayUpdateBox = true;
      else if (key.equals(DISABLE_PAPER))
         this.disablePaper = true;
      else if (key.equals(NOTIFY_OF_REPAINTS))
         this.notifyOfRepaints = true;
      else if (key.equals(USE_CACHING))
         this.useCache = true;
      else if (key.equals(FORCE_GLOBAL_REPAINTS))
         this.forceGlobalRepaints = true;
   }
}