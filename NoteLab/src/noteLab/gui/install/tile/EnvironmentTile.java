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
   private JLabel storeInstallLabel;
   private JLabel execScriptLabel;
   
   private ImageIcon checkIcon;
   
   public EnvironmentTile(ExtractTile prevTile)
   {
      super(prevTile, true, true);
      
      this.extractTile = prevTile;
      
      int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
      ImageIcon blankIcon = DefinedIcon.empty.getIcon(size);
      this.checkIcon = DefinedIcon.ok.getIcon(size);
      
      this.homeDirLabel = new JLabel("Building "+InfoCenter.getAppName()+
                                     "'s home directory.");
      this.homeDirLabel.setIcon(blankIcon);
      this.homeDirLabel.setForeground(Color.GRAY);
      
      this.storeInstallLabel = new JLabel("Storing the install environment.");
      this.storeInstallLabel.setIcon(blankIcon);
      this.storeInstallLabel.setForeground(Color.GRAY);
      
      this.execScriptLabel = new JLabel("Making the startup scripts executable.");
      this.execScriptLabel.setIcon(blankIcon);
      this.execScriptLabel.setForeground(Color.GRAY);
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("     "));
      mainPanel.add(new JLabel("Finializing the installation"));
      mainPanel.add(new JLabel("  "));
      mainPanel.add(this.homeDirLabel);
      mainPanel.add(new JLabel("  "));
      mainPanel.add(this.storeInstallLabel);
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
   
   private boolean storeInstallEnvironment()
   {
      try
      {
         File installEnvFile = InfoCenter.getInstallDirEnvFile();
         PrintWriter writer = new PrintWriter(new FileOutputStream(installEnvFile));
         
         OSType os = InfoCenter.getOperatingSystem();
         if (os == OSType.Unix)
         {
            writer.println("#!/bin/sh");
            writer.println();
            writer.print("export ");
         }
         else
         {
            writer.println("@echo off");
            writer.println();
            writer.print("set ");
         }
         
         writer.print(InfoCenter.getInstallDirVarName());
         writer.print("=\"");
         writer.print(this.extractTile.getInstallDirectory());
         writer.println("\"");
         writer.close();
         
         this.storeInstallLabel.setForeground(Color.BLACK);
         this.storeInstallLabel.setIcon(this.checkIcon);
         
         return true;
      }
      catch (IOException e)
      {
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon errorIcon = DefinedIcon.dialog_error.getIcon(size);
         String message = "The installation directory could not be stored.  The message " +
                          "returned was "+e.getMessage();
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       message, 
                                       "Error", 
                                       JOptionPane.ERROR_MESSAGE, 
                                       errorIcon);
         
         notifyTileProceedChanged(ProceedType.failed);
         return false;
      }
   }
   
   private boolean makeScriptsExecutable()
   {
      OSType os = InfoCenter.getOperatingSystem();
      if (os == OSType.Unix)
      {
         File installDir = this.extractTile.getInstallDirectory();
         
         File shFile = new File(installDir, "notelab.sh");
         File uninstallFile = new File(installDir, "uninstall.sh");
         
         try
         {
            Runtime.getRuntime().exec(new String[]
                                          {
                                             "chmod", 
                                             "+x", 
                                             shFile.getAbsolutePath()
                                          });
            
            Runtime.getRuntime().exec(new String[]
                                          {
                                             "chmod", 
                                             "+x", 
                                             uninstallFile.getAbsolutePath()
                                          });
         }
         catch (IOException e)
         {
            int size = GuiSettingsConstants.BUTTON_SIZE;
            ImageIcon errorIcon = DefinedIcon.dialog_error.getIcon(size);
            String message = "The startup script "+shFile.getAbsolutePath()+
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
      
      this.execScriptLabel.setForeground(Color.BLACK);
      this.execScriptLabel.setIcon(this.checkIcon);
      return true;
   }
   
   private class ScriptGenThread extends Thread
   {
      public void run()
      {
         if (builHomeDir())
            if (storeInstallEnvironment())
               if (makeScriptsExecutable())
                  notifyTileProceedChanged(ProceedType.can_proceed);
      }
   }
}
