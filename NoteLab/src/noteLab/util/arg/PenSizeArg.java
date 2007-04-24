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

import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;
import noteLab.util.settings.SettingsManager;

public class PenSizeArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = 
                           new ParamInfo[] 
                           {
                              new ParamInfo("size", 
                                            "a floating point number"), 
                              new ParamInfo("unit", 
                                            "either cm, in, or px")
                           };
   
   public PenSizeArg(String penSizeKey, int penNum)
   {
      super(penSizeKey, 2, PARAM_DESCS, getDesc(penNum), false);
   }
   
   private static String getDesc(int penNum)
   {
      return "Used to set the size of pen "+penNum+".";
   }
   
   public String encode(MValue value)
   {
      Unit unit = value.getUnit();
      return PREFIX+getIdentifier()+" "+value.getValue(unit)+" "+unit;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      String strVal = args[0];
      Float size = null;
      try
      {
         size = Float.parseFloat(strVal);
      }
      catch (NumberFormatException e)
      {
         size = null;
      }
      
      if (size == null)
      {
         System.out.println("Error:  The string '"+strVal+"' does not " +
                            "correspond to a floating point number.");
         return ArgResult.ERROR;
      }
      
      String unitStr = args[1];
      Unit[] units = Unit.values();
      
      int unitIndex = -1;
      for (int i=0; i<units.length; i++)
         if (units[i].toString().equals(unitStr))
            unitIndex = i;
      
      if (unitIndex == -1)
      {
         System.out.print("Error:  The unit '"+unitStr+"' is not valid."+
                          "Possible values are: ");
         
         for (Unit unit : units)
            System.out.print("'"+unit.toString()+"' ");
         
         System.out.println();
         return ArgResult.ERROR;
      }
      
      MValue value = new MValue(size.floatValue(), units[unitIndex]);
      SettingsManager.getSharedInstance().setValue(getIdentifier(), value);
      
      return ArgResult.SHOW_GUI;
   }
}
