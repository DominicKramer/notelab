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
import noteLab.util.undoRedo.action.UndoRedoConstants;

public class HistorySizeArg extends Argument implements UndoRedoConstants
{
   private static final ParamInfo[] PARAM_DESCS = new ParamInfo[1];
   static
   {
      PARAM_DESCS[0] = new ParamInfo("size", 
                                     "Specifies the number of " +
                                     "actions that can be undone.");
   }
   
   private static final String DESC = "Used to set the number of actions " +
                                      "that can be undone.";
   
   public HistorySizeArg()
   {
      super(SettingsKeys.HISTORY_SIZE_KEY, 1, PARAM_DESCS, DESC);
   }
   
   public String encode(int size)
   {
      return PREFIX+getIdentifier()+" "+size;
   }
   
   @Override
   public boolean decode(String[] args)
   {
      int size = DEFAULT_HISTORY_SIZE;
      try
      {
         size = Integer.parseInt(args[0]);
         
         if (size < 0)
         {
            System.out.println("Error:  The history size cannot be " +
                               "negative.  The size specified was '" + 
                               size+"'");
            
            return false;
         }
      }
      catch (NumberFormatException e)
      {
         System.out.println("Error:  '"+args[0]+
                            "' is not a valid history size because " +
                            "it does not represent an integer between " +
                            "0 and "+Integer.MAX_VALUE+".");
         
         return false;
      }
      
      SettingsManager.getSharedInstance().
                         setValue(SettingsKeys.HISTORY_SIZE_KEY, size);
      
      return true;
   }
}
