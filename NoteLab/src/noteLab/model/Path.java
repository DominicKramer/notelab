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
   private static final int QUAD_REG_NUM_POINTS = 2;
   private static final int LIN_REG_NUM_POINTS = 1; 
   
   private static final float LINEAR_PERCENT = 0f;
   private static final float QUAD_PERCENT = 1f;
   
   /*
   private static final JSlider PERCENT_SLIDER;
   static 
   {
      PERCENT_SLIDER = new JSlider(0,100,50);
      PERCENT_SLIDER.setPaintLabels(true);
      PERCENT_SLIDER.setPaintTicks(true);
      PERCENT_SLIDER.setPaintTrack(true);
      PERCENT_SLIDER.setSnapToTicks(true);
      PERCENT_SLIDER.setMajorTickSpacing(10);
      PERCENT_SLIDER.setMinorTickSpacing(1);
      
      JFrame frame = new JFrame();
      frame.add(PERCENT_SLIDER);
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      frame.setAlwaysOnTop(true);
      frame.setVisible(true);
      frame.pack();
      frame.setSize(new Dimension(1000, frame.getSize().height));
   }
   */
   
   //private static final float[] SMOOTHING_FACTORS = 
   //                                new float[]{0.05f, 0.9f, 0.05f};
   private static final float[] SMOOTHING_FACTORS2 = 
                                     new float[]{0.125f, 0.75f, 0.125f};
   
   /*
   private static final float[][] SMOOTHING_FACTORS;
   static
   {
      float top;
      float side;
      
      SMOOTHING_FACTORS = new float[5][5];
      for (int i=0; i<SMOOTHING_FACTORS.length; i++)
      {
         top = 1-0.1f*(i+1);
         side = 1f/3f*(1-top);
         SMOOTHING_FACTORS[i][0] = 0.5f*side;
         SMOOTHING_FACTORS[i][1] = side;
         SMOOTHING_FACTORS[i][2] = top;
         SMOOTHING_FACTORS[i][3] = side;
         SMOOTHING_FACTORS[i][4] = 0.5f*side;
      }
   }
   */
   
   /*
   // use with scale factor 0.5
   private static final float[] DECAY_FACTORS;
   static
   {
      DECAY_FACTORS = new float[5];
      DECAY_FACTORS[0] = 0.5f;
      DECAY_FACTORS[1] = 0.366025403784f;
      DECAY_FACTORS[2] = 0.34250803168f;
      DECAY_FACTORS[3] = 0.336196693163f;
      DECAY_FACTORS[4] = 0.334263242375f;
   }
   
   private static final float[][] SMOOTHING_FACTORS;
   static
   {
      float scalar = 0.5f;
      int length = 0;
      
      SMOOTHING_FACTORS = new float[5][];
      for (int i=1; i<=5; i++)
      {
         length = 2*i+1;
         SMOOTHING_FACTORS[i-1] = new float[length];
         SMOOTHING_FACTORS[i-1][i] = scalar;
         float val;
         for (int j=1; j<=i; j++)
         {
            val = (float)(scalar*Math.pow(DECAY_FACTORS[i-1], j));
            SMOOTHING_FACTORS[i-1][i+j] = val;
            SMOOTHING_FACTORS[i-1][i-j] = val;
         }
      }
   }
   */
   
   private float distSum;
   
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
      
      // Now there is no limit to the size of 'numSteps' 
      // since 'numSteps' counts the number of times the 
      // Path is smoothed.
      //if (numSteps > 5)
      //   throw new IllegalArgumentException("The Path.smooth() method can only smooth a path " +
      //                                       "with a 'numSteps' parameter in the integer range " +
      //                                       "[1,5].  However, a value of "+numSteps+" was given.");
      
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
      
      /*
      for (int i=1; i<=numSteps; i++)
      {
         if (i%2 == 1)
            smoothWithNAverages(LIN_REG_NUM_POINTS, SMOOTHING_FACTORS2);
         else
            smoothWithQuadReg(QUAD_REG_NUM_POINTS);
      }
      */
      
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
   
   // By comparing the results of the same document smoothed 
   // using various weights given to the linearly smoothed 
   // and quadratically smoothed values, I have found that 
   // the best results occur when only the quadratically 
   // smoothed value is used.  The second best is when 
   // the value of 
   //      0.5*linearValue + 0.5*quadraticValue
   // is used, and the worst results occur when only linear
   // smoothing is used.
   private void smoothWithQuadReg(int numQuadPts)
   {
      if (numQuadPts <= 0) // || numLinPts <= 0)
         return;
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numQuadPts+1)
         return;
      
      /*
      if (2*numLinPts+1 > scales.length)
         throw new IllegalArgumentException("The smoothWithQuadReg() " +
         		                             "method was specified to use " + 
         		                             numLinPts + 
         		                             " points for linear regression " +
         		                             "but " + scales.length + 
         		                             " smooth factors were given.  " +
         		                             "There cannot be more linear " +
         		                             "regression points used than " +
         		                             "there are smoothing factors.");
      */
      
      //float realPercent = PERCENT_SLIDER.getValue()/100.0f;
      //float smoothPercent = 1-realPercent;
      
      // Fill the beginning and the end of the path 
      // symmetrically with copies of the first (last) 
      // 'numPts' in the path respectively.  
      // The smoothing will start at the index 'numPts' 
      // which would correspond to and index of '0' in 
      // the original path.  
      // After the smoothing is done, these extra points 
      // will be removed.
      
      // Get the first 'numPts' points after the first point
      FloatPoint2D[] copyArr = new FloatPoint2D[numQuadPts];
      for (int i=1; i<=numQuadPts; i++)
         copyArr[i-1] = getItemAt(i).getCopy();
      
      // Now place the copied points at the start of the path
      for (int i=0; i<copyArr.length; i++)
         insertItemAt(0, copyArr[i]);
      
      // Get the first 'numPts' points before the last point
      for (int i=1; i<=numQuadPts; i++)
         copyArr[i-1] = getItemAt(size-1-i).getCopy();
      
      // Now place the copied points at the end of the path
      for (int i=0; i<copyArr.length; i++)
         addItem(copyArr[i]);
      
      float[] prevXArr = new float[numQuadPts];
      float[] prevYArr = new float[numQuadPts];
      
      FloatPoint2D ithPt;
      for (int i=0; i<numQuadPts; i++)
      {
         ithPt = getItemAt(i);
         prevXArr[i] = ithPt.getX();
         prevYArr[i] = ithPt.getY();
      }
      
      FloatPoint2D curPt;
      float curPtX;
      float curPtY;
      
      FloatPoint2D tempPt;
      float[] nextXArr = new float[numQuadPts];
      float[] nextYArr = new float[numQuadPts];
      
      float newX;
      float newY;
      
      for (int i=numQuadPts+1; i<=size-numQuadPts-1; i++)
      {
         curPt = getItemAt(i);
         if (curPt == null)
            continue;
         
         curPtX = curPt.getX();
         curPtY = curPt.getY();
         
         for (int j=i+1; j<i+1+numQuadPts; j++)
         {
            tempPt = getItemAt(j);
            nextXArr[j-i-1] = tempPt.getX();
            nextYArr[j-i-1] = tempPt.getY();
         }
         
         newX = getQuadRegValue(i, numQuadPts, true);
         newY = getQuadRegValue(i, numQuadPts, false);
         
         for (int j=0; j<numQuadPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numQuadPts-1] = curPtX;
         prevYArr[numQuadPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
      
      // Remove the extra points at the start
      for (int i=1; i<=numQuadPts; i++)
         removeFirst();
      
      // and at the end of the path
      for (int i=1; i<=numQuadPts; i++)
         removeLast();
   }
   
   /*
   private void smoothWithQuadReg(int numQuadPts, 
                                  int numLinPts, 
                                  float[] scales)
   {
      if (numQuadPts <= 0 || numLinPts <= 0)
         return;
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numQuadPts+1)
         return;
      
      if (2*numLinPts+1 > scales.length)
         throw new IllegalArgumentException("The smoothWithQuadReg() " +
                                            "method was specified to use " + 
                                            numLinPts + 
                                            " points for linear regression " +
                                            "but " + scales.length + 
                                            " smooth factors were given.  " +
                                            "There cannot be more linear " +
                                            "regression points used than " +
                                            "there are smoothing factors.");
      
      //float realPercent = PERCENT_SLIDER.getValue()/100.0f;
      //float smoothPercent = 1-realPercent;
      
      // Fill the beginning and the end of the path 
      // symmetrically with copies of the first (last) 
      // 'numPts' in the path respectively.  
      // The smoothing will start at the index 'numPts' 
      // which would correspond to and index of '0' in 
      // the original path.  
      // After the smoothing is done, these extra points 
      // will be removed.
      
      // Get the first 'numPts' points after the first point
      FloatPoint2D[] copyArr = new FloatPoint2D[numQuadPts];
      for (int i=1; i<=numQuadPts; i++)
         copyArr[i-1] = getItemAt(i).getCopy();
      
      // Now place the copied points at the start of the path
      for (int i=0; i<copyArr.length; i++)
         insertItemAt(0, copyArr[i]);
      
      // Get the first 'numPts' points before the last point
      for (int i=1; i<=numQuadPts; i++)
         copyArr[i-1] = getItemAt(size-1-i).getCopy();
      
      // Now place the copied points at the end of the path
      for (int i=0; i<copyArr.length; i++)
         addItem(copyArr[i]);
      
      float[] prevXArr = new float[numQuadPts];
      float[] prevYArr = new float[numQuadPts];
      
      FloatPoint2D ithPt;
      for (int i=0; i<numQuadPts; i++)
      {
         ithPt = getItemAt(i);
         prevXArr[i] = ithPt.getX();
         prevYArr[i] = ithPt.getY();
      }
      
      FloatPoint2D curPt;
      float curPtX;
      float curPtY;
      
      FloatPoint2D tempPt;
      float[] nextXArr = new float[numQuadPts];
      float[] nextYArr = new float[numQuadPts];
      
      float newX;
      float newY;
      
      for (int i=numQuadPts+1; i<=size-numQuadPts-1; i++)
      {
         curPt = getItemAt(i);
         if (curPt == null)
            continue;
         
         curPtX = curPt.getX();
         curPtY = curPt.getY();
         
         for (int j=i+1; j<i+1+numQuadPts; j++)
         {
            tempPt = getItemAt(j);
            nextXArr[j-i-1] = tempPt.getX();
            nextYArr[j-i-1] = tempPt.getY();
         }
         
         newX = LINEAR_PERCENT*getWeightedAverage(prevXArr, 
                                                  curPtX, 
                                                  nextXArr, 
                                                  numLinPts, 
                                                  scales) + 
                QUAD_PERCENT*getQuadRegValue(i, 
                                             numQuadPts, 
                                             true);
         
         newY = LINEAR_PERCENT*getWeightedAverage(prevYArr, 
                                                  curPtY, 
                                                  nextYArr, 
                                                  numLinPts, 
                                                  scales) + 
                QUAD_PERCENT*getQuadRegValue(i, 
                                             numQuadPts, 
                                             false);
         
         for (int j=0; j<numQuadPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numQuadPts-1] = curPtX;
         prevYArr[numQuadPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
      
      // Remove the extra points at the start
      for (int i=1; i<=numQuadPts; i++)
         removeFirst();
      
      // and at the end of the path
      for (int i=1; i<=numQuadPts; i++)
         removeLast();
   }
   */
   
   private float getQuadRegValue(int index, int numPts, boolean useX)
   {
      float sumY    = 0;
      float sumxY   = 0;
      float sumx2Y  = 0;
      float curVal  = 0;
      int   counter = 1;
      for (int i=index-numPts; i<=index+numPts; i++)
      {
         curVal = (useX)?(getItemAt(i).getX()):(getItemAt(i).getY());
         
         sumY   += curVal;
         sumxY  += counter*curVal;
         sumx2Y += counter*counter*curVal;
         
         counter++;
      }
      
      int n = 2*numPts+1;
      float c1 = 2*n-3*n*n+n*n*n;
      float c2 = (-1+n)*n*(-4+n*n);
      float c3 = (float)(4*n-5*Math.pow(n, 3)+Math.pow(n, 5));
      float c4 = 6+9*n+9*n*n;
      float c5 = 12*(1+2*n)*(11+8*n);
      float c6 = -18*(1+2*n);
      
      float a0 = c4/c1*sumY + c6/c1*sumxY + 30/c1*sumx2Y;
      float a1 = c6/c1*sumY + c5/c3*sumxY - 180/c2*sumx2Y;
      float a2 = 30/c1*sumY - 180/c2*sumxY + 180/c3*sumx2Y;
      
      float val = numPts+1;
      return a2*val*val + a1*val + a0;
   }
   
   /*
   private void smoothWithQuadReg(int numPts, float[] scales)
   {
      if (numPts <= 0)
         return;
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numPts+1)
         return;
      
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
         
         newX = getQuadRegValue(i, numPts, true);
         newY = getQuadRegValue(i, numPts, false);
         
         for (int j=0; j<numPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numPts-1] = curPtX;
         prevYArr[numPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
      
      //smoothWithAverages(scales[numPts], 1, numPts);
      //smoothWithAverages(scales[numPts], size-numPts, size-2);
   }
   
   private float getQuadRegValue(int index, int numPts, boolean useX)
   {
      float sumY    = 0;
      float sumxY   = 0;
      float sumx2Y  = 0;
      float curVal  = 0;
      int   counter = 1;
      for (int i=index-numPts; i<=index+numPts; i++)
      {
         curVal = (useX)?(getItemAt(i).getX()):(getItemAt(i).getY());
         
         sumY   += curVal;
         sumxY  += counter*curVal;
         sumx2Y += counter*counter*curVal;
         
         counter++;
      }
      
      int n = 2*numPts+1;
      float c1 = 2*n-3*n*n+n*n*n;
      float c2 = (-1+n)*n*(-4+n*n);
      float c3 = (float)(4*n-5*Math.pow(n, 3)+Math.pow(n, 5));
      float c4 = 6+9*n+9*n*n;
      float c5 = 12*(1+2*n)*(11+8*n);
      float c6 = -18*(1+2*n);
      
      float a0 = c4/c1*sumY + c6/c1*sumxY + 30/c1*sumx2Y;
      float a1 = c6/c1*sumY + c5/c3*sumxY - 180/c2*sumx2Y;
      float a2 = 30/c1*sumY - 180/c2*sumxY + 180/c3*sumx2Y;
      
      float val = numPts+1;
      return a2*val*val + a1*val + a0;
   }
   */
   
   /**
    * Simplifies this path by removing points that 
    * are not needed.  That is given the width of 
    * the pen used draw this path, a point will be 
    * removed if it is so close to the next point 
    * in the path that the circle from the pen 
    * used to draw the next point encompasses the 
    * current point.
    * 
    * @param width The width of the pen used to 
    *              draw this path.
    */
   public void simplify(float width)
   {
//      if (width <= 0)
//         return;
//      
//      // 'width' will store the square of the radius 
//      // of the circular pen used to draw this path.
//      width = width*width/4f;
//      
//      int numPts = getNumItems();
//      
//      FloatPoint2D nextPt;
//      FloatPoint2D curPt;
//      
//      float xComp;
//      float yComp;
//      float dist;
//      
//      for (int i=numPts-2; i>0; i--)
//      {
//         curPt = getItemAt(i);
//         nextPt = getItemAt(i+1);
//         
//         xComp = curPt.getX()-nextPt.getX();
//         yComp = curPt.getY()-nextPt.getY();
//         dist = xComp*xComp + yComp*yComp;
//         
//         if (dist <= width)
//            removeItemAt(i);
//      }
   }
   
   /*
   public void comb(int factor)
   {
      if (factor <= 0)
         return;
      
      float combFactor = factor/10f;
      
      if (this.distSum == 0)
         return;
      
      int numItems = getNumItems();
      
      if (numItems <= 2)
         return;
      
      float aveDist = this.distSum/numItems;
      aveDist *= combFactor;
      
      // In our calculations below we'll find 
      // the squares of distances and compare 
      // with the square of the average distance 
      // since this is computationally more 
      // efficient (there is no need to calculate 
      // a square root).
      aveDist = aveDist * aveDist;
      
      if (aveDist == 0)
         return;
      
      FloatPoint2D curPt;
      FloatPoint2D nextPt;
      
      float baseX;
      float baseY;
      
      float dist = 0;
      float xComp = 0;
      float yComp = 0;
      
      int numRemoved = 0;
      
      for (int i=numItems-2; i>0; i--)
      {
         curPt = getItemAt(i);
         nextPt = getItemAt(i+1);
         
         baseX = curPt.getX();
         baseY = curPt.getY();
         
         xComp = nextPt.getX() - baseX;
         yComp = nextPt.getY() - baseY;
         
         dist = xComp*xComp + yComp*yComp;
         
         if (dist < aveDist)
         {
            removeItemAt(i);
            numRemoved++;
         }
         
         // On the next iteration nextPt is 
         // this iteration's curPt (even if curPt is 
         // removed from the list since this will 
         // not introduce errors.  That is we don't 
         // want the compare the points in the next 
         // iteration of the loop with the modified 
         // version of this Path but instead we want 
         // to use the actual version of this Path).
         nextPt = curPt;
      }
      
      System.err.println("Removed "+numRemoved+" points");
   }
   */
   
   /*
   public void comb(int factor)
   {
      if (factor <= 0)
         return;
      
      float combFactor = factor/10f;
      
      if (this.distSum == 0)
         return;
      
      int numItems = getNumItems();
      
      if (numItems <= 2)
         return;
      
      float aveDist = this.distSum/numItems;
      aveDist *= combFactor;
      
      // In our calculations below we'll find 
      // the squares of distances and compare 
      // with the square of the average distance 
      // since this is computationally more 
      // efficient (there is no need to calculate 
      // a square root).
      aveDist = aveDist * aveDist;
      
      if (aveDist == 0)
         return;
      
      FloatPoint2D prevPt;
      FloatPoint2D curPt;
      FloatPoint2D nextPt = getLast();
      
      float baseX;
      float baseY;
      
      float distPrev = 0;
      float distNext = 0;
      float xComp = 0;
      float yComp = 0;
      
      int numRemoved = 0;
      
      for (int i=numItems-2; i>0; i--)
      {
         curPt = getItemAt(i);
         prevPt = getItemAt(i-1);
         
         baseX = curPt.getX();
         baseY = curPt.getY();
         
         xComp = prevPt.getX() - baseX;
         yComp = prevPt.getY() - baseY;
         
         distPrev = xComp*xComp + yComp*yComp;
         
         xComp = nextPt.getX() - baseX;
         yComp = nextPt.getY() - baseY;
         
         distNext = xComp*xComp + yComp*yComp;
         
         if (distPrev < aveDist && distNext < aveDist)
         {
            removeItemAt(i);
            numRemoved++;
         }
         
         // On the next iteration nextPt is 
         // this iteration's curPt (even if curPt is 
         // removed from the list since this will 
         // not introduce errors.  That is we don't 
         // want the compare the points in the next 
         // iteration of the loop with the modified 
         // version of this Path but instead we want 
         // to use the actual version of this Path).
         nextPt = curPt;
      }
   }
   */
   
   /*
   public void comb(float combFactor)
   {
      if (combFactor <= 0)
         return;
      
      if (this.distSum == 0)
         return;
      
      int numItems = getNumItems();
      
      if (numItems <= 2)
         return;
      
      float aveDist = this.distSum/numItems;
      
      if (aveDist == 0)
         return;
      
      aveDist *= combFactor;
      
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
      
      int newNumItems = newPts.size();
      System.err.println("Before:  "+numItems+
                         ", After:  "+newNumItems+
                         ", Removed:  "+(numItems-newNumItems)+
                         " = "+ (100*((float)(numItems-newNumItems))/numItems)+"%") ;
   }
   */
}
