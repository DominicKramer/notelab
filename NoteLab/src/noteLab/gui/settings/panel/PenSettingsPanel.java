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

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.control.drop.SizeControl;
import noteLab.gui.control.drop.pic.PrimitivePic.Style;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.settings.constants.PenSettingsConstants;
import noteLab.gui.settings.panel.base.ManagedSettingsPanel;
import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;
import noteLab.util.arg.PenColorArg;
import noteLab.util.arg.PenSizeArg;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class PenSettingsPanel 
                extends JPanel 
                           implements SettingsStateCapable, 
                                      SettingsSaveCapable
{
   private ManagedSettingsPanel sizePanel;
   private ManagedSettingsPanel colorPanel;
   private SizeControl sizeControl;
   private ColorControl colorControl;
   private int penNum;
   
   public PenSettingsPanel(int penNum)
   {
      this.penNum = penNum;
      
      this.colorControl = new ColorControl(getColor(penNum));
      this.colorControl.addValueChangeListener(new ColorListener());
      
      MValue curSize = getPenSize(penNum);
      Unit unit = curSize.getUnit();
      
      int res = Unit.getScreenResolution();
      // Use a unitScaleFactor of 1 so that the numbers shown in the 
      // settings dialog match those shown in the main gui.
      double min = Unit.getValue((float)PenSettingsConstants.MIN_SIZE_PX, 
                                   Unit.PIXEL, unit, res, 1);
      double max = Unit.getValue((float)PenSettingsConstants.MAX_SIZE_PX, 
                                   Unit.PIXEL, unit, res, 1);
      double step = Unit.getValue((float)PenSettingsConstants.STEP_SIZE_PX, 
                                    Unit.PIXEL, unit, res, 1);
      
      this.sizeControl = 
                     new SizeControl("", curSize.getValue(unit), 
                                     min, max, step, unit, Style.Circle, 
                                     true, Color.BLACK, 1);
      this.sizeControl.addValueChangeListener(new SizeListener());
      
      this.sizePanel = 
         new ManagedSettingsPanel("Size", "Specifies the size of pen "+penNum, 
                                  getSizeKey(penNum), 
                                  getDefaultSize(penNum))
      {
         @Override
         public void updateDisplay(Object curVal)
         {
            if (curVal == null)
               throw new NullPointerException();
            
            sizeControl.setControlValue((MValue)curVal);
         }
      };
      this.sizePanel.getDisplayPanel().add(this.sizeControl);
      
      this.colorPanel = 
         new ManagedSettingsPanel("Color", 
                                  "Specifies the color of pen "+penNum, 
                                  getColorKey(penNum), 
                                  getDefaultColor(penNum))
      {
         @Override
         public void updateDisplay(Object curVal)
         {
            if (curVal == null)
               throw new NullPointerException();
            
            colorControl.setControlValue((Color)curVal);
         }
      };
      this.colorPanel.getDisplayPanel().add(this.colorControl);
      
      setBorder(new TitledBorder("Pen "+penNum+" Settings"));
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(this.sizePanel);
      add(this.colorPanel);
   }
   
   private static String getColorKey(int penNum) 
   {
      switch (penNum)
      {
         case 1:
            return SettingsKeys.PEN_1_COLOR_KEY;
         case 2:
            return SettingsKeys.PEN_2_COLOR_KEY;
         case 3:
            return SettingsKeys.PEN_3_COLOR_KEY;
         
         default:
            throw getException(penNum);
      }
   }
   
   private static Color getColor(int penNum)
   {
      Object colorOb = SettingsManager.getSharedInstance().
                          getValue(getColorKey(penNum));
      
      if (colorOb == null || !(colorOb instanceof Color))
         return getDefaultColor(penNum);
      
      return (Color)colorOb;
   }
   
   private static String getSizeKey(int penNum) 
   {
      switch (penNum)
      {
         case 1:
            return SettingsKeys.PEN_1_SIZE_KEY;
         case 2:
            return SettingsKeys.PEN_2_SIZE_KEY;
         case 3:
            return SettingsKeys.PEN_3_SIZE_KEY;
            
         default:
            throw getException(penNum);
      }
   }
   
   private static MValue getPenSize(int penNum)
   {
      Object sizeOb = SettingsManager.getSharedInstance().
                         getValue(getSizeKey(penNum));
      
      if (sizeOb == null || !(sizeOb instanceof MValue))
         return getDefaultSize(penNum);
      
      return (MValue)sizeOb;
   }
   
   private static MValue getDefaultSize(int penNum)
   {
      switch (penNum)
      {
         case 1:
            return new MValue(PenSettingsConstants.FINE_SIZE_PX, 
                              Unit.PIXEL);
         case 2:
            return new MValue(PenSettingsConstants.MEDIUM_SIZE_PX, 
                              Unit.PIXEL);
         case 3:
            return new MValue(PenSettingsConstants.THICK_SIZE_PX, 
                              Unit.PIXEL);
         
         default:
            throw getException(penNum);
      }
   }
   
   private static Color getDefaultColor(int penNum)
   {
      switch (penNum)
      {
         case 1:
            return PenSettingsConstants.PEN_1_COLOR;
         case 2:
            return PenSettingsConstants.PEN_2_COLOR;
         case 3:
            return PenSettingsConstants.PEN_3_COLOR;
         
         default:
            throw getException(penNum);
      }
   }
   
   private static IllegalArgumentException getException(int penNum)
   {
      return new IllegalArgumentException("Unknown pen number "+
                                          penNum+".  Only pen " +
                                          "1, 2, and 3 are allowed.");
   }
   
   public static void main(String[] args)
   {
      PenSettingsPanel panel = new PenSettingsPanel(1);
      
      JFrame frame = new JFrame(PenSettingsPanel.class.getName());
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
   }
   
   private class ColorListener 
                    implements 
                       ValueChangeListener<Color, ColorControl>
   {
      public void valueChanged(ValueChangeEvent<Color, ColorControl> event)
      {
         colorPanel.setCurrentValue(event.getCurrentValue());
      }
   }
   
   private class SizeListener 
                    implements 
                       ValueChangeListener<MValue, SizeControl>
   {
      public void valueChanged(ValueChangeEvent<MValue, SizeControl> event)
      {
         sizePanel.setCurrentValue(event.getCurrentValue());
      }
   }
   
   private void sync()
   {
      MValue curSize = (MValue)this.sizePanel.getCurrentValue();
      if (curSize != null)
         this.sizeControl.setControlValue(curSize);
      
      Color curColor = (Color)this.colorPanel.getCurrentValue();
      if (curColor != null)
         this.colorControl.setControlValue(curColor);
   }

   public void restoreDefaults()
   {
      this.sizePanel.restoreDefaults();
      this.colorPanel.restoreDefaults();
      
      sync();
   }

   public void revertToSaved()
   {
      this.sizePanel.revertToSaved();
      this.colorPanel.revertToSaved();
      
      sync();
   }
   
   public void apply()
   {
      this.sizePanel.apply();
      this.colorPanel.apply();
   }

   public void encode(StringBuffer buffer)
   {
      if (buffer == null)
         throw new NullPointerException();
      
      int length = buffer.length();
      if (length > 0 && buffer.charAt(length-1) != ' ')
         buffer.append(' ');
      
      Color curColor = this.colorControl.getControlValue();
      MValue curSize = this.sizeControl.getControlValue();
      
      String sizeEncodeStr = 
                new PenSizeArg(getSizeKey(this.penNum), 
                               this.penNum).encode(curSize);
      
      String colorEncodeStr = 
                new PenColorArg(getColorKey(this.penNum), 
                                this.penNum).encode(curColor);
      
      buffer.append(sizeEncodeStr);
      buffer.append(" ");
      buffer.append(colorEncodeStr);
   }

   public String save()
   {
      return "";
   }
}
