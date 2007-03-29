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

package noteLab.util.geom.unit;

import java.awt.Dimension;

import noteLab.util.CopyReady;

/**
 * Represents size of two dimensional area in <code>MValue</code> space.
 * 
 * @author Dominic Kramer
 */
public class MDimension implements CopyReady<MDimension>
{
   /** The length of this dimension. */
   private MValue height;
   
   /** The width of this dimension. */
   private MValue width;
   
   /**
    * Constructs a dimension with the given length and width.
    * 
    * @param height The dimension's length.
    * @param width The dimension's width.
    */
   public MDimension(MValue height, MValue width)
   {
      doSetHeight(height);
      doSetWidth(width);
   }
   
   /**
    * Used to get this dimension's length.
    * 
    * @return This dimension's length.
    */
   public MValue getHeight()
   {
      return this.height;
   }
   
   /**
    * Used to set this dimension's height.
    * 
    * @param height This dimension's new height.
    */
   public void setHeight(MValue height)
   {
      doSetHeight(height);
   }
   
   private void doSetHeight(MValue height)
   {
      if (height == null)
         return;
      
      this.height = height;
   }
   
   /**
    * Used to get this dimension's width.
    * 
    * @return This dimension's width.
    */
   public MValue getWidth()
   {
      return this.width;
   }
   
   /**
    * Used to set this dimension's width.
    * 
    * @param width This dimension's new width.
    */
   public void setWidth(MValue width)
   {
      doSetWidth(width);
   }
   
   private void doSetWidth(MValue width)
   {
      if (width == null)
         throw new NullPointerException();
      
      this.width = width;
   }
   
   /**
    * Used to get a deep copy of this dimension.
    * 
    * @return A deep copy of this dimension.
    */
   public MDimension getCopy()
   {
      return new MDimension(this.height.getCopy(), this.width.getCopy());
   }
   
   /**
    * Used to get a string representation of this dimension.  
    * This method is intended for debugging purposes.
    * 
    * @return A string representation of this dimension.
    */
   @Override
   public String toString()
   {
      return "MDimension[ length='"+this.height+
                        "', width='"+this.width+"']";
   }
   
   public Dimension toPixelDimension()
   {
      int width = (int)this.width.getValue(Unit.PIXEL);
      int height = (int)this.height.getValue(Unit.PIXEL);
      
      return new Dimension(width, height);
   }
   
   public static MDimension sum(Unit unit, MDimension ... dimensions)
   {
      if (unit == null || dimensions == null)
         throw new NullPointerException();
      
      MValue[] widthArr = new MValue[dimensions.length];
      for (int i=0; i<dimensions.length; i++)
         widthArr[i] = dimensions[i].getWidth();
      
      MValue[] heightArr = new MValue[dimensions.length];
      for (int i=0; i<dimensions.length; i++)
         heightArr[i] = dimensions[i].getHeight();
      
      MValue sumWidth = MValue.sum(unit, widthArr);
      MValue sumHeight = MValue.sum(unit, heightArr);
      
      return new MDimension(sumHeight, sumWidth);
   }
   
   public static MDimension difference(Unit unit, MDimension mDim1, 
                                                  MDimension mDim2)
   {
      if (unit == null || mDim1 == null || mDim2 == null)
         throw new NullPointerException();
      
      MValue width = MValue.difference(unit, mDim1.getWidth(), 
                                             mDim2.getWidth());
      MValue height = MValue.difference(unit, mDim1.getHeight(), 
                                              mDim2.getHeight());
      
      return new MDimension(width, height);
   }
   
   public static void main(String[] args)
   {
      MDimension mDim = new MDimension(new MValue(8.5, Unit.INCH), 
                                       new MValue(11.0, Unit.INCH));
      MDimension delta = new MDimension(new MValue(1, Unit.INCH), 
                                        new MValue(4, Unit.INCH));
      
      System.out.println("mDim = "+mDim);
      System.out.println("delta = "+delta);
      System.out.println("mDim+delta = "+
                         MDimension.sum(Unit.INCH, mDim, delta));
   }
}
