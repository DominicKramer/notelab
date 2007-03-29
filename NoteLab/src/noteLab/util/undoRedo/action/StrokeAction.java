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

package noteLab.util.undoRedo.action;

import noteLab.model.Stroke;

public class StrokeAction implements HistoryAction
{
   private Stroke stroke;
   private PenAction penAction;
   private float xScale;
   private float yScale;
   
   public StrokeAction(Stroke stroke)
   {
      if (stroke == null)
         throw new NullPointerException();
      
      this.stroke = stroke;
      this.penAction = new PenAction(this.stroke.getPen());
      
      this.xScale = this.stroke.getXScaleLevel();
      this.yScale = this.stroke.getYScaleLevel();
      
   }
   
   public Stroke getStroke()
   {
      return this.stroke;
   }

   public void run()
   {
      this.stroke.scaleTo(this.xScale, this.yScale);
      this.penAction.run();
   }
}
