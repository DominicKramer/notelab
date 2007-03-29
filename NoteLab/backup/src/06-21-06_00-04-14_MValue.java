package noteLab.model.geom.base;

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
   /** Specifies the conversion ratio from pixels to inches. */
   private static final int PIXEL_PER_INCH = 25;
   
   /** Specifies the conversion ratio from centimeters to inches. */
   private static final float CM_PER_INCH = 2.54f;
   
   private static final float PURE_PER_CM = 1;
   
   /** This object's value in terms of this object's unit. */
   private double value;
   
   /** The unit that this object's stored value is in. */
   private Unit unit;
   
   public MValue()
   {
      this(0, Unit.PURE);
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
      this.value = value;
      this.unit = unit;
      setUnit(unit);
   }
   
   /**
    * Used to get this object's value in terms of the unit 
    * returned from <code>getUnit()</code>.
    * 
    * @return This object's raw value.
    */
   public double getValue()
   {
      return this.value;
   }
   
   /**
    * Used to set this object's value in terms of the unit specified.
    * 
    * @param value This object's new value.
    */
   public void setValue(double value, Unit unit)
   {
      if (unit == null)
         throw new NullPointerException();
      
      this.value = getValue(value, getUnit(), unit);
   }
   
   public void setValue(MValue newValue)
   {
      if (newValue == null)
         throw new NullPointerException();
      
      setValue(newValue.getValue(), newValue.getUnit());
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
   
   /**
    * Used to set the unit that this object's raw value should be 
    * stored in terms of.  After the unit is set, this method 
    * converts this object's current value to a new value that is 
    * equivalent to the old value but is in terms of the new unit.
    * 
    * @param newUnit The unit that this object's value should be 
    *                stored in terms of.
    */
   public void setUnit(Unit newUnit)
   {
      this.value = getValue(newUnit);
      this.unit = newUnit;
   }
   
   public double getValue(Unit newUnit)
   {
      return getValue(getValue(), getUnit(), newUnit);
   }
   
   public void multiplyByScalar(double scalar)
   {
      this.value *= scalar;
   }
   
   public void convertToAbs()
   {
      this.value = Math.abs(this.value);
   }
   
   /**
    * Given a value and the current unit that it is represented in, this 
    * method is used to get the value converted in terms of the new unit 
    * specified.
    * 
    * @param curVal The value to convert.
    * @param curUnit The unit that the value is represented in.
    * @param newUnit The unit that the value should be converted in terms 
    *                of.
    * @return The specified value converted in terms of the new unit 
    *         specified.
    */
   public static double getValue(double curVal, 
                                    Unit curUnit, 
                                    Unit newUnit)
   {
      if (curUnit == newUnit)
         return curVal;
      
      if (curUnit == Unit.PIXEL)
      {
         double inchVal = curVal/PIXEL_PER_INCH;
         if (newUnit == Unit.INCH)
            return inchVal;
         else
         {
            double cmVal = CM_PER_INCH*inchVal;
            if (newUnit == Unit.CM)
               return cmVal;
            
            return cmVal*PURE_PER_CM;
         }
      }
      else if (curUnit == Unit.CM)
      {
         double pureVal = curVal*PURE_PER_CM;
         if (curUnit == Unit.PURE)
            return pureVal;
         
         double inchVal = curVal/CM_PER_INCH;
         if (newUnit == Unit.INCH)
            return inchVal;
         
         return inchVal*PIXEL_PER_INCH;
      }
      else if (curUnit == Unit.INCH)
      {
         double pixVal = curVal*PIXEL_PER_INCH;
         if (newUnit == Unit.PIXEL)
            return pixVal;
         
         double cmVal = curVal*CM_PER_INCH;
         if (newUnit == Unit.CM)
            return cmVal;
         
         return cmVal*PURE_PER_CM;
      }
      else if (curUnit == Unit.PURE)
      {
         double cmVal = curVal/PURE_PER_CM;
         if (newUnit == Unit.CM)
            return cmVal;
         
         double inchVal = cmVal/CM_PER_INCH;
         if (newUnit == Unit.INCH)
            return inchVal;
         
         return inchVal*PIXEL_PER_INCH;
      }
      
      return Double.NaN;
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
      return "MValue[ value="+this.value+", unit='"+this.unit+"']";
   }
   
   /**
    * Used to get a deep copy of this object.
    * 
    * @return A deep copy of this object.
    */
   public MValue getCopy()
   {
      return new MValue(this.value, this.unit);
   }
   
   /**
    * Calculates the difference between the given <code>MValues</code>.  
    * To calculate the difference, the value of both <code>MValues</code> 
    * are converted in terms of the specified unit.  The value of the 
    * returned <code>MValue</code> is also represented in terms of the 
    * given unit.
    * 
    * @param unit The unit in which the calculations should be performed.
    * @param value1 The first value.
    * @param value2 The second value.
    * 
    * @return <code>value1 - value2</code>
    */
   public void subtract(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      this.value = this.getValue(this.unit)-value.getValue(this.unit);
   }
   
   /**
    * Calculates the quotient between the given <code>MValues</code>.  
    * To calculate the quotient, the value of both <code>MValues</code> 
    * are converted in terms of the specified unit.  The value of the 
    * returned <code>MValue</code> is also represented in terms of the 
    * given unit.
    * 
    * @param unit The unit in which the calculations should be performed.
    * @param value1 The first value.
    * @param value2 The second value.
    * 
    * @return <code>value1/value2</code>
    */
   public void divideBy(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      this.value = this.getValue(this.unit)/value.getValue(this.unit);
   }
   
   /**
    * Calculates the sum of the given <code>MValues</code>.  
    * To calculate the sum, the values of all of the <code>MValues</code> 
    * are converted in terms of the specified unit.  The value of the 
    * returned <code>MValue</code> is also represented in terms of the 
    * given unit.
    * 
    * @param unit The unit in which the calculations should be performed.
    * @param values The values to add.
    * 
    * @return <code>values[0]+values[1]+....+values[values.length-1]</code>
    */
   public void sum(MValue ... values)
   {
      if (values == null)
         throw new NullPointerException();
      
      double sum = this.getValue(this.unit);
      
      for (MValue value : values)
         sum += value.getValue(this.unit);
      
      this.value = sum;
   }
   
   /**
    * Calculates the product of the given <code>MValues</code>.  
    * To calculate the product, the values of all of the <code>MValues</code> 
    * are converted in terms of the specified unit.  The value of the 
    * returned <code>MValue</code> is also represented in terms of the 
    * given unit.
    * 
    * @param unit The unit in which the calculations should be performed.
    * @param values The values to multiply.
    * 
    * @return <code>values[0]*values[1]*....*values[values.length-1]</code>
    */
   public void product(MValue ... values)
   {
      if (values == null)
         throw new NullPointerException();
      
      double sum = this.getValue(this.unit);
      
      for (MValue value : values)
         sum *= value.getValue(this.unit);
      
      this.value = sum;
   }
   
   /**
    * Determines if the value of <code>value1</code> is greater than 
    * the value of <code>value2</code> where the values are compared in 
    * terms of the specified unit. 
    * 
    * @param unit The unit in which the inequality should be evaluated.
    * @param value1 The value on the left hand side of the inequality.
    * @param value2 The value on the right hand side of the inequality.
    * 
    * @return <code>value1</code>&gt;<code>value2</code>
    */
   public boolean isGreaterThan(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      return this.getValue(this.unit) > value.getValue(this.unit);
   }
   
   /**
    * Determines if the value of <code>value1</code> is greater than or 
    * equal to the value of <code>value2</code> where the values are 
    * compared in terms of the specified unit. 
    * 
    * @param unit The unit in which the inequality should be evaluated.
    * @param value1 The value on the left hand side of the inequality.
    * @param value2 The value on the right hand side of the inequality.
    * 
    * @return <code>value1</code>&gt;=<code>value2</code>
    */
   public boolean isGreaterThanOrEqualTo(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      return this.getValue(this.unit) >= value.getValue(this.unit);
   }
   
   /**
    * Determines if the value of <code>value1</code> is less than 
    * the value of <code>value2</code> where the values are compared in 
    * terms of the specified unit. 
    * 
    * @param unit The unit in which the inequality should be evaluated.
    * @param value1 The value on the left hand side of the inequality.
    * @param value2 The value on the right hand side of the inequality.
    * 
    * @return <code>value1</code>&lt;<code>value2</code>
    */
   public boolean isLessThan(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      return this.getValue(this.unit) < value.getValue(this.unit);
   }
   
   /**
    * Determines if the value of <code>value1</code> is less than or 
    * equal to the value of <code>value2</code> where the values are 
    * compared in terms of the specified unit. 
    * 
    * @param unit The unit in which the inequality should be evaluated.
    * @param value1 The value on the left hand side of the inequality.
    * @param value2 The value on the right hand side of the inequality.
    * 
    * @return <code>value1</code>&lt;=<code>value2</code>
    */
   public boolean isLessThanOrEqualTo(MValue value)
   {
      if (value == null)
         throw new NullPointerException();
      
      return this.getValue(this.unit) <= value.getValue(this.unit);
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      System.out.println(new MValue(198.2, Unit.PIXEL));
   }
}
