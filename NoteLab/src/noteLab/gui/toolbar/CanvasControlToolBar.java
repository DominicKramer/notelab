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

package noteLab.gui.toolbar;

import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import noteLab.gui.ToolBarButton;
import noteLab.model.canvas.CompositeCanvas;

public class CanvasControlToolBar extends JToolBar implements ActionListener
{
   private CompositeCanvas canvas;
   private JToolBar controlPanel;
   
   private Vector<ToolBarButton> toolBarVec;
   
   public CanvasControlToolBar(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      setFloatable(false);
      setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
      
      this.canvas = canvas;
      
      this.controlPanel = new JToolBar();
      this.controlPanel.setFloatable(false);
      this.controlPanel.setLayout(new CardLayout());
      
      this.toolBarVec = this.canvas.getToolBars();
      
      //FlowLayout mainLayout = new FlowLayout(FlowLayout.LEFT);
      //mainLayout.setVgap(0);
      // Constructor definition:  
      // new EmptyBorder(int top, int left, int bottom, int right)
      setBorder(new EmptyBorder(-10, 5, -10, 0));
      
      ToolBarButton tempButton;
      String cmmd;
      int i = 0;
      for (ToolBarButton toolBar : this.toolBarVec)
      {
         cmmd = ""+(i++);
         
         tempButton = toolBar;
         tempButton.setActionCommand(cmmd);
         tempButton.addActionListener(this);
         add(tempButton);
         
         this.controlPanel.add(toolBar.getSlidingPanel(), cmmd);
      }
      
      //setLayout(mainLayout);
      add(this.controlPanel);
      
      this.toolBarVec.firstElement().doClick();
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();
      if ( !(source instanceof ToolBarButton) )
         return;
      
      ToolBarButton toolButton = (ToolBarButton)source;
      ((CardLayout)this.controlPanel.getLayout()).
                                        show(this.controlPanel, e.getActionCommand());
      
      for (ToolBarButton button : this.toolBarVec)
      {
         button.setEnabled(true);
         button.setSelected(false);
         button.finish();
      }
      
      toolButton.setEnabled(false);
      toolButton.setSelected(true);
      
      this.canvas.setCurrentCanvas(toolButton.getCanvas());
      toolButton.start();
   }
}
