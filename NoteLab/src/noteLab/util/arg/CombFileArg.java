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

@Deprecated
// This class isn't finished yet and should not be used.
public class CombFileArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[] 
                           {
                              new ParamInfo("filename", 
                                            "the "+InfoCenter.getAppName()+
                                            " file whose strokes should be " +
                                            "combed"), 
                           };
   private static final String DESC = "Opens the given "+InfoCenter.getAppName()+
                                      " file and combs its strokes.  Then the " +
                                      "file is opened.";
   
   public CombFileArg()
   {
      super(SettingsKeys.COMB_FACTOR, 1, PARAM_DESCS, DESC);
   }
   
   @Override
   public boolean decode(String[] args)
   {
      String filename = args[0];
      
      return true;
   }
}
