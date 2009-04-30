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
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ProgressFrame;
import noteLab.gui.chooser.FileProcessor;
import noteLab.gui.main.MainFrame;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.io.FileLoader;
import noteLab.util.io.jarnal.JarnalFileLoader;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;
import noteLab.util.io.noteLab.NoteLabFileLoader;
import noteLab.util.io.pdf.PDFFileLoader;
import noteLab.util.progress.ProgressEvent;
import noteLab.util.progress.ProgressListener;
import noteLab.util.progress.Progressive;

public class OpenFileProcessor 
                implements FileProcessor, 
                           NoteLabFileLoadedListener, 
                           Progressive
{
   private File file;
   private Vector<ProgressListener> listenerVec;
   
   public OpenFileProcessor()
   {
      this.file = null;;
      this.listenerVec = new Vector<ProgressListener>();
   }
   
   public File getLastFileProcessed()
   {
      return this.file;
   }
   
   public void processFile(File file)
   {
      try
      {
         if (file == null)
            throw new IOException("No file was selected.");
         
         this.file = file;
         
         String nativeExt = InfoCenter.getFileExtension().toLowerCase();
         String jarnalExt = InfoCenter.getJarnalExtension().toLowerCase();
         String pdfExt = InfoCenter.getPDFExtension().toLowerCase();
         String path = file.getPath().toLowerCase();
         
         FileLoader loader = null;
         if (path.endsWith(nativeExt))
            loader = new NoteLabFileLoader(file, this);
         else if (path.endsWith(jarnalExt))
            loader = new JarnalFileLoader(file, this);
         else if (path.endsWith(pdfExt))
            loader = new PDFFileLoader(file, this);
         
         if (loader == null)
            throw new IOException("The file '"+file+
                                  "' is of a type not supported by "+
                                  InfoCenter.getAppName());
         
         ProgressEvent event = 
                          new ProgressEvent("Opening file "+file.getName(),
                                            null, null, true, 0, false);
         ProgressFrame frame = new ProgressFrame("", true);
         frame.progressOccured(event);
         addProgressListener(frame);
         
         loader.loadFile();
      }
      catch (Throwable throwable)
      {
         CanvasFileProcessor.notifyOfThrowable(throwable);
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
      
      for (ProgressListener listener : this.listenerVec)
            listener.progressOccured(new ProgressEvent(null,
                                                       "Complete", 
                                                       null, 
                                                       true,
                                                       0,
                                                       true));
      this.listenerVec.clear();
      
      processCanvasLoaded(canvas);
   }
   
   protected void processCanvasLoaded(CompositeCanvas canvas)
   {
      MainFrame frame = new MainFrame(canvas);
      frame.setVisible(true);
      frame.hasBeenSaved();
   }

   public File getFormattedName(File file)
   {
      return file;
   }

   public void addProgressListener(ProgressListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }

   public void removeProgressListener(ProgressListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
}
