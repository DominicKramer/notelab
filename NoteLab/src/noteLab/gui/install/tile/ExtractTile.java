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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.install.Installer;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;
import noteLab.util.percent.PercentChangedListener;

public class ExtractTile extends SequenceTile implements PercentChangedListener, ActionListener
{
   private ExtractorThread extractThread;
   private JButton startButton;
   private JProgressBar progressBar;
   private JLabel statusLabel;
   private File installDir;
   
   public ExtractTile(InstallDirTile prevTile)
   {
      super(prevTile, true, true);
      
      String lastDirName = InfoCenter.getAppName()+"_"+InfoCenter.getAppVersion();
      this.installDir = new File(prevTile.getInstallDirectory(), lastDirName);
      
      this.extractThread = new ExtractorThread(this.installDir);
      
      int size = GuiSettingsConstants.MEDIUM_BUTTON_SIZE;
      this.startButton = new JButton("Install", DefinedIcon.about.getIcon(size));
      this.startButton.addActionListener(this);
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(this.startButton);
      
      this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
      this.progressBar.setValue(0);
      this.progressBar.setStringPainted(true);
      
      JPanel progressPanel = new JPanel();
      progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("  "));
      progressPanel.add(new JLabel("Installing to '"+this.installDir+"'"));
      progressPanel.add(this.progressBar);
      this.statusLabel = new JLabel("    ");
      progressPanel.add(this.statusLabel);
      
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
      centerPanel.add(progressPanel);
      centerPanel.add(buttonPanel);
      
      setLayout(new BorderLayout());
      add(new JLabel("  "), BorderLayout.EAST);
      add(centerPanel, BorderLayout.CENTER);
      add(new JLabel("  "), BorderLayout.WEST);
   }
   
   public File getInstallDirectory()
   {
      return this.installDir;
   }
   
   @Override
   public SequenceTile getNextTile()
   {
     SequenceTile next = super.getNextTile();
     if (next != null)
        return next;
     
     next = new EnvironmentTile(this);
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

   public void percentChanged(int percent, String message)
   {
      this.progressBar.setValue(percent);
      this.statusLabel.setText(message);
   }

   public void actionPerformed(ActionEvent e)
   {
      this.extractThread.start();
      this.startButton.setEnabled(false);
   }
   
   private class ExtractorThread extends Thread
   {
      private Installer extractor;
      
      private ExtractorThread(File installDir)
      {
         if (installDir == null)
            throw new NullPointerException();
         
         try
         {
            this.extractor = new Installer(installDir, ExtractTile.this);
         }
         catch (IOException e)
         {
            showExceptionDialog(e);
         }
      }
      
      public void run()
      {
         try
         {
            this.extractor.install();
            notifyTileProceedChanged(ProceedType.can_proceed);
         }
         catch (IOException e)
         {
            showExceptionDialog(e);
         }
      }
      
      private void showExceptionDialog(Exception e)
      {
         e.printStackTrace();
         
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon icon = DefinedIcon.dialog_error.getIcon(size);
         
         String message = "An error has occured during the extraction.  " +
                          "The error returned was:  "+e.getMessage();
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       message, 
                                       "Error", 
                                       JOptionPane.ERROR_MESSAGE, 
                                       icon);
         
         notifyTileProceedChanged(ProceedType.failed);
      }
   }
}
