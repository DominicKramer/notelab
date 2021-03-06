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
import noteLab.util.settings.SettingsUtilities;

public class SmoothFactorArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[] 
                           {
                              new ParamInfo("factor", 
                                            "a nonnegative integer"), 
                           };
   // TODO:  Change this description when the smoothing algorithm is 
   //        finalized.
   private static final String DESC = "Specifies to what extent strokes drawn " +
                                      "are smoothed.  A value of zero specifies " +
                                      "strokes should not be smoothed and the " +
                                      "larger the value the more strokes are smoothed.  " +
                                      "The only consideration is that strokes can only be " +
                                      "smoothed if they have enough points " +
                                      "(specifically twice the value of this argument).  " +
                                      "Thus if the value given is too large most " +
                                      "strokes may not be smoothed.";
   
   public SmoothFactorArg()
   {
      super(SettingsKeys.SMOOTH_FACTOR, 1, PARAM_DESCS, DESC, false);
   }
   
   public String encode(int smoothFactor)
   {
      return PREFIX+getIdentifier()+" "+smoothFactor;
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
                            "correspond to an integer.");
         return ArgResult.ERROR;
      }
      
      try
      {
         /* With the new smoothing algorithm there is no upper limit for 
          * the smoothing factor.
         if (size > 5)
         {
            System.out.println("The smooth factor "+size+
                               " is invalid since it must be a whole number between 1 and 5 " +
                               "inclusive.  The maximum value of 5 will be used.");
            size = 5;
         }
         */
         
         if (size < 0)
         {
            System.out.println("The smooth factor "+size+
                  " is invalid since it must be a non-negative whole " +
                  "number.  A value of 0 will be used.");
            size = 0;
         }
         
         SettingsUtilities.setSmoothFactor(size);
      }
      catch (IllegalArgumentException e)
      {
         System.out.println(e.getMessage());
         
         return ArgResult.ERROR;
      }
      
      return ArgResult.SHOW_GUI;
   }
}
