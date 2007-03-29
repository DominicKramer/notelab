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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import noteLab.gui.control.ValueControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.geom.unit.MPaperSize;

public class PaperSizeControl 
                extends JComboBox 
                      implements ValueControl<MPaperSize, PaperSizeControl>, 
                                 ItemListener
{
   private MPaperSize preVal;
   private Vector<ValueChangeListener<MPaperSize, 
                                      PaperSizeControl>> listenerVec;
   
   public PaperSizeControl()
   {
      super(MPaperSize.values());
      this.preVal = MPaperSize.values()[0];
      addItemListener(this);
      this.listenerVec = new Vector<ValueChangeListener<MPaperSize, 
                                                        PaperSizeControl>>();
   }
   
   public MPaperSize getControlValue()
   {
      Object ob = getSelectedItem();
      if (ob == null)
         return null;
      
      MPaperSize[] sizeArr = MPaperSize.values();
      for (MPaperSize paperSize : sizeArr)
         if (ob.equals(paperSize))
            return paperSize;
      
      return null;
   }

   public void setControlValue(MPaperSize val)
   {
      if (val == null)
         throw new NullPointerException();
      
      setSelectedItem(val);
   }
   
   public MPaperSize getPreviousValue()
   {
      return this.preVal;
   }
   
   public void itemStateChanged(ItemEvent e)
   {
      MPaperSize oldVal = (MPaperSize)e.getItem();
      MPaperSize newVal = getControlValue();
      
      //if they are the same don't do anything
      if (oldVal == newVal)
         return;
      
      this.preVal = oldVal;
      
      for (ValueChangeListener<MPaperSize, PaperSizeControl> 
              listener : this.listenerVec)
         listener.valueChanged(
                  new ValueChangeEvent<MPaperSize, 
                                       PaperSizeControl>(oldVal, 
                                                         newVal, 
                                                         this));
   }

   public void addValueChangeListener(ValueChangeListener<MPaperSize, 
                                                          PaperSizeControl> 
                                                             listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<MPaperSize, 
                                                        PaperSizeControl> 
                                                           listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public static void main(String[] args)
   {
      PaperSizeControl control = new PaperSizeControl();
      ValueChangeListener<MPaperSize, PaperSizeControl> listener = 
                      new ValueChangeListener<MPaperSize, PaperSizeControl>()
      {
         public void valueChanged(ValueChangeEvent<MPaperSize, 
                                                   PaperSizeControl> event)
         {
            System.out.println("Source = "+event.getSource());
            System.out.println("Old unit = "+event.getPreviousValue());
            System.out.println("New unit = "+event.getCurrentValue());
            System.out.println();
         }
      };
      control.addValueChangeListener(listener);
      
      JFrame frame = new JFrame("MPaperSize Demo");
        frame.add(control);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
