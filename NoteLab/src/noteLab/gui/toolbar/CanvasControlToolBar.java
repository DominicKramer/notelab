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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.model.canvas.CompositeCanvas;

public class CanvasControlToolBar extends JToolBar implements ActionListener
{
   private CompositeCanvas canvas;
   private JPanel buttonPanel;
   private JPanel controlPanel;
   
   private Vector<ToolBarButton> toolBarVec;
   
   public CanvasControlToolBar(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      
      this.buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      this.buttonPanel.setBorder(new TitledBorder(""));
      
      this.controlPanel = new JPanel(new CardLayout());
      this.controlPanel.setBorder(new TitledBorder(""));
      
      this.toolBarVec = this.canvas.getToolBars();
      
      ToolBarButton tempButton;
      JToolBar tempPanel;
      String cmmd;
      int i = 0;
      for (ToolBarButton toolBar : this.toolBarVec)
      {
         cmmd = ""+(i++);
         
         tempButton = toolBar;
         tempButton.setActionCommand(cmmd);
         tempButton.addActionListener(this);
         this.buttonPanel.add(tempButton);
         
         tempPanel = toolBar.getToolBar();
         
         this.controlPanel.add(
               new JScrollPane(tempPanel, 
                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
                               cmmd);
      }
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      Dimension curDim = this.controlPanel.getPreferredSize();
      Dimension newDim = new Dimension(Math.min(curDim.width, 16*size), 
                                       (int)(1.18*curDim.height));
      
      this.controlPanel.setPreferredSize(newDim);
      this.controlPanel.setSize(newDim);
      
      setLayout(new FlowLayout(FlowLayout.LEFT));
      add(this.buttonPanel);
      add(this.controlPanel);
      
      this.toolBarVec.firstElement().doClick();
      //setPreferredSize(new Dimension(200, 40));
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
