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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Arc2D.Double;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import noteLab.gui.ButtonPair;
import noteLab.gui.ButtonPair.Orientation;
import noteLab.gui.control.ValueControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.geom.unit.MDimension;
import noteLab.util.geom.unit.MPaperSize;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;

public class MDimensionControl 
                extends JPanel 
                           implements ValueControl<MDimension, 
                                                   MDimensionControl>
{
   private ButtonPair upDownButtons;
   private ButtonPair leftRightButtons;
   
   private UnitControl unitControl;
   private PaperSizeControl sizeControl;
   
   private JLabel dimLabel;
   private Vector<ValueChangeListener<MDimension, 
                                      MDimensionControl>> listenerVec;
   
   public MDimensionControl(double vertVal, double vertMin, double vertMax, 
                            double vertStepSize, double horizVal, 
                            double horizMin, double horizMax, 
                            double horizStepSize)
   {
      ValueChangeListener sizeListener = new PaperSizeChangeListener();
      this.upDownButtons = new ButtonPair(vertVal, vertMin, vertMax, 
                                          vertStepSize, Orientation.Vertical);
      this.upDownButtons.addValueChangeListener(sizeListener);
      
      this.leftRightButtons = new ButtonPair(horizVal, horizMin, horizMax, 
                                             horizStepSize,
                                             Orientation.Horizontal);
      this.leftRightButtons.addValueChangeListener(sizeListener);
      
      this.unitControl = new UnitControl();
      this.unitControl.addItemListener(new UnitListener());
      
      this.sizeControl = new PaperSizeControl();
      this.sizeControl.addItemListener(new PaperSizeListener());
      
      this.dimLabel = new JLabel("               ");
      
      this.listenerVec = 
            new Vector<ValueChangeListener<MDimension, MDimensionControl>>();
      
      JPanel arrowPanel = new JPanel(new BorderLayout());
      arrowPanel.add(this.dimLabel, BorderLayout.CENTER);
      
      arrowPanel.add(this.upDownButtons.getRightUpButton(), 
                     BorderLayout.NORTH);
      arrowPanel.add(this.upDownButtons.getLeftDownButton(), 
                     BorderLayout.SOUTH);
      
      arrowPanel.add(this.leftRightButtons.getLeftDownButton(), 
                     BorderLayout.WEST);
      arrowPanel.add(this.leftRightButtons.getRightUpButton(), 
                     BorderLayout.EAST);
      
      JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      sizePanel.setBorder(new TitledBorder("Paper Size"));
      sizePanel.add(this.sizeControl);
      
      JPanel unitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      unitPanel.setBorder(new TitledBorder("Unit"));
      unitPanel.add(this.unitControl);
      
      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.add(sizePanel);
      leftPanel.add(unitPanel);
      
      setLayout(new BorderLayout());
      add(arrowPanel, BorderLayout.CENTER);
      add(leftPanel, BorderLayout.WEST);
      
      updateDisplay();
   }
   
   public MDimension getControlValue()
   {
      Unit unit = this.unitControl.getControlValue();
      
      MValue mWidth = new MValue(this.leftRightButtons.getValue(), unit);
      MValue mHeight = new MValue(this.upDownButtons.getValue(), unit);
      
      return new MDimension(mHeight, mWidth);
   }

   public void setControlValue(MDimension val)
   {
      if (val == null)
         throw new NullPointerException();
      
      MValue mWidth = val.getWidth();
      Unit unit = mWidth.getUnit();
      MValue mHeight = val.getHeight();
      
      this.unitControl.setControlValue(unit);
      this.sizeControl.setControlValue(MPaperSize.Custom);
      this.leftRightButtons.setValue(mWidth.getValue(unit));
      this.upDownButtons.setValue(mHeight.getValue(unit));
      
      updateDisplay();
   }
   
   private void updateDisplay()
   {
      Unit unit = this.unitControl.getControlValue();
      
      StringBuffer dimBuffer = new StringBuffer();
      dimBuffer.append(this.leftRightButtons.getValue());
      dimBuffer.append(" ");
      dimBuffer.append(unit);
      dimBuffer.append(" x ");
      dimBuffer.append(this.upDownButtons.getValue());
      dimBuffer.append(" ");
      dimBuffer.append(unit);
      
      this.dimLabel.setText(dimBuffer.toString());
      invalidate();
      
      MDimension oldVal;
      MDimension newVal;
      Unit curUnit;
      MValue val1;
      MValue val2;
      for (ValueChangeListener<MDimension, 
                               MDimensionControl> listener : this.listenerVec)
      {
         curUnit = this.unitControl.getControlValue();
         val1 = new MValue(this.upDownButtons.getPreviousValue(), curUnit);
         val2 = new MValue(this.leftRightButtons.getPreviousValue(), curUnit);
         oldVal = new MDimension(val1, val2);
         
         val1 = new MValue(this.upDownButtons.getValue(), curUnit);
         val2 = new MValue(this.leftRightButtons.getValue(), curUnit);
         newVal = new MDimension(val1, val2);
         
         listener.valueChanged(
                     new ValueChangeEvent<MDimension, 
                                          MDimensionControl>(oldVal, 
                                                             newVal, 
                                                             this));
      }
   }
   
   private class UnitListener implements ItemListener
   {
      public void itemStateChanged(ItemEvent e)
      {
         updateDisplay();
      }
   }
   
   private class PaperSizeListener implements ItemListener
   {
      public void itemStateChanged(ItemEvent e)
      {
         MPaperSize paperSize = sizeControl.getControlValue();
         if (paperSize == MPaperSize.Custom)
            return;
         
         setControlValue(paperSize.getMDimension());
      }
   }
   
   public void addValueChangeListener(ValueChangeListener<MDimension, 
                                                          MDimensionControl> 
                                                             listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<MDimension, 
                                                        MDimensionControl> 
                                                           listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }

   private class PaperSizeChangeListener 
                    implements ValueChangeListener<Double, ButtonPair>
   {
      public void valueChanged(ValueChangeEvent<Double, ButtonPair> event)
      {
         sizeControl.setSelectedItem(MPaperSize.Custom);
         updateDisplay();
      }
   }
   
   public static void main(String[] args)
   {
      MDimensionControl control = 
         new MDimensionControl(5, -10, 8, 0.5, 10, 4, 20, 2);
      ValueChangeListener<MDimension, MDimensionControl> listener = 
         new ValueChangeListener<MDimension, MDimensionControl>()
      {
         public void valueChanged(ValueChangeEvent<MDimension, 
                                                   MDimensionControl> event)
         {
            System.out.println("Source = "+event.getSource());
            System.out.println("Old unit = "+event.getPreviousValue());
            System.out.println("New unit = "+event.getCurrentValue());
            System.out.println();
         }
      };
      control.addValueChangeListener(listener);
      
      JFrame testFrame = new JFrame("Test MDimensionControl");
         testFrame.add(control);
         testFrame.pack();
         testFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      testFrame.setVisible(true);
   }
}
