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
   
   public void interpolateLinear(int numAdd)
   {
      if (numAdd < 0)
         throw new IllegalArgumentException();
      
      if (numAdd == 0)
         return;
      
      int size = getNumItems();
      
      Vector<FloatPoint2D> newPts = new Vector<FloatPoint2D>(numAdd*(size-1)+size);
      
      FloatPoint2D curPt;
      FloatPoint2D nextPt;
      
      float mx;
      float curX;
      
      float my;
      float curY;
      
      float delta;
      float evalPt;
      
      float newX;
      float newY;
      
      for (int i=0; i<size-1; i++)
      {
         curPt = getItemAt(i);
         nextPt = getItemAt(i+1);
         
         curX = curPt.getX();
         curY = curPt.getY();
         
         mx = (nextPt.getX()-curX)/2f;
         my = (nextPt.getY()-curY)/2f;
         
         newPts.add(curPt);
         
         delta = 2f/(numAdd+2f);
         
         for (int j=1; j<=numAdd; j++)
         {
            evalPt = -1+j*delta;
            
            newX = mx*(evalPt+1)+curX;
            newY = my*(evalPt+1)+curY;
            
            newPts.add(new FloatPoint2D(newX, newY, 
                                        this.xScaleLevel, 
                                        this.yScaleLevel));
         }
      }
      
      clear();
      for (FloatPoint2D pt : newPts)
         addItem(pt);
   }
   
   public void smooth(int numSteps)
   {
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      
      scaleTo(1, 1);
      
      /*
      float factor = 1;
      try
      {
         factor = 0.6f/drawnZoomLevel;
      }
      catch (ArithmeticException e)
      {
         factor = 1;
      }
      */
      
      //float weight = 0.9f;
      //float weight = 1f/(1+factor*factor);
      //System.err.println("Weight = "+weight);
      
      for (int i=1; i<=numSteps; i++)
         smoothWithNAverages(3, 1f, 0.5f);//weight);//0.9f);
      
      scaleTo(xScale, yScale);
   }
   
   private void smoothWithNAverages(int numPts, float scaleWeight, float weight)
   {
      if (weight < 0 || weight > 1)
         throw new IllegalArgumentException("The parameter 'weight' must be " +
                                             "a floating point number in the " +
                                             "range [0,1].  However, a value of "+
                                             weight+" was given.");
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numPts+1)
         return;
      
      // When calculating the new smoothed value if 'newVal' represents the 
      // smoothed value obtained by using averaging and 'oldVal' is the previous 
      // value.  Then the new value is set to the weighted average of the 
      // old value and the new value.  This average is calculated as 
      //    newValue = (newWeight)*(newVal)+(weight)*(oldVal);
      float newWeight = 1-weight;
      
      // To smooth a point 'p', 'numPts' points are used to the left and right of 
      // the point and their values are averaged together to get the new value.  
      // The variable 'baseScale' represents the amount of weight to give to the 
      // current value.  Then as one steps 'i' units (either left or right) away 
      // from the current value, the weight given to the 'ith' point is 
      // specified as 
      //    baseScale - i*delta
      // That is the weights decay linearly in such a way that the current value 
      // is given a weight of 'baseScale' and the other values decay linearly 
      // such that the sum of the weights is one.
      // The array 'scales' below holds the values of the weights from left to 
      // right.  That is the middle value in the array is the weight of the 
      // current value (namely 'baseScale').
      float maxBase = 1f/(numPts+1f);
      float minBase = 1f/(2*numPts+1);
      float baseDiff = maxBase-minBase;
      
      float maxWeight = minBase+scaleWeight*baseDiff;
      float minWeight = (1f-maxWeight*numPts)/(numPts+1f);
      float slope = (minWeight-maxWeight)/numPts;
      
      float[] scales = new float[2*numPts+1];
      scales[numPts] = maxWeight;
      float scaleVal;
      for (int i=1; i<=numPts; i++)
      {
         scaleVal = slope*i+maxWeight;
         scales[numPts+i] = scaleVal;
         scales[numPts-i] = scaleVal;
      }
      
      /*
      float sum = 0;
      for (int i=0; i<scales.length; i++)
      {
         System.err.println("scale "+i+"="+scales[i]);
         sum += scales[i];
      }
      System.err.println("sum="+sum);
      System.err.println();
      */
      
      float[] prevXArr = new float[numPts];
      float[] prevYArr = new float[numPts];
      
      FloatPoint2D ithPt;
      for (int i=0; i<numPts; i++)
      {
         ithPt = getItemAt(i);
         prevXArr[i] = ithPt.getX();
         prevYArr[i] = ithPt.getY();
      }
      
      FloatPoint2D curPt;
      float curPtX;
      float curPtY;
      
      FloatPoint2D tempPt;
      float[] nextXArr = new float[numPts];
      float[] nextYArr = new float[numPts];
      
      float newX;
      float newY;
      
      for (int i=numPts; i<size-numPts; i++)
      {
         curPt = getItemAt(i);
         if (curPt == null)
            continue;
         
         curPtX = curPt.getX();
         curPtY = curPt.getY();
         
         for (int j=i+1; j<i+1+numPts; j++)
         {
            tempPt = getItemAt(j);
            nextXArr[j-i-1] = tempPt.getX();
            nextYArr[j-i-1] = tempPt.getY();
         }
         
         newX = getWeightedAverage(prevXArr, curPtX, nextXArr, numPts, scales);
         newY = getWeightedAverage(prevYArr, curPtY, nextYArr, numPts, scales);
         
         newX = weight*curPtX + newWeight*newX;
         newY = weight*curPtY + newWeight*newY;
         
         for (int j=0; j<numPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numPts-1] = curPtX;
         prevYArr[numPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
      
      smoothWithAverages(maxWeight, weight, 0, numPts);
      smoothWithAverages(maxWeight, weight, size-numPts, size-1);
   }
   
   private static float getWeightedAverage(float[] prevPts, float curPt, float[] nextPts, 
                                             int numPts, float[] scales)
   {
      float sum = 0;
      int index = 0;
      
      for (int i=0; i<numPts; i++)
         sum += prevPts[i]*scales[index++];
      
      sum += curPt*scales[index++];
      
      for (int i=0; i<numPts; i++)
         sum += nextPts[i]*scales[index++];
      
      return sum;
   }
   
   private void smoothWithAverages(float baseScale, float weight, int start, int end)
   {
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 3)
         return;
      
      if (start < 0 || start >= size || end < 0 || end >= size)
         throw new IllegalArgumentException("The parameters 'start' and 'end' must have " +
                                             "values in the range [0,"+size+").  However, " +
                                             "the following values were given:  start=" + 
                                             start+", end="+end);
      
      if (baseScale < 0 || baseScale > 1)
         throw new IllegalArgumentException("The parameter 'baseScale' must be in the range " +
                                             "[0,1].  However, a value of "+baseScale+"was given");
      
      FloatPoint2D firstPt = getItemAt(start);
      float prevX = firstPt.getX();
      float prevY = firstPt.getY();
      
      FloatPoint2D curPt;
      FloatPoint2D nextPt;
      
      float curPtX = 0;
      float curPtY = 0;
      
      float newX = 0;
      float newY = 0;
      
      // Consider the points prevPt, curPt, and nextPt.  Now suppose we want to give weights 
      // to the value of each point when calculating the average so that curPt is given more 
      // weight.  Specifically we'll give prevPt and nextPt a weight represented by 'a' and 
      // curPt a weight represented by 'b'.  Then we need 
      //                               a + b + a = 1
      // We'll set 'b=baseScale' and then calculate 'a' so that 'b+2a=1' as specified above.  
      // The value 'b' would then be the weight given to the current value and 'a' would be 
      // the weight given to each of the points to the left and right of the current point.
      float b = baseScale;
      float a = (1-baseScale)/2f;
      
      float newWeight = 1-weight;
      
      for (int i=start; i<=end; i++)
      {
         curPt = getItemAt(i);
         nextPt = getItemAt(i+1);
         if (curPt == null || nextPt == null)
            continue;
         
         curPtX = curPt.getX();
         curPtY = curPt.getY();
         
         newX = a*prevX + b*curPtX + a*nextPt.getX();
         newY = a*prevY + b*curPtY + a*nextPt.getY();
         
         newX = (weight)*curPtX+newWeight*newX;
         newY = (weight)*curPtY+newWeight*newY;
         
         prevX = curPtX;
         prevY = curPtY;
         
         curPt.translateTo(newX, newY);
      }
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
