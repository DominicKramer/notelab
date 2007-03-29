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

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class DropDownButton 
                extends JToggleButton 
                           implements ActionListener, 
                                      ComponentListener, 
                                      WindowListener
{
   private JWindow popupWindow;
   private boolean drawArrow;
   
   public DropDownButton()
   {
      super();
      
      addActionListener(this);
      
      JPanel popupPane = new JPanel();
      popupPane.setBorder(new TitledBorder(""));
      
      this.popupWindow = new JWindow();
      this.popupWindow.setContentPane(popupPane);
      this.popupWindow.setAlwaysOnTop(true);
      
      this.popupWindow.pack();
      
      this.drawArrow = true;
   }
   
   public JWindow getPopupWindow()
   {
      return this.popupWindow;
   }
   
   public void actionPerformed(ActionEvent event)
   {
      showPopupWindow();
   }
   
   @Override
   public void setSelected(boolean selected)
   {
      super.setSelected(selected);
      this.popupWindow.setVisible(selected);
   }
   
   public boolean getDrawArrow()
   {
      return this.drawArrow;
   }
   
   public void setDrawArrow(boolean drawArrow)
   {
      this.drawArrow = drawArrow;
   }
   
   //this method is invoked when this button gets a new parent
   @Override
   public void addNotify()
   {
      super.addNotify();
      
      Container parent = this;
      while ( (parent != null) && !(parent instanceof Window) )
      {
         parent = parent.getParent();
      }
      
      if (parent != null)
      {
         Window parWin = (Window)parent;
         parWin.addComponentListener(this);
         parWin.addWindowListener(this);
         showPopupWindow();
      }
      else
         System.out.println("DropDownButton:  Could not find parent window");
   }
   
   @Override
   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      
      if (!this.drawArrow)
         return;
      
      int width = getWidth();
      int height = getHeight();
      
      int midW = width/2;
      int midH = height/2;
      int delta = Math.min(width, height);
      delta *= 0.3;
      
      int[] xPts = 
      {
         midW-delta, 
         midW+delta, 
         midW
      };
      
      int[] yPts = 
      {
         midH-delta, 
         midH-delta, 
         midH+delta
      };
      
      g.fillPolygon(xPts, yPts, 3);
   }
   
   public void showPopupWindow()
   {
      try
      {
         Point locOnScreen = getLocationOnScreen();
         locOnScreen.y += getHeight();
         
         this.popupWindow.setLocation(locOnScreen);
         this.popupWindow.pack();
         this.popupWindow.setVisible(this.isSelected());
      }
      catch (IllegalComponentStateException e)
      {
         //CHANGED:  This is ignored because if this exception is 
         //          thrown, the componenet is not visible yet.  
         //          This is not a problem because it will be soon 
         //          visible.
         //System.err.println(e);
      }
   }
   
   private void closePopupWindow()
   {
      this.popupWindow.dispose();
      setSelected(false);
   }
   
   public static void main(String[] args)
   {
      DropDownButton button = new DropDownButton();
      button.getPopupWindow().getContentPane().add(new JSlider(0, 100, 40));
      
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      panel.add(button);
      
      JFrame frame = new JFrame("DropDownButton Demo");
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }

   public void componentResized(ComponentEvent e)
   {
      showPopupWindow();
   }

   public void componentMoved(ComponentEvent e)
   {
      showPopupWindow();
   }

   public void componentShown(ComponentEvent e)
   {
   }

   public void componentHidden(ComponentEvent e)
   {
   }

   public void windowOpened(WindowEvent e)
   {
   }

   public void windowClosing(WindowEvent e)
   {
      closePopupWindow();
   }

   public void windowClosed(WindowEvent e)
   {
      closePopupWindow();
   }

   public void windowIconified(WindowEvent e)
   {
      setSelected(false);
   }

   public void windowDeiconified(WindowEvent e)
   {
      setSelected(false);
   }

   public void windowActivated(WindowEvent e)
   {
   }

   public void windowDeactivated(WindowEvent e)
   {
   }
}
