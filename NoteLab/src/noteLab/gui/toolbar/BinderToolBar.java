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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.Tooltips;
import noteLab.model.Page;
import noteLab.model.binder.Binder;
import noteLab.model.binder.BinderListener;
import noteLab.model.canvas.CompositeCanvas;

public class BinderToolBar implements ActionListener, 
                                      BinderListener, 
                                      GuiSettingsConstants
{
   private static final String ZOOM_OUT = "ZoomOut";
   private static final String ZOOM_NORMAL = "ZoomNormal";
   private static final String ZOOM_IN = "ZoomIn";
   
   private static final String NEXT = "Next";
   private static final String PREVIOUS = "Previous";
   private static final String CURRENT = "Current";
   
   private static final String NEW_PAGE = "NewPage";
   
   private static final String REFRESH = "Refresh";
   
   private static final float ZOOM_IN_FACTOR = 1.2f;
   private static final float ZOOM_OUT_FACTOR = 1/ZOOM_IN_FACTOR;
   
   private JButton zoomInButton;
   private JButton zoomNormalButton;
   private JButton zoomOutButton;
   
   private JButton nextButton;
   private JButton prevButton;
   private JButton currentButton;
   
   private JButton newPageButton;
   
   private JButton refreshButton;
   
   private CompositeCanvas canvas;
   
   public BinderToolBar(CompositeCanvas canvas)
   {
      this.canvas = canvas;
      
      // construct the buttons for zooming
      this.zoomInButton = 
         new JButton(DefinedIcon.zoom_in.getIcon(BUTTON_SIZE));
      this.zoomInButton.setActionCommand(ZOOM_IN);
      this.zoomInButton.addActionListener(this);
      this.zoomInButton.setToolTipText(Tooltips.ZOOM_IN);
      
      this.zoomNormalButton = 
         new JButton(DefinedIcon.zoom_100.getIcon(BUTTON_SIZE));
      this.zoomNormalButton.setActionCommand(ZOOM_NORMAL);
      this.zoomNormalButton.addActionListener(this);
      this.zoomNormalButton.setToolTipText(Tooltips.ZOOM_100);
      
      this.zoomOutButton = 
         new JButton(DefinedIcon.zoom_out.getIcon(BUTTON_SIZE));
      this.zoomOutButton.setActionCommand(ZOOM_OUT);
      this.zoomOutButton.addActionListener(this);
      this.zoomOutButton.setToolTipText(Tooltips.ZOOM_OUT);
      
      // construct the buttons for moving to the next or 
      // previous page
      this.nextButton = 
         new JButton(DefinedIcon.forward.getIcon(BUTTON_SIZE));
      this.nextButton.setActionCommand(NEXT);
      this.nextButton.addActionListener(this);
      this.nextButton.setToolTipText(Tooltips.NEXT_PAGE);
      
      this.prevButton = 
         new JButton(DefinedIcon.backward.getIcon(BUTTON_SIZE));
      this.prevButton.setActionCommand(PREVIOUS);
      this.prevButton.addActionListener(this);
      this.prevButton.setToolTipText(Tooltips.PREVIOUS_PAGE);
      
      this.currentButton = 
         new JButton(DefinedIcon.down.getIcon(BUTTON_SIZE));
      this.currentButton.setActionCommand(CURRENT);
      this.currentButton.addActionListener(this);
      this.currentButton.setToolTipText(Tooltips.CURRENT_PAGE);
      
      // construct the buttons that allow you to edit pages
      this.newPageButton = 
         new JButton(DefinedIcon.page.getIcon(BUTTON_SIZE));
      this.newPageButton.setActionCommand(NEW_PAGE);
      this.newPageButton.addActionListener(this);
      this.newPageButton.setToolTipText(Tooltips.NEW_PAGE);
      
      this.refreshButton = 
         new JButton(DefinedIcon.refresh.getIcon(BUTTON_SIZE));
      this.refreshButton.setActionCommand(REFRESH);
      this.refreshButton.addActionListener(this);
      this.refreshButton.setToolTipText(Tooltips.REFRESH);
      
      this.canvas.getBinder().addBinderListener(this);
      adjustNextPrevButtons();
   }
   
   public void appendTo(JToolBar toolbar)
   {
      if (toolbar == null)
         throw new NullPointerException();
      
      toolbar.add(this.zoomOutButton);
      toolbar.add(this.zoomNormalButton);
      toolbar.add(this.zoomInButton);
      toolbar.addSeparator();
      
      toolbar.add(this.prevButton);
      toolbar.add(this.currentButton);
      toolbar.add(this.nextButton);
      toolbar.addSeparator();
      
      toolbar.add(this.newPageButton);
      toolbar.addSeparator();
      
      toolbar.add(this.refreshButton);
   }
   
   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      
      if (cmmd.equals(NEW_PAGE))
      {
         Binder binder = this.canvas.getBinder();
         binder.addNewPageAfterCurrent();
         // from a usability perspective moving the current 
         // new page is not needed
         //binder.showCurrent();
         
         adjustNextPrevButtons();
      }
      else if (cmmd.equals(ZOOM_OUT))
         this.canvas.zoomBy(ZOOM_OUT_FACTOR);
      else if (cmmd.equals(ZOOM_NORMAL))
         this.canvas.zoomTo(1);
      else if (cmmd.equals(ZOOM_IN))
         this.canvas.zoomBy(ZOOM_IN_FACTOR);
      else if (cmmd.equals(NEXT))
      {
         Binder binder = this.canvas.getBinder();
         binder.flipForward();
         binder.showCurrent();
      }
      else if (cmmd.equals(PREVIOUS))
      {
         Binder binder = this.canvas.getBinder();
         binder.flipBack();
         binder.showCurrent();
      }
      else if (cmmd.equals(CURRENT))
         this.canvas.getBinder().showCurrent();
      
      // if cmmd.equals(REFRESH) nothing needs to be done except 
      // repainting which is done by the next line
      this.canvas.doRepaint();
   }
   
   private void adjustNextPrevButtons()
   {
      Binder binder = this.canvas.getBinder();
      this.prevButton.setEnabled(binder.canFlipBack());
      this.nextButton.setEnabled(binder.canFlipForward());
   }

   public void currentPageChanged(Binder source)
   {
      adjustNextPrevButtons();
   }

   public void pageAdded(Binder source, Page page)
   {
      adjustNextPrevButtons();
   }

   public void pageRemoved(Binder source, Page page)
   {
      adjustNextPrevButtons();
   }
}
