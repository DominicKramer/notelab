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

package noteLab.gui.settings;

import java.io.File;

import noteLab.util.InfoCenter;

public class SettingsInfoCenter
{
   private static final String DEFAULT_INITENV_NAME = "initenv"+
                                                         InfoCenter.
                                                            getScriptExtension();
   
   private static final String NOTELAB_ARGS_VAR_NAME = "NOTELAB_ARGS";
   
   private static final File NOTELAB_SETTINGS_FILE;
   static
   {
      String filename = System.getProperty("NOTELAB_SETTINGS_FILENAME");
      if (filename == null)
      {
         File appHomeDir = InfoCenter.getAppHome();
         File initEnvFile = new File(appHomeDir, DEFAULT_INITENV_NAME);
         NOTELAB_SETTINGS_FILE = initEnvFile;
      }
      else
         NOTELAB_SETTINGS_FILE = new File(filename);
   }
   
   private SettingsInfoCenter() {}
   
   public static File getSettingsFile()
   {
      return NOTELAB_SETTINGS_FILE;
   }
   
   public static String getAppArgsVarName()
   {
      return NOTELAB_ARGS_VAR_NAME;
   }
}
