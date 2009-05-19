/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2009, Dominic Kramer
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

import java.awt.Color;

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.geom.Bounded;

/**
 * A renderer that does nothing.  This renderer is used when 
 * rendering the canvas with an <code>SVGRenderer2D</code> or 
 * and <code>ImageRenderer2D</code>.  When rendering the canvas 
 * a renderer has to be given to render the overlay.  If the 
 * canvas is to be exported to an image or an SVG file, the 
 * overlay doesn't matter.  As such, a renderer of this type is 
 * used as the overlay's render since the rendering of the 
 * overlay is ignored anyway.
 * 
 * @author Dominic Kramer
 */
public class EmptyRenderer2D extends Renderer2D
{
   public EmptyRenderer2D()
   {
   }
   
   @Override
   protected void beginGroupImpl(Renderable renderable, String desc,
                                 float scaleFactor, float scaleFactor2)
   {
   }
   
   @Override
   public void drawLine(FloatPoint2D pt1, FloatPoint2D pt2)
   {
   }
   
   @Override
   public void drawPath(Path path)
   {
   }
   
   @Override
   public void drawRectangle(float x, float y, float width, float height)
   {
   }
   
   @Override
   protected void endGroupImpl(Renderable renderable)
   {
   }
   
   @Override
   public void fillRectangle(float x, float y, float width, float height)
   {
   }
   
   @Override
   public void finish()
   {
   }
   
   @Override
   public float getLineWidth()
   {
      return 0;
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      return false;
   }
   
   @Override
   public void setColor(Color color)
   {
   }
   
   @Override
   public void setLineWidth(float width)
   {
   }
   
   @Override
   public void translate(float x, float y)
   {
   }

   @Override
   public Color getColor()
   {
      return Color.BLACK;
   }
}
