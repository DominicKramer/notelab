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
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;
import noteLab.util.InfoCenter.OSType;

public class EnvironmentTile extends SequenceTile
{
   private ExtractTile extractTile;
   
   private JLabel homeDirLabel;
   private JLabel execScriptLabel;
   
   private ImageIcon checkIcon;
   
   public EnvironmentTile(ExtractTile prevTile)
   {
      super(prevTile, true, true);
      
      this.extractTile = prevTile;
      
      int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
      ImageIcon blankIcon = DefinedIcon.getEmptyIcon(size);
      this.checkIcon = DefinedIcon.ok.getIcon(size);
      
      this.homeDirLabel = new JLabel("Building "+InfoCenter.getAppName()+
                                     "'s home directory.");
      this.homeDirLabel.setIcon(blankIcon);
      this.homeDirLabel.setForeground(Color.GRAY);
      
      this.execScriptLabel = new JLabel("Making the startup scripts executable.");
      this.execScriptLabel.setIcon(blankIcon);
      this.execScriptLabel.setForeground(Color.GRAY);
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("Finalizing the installation"));
      mainPanel.add(new JLabel("  "));
      mainPanel.add(this.homeDirLabel);
      mainPanel.add(new JLabel("  "));
      mainPanel.add(this.execScriptLabel);
      mainPanel.add(new JLabel("  "));
      mainPanel.add(new JLabel("  "));
      mainPanel.add(new JLabel("  "));
      mainPanel.add(new JLabel("  "));
      
      setLayout(new FlowLayout(FlowLayout.CENTER));
      add(mainPanel);
      
      new ScriptGenThread().start();
   }
   
   public File getInstallDirectory()
   {
      return this.extractTile.getInstallDirectory();
   }
   
   @Override
   public SequenceTile getNextTile()
   {
     SequenceTile next = super.getNextTile();
     if (next != null)
        return next;
     
     next = new FinishedTile(this);
     super.setNextTile(next);
     return next;
   }
   
   @Override
   public void sequenceCancelled()
   {
      
   }
   
   @Override
   public void sequenceCompleted()
   {
   }
   
   private boolean builHomeDir()
   {
      String errorMessage = InfoCenter.buildAppHome();
      if (errorMessage == null)
      {
         this.homeDirLabel.setForeground(Color.BLACK);
         this.homeDirLabel.setIcon(this.checkIcon);
         return true;
      }
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon errorIcon = DefinedIcon.dialog_error.getIcon(size);
      String message = InfoCenter.getAppName()+"'s home directory \""+
                       InfoCenter.getAppHome().getAbsolutePath()+
                       "\" could not be constructed.";
      
      JOptionPane.showMessageDialog(new JFrame(), 
                                    message, 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE, 
                                    errorIcon);
      
      notifyTileProceedChanged(ProceedType.failed);
      return false;
   }
   
   private boolean makeScriptsExecutable()
   {
      OSType os = InfoCenter.getOperatingSystem();
      if (os.equals(OSType.Unix))
      {
         File installDir = this.extractTile.getInstallDirectory();
         
         String runName = "notelab";
         String uninstallName = "uninstall";
         String ext = InfoCenter.getScriptExtension();
         
         File runFile = new File(installDir, runName+ext);
         File uninstallFile = new File(installDir, uninstallName+ext);
         
         boolean success =  makeFileExecutable(runFile) && 
                            makeFileExecutable(uninstallFile);
         if (!success)
            return false;
      }
      
      this.execScriptLabel.setForeground(Color.BLACK);
      this.execScriptLabel.setIcon(this.checkIcon);
      return true;
   }
   
   private boolean makeFileExecutable(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      try
      {
         Runtime.getRuntime().exec(new String[]
                                       {
                                          "chmod", 
                                          "+x", 
                                          file.getAbsolutePath()
                                       });
         
         return true;
      }
      catch (IOException e)
      {
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon errorIcon = DefinedIcon.dialog_error.getIcon(size);
         String message = "The startup script "+file.getAbsolutePath()+
                          " could not be made executable.  The error returned was "+
                          e.getMessage();
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       message, 
                                       "Error", 
                                       JOptionPane.ERROR_MESSAGE, 
                                       errorIcon);
         
         notifyTileProceedChanged(ProceedType.failed);
         return false;
      }
   }
   
   private class ScriptGenThread extends Thread
   {
      public void run()
      {
         if (builHomeDir())
            if (makeScriptsExecutable())
               notifyTileProceedChanged(ProceedType.can_proceed);
      }
   }
}
