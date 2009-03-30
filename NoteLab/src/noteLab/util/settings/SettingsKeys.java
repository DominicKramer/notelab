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

public interface SettingsKeys
{
   public static final String PEN_1_SIZE_KEY = "pen1Size";
   public static final String PEN_2_SIZE_KEY = "pen2Size";
   public static final String PEN_3_SIZE_KEY = "pen3Size";
   
   public static final String PEN_1_COLOR_KEY = "pen1Color";
   public static final String PEN_2_COLOR_KEY = "pen2Color";
   public static final String PEN_3_COLOR_KEY = "pen3Color";
   
   public static final String PAPER_TYPE_KEY = "paperType";
   public static final String PAPER_COLOR_KEY = "paperColor";
   public static final String PAPER_SIZE_KEY = "paperSize";
   
   public static final String HISTORY_SIZE_KEY = "historySize";
   
   public static final String COMB_FACTOR = "combFactor";
   public static final String UNIT_SCALE_FACTOR = "unitFactor";
   
   public static final String SMOOTH_FACTOR = "smoothFactor";
   
   public static final String DEBUG_MENU_KEY = "debugMenu";
   
   //The keys below are not implemented yet
   public static final String WINDOW_SIZE = "windowSize";
   public static final String WINDOW_LOCATION = "windowLocation";
   
   public static final String ANTIALIAS = "antialias";
   
   public static final String ZOOM_LEVEL = "zoom";
   
   public static final String CURRENT_DIR = "currentDirectory";
   
   public static final String DISPLAY_BOUNDING_BOX = "displayBounds";
   public static final String DISPLAY_UPDATE_BOX = "displayUpdates";
   public static final String DISABLE_PAPER = "disablePaper";
   public static final String NOTIFY_OF_REPAINTS = "notifyRepaints";
   public static final String USE_CACHING = "useCaching";
   public static final String FORCE_GLOBAL_REPAINTS = "forceGlobalRepaints";
   public static final String DISPLAY_KNOTS = "displayKnots";
}
