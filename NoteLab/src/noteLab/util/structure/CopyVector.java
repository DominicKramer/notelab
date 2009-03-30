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

package noteLab.util.structure;

import java.util.Collection;
import java.util.Vector;

import noteLab.util.CopyReady;

public class CopyVector<E extends Object & CopyReady<E>> 
                extends Vector<E> 
                           implements CopyReady<CopyVector<E>>
{
   public CopyVector()
   {
      super();
   }

   public CopyVector(Collection<? extends E> c)
   {
      super(c);
   }

   public CopyVector(int initialCapacity, int capacityIncrement)
   {
      super(initialCapacity, capacityIncrement);
   }

   public CopyVector(int initialCapacity)
   {
      super(initialCapacity);
   }

   public CopyVector<E> getCopy()
   {
      CopyVector<E> copy = new CopyVector<E>();
      for (E item : this)
         copy.add(item.getCopy());
      
      return copy;
   }
}
