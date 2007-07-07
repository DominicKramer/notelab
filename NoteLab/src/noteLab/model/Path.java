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
   private static final float[] DECAY_FACTORS;
   static
   {
      DECAY_FACTORS = new float[5];
      DECAY_FACTORS[0] = 0.0555555555f;
      DECAY_FACTORS[1] = 0.052770798393f;
      DECAY_FACTORS[2] = 0.052638852466f;
      DECAY_FACTORS[3] = 0.052631961567f;
      DECAY_FACTORS[4] = 0.052631599085f;
   }
   
   private static final float[][] SMOOTHING_FACTORS;
   static
   {
      float scalar = 0.9f;
      int length = 0;
      
      SMOOTHING_FACTORS = new float[5][];
      for (int i=1; i<=5; i++)
      {
         length = 2*i+1;
         SMOOTHING_FACTORS[i-1] = new float[length];
         SMOOTHING_FACTORS[i-1][i] = scalar;
         float val;
         for (int j=0; j<i; j++)
         {
            val = (float)(scalar*Math.pow(DECAY_FACTORS[i-1], j+1));
            SMOOTHING_FACTORS[i-1][j] = val;
            SMOOTHING_FACTORS[i-1][length-j-1] = val;
         }
      }
      
      for (int i=0; i<SMOOTHING_FACTORS.length; i++)
      {
         float sum = 0;
         System.err.println("at i = "+(i+1));
         for (int j = 0; j<SMOOTHING_FACTORS[i].length; j++)
         {
            float val = SMOOTHING_FACTORS[i][j];
            System.err.println(val);
            sum += val;
         }
         System.err.println("sum = "+sum);
         System.err.println();
      }
   }
   
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
      if (numSteps <= 0 || numSteps > 5)
         throw new IllegalArgumentException("The Path.smooth() method can only smooth a path " +
                                             "with a 'numSteps' parameter in the integer range " +
                                             "[1,5].  However, a value of "+numSteps+" was given.");
      
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      
      scaleTo(1, 1);
      
      //for (int i=1; i<=numSteps; i++)
      for (int i=1; i<=3; i++)
         smoothWithNAverages(numSteps, SMOOTHING_FACTORS[numSteps-1]);
      
      scaleTo(xScale, yScale);
   }
   
   private float[] getLinearWeights(int numPts, float weight)
   {
      // A weighted average is used to calculate a smoothed value of a point.  
      // To smooth the point 'numPts' are used to the left of the current point 
      // and 'numPts' are used to the right to calculate the average.
      // 
      // There are two possible extremes that can be done to achieve proper 
      // weighting.  That is the middle point could be given maximum weight and 
      // the weight is decreased linearly as the distance from the middle 
      // point increases.  The other extreme is that all points could be given 
      // the same weight.
      // 
      // In the first case the center point has weight 'maxWeight' and in the 
      // the second case it has weight 'minWeight'.  The value of the parameter
      // 'weight' (which is a floating point number in the range [0,1]) 
      // describes to which extreme the weighting should be done.
      // 
      // For example a value of 0.75f implies that the weight of the center 
      // point should be 75% of the way between 'maxBase' and 'minBase' being 
      // closer to 'maxBase'.
      // 
      // After the weight of the middle point is determined from the value of 
      // the parameters 'weight', 'maxBase', and 'minBase' the weight of the 
      // points farthest from the middle point are determined.  The parameter 
      // 'maxWeight' stores the weight of the middle point and the parameter 
      // 'minWeight' stores the weight of the points farthest away.  
      // 
      // The value of the weight of the points farthest away are calculated so 
      // that the weights decrease linearly as one moves away from the center 
      // point and the sum of the weights is equal to one.
      // 
      // That is is 'f(x) = mx+b' represents the weight at 'x' units away from 
      // the center point then we need 'f(0) = b = maxWeight' and 
      // 'f(numPts) = minWeight'.  Last we need 
      //                 b + 2*sum(f(x)) = b + 2*sum(m*x+b) = 1 
      // Where the sum is taken over 'x' as 'x' ranges from '1' to 'numPts' and 
      // 'x' is an integer.  The expressions for 'minWeight' and 'slope' below 
      // follow from this equation.
      
      float maxBase = 1f/(numPts+1f);
      float minBase = 1f/(2*numPts+1);
      float baseDiff = maxBase-minBase;
      
      float maxWeight = minBase+weight*baseDiff;
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
      
      return scales;
   }
   
   private void smoothWithNAverages(int numPts, float[] scales)
   {
      if (numPts <= 0)
         return;
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numPts+1)
         return;
      
      if (scales == null)
         throw new NullPointerException();
      
      if (scales.length != (2*numPts+1))
         throw new IllegalArgumentException("The array of scales given to smooth a path "+
                                             "must have a length of '2*numPts+1'="+(2*numPts+1)+
                                             ".  However, an array of length "+scales.length+
                                             " was given.");
      
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
         
         for (int j=0; j<numPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numPts-1] = curPtX;
         prevYArr[numPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
      
      smoothWithAverages(scales[numPts], 1, numPts);
      smoothWithAverages(scales[numPts], size-numPts, size-2);
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
   
   private void smoothWithAverages(float baseScale, int start, int end)
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
