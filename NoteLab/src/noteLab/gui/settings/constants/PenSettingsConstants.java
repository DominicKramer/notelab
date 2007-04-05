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

package noteLab.gui.settings.constants;

import java.awt.Color;

import noteLab.gui.GuiSettingsConstants;

public interface PenSettingsConstants extends GuiSettingsConstants
{
   public static final double MIN_SIZE_PX = 0.1;
   public static final double MAX_SIZE_PX = BUTTON_SIZE;
   public static final double STEP_SIZE_PX = 0.1;
   
   public static final double FINE_SIZE_PX = 1.4;
   public static final double MEDIUM_SIZE_PX = 4;
   public static final double THICK_SIZE_PX = 8;
   
   public static final Color PEN_1_COLOR = Color.BLACK;
   public static final Color PEN_2_COLOR = Color.BLUE;
   public static final Color PEN_3_COLOR = Color.RED;
   
   public static final int SMOOTH_FACTOR = 5;//4;
}
