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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.file.ViewFilePanel;
import noteLab.util.InfoCenter;

public class AboutFrame 
                extends JFrame 
                           implements ActionListener, HelpConstants
{
   private static final String CLOSE_TEXT = "Close";
   private static final String VIEW_LICENSE_TEXT = "View License";
   private static final String VIEW_CREDITS_TEXT = "View Credits";
   
   private static final String MAX_MEMORY_LABEL = "Maximum available memory:  ";
   private static final String FREE_MEMORY_LABEL = "Total free memory:  ";
   private static final String USED_MEMORY_LABEL = "Total used memory:  ";
   private static final String MB_LABEL = " Mb";
   
   private JLabel maxLabel;
   private JLabel freeLabel;
   private JLabel usedLabel;
   
   private ViewTextFrame textFrame;
   
   public AboutFrame()
   {
      super("About");
      
      this.textFrame = new ViewTextFrame();
      this.textFrame.setVisible(false);
      
      setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      setResizable(false);
      setAlwaysOnTop(true);
      
      JPanel infoPanel = new JPanel();
      infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
      
      JLabel versionLabel = new JLabel("Version:  "+InfoCenter.getAppVersion());
        Font font = versionLabel.getFont().deriveFont(Font.PLAIN);
        versionLabel.setFont(font);
      infoPanel.add(versionLabel);
      
      JLabel authorLabel = new JLabel("Author:  "+InfoCenter.getAuthor()+
                                      " ("+InfoCenter.getAuthorEmail()+")");
        authorLabel.setFont(font);
      infoPanel.add(authorLabel);
      
      String blankStr = "           ";
      
      this.maxLabel = new JLabel(blankStr);
      this.maxLabel.setFont(font);
      infoPanel.add(this.maxLabel);
      
      this.usedLabel = new JLabel(blankStr);
      this.usedLabel.setFont(font);
      infoPanel.add(this.usedLabel);
      
      this.freeLabel = new JLabel(blankStr);
      this.freeLabel.setFont(font);
      infoPanel.add(this.freeLabel);
      
      ImageIcon logoImage = DefinedIcon.logo.
                               getIcon(DefinedIcon.ORIGINAL_SIZE);
      
      ImageIcon paperIcon = DefinedIcon.
                               college_rule.
                                  getIcon(GuiSettingsConstants.
                                             SMALL_BUTTON_SIZE);
      ImageIcon closeIcon = DefinedIcon.
                               close.
                                  getIcon(GuiSettingsConstants.
                                             SMALL_BUTTON_SIZE);
      
      JButton licenseButton = new JButton(VIEW_LICENSE_TEXT, paperIcon);
      licenseButton.setActionCommand(VIEW_LICENSE_TEXT);
      licenseButton.addActionListener(this);
      
      JButton readmeButton = new JButton(VIEW_CREDITS_TEXT, paperIcon);
      readmeButton.setActionCommand(VIEW_CREDITS_TEXT);
      readmeButton.addActionListener(this);
      
      JButton closeButton = new JButton(CLOSE_TEXT, closeIcon);
      closeButton.setActionCommand(CLOSE_TEXT);
      closeButton.addActionListener(this);
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(licenseButton);
      buttonPanel.add(readmeButton);
      buttonPanel.add(closeButton);
      
      JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      topPanel.add(new JLabel(logoImage));
      
      JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      bottomPanel.add(infoPanel);
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(topPanel, BorderLayout.CENTER);
      mainPanel.add(bottomPanel, BorderLayout.SOUTH);
      
      setLayout(new BorderLayout());
      add(mainPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
      
      pack();
   }
   
   public void updateMemoryDisplayed()
   {
      DecimalFormat formatter = new DecimalFormat(".##");
      String maxMemStr = formatter.format(InfoCenter.getMaxMemoryMb());
      String usedMemstr = formatter.format(InfoCenter.getTotalUsedMemoryMb());
      String freeMemStr = formatter.format(InfoCenter.getTotalFreeMemoryMb());
      
      this.maxLabel.setText(MAX_MEMORY_LABEL+maxMemStr+MB_LABEL);
      this.usedLabel.setText(USED_MEMORY_LABEL+usedMemstr+MB_LABEL);
      this.freeLabel.setText(FREE_MEMORY_LABEL+freeMemStr+MB_LABEL);
   }
   
   public void actionPerformed(ActionEvent event)
   {
      String cmmd = event.getActionCommand();
      if (cmmd == null)
         return;
      
      if (cmmd.equals(VIEW_CREDITS_TEXT))
      {
         this.textFrame.setVisible(true);
         this.textFrame.
                 getTextPanel().
                    setText(CREDITS_URL, 
                            INFO_PREFIX+"/"+CREDITS_URL, 
                            CREDITS_NAME);
      }
      else if (cmmd.equals(VIEW_LICENSE_TEXT))
      {
         this.textFrame.setVisible(true);
         this.textFrame.
                 getTextPanel().
                    setText(LICENSE_URL, 
                            INFO_PREFIX+"/"+LICENSE_URL, 
                            LICENSE_NAME);
      }
      else if (cmmd.equals(CLOSE_TEXT))
         setVisible(false);
   }
   
   private class ViewTextFrame extends JFrame implements ActionListener
   {
      private ViewFilePanel textPanel;
      
      public ViewTextFrame()
      {
         this.textPanel = new ViewFilePanel();
         
         ImageIcon closeIcon = DefinedIcon.
                                  close.
                                     getIcon(GuiSettingsConstants.
                                                SMALL_BUTTON_SIZE);
         JButton closeButton = new JButton("Close", closeIcon);
         closeButton.addActionListener(this);
         
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.add(closeButton);
         
         setLayout(new BorderLayout());
         add(this.textPanel);
         add(buttonPanel, BorderLayout.SOUTH);
         
         pack();
         setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      }
      
      public ViewFilePanel getTextPanel()
      {
         return this.textPanel;
      }
      
      public void actionPerformed(ActionEvent e)
      {
         setVisible(false);
      }
   }
}
