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

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MemoryAnalyzer2 extends JFrame implements ActionListener
{
   private JLabel freeLabel;
   private JLabel totalLabel;
   private JLabel maxLabel;
   private JLabel percentLabel;
   
   public MemoryAnalyzer2(int delay)
   {
      String text = "               ";
      this.freeLabel = new JLabel(text);
      JPanel freePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      freePanel.add(new JLabel("Free memory:  "));
      freePanel.add(this.freeLabel);
      
      this.totalLabel = new JLabel(text);
      JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      totalPanel.add(new JLabel("Total memory:  "));
      totalPanel.add(this.totalLabel);
      
      this.maxLabel = new JLabel(text);
      JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      maxPanel.add(new JLabel("Max memory:  "));
      maxPanel.add(this.maxLabel);
      
      this.percentLabel = new JLabel(text);
      JPanel percentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      percentPanel.add(new JLabel("Percent:  "));
      percentPanel.add(this.percentLabel);
      
      setTitle("Memory Heap Usage");
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(freePanel);
      panel.add(maxPanel);
      panel.add(totalPanel);
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
      Runtime run = Runtime.getRuntime();
      double freeMB = getMB(run.freeMemory());
      double maxMB = getMB(run.maxMemory());
      double totalMB = getMB(run.totalMemory());
      
      // the total free is the amount free inside the 
      // virtual machine plus the extra memory not 
      // already used by the virtual machine.
      double totalFree = freeMB+(maxMB-totalMB);
      double percent = (1-totalFree/maxMB)*100;
      
      this.freeLabel.setText(""+freeMB+" MB");
      this.maxLabel.setText(""+maxMB+" MB");
      this.totalLabel.setText(""+totalMB+" MB");
      this.percentLabel.setText(""+percent+"%");
   }
   
   public static void main(String[] args)
   {
      new MemoryAnalyzer2(100).setVisible(true);
   }
}
