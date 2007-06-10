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

package noteLab.gui.settings.panel;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;

public class TriPenSettingsPanel 
                extends JPanel 
                           implements 
                              SettingsStateCapable, 
                              SettingsSaveCapable
{
   private PenSettingsPanel pen1Panel;
   private PenSettingsPanel pen2Panel;
   private PenSettingsPanel pen3Panel;
   private StrokeSmoothingPanel smoothPanel;
   
   public TriPenSettingsPanel()
   {
      this.pen1Panel = new PenSettingsPanel(1);
      this.pen2Panel = new PenSettingsPanel(2);
      this.pen3Panel = new PenSettingsPanel(3);
      this.smoothPanel = new StrokeSmoothingPanel();
      
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.add(this.pen1Panel);
      innerPanel.add(this.pen2Panel);
      innerPanel.add(this.pen3Panel);
      innerPanel.add(this.smoothPanel);
      
      setLayout(new GridLayout());
      add(innerPanel);
   }
   
   public void restoreDefaults()
   {
      this.pen1Panel.restoreDefaults();
      this.pen2Panel.restoreDefaults();
      this.pen3Panel.restoreDefaults();
      this.smoothPanel.restoreDefaults();
   }

   public void revertToSaved()
   {
      this.pen1Panel.revertToSaved();
      this.pen2Panel.revertToSaved();
      this.pen3Panel.revertToSaved();
      this.smoothPanel.revertToSaved();
   }

   public void encode(StringBuffer buffer)
   {
      this.pen1Panel.encode(buffer);
      this.pen2Panel.encode(buffer);
      this.pen3Panel.encode(buffer);
      this.smoothPanel.encode(buffer);
   }

   public String save()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(this.pen1Panel.save());
      buffer.append(this.pen2Panel.save());
      buffer.append(this.pen3Panel.save());
      buffer.append(this.smoothPanel.save());
      return buffer.toString();
   }
   
   public void apply()
   {
      this.pen1Panel.apply();
      this.pen2Panel.apply();
      this.pen3Panel.apply();
      this.smoothPanel.apply();
   }
}
