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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JPanel;

import noteLab.gui.DecoratedButton;
import noteLab.gui.DecoratedButton.Style;
import noteLab.gui.control.ValueControl;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.Selectable;

public abstract class DualDropButton<V, S> 
                         extends JPanel implements ValueControl<V, S>, 
                                                   Selectable
{
   private DecoratedButton decorButton;
   private DropDownButton dropButton;
   protected Vector<ValueChangeListener<V, S>> listenerVec;
   
   public DualDropButton(int width, Color color, 
                         boolean fill, Style style, 
                         int prefSize, float scaleFactor)
   {
      super();
      
      this.decorButton = 
            new DecoratedButton(width, color, fill, style, 
                                prefSize, scaleFactor);
      this.dropButton = new DropDownButton();
      this.dropButton.setPreferredSize(new Dimension(15, prefSize));
      
      this.listenerVec = new Vector<ValueChangeListener<V, S>>();
      
      BorderLayout layout = new BorderLayout();
      layout.setHgap(2);
      setLayout(layout);
      add(this.decorButton, BorderLayout.CENTER);
      add(this.dropButton, BorderLayout.EAST);
   }
   
   @Override
   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      this.decorButton.setEnabled(enabled);
      this.dropButton.setEnabled(enabled);
   }
   
   public boolean isSelected()
   {
      return this.decorButton.isSelected();
   }
   
   public void setSelected(boolean selected)
   {
      this.decorButton.setSelected(selected);
   }
   
   public DecoratedButton getDecoratedButton()
   {
      return this.decorButton;
   }
   
   public DropDownButton getDropDownButton()
   {
      return this.dropButton;
   }
   
   public void doClick()
   {
      this.decorButton.doClick();
   }
   
   public void addValueChangeListener(ValueChangeListener<V, S> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeValueChangeListener(ValueChangeListener<V, S> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public abstract V getPreviousValue();
}
