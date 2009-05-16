/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2009, Dominic Kramer
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

package noteLab.model.geom;

import noteLab.util.CopyReady;

public class ScalableFloat implements CopyReady<ScalableFloat>
{
   private float initVal;
   private float srcVal;
   private float scaleLevel;
   
   public ScalableFloat(float value, float scaleLevel)
   {
      setValue(value, scaleLevel);
   }
   
   public ScalableFloat(ScalableFloat value)
   {
      this(value.getValue(), value.getScaleLevel());
   }
   
   
   public void setValue(float value, float scaleLevel)
   {
      this.initVal = value/scaleLevel;
      this.srcVal = value;
      this.scaleLevel = scaleLevel;
   }
   
   /*
   public void setValue(ScalableFloat value)
   {
      if (value == null)
         throw new NullPointerException();
      
      setValue(value.getValue(), value.getScaleLevel());
   }
   */
   
   public float getScaleLevel()
   {
      return this.scaleLevel;
   }
   
   public float getValue()
   {
      return this.srcVal;
   }
   
   public void resizeTo(float x)
   {
      float oldXScale = this.scaleLevel;
      scaleTo(1);
      
      this.initVal *= x;
      this.srcVal = this.initVal;
      this.scaleLevel = 1;
      
      scaleTo(oldXScale);
   }
   
   public void scaleBy(float x)
   {
      this.scaleLevel *= x;
      this.srcVal = this.initVal * this.scaleLevel;
   }
   
   public void scaleTo(float x)
   {
      this.scaleLevel = x;
      this.srcVal = this.initVal * x;
   }
   
   public void translateBy(float x)
   {
      this.srcVal += x;
      this.initVal = this.srcVal/this.scaleLevel;
   }
   
   public void translateTo(float x)
   {
      this.initVal = x;
      this.srcVal = x;
   }
   
   public ScalableFloat getCopy()
   {
      return new ScalableFloat(this);
   }
   
   @Override
   public boolean equals(Object ob)
   {
      if (ob == null)
         throw new NullPointerException();
      
      if ( !(ob instanceof ScalableFloat) )
         return false;
      
      ScalableFloat value = (ScalableFloat)ob;
      
      return (this.initVal == value.initVal) && 
             (this.srcVal == value.srcVal) && 
             (this.scaleLevel == value.scaleLevel);
   }
   
   @Override
   public String toString()
   {
      return ""+ScalableFloat.class.getSimpleName()+
             ": "+getValue();
   }
}
