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

package noteLab.model.binder;

import java.awt.geom.Rectangle2D;

import noteLab.model.Page;

public class FlowBinder extends Binder
{
   private static final float DEFAULT_GAP = 7;
   
   private float pageGap;
   
   private float width;
   private float height;
   
   public FlowBinder(float xScaleLevel, float yScaleLevel, Page ... pages)
   {
      super(xScaleLevel, yScaleLevel, pages);
      
      setPageGap(DEFAULT_GAP);
      
      doLayout();
   }
   
   public float getPageGap()
   {
      return this.pageGap;
   }
   
   public void setPageGap(float pageGap)
   {
      this.pageGap = pageGap;
   }
   
   @Override
   public void doLayoutImpl()
   {
      this.width = Float.NaN;
      this.height = Float.NaN;
      
      float gap = getPageGap();
      float sumHeight = 0;
      for (Page p : this)
      {
         sumHeight += gap;
         
         p.setX(gap);
         p.setY(sumHeight);
         
         sumHeight += p.getHeight();
      }
   }

   @Override
   public Rectangle2D.Float getBounds2D()
   {
      return new Rectangle2D.Float(getX(), 
                                   getY(), 
                                   getWidth(), 
                                   getHeight());
   }
   
   @Override
   public float getX()
   {
      return this.xScaleLevel*this.pageGap;
   }
   
   @Override
   public float getY()
   {
      return this.yScaleLevel*this.pageGap;
   }
   
   @Override
   public float getWidth()
   {
      if (Float.isNaN(this.width))
      {
         this.width = 0;
         for (Page p : this)
            this.width = Math.max(p.getWidth(), this.width);
         
         this.width += 2*this.pageGap;
      }
      
      return this.width;
   }
   
   @Override
   public float getHeight()
   {
      if (Float.isNaN(this.height))
      {
         this.height = getPageGap();
         for (Page p : this)
         {
            this.height += p.getHeight();
            this.height += this.pageGap;
         }
      }
      
      return this.height;
   }

   @Override
   public void scaleBy(float x, float y)
   {
      super.scaleBy(x, y);
      
      this.width = Float.NaN;
      this.height = Float.NaN;
   }

   @Override
   public void scaleTo(float x, float y)
   {
      super.scaleTo(x, y);
      
      this.width = Float.NaN;
      this.height = Float.NaN;
   }
   
   @Override
   public void resizeTo(float x, float y)
   {
      super.resizeTo(x, y);
      
      this.width = Float.NaN;
      this.height = Float.NaN;
   }

   public FlowBinder getCopy()
   {
      Page[] pageCpArr = new Page[getNumberOfPages()];
      int i=0;
      for (Page page : this)
         pageCpArr[i++] = page.getCopy();
      
      return new FlowBinder(super.xScaleLevel, 
                            super.yScaleLevel, 
                            pageCpArr);
   }
}
