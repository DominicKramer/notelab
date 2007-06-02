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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Stack;
import java.util.Vector;

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.Selectable;
import noteLab.util.geom.Bounded;
import noteLab.util.settings.DebugSettings;

/**
 * A class of this type, has the ability to render a display.  
 * Subclasses extend this class to implement the ability to 
 * render to a computer display, bitmapped image, or 
 * scalable vector graphics (SVG) image.
 * 
 * @author Dominic Kramer
 */
public abstract class Renderer2D implements Selectable
{
   private Stack<String> groupIDStack;
   private boolean selected;
   
   private Vector<RenderListener> listenerVec;
   
   public Renderer2D()
   {
      this.groupIDStack = new Stack<String>();
      this.listenerVec = new Vector<RenderListener>();
      setSelected(false);
   }
   
   public boolean isSelected()
   {
      return this.selected;
   }
   
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }
   
   public void addRenderListener(RenderListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeRenderListener(RenderListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public void tryRenderBoundingBox(Bounded bounded)
   {
      if (!DebugSettings.getSharedInstance().displayBoundingBox())
         return;
      
      if (bounded == null)
         throw new NullPointerException();
      
      setColor(Color.RED);
      setLineWidth(1);
      
      Rectangle2D.Float bounds = bounded.getBounds2D();
      if (bounds == null)
         throw new NullPointerException();

      drawRectangle( (float)bounds.getX(), 
                     (float)bounds.getY(), 
                     (float)bounds.getWidth(), 
                     (float)bounds.getHeight() );
   }
   
   public void beginGroup(Renderable renderable, String desc, 
                          float xScaleLevel, float yScaleLevel)
   {
      if (renderable == null || desc == null)
         throw new NullPointerException();
      
      this.groupIDStack.push(renderable.getClass().getName());
      beginGroupImpl(renderable, desc, xScaleLevel, yScaleLevel);
   }
   
   public void endGroup(Renderable renderable)
   {
      if (renderable == null)
         throw new NullPointerException();
      
      String curName = renderable.getClass().getName();
      String grpOnStack = this.groupIDStack.pop();
      if (!grpOnStack.equals(curName))
         System.out.println("Renderer2D:  Warning:  Ended the group "+
                            curName+" while the last group that had " +
                            "begun was "+grpOnStack);
      
      endGroupImpl(renderable);
      
      if (this.listenerVec.size() > 0)
         for (RenderListener listener : this.listenerVec)
            listener.objectRendered(renderable);
   }
   
   public static float getStrokeWidth(float width, boolean isSelected)
   {
      if (!isSelected)
         return width;
      
      return width+4;
   }
   
   public abstract void drawPath(Path path);
   public abstract void drawLine(FloatPoint2D pt1, FloatPoint2D pt2);
   public abstract void drawRectangle(float x, float y, 
                                      float width, float height);
   public abstract void fillRectangle(float x, float y, 
                                      float width, float height);
   
   public abstract void setColor(Color color);
   public abstract void setLineWidth(float width);
   public abstract float getLineWidth();
   
   public abstract void finish();
   
   public abstract void translate(float x, float y);
   // Nothing directly needs scale functionality when being 
   // rendered and the SVG rendering system is slightly 
   // cumbersome to have implement scaling.
   //public abstract void scale(float x, float y);
   
   public abstract boolean isInClipRegion(Bounded bounded);
   
   protected abstract void beginGroupImpl(Renderable renderable, 
                                          String desc, 
                                          float xScaleFactor, 
                                          float yScaleFactor);
   protected abstract void endGroupImpl(Renderable renderable);
}
