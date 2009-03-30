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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;

public class SliderControl 
                extends JPanel 
                           implements ValueControl<Integer, SliderControl>, 
                                      ActionListener, 
                                      ChangeListener
{
   private JLabel label;
   private JCheckBox enableBox;
   private JSlider slider;
   private Vector<ValueChangeListener<Integer, SliderControl>> listenerVec;
   private int prevValue;
   private int disableValue;
   
   public SliderControl(String title, String desc, 
                        int min, int max, int value,  
                        int disableValue)
   {
      this.label = new JLabel(desc);
      
      this.enableBox = new JCheckBox(title, value > disableValue);
      this.enableBox.setHorizontalTextPosition(SwingConstants.LEFT);
      
      this.slider = new JSlider(min, max, value);
      this.slider.setSnapToTicks(true);
      this.slider.setPaintLabels(true);
      this.slider.setPaintTicks(true);
      this.slider.addChangeListener(this);
      
      this.prevValue = value;
      
      this.disableValue = disableValue;
      
      this.listenerVec = new Vector<ValueChangeListener<Integer,SliderControl>>();
      
      setLayout(new FlowLayout(FlowLayout.LEFT));
      add(this.enableBox);
      add(this.label);
      add(this.slider);
      
      this.enableBox.addActionListener(this);
      syncDisplay();
   }
   
   public boolean isSelected()
   {
      return this.enableBox.isSelected();
   }
   
   public void setSelected(boolean selected)
   {
      this.enableBox.setSelected(selected);
   }
   
   public void addValueChangeListener(ValueChangeListener<Integer, SliderControl> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeValueChangeListener(ValueChangeListener<Integer, SliderControl> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public Integer getControlValue()
   {
      if (!isSelected())
         return this.disableValue;
      
      return this.slider.getValue();
   }
   
   public void setControlValue(Integer val)
   {
      if (val == null)
         throw new NullPointerException();
      
      // Invoking the setValue() method dispatches 
      // a ChangeEvent and thus this class's 
      // stateChanged() method is called as a 
      // consequence of invoking the following method.
      this.slider.setValue(val);
      
      // There appears to be a bug in the JSlider in 
      // the Java 1.5 and Java 6 builds, at least on 
      // Linux systems.  Specifically, the setValue() 
      // method does not update the GUI to reflect the 
      // correct value.  Inverting the display forces 
      // the correct value to be shown.
      this.slider.setInverted(!this.slider.getInverted());
      this.slider.setInverted(!this.slider.getInverted());
      
      syncDisplay();
   }
   
   public void actionPerformed(ActionEvent e)
   {
      if (this.enableBox.isSelected())
         setControlValue(this.prevValue);
      else
         setControlValue(0);
      
      //if (this.slider.getValue() <= this.disableValue)
      //   setControlValue(this.disableValue+1);
      
      syncDisplay();
      notifyOfValueChange(getControlValue());
   }
   
   private void syncDisplay()
   {
      //boolean selected = this.enableBox.isSelected() && 
      //                      this.slider.getValue() > this.disableValue;
      boolean selected = this.slider.getValue() > this.disableValue;
      this.slider.setEnabled(selected);
      this.enableBox.setSelected(selected);
      this.slider.setToolTipText(""+getControlValue());
   }
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame(SliderControl.class.getSimpleName()+" Demo");
      frame.add(new SliderControl("Smooth Strokes: ","Smoothing Factor:",1,10,2,1));
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }

   public void stateChanged(ChangeEvent e)
   {
      int curVal = this.slider.getValue();
      if (curVal > this.disableValue)
         this.prevValue = curVal;
      syncDisplay();
      notifyOfValueChange(getControlValue());
   }
   
   private void notifyOfValueChange(int curVal)
   {
      for (ValueChangeListener<Integer, SliderControl> listener : this.listenerVec)
         listener.valueChanged(new ValueChangeEvent<Integer, SliderControl>(this.prevValue, 
                                                                            curVal, 
                                                                            this));
   }
   
   public void setSnapToTicks(boolean snap)
   {
      this.slider.setSnapToTicks(snap);
   }
   
   public void setMajorTickSpacing(int majorTicks)
   {
      this.slider.setMajorTickSpacing(majorTicks);
   }
   
   public void setMinorTickSpacing(int minorTicks)
   {
      this.slider.setMinorTickSpacing(minorTicks);
   }
}
