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

package noteLab.util.io.jarnal;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.model.Page;
import noteLab.model.binder.Binder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.io.FileLoader;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;

import org.xml.sax.SAXException;

public class JarnalFileLoader implements JarnalPageLoadedListener, FileLoader
{
   private static final String CONFIG_ENTRY_NAME = "jarnal.conf";
   
   private static final String NUM_PAGES_KEY = "pageNumber";
   private static final String SCALE_KEY = "scale";
   
   private ZipFile zipFile;
   private NoteLabFileLoadedListener listener;
   
   private int numPages;
   private int numLoaded;
   
   private float scale;
   
   private boolean usesBgImage;
   
   private CompositeCanvas canvas;
   
   private StringBuffer messageBuffer;
   
   public JarnalFileLoader(File file, 
                           NoteLabFileLoadedListener listener) 
                              throws IOException, 
                                     ParserConfigurationException, 
                                     SAXException, 
                                     IOException
   {
      this.canvas = new CompositeCanvas(1);
      this.listener = listener;
      this.zipFile = new ZipFile(file);
      this.usesBgImage = false;
      this.messageBuffer = new StringBuffer();
      
      Properties configProps = getJarnalConfig();
      Object foundOb = configProps.get(NUM_PAGES_KEY);
      
      this.numLoaded = 0;
      this.numPages = 0;
      if (foundOb != null)
      {
         String numPagesStr = foundOb.toString();
         try
         {
            this.numPages = Integer.parseInt(numPagesStr);
         }
         catch (NumberFormatException e)
         {
            this.numPages = 0;
         }
      }
      
      this.scale = 1;
      foundOb = configProps.get(SCALE_KEY);
      if (foundOb != null)
      {
         try
         {
            this.scale = Float.parseFloat(foundOb.toString());
         }
         catch (NumberFormatException e)
         {
            this.scale = 1;
         }
      }
   }
   
   private Properties getJarnalConfig() throws IOException
   {
      ZipEntry configEntry = this.zipFile.getEntry(CONFIG_ENTRY_NAME);
      if (configEntry == null)
         return new Properties();
      
      Properties configProps = new Properties();
      configProps.load(this.zipFile.getInputStream(configEntry));
      
      return configProps;
   }

   public void pageLoaded(Page page, int pageNumber, 
                          boolean usesBgImage, String message)
   {
      this.numLoaded++;
      this.canvas.getBinder().addPage(page);
      this.usesBgImage = this.usesBgImage || usesBgImage;
      
      if (message != null)
         this.messageBuffer.append(message);
      
      if (this.numLoaded == this.numPages)
      {
         // When the canvas is first constructed it is given a blank 
         // page as the first page because the canvas must have at least 
         // one page.  The page is not needed so it will be removed.
         Binder binder = this.canvas.getBinder();
         binder.setCurrentPage(0);
         binder.removeCurrentPage();
         
         this.canvas.zoomTo(this.scale);
         this.listener.noteLabFileLoaded(this.canvas, 
                                         this.messageBuffer.toString());
         
         ImageIcon icon = DefinedIcon.dialog_info.
                                         getIcon(GuiSettingsConstants.BUTTON_SIZE);
         
         String appName = InfoCenter.getAppName();
         
         StringBuffer messageBuffer = new StringBuffer();
         messageBuffer.append(appName);
         messageBuffer.append(" has tried its best to guess the correct paper type.  ");
         if (this.usesBgImage)
         {
            messageBuffer.append("Also, ");
            messageBuffer.append(appName);
            messageBuffer.append(" doesn't support background images.");
         }
         
         JOptionPane.showMessageDialog(new JFrame(), 
                                       messageBuffer.toString(), 
                                       "Notice", 
                                       JOptionPane.INFORMATION_MESSAGE, icon);
      }
   }

   public void loadFile() throws ParserConfigurationException, 
                                 SAXException, 
                                 IOException
   {
      for (int i=0; i<this.numPages; i++)
         SwingUtilities.invokeLater(new JarnalPageLoader(this.zipFile, i, 1, this));
   }

   public void pageInvalid(int pageNumber, Exception e)
   {
      this.numLoaded++;
      
      ImageIcon icon = DefinedIcon.dialog_error.
                          getIcon(GuiSettingsConstants.BUTTON_SIZE);
      
      String message = "An error occured while loading page "+(pageNumber+1)+
                       ".  The message returned was:  "+e.getMessage();
      
      JOptionPane.showMessageDialog(new JFrame(), 
                                    message, 
                                    "Error", 
                                    JOptionPane.WARNING_MESSAGE, icon);
   }
}
