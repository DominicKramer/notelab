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

package noteLab.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;

public class ButtonPair implements ActionListener, GuiSettingsConstants
{
   public enum Orientation
   {
      Horizontal, 
      Vertical
   }
   
   private static final String LEFT_DOWN = "Left_Down";
   private static final String RIGHT_UP = "Right_Up";
   
   private SpinnerNumberModel spinnerModel;
   private AutoButton leftDownButton;
   private AutoButton rightUpButton;
   
   private Vector<ValueChangeListener<Double, ButtonPair>> listenerVec;
   private double prevVal;
   
   public ButtonPair(double value, double min, double max, double stepSize, 
         Orientation orientation)
   {
      this(value, min, max, stepSize, orientation, 1000, 200);
   }
   
   public ButtonPair(double value, double min, double max, double stepSize, 
                     Orientation orientation, int initDelay, int delay)
   {
      if (orientation == null)
         throw new NullPointerException();
      
      this.spinnerModel = new SpinnerNumberModel(value, min, max, stepSize);
      
      DefinedIcon leftDownIcon = DefinedIcon.backward;
      if (orientation == Orientation.Vertical)
         leftDownIcon = DefinedIcon.down;
      
      this.leftDownButton = 
         new AutoButton(leftDownIcon.getIcon(MEDIUM_BUTTON_SIZE), 
                        initDelay, delay);
      this.leftDownButton.addActionListener(this);
      this.leftDownButton.setActionCommand(LEFT_DOWN);
      
      DefinedIcon rightUpIcon = DefinedIcon.forward;
      if (orientation == Orientation.Vertical)
         rightUpIcon = DefinedIcon.up;
      
      this.rightUpButton = 
         new AutoButton(rightUpIcon.getIcon(MEDIUM_BUTTON_SIZE), 
                        initDelay, delay);
      this.rightUpButton.addActionListener(this);
      this.rightUpButton.setActionCommand(RIGHT_UP);
      
      this.listenerVec = 
         new Vector<ValueChangeListener<Double, ButtonPair>>();
      this.prevVal = getValue();
   }
   
   public JButton getLeftDownButton()
   {
      return this.leftDownButton;
   }
   
   public JButton getRightUpButton()
   {
      return this.rightUpButton;
   }
   
   public double getPreviousValue()
   {
      return this.prevVal;
   }
   
   public double getValue()
   {
      return ((Number)this.spinnerModel.getValue()).doubleValue();
   }
   
   public void setValue(double val)
   {
      this.spinnerModel.setValue(new Double(val));
   }
   
   public double getMin()
   {
      double val = getValue();
      Double prevPrev = null;
      Double prev = null;
      do
      {
         prevPrev = prev;
         prev = (Double)this.spinnerModel.getPreviousValue();
         if (prev != null)
            this.spinnerModel.setValue(prev);
      } while (prev != null);
      
      this.spinnerModel.setValue(val);
      if (prevPrev == null)
         return val;
      
      return prevPrev;
   }
   
   public double getMax()
   {
      double val = getValue();
      Double nextNext = null;
      Double next = null;
      do
      {
         nextNext = next;
         next = (Double)this.spinnerModel.getNextValue();
         if (next != null)
            this.spinnerModel.setValue(next);
      } while (next != null);
      
      this.spinnerModel.setValue(val);
      if (nextNext == null)
         return val;
      
      return nextNext;
   }
   
   public double getStepSize()
   {
      return (Double)this.spinnerModel.getStepSize();
   }
   
   public void setMin(double min)
   {
      this.spinnerModel.setMinimum(min);
   }
   
   public void setMax(double max)
   {
      this.spinnerModel.setMaximum(max);
   }
   
   public void setStepSize(double stepSize)
   {
      this.spinnerModel.setStepSize(stepSize);
   }
   
   public void addValueChangeListener(ValueChangeListener<Double, ButtonPair> 
                                         listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeValueChangeListener(ValueChangeListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }

   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      
      double prevVal = getValue();
      Object val = null;
      
      if (cmmd.equals(LEFT_DOWN))
         val = this.spinnerModel.getPreviousValue();
      else if (cmmd.equals(RIGHT_UP))
         val = this.spinnerModel.getNextValue();
      
      if (val != null)
      {
         this.prevVal = prevVal;
         this.spinnerModel.setValue(val);
         
         boolean hasPrev = this.spinnerModel.getPreviousValue() != null;
         boolean hasNext = this.spinnerModel.getNextValue() != null;
         this.leftDownButton.setEnabled(hasPrev);
         this.rightUpButton.setEnabled(hasNext);
         
         this.spinnerModel.setValue(val);
         
         for (ValueChangeListener<Double, ButtonPair> listener : 
                 this.listenerVec)
            listener.valueChanged(
                  new ValueChangeEvent<Double, ButtonPair>(prevVal, 
                                                           getValue(), 
                                                           this));
      }
   }
   
   public static void main(String[] args)
   {
      final double max = 20.0;
      final double min = 0.1;
      final double stepSize = 0.1;
      final double fine = 1.4;
      
      ButtonPair pair = 
         new ButtonPair(fine, min, max, stepSize, Orientation.Horizontal);
      System.out.println("Pair.getValue() = "+pair.getValue());
      System.out.println("Pair.getMin() = "+pair.getMin());
      System.out.println("Pair.getMax()="+pair.getMax());
      System.out.println("Pair.getValue() = "+pair.getValue());
      
      JFrame frame = new JFrame(""+ButtonPair.class.getName()+" Demo");
        frame.setLayout(new FlowLayout(FlowLayout.CENTER));
        frame.add(pair.getLeftDownButton());
        frame.add(pair.getRightUpButton());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
