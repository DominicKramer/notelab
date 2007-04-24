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

public abstract class Argument
{
   public static final String PREFIX = "--";
   
   private String id;
   private int numArgs;
   private String desc;
   private ParamInfo[] paramInfoArr;
   private boolean isDeprecated;
   
   public Argument(String id, int numArgs, 
                   ParamInfo[] paramInfoArr, String desc, 
                   boolean isDeprecated)
   {
      if (id == null || paramInfoArr == null || desc == null)
         throw new NullPointerException();
      
      if (numArgs < 0)
         throw new IllegalArgumentException("The number of arguments " +
                                            "cannot be negative");
      
      this.id = id;
      this.numArgs = numArgs;
      this.paramInfoArr = paramInfoArr;
      this.desc = desc;
      this.isDeprecated = isDeprecated;
   }
   
   public boolean isDeprecated()
   {
      return this.isDeprecated;
   }
   
   public String getIdentifier()
   {
      return this.id;
   }
   
   public int getNumArgs()
   {
      return this.numArgs;
   }
   
   public ParamInfo[] getParamDescriptions()
   {
      return this.paramInfoArr;
   }
   
   public String getDescription()
   {
      return this.desc;
   }
   
   public abstract ArgResult decode(String[] args);
}
