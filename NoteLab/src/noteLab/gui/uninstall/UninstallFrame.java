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

package noteLab.gui.uninstall;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import noteLab.gui.DefinedIcon;
import noteLab.gui.sequence.SequenceFrame;
import noteLab.util.InfoCenter;

public class UninstallFrame extends SequenceFrame
{
   public UninstallFrame()
   {
      super(new WelcomeUninstallTile(), 
            
            new Runnable()
            {
               public void run()
               {
                  System.exit(0);
               }
            }, 
            
            new Runnable()
            {
               public void run()
               {
               }
            });
      
      setTitle("The "+InfoCenter.getAppName()+" Version "+
               InfoCenter.getAppVersion()+" Uninstaller");
      
      DefinedIcon logo = DefinedIcon.logo;
      ImageIcon logoIcon = logo.getIcon(DefinedIcon.ORIGINAL_SIZE);
      
      JPanel westPanel = getWestPanel();
      westPanel.setLayout(new GridLayout(1, 1));
      westPanel.add(new JLabel(logoIcon));
   }
   
   public static void main(String[] args)
   {
      UninstallFrame frame = new UninstallFrame();
      frame.pack();
      frame.setVisible(true);
   }
}
