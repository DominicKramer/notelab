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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import noteLab.gui.settings.panel.base.PrimitiveSettingsPanel;
import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;
import noteLab.util.InfoCenter;
import noteLab.util.InfoCenter.OSType;

public class VMSettingsPanel 
                extends JPanel 
                           implements SettingsSaveCapable,
                                      SettingsStateCapable, 
                                      ActionListener
{
   private static int SAVED_INIT_MEM_MB = -1;
   private static int SAVED_MAX_MEM_MB = -1;
   
   private MemoryControlPanel initMemoryPanel;
   private MemoryControlPanel maxMemoryPanel;
   private int maxAllowedMemMb;
   
   public VMSettingsPanel()
   {
      this.maxAllowedMemMb = getMaxAllowedMemory();
      
      this.maxMemoryPanel = 
              new MemoryControlPanel("Maximum memory", 
                                     "Specifies the maximum amount of " +
                                     "memory that NoteLab can use.", 
                                     this.maxAllowedMemMb);
      this.maxMemoryPanel.addActionListener(this);
      
      this.initMemoryPanel =
              new MemoryControlPanel("Initial memory", 
                                    "Specifies the amount of memory " +
                                    "that should be given to NoteLab " +
                                    "when it starts.", 
                                    this.maxMemoryPanel.getSelectedMemoryMb());
      
      JPanel fillerPanel = new JPanel();
      fillerPanel.setLayout(new BoxLayout(fillerPanel, BoxLayout.Y_AXIS));
      for (int i=1; i<=17; i++)
         fillerPanel.add(new JLabel(" "));
      
      JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel descLabel = new JLabel("If "+InfoCenter.getAppName()+
                                      " is running slow, increasing " +
                                      "its amount of available memory " +
                                      "may help.");
      descPanel.add(descLabel);
      
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.add(descPanel);
      innerPanel.add(this.maxMemoryPanel);
      innerPanel.add(this.initMemoryPanel);
      innerPanel.add(fillerPanel);
      
      setLayout(new FlowLayout(FlowLayout.CENTER));
      add(innerPanel);
      
      revertToSaved();
   }
   
   public void actionPerformed(ActionEvent event)
   {
      syncInitPanel();
   }
   
   private void syncInitPanel()
   {
      int maxMem = this.maxMemoryPanel.getSelectedMemoryMb();
      this.initMemoryPanel.setSelectedMemoryMb(maxMem);
   }
   
   private int getMemoryAt(int index)
   {
      return 128*index+128;
   }
   
   private int getMaxAllowedMemory()
   {
      int fourGbInMb = 4*1024;
      int maxIndex = fourGbInMb/128;
      
      return getMaxAllowedMemory(0, maxIndex);
   }
   
   private int getMaxAllowedMemory(int minIndex, int maxIndex)
   {
      if (minIndex == maxIndex || maxIndex == (minIndex+1))
         return getMemoryAt(minIndex);
      
      int midIndex = (minIndex+maxIndex)/2;
      boolean isMidValid = isValidMemory(getMemoryAt(midIndex));
      if (isMidValid)
         return getMaxAllowedMemory(midIndex, maxIndex);
      
      return getMaxAllowedMemory(minIndex, midIndex);
   }
   
   private boolean isValidMemory(int memMb)
   {
      String[] cmdArr = new String[4];
      cmdArr[0] = "java";
      cmdArr[1] = "-Xms"+memMb+"M";
      cmdArr[2] = "-Xmx"+memMb+"M";
      cmdArr[3] = "-version";
      
      boolean isError = false;
      try
      {
         Process process = Runtime.getRuntime().exec(cmdArr);
         
         int exitVal = process.waitFor();
         if (exitVal != 0)
            isError = true;
      }
      catch (Exception e)
      {
         isError = true;
      }
      
      return !isError;
   }
   
   public void encode(StringBuffer buffer)
   {
   }

   public String save()
   {
      int initMem = this.initMemoryPanel.getSelectedMemoryMb();
      int maxMem = this.maxMemoryPanel.getSelectedMemoryMb();
      
      SAVED_INIT_MEM_MB = initMem;
      SAVED_MAX_MEM_MB = maxMem;
      
      OSType os = InfoCenter.getOperatingSystem();
      String exportStr = "export ";
      String quoteStr = "\"";
      if (os.equals(OSType.Windows))
      {
         exportStr = "set ";
         quoteStr = "";
      }
      
      return exportStr+"NOTELAB_VM_ARGS="+quoteStr+"-Xms"+
             initMem+"m -Xmx"+maxMem+"m"+quoteStr;
   }

   public void apply()
   {
   }

   public void restoreDefaults()
   {
      this.maxMemoryPanel.setSelectedMemoryMb(this.maxAllowedMemMb);
      syncInitPanel();
      
      this.maxMemoryPanel.setSelectedMemoryMb(this.maxAllowedMemMb);
      this.initMemoryPanel.setSelectedMemoryMb(this.maxAllowedMemMb);
   }

   public void revertToSaved()
   {
      revertMaxPanelToSaved();
      syncInitPanel();
      revertInitPanelToSaved();
   }
   
   private void revertInitPanelToSaved()
   {
      if (SAVED_INIT_MEM_MB != -1)
         this.initMemoryPanel.setSelectedMemoryMb(SAVED_INIT_MEM_MB);
      else
      {
         int initMem = (int)InfoCenter.getInitialMemoryMb();
         initMem = roundToNear128(initMem, false);
         
         this.initMemoryPanel.setSelectedMemoryMb(initMem);
      }
   }
   
   private void revertMaxPanelToSaved()
   {
      if (SAVED_MAX_MEM_MB != -1)
         this.maxMemoryPanel.setSelectedMemoryMb(SAVED_MAX_MEM_MB);
      else
      {
         int maxMem = (int)InfoCenter.getMaxMemoryMb();
         maxMem = roundToNear128(maxMem, true);
         
         this.maxMemoryPanel.setSelectedMemoryMb(maxMem);
      }
   }
   
   private static int roundToNear128(int mem, boolean up)
   {
      // mem = num*128+rem
      int rem = mem%128;
      int num = (mem-rem)/128;
      
      // num*128 is the closest multiple of 128 less than 
      // mem and (num+1)*128 is the closest multiple of 
      // 128 greater than mem
      
      if (up)
         num++;
      
      return num*128;
   }
   
   private class MemoryControlPanel extends PrimitiveSettingsPanel
   {
      private MemoryValue[] memArr;
      private JComboBox memBox;
      
      public MemoryControlPanel(String title, String info, int maxMemory)
      {
         super(title, info);
         
         if (title == null || info == null)
            throw new NullPointerException();
         
         this.memBox = new JComboBox(new DefaultComboBoxModel());
         initializeMemBox(128, maxMemory);
         setSelectedMemoryMb(maxMemory);
         
         JPanel displayPanel = getDisplayPanel();
         displayPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
         displayPanel.add(this.memBox);
      }
      
      private void initializeMemBox(int minMB, int maxMB)
      {
         int num = (maxMB-minMB)/128+1;
         this.memArr = new MemoryValue[num];
         for (int i=0; i<num; i++)
            this.memArr[i] = new MemoryValue(minMB+i*128);
         
         DefaultComboBoxModel model = 
                              (DefaultComboBoxModel)this.memBox.getModel();
         model.removeAllElements();
         
         for (MemoryValue memVal : this.memArr)
            model.addElement(memVal);
      }
      
      public void addActionListener(ActionListener listener)
      {
         this.memBox.addActionListener(listener);
      }
      
      public void setActionCommand(String command)
      {
         if (command == null)
            throw new NullPointerException();
         
         this.memBox.setActionCommand(command);
      }
      
      public int getSelectedMemoryMb()
      {
         Object selOb = this.memBox.getSelectedItem();
         if (selOb == null || !(selOb instanceof MemoryValue))
            return 128;
         
         return ((MemoryValue)selOb).getMemoryMb();
      }
      
      public int getMaxListedMemoryMB()
      {
         MemoryValue maxValue = this.memArr[this.memArr.length-1];
         return maxValue.getMemoryMb();
      }
      
      public void setSelectedMemoryMb(int memMb)
      {
         if (memMb > getMaxListedMemoryMB())
            initializeMemBox(128, memMb);
         
         int index = (memMb-128)/128;
         
         if (index < 0)
            index = 0;
         
         if (index >= this.memArr.length)
            index = this.memArr.length-1;
         
         this.memBox.setSelectedIndex(index);
      }
      
      public void apply()
      {
      }
      
      public void restoreDefaults()
      {
         VMSettingsPanel.this.restoreDefaults();
      }
      
      public void revertToSaved()
      {
         VMSettingsPanel.this.revertToSaved();
      }
   }
   
   private class MemoryValue
   {
      private int memMb;
      private float memGb;
      private String text;
      
      public MemoryValue(int memMb)
      {
         this.memMb = memMb;
         this.memGb = memMb/1024f;
         constructString();
      } 
      
      /*
      public MemoryValue(float memGb)
      {
         this.memGb = memGb;
         this.memMb = (int)(memGb*1024);
         constructString();
      }
      */
      
      private void constructString()
      {
         this.text = ""+this.memMb+" Mb ("+this.memGb+" Gb)";
      }
      
      public int getMemoryMb()
      {
         return this.memMb;
      }
      
      /*
      public float getMemoryGb()
      {
         return this.memGb;
      }
      */
      
      @Override
      public String toString()
      {
         return this.text;
      }
   }
}
