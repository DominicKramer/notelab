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
import noteLab.util.math.Polynomial;
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
   private static final Polynomial POLY_3_1  = new Polynomial(0, 
                                                              0, 
                                                              0,
                                                              0,
                                                              1f/42000f, 
                                                              0, 
                                                              -13f/151200f, 
                                                              0, 
                                                              713f/6048000f,
                                                              0, 
                                                              -23f/302400f,
                                                              0,
                                                              71f/3024000f,
                                                              0, 
                                                              -1f/302400f,
                                                              0, 
                                                              1f/6048000f);
   private static final Polynomial POLY_3_2  = new Polynomial(0,
                                                              0,
                                                              0,
                                                              -1f/10500f,
                                                              -1f/2520f,
                                                              -29f/75600f,
                                                              1f/2400f,
                                                              103f/108000f,
                                                              1f/2520f,
                                                              -7f/21600f,
                                                              -11f/25200f,
                                                              -151f/756000f,
                                                              0,
                                                              1f/21600f,
                                                              1f/50400f,
                                                              1f/378000f);
   private static final Polynomial POLY_3_3  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              1f/2520f, 
                                                              13f/10800f, 
                                                              43f/50400f, 
                                                              -391f/302400f, 
                                                              -1f/360f, 
                                                              -131f/100800f, 
                                                              2f/1575f, 
                                                              71f/43200f, 
                                                              1f/2520f, 
                                                              -71f/302400f, 
                                                              -1f/7200f, 
                                                              -1f/50400f);
   private static final Polynomial POLY_3_4  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              -1f/2100f, 
                                                              -23f/12600f, 
                                                              -29f/25200f, 
                                                              31f/10080f, 
                                                              13f/3150f, 
                                                              -1f/2400f, 
                                                              -1f/350f, 
                                                              -11f/10080f, 
                                                              1f/3150f, 
                                                              13f/50400f, 
                                                              1f/25200f);
   private static final Polynomial POLY_3_5  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              1f/1800f, 
                                                              11f/10800f, 
                                                              -1f/1440f, 
                                                              -19f/8640f, 
                                                              -1f/2400f, 
                                                              19f/14400f, 
                                                              1f/1440f, 
                                                              -1f/8640f, 
                                                              -1f/7200f, 
                                                              -1f/43200f);
   private static final Polynomial POLY_3_6  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              -11f/7560f, 
                                                              -1f/252f, 
                                                              -23f/10080f, 
                                                              3f/560f, 
                                                              1f/105f, 
                                                              1f/480f, 
                                                              -187f/30240f, 
                                                              -11f/2520f, 
                                                              1f/5040f, 
                                                              1f/1120f, 
                                                              1f/5040f);
   private static final Polynomial POLY_3_7  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              1f/504f, 
                                                              31f/5040f, 
                                                              1f/672f, 
                                                              -27f/2240f, 
                                                              -1f/96f, 
                                                              1f/192f, 
                                                              17f/2016f, 
                                                              23f/20160f, 
                                                              -1f/672f, 
                                                              -1f/2240f);
   private static final Polynomial POLY_3_8  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              -11f/5400f, 
                                                              -1f/360f, 
                                                              1f/288f, 
                                                              1f/160f, 
                                                              -1f/1800f, 
                                                              -1f/240f, 
                                                              -1f/864f, 
                                                              1f/1440f, 
                                                              1f/3600f);
   private static final Polynomial POLY_3_9  = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              -13f/4200f, 
                                                              -1f/120f, 
                                                              3f/1120f, 
                                                              3f/160f, 
                                                              1f/200f, 
                                                              -1f/80f, 
                                                              -19f/3360f, 
                                                              1f/480f, 
                                                              3f/2800f);
   private static final Polynomial POLY_3_10 = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              1f/360f, 
                                                              1f/360f, 
                                                              -1f/160f, 
                                                              -1f/160f, 
                                                              1f/240f, 
                                                              1f/240f, 
                                                              -1f/1440f, 
                                                              -1f/1440f);
   private static final Polynomial POLY_3_11 = new Polynomial(0, 
                                                              0, 
                                                              0, 
                                                              -1f/540f, 
                                                              0, 
                                                              1f/240f, 
                                                              0, 
                                                              -1f/360f, 
                                                              0, 
                                                              1f/2160f);
   
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
   
   public void smooth(int numSteps)
   {
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      
      scaleTo(1, 1);
      
      for (int i=1; i<=numSteps; i++)
         smoothWithAverages(1.5f);
      
      scaleTo(xScale, yScale);
   }
   
   private void smoothWithTimedLinear(float speed)
   {
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 3)
         return;
      
      FloatPoint2D firstPt = getFirst();
      float prevX = firstPt.getX();
      float prevY = firstPt.getY();
      
      FloatPoint2D curPt;
      FloatPoint2D nextPt;
      
      float curPtX = 0;
      float curPtY = 0;
      
      float nextPtX;
      float nextPtY;
      
      float newX = 0;
      float newY = 0;
      
      float xDiff;
      float yDiff;
      
      float prevT;
      float nextT;
      
      for (int i=1; i<size-1; i++)
      {
         curPt = getItemAt(i);
         nextPt = getItemAt(i+1);
         if (curPt == null || nextPt == null)
            continue;
         
         curPtX = curPt.getX();
         curPtY = curPt.getY();
         
         nextPtX = nextPt.getX();
         nextPtY = nextPt.getY();
         
         xDiff = curPtX-prevX;
         yDiff = curPtY-prevY;
         prevT = (float)Math.sqrt(xDiff*xDiff + yDiff*yDiff)/speed;
         
         xDiff = nextPtX-curPtX;
         yDiff = nextPtY-curPtY;
         nextT = (float)Math.sqrt(xDiff*xDiff + yDiff*yDiff)/speed;
         
         newX = getTimedLinearValue(prevX, curPtX, nextPtX, prevT, nextT);
         newY = getTimedLinearValue(prevY, curPtY, nextPtY, prevT, nextT);
         
         prevX = curPtX;
         prevY = curPtY;
         
         curPt.translateTo(newX, newY);
      }
   }
   
   private float getTimedLinearValue(float prevX, float curX, float nextX, 
                                     float prevT, float nextT)
   {
      //return (float)Math.pow(prevX*curX*nextX, 1/3f);
      
      //return (prevX+curX+nextX)/3f;
      
      //float denom = prevT/prevX + (prevT+nextT)/curX + nextT/nextX;
      //return 2*(prevT+nextT)/denom;
      
      /*
      float factor = 1;
      float prevW = 1f/prevT;
      float nextW = 1f/nextT;
      float curW = factor*(prevW+nextT);
      float sumT = prevW + curW + nextW;
      
      return (prevW*prevX + curW*curX + nextW*nextX)/sumT;
      */
      
      float sumT = nextT - prevT;
      float sumX = prevX + curX + nextX;
      
      float sumXT = nextT*nextX - prevT*prevX;
      float sumT2 = nextT*nextT + prevT*prevT;
      
      float a = (3*sumXT - sumT*sumX)/(3*sumT2 - sumT*sumT);
      float b = (sumX - a*sumT)/3f;
      
      return b;
   }
   
   private void smoothWithAverages(float weight)
   {
      if (weight < 0)
         throw new IllegalArgumentException("The weight given to smooth a path using the " +
                                            "method of moving averages cannot be negative.  " +
                                            "A value of "+weight+", however, was given.");
      
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 3)
         return;
      
      FloatPoint2D firstPt = getFirst();
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
      // Now let 'k' represent the parameter 'weight' and suppose we want b = k*a
      // (i.e. we want b to be k times the weight of a).  Then we'll have 
      //                               2a + ka = 1
      // Thus 
      //                               a = 1/(2+k)
      // and 
      //                               b = ka
      // This describes the two variables below.
      
      float a = 1f/(2f+weight);
      float b = weight*a;
      
      for (int i=1; i<size-1; i++)
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
   
   private void smoothWithNAverages(int numPts)
   {
      int size = getNumItems();
      // Return if there are not enough points to smooth.  
      // We need at least three points for smoothing.
      if (size < 2*numPts+1)
         return;
      
      // denom = 2*(sum from 1 to numPts) + (numPts+1) 
      //       = 2*numPts*(numPts+1)/2 + (numPts+1)
      //       = numPts*numPts + 2*numPts + 1;
      final float denom = numPts*numPts+2*numPts+1;
      
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
         
         newX = getWeightedAverage(prevXArr, curPtX, nextXArr, numPts, denom);
         newY = getWeightedAverage(prevYArr, curPtY, nextYArr, numPts, denom);
         
         for (int j=0; j<numPts-1; j++)
         {
            prevXArr[j] = prevXArr[j+1];
            prevYArr[j] = prevYArr[j+1];
         }
         
         prevXArr[numPts-1] = curPtX;
         prevYArr[numPts-1] = curPtY;
         
         curPt.translateTo(newX, newY);
      }
   }
   
   private static float getWeightedAverage(float[] prevPts, float curPt, float[] nextPts, 
                                           int numPts, float denom)
   {
      float sum = 0;
      for (int i=0; i<numPts; i++)
         sum += (i+1)*prevPts[i];
      
      sum += (numPts+1)*curPt;
      
      for (int i=0; i<numPts; i++)
         sum += (numPts-i)*nextPts[i];
      
      return sum/denom;
   }
   
   private void smoothImpl(int numPts, int degree)
   {
      if (degree < 0 || degree > 3)
         throw new IllegalArgumentException("Cannot smooth the curve using "+degree+
                                            " degree polynomials.");
      
      if (numPts <= 0)
         return;
      
      int numItems = getNumItems();
      
      if (numItems <= 2*numPts+1)
         return;
      
      Vector<FloatPoint2D> newPts = new Vector<FloatPoint2D>(numItems);
      newPts.add(getFirst().getCopy());
      
      Polynomial xCurve = new Polynomial(degree);
      Polynomial yCurve = new Polynomial(degree);
      FloatPoint2D curPt;
      float xScale = 1;
      float yScale = 1;
      float xVal;
      float yVal;
      int   evalIndex;
      
      int numPtsPlus1 = numPts+1;
      int endDiff = numItems-numPts-1;
      float calcPt = 0;
      
      for (int i=1; i<numItems-1; i++)
      {
         evalIndex = i;
         if (i <= numPts)
            evalIndex = numPtsPlus1;
         else if (i > endDiff)
            evalIndex = endDiff;
         
         if (degree == 0)
         {
            fillConst(evalIndex, numPts, true, xCurve);
            fillConst(evalIndex, numPts, false, yCurve);
         }
         else if (degree == 1)
         {
            fillLinear(evalIndex, numPts, true, xCurve);
            fillLinear(evalIndex, numPts, false, yCurve);
         }
         else if (degree == 2)
         {
            fillQuadratic(evalIndex, numPts, true, xCurve);
            fillQuadratic(evalIndex, numPts, false, yCurve);
         }
         else if (degree == 3)
         {
            fillCubic(evalIndex, numPts, true, xCurve);
            fillCubic(evalIndex, numPts, false, yCurve);
         }
         
         curPt = getItemAt(i);
         if (curPt != null)
         {
            xScale = curPt.getXScaleLevel();
            yScale = curPt.getYScaleLevel();
         }
         else
         {
            xScale = this.xScaleLevel;
            yScale = this.yScaleLevel;
         }
         
         calcPt = numPtsPlus1;
         if (i <= numPts)
            calcPt = i;
         else if (i > endDiff)
            calcPt = numPtsPlus1+(i-endDiff);
         
         xVal = xCurve.eval(calcPt);
         yVal = yCurve.eval(calcPt);
         
         newPts.add(new FloatPoint2D(xVal, 
                                     yVal, 
                                     xScale, 
                                     yScale));
      }
      
      newPts.add(getLast().getCopy());
      
      clear();
      for (FloatPoint2D pt : newPts)
         addItem(pt);
   }
   
   private void fillConst(int index, int numPts, boolean useX, Polynomial curve)
   {
      if (curve == null)
         throw new NullPointerException();
      
      int numTotal = getNumItems();

      if (index < 0 || index >= numTotal)
         throw new ArrayIndexOutOfBoundsException();

      if (numPts < 0)
         throw new IllegalArgumentException();
      
      if ( (index-numPts < 0) || (index+numPts >= numTotal) )
      {
         float val = (useX)?(getItemAt(index).getX()):(getItemAt(index).getY());
         curve.setCoefficient(0, val);
         return;
      }
      
      float sum = 0;
      for (int i=index-numPts; i<=index+numPts; i++)
         sum += (useX)?(getItemAt(i).getX()):(getItemAt(i).getY());

      sum /= (2*numPts+1f);

      curve.setCoefficient(0, sum);
   }
   
   private void fillLinear(int index, int numPts, boolean useX, Polynomial curve)
   {
      if (curve == null)
         throw new NullPointerException();
      
      if (curve.getDegree() < 1)
         throw new IllegalArgumentException("A polynomial of at least degree 1 is needed " +
                                            "to smooth the points.  However a " +
                                            "polynomial of degree "+curve.getDegree()+
                                            " was given.");
      
      if (index < 0 || index >= getNumItems())
         throw new ArrayIndexOutOfBoundsException("index="+index+
                                                  " is not in the range [0,"+
                                                  getNumItems()+")");
      
      if (numPts < 0)
         throw new IllegalArgumentException("The number of points to interpolate the points " +
                                            "cannot be negative.  " +
                                            "The value "+numPts+" was given");
      
      if (index - numPts < 0)
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                             "enough points to the left " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
      if (index + numPts >= getNumItems())
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                            "enough points to the right " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
      final int n = 2*numPts+1;
      final float sumX  = n*(n+1)/2f;
      final float sumX2 = n*(n+1)*(2*n+1)/6f;
      float sumXY = 0;
      float sumY  = 0;
      
      float curVal  = 0;
      int   counter = 1;
      for (int i=index-numPts; i<=index+numPts; i++)
      {
         curVal = (useX)?(getItemAt(i).getX()):(getItemAt(i).getY());
         
         sumXY += counter*curVal;
         sumY  += curVal;
         
         counter++;
      }
      
      float a = (n*sumXY - sumX*sumY)/(n*sumX2 - sumX*sumX);
      float b = 1f/((float)n)*(sumY - a*sumX);
      
      curve.setCoefficient(0, b);
      curve.setCoefficient(1, a);
   }
   
   private void fillCubic(int index, int numPts, boolean useX, Polynomial curve)
   {
      if (curve == null)
         throw new NullPointerException();
      
      if (curve.getDegree() < 3)
         throw new IllegalArgumentException("A polynomial of at least degree 3 is needed " +
                                            "to smooth the points.  However a " +
                                            "polynomial of degree "+curve.getDegree()+
                                            " was given.");
      
      if (index < 0 || index >= getNumItems())
         throw new ArrayIndexOutOfBoundsException("index="+index+
                                                  " is not in the range [0,"+
                                                  getNumItems()+")");
      
      if (numPts < 0)
         throw new IllegalArgumentException("The number of points to interpolate the points " +
                                            "cannot be negative.  " +
                                            "The value "+numPts+" was given");
      
      if (index - numPts < 0)
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                             "enough points to the left " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
      if (index + numPts >= getNumItems())
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                            "enough points to the right " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
      float sumY    = 0;
      float sumxY   = 0;
      float sumx2Y  = 0;
      float sumx3Y  = 0;
      
      float curVal  = 0;
      int   counter = 1;
      for (int i=index-numPts; i<=index+numPts; i++)
      {
         curVal = (useX)?(getItemAt(i).getX()):(getItemAt(i).getY());
         
         sumY   += curVal;
         sumxY  += counter*curVal;
         sumx2Y += counter*counter*curVal;
         sumx3Y += counter*counter*counter*curVal;
         
         counter++;
      }
      
      float numTotal = 2*numPts+1;
      float c1  = POLY_3_1.eval(numTotal);
      float c2  = POLY_3_2.eval(numTotal);
      float c3  = POLY_3_3.eval(numTotal);
      float c4  = POLY_3_4.eval(numTotal);
      float c5  = POLY_3_5.eval(numTotal);
      float c6  = POLY_3_6.eval(numTotal);
      float c7  = POLY_3_7.eval(numTotal);
      float c8  = POLY_3_8.eval(numTotal);
      float c9  = POLY_3_9.eval(numTotal);
      float c10 = POLY_3_10.eval(numTotal);
      float c11 = POLY_3_11.eval(numTotal);
      
      float a0 = (c2*sumY + c3*sumxY +  c4*sumx2Y +  c5*sumx3Y)/c1;
      float a1 = (c3*sumY + c6*sumxY +  c7*sumx2Y +  c8*sumx3Y)/c1;
      float a2 = (c4*sumY + c7*sumxY +  c9*sumx2Y + c10*sumx3Y)/c1;
      float a3 = (c5*sumY + c8*sumxY + c10*sumx2Y + c11*sumx3Y)/c1;
      
      curve.setCoefficient(0, a0);
      curve.setCoefficient(1, a1);
      curve.setCoefficient(2, a2);
      curve.setCoefficient(3, a3);
   }
   
   private void fillQuadratic(int index, int numPts, boolean useX, Polynomial curve)
   {
      if (curve == null)
         throw new NullPointerException();
      
      if (curve.getDegree() < 2)
         throw new IllegalArgumentException("A polynomial of at least degree 2 is needed " +
                                            "to smooth the points.  However a " +
                                            "polynomial of degree "+curve.getDegree()+
                                            " was given.");
      
      if (index < 0 || index >= getNumItems())
         throw new ArrayIndexOutOfBoundsException("index="+index+
                                                  " is not in the range [0,"+
                                                  getNumItems()+")");
      
      if (numPts < 0)
         throw new IllegalArgumentException("The number of points to interpolate the points " +
                                            "cannot be negative.  " +
                                            "The value "+numPts+" was given");
      
      if (index - numPts < 0)
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                             "enough points to the left " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
      if (index + numPts >= getNumItems())
         throw new IllegalArgumentException("Smoothing cannot continue because there is not " +
                                            "enough points to the right " +
                                            "of the current point to use to construct the " +
                                            "smoothing polynomial.");
      
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
      
      /* There is a mistake in these coefficients
      float c1 = (float)(Math.pow(n,3)-3*Math.pow(n,3)+2*n);
      float c2 = (float)(Math.pow(n,4)-Math.pow(n,3)-4*Math.pow(n,2)+4*n);
      float c3 = (float)(Math.pow(n,5)-5*Math.pow(n,3)+4*n);
      float c4 = (float)(9*Math.pow(n,2)+9*n+6);
      float c5 = 12*(float)(16*Math.pow(n,2)+30*n+11);
      float c6 = -18*(float)(1+2*n);
      */
      
      int n = 2*numPts+1;
      float c1 = 2*n-3*n*n+n*n*n;
      float c2 = (-1+n)*n*(-4+n*n);
      float c3 = (float)(4*n-5*Math.pow(n, 3)+Math.pow(n, 5));
      float c4 = 6+9*n+9*n*n;
      float c5 = 12*(1+2*n)*(11+8*n);
      float c6 = -18*(1+2*n);
      
      curve.setCoefficient(0, c4/c1*sumY + c6/c1*sumxY + 30/c1*sumx2Y);
      curve.setCoefficient(1, c6/c1*sumY + c5/c3*sumxY - 180/c2*sumx2Y);
      curve.setCoefficient(2, 30/c1*sumY - 180/c2*sumxY + 180/c3*sumx2Y);
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
   
   public static void main(String[] args)
   {
      Path path = new Path(1, 1);
      path.addItem(new FloatPoint2D(1,1,1,1));
      path.addItem(new FloatPoint2D(3,3,1,1));
      path.addItem(new FloatPoint2D(4,4,1,1));
      path.addItem(new FloatPoint2D(5,5,1,1));
      path.addItem(new FloatPoint2D(2,2,1,1));
      
      Polynomial curve = new Polynomial(3);
      path.fillCubic(2, 2, true, curve);
      
      System.err.println(curve);
   }
}
