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

import noteLab.model.Paper.PaperType;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class PaperTypeArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = new ParamInfo[1];
   static
   {
      StringBuffer buffer = new StringBuffer("Either:  ");
      
      PaperType[] types = PaperType.values();
      for (int i=0; i<types.length; i++)
      {
         buffer.append(types[i].toString());
         if (i == types.length-2)
            buffer.append(", or ");
         else if (i != types.length-1)
         buffer.append(", ");
      }
      
      PARAM_DESCS[0] = new ParamInfo("type", buffer.toString());
   }
   
   private static final String DESC = "Used to set the paper type.";
   
   public PaperTypeArg()
   {
      super(SettingsKeys.PAPER_TYPE_KEY, 1, PARAM_DESCS, DESC, false);
   }
   
   public String encode(PaperType type)
   {
      if (type == null)
         throw new NullPointerException();
      
      return PREFIX+getIdentifier()+" "+type;
   }
   
   @Override
   public boolean decode(String[] args)
   {
      PaperType setType = null;
      PaperType[] types = PaperType.values();
      for (PaperType type : types)
      {
         if (type.toString().equals(args[0]))
         {
            setType = type;
            break;
         }
      }
      
      if (setType == null)
      {
         System.out.print("Error:  The paper type '"+args[0]+"' is not " +
                          "valid.  The only possible types are:  ");
         for (PaperType type : types)
            System.out.print("'"+type.toString()+"' ");
         System.out.println();
         
         return false;
      }
      
      SettingsManager.getSharedInstance().
                         setValue(SettingsKeys.PAPER_TYPE_KEY, setType);
      return true;
   }
}
