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

public class DebugArgGenerator implements SettingsKeys
{
   private static final ParamInfo[] PARAM_DESCS = new ParamInfo[0];
   private static final String NOTICE_STR = 
                                  "Note:  This argument is meant only for " +
                                  "debugging purposes.  Its behavior is not "+
                                  "garaunteed to substain maximum " +
                                  "application performance.";
   
   private DebugArgGenerator() {}
   
   public static Argument[] generateDebugArgs()
   {
      BooleanArg[] argArr = new BooleanArg[4];
        argArr[0] = new BooleanArg(DISPLAY_BOUNDING_BOX, 
                                   NOTICE_STR+
                                   "Specifies that as items are drawn on " +
                                   "the screen their bounding boxes " +
                                   "should be drawn also.");
        argArr[1] = new BooleanArg(DISPLAY_UPDATE_BOX, 
                                   NOTICE_STR+
                                   "Specifies that regions of the screen "+
                                   "are identified as they are repainted by "+
                                   "the application.");
        argArr[2] = new BooleanArg(DISABLE_PAPER, 
                                   NOTICE_STR+
                                   "Specifies that the paper associated " +
                                   "with a page should not be rendered.");
        argArr[3] = new BooleanArg(NOTIFY_OF_REPAINTS, 
                                   NOTICE_STR+
                                   "Specifies that a message should be " +
                                   "printed to standard output every time " +
                                   "the entire screen is repainted.");
      return argArr;
   }
   
   private static class BooleanArg extends Argument
   {
      public BooleanArg(String key, String desc)
      {
         super(key, 0, PARAM_DESCS, desc, false);
      }
      
      @Override
      public boolean decode(String[] args)
      {
         SettingsManager.getSharedInstance().
                            setValue(getIdentifier(), true);
         return true;
      }
      
   }
}
