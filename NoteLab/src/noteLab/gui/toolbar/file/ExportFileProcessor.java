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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;

import noteLab.gui.chooser.filter.ImageFileFilter;
import noteLab.gui.main.MainFrame;
import noteLab.model.binder.Binder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.progress.ProgressEvent;
import noteLab.util.render.ImageRenderer2D;

public class ExportFileProcessor extends CanvasFileProcessor implements IIOWriteProgressListener
{
   public ExportFileProcessor(MainFrame frame)
   {
      super(frame);
   }
   
   public void processFileImpl(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      MainFrame mainFrame = getMainFrame();
      CompositeCanvas canvas = mainFrame.getCompositeCanvas();
      
      Binder binder = canvas.getBinder();
      float width = binder.getWidth();
      float height = binder.getHeight();
      
      BufferedImage image = 
         new BufferedImage( (int)width, (int)height, 
                            BufferedImage.TYPE_INT_RGB );
      ImageRenderer2D image2D = new ImageRenderer2D(image);
      image2D.setColor(Color.WHITE);
      image2D.fillRectangle(0, 0, width, height);
      
      synchronized(canvas)
      {
         mainFrame.setMessage("Exporting the session requires momentarily disabling the canvas.", 
                              Color.BLACK);
         canvas.setEnabled(false);
         canvas.renderInto(image2D);
         canvas.setEnabled(true);
      }
      
      String ext = getExtension(file);
      File formatFile = getFormattedName(file);
      
      String svgExt = InfoCenter.getSVGExt();
      String svgzExt = InfoCenter.getZippedSVGExt();
      
      if (ext.equalsIgnoreCase(svgExt))
      {
         saveAsSVG(file, "."+svgExt, 
                   false, // Don't zip the file 
                   true,  // Report progress to the user 
                   "Exporting the session");
         return;
      }
      else if (ext.equalsIgnoreCase(svgzExt))
      {
         saveAsSVG(file, "."+svgzExt, 
                   true, // Zip the file 
                   true, // Report progress to the user
                   "Exporting the session");
         return;
      }
      
      StringBuffer messageBuffer = new StringBuffer("Exporting to the file '");
      messageBuffer.append(formatFile.getAbsolutePath());
      
      try
      {
         String errorText = "There are no writers available to write images with " +
                            "the extension '"+ext+"'";
         
         Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(ext);
         if (!writers.hasNext())
            throw new Exception(errorText);
         
         ImageWriter writer = writers.next();
         if (writer == null)
            throw new NullPointerException(errorText);
         
         ImageOutputStream output = ImageIO.createImageOutputStream(formatFile);
         writer.setOutput(output);
         writer.addIIOWriteProgressListener(this);
         writer.write(image);
         writer.removeIIOWriteProgressListener(this);
         writer.dispose();
         
         messageBuffer.append("' completed successfully.");
         mainFrame.setMessage(messageBuffer.toString(), Color.BLACK);
      }
      catch (Throwable throwable)
      {
         notifyOfThrowable(throwable);
         
         messageBuffer.append("' failed.");
         mainFrame.setMessage(messageBuffer.toString(), Color.RED);
      }
   }

   public File getFormattedName(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      String ext = getExtension(file);
      
      // if a valid extension isn't given, use a default
      if (ext == null)
      {
         String defaultExt = "png";
         ext = defaultExt;
         file = new File(file.getAbsolutePath()+"."+ext);
      }
      
      return file;
   }
   
   private String getExtension(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      String ext = null;
      String path = file.getPath().toLowerCase();
      for (String fileExt : ImageFileFilter.EXT_ARR)
      {
         if (path.endsWith(fileExt.toLowerCase()))
         {
            ext = fileExt;
            break;
         }
      }
      
      return ext;
   }

   public void imageComplete(ImageWriter source)
   {
      MainFrame frame = getMainFrame();
      frame.progressOccured(new ProgressEvent(null, null, null, false, 100, true));
   }
   
   public void imageProgress(ImageWriter source, float percentageDone)
   {
      MainFrame frame = getMainFrame();
      frame.progressOccured(new ProgressEvent(null, null, null, false, 
                                              (int)(percentageDone), false));
   }
   
   public void imageStarted(ImageWriter source, int imageIndex)
   {
   }
   
   public void thumbnailComplete(ImageWriter source)
   {
   }
   
   public void thumbnailProgress(ImageWriter source, float percentageDone)
   {
   }
   
   public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex)
   {
   }
   
   public void writeAborted(ImageWriter source)
   {
   }
}
