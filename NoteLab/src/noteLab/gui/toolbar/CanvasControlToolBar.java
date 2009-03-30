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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import noteLab.gui.ToolBarButton;
import noteLab.model.canvas.CompositeCanvas;

public class CanvasControlToolBar extends JPanel implements ActionListener, 
                                                            ComponentListener
{
   private static final int HGAP = 2;
   
   private CompositeCanvas canvas;
   private JPanel controlPanel;
   private JToolBar buttonBar;
   
   private Vector<ToolBarButton> toolBarVec;
   
   public CanvasControlToolBar(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      setLayout(new FlowLayout(FlowLayout.LEFT, HGAP, 0));
      setBorder(new EmptyBorder(0,0,0,0));
      
      this.canvas = canvas;
      
      this.controlPanel = new JPanel();
      this.controlPanel.setLayout(new CardLayout());
      
      this.toolBarVec = this.canvas.getToolBars();
      
      this.buttonBar = new JToolBar();
      this.buttonBar.setFloatable(false);
      
      ToolBarButton tempButton;
      String cmmd;
      int i = 0;
      for (ToolBarButton toolBar : this.toolBarVec)
      {
         cmmd = ""+(i++);
         
         tempButton = toolBar;
         tempButton.setActionCommand(cmmd);
         tempButton.addActionListener(this);
         this.buttonBar.add(tempButton);
         
         this.controlPanel.add(toolBar.getToolBar(), cmmd);
      }
      
      this.buttonBar.addSeparator();
      
      add(this.buttonBar);
      add(this.controlPanel);
      
      this.toolBarVec.firstElement().doClick();
      
      addComponentListener(this);
      syncToolbarSize();
   }
   
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();
      if ( !(source instanceof ToolBarButton) )
         return;
      
      ToolBarButton toolButton = (ToolBarButton)source;
      ((CardLayout)this.controlPanel.getLayout()).
                                        show(this.controlPanel, 
                                             e.getActionCommand());
      
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
   
   public void componentHidden(ComponentEvent e)
   {
   }
   
   public void componentMoved(ComponentEvent e)
   {
   }
   
   public void componentResized(ComponentEvent e)
   {
      syncToolbarSize();
   }
   
   public void componentShown(ComponentEvent e)
   {
   }
   
   private void syncToolbarSize()
   {
      JToolBar toolbar;
      Dimension prefSize;
      int width = getWidth()-this.buttonBar.getPreferredSize().width-2*HGAP;
      if (width <= 0)
         return;
      
      for (ToolBarButton button : this.toolBarVec)
      {
         toolbar = button.getToolBar();
         prefSize = toolbar.getPreferredSize();
         prefSize.width = width;
         toolbar.setPreferredSize(prefSize);
         toolbar.invalidate();
      }
      
      invalidate();
      revalidate();
   }
}
