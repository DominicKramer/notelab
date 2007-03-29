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

package noteLab.gui.sequence;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;

public class SequenceFrame 
                extends JFrame 
                           implements ActionListener, 
                                      SequenceTileListener, 
                                      WindowListener
{
   private static final DefinedIcon FORWARD_ICON = DefinedIcon.go_forward_ltr;
   private static final DefinedIcon BACKWARD_ICON = DefinedIcon.go_back_ltr;
   private static final DefinedIcon CANCEL_ICON = DefinedIcon.close;
   private static final DefinedIcon FINISH_ICON = DefinedIcon.ok;
   
   private JPanel northPanel;
   private JPanel eastPanel;
   private JPanel westPanel;
   private JPanel centerPanel;
   
   private JButton prevButton;
   private JButton nextButton;
   
   private SequenceTile curTile;
   
   private Runnable quitRunnable;
   private Runnable finishRunnable;
   
   public SequenceFrame(SequenceTile curTile, 
                        Runnable quitRunnable, 
                        Runnable finishRunnable)
   {
      if (curTile == null || quitRunnable == null || finishRunnable == null)
         throw new NullPointerException();
      
      this.quitRunnable = quitRunnable;
      this.finishRunnable = finishRunnable;
      
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(this);
      
      this.curTile = curTile;
      
      int size = GuiSettingsConstants.MEDIUM_BUTTON_SIZE;
      
      this.prevButton = new JButton("Previous", BACKWARD_ICON.getIcon(size));
      this.prevButton.setActionCommand(BACKWARD_ICON.name());
      this.prevButton.addActionListener(this);
      
      this.nextButton = new JButton();
      this.nextButton.setActionCommand(FORWARD_ICON.name());
      this.nextButton.addActionListener(this);
      
      processSequenceChange();
      
      JButton cancelButton = new JButton("Cancel", CANCEL_ICON.getIcon(size));
      cancelButton.setActionCommand(CANCEL_ICON.name());
      cancelButton.addActionListener(this);
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(this.prevButton);
      buttonPanel.add(this.nextButton);
      buttonPanel.add(cancelButton);
      
      this.northPanel = new JPanel(new GridLayout(1,1));
      this.eastPanel = new JPanel(new GridLayout(1,1));
      this.westPanel = new JPanel(new GridLayout(1,1));
      this.centerPanel = new JPanel(new GridLayout(1,1));
      
      setLayout(new BorderLayout());
      add(this.northPanel, BorderLayout.NORTH);
      add(this.eastPanel, BorderLayout.EAST);
      add(this.westPanel, BorderLayout.WEST);
      add(this.centerPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
      
      if (setCurrentTile(curTile))
         processSequenceChange();
   }
   
   public JPanel getNorthPanel()
   {
      return this.northPanel;
   }
   
   public JPanel getEastPanel()
   {
      return this.eastPanel;
   }
   
   public JPanel getWestPanel()
   {
      return this.westPanel;
   }
   
   private void setButtonAsNext()
   {
      int size = GuiSettingsConstants.MEDIUM_BUTTON_SIZE;
      
      this.nextButton.setText("Next");
      this.nextButton.setIcon(FORWARD_ICON.getIcon(size));
      this.nextButton.setActionCommand(FORWARD_ICON.name());
   }
   
   private void setButtonAsFinish()
   {
      int size = GuiSettingsConstants.MEDIUM_BUTTON_SIZE;
      
      this.nextButton.setText("Finish");
      this.nextButton.setIcon(FINISH_ICON.getIcon(size));
      this.nextButton.setActionCommand(FINISH_ICON.name());
   }
   
   private void processSequenceChange()
   {
      if (!this.curTile.hasNextTile())
         setButtonAsFinish();
      else
         setButtonAsNext();
      
      this.prevButton.setEnabled(this.curTile.hasPreviousTile());
      this.nextButton.setEnabled(this.curTile.getProceedType().canProceed());
   }
   
   private boolean setCurrentTile(SequenceTile tile)
   {
      if (tile == null)
         return false;
      
      this.curTile.removeSequenceTileListener(this);
      this.curTile = tile;
      this.curTile.addSequenceTileListener(this);
      
      this.centerPanel.removeAll();
      this.centerPanel.add(tile);
      this.centerPanel.revalidate();
      this.centerPanel.repaint();
      
      return true;
   }
   
   public void actionPerformed(ActionEvent event)
   {
      if (event == null)
         return;
      
      String cmmd = event.getActionCommand();
      if (cmmd == null)
         return;
      
      if (cmmd.equals(FORWARD_ICON.name()))
      {
         if (setCurrentTile(this.curTile.getNextTile()))
            processSequenceChange();
      }
      else if (cmmd.equals(BACKWARD_ICON.name()))
      {
         if (setCurrentTile(this.curTile.getPreviousTile()))
            processSequenceChange();
      }
      else if (cmmd.equals(CANCEL_ICON.name()))
      {
         if (showConfirmDialog())
         {
            this.curTile.sequenceCancelled();
            dispose();
            this.quitRunnable.run();
         }
      }
      else if (cmmd.equals(FINISH_ICON.name()))
      {
         this.curTile.sequenceCompleted();
         dispose();
         this.finishRunnable.run();
      }
   }
   
   public void tileProceedChanged(SequenceTile tile, ProceedType proceed)
   {
      if (tile == null || tile != this.curTile || proceed == null)
         return;
      
      this.nextButton.setEnabled(proceed.canProceed());
   }
   
   private boolean showConfirmDialog()
   {
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.dialog_question.getIcon(size);
      
      String message = "Are you sure you want to quit?";
      String title = "Quit?";
      
      int ret = JOptionPane.showConfirmDialog(new JFrame(), 
                                              message, 
                                              title, 
                                              JOptionPane.YES_NO_OPTION, 
                                              JOptionPane.QUESTION_MESSAGE, 
                                              icon);
      
      return ret == JOptionPane.YES_OPTION;
   }

   public void windowActivated(WindowEvent e)
   {
   }

   public void windowClosed(WindowEvent e)
   {
   }

   public void windowClosing(WindowEvent e)
   {
      if (showConfirmDialog())
         this.quitRunnable.run();
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
}
