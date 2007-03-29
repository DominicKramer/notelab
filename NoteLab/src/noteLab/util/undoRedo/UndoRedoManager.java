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

package noteLab.util.undoRedo;

import java.util.Vector;

import noteLab.util.settings.SettingsChangedEvent;
import noteLab.util.settings.SettingsChangedListener;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;
import noteLab.util.structure.FiniteStack;
import noteLab.util.undoRedo.action.HistoryAction;
import noteLab.util.undoRedo.action.UndoRedoConstants;

public class UndoRedoManager 
                implements UndoRedoConstants, 
                           SettingsChangedListener
{
   private FiniteStack<UndoRedoAction> undoStack;
   private FiniteStack<UndoRedoAction> redoStack;
   
   private Vector<UndoRedoListener> listenerVec;
   
   public UndoRedoManager(UndoRedoListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      int historySize = DEFAULT_HISTORY_SIZE;
      
      Object sizeOb = SettingsManager.
                         getSharedInstance().
                            getValue(SettingsKeys.HISTORY_SIZE_KEY);
      
      if (sizeOb != null)
      {
         try
         {
            historySize = Integer.parseInt(sizeOb.toString());
         }
         catch (NumberFormatException e)
         {
            historySize = DEFAULT_HISTORY_SIZE;
         }
      }
      
      this.listenerVec = new Vector<UndoRedoListener>();
      addUndoRedoListener(listener);
      
      try
      {
         this.undoStack = 
            new FiniteStack<UndoRedoAction>(historySize);
         this.redoStack = 
            new FiniteStack<UndoRedoAction>(historySize);
      }
      catch (OutOfMemoryError error)
      {
         StringBuffer buffer = new StringBuffer("The system does not ");
         buffer.append("have the memory to support a history size of ");
         buffer.append(historySize);
         buffer.append(".  The default size of ");
         buffer.append(DEFAULT_HISTORY_SIZE);
         buffer.append(" will be used.");
         
         this.undoStack = 
            new FiniteStack<UndoRedoAction>(DEFAULT_HISTORY_SIZE);
         this.redoStack = 
            new FiniteStack<UndoRedoAction>(DEFAULT_HISTORY_SIZE);
         
         notifyOfWarning(buffer.toString());
      }
   }
   
   public void addUndoRedoListener(UndoRedoListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeUndoRedoListener(UndoRedoListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   private void notifyOfChange()
   {
      for (UndoRedoListener listener : this.listenerVec)
         listener.undoRedoStackChanged(this);
   }
   
   private void notifyOfWarning(String message)
   {
      for (UndoRedoListener listener : this.listenerVec)
         listener.undoRedoStackWarning(this, message);
   }
   
   public void actionDone(HistoryAction actionDone, HistoryAction undoAction)
   {
      if (actionDone == null || undoAction == null)
         throw new NullPointerException();
      
      this.undoStack.push(new UndoRedoAction(undoAction, actionDone));
      this.redoStack.clear();
      
      notifyOfChange();
   }
   
   public boolean canUndo()
   {
      return !this.undoStack.isEmpty();
   }
   
   public void undo()
   {
      if (!canUndo())
         return;
      
      UndoRedoAction topAction = this.undoStack.pop();
      this.redoStack.push(topAction);
      
      topAction.getUndoAction().run();
      notifyOfChange();
   }
   
   public boolean canRedo()
   {
      return !this.redoStack.isEmpty();
   }
   
   public void redo()
   {
      if (!canRedo())
         return;
      
      UndoRedoAction topAction = this.redoStack.pop();
      this.undoStack.push(topAction);
      
      topAction.getRedoAction().run();
      notifyOfChange();
   }
   
   private static class UndoRedoAction
   {
      private HistoryAction undoAction;
      private HistoryAction redoAction;
      
      public UndoRedoAction(HistoryAction undoAction, 
                            HistoryAction redoAction)
      {
         if (undoAction == null || redoAction == null)
            throw new NullPointerException();
         
         this.undoAction = undoAction;
         this.redoAction = redoAction;
      }
      
      public HistoryAction getUndoAction()
      {
         return this.undoAction;
      }
      
      public HistoryAction getRedoAction()
      {
         return this.redoAction;
      }
   }

   public void settingsChanged(SettingsChangedEvent event)
   {
      if (event == null)
         return;
      
      String key = event.getKey();
      if (SettingsKeys.HISTORY_SIZE_KEY.equals(key))
      {
         Object val = event.getNewValue();
         if (val instanceof Integer)
         {
            int historySize = (Integer)val;
            this.undoStack.setSize(historySize);
            this.redoStack.setSize(historySize);
            
            notifyOfChange();
         }
      }
   }
}
