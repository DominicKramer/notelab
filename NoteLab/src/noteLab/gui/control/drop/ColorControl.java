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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import noteLab.gui.control.drop.pic.PrimitivePic;
import noteLab.gui.control.drop.pic.PrimitivePic.Style;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;


/**
 * @author Dominic Kramer
 */
public class ColorControl extends ComboButton<Color, ColorControl> 
{
   private JColorChooser colorChooser;
   private Color prevColor;
   
   public ColorControl(Color col)
   {
      super(new PrimitivePic(-1, col, true, Style.Square, 1));
      
      this.colorChooser = new JColorChooser(col);
      
      this.prevColor = getControlValue();
      
      JDialog chooserDialog = 
         JColorChooser.createDialog(new JFrame(), "Select A Color", 
                                    true, colorChooser, new OkListener(),
                                    new CancelListener());
      
      getPopupWindow().add(chooserDialog.getContentPane());
   }
   
   public Color getPreviousValue()
   {
      return this.prevColor;
   }
   
   private class OkListener implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {
         setControlValue(colorChooser.getColor());
         getPopupWindow().setVisible(false);
      }
   }

   /**
    * Handles the ActionEvent sent when the cancel button is clicked.
    * 
    * @author Dominic Kramer
    */
   private class CancelListener implements ActionListener
   {
      /**
       * This is called when the cancel button is clicked.
       */
      public void actionPerformed(ActionEvent event)
      {
         colorChooser.setColor(getControlValue());
         getPopupWindow().setVisible(false);
      }
   }
   
   @Override
   public PrimitivePic getButtonPic()
   {
      return (PrimitivePic)super.getButtonPic();
   }
   
   public Color getControlValue()
   {
      return getButtonPic().getColor();
   }

   public void setControlValue(Color col)
   {
      if (col == null)
         throw new NullPointerException();
      
      getButtonPic().setColor(col);
      repaint();
      
      if (this.colorChooser != null)
      {
         this.prevColor = this.colorChooser.getColor();
         this.colorChooser.setColor(col);
      }
      
      for (ValueChangeListener<Color, ColorControl> listener : 
              this.valueListenerVec)
         listener.valueChanged(
                     new ValueChangeEvent<Color, ColorControl>(this.prevColor, 
                                                               col, 
                                                               this));
   }
   
   public static void main(String[] args)
   {
      ColorControl control = new ColorControl(Color.BLUE);
      control.setBorder(new TitledBorder(""));
      ValueChangeListener<Color, ColorControl> listener = 
               new ValueChangeListener<Color, ColorControl>()
      {
         public void valueChanged(ValueChangeEvent<Color, ColorControl> event)
         {
            System.out.println("previous color = "+event.getPreviousValue());
            System.out.println("current color = "+event.getCurrentValue());
            System.out.println("source = "+event.getSource());
         }
      };
      control.addValueChangeListener(listener);
      
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      panel.add(control);
      
      JFrame frame = new JFrame("ColorControl Demo");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
