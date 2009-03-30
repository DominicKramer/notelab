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

package noteLab.gui.file;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import noteLab.util.InfoCenter;

public class ViewFilePanel extends JPanel
{
   private JTextArea textArea;
   
   public ViewFilePanel()
   {
      this(50, 50);
   }
   
   public ViewFilePanel(int rows, int cols)
   {
      this.textArea = new JTextArea(rows, cols);
      this.textArea.setEditable(false);
      this.textArea.setWrapStyleWord(true);
      this.textArea.setLineWrap(true);
      
      setLayout(new GridLayout(1, 1));
      add(new JScrollPane(this.textArea), BorderLayout.CENTER);
   }
   
   public void setText(String realUrl, String urlName, String name)
   {
      this.textArea.setText("");
      
      InputStream input = ClassLoader.getSystemResourceAsStream(realUrl);
      if (input == null)
      {
         StringBuffer buffer = new StringBuffer("The ");
         buffer.append(name);
         buffer.append(" could not be found.  It was stored in the file \"");
         buffer.append(urlName);
         buffer.append("\" in ");
         buffer.append(InfoCenter.getAppName());
         buffer.append("'s installation directory.");
         
         this.textArea.setText(buffer.toString());
         
         return;
      }
      
      StringBuffer textBuffer = new StringBuffer();
      try
      {
         while (input.available() > 0)
            textBuffer.append((char)input.read());
      }
      catch (IOException e)
      {
         this.textArea.setText("An error has occured.  The " +
                               "message returned was:  "+e.getMessage());
         return;
      }
      
      this.textArea.setText(textBuffer.toString());
   }
   
   public void clear()
   {
      this.textArea.setText("");
   }
}
