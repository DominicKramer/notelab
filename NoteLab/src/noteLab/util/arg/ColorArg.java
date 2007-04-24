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

import java.awt.Color;

import noteLab.util.settings.SettingsManager;

public class ColorArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[]
                           {
                              new ParamInfo("red", 
                                            "An integer between 0 and 255 " +
                                            "inclusive."),
                              
                              new ParamInfo("green", 
                                            "An integer between 0 and 255 " +
                                            "inclusive."), 

                              new ParamInfo("blue", 
                                            "An integer between 0 and 255 " +
                                            "inclusive.")
                           };
   
   public ColorArg(String id, String desc)
   {
      super(id, 3, PARAM_DESCS, desc, false);
   }
   
   public String encode(Color color)
   {
      int red   = color.getRed();
      int green = color.getGreen();
      int blue  = color.getBlue();
      
      return PREFIX+getIdentifier()+" "+red+" "+green+" "+blue;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      Integer num0 = getInt(args[0]);
      if (num0 == null)
         return ArgResult.ERROR;
      
      Integer num1 = getInt(args[1]);
      if (num1 == null)
         return ArgResult.ERROR;
      
      Integer num2 = getInt(args[2]);
      if (num2 == null)
         return ArgResult.ERROR;
      
      Color col = new Color(num0.intValue(), 
                            num1.intValue(), 
                            num2.intValue());
      
      SettingsManager.getSharedInstance().setValue(getIdentifier(), col);
      
      return ArgResult.SHOW_GUI;
   }
   
   private Integer getInt(String valueStr)
   {
      Integer bigInt = null;
      try
      {
         bigInt = Integer.parseInt(valueStr);
      }
      catch (NumberFormatException e)
      {
         bigInt = null;
      }
      
      if (bigInt == null)
      {
         System.out.println("Error:  The string '"+valueStr+"' does not " +
                            "correspond to an integer.");
      }
      else
      {
         int val = bigInt.intValue();
         if (val < 0 || val > 255)
         {
            System.out.println("Error:  The value '"+val+"' must be an " +
                               "integer in the range 0 to 255 inclusive.");
            
            return null;
         }
      }
      
      return bigInt;
   }
}
