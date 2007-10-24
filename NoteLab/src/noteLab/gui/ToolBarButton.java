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

import javax.swing.JToggleButton;

import noteLab.gui.menu.Menued;
import noteLab.model.canvas.SubCanvas;

public abstract class ToolBarButton 
                        extends JToggleButton 
                                   implements Menued, GuiSettingsConstants
{
   private SlidingPanel slidingPanel;
   
   public ToolBarButton(DefinedIcon icon)
   {
      super(icon.getIcon(BUTTON_SIZE));
      
      this.slidingPanel = new SlidingPanel();
      
      /*
      this.toolBar = new JToolBar(JToolBar.HORIZONTAL);
      this.toolBar.setMargin(new Insets(0, 0, 0, 0));
      this.toolBar.setFloatable(false);
      FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
      layout.setVgap(0);
      layout.setHgap(2);
      this.toolBar.setLayout(layout);
      this.toolBar.setBorderPainted(false);
      */
   }
   
   public SlidingPanel getSlidingPanel()
   {
      return this.slidingPanel;
   }
   
   public abstract SubCanvas getCanvas();
   
   public abstract void start();
   public abstract void finish();
}
