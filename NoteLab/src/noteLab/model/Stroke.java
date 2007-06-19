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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Pen;
import noteLab.util.CopyReady;
import noteLab.util.Selectable;
import noteLab.util.geom.ItemContainer;
import noteLab.util.mod.ModType;
import noteLab.util.render.ImageRenderer2D;
import noteLab.util.render.Renderable;
import noteLab.util.render.Renderer2D;
import noteLab.util.render.SVGRenderer2D;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.settings.DebugSettings;

public class Stroke 
                extends ItemContainer<Path> 
                           implements Renderable, CopyReady<Stroke>, 
                                      Selectable
{
   private Pen pen;
   private boolean isSelected;
   private boolean isStable;
   private BufferedImage cacheImage;
   
   public Stroke(Pen pen, Path path)
   {
      super(path.getXScaleLevel(), path.getYScaleLevel());
      if (path == null || pen == null)
         throw new NullPointerException();
      
      addItem(path);
      setPen(pen);
      this.isSelected = false;
      this.isStable = false;
      
      this.cacheImage = null;
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
   
   public void setIsStable(boolean isStable)
   {
      this.isStable = isStable;
      
      getPath().setIsStable(isStable);
      
      if (DebugSettings.getSharedInstance().useCache())
      {
         if (isStable)
         {
            Rectangle2D bounds = getBounds2D();
            
            float delta = 2*this.pen.getWidth();
            int width  = (int)( (bounds.getWidth()+delta));
            int height = (int)( (bounds.getHeight()+delta));
            
            this.cacheImage = new BufferedImage(width, height, 
                                                BufferedImage.TYPE_INT_ARGB);
         
            ImageRenderer2D renderer = new ImageRenderer2D(this.cacheImage);
            boolean tmpIsSel = this.isSelected;
            setSelected(false);
            doRenderInto(renderer);
            setSelected(tmpIsSel);
         }
         else
            this.cacheImage = null;
      }
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
      return copy;
   }
   
   public void renderInto(Renderer2D mG2d)
   {
      if (mG2d == null)
         throw new NullPointerException();
      
      if (this.cacheImage != null && mG2d instanceof SwingRenderer2D && 
          !this.isSelected)
      {
         ((SwingRenderer2D)mG2d).drawImage(this.cacheImage);
         return;
      }
      
      // We render the stroke with a slightly larger width and slightly 
      // brighter color before rendering the stroke with its actual 
      // width and color to make the stroke look smoother.  By doing 
      // multiple renders, small anomolies in the stroke are painted over.
      
      // if the renderer is an SVG renderer don't render the stroke multiple times
      
      if (!this.isSelected && !(mG2d instanceof SVGRenderer2D))
      {
         Pen realPen = this.pen;
         
         float width = this.pen.getWidth();
         Color color = this.pen.getColor();
         float scale = this.pen.getScaleLevel();
         
         setPen(new Pen(1.5f*width, color.brighter().brighter(), scale));
         doRenderInto(mG2d);
         
         setPen(new Pen(1.25f*width, color.brighter(), scale));
         doRenderInto(mG2d);
         
         setPen(realPen);
      }
      
      doRenderInto(mG2d);
   }
   
   private void doRenderInto(Renderer2D mG2d)
   {
      mG2d.beginGroup(Stroke.this, "", 
                      super.xScaleLevel, super.yScaleLevel);
      
      mG2d.tryRenderBoundingBox(Stroke.this);
      
      if (this.isSelected)
         mG2d.setSelected(true);
      
      this.pen.adjustRenderer(mG2d);
      
      // If this stroke's path is stable, draw the path.  This uses 
      // memory because a cached array of the x and y coordinates of 
      // the points must be used.  
      // 
      // If the curve isn't stable don't waste memory making an array 
      // that will be soon be out of date.  Instead draw the path point 
      // by point.
      if (this.isStable)
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
            FloatPoint2D pt1;
            FloatPoint2D pt2;
            for (int i=0; i<numPts-1; i++)
            {
               pt1 = path.getItemAt(i);
               pt2 = path.getItemAt(i+1);
               if (pt1 != null && pt2 != null)
                  mG2d.drawLine(pt1, pt2);
            }
         }
      }
      
      if (this.isSelected)
         mG2d.setSelected(false);
      
      mG2d.endGroup(Stroke.this);
   }

   @Override
   public void scaleBy(float x, float y)
   {
      super.scaleBy(x, y);
      this.pen.scaleBy(Math.max(x, y));
      setIsStable(true);
   }

   @Override
   public void scaleTo(float x, float y)
   {
      super.scaleTo(x, y);
      this.pen.scaleTo(Math.max(x, y));
      setIsStable(true);
   }
   
   @Override
   public void resizeTo(float x, float y)
   {
      super.resizeTo(x, y);
      this.pen.resizeTo(Math.max(x, y));
      setIsStable(true);
   }

   @Override
   public void translateBy(float x, float y)
   {
      super.translateBy(x, y);
      setIsStable(true);
   }

   @Override
   public void translateTo(float x, float y)
   {
      super.translateTo(x, y);
      setIsStable(true);
   }
   
   @Override
   public Rectangle2D.Float getBounds2D()
   {
      Rectangle2D.Float bounds = super.getBounds2D();
      
      // modify the bounds to also account for the width of the current 
      // pen's stroke
      float delta = Renderer2D.getStrokeWidth(this.pen.getWidth(), 
                                              this.isSelected);
      
      double x = bounds.getX();
      double y = bounds.getY();
      double width = bounds.getWidth();
      double height = bounds.getHeight();
      
      // shift the box left and up half of the pen's width
      // also add delta to the width and height to put the 
      // edge of the rectangle back to the position it was at before 
      // the box was shifted.  Then add delta to the width and 
      // height again to account for the fact that the line has a width, 
      // specifically 2*delta.
      bounds.setRect(x-delta, y-delta, 
                     width+2*delta, height+2*delta);
      
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
