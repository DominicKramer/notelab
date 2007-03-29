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

package noteLab.gui.listener;

public class ValueChangeEvent<V, S>
{
   private V prevVal;
   private V curVal;
   private S source;
   
   public ValueChangeEvent(V prevVal, V curVal, S source)
   {
      if (prevVal == null || curVal == null || source == null)
         throw new NullPointerException();
      
      this.prevVal = prevVal;
      this.curVal = curVal;
      this.source = source;
   }
   
   public V getPreviousValue()
   {
      return this.prevVal;
   }
   
   public V getCurrentValue()
   {
      return this.curVal;
   }
   
   public S getSource()
   {
      return this.source;
   }
}
