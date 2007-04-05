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
import noteLab.util.settings.SettingsManager;

public class UnitScaleArg extends Argument
{
   private static final String DESC = "When "+InfoCenter.getAppName()+
                                      " renders a one inch or centimeter " +
                                      "line on the screen, the line is " +
                                      "one inch or centimeter long.  If this " +
                                      "scale factor is set to 'x', then a one " +
                                      "inch or centimeter line will be 'x' " +
                                      "inches or centimeters when rendered " +
                                      "on the screen.";
   
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[] 
                           {
                              new ParamInfo("factor", 
                                            "a floating point number")
                           };
   
   public UnitScaleArg()
   {
      super(SettingsKeys.UNIT_SCALE_FACTOR, 1, PARAM_DESCS, DESC, false);
   }
   
   public String encode(float size)
   {
      return PREFIX+getIdentifier()+" "+size;
   }
   
   @Override
   public boolean decode(String[] args)
   {
      Float size = null;
      try
      {
         size = Float.parseFloat(args[0]);
      }
      catch (NumberFormatException e)
      {
         size = null;
      }
      
      if (size == null)
      {
         System.out.println("Error:  The string '"+args[0]+"' does not " +
                            "correspond to a floating point number.");
         
         return false;
      }
      
      SettingsManager.getSharedInstance().setValue(getIdentifier(), size);
      return true;
   }
}
