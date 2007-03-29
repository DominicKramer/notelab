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
import noteLab.util.geom.unit.Unit;

public class UnitControl 
                extends JComboBox 
                           implements ValueControl<Unit, UnitControl>, 
                                      ItemListener
{
   private Unit prevUnit;
   private Vector<ValueChangeListener<Unit, UnitControl>> listenerVec;
   
   public UnitControl()
   {
      super(new Unit[] { Unit.INCH, Unit.CM, Unit.PIXEL });
      this.listenerVec = new Vector<ValueChangeListener<Unit, UnitControl>>();
      addItemListener(this);
      this.prevUnit = getControlValue();
   }
   
   public UnitControl(Unit unit)
   {
      this();
      setControlValue(unit);
   }
   
   public Unit getControlValue()
   {
      return (Unit)getSelectedItem();
   }

   public void setControlValue(Unit unit)
   {
      if (unit == null)
         throw new NullPointerException();
      
      this.prevUnit = getControlValue();
      setSelectedItem(unit);
   }
   
   public Unit getPreviousUnit()
   {
      return this.prevUnit;
   }

   public void itemStateChanged(ItemEvent e)
   {
      Unit oldVal = (Unit)e.getItem();
      Unit newVal = getControlValue();
      
      //if they are the same don't do anything
      if (oldVal == newVal)
         return;
      
      this.prevUnit = oldVal;
      
      for (ValueChangeListener<Unit, UnitControl> listener : this.listenerVec)
         listener.valueChanged(
                  new ValueChangeEvent<Unit, UnitControl>(oldVal, 
                                                          newVal, 
                                                          this));
   }
   
   public void addValueChangeListener(ValueChangeListener<Unit, 
                                                          UnitControl> 
                                                             listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<Unit, 
                                                        UnitControl> 
                                                           listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public static void main(String[] args)
   {
      UnitControl control = new UnitControl();
      ValueChangeListener<Unit, UnitControl> listener = 
                             new ValueChangeListener<Unit, UnitControl>()
      {
         public void valueChanged(ValueChangeEvent<Unit, UnitControl> event)
         {
            System.out.println("Source = "+event.getSource());
            System.out.println("Old unit = "+event.getPreviousValue());
            System.out.println("New unit = "+event.getCurrentValue());
            System.out.println();
         }
      };
      control.addValueChangeListener(listener);
      
      JFrame frame = new JFrame("UnitControl Demo");
        frame.add(control);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
