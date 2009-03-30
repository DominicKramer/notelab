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

import javax.swing.JButton;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.util.CopyReady;
import noteLab.util.copy.CopyStateListener;
import noteLab.util.copy.CutCopyPasteReady;

public class CutCopyPasteToolBar<E extends Object & CopyReady<E>> 
                                      implements ActionListener, 
                                                 GuiSettingsConstants, 
                                                 CopyStateListener
{
   private static final String CUT_CMMD = "Cut";
   private static final String COPY_CMMD = "Copy";
   private static final String PASTE_CMMD = "Paste";
   
   private CutCopyPasteReady<E> handler;
   private E copiedItem;
   
   private JButton cutButton;
   private JButton copyButton;
   private JButton pasteButton;
   
   public CutCopyPasteToolBar(CutCopyPasteReady<E> handler)
   {
      if (handler == null)
         throw new NullPointerException();
      
      this.cutButton = 
         new JButton(DefinedIcon.cut.getIcon(BUTTON_SIZE));
      this.cutButton.addActionListener(this);
      this.cutButton.setActionCommand(CUT_CMMD);
      this.cutButton.setEnabled(false);
      
      this.copyButton = 
         new JButton(DefinedIcon.copy_stroke.getIcon(BUTTON_SIZE));
      this.copyButton.addActionListener(this);
      this.copyButton.setActionCommand(COPY_CMMD);
      this.copyButton.setEnabled(false);
      
      this.pasteButton = 
         new JButton(DefinedIcon.paste.getIcon(BUTTON_SIZE));
      this.pasteButton.addActionListener(this);
      this.pasteButton.setActionCommand(PASTE_CMMD);
      this.pasteButton.setEnabled(false);
      
      this.handler = handler;
      this.handler.addCopyStateListener(this);
   }
   
   /**
    * Returns the last item that was copied or 
    * <code>null</code> if no item has been 
    * copied yet.
    * 
    * @return The current copied item or 
    *         <code>null</code> if non exists.
    */
   public E getCopiedItem()
   {
      return this.copiedItem;
   }
   
   public void appendTo(JToolBar toolbar)
   {
      if (toolbar == null)
         throw new NullPointerException();
      
      toolbar.add(this.cutButton);
      toolbar.add(this.copyButton);
      toolbar.add(this.pasteButton);
   }
   
   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      
      if (cmmd.equals(CUT_CMMD))
      {
         E copy = this.handler.cut();
         if (copy != null)
            this.copiedItem = copy;
      }
      else if (cmmd.equals(COPY_CMMD))
      {
         E copy = this.handler.copy();
         if (copy != null)
            this.copiedItem = copy;
      }
      else if (cmmd.equals(PASTE_CMMD) && this.copiedItem != null)
         this.handler.paste(this.copiedItem.getCopy());
      
      this.pasteButton.setEnabled(this.copiedItem != null);
   }
   
   public void setCutIcon(DefinedIcon icon)
   {
      if (icon == null)
         icon = DefinedIcon.cut;
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      this.cutButton.setIcon(icon.getIcon(size));
   }
   
   public void setCopyIcon(DefinedIcon icon)
   {
      if (icon == null)
         icon = DefinedIcon.copy_stroke;
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      this.copyButton.setIcon(icon.getIcon(size));
   }
   
   public void setPasteIcon(DefinedIcon icon)
   {
      if (icon == null)
         icon = DefinedIcon.paste;
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      this.pasteButton.setIcon(icon.getIcon(size));
   }
   
   public void copyStateChanged(boolean canCopy)
   {
      this.cutButton.setEnabled(canCopy);
      this.copyButton.setEnabled(canCopy);
   }
}
