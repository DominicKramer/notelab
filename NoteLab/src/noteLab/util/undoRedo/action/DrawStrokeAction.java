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

import java.awt.geom.Rectangle2D;
import java.util.Vector;

import noteLab.model.Page;
import noteLab.model.Stroke;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.geom.RectangleUnioner;

public class DrawStrokeAction extends CanvasAction
{
   private Vector<Stroke> strokeVec;
   private Page page;
   
   public DrawStrokeAction(CompositeCanvas canvas, Stroke stroke, Page page)
   {
      this(canvas, constructStrokeVec(stroke), page);
   }
   
   private static Vector<Stroke> constructStrokeVec(Stroke stroke)
   {
      if (stroke == null)
         throw new NullPointerException();
      
      Vector<Stroke> strokeVec = new Vector<Stroke>(1);
      strokeVec.add(stroke);
      return strokeVec;
   }
   
   public DrawStrokeAction(CompositeCanvas canvas, 
                           Vector<Stroke> strokeVec, 
                           Page page)
   {
      super(canvas);
      
      if (strokeVec == null || page == null)
         throw new NullPointerException();
      
      this.strokeVec = strokeVec;
      this.page = page;
   }
   
   public void run()
   {
      CompositeCanvas canvas = getCompositeCanvas();
      float zoomLevel = canvas.getZoomLevel();
      
      RectangleUnioner unioner = new RectangleUnioner();
      
      float maxWidth = 0;
      for (Stroke stroke : this.strokeVec)
      {
         stroke.scaleTo(zoomLevel, zoomLevel);
         this.page.addStroke(stroke);
         stroke.setSelected(false);
         
         maxWidth = Math.max(maxWidth, stroke.getPen().getWidth());
         
         unioner.union(stroke.getBounds2D());
      }
      
      Rectangle2D.Float union = unioner.getUnion();
      canvas.doRepaint((float)union.getX()+this.page.getX(), 
                       (float)union.getY()+this.page.getY(), 
                       (float)union.getWidth(), 
                       (float)union.getHeight(), 
                       maxWidth);
   }
}
