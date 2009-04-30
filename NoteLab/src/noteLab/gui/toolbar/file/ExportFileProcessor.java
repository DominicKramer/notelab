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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.chooser.filter.ImageFileFilter;
import noteLab.gui.main.MainFrame;
import noteLab.model.Page;
import noteLab.model.binder.Binder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.progress.ProgressEvent;
import noteLab.util.render.EmptyRenderer2D;
import noteLab.util.render.ImageRenderer2D;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.render.SwingRenderer2D.RenderMode;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class ExportFileProcessor extends CanvasFileProcessor implements IIOWriteProgressListener
{
   private static final EmptyRenderer2D OVERLAY_RENDERER = 
                                           new EmptyRenderer2D();
   
   private static final String[] OVERWRITE_OPTIONS 
                                    = new String[]{"Never Overwrite", 
                                                   "Don't Overwrite Now", 
                                                   "Always Overwrite", 
                                                   "Overwrite Now"
                                                   };
   
   private static final int NEVER_OVERWRITE_OPTION = 0;
   private static final int DONT_OVERWRITE_NOW_OPTION = 1;
   private static final int OVERWRITE_ALWAYS_OPTION = 2;
   private static final int OVERWRITE_NOW_OPTION = 3;
   
   private String defaultExt;
   
   public ExportFileProcessor(MainFrame frame, String defaultExt)
   {
      super(frame);
      
      if (defaultExt == null)
         throw new NullPointerException();
      
      this.defaultExt = defaultExt;
   }
   
   public void processFileImpl(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      File formatFile = getFormattedName(file);
      String formatName = formatFile.getAbsolutePath().toLowerCase();
      
      String svgExt = InfoCenter.getSVGExt().toLowerCase();
      String svgzExt = InfoCenter.getZippedSVGExt().toLowerCase();
      
      if (formatName.endsWith(svgExt))
      {
         saveAsSVG(file, svgExt, 
                   false, // Don't zip the file 
                   true,  // Report progress to the user 
                   "Exporting the session");
         return;
      }
      else if (formatName.endsWith(svgzExt))
      {
         saveAsSVG(file, svgzExt, 
                   true, // Zip the file 
                   true, // Report progress to the user
                   "Exporting the session");
         return;
      }
      
      StringBuffer messageBuffer = new StringBuffer("Exporting to the file '");
      messageBuffer.append(formatFile.getAbsolutePath());
      
      MainFrame mainFrame = getMainFrame();
      CompositeCanvas canvas = mainFrame.getCompositeCanvas();
      
      try
      {
         String pdfExt = InfoCenter.getPDFExtension().toLowerCase();
         if (formatName.endsWith(pdfExt))
         {
            mainFrame.progressOccured(new ProgressEvent(
                                             messageBuffer.toString(), 
                                             "", "", true, 0, false));
            
            // Process the PDF file
            processPdfFile(formatFile, mainFrame, canvas, messageBuffer);
            
            messageBuffer.append("' completed successfully.");
            mainFrame.progressOccured(new ProgressEvent(
                                             messageBuffer.toString(), 
                                             "", "", false, 100, true));
         }
         else
         {
            messageBuffer.append("' completed successfully.");
            processImageFile(formatFile, mainFrame, canvas, messageBuffer);
         }
         
         mainFrame.setMessage(messageBuffer.toString(), Color.BLACK);
      }
      catch (Throwable throwable)
      {
         notifyOfThrowable(throwable);
         
         messageBuffer.append("' failed.");
         mainFrame.setMessage(messageBuffer.toString(), Color.RED);
         
         // Make sure the progress bar isn't set to be indeterminate
         mainFrame.progressOccured(new ProgressEvent(messageBuffer.toString(), 
                                                     "", 
                                                     "", 
                                                     false, 
                                                     100, 
                                                     true));
      }
   }
   
   private void processPdfFile(File formatFile, 
                               MainFrame mainFrame, 
                               CompositeCanvas canvas, 
                               StringBuffer messageBuffer) 
                                  throws DocumentException, 
                                         FileNotFoundException, 
                                         IOException
   {
      synchronized(canvas)
      {
         mainFrame.setMessage("Exporting the session requires momentarily disabling the canvas.", 
                              Color.BLACK);
         canvas.setEnabled(false);
         
         Binder binder = canvas.getBinder();
         
         if (!formatFile.exists())
            formatFile.createNewFile();
         
         // Create the document
         Document doc = new Document();
         
         // Create a writer
         PdfWriter pdfWriter =
                      PdfWriter.
                         getInstance(doc,
                                     new FileOutputStream(formatFile));
         
         Rectangle pageSize = pdfWriter.getPageSize();
         float width = pageSize.getWidth();
         float height = pageSize.getHeight();
         
         // Open the document
         doc.open();
         
         // Configure the fonts
         DefaultFontMapper mapper = new DefaultFontMapper();
         FontFactory.registerDirectories();
         
         // Get the content-byte to work with
         PdfContentByte content = pdfWriter.getDirectContent();
         
         for (Page page : binder)
         {
            renderPage(width, height, content, mapper, page);
            doc.newPage();
         }
         
         // Close the document
         doc.close();
         
         canvas.setEnabled(true);
      }
   }
   
   private void renderPage(float width, float height, 
                           PdfContentByte content, 
                           DefaultFontMapper mapper, 
                           Page page)
   {
      // Make a template onto which data is written
      PdfTemplate template = content.createTemplate(width, height);
      
      // Get the template's Graphics2D object
      Graphics2D g2d = template.createGraphics(width, height, mapper);
      
      // Configure the template
      template.setWidth(width);
      template.setHeight(height);
      
      // Render the canvas
      g2d.scale(width/page.getWidth(), 
                height/page.getHeight());
      
      SwingRenderer2D pdf2D = new SwingRenderer2D();
      pdf2D.setSwingGraphics(g2d, RenderMode.Appearance);
      page.renderInto(pdf2D);
      
      // Dispose of the graphics object
      g2d.dispose();
      
      // Add the template to the content
      content.addTemplate(template, 0, 0);
   }
   
   private void processImageFile(File formatFile, 
                                 MainFrame mainFrame, 
                                 CompositeCanvas canvas, 
                                 StringBuffer messageBuffer) 
                                    throws IOException
   {
      String ext = getExtension(formatFile);
      String errorText = "There are no writers available to write images with " +
                         "the extension '"+ext+"'";
      
      Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(ext);
      if (!writers.hasNext())
         throw new IOException(errorText);
      
      ImageWriter writer = writers.next();
      if (writer == null)
         throw new NullPointerException(errorText);
      
      writer.addIIOWriteProgressListener(this);
      
      synchronized(canvas)
      {
         mainFrame.setMessage("Exporting the session requires momentarily disabling the canvas.", 
                              Color.BLACK);
         canvas.setEnabled(false);
         
         Binder binder = canvas.getBinder();
         
         try
         {
            float width = binder.getWidth();
            float height = binder.getHeight();
            
            BufferedImage image = 
               new BufferedImage( (int)width, (int)height, 
                                  BufferedImage.TYPE_INT_RGB );
            ImageRenderer2D image2D = new ImageRenderer2D(image);
            image2D.setColor(Color.WHITE);
            image2D.fillRectangle(0, 0, width, height);
            
            canvas.renderInto(OVERLAY_RENDERER, image2D, false);
            
            ImageOutputStream output = ImageIO.createImageOutputStream(formatFile);
            writer.setOutput(output);
            writer.write(image);
         }
         catch (Throwable t)
         {
            int size = GuiSettingsConstants.BUTTON_SIZE;
            ImageIcon icon = DefinedIcon.dialog_error.getIcon(size);
            
            String title = "Warning";
            String message = "There is not enough memory to export to " +
                             "a single image.  Export each page to a " +
                             "separate image?";
            
            int result = JOptionPane.showConfirmDialog(new JFrame(), 
                                                       message, 
                                                       title, 
                                                       JOptionPane.YES_NO_OPTION, 
                                                       JOptionPane.INFORMATION_MESSAGE, 
                                                       icon);
            
            if (result == JOptionPane.YES_OPTION)
               writePages(binder, writer, mainFrame, formatFile);
         }
         
         writer.dispose();
         canvas.setEnabled(true);
      }
      
      writer.removeIIOWriteProgressListener(this);
      writer.dispose();
   }
   
   private void writePages(Binder binder, 
                           ImageWriter writer, 
                           MainFrame mainFrame, 
                           File file) throws IOException
   {
      String path = file.getAbsolutePath();
      String ext = getExtension(file);
      path = path.substring(0, path.length()-ext.length()-1);
      
      float width;
      float height;
      BufferedImage image;
      ImageRenderer2D image2D;
      File pageFile;
      
      boolean alwaysOverwrite = false;
      boolean neverOverwrite = false;
      int result;
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.dialog_question.getIcon(size);
      
      int pageNum = 1;
      int numPages = binder.getNumberOfPages();
      
      for (Page page : binder)
      {
         width = page.getWidth();
         height = page.getHeight();
         
         image = new BufferedImage( (int)width, (int)height, 
                                    BufferedImage.TYPE_INT_RGB );
         image2D = new ImageRenderer2D(image);
         image2D.setColor(Color.WHITE);
         image2D.fillRectangle(0, 0, width, height);
         
         page.renderInto(image2D);
         
         mainFrame.setMessage("Exporting page "+
                              pageNum+" of "+numPages, Color.BLACK);
         
         pageFile = new File(path+"_page"+pageNum+"."+ext);
         
         result = OVERWRITE_NOW_OPTION;
         if (!neverOverwrite && !alwaysOverwrite && pageFile.exists())
         {
            result = 
                JOptionPane.showOptionDialog(new JFrame(), 
                                             "The file "+
                                             pageFile.getAbsolutePath()+
                                             " already exists.  Overwrite?", 
                                             "Overwrite", 
                                             JOptionPane.YES_NO_CANCEL_OPTION, 
                                             JOptionPane.QUESTION_MESSAGE, 
                                             icon, 
                                             OVERWRITE_OPTIONS, 
                                             null);
         }
         
         if (result == NEVER_OVERWRITE_OPTION)
            neverOverwrite = true;
         else if (result == OVERWRITE_ALWAYS_OPTION)
            alwaysOverwrite = true;
         
         if (!neverOverwrite || result == DONT_OVERWRITE_NOW_OPTION)
         {
            if (alwaysOverwrite || result == OVERWRITE_NOW_OPTION)
            {
               writer.setOutput(ImageIO.createImageOutputStream(pageFile));
               writer.write(image);
            }
         }
         
         pageNum++;
      }
   }
   
   public File getFormattedName(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      String ext = getExtension(file);
      
      // if a valid extension isn't given, use a default
      if (ext == null)
         file = new File(file.getAbsolutePath()+this.defaultExt);
      
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
      
      // If the file extension is 'null' 
      // check if the file is a PDF file.
      if (ext == null)
      {
         String pdfExt = InfoCenter.getPDFExtension();
         if (path.endsWith(pdfExt.toLowerCase()))
            ext = pdfExt;
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
