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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.geom.Bounded;

public class SwingRenderer2D extends Renderer2D
{
   public enum RenderMode
   {
      Appearance, 
      Performance
   };
   
   private Graphics2D g2d;
   private float width;
   
   public SwingRenderer2D()
   {
      super();
      
      this.width = 0;
   }
   
   public void setSwingGraphics(Graphics2D g2d, RenderMode mode)
   {
      if (g2d == null)
         throw new NullPointerException();
      
      this.g2d = g2d;
      setSelected(false);
      
      setRenderingHints(mode);
   }
   
   private void setRenderingHints(RenderMode mode)
   {
      if (mode == null)
         throw new NullPointerException();
      
      if (mode == RenderMode.Appearance)
      {
         this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
         
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
         
         this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                   RenderingHints.VALUE_RENDER_QUALITY);
      }
      else if (mode == RenderMode.Performance)
      {
         this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_OFF);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                   RenderingHints.VALUE_RENDER_SPEED);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                                   RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                                   RenderingHints.VALUE_COLOR_RENDER_SPEED);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                                   RenderingHints.VALUE_STROKE_PURE);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_DITHERING, 
                                   RenderingHints.VALUE_DITHER_DISABLE);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
         
         this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                   RenderingHints.VALUE_RENDER_SPEED);
      }
   }
   
   private boolean hitsClip(FloatPoint2D pt1, FloatPoint2D pt2)
   {
      float pt1x = pt1.getX();
      float pt1y = pt1.getY();
      
      float pt2x = pt2.getX();
      float pt2y = pt2.getY();
      
      return hitsClip(Math.min(pt1x, pt2x), 
                      Math.min(pt1y, pt2y), 
                      Math.abs(pt1x-pt2x), 
                      Math.abs(pt1y-pt2y));
   }
   
   private boolean hitsClip(float x, float y, float width, float height)
   {
      float twoWidth = 2*this.width;
      
      x -= this.width;
      y -= this.width;
      width = Math.max(width+twoWidth,1);
      height = Math.max(height+twoWidth,1);
      
      return this.g2d.hitClip((int)x, 
                              (int)y, 
                              (int)width, 
                              (int)height);
   }
   
   @Override
   public void drawPath(Path path)
   {
      if (path == null)
         throw new NullPointerException();
      
      int numPts = path.getNumItems();
      if (numPts < 1)
         return;
      
      if (numPts == 1)
      {
         FloatPoint2D pt1 = path.getFirst();
         drawLine(pt1, pt1);
         return;
      }
      
      Path2D.Float floatPath = new Path2D.Float();
      
      FloatPoint2D pt = path.getItemAt(0);
      floatPath.moveTo(pt.getX(), pt.getY());
      
      for (int i=1; i<numPts; i++)
      {
         pt = path.getItemAt(i);
         if (pt != null)
            floatPath.lineTo(pt.getX(), pt.getY());
      }
      
      this.g2d.draw(floatPath);
      
      // Uncomment to enable the "Display Knots" debug setting.
      // Since this method is called often, simply checking 
      // if knots should be displayed could decrease performance.
      /*
      if (DebugSettings.getSharedInstance().displayKnots())
      {
         for (int i=0; i<numPts; i++)
         {
            pt = path.getItemAt(i);
            if (pt != null)
               drawKnot(pt);
         }
      }
      */
   }
   
   @Override
   public void drawLine(FloatPoint2D pt1, FloatPoint2D pt2)
   {
      if (pt1 == null || pt2 == null)
         throw new NullPointerException();
      
      if (hitsClip(pt1, pt2))
      {
         this.g2d.draw(new Line2D.Float(pt1.getX(), pt1.getY(), 
                                        pt2.getX(), pt2.getY()));
      }
      
      // Uncomment to enable the "Display Knots" debug setting.
      // Since this method is called often, simply checking 
      // if knots should be displayed could decrease performance.
      /*
      if (DebugSettings.getSharedInstance().displayKnots())
      {
         drawKnot(pt1);
         drawKnot(pt2);
      }
      */
   }
   
   private void drawKnot(FloatPoint2D pt)
   {
      float widthHalf = 2*getLineWidth();
      float width = 2*widthHalf;
      
      this.g2d.draw(new Ellipse2D.Float(pt.getX()-widthHalf, 
                                        pt.getY()-widthHalf, 
                                        width, 
                                        width));
   }
   
   @Override
   public void drawRectangle(float x, float y, 
                             float width, float height)
   {
      if (hitsClip(x, y, width, height))
         this.g2d.draw(new Rectangle2D.Float(x, y, width, height));
   }
   
   @Override
   public void fillRectangle(float x, float y, 
                             float width, float height)
   {
      if (hitsClip(x, y, width, height))
         this.g2d.fill(new Rectangle2D.Float(x, y, width, height));
      
      /*
      if (!hitsClip(x, y, width, height))
         return;
      
      Rectangle clip = this.g2d.getClipBounds();
      
      if (clip == null)
      {
         this.g2d.fillRect( (int)(SCALE_FACTOR*x), 
                            (int)(SCALE_FACTOR*y), 
                            (int)(SCALE_FACTOR*width), 
                            (int)(SCALE_FACTOR*height) );
      }
      else if (clip != null)
      {
         Rectangle rect = new Rectangle((int)(x*SCALE_FACTOR), 
                                        (int)(y*SCALE_FACTOR), 
                                        (int)(width*SCALE_FACTOR), 
                                        (int)(height*SCALE_FACTOR));
         Rectangle.intersect(rect, clip, rect);
         
         if (!rect.isEmpty())
            this.g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
      }
      */
   }
   
   @Override
   public void setSelected(boolean selected)
   {
      super.setSelected(selected);
      if (this.g2d != null)
      {
         if (selected)
            this.g2d.setStroke(new SelectedStroke(this.width));
         else
            this.g2d.setStroke(new BasicStroke(this.width, 
                                               BasicStroke.CAP_ROUND, 
                                               BasicStroke.JOIN_ROUND));
      }
   }

   @Override
   public void setColor(Color color)
   {
      this.g2d.setColor(color);
   }
   
   @Override
   public Color getColor()
   {
      return this.g2d.getColor();
   }
   
   @Override
   public void setLineWidth(float width)
   {
      this.width = width;
      setSelected(isSelected());
   }
   
   @Override
   public float getLineWidth()
   {
      return this.width;
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
      this.g2d.dispose();
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
      this.g2d.translate(x, y);
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      if (bounded == null)
         throw new NullPointerException();
      
      Rectangle2D bounds = bounded.getBounds2D();
      int x = (int)bounds.getMinX();
      int y = (int)bounds.getMinY();
      int w = (int)bounds.getWidth();
      int h = (int)bounds.getHeight();
      
      return this.g2d.hitClip(x, y, w, h);
   }
   
   @Override
   public boolean isCompletelyInClipRegion(Bounded bounded)
   {
      if (bounded == null)
         throw new NullPointerException();

      Rectangle2D bounds = bounded.getBounds2D();
      int x = (int)bounds.getMinX();
      int y = (int)bounds.getMinY();
      int w = (int)bounds.getWidth();
      int h = (int)bounds.getHeight();

      Rectangle clipBounds = this.g2d.getClipBounds();
      if (clipBounds == null)
         return true;

      return clipBounds.contains(x, y, w, h);
   }
   
   /**
    * Use this method only if absolutely necessary.
    */
   public Graphics2D createGraphics()
   {
      return (Graphics2D)(this.g2d.create());
   }
   
   private static class SelectedStroke implements Stroke
   {
      private BasicStroke outerSelStroke;
      private BasicStroke innerSelStroke;
      
      public SelectedStroke(float width)
      {
         width *= 0.7f;
         
         this.outerSelStroke = new BasicStroke(getStrokeWidth(width, true), 
                                               BasicStroke.CAP_ROUND, 
                                               BasicStroke.JOIN_ROUND);
         
         this.innerSelStroke = new BasicStroke(getStrokeWidth(width, false), 
                                               BasicStroke.CAP_ROUND, 
                                               BasicStroke.JOIN_ROUND);
      }
      
      /*
       * The code in this method was inspired by a description of making custom 
       * strokes found in the book "Java Examples in a Nutshell, 3nd Edition" by 
       * David Flanagan.  As stated in the copyright notice attached to the code, 
       * permission is given to use the code in any open-source project as long 
       * as the notice below is attached.  As such, since NoteLab is open-source, 
       * it is fine to use the code since the notice has been attached.
       * 
       * Original notice:
       * 
       * Copyright (c) 2004 David Flanagan.  All rights reserved.
       * This code is from the book Java Examples in a Nutshell, 3nd Edition.
       * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
       * You may study, use, and modify it for any non-commercial purpose,
       * including teaching and use in open-source projects.
       * You may distribute it non-commercially as long as you retain this notice.
       * For a commercial use license, or to purchase the book, 
       * please visit http://www.davidflanagan.com/javaexamples3.
       * 
       * The original code that was used was:  
       * 
       * public Shape createStrokedShape(Shape s) {
       *    // Use the first stroke to create an outline of the shape
       *    Shape outline = stroke1.createStrokedShape(s);  
       *    // Use the second stroke to create an outline of that outline.
       *    // It is this outline of the outline that will be filled in
       *    return stroke2.createStrokedShape(outline);
       * }
       */
      public Shape createStrokedShape(Shape p)
      {
         return this.innerSelStroke.
                    createStrokedShape(this.outerSelStroke.createStrokedShape(p));
      }
   }
}
