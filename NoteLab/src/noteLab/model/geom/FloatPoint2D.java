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
   private static final int LINE_OFFSET_DELTA = 2;
   
   private ScalableFloat xValue;
   private ScalableFloat yValue;
   
   /**
    * The vector of listeners that are notified when this point 
    * is modified.
    */
   private Vector<ModListener> modListenerVec;
   
   private FloatPoint2D(FloatPoint2D point)
   {
      this(point.getX(), point.getY(), 
           point.getXScaleLevel(), point.getYScaleLevel());
      
      for (ModListener listener : point.modListenerVec)
         this.modListenerVec.add(listener);
   }
   
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
      this.xValue = new ScalableFloat(x, xScaleLevel);
      this.yValue = new ScalableFloat(y, yScaleLevel);
      
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
      return this.xValue.getScaleLevel();
   }
   
   public float getYScaleLevel()
   {
      return this.yValue.getScaleLevel();
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
      this.xValue.scaleBy(x);
      this.yValue.scaleBy(y);
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
      this.xValue.scaleTo(x);
      this.yValue.scaleTo(y);
      notifyModListeners(ModType.ScaleTo);
   }
   
   public void resizeTo(float x, float y)
   {
      this.xValue.resizeTo(x);
      this.yValue.resizeTo(y);
      notifyModListeners(ModType.ScaleTo);
   }
   
   /**
    * Translates this point by the given amounts.
    * 
    * @param x The amount the point is translated in the x direction.
    * @param y The amount the point is translated in the y direction.
    */
   public void translateBy(float x, float y)
   {
      this.xValue.translateBy(x);
      this.yValue.translateBy(y);
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
      this.xValue.translateTo(x);
      this.yValue.translateTo(y);
      notifyModListeners(ModType.TranslateTo);
   }
   
   /**
    * Used to get a deep copy of this point.
    * 
    * @return A deep copy of this point.
    */
   public FloatPoint2D getCopy()
   {
      return new FloatPoint2D(this);
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
      return new Rectangle2D.Float(getX(), getY(), 0, 0);
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
    * @param delta The distance the point can be from the line 
    *              and still be considered on the line.  This 
    *              parameter exists since points probably do not 
    *              exactly line on the line due to rounding errors.  
    *              A good default value is 1.
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
      
      double u1 = pt1.getX();
      double u2 = pt1.getY();
      
      double v1 = pt2.getX();
      double v2 = pt2.getY();
      
      // get the coordinates of the point where the point 'u' 
      // is treated as the origin
      double x1 = curPt.getX()-u1;
      double x2 = curPt.getY()-u2;
      
      // if the two points are equal see if the given 
      // point is within 'delta' units of the common point.
      if (pt1.equals(pt2))
         return (x1*x1+x2*x2 < LINE_OFFSET_DELTA*LINE_OFFSET_DELTA);
      
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
      
      if (gamma > LINE_OFFSET_DELTA)
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
   
   /*
   public void setX(float x)
   {
      this.srcPoint.x = x;
      this.initPoint.x = x;
   }
   */
   
   public float getX()
   {
      return this.xValue.getValue();
   }
   
   /*
   public void setY(float y)
   {
      this.srcPoint.y = y;
      this.initPoint.y = y;
   }
   */
   
   public float getY()
   {
      return this.yValue.getValue();
   }
   
   @Override
   public String toString()
   {
      return ""+FloatPoint2D.class.getSimpleName()+
             ": ("+getX()+", "+getY()+")";
   }
   
   @Override
   public boolean equals(Object ob)
   {
      if (ob == null)
         throw new NullPointerException();
      
      if ( !(ob instanceof FloatPoint2D) )
         return false;
      
      FloatPoint2D pt2 = (FloatPoint2D)ob;
      
      return this.xValue.equals(pt2.xValue) && 
             this.yValue.equals(pt2.yValue);
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
