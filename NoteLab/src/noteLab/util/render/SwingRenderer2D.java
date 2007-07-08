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

package noteLab.util.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.geom.Bounded;
import noteLab.util.settings.DebugSettings;

public class SwingRenderer2D extends Renderer2D
{
   private static final float SCALE_FACTOR = 100000;
   
   private Graphics2D g2d;
   
   public SwingRenderer2D()
   {
      super();
   }
   
   public void setSwingGraphics(Graphics2D g2d, boolean antialias)
   {
      if (g2d == null)
         throw new NullPointerException();
      
      this.g2d = g2d;
      this.g2d.scale(1.0/SCALE_FACTOR, 1.0/SCALE_FACTOR);
      this.g2d.setStroke(new SelectableStroke(getLineWidth()));
      setSelected(false);
      
      setRenderingHints(antialias);
   }
   
   private void setRenderingHints(boolean antialias)
   {
      Object val = RenderingHints.VALUE_ANTIALIAS_OFF;
      if (antialias)
         val = RenderingHints.VALUE_ANTIALIAS_ON;
      
      this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, val);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                RenderingHints.VALUE_RENDER_QUALITY);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                                RenderingHints.VALUE_STROKE_PURE);
      
      this.g2d.setRenderingHint(RenderingHints.KEY_DITHERING, 
                                RenderingHints.VALUE_DITHER_ENABLE);
   }
   
   @Override
   public void drawPath(final Path path)
   {
      if (path == null)
         throw new NullPointerException();
      
      doDrawing(new Runnable()
      {
         public void run()
         {
            int numPts = path.getNumItems();
            
            if (numPts == 1)
            {
               FloatPoint2D pt1 = path.getFirst();
               drawLine(pt1, pt1);
               return;
            }
            
            FloatPoint2D pt1;
            FloatPoint2D pt2;
            for (int i=0; i<numPts-1; i++)
            {
               pt1 = path.getItemAt(i);
               pt2 = path.getItemAt(i+1);
               if (pt1 != null && pt2 != null)
                  drawLine(pt1, pt2);
            }
         }
      });
   }
   
   public void drawLine(final FloatPoint2D pt1, final FloatPoint2D pt2)
   {
      if (pt1 == null || pt2 == null)
         throw new NullPointerException();
      
      doDrawing(new Runnable()
      {
         public void run()
         {
            int pt1x = (int)(SCALE_FACTOR*pt1.getX());
            int pt1y = (int)(SCALE_FACTOR*pt1.getY());
            
            int pt2x = (int)(SCALE_FACTOR*pt2.getX());
            int pt2y = (int)(SCALE_FACTOR*pt2.getY());
            
            g2d.drawLine( pt1x, pt1y, pt2x, pt2y );
            
            if (DebugSettings.getSharedInstance().displayKnots())
            {
               drawKnot(pt1);
               drawKnot(pt2);
            }
         }
      });
   }
   
   private void drawKnot(FloatPoint2D pt)
   {
      int width = (int)(4*getLineWidth());
      int widthHalf = width/2;
      
      this.g2d.fillOval((int)pt.getX()-widthHalf, 
                        (int)pt.getY()-widthHalf, 
                        width, 
                        width);
   }
   
   public void drawRectangle(final float x, final float y, 
                             final float width, final float height)
   {
      doDrawing(new Runnable()
      {
         public void run()
         {
            g2d.drawRect( (int)(SCALE_FACTOR*x), 
                          (int)(SCALE_FACTOR*y), 
                          (int)(SCALE_FACTOR*width), 
                          (int)(SCALE_FACTOR*height) );
         }
      });
   }
   
   public void fillRectangle(final float x, final float y, 
                             final float width, final float height)
   {
      doDrawing(new Runnable()
      {
         public void run()
         {
            g2d.fillRect( (int)(SCALE_FACTOR*x), 
                          (int)(SCALE_FACTOR*y), 
                          (int)(SCALE_FACTOR*width), 
                          (int)(SCALE_FACTOR*height) );
         }
      });
   }
   
   @Override
   public void setSelected(boolean selected)
   {
      super.setSelected(selected);
      if (this.g2d != null)
      {
         Stroke stroke = this.g2d.getStroke();
         if (stroke instanceof SelectableStroke)
            ((SelectableStroke)stroke).setSelected(selected);
      }
   }

   @Override
   public void setColor(Color color)
   {
      this.g2d.setColor(color);
   }
   
   @Override
   public void setLineWidth(float width)
   {
      Stroke stroke = this.g2d.getStroke();
      if (stroke instanceof SelectableStroke)
         ((SelectableStroke)stroke).setLineWidth(width);
   }
   
   @Override
   public float getLineWidth()
   {
      Stroke stroke = this.g2d.getStroke();
      if ( !(stroke instanceof SelectableStroke) )
         return 0;
      
      return ((SelectableStroke)stroke).getLineWidth();
   }
   
   @Override
   protected void beginGroupImpl(Renderable renderable, String desc, 
                                 float xScaleLevel, float yScaleLevel)
   {
   }

   @Override
   protected void endGroupImpl(Renderable renderable)
   {
   }

   @Override
   public void finish()
   {
      this.g2d.scale(SCALE_FACTOR, SCALE_FACTOR);
      this.g2d.dispose();
   }
   
   public void drawImage(Image image)
   {
      if (image == null)
         throw new NullPointerException();
      
      this.g2d.drawImage(image, 0, 0, null);
   }
   
   private void doDrawing(Runnable drawRunnable)
   {
      if (drawRunnable == null)
         throw new NullPointerException();
      
      drawRunnable.run();
   }
   
   // The Renderer2D class doesn't specify a scale() method
   //@Override
   //public void scale(float x, float y)
   //{
   //   this.g2d.scale(x, y);
   //}

   @Override
   public void translate(float x, float y)
   {
      this.g2d.translate( (int)(SCALE_FACTOR*x), (int)(SCALE_FACTOR*y) );
   }

   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      if (bounded == null)
         throw new NullPointerException();
      
      Rectangle clipBounds = this.g2d.getClipBounds();
      if (clipBounds == null)
         return true;
      
      clipBounds.x /= SCALE_FACTOR;
      clipBounds.y /= SCALE_FACTOR;
      clipBounds.width /= SCALE_FACTOR;
      clipBounds.height /= SCALE_FACTOR;
      
      Rectangle2D bounds = bounded.getBounds2D();
      
      Rectangle2D intersection = new Rectangle2D.Double();
      Rectangle.intersect(clipBounds, bounds, intersection);
      
      return !intersection.isEmpty();
   }
   
   private static class SelectableStroke implements Stroke
   {
      private boolean selected;
      
      private BasicStroke baseStroke;
      
      private BasicStroke outerSelStroke;
      private BasicStroke innerSelStroke;
      
      public SelectableStroke(float width)
      {
         this.selected = false;
         setLineWidth(width);
      }
      
      public boolean isSelected()
      {
         return this.selected;
      }
      
      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }
      
      public float getLineWidth()
      {
         return this.baseStroke.getLineWidth()/SCALE_FACTOR;
      }
      
      public void setLineWidth(float width)
      {
         float scaledWidth = SCALE_FACTOR*width;
         
         this.baseStroke = new BasicStroke(scaledWidth, 
                                            BasicStroke.CAP_ROUND, 
                                            BasicStroke.JOIN_ROUND);
         
         // shrink the width down slightly so that the line surrounding the 
         // highlighted (selected) stroke is not too thick
         scaledWidth *= 0.30;
         
         this.outerSelStroke = new BasicStroke(getStrokeWidth(scaledWidth, true), 
                                                BasicStroke.CAP_ROUND, 
                                                BasicStroke.JOIN_ROUND);
         
         this.innerSelStroke = new BasicStroke(getStrokeWidth(scaledWidth, false), 
                                                BasicStroke.CAP_ROUND, 
                                                BasicStroke.JOIN_ROUND);
      }
      
      public Shape createStrokedShape(Shape p)
      {
         if (this.selected)
            return this.innerSelStroke.
                       createStrokedShape(this.outerSelStroke.createStrokedShape(p));
         
         return this.baseStroke.createStrokedShape(p);
      }
   }
}
