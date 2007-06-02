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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import noteLab.util.progress.ProgressEvent;
import noteLab.util.progress.ProgressListener;
import noteLab.util.progress.Progressive;

public class ProgressFrame 
                extends JFrame 
                           implements ProgressListener, 
                                      ActionListener, 
                                      WindowListener
{
   private JLabel mainMessage;
   private JLabel subMessage;
   private JLabel errorMessage;
   
   private JProgressBar progressBar;
   
   private boolean autoClose;
   
   public ProgressFrame(String title, boolean autoClose)
   {
      super(title);
      addWindowListener(this);
      setAlwaysOnTop(true);
      
      this.autoClose = autoClose;
      
      JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      this.mainMessage = new JLabel();
      Font font = this.mainMessage.getFont();
      Font newFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
      this.mainMessage.setFont(newFont);
      mainPanel.add(this.mainMessage);
      
      JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      this.subMessage = new JLabel();
      subPanel.add(this.subMessage);
      
      JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      this.errorMessage = new JLabel();
      this.errorMessage.setBackground(Color.RED);
      this.errorMessage.setFont(newFont);
      
      JPanel textPanel = new JPanel();
      textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
      textPanel.add(mainPanel);
      textPanel.add(subPanel);
      textPanel.add(errorPanel);
      
      JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      this.progressBar = new JProgressBar(0, 100);
      progressPanel.add(this.progressBar);
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      int size = GuiSettingsConstants.BUTTON_SIZE;
      JButton okButton = new JButton("Close", DefinedIcon.ok.getIcon(size));
      okButton.addActionListener(this);
      buttonPanel.add(okButton);
      
      setLayout(new BorderLayout());
      add(textPanel, BorderLayout.NORTH);
      add(progressPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
      
      pack();
      setVisible(true);
   }
   
   /*
   public static void showProgressFrame(final String title, 
                                        final Progressive progressive)
   {
      showProgressFrame(title, progressive, null);
   }
   
   public static void showProgressFrame(final String title, 
                                        final Progressive progressive, 
                                        final ProgressEvent event)
   {
      new Thread(new Runnable()
      {
         public void run()
         {
            ProgressFrame frame = new ProgressFrame(title);
            progressive.addProgressListener(frame);
            if (event != null)
               frame.progressOccured(event);
         }
      }).start();
   }
   */
   
   public void progressOccured(ProgressEvent event)
   {
      if (event == null)
         throw new NullPointerException();
      
      String message = event.getMainMessage();
      if (message != null)
         this.mainMessage.setText(message);
      
      message = event.getSubMessage();
      if (message != null)
         this.subMessage.setText(message);
      
      message = event.getErrorMessage();
      if (message != null)
         this.errorMessage.setText(message);
      
      boolean indeterminant = event.isIndeterminate();
      this.progressBar.setStringPainted(!indeterminant);
      if (indeterminant)
         this.progressBar.setIndeterminate(true);
      else
         this.progressBar.setValue(event.getPercent());
      
      if (this.autoClose && event.isComplete())
         dispose();
      
      pack();
   }

   public void actionPerformed(ActionEvent e)
   {
      dispose();
   }
   
   public void windowActivated(WindowEvent e)
   {
   }

   public void windowClosed(WindowEvent e)
   {
   }

   public void windowClosing(WindowEvent e)
   {
      dispose();
   }

   public void windowDeactivated(WindowEvent e)
   {
   }

   public void windowDeiconified(WindowEvent e)
   {
   }

   public void windowIconified(WindowEvent e)
   {
   }

   public void windowOpened(WindowEvent e)
   {
   }
   
   public static void main(String[] args)
   {
      Progressive progressive = new Progressive()
      {
         private ProgressListener listener;
         
         public void addProgressListener(ProgressListener listener)
         {
            this.listener = listener;
            
            try
            {
               Thread.sleep(1000);
               start();
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
         }

         public void removeProgressListener(ProgressListener listener)
         {
            this.listener = null;
         }
         
         public void start() throws InterruptedException
         {
            int max = 100;
            for (int i=1; i<=max; i++)
            {
               Thread.sleep(500);
               if (this.listener != null)
               {
                  System.err.println("i="+i);
                  this.listener.progressOccured(new ProgressEvent("Doing something", 
                                                                  "Looking at i="+i, 
                                                                  null, 
                                                                  false, 
                                                                  i, 
                                                                  false));
               }
            }
         }
      };
      
      ProgressFrame frame = new ProgressFrame("Demo", false);
      progressive.addProgressListener(frame);
      frame.setVisible(true);
   }
}
