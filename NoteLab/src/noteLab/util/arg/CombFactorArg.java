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

import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class CombFactorArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[] 
                           {
                              new ParamInfo("comb factor", 
                                            "a floating point number"), 
                           };
   private static final String DESC = "Specifies how finely small bends are " +
                                      "combed out of strokes.  The smaller the " +
                                      "number the finer the comb.";
   
   public CombFactorArg()
   {
      super(SettingsKeys.COMB_FACTOR, 1, PARAM_DESCS, DESC, true);
   }
   
   public String encode(int combFactor)
   {
      return PREFIX+getIdentifier()+" "+combFactor;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      String strVal = args[0];
      Integer size = null;
      try
      {
         size = Integer.parseInt(strVal);
      }
      catch (NumberFormatException e)
      {
         size = null;
      }
      
      if (size == null)
      {
         System.out.println("Error:  The string '"+strVal+"' does not " +
                            "correspond to an integer valued number.");
         return ArgResult.ERROR;
      }
      
      SettingsManager.getSharedInstance().setValue(getIdentifier(), size);
      
      return ArgResult.SHOW_GUI;
   }
}
