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

import noteLab.util.CopyReady;

/**
 * This class encapsulates a value along with a unit.  As such, it makes 
 * the value of a number precise because it also carries along its unit.  
 * Moreover, given an object of this type, it is possible to get its 
 * value converted to any unit.
 * 
 * @author Dominic Kramer
 *
 */
public class MValue implements CopyReady<MValue>
{
   /** This object's value in terms of this object's unit. */
   private double cmValue;
   private double inchValue;
   private double pxValue;
   
   /** The unit that this object's stored value is in. */
   private Unit unit;
   
   public MValue()
   {
      this(0, Unit.CM);
   }
   
   /**
    * Constructs this object with the given value in terms of 
    * the given unit.
    * 
    * @param value The object's value.
    * @param unit The unit that this object's value is in.
    */
   public MValue(double value, Unit unit)
   {
      setValue(value, unit);
      
      /*
      this.value = value;
      this.unit = unit;
      setUnit(unit);
      */
   }
   
   /*
    * Used to get this object's value in terms of the unit 
    * returned from <code>getUnit()</code>.
    * 
    * @return This object's raw value.
    /
   public double getValue()
   {
      return this.value;
   }
   */
   
   /**
    * Used to set this object's value in terms of the unit specified.
    * 
    * @param value This object's new value.
    */
   public void setValue(double value, Unit unit)
   {
      if (unit == null)
         throw new NullPointerException();
      
      int res = Unit.getScreenResolution();
      this.cmValue = Unit.getValue((float)value, unit, Unit.CM, res, 1);
      this.inchValue = Unit.getValue((float)value, unit, Unit.INCH, res, 1);
      this.pxValue = Unit.getValue((float)value, unit, Unit.PIXEL, res, 1);
      this.unit = unit;
   }
   
   public void setValue(MValue newValue)
   {
      if (newValue == null)
         throw new NullPointerException();
      
      this.cmValue = newValue.cmValue;
      this.inchValue = newValue.inchValue;
      this.pxValue = newValue.pxValue;
   }
   
   /**
    * Used to get the unit that this object's value is stored in.
    * 
    * @return The unit that describes this object's raw value.
    */
   public Unit getUnit()
   {
      return this.unit;
   }
   
   /*
    * Used to set the unit that this object's raw value should be 
    * stored in terms of.  After the unit is set, this method 
    * converts this object's current value to a new value that is 
    * equivalent to the old value but is in terms of the new unit.
    * 
    * @param newUnit The unit that this object's value should be 
    *                stored in terms of.
    /
   public void setUnit(Unit newUnit)
   {
      this.value = getValue(newUnit);
      this.unit = newUnit;
   }
   */
   
   public double getValue(Unit unit)
   {
      if (unit == null)
         throw new NullPointerException();
      
      double value = -1;
      if (unit == Unit.CM)
         value =  this.cmValue;
      else if (unit == Unit.INCH)
         value =  this.inchValue;
      else
         value = this.pxValue;
      
      return value;
   }
   
   public void multiplyByScalar(double scalar)
   {
      this.cmValue *= scalar;
      this.inchValue *= scalar;
      this.pxValue *= scalar;
   }
   
   public static MValue abs(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      MValue copy = value.getCopy();
      copy.cmValue = Math.abs(copy.cmValue);
      copy.inchValue = Math.abs(copy.inchValue);
      copy.pxValue = Math.abs(copy.pxValue);
      return copy;
   }
   /**
    * Used to get a string representation of this object.  This method 
    * is meant for debugging purposes only.
    * 
    * @return A string representation of this object.
    */
   @Override
   public String toString()
   {
      return "MValue[ value=("+this.cmValue+", "+
                               this.inchValue+", "+
                               this.pxValue+"), unit='(CM, INCH, PIXEL)']";
   }
   
   /**
    * Used to get a deep copy of this object.
    * 
    * @return A deep copy of this object.
    */
   public MValue getCopy()
   {
      MValue copy = new MValue();
      copy.cmValue = this.cmValue;
      copy.inchValue = this.inchValue;
      copy.pxValue = this.pxValue;
      copy.unit = this.unit;
      
      return copy;
   }
   
   public static MValue difference(Unit unit, MValue value1, MValue value2)
   {
      if (unit == null || value1 == null || value2 == null)
         throw new NullPointerException();
      
      return new MValue(value1.getValue(unit)-value2.getValue(unit), unit);
   }
   
   public static MValue quotient(Unit unit, MValue value1, MValue value2)
   {
      if (unit == null || value1 == null || value2 == null)
         throw new NullPointerException();
      
      return new MValue(value1.getValue(unit)/value2.getValue(unit), unit);
   }
   
   public static MValue sum(Unit unit, MValue ... values)
   {
      if (unit == null || values == null)
         throw new NullPointerException();
      
      double sum = 0;
      
      for (MValue value : values)
         sum += value.getValue(unit);
      
      return new MValue(sum, unit);
   }
   
   public static MValue product(Unit unit, MValue ... values)
   {
      if (unit == null || values == null)
         throw new NullPointerException();
      
      double sum = 1;
      
      for (MValue value : values)
         sum *= value.getValue(unit);
      
      return new MValue(sum, unit);
   }
   
   public static MValue scalarProduct(Unit unit, double scalar, MValue mValue)
   {
      if (unit == null || mValue == null)
         throw new NullPointerException();
      
      double rawVal = mValue.getValue(unit);
      return new MValue(scalar*rawVal, unit);
   }
   
   public boolean isGreaterThan(Unit unit, MValue value)
   {
      if (unit == null || value == null)
         throw new NullPointerException();
      
      return getValue(unit) > value.getValue(unit);
   }
   
   public boolean isGreaterThanOrEqualTo(Unit unit, MValue value)
   {
      if (unit == null || value == null)
         throw new NullPointerException();
      
      return getValue(unit) >= value.getValue(unit);
   }
   
   public boolean isLessThan(Unit unit, MValue value)
   {
      if (unit == null || value == null)
         throw new NullPointerException();
      
      return getValue(unit) < value.getValue(unit);
   }
   
   public boolean isLessThanOrEqualTo(Unit unit, MValue value)
   {
      if (unit == null || value == null)
         throw new NullPointerException();
      
      return getValue(unit) <= value.getValue(unit);
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      MValue val = new MValue(8.5, Unit.INCH);
      
      System.out.println(" value in inch:  "+val.getValue(Unit.INCH));
      System.out.println(" value in cm:  "+val.getValue(Unit.CM));
      System.out.println(" value in pixel:  "+val.getValue(Unit.PIXEL));
   }
}
