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

package noteLab.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class SlidingPanel extends JPanel implements ActionListener
{
   private static final String FORWARD = "Forward";
   private static final String BACK = "Back";
   
   private JButton forwardButton;
   private JButton backButton;
   
   private JPanel contentPanel;
   private CardLayout cardLayout;
   
   public SlidingPanel()
   {
      super(new BorderLayout());
      
      this.cardLayout = new CardLayout(0, 0);
      this.contentPanel = new JPanel(this.cardLayout);
      
      int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
      int prefSize = size+2;
      
      this.backButton = new JButton(DefinedIcon.backward.getIcon(size)); 
      this.backButton.addActionListener(this);
      this.backButton.setActionCommand(BACK);
      this.backButton.setPreferredSize(new Dimension(prefSize, prefSize));
      
      this.forwardButton = new JButton(DefinedIcon.forward.getIcon(size));
      this.forwardButton.addActionListener(this);
      this.forwardButton.setActionCommand(FORWARD);
      this.forwardButton.setPreferredSize(new Dimension(prefSize, prefSize));
      
      add(this.backButton, BorderLayout.WEST);
      add(this.contentPanel, BorderLayout.CENTER);
      add(this.forwardButton, BorderLayout.EAST);
   }
   
   public void append(Component comp)
   {
      if (comp == null)
         throw new NullPointerException();
      
      this.contentPanel.add(comp, "");
      
      Dimension minPrefSize = this.cardLayout.preferredLayoutSize(this.contentPanel);
      this.contentPanel.setPreferredSize(minPrefSize);
   }
   
   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      if (cmmd.equals(FORWARD))
      {
         this.cardLayout.next(this.contentPanel);
      }
      else if (cmmd.equals(BACK))
      {
         this.cardLayout.previous(this.contentPanel);
      }
   }
   
   public static void main(String[] args)
   {
      SlidingPanel panel = new SlidingPanel();
      panel.append(new JLabel("Label 1"));
      panel.append(new JLabel("Label 2"));
      panel.append(new JLabel("Label 3"));
      panel.append(new JLabel("Label 4"));
      
      JFrame frame = new JFrame(SlidingPanel.class.getSimpleName()+" Demo");
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
   }
}
