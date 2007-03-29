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
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import noteLab.gui.DecoratedButton.Style;
import noteLab.gui.control.geom.MValueControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;

public class SizeControl 
                extends DualDropButton<MValue, SizeControl> 
                           implements ValueChangeListener<MValue, 
                                                          MValueControl>
{
   private MValueControl valueControl;
   
   public SizeControl(String text, double value, double min, double max, 
                      double stepSize, Unit unit, 
                      Style style, boolean fill, Color color, 
                      int prefSize, float scaleFactor)
   {
      super((int)value, color, true, style, prefSize, scaleFactor);
      
      if (text == null || unit == null || style == null || color == null)
         throw new NullPointerException();
      
      this.valueControl = new MValueControl(text, value, min, max, 
                                            stepSize, unit);
      this.valueControl.addValueChangeListener(this);
      
      getDropDownButton().setPreferredSize(new Dimension(12, prefSize+4));
      
      getDropDownButton().getPopupWindow().
         getContentPane().add(this.valueControl);
   }
   
   public MValue getControlValue()
   {
      return this.valueControl.getControlValue();
   }
   
   public MValue getPreviousValue()
   {
      return this.valueControl.getPreviousValue();
   }
   

   public void setControlValue(MValue val)
   {
      if (val == null)
         throw new NullPointerException();
      
      this.valueControl.setControlValue(val);
      getDecoratedButton().
         setValue((int)this.valueControl.getControlValue().
                                         getValue(Unit.PIXEL));
      
      notifyListeners();
   }
   
   public void valueChanged(ValueChangeEvent<MValue, 
                                             MValueControl> event)
   {
      notifyListeners();
      getDecoratedButton().
         setValue((int)event.getCurrentValue().getValue(Unit.PIXEL));
   }
   
   private void notifyListeners()
   {
      MValue prevVal;
      MValue curVal;
      for (ValueChangeListener<MValue, SizeControl> listener : 
              this.listenerVec)
      {
         prevVal = this.valueControl.getPreviousValue();
         curVal = this.valueControl.getControlValue();
         
         listener.valueChanged(new ValueChangeEvent<MValue, 
                                                    SizeControl>(prevVal, 
                                                                 curVal, 
                                                                 this));
      }
   }
   
   public static void main(String[] args)
   {
      String text = "Size:  ";
      double value = 20;
      double min = 10;
      double max = 40;
      double step = 1;
      Unit unit = Unit.PIXEL;
      Color color = Color.BLUE;
      Style type = Style.Circle;
      int prefSize = 32;
      boolean fill = true;
      
      SizeControl control = 
                     new SizeControl(text, value, min, max, step, unit, 
                                     type, fill, color, prefSize, 0.5f);
      ValueChangeListener<MValue, SizeControl> listener = 
                             new ValueChangeListener<MValue, SizeControl>()
      {
         public void valueChanged(ValueChangeEvent<MValue, SizeControl> event)
         {
            System.out.println("previous value = "+event.getPreviousValue());
            System.out.println("current value = "+event.getCurrentValue());
            System.out.println("source = "+event.getSource());
         }
      };
      control.addValueChangeListener(listener);
      
      JFrame frame = new JFrame("SizeControl Demo");
        frame.add(control);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
