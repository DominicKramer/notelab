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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class NotificationFrame extends JFrame
{
   public NotificationFrame(String message, String title)
   {
      super(title);
      
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            dispose();
         }
      });
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(closeButton);
      
      JTextArea area = constructTextArea(message);
      area.setBackground(getBackground());
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(new JScrollPane(area), BorderLayout.CENTER);
      mainPanel.add(buttonPanel, BorderLayout.SOUTH);
      
      getContentPane().add(mainPanel);
      setSize(500, 500);
   }
   
   private static JTextArea constructTextArea(String message)
   {
      if (message == null)
         throw new NullPointerException();
      
      JTextArea textArea = new JTextArea(message);
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      
      return textArea;
   }
   
   public static void main(String[] args)
   {
      StringBuffer buffer = new StringBuffer();
      String text = "This is a sample message that is really long. ";
      for (int i=0; i<50; i++)
         buffer.append(text);
      
      (new NotificationFrame(buffer.toString(), "Title")).setVisible(true);
   }
}
