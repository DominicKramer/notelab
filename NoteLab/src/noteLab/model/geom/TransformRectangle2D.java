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

package noteLab.model.geom;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import noteLab.util.geom.Bounded;
import noteLab.util.geom.Transformable;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;

/**
 * An implementation of a rectangle that is both bounded and transformable.  
 * That is, the bounds of the rectangle can be acquired and the 
 * rectangle can be scaled and translated.
 * 
 * @author Dominic Kramer
 */
public class TransformRectangle2D implements Bounded, Transformable, 
                                             ModBroadcaster
{
   private Rectangle2D.Float srcRect;
   
   /** The amount this rectangle was scaled in the x direction. */
   private float xScaleLevel;
   
   /** The amount this rectangle was scaled in the y direction. */
   private float yScaleLevel;
   
   /**
    * The vector of listeners that are notified when this rectangle 
    * is modified.
    */
   protected Vector<ModListener> modListenerVec;
   
   /**
    * Constructs a rectangle with its values initialized from the 
    * given rectangle.
    * 
    * @param rect The rectangle on which this rectangle is based.
    * @param xScaleLevel The amount this rectangle was scaled at 
    *                    in the x direction at the time this 
    *                    rectangle was constructed.
    * @param yScaleLevel The amount this rectangle was scaled at 
    *                    in the y direction at the time this 
    *                    rectangle was constructed.
    */
   public TransformRectangle2D(Rectangle2D.Float rect, 
                               float xScaleLevel, float yScaleLevel)
   {
      this(rect.x, rect.y, rect.width, rect.height, 
           xScaleLevel, yScaleLevel);
   }
   
   /**
    * Constructs a rectangle with the given parameters.
    * 
    * @param pt The upper-left corner of this rectangle.
    * @param w The width of this rectangle.
    * @param h The height of this rectangle.
    * @param xScaleLevel The amount this rectangle was scaled at 
    *                    in the x direction at the time this 
    *                    rectangle was constructed.
    * @param yScaleLevel The amount this rectangle was scaled at 
    *                    in the y direction at the time this 
    *                    rectangle was constructed.
    */
   public TransformRectangle2D(Point2D.Float pt, float w, float h, 
                               float xScaleLevel, float yScaleLevel)
   {
      this(pt.x, pt.y, w, h, xScaleLevel, yScaleLevel);
   }
   
   /**
    * Constructs a rectangle with the given parameters.
    * 
    * @param x The x coordinate of the upper-left corner of this rectangle.
    * @param y The y coordinate of the upper-left corner of this rectangle.
    * @param w The width of this rectangle.
    * @param h The height of this rectangle.
    * @param xScaleLevel The amount this rectangle was scaled at 
    *                    in the x direction at the time this 
    *                    rectangle was constructed.
    * @param yScaleLevel The amount this rectangle was scaled at 
    *                    in the y direction at the time this 
    *                    rectangle was constructed.
    */
   public TransformRectangle2D(float x, float y, float w, float h, 
                               float xScaleLevel, float yScaleLevel)
   {
     this.srcRect = new Rectangle2D.Float(x, y, w, h);
     
      this.modListenerVec = new Vector<ModListener>();
      
      this.xScaleLevel = xScaleLevel;
      this.yScaleLevel = yScaleLevel;
   }
   
   /**
    * Scales this rectangle by the given scaling factor.
    * 
    * @param x The amount this rectangle is scaled in the x direction.
    * @param y The amount this rectangle is scaled in the y direction.
    */
   public void scaleBy(float x, float y)
   {
      this.srcRect.x *= x;
      this.srcRect.y *= y;
      
      this.srcRect.width *= x;
      this.srcRect.height *= y;
      
      this.xScaleLevel *= x;
      this.yScaleLevel *= y;
      
      notifyModListeners(ModType.ScaleBy);
   }
   
   /**
    * Scales this rectangle to the given scale values.
    * 
    * @param x The amount this rectangle should be scaled to in 
    *          the x direction.
    * @param y The amount this rectangle should be scaled to in 
    *          the y direction.
    */
   public void scaleTo(float x, float y)
   {
      float xScaleTo = 1;
      float yScaleTo = 1;
      
      try
      {
         xScaleTo = x/this.xScaleLevel;
         yScaleTo = y/this.yScaleLevel;
      }
      catch (ArithmeticException e)
      {
         System.err.println(FloatPoint2D.class.getName()+" Warning:  " +
                            "cannot scale to the level ("+x+", "+y+") " +
                            "because this object has been scaled down " +
                            "beyond this machines floating point delta.");
      }
      
      this.srcRect.x *= xScaleTo;
      this.srcRect.y *= yScaleTo;
      
      this.srcRect.width *= xScaleTo;
      this.srcRect.height *= yScaleTo;
      
      this.xScaleLevel = x;
      this.yScaleLevel = y;
      
      notifyModListeners(ModType.ScaleTo);
   }
   
   public void resizeTo(float x, float y)
   {
      float oldXScale = this.xScaleLevel;
      float oldYScale = this.yScaleLevel;
      scaleTo(1, 1);
      
      this.srcRect.x *= x;
      this.srcRect.y *= y;
      
      this.srcRect.width *= x;
      this.srcRect.height *= y;
      
      this.xScaleLevel = 1;
      this.yScaleLevel = 1;
      
      scaleTo(oldXScale, oldYScale);
   }
   
   /**
    * Translates this rectangle by the given amount.
    * 
    * @param x The amount the rectangle should be translated in 
    *          the x direction.
    * @param y The amount the rectangle should be translated in 
    *          the y direction.
    */
   public void translateBy(float x, float y)
   {
      this.srcRect.x += x;
      this.srcRect.y += y;
      
      notifyModListeners(ModType.TranslateBy);
   }
   
   /**
    * Translates this rectangle to the given coordinates.
    * 
    * @param x The new x coordinate of this rectangle's upper 
    *          left corner.
    * @param y The new y coordinate of this rectangle's upper 
    *          left corner.
    */
   public void translateTo(float x, float y)
   {
      this.srcRect.x = x;
      this.srcRect.y = y;
      
      notifyModListeners(ModType.TranslateTo);
   }
   
   /**
    * Used to add a listener that is notified when this rectangle is 
    * modified.
    * 
    * @param listener The listener to add.
    */
   public void addModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.modListenerVec.contains(listener))
         this.modListenerVec.add(listener);
   }
   
   /**
    * Used to removed a listener from the list of listeners that are 
    * notified when this rectangle is modified.
    * 
    * @param listener The listener to remove.
    */
   public void removeModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.modListenerVec.remove(listener);
   }
   
   /**
    * Informs all of the ModListeners that this rectangle has been modified.
    * 
    * @param type The type of modification.
    */
   protected void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
   
   public void setX(float x)
   {
      this.srcRect.x = x;
   }
   
   public float getX()
   {
      return this.srcRect.x;
   }
   
   public void setY(float y)
   {
      this.srcRect.y = y;
   }
   
   public float getY()
   {
      return this.srcRect.y;
   }
   
   public void setWidth(float w)
   {
      this.srcRect.width = w;
   }
   
   public float getWidth()
   {
      return this.srcRect.width;
   }
   
   public void setHeight(float h)
   {
      this.srcRect.height = h;
   }
   
   public float getHeight()
   {
      return this.srcRect.height;
   }
   
   public boolean contains(Point2D point)
   {
      if (point == null)
         throw new NullPointerException();
      
      return this.srcRect.contains(point);
   }
   
   public boolean contains(FloatPoint2D point)
   {
      if (point == null)
         throw new NullPointerException();
      
      return contains(new Point2D.Float(point.getX(), point.getY()));
   }
   
   public float getMinX()
   {
      return (float)this.srcRect.getMinX();
   }
   
   public float getMaxX()
   {
      return (float)this.srcRect.getMaxX();
   }
   
   public float getMinY()
   {
      return (float)this.srcRect.getMinY();
   }
   
   public float getMaxY()
   {
      return (float)this.srcRect.getMaxY();
   }
   
   public float getXScaleLevel()
   {
      return this.xScaleLevel;
   }
   
   public float getYScaleLevel()
   {
      return this.yScaleLevel;
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getSimpleName());
      buffer.append(":  At (");
      buffer.append(getX());
      buffer.append(",");
      buffer.append(getY());
      buffer.append("), Size(w,h)=(");
      buffer.append(getWidth());
      buffer.append(",");
      buffer.append(getHeight());
      buffer.append(")");
      
      return buffer.toString();
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      TransformRectangle2D rect = new TransformRectangle2D(2, 3, 5, 7, 1, 1);
      System.err.println("rect = "+rect);
      
      rect.scaleTo(0.5f, 0.5f);
      System.err.println("scale to 0.5f = "+rect);
      
      rect.resizeTo(2, 2);
      System.err.println(" resize to 2 = "+rect);
      
      rect.scaleTo(1, 1);
      System.err.println(" scaleTo(1,1) = "+rect);
   }

   public Rectangle2D.Float getBounds2D()
   {
      Rectangle2D bounds = this.srcRect.getBounds2D();
      
      return new Rectangle2D.Float((float)bounds.getX(), 
                                 (float)bounds.getY(), 
                                 (float)bounds.getWidth(), 
                                 (float)bounds.getHeight());
   }
}
