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

package noteLab.gui.control.geom;

import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import noteLab.gui.ButtonPair;
import noteLab.gui.ButtonPair.Orientation;
import noteLab.gui.control.ValueControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;

public class MValueControl 
                extends JPanel 
                           implements ValueControl<MValue, MValueControl>
{
   private ButtonPair buttonPair;
   private UnitControl unitCombo;
   private JTextField valueField;
   private Vector<ValueChangeListener<MValue, MValueControl>> listenerVec;
   
   public MValueControl(String text, double value, double min, double max, 
                        double stepSize, Unit unit)
   {
      this.buttonPair = new ButtonPair(value, min, max, stepSize, 
                                       Orientation.Horizontal);
      this.buttonPair.addValueChangeListener(
                         new ButtonPairValueChangeListener());
      
      this.unitCombo = new UnitControl(unit);
      this.unitCombo.addValueChangeListener(new UnitValueChangeListener());
      
      this.valueField = new JTextField(15);
      this.valueField.setEditable(false);
      this.valueField.setText(""+this.buttonPair.getValue());
      
      this.listenerVec = new Vector<ValueChangeListener<MValue, 
                                                        MValueControl>>();
      
      setLayout(new FlowLayout(FlowLayout.LEFT));
      add(new JLabel(text));
      add(this.valueField);
      add(this.unitCombo);
      add(this.buttonPair.getLeftDownButton());
      add(this.buttonPair.getRightUpButton());
   }
   
   public MValue getControlValue()
   {
      double val = this.buttonPair.getValue();
      Unit unit = this.unitCombo.getControlValue();
      
      return new MValue(val, unit);
   }

   public void setControlValue(MValue mvalue)
   {
      if (mvalue == null)
         throw new NullPointerException();
      
      Unit unit = mvalue.getUnit();
      double num = mvalue.getValue(unit);
      
      this.buttonPair.setValue(num);
      this.unitCombo.setControlValue(unit);
      
      updateDisplay();
      notifyListeners();
   }
   
   public Unit getUnit()
   {
      return this.unitCombo.getControlValue();
   }
   
   public MValue getPreviousValue()
   {
      return new MValue(this.buttonPair.getPreviousValue(), 
                        this.unitCombo.getPreviousUnit());
   }
   
   public void addValueChangeListener(ValueChangeListener<MValue, 
                                                          MValueControl> 
                                                             listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<MValue, 
                                                        MValueControl> 
                                                           listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   private void updateDisplay()
   {
      String text = ""+this.buttonPair.getValue();
      
      double curVal = this.buttonPair.getValue();
      if ( (getUnit() == Unit.PIXEL) && ( curVal != (int)curVal) )
         text = text + " = "+(int)curVal;
      
      this.valueField.setText(text);
   }
   
   private void notifyListeners()
   {
      MValue oldVal;
      MValue newVal;
      for (ValueChangeListener<MValue, MValueControl> listener : 
              this.listenerVec)
      {
         oldVal = getPreviousValue();
         newVal = getControlValue();
         
         listener.valueChanged(new ValueChangeEvent<MValue, 
                                                    MValueControl>(oldVal, 
                                                                   newVal, 
                                                                   this));
      }
   }
   
   private class UnitValueChangeListener 
                    implements ValueChangeListener<Unit, UnitControl>
   {
      public void valueChanged(ValueChangeEvent<Unit, UnitControl> event)
      {
         Unit prevUnit = event.getPreviousValue();
         Unit newUnit = event.getCurrentValue();
         double min = buttonPair.getMin();
         double max = buttonPair.getMax();
         double step = buttonPair.getStepSize();
         double val = buttonPair.getValue();
         
         int res = Unit.getScreenResolution();
         min = Unit.getValue((float)min, prevUnit, newUnit, res, 1);
         max = Unit.getValue((float)max, prevUnit, newUnit, res, 1);
         step = Unit.getValue((float)step, prevUnit, newUnit, res,1 );
         val = Unit.getValue((float)val, prevUnit, newUnit, res, 1);
         
         buttonPair.setMin(min);
         buttonPair.setMax(max);
         buttonPair.setStepSize(step);
         buttonPair.setValue(val);
         
         updateDisplay();
         notifyListeners();
      }
   }
   
   private class ButtonPairValueChangeListener 
                    implements ValueChangeListener<Double, ButtonPair>
   {
      public void valueChanged(ValueChangeEvent<Double, ButtonPair> event)
      {
         setControlValue(getControlValue());
      }
   }
   
   public static void main(String[] args)
   {
      final double max = 20.0;
      final double min = 0.1;
      final double stepSize = 0.1;
      final double val = 1.4;
      
      MValueControl control = new MValueControl("", val, min, max, 
                                                stepSize, Unit.PIXEL);
      ValueChangeListener listener = new ValueChangeListener()
      {
         public void valueChanged(ValueChangeEvent event)
         {
            System.out.println("Source = "+event.getSource());
            System.out.println("Old unit = "+event.getPreviousValue());
            System.out.println("New unit = "+event.getCurrentValue());
            System.out.println();
         }
      };
      control.addValueChangeListener(listener);
      
      JFrame testFrame = new JFrame("Test MValueControl");
        testFrame.add(control);
        testFrame.pack();
        testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      testFrame.setVisible(true);
   }
}
