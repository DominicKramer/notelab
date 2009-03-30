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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.settings.constants.PageSettingsConstants;
import noteLab.gui.settings.panel.base.ManagedSettingsPanel;
import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;
import noteLab.model.Paper.PaperType;
import noteLab.util.arg.PaperColorArg;
import noteLab.util.arg.PaperTypeArg;
import noteLab.util.arg.UnitScaleArg;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class PageSettingsPanel 
                extends JPanel 
                           implements SettingsStateCapable, 
                                      SettingsSaveCapable
{
   private PaperTypePanel typePanel;
   private PaperColorPanel colorPanel;
   private UnitScalePanel unitPanel;
   
   public PageSettingsPanel()
   {
      this.typePanel = new PaperTypePanel();
      this.colorPanel = new PaperColorPanel();
      this.unitPanel = new UnitScalePanel();
      
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.add(this.typePanel);
      innerPanel.add(this.colorPanel);
      innerPanel.add(this.unitPanel);
      
      setBorder(new TitledBorder("Paper Settings"));
      add(innerPanel);
   }
   
   private void sync()
   {
      this.typePanel.sync();
      this.colorPanel.sync();
      this.unitPanel.sync();
   }
   
   public void restoreDefaults()
   {
      this.typePanel.restoreDefaults();
      this.colorPanel.restoreDefaults();
      this.unitPanel.restoreDefaults();
      
      sync();
   }
   
   public void revertToSaved()
   {
      this.typePanel.revertToSaved();
      this.colorPanel.revertToSaved();
      this.unitPanel.revertToSaved();
      
      sync();
   }
   
   public void apply()
   {
      this.typePanel.apply();
      this.colorPanel.apply();
      this.unitPanel.apply();
   }
   
   public void encode(StringBuffer buffer)
   {
      if (buffer == null)
         throw new NullPointerException();
      
      this.colorPanel.encode(buffer);
      this.typePanel.encode(buffer);
      this.unitPanel.encode(buffer);
   }
   
   public String save()
   {
      return "";
   }
   
   private class UnitScalePanel 
                    extends ManagedSettingsPanel 
                               implements ChangeListener
   {
      private JSlider unitSlider;
      
      public UnitScalePanel()
      {
         super("Unit Scale Percent", 
               "Scales lengths by the given percent.", 
               SettingsKeys.UNIT_SCALE_FACTOR, 
               100*PageSettingsConstants.DEFAULT_UNIT_SCALE_FACTOR);
         
         this.unitSlider = new JSlider(0, 200);
         this.unitSlider.setMajorTickSpacing(50);
         this.unitSlider.setPaintLabels(true);
         this.unitSlider.setPaintTicks(true);
         this.unitSlider.setPaintTrack(true);
         this.unitSlider.addChangeListener(this);
         sync();
         
         getDisplayPanel().add(this.unitSlider);
      }
      
      @Override
      public void updateDisplay(Object curVal)
      {
         if (curVal == null)
            throw new NullPointerException();
         
         Number number = (Number)curVal;
         int val = number.intValue();
         this.unitSlider.setValue(val);
      }
      
      public void encode(StringBuffer buffer)
      {
         if (buffer == null)
            throw new NullPointerException();
         
         int length = buffer.length();
         if (length > 0 && buffer.charAt(length-1) != ' ')
            buffer.append(' ');
         
         float scaleFactor = this.unitSlider.getValue()/100f;
         String encodedStr = new UnitScaleArg().encode(scaleFactor);
         buffer.append(encodedStr);
      }
      
      public void sync()
      {
         Object factorOb = SettingsManager.getSharedInstance().
                              getValue(SettingsKeys.UNIT_SCALE_FACTOR);
         float factor = PageSettingsConstants.DEFAULT_UNIT_SCALE_FACTOR;
         if (factorOb != null && factorOb instanceof Float)
            factor = (Float)factorOb;
         factor *= 100;
         
         this.unitSlider.setValue((int)factor);
      }

      public void stateChanged(ChangeEvent e)
      {
         int curVal = this.unitSlider.getValue();
         if (curVal == 0)
            this.unitSlider.setValue(1);
         
         setCurrentValue(this.unitSlider.getValue()/100f);
      }
   }
   
   private class PaperColorPanel 
                    extends ManagedSettingsPanel 
                               implements ValueChangeListener<Color, 
                                          ColorControl>
   {
      private ColorControl colorControl;
      
      public PaperColorPanel()
      {
         super("Paper Color", 
               "Specifies the background color " +
               "of newly constructed paper.", 
               SettingsKeys.PAPER_COLOR_KEY, 
               PageSettingsConstants.PAPER_COLOR);
         
         Color color = PageSettingsConstants.PAPER_COLOR;
         Object colorOb = SettingsManager.
                             getSharedInstance().
                                getValue(SettingsKeys.PAPER_COLOR_KEY);

         if ( (colorOb != null) && (colorOb instanceof Color))
            color = (Color)colorOb;
         
         this.colorControl = new ColorControl(color);
         this.colorControl.addValueChangeListener(this);
         
         getDisplayPanel().add(this.colorControl);
      }
      
      @Override
      public void updateDisplay(Object curVal)
      {
         if (curVal == null)
            throw new NullPointerException();
         
         this.colorControl.setControlValue((Color)curVal);
      }
      
      public void sync()
      {
         this.colorControl.
                 setControlValue(this.colorControl.getControlValue());
      }
      
      public Color getSelectedColor()
      {
         return this.colorControl.getControlValue();
      }
      
      public void encode(StringBuffer buffer)
      {
         if (buffer == null)
            throw new NullPointerException();
         
         int length = buffer.length();
         if (length > 0 && buffer.charAt(length-1) != ' ')
            buffer.append(' ');
         
         String encodedCol = new PaperColorArg().encode(getSelectedColor());
         buffer.append(encodedCol);
      }
      
      public void valueChanged(ValueChangeEvent<Color, ColorControl> event)
      {
         setCurrentValue(event.getCurrentValue());
      }
   }
   
   private class PaperTypePanel 
                    extends ManagedSettingsPanel 
                               implements ActionListener, 
                                          SettingsSaveCapable
   {
      private final DefinedIcon PLAIN_ICON = DefinedIcon.page;
      private final DefinedIcon GRAPH_ICON = DefinedIcon.graph;
      private final DefinedIcon COLLEGE_RULE_ICON = DefinedIcon.college_rule;
      private final DefinedIcon WIDE_RULE_ICON = DefinedIcon.wide_rule;
      
      private JToggleButton plainButton;
      private JToggleButton graphButton;
      private JToggleButton collegeButton;
      private JToggleButton wideButton;
      
      public PaperTypePanel()
      {
         super("Paper Type", 
               "Specifies the paper type to use " +
               "when constructing new paper.", 
               SettingsKeys.PAPER_TYPE_KEY, 
               PageSettingsConstants.PAPER_TYPE);
         
         int size = GuiSettingsConstants.BUTTON_SIZE;
         
         this.plainButton = 
                          new JToggleButton(PLAIN_ICON.getIcon(size));
         this.plainButton.addActionListener(this);
         this.plainButton.setActionCommand(PLAIN_ICON.name());
         
         this.graphButton = 
                          new JToggleButton(GRAPH_ICON.getIcon(size));
         this.graphButton.addActionListener(this);
         this.graphButton.setActionCommand(GRAPH_ICON.name());
         
         this.collegeButton = 
                          new JToggleButton(COLLEGE_RULE_ICON.getIcon(size));
         this.collegeButton.addActionListener(this);
         this.collegeButton.setActionCommand(COLLEGE_RULE_ICON.name());
         
         this.wideButton = 
                          new JToggleButton(WIDE_RULE_ICON.getIcon(size));
         this.wideButton.addActionListener(this);
         this.wideButton.setActionCommand(WIDE_RULE_ICON.name());
         
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.add(this.plainButton);
         buttonPanel.add(this.graphButton);
         buttonPanel.add(this.collegeButton);
         buttonPanel.add(this.wideButton);
         
         ButtonGroup group = new ButtonGroup();
         group.add(this.plainButton);
         group.add(this.graphButton);
         group.add(this.collegeButton);
         group.add(this.wideButton);
         
         sync();
         
         getDisplayPanel().add(buttonPanel);
      }
      
      public void sync()
      {
         PaperType type = (PaperType)getCurrentValue();
         setType(type);
      }
      
      @Override
      public void updateDisplay(Object curVal)
      {
         if (curVal == null)
            throw new NullPointerException();
         
         setType((PaperType)curVal);
      }
      
      public void actionPerformed(ActionEvent e)
      {
         String cmmd = e.getActionCommand();
         
         if (cmmd.equals(PLAIN_ICON.name()))
            typePanel.setCurrentValue(PaperType.Plain);
         else if (cmmd.equals(GRAPH_ICON.name()))
            typePanel.setCurrentValue(PaperType.Graph);
         else if (cmmd.equals(COLLEGE_RULE_ICON.name()))
            typePanel.setCurrentValue(PaperType.CollegeRuled);
         else if (cmmd.equals(WIDE_RULE_ICON.name()))
            typePanel.setCurrentValue(PaperType.WideRuled);
      }

      public void encode(StringBuffer buffer)
      {
         if (buffer == null)
            throw new NullPointerException();
         
         int length = buffer.length();
         if (length > 0 && buffer.charAt(length-1) != ' ')
            buffer.append(' ');
         
         String encodedStr = new PaperTypeArg().encode(getType());
         buffer.append(encodedStr);
      }

      public String save()
      {
         return "";
      }
      
      public PaperType getType()
      {
         if (this.plainButton.isSelected())
            return PaperType.Plain;
         
         if (this.graphButton.isSelected())
            return PaperType.Graph;
         
         if (this.collegeButton.isSelected())
            return PaperType.CollegeRuled;
         
         return PaperType.WideRuled;
      }
      
      public void setType(PaperType type)
      {
         if (type == null)
            throw new NullPointerException();
         
         this.plainButton.setSelected(false);
         this.graphButton.setSelected(false);
         this.collegeButton.setSelected(false);
         this.wideButton.setSelected(false);
         
         if (type.equals(PaperType.Plain))
            this.plainButton.setSelected(true);
         else if (type.equals(PaperType.Graph))
            this.graphButton.setSelected(true);
         else if (type.equals(PaperType.CollegeRuled))
            this.collegeButton.setSelected(true);
         else
            this.wideButton.setSelected(true);
      }
   }
}
