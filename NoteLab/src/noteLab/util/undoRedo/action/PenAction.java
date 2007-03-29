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

import java.awt.Color;

import noteLab.model.tool.Pen;

public class PenAction implements HistoryAction
{
   private Pen pen;
   
   private Color color;
   private float width;
   private float scaleFactor;
   
   public PenAction(Pen pen)
   {
      if (pen == null)
         throw new NullPointerException();
      
      this.pen = pen;
      
      Color penColor = this.pen.getColor();
      this.color = new Color(penColor.getRGB());
      this.width = this.pen.getWidth();
      this.scaleFactor = this.pen.getScaleLevel();
   }
   
   public void run()
   {
      this.pen.setColor(this.color);
      this.pen.setWidth(this.width);
      this.pen.scaleTo(this.scaleFactor);
   }
}
