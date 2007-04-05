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

package noteLab.util.math;

public class Polynomial
{
   private float[] coefArr;
   
   public Polynomial(int degree)
   {
      if (degree < 0)
         throw new IllegalArgumentException("The degree of a polynomial cannot be negative.  " +
                                            "The degree "+degree+" was given.");
      
      this.coefArr = new float[degree+1];
   }
   
   public float getCoefficient(int index)
   {
      return this.coefArr[index];
   }
   
   public void setCoefficient(int index, float value)
   {
      this.coefArr[index] = value;
   }
   
   public int getDegree()
   {
      return this.coefArr.length-1;
   }
   
   public float eval(float x)
   {
      float sum = 0;
      for (int i=0; i<this.coefArr.length; i++)
         sum += this.coefArr[i]*Math.pow(x, i);
      
      return sum;
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      for (int i=0; i<this.coefArr.length; i++)
      {
         buffer.append(getCoefficient(i));
         buffer.append(" x^");
         buffer.append(i);
         if (i != this.coefArr.length-1)
            buffer.append(" + ");
      }
      
      return buffer.toString();
   }
}
