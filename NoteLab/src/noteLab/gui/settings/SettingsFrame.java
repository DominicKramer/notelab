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

package noteLab.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.fullscreen.FullScreenManager;
import noteLab.gui.settings.panel.OtherSettingsPanel;
import noteLab.gui.settings.panel.PageSettingsPanel;
import noteLab.gui.settings.panel.TriPenSettingsPanel;
import noteLab.util.InfoCenter;
import noteLab.util.InfoCenter.OSType;

public class SettingsFrame extends JFrame
{
   private TriPenSettingsPanel tripPenPanel;
   private PageSettingsPanel pagePanel;
   private OtherSettingsPanel otherPanel;
   
   public SettingsFrame()
   {
      super("Settings");
      
      FullScreenManager.getSharedInstance().revokeFullScreenMode();
      
      this.tripPenPanel = new TriPenSettingsPanel();
      this.pagePanel = new PageSettingsPanel();
      this.otherPanel = new OtherSettingsPanel();
      
      JPanel penPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      penPanel.add(this.tripPenPanel);
      
      JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      pagePanel.add(this.pagePanel);
      
      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      tabbedPane.addTab("Pen", 
                        DefinedIcon.pencil.getIcon(16), 
                        new JScrollPane(penPanel, 
                                        JScrollPane.
                                           VERTICAL_SCROLLBAR_AS_NEEDED, 
                                        JScrollPane.
                                           HORIZONTAL_SCROLLBAR_AS_NEEDED));
      tabbedPane.addTab("Page", 
                        DefinedIcon.copy_page.getIcon(16), 
                        new JScrollPane(pagePanel, 
                                        JScrollPane.
                                           VERTICAL_SCROLLBAR_AS_NEEDED, 
                                        JScrollPane.
                                           HORIZONTAL_SCROLLBAR_AS_NEEDED));
      tabbedPane.addTab("Other", 
                        DefinedIcon.preferences.getIcon(16), 
                        new JScrollPane(this.otherPanel, 
                                        JScrollPane.
                                           VERTICAL_SCROLLBAR_AS_NEEDED, 
                                        JScrollPane.
                                           HORIZONTAL_SCROLLBAR_AS_NEEDED));
      
      setLayout(new BorderLayout());
      add(tabbedPane, BorderLayout.CENTER);
      add(new ButtonPanel(), BorderLayout.SOUTH);
      
      pack();
      setVisible(true);
      
      Dimension windowDim = getSize();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Point locOnScreen = getLocationOnScreen();
      
      int useableWidth = (int)(screenSize.getWidth()-locOnScreen.getX());
      int useableHeight = (int)(screenSize.getHeight()-locOnScreen.getY());
      
      float width = (float)windowDim.getWidth();
      if (width >= 0.9f*useableWidth)
         width = 0.75f*useableWidth;
      
      float height = (float)windowDim.getHeight();
      if (height >= 0.9f*useableHeight)
         height = 0.75f*useableHeight;
      
      setSize((int)width, (int)height);
   }
   
   private class ButtonPanel extends JPanel implements ActionListener
   {
      private final String OK_NAME = "ok";
      private final String APPLY_NAME = "apply";
      
      private final DefinedIcon OK_ICON = DefinedIcon.ok;
      private final DefinedIcon APPLY_ICON = DefinedIcon.ok;
      private final DefinedIcon CANCEL_ICON = DefinedIcon.close;
      private final DefinedIcon REVERT_TO_SAVED = DefinedIcon.revert_to_saved;
      private final DefinedIcon RESTORE_DEFAULTS = DefinedIcon.preferences;
      
      public ButtonPanel()
      {
         int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
         
         JButton okButton = new JButton("Ok", 
                                        OK_ICON.getIcon(size));
         okButton.setActionCommand(OK_NAME);
         okButton.addActionListener(this);
         
         JButton applyButton = new JButton("Apply", 
                                           APPLY_ICON.getIcon(size));
         applyButton.setActionCommand(APPLY_NAME);
         applyButton.addActionListener(this);
         
         JButton cancelButton = new JButton("Cancel", 
                                            CANCEL_ICON.getIcon(size));
         cancelButton.setActionCommand(CANCEL_ICON.name());
         cancelButton.addActionListener(this);
         
         JButton revertButton = new JButton("Restore Saved", 
                                            REVERT_TO_SAVED.getIcon(size));
         revertButton.setActionCommand(REVERT_TO_SAVED.name());
         revertButton.addActionListener(this);
         
         JButton defaultsButton = new JButton("Restore Defaults", 
                                              RESTORE_DEFAULTS.getIcon(size));
         defaultsButton.setActionCommand(RESTORE_DEFAULTS.name());
         defaultsButton.addActionListener(this);
         
         setLayout(new FlowLayout(FlowLayout.RIGHT));
         add(defaultsButton);
         add(revertButton);
         add(cancelButton);
         add(applyButton);
         add(okButton);
      }
      
      public void actionPerformed(ActionEvent e)
      {
         String cmmd = e.getActionCommand();
         
         if (cmmd.equals(OK_NAME))
         {
            tripPenPanel.apply();
            pagePanel.apply();
            otherPanel.apply();
            
            try
            {
               PrintWriter writer = 
                              new PrintWriter(
                                    SettingsInfoCenter.getSettingsFile());
               
               OSType os = InfoCenter.getOperatingSystem();
               
               if (os.equals(OSType.Unix))
                  writer.println("#!/bin/sh");
               else
                  writer.println("@echo off");
               
               StringBuffer buffer = new StringBuffer();
               tripPenPanel.encode(buffer);
               pagePanel.encode(buffer);
               otherPanel.encode(buffer);
               
               String exportStr = "export ";
               String quoteStr = "\"";
               if (os.equals(OSType.Windows))
               {
                  exportStr = "set ";
                  quoteStr = "";
               }
               
               writer.println();
               writer.print(exportStr);
               writer.print(SettingsInfoCenter.getAppArgsVarName());
               writer.print("=");
               writer.print(quoteStr);
               writer.print(buffer.toString());
               writer.println(quoteStr);
               
               String saveStr = tripPenPanel.save();
               if (saveStr != null && saveStr.trim().length() > 0)
                  writer.println(saveStr);
               
               saveStr = pagePanel.save();
               if (saveStr != null && saveStr.trim().length() > 0)
                  writer.println(saveStr);
               
               saveStr = otherPanel.save();
               if (saveStr != null && saveStr.trim().length() > 0)
                  writer.println(saveStr);
               
               writer.close();
               
               dispose();
            }
            catch (IOException exception)
            {
               JOptionPane.
                  showMessageDialog(new JFrame(), 
                                    "An error has occured while saving " +
                                    "the settings.  The message returned " +
                                    "was:  "+exception.getMessage(), 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE, 
                                    DefinedIcon.
                                       dialog_error.
                                          getIcon(GuiSettingsConstants.
                                                     BUTTON_SIZE));
            }
         }
         else if (cmmd.equals(APPLY_NAME))
         {
            tripPenPanel.apply();
            pagePanel.apply();
            otherPanel.apply();
         }
         else if (cmmd.equals(CANCEL_ICON.name()))
         {
            tripPenPanel.revertToSaved();
            pagePanel.revertToSaved();
            otherPanel.revertToSaved();
            
            dispose();
         }
         else if (cmmd.equals(REVERT_TO_SAVED.name()))
         {
            tripPenPanel.revertToSaved();
            pagePanel.revertToSaved();
            otherPanel.revertToSaved();
         }
         else if (cmmd.equals(RESTORE_DEFAULTS.name()))
         {
            tripPenPanel.restoreDefaults();
            pagePanel.restoreDefaults();
            otherPanel.restoreDefaults();
         }
      }
   }
}
