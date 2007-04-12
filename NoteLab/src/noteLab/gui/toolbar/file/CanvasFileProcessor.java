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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.chooser.FileProcessor;
import noteLab.gui.main.MainFrame;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.render.SVGRenderer2D;
import noteLab.util.settings.SettingsUtilities;

public abstract class CanvasFileProcessor implements FileProcessor
{
   private MainFrame mainFrame;
   
   public CanvasFileProcessor(MainFrame frame)
   {
      if (frame == null)
         throw new NullPointerException();
      
      this.mainFrame = frame;
   }
   
   public MainFrame getMainFrame()
   {
      return this.mainFrame;
   }
   
   protected void saveAsSVG(File file, String ext, boolean zip)
   {
      saveAsSVG(getMainFrame(), file, ext, zip);
   }
   
   public static void saveAsSVG(MainFrame mainFrame, 
                                File file, 
                                String ext, 
                                boolean zip)
   {
      if (mainFrame == null || file == null || ext == null)
         throw new NullPointerException();
      
      String fullPath = file.getAbsolutePath();
      if (!fullPath.toLowerCase().endsWith(ext.toLowerCase()))
      {
         fullPath += ext;
         file = new File(fullPath);
      }
      
      CompositeCanvas canvas = mainFrame.getCompositeCanvas();
      synchronized(canvas)
      {
         canvas.setEnabled(false);
         
         float zoomFactor = canvas.getZoomLevel();
         float unitScaleFactor = SettingsUtilities.getUnitScaleFactor();
         canvas.zoomTo(1);
         canvas.resizeTo(1/unitScaleFactor);
         boolean hasBeenSaved = false;
         
         try
         {
            if (!file.exists())
               file.createNewFile();
            
            OutputStream outStream = new FileOutputStream(file);
            if (zip)
               outStream = new GZIPOutputStream(outStream);
            
            SVGRenderer2D msvg2D = new SVGRenderer2D(canvas, outStream);
            canvas.renderInto(msvg2D);
            
            Exception error = msvg2D.getError();
            if (error != null)
               throw error;
            
            hasBeenSaved = true;
            
            if (ext.equalsIgnoreCase(InfoCenter.getFileExtension()))
               canvas.setFile(file);
         }
         catch (Throwable throwable)
         {
            notifyOfThrowable(throwable);
         }
         finally
         {
            canvas.resizeTo(unitScaleFactor);
            canvas.zoomTo(zoomFactor);
            if (hasBeenSaved)
               mainFrame.hasBeenSaved();
            
            canvas.setEnabled(true);
         }
      }
   }
   
   public static void notifyOfThrowable(Throwable throwable)
   {
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.dialog_error.getIcon(size);
      
      String title = "Error";
      String message = "";
      if (throwable == null)
         message = "The cause of the error is unknown.";
      else
         message = throwable.getClass().getSimpleName()+":  "+
                   throwable.getMessage();
      
      JOptionPane.showMessageDialog(new JFrame(), 
                                    message, 
                                    title, 
                                    JOptionPane.ERROR_MESSAGE, 
                                    icon);
   }
}
