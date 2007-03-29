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

package noteLab.gui.toolbar.file;

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.main.MainFrame;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.io.FileLoader;
import noteLab.util.io.jarnal.JarnalFileLoader;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;
import noteLab.util.io.noteLab.NoteLabFileLoader;

public class OpenFileProcessor 
                extends CanvasFileProcessor 
                           implements NoteLabFileLoadedListener
{
   public OpenFileProcessor(MainFrame frame)
   {
      super(frame);
   }
   
   public void processFile(File file)
   {
      try
      {
         if (file == null)
            throw new IOException("No file was selected.");
         
         String nativeExt = InfoCenter.getFileExtension().toLowerCase();
         String jarnalExt = InfoCenter.getJarnalExtension().toLowerCase();
         String path = file.getPath().toLowerCase();
         
         FileLoader loader = null;
         if (path.endsWith(nativeExt))
            loader = new NoteLabFileLoader(file, this);
         else if (path.endsWith(jarnalExt))
            loader = new JarnalFileLoader(file, this);
         
         if (loader == null)
            throw new IOException("The file '"+file+
                                  "' is of a type not supported by "+
                                  InfoCenter.getAppName());
         
         loader.loadFile();
      }
      catch (Throwable throwable)
      {
         notifyOfThrowable(throwable);
      }
   }
   
   public void noteLabFileLoaded(CompositeCanvas canvas, String message)
   {
      if (message != null && message.length() > 0)
      {
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon icon = DefinedIcon.dialog_info.getIcon(size);
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       message, 
                                       "Notice", 
                                       JOptionPane.INFORMATION_MESSAGE, 
                                       icon);
      }
      
      new MainFrame(canvas).setVisible(true);
   }

   public File getFormattedName(File file)
   {
      return file;
   }
}
