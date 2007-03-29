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

import java.util.List;
import java.util.Vector;

import noteLab.model.geom.FloatPoint2D;
import noteLab.util.CopyReady;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.ItemContainer;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

/**
 * This class basically represents a curve.  That is, it represents a 
 * path of points.
 * 
 * @author Dominic Kramer
 */
public class Path 
                extends ItemContainer<FloatPoint2D> 
                           implements CopyReady<Path>, Bounded
{
   private static final float DEFAULT_COMB_FACTOR = 2;//1.5f;//5;
   
   private float distSum;
   
   private int[] xArr;
   private int[] yArr;
   
   /**
    * Constructs an empty path.
    * 
    * @param xScaleLevel The amount this path is scaled to in the x 
    *                    direction when it is constructed.
    * @param yScaleLevel The amount this path is scaled to in the y 
    *                    direction when it is constructed.
    */
   public Path(float xScaleLevel, float yScaleLevel)
   {
      super(xScaleLevel, yScaleLevel);
      
      this.distSum = 0;
      this.xArr = null;
      this.yArr = null;
   }
   
   /**
    * Constructs a path composed of the given points.
    * 
    * @param points The points that make up this path.  The order of the 
    *               points in the path are the order of the points in 
    *               this list.
    * @param xScaleLevel The amount this path is scaled to in the x 
    *                    direction when it is constructed.
    * @param yScaleLevel The amount this path is scaled to in the y 
    *                    direction when it is constructed.
    */
   public Path(List<FloatPoint2D> points, 
               float xScaleLevel, float yScaleLevel)
   {
      this(xScaleLevel, yScaleLevel);
      
      if (points == null)
         throw new NullPointerException();
      
      if (points.isEmpty())
         throw new NullPointerException("Path:  Their must be at least one " +
                                        "MLine supplied to construct a " +
                                        "Path.");
      
      for (FloatPoint2D pt : points)
         addItem(pt);
   }
   
   /**
    * Used to get a deep copy of this path.
    * 
    * @return A deep copy of this path.
    */
   public Path getCopy()
   {
      Path copy = new Path(super.xScaleLevel, super.yScaleLevel);
      for (FloatPoint2D pt : this)
         copy.addItem(pt.getCopy());
      return copy;
   }
   
   @Override
   public void addItem(FloatPoint2D point)
   {
      FloatPoint2D prevPt = getLast();
      if (prevPt != null)
      {
         float xDiff = point.getX()-prevPt.getX();
         float yDiff = point.getY()-prevPt.getY();
         
         float dist = (float)Math.sqrt(xDiff*xDiff + yDiff*yDiff);
         this.distSum += dist;
      }
      
      super.addItem(point);
   }
   
   @Override
   protected void clear()
   {
      this.distSum = 0;
      super.clear();
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getSimpleName());
      buffer.append(":  ");
      for (FloatPoint2D pt : this)
      {
         buffer.append(pt.toString());
         buffer.append(" ");
      }
      
      return buffer.toString();
   }
   
   public void comb()
   {
      if (this.distSum == 0)
         return;
      
      float aveDist = this.distSum/getNumItems();
      if (aveDist == 0)
         return;
      
      float combFactor = DEFAULT_COMB_FACTOR;
      Object combOb = SettingsManager.getSharedInstance().
                                         getValue(SettingsKeys.COMB_FACTOR);
      if (combOb != null && combOb instanceof Float)
         combFactor = (Float)combOb;
      
      /*
      float smallSize = 2;
      smallSize /= Math.max(this.yScaleLevel, this.xScaleLevel);
      
      if (aveDist <= smallSize)
         combFactor = 3;
      */
      
      aveDist *= combFactor;
      
      int numItems = getNumItems();
      
      if (numItems <= 2)
         return;
      
      Vector<FloatPoint2D> newPts = new Vector<FloatPoint2D>(numItems);
      
      FloatPoint2D basePt = getFirst();
      newPts.add(basePt);
      
      float baseX = basePt.getX();
      float baseY = basePt.getY();
      
      float dist = 0;
      float xComp = 0;
      float yComp = 0;
      
      for (FloatPoint2D point : this)
      {
         if (point == null)
            continue;
         
         xComp = point.getX() - baseX;
         yComp = point.getY() - baseY;
         
         dist = xComp*xComp + yComp*yComp;
         if (dist >= aveDist)
         {
            newPts.add(point);
            
            basePt = point;
            baseX = point.getX();
            baseY = point.getY();
         }
      }
      
      FloatPoint2D lastPt = getLast();
      if (!newPts.lastElement().equals(lastPt))
         newPts.add(lastPt);
      
      clear();
      for (FloatPoint2D pt : newPts)
         addItem(pt); 
   }
   
   public void setIsStable(boolean isStable)
   {
      if (!isStable)
      {
         this.xArr = null;
         this.yArr = null;
         
         return;
      }
      
      rebuildArrays();
   }
   
   private void rebuildArrays()
   {
      this.xArr = generateXArray();
      this.yArr = generateYArray();
   }
   
   public int[] getXArray()
   {
      if (this.xArr != null)
         return this.xArr;
      
      return generateXArray();
   }
   
   private int[] generateXArray()
   {
      int size = getNumItems();
      int[] xPts = new int[size];
      FloatPoint2D pt;
      for (int i=0; i<size; i++)
      {
         pt = getItemAt(i);
         if (pt == null)
            continue;
         
         xPts[i] = (int)pt.getX();
      }
      
      return xPts;
   }
   
   public int[] getYArray()
   {
      if (this.yArr != null)
         return this.yArr;
      
      return generateYArray();
   }
   
   private int[] generateYArray()
   {
      int size = getNumItems();
      int[] yPts = new int[size];
      FloatPoint2D pt;
      for (int i=0; i<size; i++)
      {
         pt = getItemAt(i);
         if (pt == null)
            continue;
         
         yPts[i] = (int)pt.getY();
      }
      
      return yPts;
   }
}
