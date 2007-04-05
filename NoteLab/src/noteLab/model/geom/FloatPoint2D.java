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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import noteLab.util.CopyReady;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.Transformable;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;

/**
 * An implementation of a two-dimensional point with floating point 
 * coordinates that is bounded and can be transformed.  That is, 
 * the bounds of this point can be obtained, and this point can 
 * be scaled and translated.
 * 
 * @author Dominic Kramer
 */
public class FloatPoint2D implements Transformable, 
                                      CopyReady<FloatPoint2D>, 
                                      ModBroadcaster, 
                                      Bounded
{
   private Point2D.Float initPoint;
   private Point2D.Float srcPoint;
   
   /** The amount this point was scaled in the x direction. */
   private float xScaleLevel;
   
   /** The amount this point was scaled in the y direction. */
   private float yScaleLevel;
   
   /**
    * The vector of listeners that are notified when this point 
    * is modified.
    */
   private Vector<ModListener> modListenerVec;
   
   /**
    * Constructs a point at the given coordinates.
    * 
    * @param x This point's x coordinate.
    * @param y This point's y coordinate.
    * @param xScaleLevel The amount this point was scaled at 
    *                    in the x direction at the time this 
    *                    pont was constructed.
    * @param yScaleLevel The amount this point was scaled at 
    *                    in the y direction at the time this 
    *                    point was constructed.
    */
   public FloatPoint2D(float x, float y, 
                       float xScaleLevel, float yScaleLevel)
   {
      this.initPoint = new Point2D.Float(x/xScaleLevel, y/yScaleLevel);
      this.srcPoint = new Point2D.Float(x, y);
      
      this.xScaleLevel = xScaleLevel;
      this.yScaleLevel = yScaleLevel;
      
      this.modListenerVec = new Vector<ModListener>();
   }
   
   /**
    * Constructs a point with the same coordinates of the given point.
    * 
    * @param pt The point on which this point is based.
    * @param xScaleLevel The amount this point was scaled at 
    *                    in the x direction at the time this 
    *                    pont was constructed.
    * @param yScaleLevel The amount this point was scaled at 
    *                    in the y direction at the time this 
    *                    point was constructed.
    */
   public FloatPoint2D(Point pt, float xScaleLevel, float yScaleLevel)
   {
      this(pt.x, pt.y, xScaleLevel, yScaleLevel);
   }
   
   public float getXScaleLevel()
   {
      return this.xScaleLevel;
   }
   
   public float getYScaleLevel()
   {
      return this.yScaleLevel;
   }
   
   /**
    * Scales this point by the given amount.  To understand how a 
    * point is scaled, imagine the point as being glued onto the 
    * coordinate plane.  Scaling amounts to stretching or compressing 
    * the coordinate plane by the given amounts.  The point changes 
    * because is moves as the plane changes.
    * 
    * @param x The amount this point is scaled in the x direction.
    * @param y The amount this point is scaled in the y direction.
    */
   public void scaleBy(float x, float y)
   {
      this.xScaleLevel *= x;
      this.yScaleLevel *= y;
      
      this.srcPoint.x = this.initPoint.x * this.xScaleLevel;
      this.srcPoint.y = this.initPoint.y * this.yScaleLevel;
      
      notifyModListeners(ModType.ScaleBy);
   }
   
   /**
    * Scales this point to the given amount.  To understand how a point 
    * is scaled see {@link #scaleBy(float, float) scaleBy(float, float)}.
    * 
    * @param x The amount this point is scaled to in the x direction.
    * @param y The amount this point is scaled to in the y direction.
    */
   public void scaleTo(float x, float y)
   {
      this.xScaleLevel = x;
      this.yScaleLevel = y;
      
      this.srcPoint.x = this.initPoint.x * x;
      this.srcPoint.y = this.initPoint.y * y;
      
      notifyModListeners(ModType.ScaleTo);
   }
   
   public void resizeTo(float x, float y)
   {
      float oldXScale = this.xScaleLevel;
      float oldYScale = this.yScaleLevel;
      scaleTo(1, 1);
      
      this.initPoint.x *= x;
      this.initPoint.y *= y;
      
      this.srcPoint.x = this.initPoint.x;
      this.srcPoint.y = this.initPoint.y;
      
      this.xScaleLevel = 1;
      this.yScaleLevel = 1;
      
      scaleTo(oldXScale, oldYScale);
   }
   
   /**
    * Translates this point by the given amounts.
    * 
    * @param x The amount the point is translated in the x direction.
    * @param y The amount the point is translated in the y direction.
    */
   public void translateBy(float x, float y)
   {
      this.srcPoint.x += x;
      this.srcPoint.y += y;
      
      this.initPoint.x = this.srcPoint.x;
      this.initPoint.y = this.srcPoint.y;
      
      notifyModListeners(ModType.TranslateBy);
   }
   
   /**
    * Translates the point to the given coordinates.
    * 
    * @param x This point's new x coordinate.
    * @param y This point's new y coordinate.
    */
   public void translateTo(float x, float y)
   {
      this.initPoint.x = x;
      this.initPoint.y = y;
      
      this.srcPoint.x = x;
      this.srcPoint.y = y;
      
      notifyModListeners(ModType.TranslateTo);
   }
   
   /**
    * Used to get a deep copy of this point.
    * 
    * @return A deep copy of this point.
    */
   public FloatPoint2D getCopy()
   {
      return new FloatPoint2D(this.srcPoint.x, this.srcPoint.y, 
                              this.xScaleLevel, this.yScaleLevel);
   }
   
   /**
    * Used to get the bounds of this point.  The bounds of this point 
    * is a rectangle at this point's x and y coordinates with width and 
    * height of zero.
    * 
    * @return This points bounds.
    */
   public Rectangle2D.Float getBounds2D()
   {
      return new Rectangle2D.Float(this.srcPoint.x, this.srcPoint.y, 0, 0);
   }
   
   /**
    * Used to determine if the point <code>curPt</code> lies on the 
    * line segment connecting the points <code>pt1<code> and 
    * <code>pt2</code>.  Because the line segment is theoretically 
    * infitesimally small, this method actually looks if the point lies 
    * within 20 pixels of the line segment.
    * 
    * @param pt1 The first point of the line segment.
    * @param pt2 The second point of the line segment.
    * @param curPt The point that should be checked.
    * 
    * @return <code>True</code> if <code>curPt</code> lies on the 
    *         line segment connecting <code>pt1</code> and 
    *         <code>pt2</code> or <code>false</code> if it doesn't.
    */
   public static boolean lineContainsPoint(FloatPoint2D pt1, 
                                           FloatPoint2D pt2, 
                                           FloatPoint2D curPt)
   {
      if (pt1 == null || pt2 == null || curPt == null)
         throw new NullPointerException();
      
      // these are labeled x1 and x2 because when proving this method 
      // mathematically, I used vectors in R^2 which are typically 
      // denoted X = x1*e1 + x2*e2 where e1 and e2 are basis elements
      
      final double delta = 1;
      
      double u1 = pt1.getX();
      double u2 = pt1.getY();
      
      double v1 = pt2.getX();
      double v2 = pt2.getY();
      
      // get the coordinates of the point where the point 'u' 
      // is treated as the origin
      double x1 = curPt.getX()-u1;
      double x2 = curPt.getY()-u2;
      
      // W = V-U
      double w1 = v1-u1;
      double w2 = v2-u2;
      
      // Wt is perpendicular to W
      double wt1 = -w2;
      double wt2 =  w1;
      
      // Note:  |W| = |Wt|
      double wnorm = Math.sqrt(w1*w1 + w2*w2);
      
      // lambda = Pr_W(X) = W.X/|w|
      double lambda = (w1*x1+w2*x2)/wnorm;
      
      // gamma = Pr_Wt(X) = Wt.X/|Wt|
      double gamma = (wt1*x1+wt2*x2)/wnorm;
      gamma = Math.abs(gamma);
      
      if (gamma > delta)
      return false;
      
      if ( (lambda < 0) || (lambda > wnorm) )
      return false;
      
      return true;
   }
   
   /**
    * Used to add a listener that is notified when this point is 
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
    * notified when this point is modified.
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
    * Informs all of the ModListeners that this point has been modified.
    * 
    * @param type The type of modification.
    */
   private void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
   
   public void setX(float x)
   {
      this.srcPoint.x = x;
   }
   
   public float getX()
   {
      return this.srcPoint.x;
   }
   
   public void setY(float y)
   {
      this.srcPoint.y = y;
   }
   
   public float getY()
   {
      return this.srcPoint.y;
   }
   
   @Override
   public String toString()
   {
      return ""+FloatPoint2D.class.getSimpleName()+
             ": ("+getX()+", "+getY()+")";
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      FloatPoint2D pt = new FloatPoint2D(2,3,1,1);
      System.err.println("pt="+pt);
      
      pt.scaleTo(0.5f, 0.5f);
      System.err.println("scaleTo 0.5 = "+pt);
      
      pt.resizeTo(2, 2);
      System.err.println("resize to 2 = "+pt);
      
      pt.scaleTo(1, 1);
      System.err.println("scale to 1 = "+pt);
      
      /*
      FloatPoint2D pt = new FloatPoint2D(1, 1, 1, 1);
      System.out.println("pt="+pt);
      float xScale = 1/1.2f;
      float yScale = 1/1.2f;
      
      for (int i=0; i<10; i++)
      {
         pt.scaleBy(xScale, yScale);
         System.out.println(" scale by ("+xScale+", "+yScale+") = "+pt);
      }
      
      pt.scaleTo(1, 1);
      System.out.println(" scale to (1, 1) = "+pt);
      */
   }
}
