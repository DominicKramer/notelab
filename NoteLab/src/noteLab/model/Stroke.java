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

package noteLab.model;

import java.awt.geom.Rectangle2D;

import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Pen;
import noteLab.util.CopyReady;
import noteLab.util.Selectable;
import noteLab.util.geom.ItemContainer;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.Renderable;
import noteLab.util.render.Renderer2D;

public class Stroke 
                extends ItemContainer<Path> 
                           implements Renderable, CopyReady<Stroke>, 
                                      Selectable
{
   private Pen pen;
   private boolean isSelected;
   
   public Stroke(Pen pen, Path path)
   {
      super(path.getXScaleLevel(), path.getYScaleLevel());
      if (path == null || pen == null)
         throw new NullPointerException();
      
      addItem(path);
      setPen(pen);
      this.isSelected = false;
   }
   
   public boolean isSelected()
   {
      return this.isSelected;
   }
   
   public void setSelected(boolean selected)
   {
      if (this.isSelected == selected)
         return;
      
      this.isSelected = selected;
      notifyModListeners(ModType.Other);
   }
   
   public Pen getPen()
   {
      return this.pen;
   }
   
   public void setPen(Pen pen)
   {
      if (pen == null)
         throw new NullPointerException();
      
      if (this.pen != null)
         this.pen.removeModListener(this);
      
      this.pen = pen;
      this.pen.addModListener(this);
      
      notifyModListeners(ModType.Other);
   }
   
   public Path getPath()
   {
      return getFirst();
   }
   
   public void setPath(Path path)
   {
      if (path == null)
         throw new NullPointerException();
      
      clear();
      addItem(path);
   }
   
   public boolean containsPoint(FloatPoint2D point)
   {
      if (point == null)
         throw new NullPointerException();
      
      Path path = getPath();
      int numPts = path.getNumItems();
      if (numPts <= 2)
         return FloatPoint2D.lineContainsPoint(path.getFirst(), 
                                               path.getLast(), 
                                               point);
      
      for (int i=0; i<numPts-2; i++)
         if (FloatPoint2D.lineContainsPoint(path.getItemAt(i), 
                                            path.getItemAt(i+1), 
                                            point))
            return true;
      
      return false;
   }
   
   public Stroke getCopy()
   {
      Stroke copy = new Stroke(getPen().getCopy(), 
                               getPath().getCopy());
      copy.setSelected(this.isSelected);
      
      for (ModListener listener : super.modListenerVec)
         copy.addModListener(listener);
      
      return copy;
   }
   
   public void renderInto(Renderer2D mG2d)
   {
      if (mG2d == null)
         throw new NullPointerException();
      
      mG2d.beginGroup(Stroke.this, "", 
                      super.xScaleLevel, super.yScaleLevel);
      
      mG2d.tryRenderBoundingBox(Stroke.this);
      
      if (this.isSelected)
         mG2d.setSelected(true);
      
      this.pen.adjustRenderer(mG2d);
      
      // If the stroke isn't selected just draw its path.  
      // Otherwise if it is selected, every other segment 
      // of the path is drawn to look selected.  This 
      // has a better appearance than drawing every 
      // segment of the path in the selected style.
      if (!this.isSelected)
         mG2d.drawPath(getPath());
      else
      {
         Path path = getPath();
         int numPts = path.getNumItems();
         
         if (numPts == 1)
         {
            FloatPoint2D pt1 = path.getFirst();
            mG2d.drawLine(pt1, pt1);
         }
         else
         {
            int increment = 1;
            if (this.isSelected && numPts > 2)
               increment = 2;
            
            FloatPoint2D pt1;
            FloatPoint2D pt2;
            for (int i=0; i<numPts-increment; i=i+increment)
            {
               pt1 = path.getItemAt(i);
               pt2 = path.getItemAt(i+1);
               if (pt1 != null && pt2 != null)
                  mG2d.drawLine(pt1, pt2);
            }
            
            if (this.isSelected && (numPts%2==1) )
            {
               FloatPoint2D last = path.getLast();
               if (last != null)
                  mG2d.drawLine(last, last);
            }
         }
         
         if (this.isSelected)
            mG2d.setSelected(false);
      }
      
      mG2d.endGroup(Stroke.this);
   }

   @Override
   public void scaleBy(float x, float y)
   {
      super.scaleBy(x, y);
      this.pen.scaleBy(Math.max(x, y));
   }

   @Override
   public void scaleTo(float x, float y)
   {
      super.scaleTo(x, y);
      this.pen.scaleTo(Math.max(x, y));
   }
   
   @Override
   public void resizeTo(float x, float y)
   {
      super.resizeTo(x, y);
      this.pen.resizeTo(Math.max(x, y));
   }

   @Override
   public void translateBy(float x, float y)
   {
      super.translateBy(x, y);
   }

   @Override
   public void translateTo(float x, float y)
   {
      super.translateTo(x, y);
   }
   
   @Override
   public Rectangle2D.Float getBounds2D()
   {
      Rectangle2D.Float bounds = super.getBounds2D();
      
      // modify the bounds to also account for the width of the current 
      // pen's stroke
      float penWidth = this.pen.getWidth();
      if (this.isSelected)
         penWidth = Renderer2D.getStrokeWidth(penWidth, true);
      
      // Half of the pen's width
      float penWidthHalf = penWidth/2;
      
      double x = bounds.getX();
      double y = bounds.getY();
      double width = bounds.getWidth();
      double height = bounds.getHeight();
      
      // Shift the box left and up half of the pen's width 
      // and increase the width and height by the pen's width.  
      // Also, shift the box up and to the left one additional 
      // pixel and increase its width and height by 2 pixels to 
      // account for antialiasing.
      bounds.setRect(x-penWidthHalf-1, y-penWidthHalf-1, 
                     width+penWidth+2, height+penWidth+2);
      
      return bounds;
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getSimpleName());
      buffer.append(":  [");
      buffer.append(this.pen.toString());
      buffer.append("] [");
      buffer.append(getPath().toString());
      buffer.append("]");
      
      return buffer.toString();
   }
}
