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

package noteLab.gui.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.util.undoRedo.UndoRedoListener;
import noteLab.util.undoRedo.UndoRedoManager;

public class UndoRedoToolBar extends JToolBar implements UndoRedoListener, 
                                                         GuiSettingsConstants
{
   private UndoRedoManager manager;
   
   private JButton undoButton;
   private JButton redoButton;
   
   public UndoRedoToolBar(UndoRedoManager manager)
   {
      if (manager == null)
         throw new NullPointerException();
      
      this.manager = manager;
      this.manager.addUndoRedoListener(this);
      
      this.undoButton = new JButton(DefinedIcon.undo.getIcon(BUTTON_SIZE));
      this.undoButton.addActionListener(new UndoListener());
      
      this.redoButton = new JButton(DefinedIcon.redo.getIcon(BUTTON_SIZE));
      this.redoButton.addActionListener(new RedoListener());
      
      add(this.undoButton);
      add(this.redoButton);
      
      undoRedoStackChanged(this.manager);
   }

   public void undoRedoStackChanged(UndoRedoManager manager)
   {
      if (manager == null)
         throw new NullPointerException();
      
      this.undoButton.setEnabled(manager.canUndo());
      this.redoButton.setEnabled(manager.canRedo());
   }
   
   private class UndoListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         manager.undo();
      }
   }
   
   private class RedoListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         manager.redo();
      }
   }

   public void undoRedoStackWarning(UndoRedoManager manager, 
                                    String message)
   {
      if (message == null)
         message = "Unknown";
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.dialog_warning.getIcon(size);
      
      String title = "Warning";
      
      JOptionPane.showMessageDialog(new JFrame(), 
                                    message, 
                                    title, 
                                    JOptionPane.WARNING_MESSAGE, 
                                    icon);
   }
}
