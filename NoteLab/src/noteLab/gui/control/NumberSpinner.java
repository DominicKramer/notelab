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

package noteLab.gui.control;

import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import noteLab.gui.ButtonPair;
import noteLab.gui.ButtonPair.Orientation;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;

public class NumberSpinner 
                extends JPanel 
                           implements ValueControl<Double, NumberSpinner>, 
                                      ValueChangeListener<Double, ButtonPair>
{
   private JLabel title;
   private JTextField valueField;
   private ButtonPair pair;
   private JLabel label;
   private Vector<ValueChangeListener<Double, NumberSpinner>> listenerVec;
   
   public NumberSpinner(String titleTxt, String labelTxt, 
                        double value, double min, 
                        double max, double stepSize)
   {
      if (titleTxt == null || labelTxt == null)
         throw new NullPointerException();
      
      this.title = new JLabel(titleTxt);
      this.label = new JLabel(labelTxt);
      
      this.valueField = new JTextField(5);
      this.valueField.setEditable(false);
      
      if (value < min)
      {
         System.out.println("Warning:  When constructing a number spinner, the current value " +
                            "was set to a value less than that of the minimum value");
         System.out.println("Minimum = "+min);
         System.out.println("Maximum = "+max);
         System.out.println("Current value = "+value);
         System.out.println("Setting the current value to the minimum.");
         
         value = min;
      }
      
      if (value > max)
      {
         System.out.println("Warning:  When constructing a number spinner, the current value " +
                            "was set to a value greater than that of the maximum value");
         System.out.println("Minimum = "+min);
         System.out.println("Maximum = "+max);
         System.out.println("Current value = "+value);
         System.out.println("Setting the current value to the maximum.");
         
         value = max;
      }
      
      this.pair = new ButtonPair(value, min, max, stepSize, 
                                 Orientation.Horizontal);
      this.pair.addValueChangeListener(this);
      
      this.listenerVec = 
         new Vector<ValueChangeListener<Double, NumberSpinner>>();
      
      setLayout(new FlowLayout(FlowLayout.LEFT));
      add(this.title);
      add(this.valueField);
      add(this.label);
      add(this.pair.getLeftDownButton());
      add(this.pair.getRightUpButton());
      
      updateDisplay();
   }
   
   public Double getControlValue()
   {
      return this.pair.getValue();
   }

   public void setControlValue(Double val)
   {
      if (val == null)
         throw new NullPointerException();
      
      this.pair.setValue(val);
      updateDisplay();
   }
   
   public Double getPreviousValue()
   {
      return this.pair.getPreviousValue();
   }
   
   private void updateDisplay()
   {
      this.valueField.setText(""+getControlValue());
   }
   
   public static void main(String[] args)
   {
      String title = "Scale by ";
      String label = "%";
      double value = 10;
      double min = 0;
      double max = 100; 
      double stepSize = 5;
      
      JFrame frame = new JFrame("NumberSpinner Demo");
        frame.add(new NumberSpinner(title, label, value, min, max, stepSize));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }

   public void addValueChangeListener(ValueChangeListener<Double, 
                                                          NumberSpinner> 
                                                             listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<Double, 
                                                        NumberSpinner> 
                                                           listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }

   public void valueChanged(ValueChangeEvent<Double, ButtonPair> event)
   {
      updateDisplay();
      for (ValueChangeListener<Double, NumberSpinner> listener : 
              this.listenerVec)
         listener.valueChanged(
               new ValueChangeEvent<Double, 
                                    NumberSpinner>(event.getPreviousValue(), 
                                                   event.getCurrentValue(), 
                                                   this));
   }
}
