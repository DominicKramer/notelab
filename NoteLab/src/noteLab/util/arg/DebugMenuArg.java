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

import noteLab.util.InfoCenter;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsUtilities;

public class DebugMenuArg extends Argument
{
   public DebugMenuArg()
   {
      super(SettingsKeys.DEBUG_MENU_KEY, 
            0, new ParamInfo[0], 
            "Activates the debug menu.  This argument is intended to be used " +
            "by software developers to locate and fix problems in the "+InfoCenter.getAppName()+
            " application itself and should NOT be used in a production setting " +
            "since it may reduce "+InfoCenter.getAppName()+"'s stability and performance.", 
            false);
   }

   @Override
   public ArgResult decode(String[] args)
   {
      SettingsUtilities.setShowDebugMenu(true);
      
      return ArgResult.SHOW_GUI;
   }
}
