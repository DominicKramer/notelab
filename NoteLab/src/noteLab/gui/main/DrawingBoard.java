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

package noteLab.gui.main;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.Scrollable;

import noteLab.model.canvas.CompositeCanvas;

public class DrawingBoard implements Scrollable
{
   private static final int SCROLL_STEP = 20;
   
   private CompositeCanvas canvas;
   
   public DrawingBoard(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
   }
   
   public CompositeCanvas getCompositeCanvas()
   {
      return this.canvas;
   }
   
   public Dimension getPreferredScrollableViewportSize()
   {
      Rectangle2D bounds = canvas.getPreferredSize();
      return new Dimension(1+(int)bounds.getWidth(), 
                           1+(int)bounds.getHeight());
   }

   public int getScrollableBlockIncrement(Rectangle visibleRect, 
                                          int orientation, 
                                          int direction)
   {
      return SCROLL_STEP;
   }

   public boolean getScrollableTracksViewportHeight()
   {
      return false;
   }

   public boolean getScrollableTracksViewportWidth()
   {
      return false;
   }

   public int getScrollableUnitIncrement(Rectangle visibleRect, 
                                         int orientation, 
                                         int direction)
   {
      return SCROLL_STEP;
   }
}
