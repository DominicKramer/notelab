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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import noteLab.util.Selectable;

public class DecoratedButton extends JButton implements Selectable
{
   private static final int DELTA = 2;
   private static final Color SELECTION_COLOR = Color.BLUE;
   
   public enum Style
   {
      Line, 
      Circle, 
      Square
   }
   
   private int imageWidth;
   private Color color;
   private boolean fill;
   private Style style;
   private boolean isSelected;
   private float scaleFactor;
   
   public DecoratedButton(int imageWidth, Color color, 
                          boolean fill, Style style, 
                          int buttonWidth, float scaleFactor)
   {
      setValue(imageWidth);
      setColor(color);
      setStyle(style);
      setFilled(fill);
      setSelected(false);
      scaleTo(scaleFactor);
      
      buttonWidth += 2*DELTA;
      setPreferredSize(new Dimension(buttonWidth, buttonWidth));
   }
   
   public float getScaleFactor()
   {
      return this.scaleFactor;
   }
   
   public void resizeTo(float factor)
   {
      this.imageWidth *= factor;
   }
   
   public void scaleBy(float scaleFactor)
   {
      if (scaleFactor <= 0)
         throw new IllegalArgumentException("The scale factor cannot be " +
                                            "<= 0 but a value of "+
                                            scaleFactor+" was supplied.");
      
      this.scaleFactor *= scaleFactor;
   }
   
   public void scaleTo(float scaleFactor)
   {
      if (scaleFactor <= 0)
         throw new IllegalArgumentException("The scale factor cannot be " +
                                            "<= 0 but a value of "+
                                            scaleFactor+" was supplied.");
      
      this.scaleFactor = scaleFactor;
   }
   
   public boolean isSelected()
   {
      return this.isSelected;
   }
   
   public void setSelected(boolean selected)
   {
      this.isSelected = selected;
   }

   public Color getColor()
   {
      return this.color;
   }

   public void setColor(Color color)
   {
      this.color = color;
      repaint();
   }
   
   public int getValue()
   {
      return this.imageWidth;
   }

   public void setValue(int width)
   {
      this.imageWidth = width;
      repaint();
   }
   
   public Style getStyle()
   {
      return this.style;
   }
   
   public void setStyle(Style style)
   {
      if (style == null)
         throw new NullPointerException();
      
      this.style = style;
      repaint();
   }
   
   public boolean isFilled()
   {
      return this.fill;
   }
   
   public void setFilled(boolean fill)
   {
      this.fill = fill;
      repaint();
   }
   
   @Override
   public void paint(Graphics g)
   {
      super.paint(g);
      
      g.setColor(this.color);
      
      int realValue = (int)(this.scaleFactor*this.imageWidth);
      
      Dimension sizeDim = getSize();
      
      int xMid = sizeDim.width/2;
      int x1 = xMid-(int)Math.round(realValue/2.0);
      int x2 = xMid+realValue/2;
      
      int yMid = sizeDim.height/2;
      int y1 = yMid-(int)Math.round(realValue/2.0);
      
      if (this.style == Style.Circle)
      {
         if (this.fill)
            g.fillOval(x1, y1, realValue, realValue);
         else
            g.drawOval(x1, y1, realValue, realValue);
      }
      else if (this.style == Style.Square)
      {
         if (this.fill)
            g.fillRect(x1, y1, realValue, realValue);
         else
            g.drawRect(x1, y1, realValue, realValue);
      }
      else if (this.style == Style.Line)
      {
         int littleDelta = 2;
         int yTop = yMid+littleDelta;
         int yBottom = yMid-littleDelta;
         
         g.drawLine(x1, xMid, x2, xMid);
         g.drawLine(x1, yTop, x1, yBottom);
         g.drawLine(x2, yTop, x2, yBottom);
      }
      
      if (this.isSelected)
      {
         int width = sizeDim.width-4;
         int height = sizeDim.height-4;
         g.setColor(SELECTION_COLOR);
         g.draw3DRect(1, 1, width, height, true);
      }
   }
   
   public static void main(String[] args)
   {
      DecoratedButton square = 
         new DecoratedButton(20, Color.BLUE, true, Style.Square, 32, 0.5f);
      DecoratedButton circle = 
         new DecoratedButton(20, Color.BLUE, true, Style.Circle, 32, 0.25f);
      DecoratedButton line = 
         new DecoratedButton(20, Color.BLUE, true, Style.Line, 32, 1);
      
      JFrame frame = new JFrame("DecoratedButton Demo");
        frame.setLayout(new FlowLayout());
        frame.add(square);
        frame.add(circle);
        frame.add(line);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
      frame.setVisible(true);
   }
}
