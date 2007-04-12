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

import javax.imageio.ImageIO;

import noteLab.gui.chooser.filter.ImageFileFilter;
import noteLab.gui.main.MainFrame;
import noteLab.model.binder.Binder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.render.ImageRenderer2D;

public class ExportFileProcessor extends CanvasFileProcessor
{
   public ExportFileProcessor(MainFrame frame)
   {
      super(frame);
   }
   
   public void processFile(File file)
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
         saveAsSVG(file, "."+svgExt, false);
         return;
      }
      else if (ext.equalsIgnoreCase(svgzExt))
      {
         saveAsSVG(file, "."+svgzExt, true);
         return;
      }
      
      try
      {
         ImageIO.write(image, ext, formatFile);
         mainFrame.setMessage("Exporting completed successfully.", Color.BLACK);
      }
      catch (Throwable throwable)
      {
         notifyOfThrowable(throwable);
         mainFrame.setMessage("Exporting completed unsuccessfully.", Color.RED);
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
}
