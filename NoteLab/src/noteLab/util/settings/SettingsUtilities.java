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

import java.awt.Color;

import noteLab.gui.settings.constants.PageSettingsConstants;
import noteLab.gui.settings.constants.PenSettingsConstants;
import noteLab.model.Paper.PaperType;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;

public class SettingsUtilities implements SettingsKeys
{
   private static final boolean DEFAULT_SHOW_DEBUG_MENU = false;
   
   private SettingsUtilities() {}
   
   public static float getUnitScaleFactor()
   {
      Object val = SettingsManager.getSharedInstance().getValue(UNIT_SCALE_FACTOR);
      if (val == null || !(val instanceof Number) )
         return PageSettingsConstants.DEFAULT_UNIT_SCALE_FACTOR;
      
      return ((Number)val).floatValue();
   }
   
   public static PaperType getPaperType()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PAPER_TYPE_KEY);
      if (val == null || !(val instanceof PaperType))
         return PageSettingsConstants.PAPER_TYPE;
      
      return (PaperType)val;
   }
   
   public static void setPaperType(PaperType type)
   {
      if (type == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PAPER_TYPE_KEY, type);
   }
   
   public static Color getPaperColor()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PAPER_COLOR_KEY);
      if (val == null || !(val instanceof Color))
         return PageSettingsConstants.PAPER_COLOR;
      
      return (Color)val;
   }
   
   public static void setPaperColor(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PAPER_COLOR_KEY, color);
   }
   
   public static MValue getPen1Size()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_1_SIZE_KEY);
      if (val == null || !(val instanceof MValue))
         return new MValue(PenSettingsConstants.FINE_SIZE_PX, Unit.PIXEL);
      
      return (MValue)val;
   }
   
   public static void setPen1Size(MValue size)
   {
      if (size == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_1_SIZE_KEY, size);
   }
   
   public static MValue getPen2Size()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_2_SIZE_KEY);
      if (val == null || !(val instanceof MValue))
         return new MValue(PenSettingsConstants.MEDIUM_SIZE_PX, Unit.PIXEL);
      
      return (MValue)val;
   }
   
   public static void setPen2Size(MValue size)
   {
      if (size == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_2_SIZE_KEY, size);
   }
   
   public static MValue getPen3Size()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_3_SIZE_KEY);
      if (val == null || !(val instanceof MValue))
         return new MValue(PenSettingsConstants.MAX_SIZE_PX, Unit.PIXEL);
      
      return (MValue)val;
   }
   
   public static void setPen3Size(MValue size)
   {
      if (size == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_3_SIZE_KEY, size);
   }
   
   public static Color getPen1Color()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_1_COLOR_KEY);
      if (val == null || !(val instanceof Color))
         return PenSettingsConstants.PEN_1_COLOR;
      
      return (Color)val;
   }
   
   public static void setPen1Color(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_1_COLOR_KEY, color);
   }
   
   public static Color getPen2Color()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_2_COLOR_KEY);
      if (val == null || !(val instanceof Color))
         return PenSettingsConstants.PEN_2_COLOR;
      
      return (Color)val;
   }
   
   public static void setPen2Color(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_2_COLOR_KEY, color);
   }
   
   public static Color getPen3Color()
   {
      Object val = SettingsManager.getSharedInstance().getValue(PEN_3_COLOR_KEY);
      if (val == null || !(val instanceof Color))
         return PenSettingsConstants.PEN_3_COLOR;
      
      return (Color)val;
   }
   
   public static void setPen3Color(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      SettingsManager.getSharedInstance().setValue(PEN_3_COLOR_KEY, color);
   }
   
   public static int getSmoothFactor()
   {
      Object val = SettingsManager.getSharedInstance().getValue(SMOOTH_FACTOR);
      if (val == null || !(val instanceof Integer))
         return PenSettingsConstants.SMOOTH_FACTOR;
      
      return (Integer)val;
   }
   
   public static void setSmoothFactor(int factor)
   {
      if (factor < 0)
         throw new IllegalArgumentException("The smoothing factor "+factor+
                                            " is not valid since it must be nonnegative");
      
      SettingsManager.getSharedInstance().setValue(SMOOTH_FACTOR, factor);
   }
   
   public static boolean getShowDebugMenu()
   {
      Object val = SettingsManager.getSharedInstance().getValue(DEBUG_MENU_KEY);
      if (val == null || !(val instanceof Boolean))
         return DEFAULT_SHOW_DEBUG_MENU;
      
      return (Boolean)val;
   }
   
   public static void setShowDebugMenu(boolean display)
   {
      SettingsManager.getSharedInstance().setValue(DEBUG_MENU_KEY, display);
   }
}
