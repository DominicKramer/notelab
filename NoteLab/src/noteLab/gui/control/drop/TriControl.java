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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import noteLab.gui.control.ValueControl;
import noteLab.gui.listener.SelectionChangeEvent;
import noteLab.gui.listener.SelectionChangeListener;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;

public class TriControl<V, T extends ComboButton<V, T>> 
                                implements ValueControl<V, T>, 
                                           ValueChangeListener<V, T>
{
   private T drop1;
   private T drop2;
   private T drop3;
   private T selDrop;
   private Vector<ValueChangeListener<V, T>> changeListenerVec;
   private Vector<SelectionChangeListener<T, TriControl>> selListenerVec;
   
   public TriControl(T givenDrop1, T givenDrop2, T givenDrop3)
   {
      if (givenDrop1 == null || givenDrop2 == null || givenDrop3 == null)
         throw new NullPointerException();
      
      this.changeListenerVec = new Vector<ValueChangeListener<V, T>>();
      this.selListenerVec = 
         new Vector<SelectionChangeListener<T, TriControl>>();
      
      this.drop1 = givenDrop1;
      ActionListener drop1Listener = new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            updateSelection(drop1);
         }
      };
      this.drop1.addActionListener(drop1Listener);
      this.drop1.addValueChangeListener(this);
      
      this.drop2 = givenDrop2;
      ActionListener drop2Listener = new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            updateSelection(drop2);
         }
      };
      this.drop2.addActionListener(drop2Listener);
      this.drop2.addValueChangeListener(this);
      
      this.drop3 = givenDrop3;
      ActionListener drop3Listener = new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            updateSelection(drop3);
         }
      };
      this.drop3.addActionListener(drop3Listener);
      this.drop3.addValueChangeListener(this);
      
      updateSelection(this.drop1);
   }
   
   private void updateSelection(T selDrop)
   {
      T prevSel = this.selDrop;
      this.selDrop = selDrop;
      
      this.drop1.setSelected(false);
      this.drop2.setSelected(false);
      this.drop3.setSelected(false);
      this.selDrop.setSelected(true);
      
      this.drop1.repaint();
      this.drop2.repaint();
      this.drop3.repaint();
      this.selDrop.repaint();
      
      for (SelectionChangeListener<T, TriControl> listener : 
              this.selListenerVec)
         listener.selectionChanged(
               new SelectionChangeEvent<T, TriControl>(prevSel, 
                                                       this.selDrop, 
                                                       this));
   }
   
   public V getControlValue()
   {
      return this.selDrop.getControlValue();
   }

   public void setControlValue(V val)
   {
      this.selDrop.setControlValue(val);
   }
   
   public V getValue1()
   {
      return this.drop1.getControlValue();
   }
   
   public void setValue1(V val)
   {
      this.drop1.setControlValue(val);
   }
   
   public V getValue2()
   {
      return this.drop2.getControlValue();
   }
   
   public void setValue2(V val)
   {
      this.drop2.setControlValue(val);
   }
   
   public V getValue3()
   {
      return this.drop3.getControlValue();
   }
   
   public void setValue3(V val)
   {
      this.drop3.setControlValue(val);
   }
   
   public T getControl1()
   {
      return this.drop1;
   }
   
   public T getControl2()
   {
      return this.drop2;
   }
   
   public T getControl3()
   {
      return this.drop3;
   }
   
   public void addValueChangeListener(ValueChangeListener<V, T> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.changeListenerVec.contains(listener))
         this.changeListenerVec.add(listener);
   }

   public void removeValueChangeListener(ValueChangeListener<V, T> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.changeListenerVec.remove(listener);
   }
   
   public void addSelectionChangeListener(SelectionChangeListener<T, 
                                                                  TriControl> 
                                                                     listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.selListenerVec.contains(listener))
         this.selListenerVec.add(listener);
   }
   
   public void removeSelectionChangeListener(
                                       SelectionChangeListener<T, 
                                                               TriControl> 
                                                                  listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.selListenerVec.remove(listener);
   }
   
   public void valueChanged(ValueChangeEvent<V, T> event)
   {
      V prevVal = event.getPreviousValue();
      V curVal  = event.getCurrentValue();
      
      for (ValueChangeListener<V, T> listener : this.changeListenerVec)
         listener.valueChanged(new ValueChangeEvent<V, T>(prevVal, 
                                                          curVal, 
                                                          event.getSource()));
   }
   
   /*
   public static void main(String[] args)
   {
      SizeControl control1 = 
         new SizeControl("Control1:  ", 30, 20, 40, 1, Unit.PIXEL, 
                         Style.Square, true, Color.BLUE, 32, 0.5f);
      SizeControl control3 = 
         new SizeControl("Control1:  ", 30, 20, 40, 1, Unit.PIXEL, 
                         Style.Square, true, Color.BLUE, 32, 0.5f);
      SizeControl control2 = 
         new SizeControl("Control1:  ", 30, 20, 40, 1, Unit.PIXEL, 
                         Style.Square, true, Color.BLUE, 32, 0.5f);
      
      TriControl<MValue, SizeControl> triControl = 
         new TriControl<MValue, SizeControl>(control1, control2, control3);
      
      JFrame frame = new JFrame("TriControl Demo");
        frame.add(triControl);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
   */
}
