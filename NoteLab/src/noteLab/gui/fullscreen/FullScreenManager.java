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

package noteLab.gui.fullscreen;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Vector;

public class FullScreenManager
{
   private static FullScreenManager FULLSCREEN_MANGER = 
                                       new FullScreenManager();
   
   private Vector<FullScreenListener> listenerVec;
   
   private FullScreenManager()
   {
      this.listenerVec = new Vector<FullScreenListener>();
   }
   
   public static FullScreenManager getSharedInstance()
   {
      return FULLSCREEN_MANGER;
   }
   
   public void addFullScreenListener(FullScreenListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeFullScreenListener(FullScreenListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public void revokeFullScreenMode()
   {
      GraphicsDevice screen = GraphicsEnvironment.
                                 getLocalGraphicsEnvironment().
                                    getDefaultScreenDevice();
      
      if (screen.getFullScreenWindow() != null)
      {
         screen.setFullScreenWindow(null);
         
         for (FullScreenListener listener : this.listenerVec)
            listener.fullScreenRevoked();
      }
   }
}
