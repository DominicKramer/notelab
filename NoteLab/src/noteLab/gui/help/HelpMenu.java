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

package noteLab.gui.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import noteLab.gui.DefinedIcon;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.Menued;
import noteLab.gui.menu.PathMenuItem;

public class HelpMenu implements Menued, ActionListener
{
   private PathMenuItem aboutItem;
   private AboutFrame aboutFrame;
   
   private Vector<PathMenuItem> itemVec;
   
   public HelpMenu()
   {
      JMenuItem aboutItem = 
            new JMenuItem("About", DefinedIcon.about.getIcon(16));
      aboutItem.addActionListener(this);
      
      this.aboutItem = new PathMenuItem(aboutItem, 
                                        MenuConstants.HELP_MENU_PATH);
      this.aboutFrame = new AboutFrame();
      
      this.itemVec = new Vector<PathMenuItem>();
      this.itemVec.add(this.aboutItem);
   }
   
   public List<PathMenuItem> getPathMenuItems()
   {
      return this.itemVec;
   }

   public void actionPerformed(ActionEvent e)
   {
      this.aboutFrame.updateMemoryDisplayed();
      this.aboutFrame.setVisible(true);
   }
}
