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

package noteLab.util.mem;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MemoryAnalyzer extends JFrame implements ActionListener
{
   private MemoryMXBean memoryBean;
   
   private JLabel initLabel;
   private JLabel usedLabel;
   private JLabel committedLabel;
   private JLabel maxLabel;
   private JLabel percentLabel;
   
   public MemoryAnalyzer(int delay)
   {
      this.memoryBean = ManagementFactory.getMemoryMXBean();
      
      String text = "               ";
      this.initLabel = new JLabel(text);
      JPanel initPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      initPanel.add(new JLabel("Initial memory:  "));
      initPanel.add(this.initLabel);
      
      this.usedLabel = new JLabel(text);
      JPanel usedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      usedPanel.add(new JLabel("Used memory:  "));
      usedPanel.add(this.usedLabel);
      
      this.committedLabel = new JLabel(text);
      JPanel commitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      commitPanel.add(new JLabel("Commmitted memory:  "));
      commitPanel.add(this.committedLabel);
      
      this.maxLabel = new JLabel(text);
      JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      maxPanel.add(new JLabel("Max memory:  "));
      maxPanel.add(this.maxLabel);
      
      this.percentLabel = new JLabel(text);
      JPanel percentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      percentPanel.add(new JLabel("% of max:  "));
      percentPanel.add(this.percentLabel);
      
      setTitle("Memory Heap Usage");
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(initPanel);
      panel.add(usedPanel);
      panel.add(commitPanel);
      panel.add(maxPanel);
      panel.add(percentPanel);
      
      getContentPane().add(panel);
      pack();
      
      Timer timer = new Timer(delay, this);
      timer.start();
   }
   
   private double getKB(long bytes)
   {
      return bytes/1024.0;
   }
   
   private double getMB(long bytes)
   {
      return getKB(bytes)/1024.0;
   }
   
   private double getGB(long bytes)
   {
      return getMB(bytes)/1024.0;
   }
   
   
   public void actionPerformed(ActionEvent e)
   {
      MemoryUsage memUsage = this.memoryBean.getHeapMemoryUsage();
      double initMB = getMB(memUsage.getInit());
      double usedMB = getMB(memUsage.getUsed());
      double commitMB = getMB(memUsage.getCommitted());
      double maxMB = getMB(memUsage.getMax());
      double percent = usedMB/maxMB*100;
      
      this.initLabel.setText(""+initMB+" MB");
      this.usedLabel.setText(""+usedMB+" MB");
      this.committedLabel.setText(""+commitMB+" MB");
      this.maxLabel.setText(""+maxMB+" MB");
      this.percentLabel.setText(""+percent+"%");
   }
   
   public static void main(String[] args)
   {
      new MemoryAnalyzer(100).setVisible(true);
   }
}
