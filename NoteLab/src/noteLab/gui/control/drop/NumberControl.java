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

package noteLab.gui.control.drop;

import java.awt.Color;

import javax.swing.JWindow;

import noteLab.gui.DecoratedButton.Style;
import noteLab.gui.control.NumberSpinner;

public class NumberControl 
                extends DualDropButton<Double, NumberControl> 
{
   private NumberSpinner numSpinner;
   
   public NumberControl(String title, String label, double val, 
                        double min, double max, double stepSize, 
                        int prefSize, float scaleFactor)
   {
      super((int)(scaleFactor*val), Color.BLACK, false, 
            Style.Circle, prefSize, scaleFactor);
      
      this.numSpinner = 
         new NumberSpinner(title, label, val, min, max, stepSize);
      
      JWindow window = getDropDownButton().getPopupWindow();
      window.getContentPane().add(this.numSpinner);
   }
   
   @Override
   public Double getPreviousValue()
   {
      return this.numSpinner.getPreviousValue();
   }

   public Double getControlValue()
   {
      return this.numSpinner.getControlValue();
   }

   public void setControlValue(Double val)
   {
      this.numSpinner.setControlValue(val);
   }
}
