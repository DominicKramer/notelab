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
   
   public SliderControl(String title, String desc, 
                        int min, int max, int value, int spacing, boolean selected)
   {
      this.label = new JLabel(desc);
      
      this.enableBox = new JCheckBox(title, selected);
      this.enableBox.setHorizontalTextPosition(SwingConstants.LEFT);
      
      this.slider = new JSlider(min, max, value);
      this.slider.setMajorTickSpacing(spacing);
      this.slider.setSnapToTicks(true);
      this.slider.setPaintLabels(true);
      this.slider.addChangeListener(this);
      
      this.prevValue = value;
      
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
      return this.slider.getValue();
   }
   
   public void setControlValue(Integer val)
   {
      if (val == null)
         throw new NullPointerException();
      
      this.slider.setValue(val);
   }

   public void actionPerformed(ActionEvent e)
   {
      syncDisplay();
      notifyOfValueChange(0);
   }
   
   private void syncDisplay()
   {
      this.slider.setEnabled(this.enableBox.isSelected());
   }
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame(SliderControl.class.getSimpleName()+" Demo");
      frame.add(new SliderControl("Smooth Strokes: ","Smoothing Factor:",1,10,2,1,true));
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }

   public void stateChanged(ChangeEvent e)
   {
      int curVal = this.slider.getValue();
      notifyOfValueChange(curVal);
      this.prevValue = curVal;
   }
   
   private void notifyOfValueChange(int curVal)
   {
      for (ValueChangeListener<Integer, SliderControl> listener : this.listenerVec)
         listener.valueChanged(new ValueChangeEvent<Integer, SliderControl>(this.prevValue, 
                                                                            curVal, 
                                                                            this));
   }
}
