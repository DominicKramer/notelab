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

package noteLab.gui.install;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import noteLab.gui.DefinedIcon;
import noteLab.gui.install.tile.WelcomeInstallTile;
import noteLab.gui.sequence.SequenceFrame;
import noteLab.util.InfoCenter;

public class InstallFrame extends SequenceFrame
{
   public InstallFrame()
   {
      super(new WelcomeInstallTile(), 
            
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
               InfoCenter.getAppVersion()+" Installer");
      
      DefinedIcon logo = DefinedIcon.logo;
      ImageIcon logoIcon = logo.getIcon(DefinedIcon.ORIGINAL_SIZE);
      
      JPanel westPanel = getWestPanel();
      westPanel.setLayout(new GridLayout(1, 1));
      westPanel.add(new JLabel(logoIcon));
   }
   
   public static void main(String[] args)
   {
      try
      {
         // Search for the look and feel with the name Nimbus instead of 
         // setting the look and feel by instantiating an object representing 
         // the Nimbus look and feel.  This is done because the full 
         // classname of the Nimbus look and feel might change from one 
         // version of Java to the next.  Hence if this occurs and an object 
         // of the Nimbus look and feel is used below, this class won't 
         // compile.  However, the code below avoids this problem.
         LookAndFeelInfo[] lafArr = UIManager.getInstalledLookAndFeels();
         for (LookAndFeelInfo info : lafArr)
         {
            if (info.getName().toLowerCase().contains("nimbus"))
            {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      }
      catch (Exception e)
      {
         System.err.println("The Nimbus look and feel could not be loaded.  " +
         		             "The default look and feel will be used instead.");
      }
      
      InstallFrame frame = new InstallFrame();
      frame.pack();
      frame.setVisible(true);
   }
}
