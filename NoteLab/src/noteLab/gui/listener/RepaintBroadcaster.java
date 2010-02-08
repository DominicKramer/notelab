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

import java.util.Iterator;
import java.util.Vector;

public class RepaintBroadcaster implements RepaintBroadcastCenter, 
                                           Iterable<RepaintListener>
{
   private Vector<RepaintListener> listeners;
   
   public RepaintBroadcaster()
   {
      this.listeners = new Vector<RepaintListener>();
   }
   
   public void addRepaintListener(RepaintListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listeners.contains(listener))
         this.listeners.add(listener);
   }
   
   public void removeRepaintListener(RepaintListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listeners.remove(listener);
   }
   
   public Iterator<RepaintListener> iterator()
   {
      return this.listeners.iterator();
   }
   
   public void doRedraw()
   {
      for (RepaintListener listener : this.listeners)
         listener.redraw();
   }
   
   public void doRedraw(float x, float y, float width, float height, 
                        float delta)
   {
      x -= delta;
      y -= delta;
      
      width += 2*delta;
      height += 2*delta;
      
      for (RepaintListener listener : this.listeners)
         listener.redraw(x, y, width, height);
   }
   
   public void doRedrawOverlay(float x, float y, float width, float height, 
                               float delta)
   {
      x -= delta;
      y -= delta;
      
      width += 2*delta;
      height += 2*delta;
      
      for (RepaintListener listener : this.listeners)
         listener.redrawOverlay(x, y, width, height);
   }
}
