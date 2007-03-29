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

package noteLab.gui.settings.panel.base;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.control.drop.DropDownButton;
import noteLab.gui.settings.state.SettingsStateCapable;

public abstract class PrimitiveSettingsPanel 
                         extends JPanel implements ActionListener, 
                                                   SettingsStateCapable
{
   private static final DefinedIcon REVERT_TO_SAVED_ICON = 
                                       DefinedIcon.revert_to_saved;
   private static final DefinedIcon RESTORE_DEFAULTS_ICON = 
                                       DefinedIcon.preferences;
   private static final DefinedIcon INFO_ICON = 
                                       DefinedIcon.dialog_info;
   
   private JPanel displayPanel;
   private String title;
   private String info;
   
   public PrimitiveSettingsPanel(String title, String info)
   {
      if (title == null || info == null)
         throw new NullPointerException();
      
      this.info = info;
      this.title = title;
      
      DefinedIcon icon = REVERT_TO_SAVED_ICON;
      JButton revertSavedButton = 
                 new JButton(icon.getIcon(GuiSettingsConstants.
                                             MEDIUM_BUTTON_SIZE));
      revertSavedButton.setActionCommand(icon.name());
      revertSavedButton.addActionListener(this);
      
      
      icon = RESTORE_DEFAULTS_ICON;
      JButton restoreDefaultsButton = 
                 new JButton(icon.getIcon(GuiSettingsConstants.
                                             MEDIUM_BUTTON_SIZE));
      restoreDefaultsButton.setActionCommand(icon.name());
      restoreDefaultsButton.addActionListener(this);
      
      
      icon = INFO_ICON;
      DropDownButton infoButton = new DropDownButton();
      infoButton.setDrawArrow(false);
      infoButton.setIcon(icon.getIcon(GuiSettingsConstants.
                                         MEDIUM_BUTTON_SIZE));
      JWindow popupWindow = infoButton.getPopupWindow();
      popupWindow.setLayout(new FlowLayout());
      popupWindow.add(new JLabel(getInfo()));
      
      
      JPanel buttonPanel = new JPanel(new FlowLayout());
      buttonPanel.add(infoButton);
      buttonPanel.add(revertSavedButton);
      buttonPanel.add(restoreDefaultsButton);
      
      this.displayPanel = new JPanel(new GridLayout());
      
      JPanel leftPanel = new JPanel(new BorderLayout());
         FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
           layout.setVgap((int)(GuiSettingsConstants.BUTTON_SIZE*0.5));
         JPanel labelPanel = new JPanel(layout);
           labelPanel.add(new JLabel(this.title));
      leftPanel.add(labelPanel, BorderLayout.WEST);
         JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
           flowPanel.add(this.displayPanel);
      leftPanel.add(flowPanel, BorderLayout.CENTER);
      
      JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(buttonPanel);
      
      setLayout(new BorderLayout());
      add(leftPanel, BorderLayout.CENTER);
      add(rightPanel, BorderLayout.EAST);
   }
   
   public JPanel getDisplayPanel()
   {
      return this.displayPanel;
   }
   
   public String getTitle()
   {
      return this.title;
   }
   
   public String getInfo()
   {
      return this.info;
   }
   
   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      
      if (cmmd.equals(RESTORE_DEFAULTS_ICON.name()))
         restoreDefaults();
      else if (cmmd.equals(REVERT_TO_SAVED_ICON.name()))
         revertToSaved();
   }
   
   public static void main(String[] args)
   {
      PrimitiveSettingsPanel panel = 
            new PrimitiveSettingsPanel("Demo", "This is some information")
      {
         public void restoreDefaults()
         {
         }

         public void revertToSaved()
         {
         }
         
         public void apply()
         {
         }
      };
      
      JFrame frame = new JFrame(PrimitiveSettingsPanel.class.getName());
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
   }
}
