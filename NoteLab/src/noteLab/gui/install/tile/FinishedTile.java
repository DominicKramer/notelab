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

package noteLab.gui.install.tile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;
import noteLab.util.InfoCenter.OSType;

public class FinishedTile extends SequenceTile
{
   private JCheckBox launchBox;
   private File execFile;
   
   public FinishedTile(EnvironmentTile prevTile)
   {
      super(prevTile, true, false);
      
      if (prevTile == null)
         throw new NullPointerException();
      
      File installDir = prevTile.getInstallDirectory();
      String appName = InfoCenter.getAppName();
      
      this.execFile = new File(installDir, "notelab"+InfoCenter.getScriptExtension());
      
      StringBuffer buffer = new StringBuffer();
      buffer.append("<center><b><font color=\"blue\">Success</font></b></center>");
      buffer.append("<br><br>");
      buffer.append(appName);
      buffer.append(" was successfully installed to <br><br><center><font color=\"blue\">");
      buffer.append(installDir.getAbsolutePath());
      buffer.append("</font></center><br> ");
      buffer.append(appName);
      buffer.append(" can be started by running <br><br><center><font color=\"blue\">");
      buffer.append(this.execFile.getAbsolutePath());
      buffer.append("</font></center><br>");
      buffer.append("and can be uninstalled by running <br><br><center><font color=\"blue\">");
      buffer.append(new File(installDir, 
                             "uninstall"+InfoCenter.getScriptExtension()).
                                getAbsolutePath());
      buffer.append("</font></center>");
      
      Color bgColor = getBackground();
      
      JEditorPane htmlPane = new JEditorPane();
      htmlPane.setPreferredSize(new Dimension(500, 300));
      htmlPane.setEditorKit(new HTMLEditorKit());
      htmlPane.setText(buffer.toString());
      htmlPane.setBackground(bgColor);
      
      JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      welcomePanel.add(new JScrollPane(htmlPane));
      
      this.launchBox = new JCheckBox("Start "+appName);
      this.launchBox.setSelected(true);
      
      JPanel launchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      launchPanel.add(this.launchBox);
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(new JLabel("    "));
      add(new JLabel("    "));
      add(welcomePanel);
      add(launchPanel);
      
      notifyTileProceedChanged(ProceedType.can_proceed);
   }
   
   @Override
   public void sequenceCancelled()
   {
   }

   @Override
   public void sequenceCompleted()
   {
      try
      {
         if (this.launchBox.isSelected())
         {
            String filename = this.execFile.getAbsolutePath();
            OSType os = InfoCenter.getOperatingSystem();
            
            // If its a UNIX system replace all spaces in the filename with 
            // the sequence "\ "
            if (os.equals(OSType.Unix))
               filename = filename.replace(" ", "\\ ");
            
            Runtime.getRuntime().exec(filename);
         }
      }
      catch (IOException e)
      {
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon errorIcon = DefinedIcon.dialog_error.getIcon(size);
         String message = InfoCenter.getAppName()+" could not be started.  The " +
                          "error returned was "+e.getMessage();
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       message, 
                                       "Error", 
                                       JOptionPane.ERROR_MESSAGE, 
                                       errorIcon);
         
         System.exit(1);
      }
      
      // Exit the virtual machine because the installation is complete
      System.exit(0);
   }
}
