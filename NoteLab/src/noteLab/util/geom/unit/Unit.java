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

package noteLab.util.geom.unit;

import java.awt.Toolkit;

import noteLab.util.io.noteLab.NoteLabFileConstants;
import noteLab.util.settings.SettingsUtilities;


/**
 * This class serves to formally define a list of known units.
 * 
 * @author Dominic Kramer
 */
public enum Unit implements NoteLabFileConstants
{
   /** Defines that a value is in terms of pixels. */
   PIXEL, 
   
   /** Defines that a value is in terms of inches. */
   INCH, 
   
   /** Defines that a value is in terms of centimeters. */
   CM;
   
   /** Specifies the conversion ratio from centimeters to inches. */
   private static final float CM_PER_INCH = 2.54f;
   
   @Override
   public String toString()
   {
      switch (this)
      {
         case PIXEL:
            return PIXEL_UNIT_NAME;
         case INCH:
            return INCH_UNIT_NAME;
         case CM:
            return CM_UNIT_NAME;
         default:
            return "";
      }
   }
   
   public static int getScreenResolution()
   {
      return Toolkit.getDefaultToolkit().getScreenResolution();
   }
   
   public static float getValue(float curVal, 
                                Unit curUnit, 
                                Unit newUnit, 
                                int screenRes)
   {
      return getValue(curVal, curUnit, newUnit, screenRes, SettingsUtilities.getUnitScaleFactor());
   }
   
   /**
    * Given a value and the current unit that it is represented in, this 
    * method is used to get the value converted in terms of the new unit 
    * specified.
    * 
    * @param curVal The value to convert.
    * @param curUnit The unit that the value is represented in.
    * @param newUnit The unit that the value should be converted in terms 
    *                of.
    * @return The specified value converted in terms of the new unit 
    *         specified.
    */
   public static float getValue(float curVal, 
                                Unit curUnit, 
                                Unit newUnit, 
                                int screenRes, 
                                float unitScaleFactor)
   {
      if (curUnit == newUnit)
         return unitScaleFactor*curVal;
      
      // Note:  'screenRes' gives the number of pixels per inch
      
      if (curUnit == Unit.PIXEL)
      {
         if (newUnit == Unit.INCH)
            return unitScaleFactor*curVal/screenRes;
         else 
            return unitScaleFactor*CM_PER_INCH*curVal/screenRes;
      }
      else if (curUnit == Unit.CM)
      {
         if (newUnit == Unit.PIXEL)
            return unitScaleFactor*(curVal/CM_PER_INCH)*screenRes;
         else
            return unitScaleFactor*curVal/CM_PER_INCH;
      }
      else if (curUnit == Unit.INCH)
      {
         if (newUnit == Unit.PIXEL)
            return unitScaleFactor*curVal*screenRes;
         else
            return unitScaleFactor*curVal*CM_PER_INCH;
      }
      
      return Float.NaN;
   }
}
