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
import noteLab.util.mod.ModListener;

/*
 * Dominic Kramer
 * NoteLab Project Leader
 * July 8, 2007
 * 
 * The performance of the smooth() method in this class was compared against the 
 * Jarnal program.  This program is a pen-centric journaling application and is 
 * licensed under the GNU GPL just like NoteLab.  The version being used was 
 * Jarnal 908, was written by David K. Levine and Gunnar Teege, and can be found 
 * at http://www.dklevine.com/general/software/tc1000/jarnal.htm as of July 8, 2007.
 * 
 * Even though the GNU GPL encourages use of code from one program to improve another, 
 * no code from the Jarnal project was explicitly used in this class.  Before comparing 
 * NoteLab against Jarnal, methods for smoothing a path using linear, quadratic, and 
 * cubic regression, as well as using cubic spline interpolation were added to this class.  
 * After looking at Jarnal, I found it used linear regression using two points to the 
 * left and right of the current point to smooth strokes.  I also found it used a 
 * weighted average with weights that decayed approximately exponentially.  I was using 
 * weights that decayed linearly.
 * 
 * As such I tried using weights that decayed exponentially and they seemed to work very 
 * well.  Thus even though I did not use any code from the Jarnal project, I used the idea 
 * of using weights that decayed exponentially.  Also even though I thought about using an 
 * exponential decay model before looking at Jarnal, after looking at Jarnal I found that 
 * it was probably the model to use since Jarnal used it.
 * 
 * Also in the process of testing NoteLab against Jarnal, I thought about rendering strokes 
 * multiple times to make them look smoother.  This is not done in Jarnal.  However, I am 
 * mentioning it because I thought of it while comparing NoteLab against Jarnal and as 
 * such the comparison against Jarnal allowed myself to develop this idea.
 * 
 * Last I found that a rounding error in the SwingRenderer2D class caused the strokes to 
 * not look as smooth as they could.  After fixing the problem I found that Jarnal had a 
 * similar problem that it had to overcome.  Again I did not use any code from the Jarnal 
 * project in the fix.  In addition, I also didn't understand that Jarnal had the same 
 * problem until after I fixed the problem in NoteLab.  However, I would still like to note 
 * that this fix could not have been found without comparing the performance of NoteLab 
 * against that of Jarnal.
 * 
 * Therefore, in conclusion, no code from the Jarnal project was used in NoteLab.  However, 
 * it was very useful to compare the performance of NoteLab against that of Jarnal to 
 * find problems in and improve NoteLab.
 */

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
      
      for (ModListener listener : super.modListenerVec)
         copy.addModListener(listener);
      
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
      if (numSteps <= 0)
         return;
      
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      
      scaleTo(1, 1);
      
      float[] scales = new float[3];
      //float middle = 0.4f;
      float middle = 0.6f;
      float side;
      
      for (int i=0; i<numSteps; i++)
      {
         side = (1-middle)/2;
         
         scales[0] = side;
         scales[1] = middle;
         scales[2] = side;
         
         smoothWithNAverages(1, scales);
         
         middle = middle + (1 - middle)*3f/4f;
      }
      
      scaleTo(xScale, yScale);
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
}
