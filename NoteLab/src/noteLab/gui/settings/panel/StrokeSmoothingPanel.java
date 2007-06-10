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

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import noteLab.gui.control.SliderControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.settings.constants.PenSettingsConstants;
import noteLab.gui.settings.panel.base.ManagedSettingsPanel;
import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;
import noteLab.util.arg.SmoothFactorArg;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsUtilities;

public class StrokeSmoothingPanel 
                extends JPanel 
                           implements SettingsStateCapable, 
                                      SettingsSaveCapable, 
                                      ValueChangeListener<Integer, SliderControl>
{
   private ManagedSettingsPanel smoothPanel;
   private SliderControl smoothControl;
   
   public StrokeSmoothingPanel()
   {
      int smoothFactor = SettingsUtilities.getSmoothFactor();
      boolean smoothingEnabled = (smoothFactor > 0);
      
      int min = 1;
      int max = 10;
      int space = 1;
      if (smoothFactor < min)
         smoothFactor = min;
      else if (smoothFactor > max)
         smoothFactor = max;
      
      this.smoothControl = new SliderControl("Smooth Strokes: ","Smoothing Factor:",
                                             min,     // min value
                                             max,     // max value
                                             smoothFactor, // current value
                                             space,   // spacing
                                             smoothingEnabled); // enable smoothing
      this.smoothControl.addValueChangeListener(this);
      
      this.smoothPanel = 
         new ManagedSettingsPanel("", 
                                  "Specifies if strokes should be smoothed as they are " +
                                  "drawn.  The higher the smoothing factor the more a " +
                                  "stroke will be smoothed.",  
                                  SettingsKeys.SMOOTH_FACTOR, 
                                  PenSettingsConstants.SMOOTH_FACTOR)
      {
         @Override
         public void updateDisplay(Object curVal)
         {
            if (curVal == null)
               throw new NullPointerException();
            
            if (curVal instanceof Integer)
            {
               int smoothFactor = (Integer)curVal;
               if (smoothFactor <= 0)
                  smoothControl.setSelected(false);
               else
               {
                  smoothControl.setSelected(true);
                  smoothControl.setControlValue(smoothFactor);
               }
            }
         }
      };
      this.smoothPanel.getDisplayPanel().add(this.smoothControl);
      
      setBorder(new TitledBorder("Stroke Settings"));
      setLayout(new GridLayout(1,1));
      add(this.smoothPanel);
   }
   
   private void sync()
   {
      Integer smoothFactor = (Integer)this.smoothPanel.getCurrentValue();
      if (smoothFactor != null)
         this.smoothControl.setControlValue(smoothFactor);
   }

   public void restoreDefaults()
   {
      this.smoothPanel.restoreDefaults();
      
      sync();
   }

   public void revertToSaved()
   {
      this.smoothPanel.revertToSaved();
      
      sync();
   }
   
   public void apply()
   {
      this.smoothPanel.apply();
   }

   public void encode(StringBuffer buffer)
   {
      if (buffer == null)
         throw new NullPointerException();
      
      int length = buffer.length();
      if (length > 0 && buffer.charAt(length-1) != ' ')
         buffer.append(' ');
      
      Integer smoothFactor = 0;
      if (this.smoothControl.isSelected())
         smoothFactor = this.smoothControl.getControlValue();
      
      String smoothEncodeStr = 
                new SmoothFactorArg().encode(smoothFactor);
      
      buffer.append(smoothEncodeStr);
      buffer.append(" ");
   }

   public String save()
   {
      return "";
   }
   
   public void valueChanged(ValueChangeEvent<Integer, SliderControl> event)
   {
      Integer smoothFactor = 0;
      if (this.smoothControl.isSelected())
         smoothFactor = this.smoothControl.getControlValue();
      
      smoothPanel.setCurrentValue(smoothFactor);
   }
}
