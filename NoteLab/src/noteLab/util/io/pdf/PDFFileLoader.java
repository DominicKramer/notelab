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

package noteLab.util.io.pdf;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import noteLab.model.Page;
import noteLab.model.Paper.PaperType;
import noteLab.model.binder.FlowBinder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.model.pdf.PDFFileInfo;
import noteLab.model.pdf.PDFPageInfo;
import noteLab.util.geom.unit.Unit;
import noteLab.util.io.FileLoader;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;
import noteLab.util.settings.SettingsUtilities;

import org.xml.sax.SAXException;

public class PDFFileLoader implements FileLoader
{
   private File file;
   private NoteLabFileLoadedListener listener;
   
   public PDFFileLoader(File pdfFile, NoteLabFileLoadedListener listener)
   {
      if (pdfFile == null || listener == null)
         throw new NullPointerException();
      
      this.file = pdfFile;
      this.listener = listener;
   }

   public void loadFile() throws ParserConfigurationException, 
                                 SAXException,
                                 IOException
   {
      PDFFileInfo pdfFileInfo = new PDFFileInfo(this.file);
      
      int res = Unit.getScreenResolution();
      
      int numPages = pdfFileInfo.getPDFFile().getNumPages();
      Page[] pages = new Page[numPages];
      
      for (int i=0; i<pages.length; i++)
      {
         pages[i] = new Page(PaperType.Plain, 1, 1, 
                             res, 
                             SettingsUtilities.getUnitScaleFactor());
         // Note:  Pages are numbered starting at 1
         pages[i].getPaper().
                     setPDFPageInfo(new PDFPageInfo(pdfFileInfo, i+1));
      }
      
      FlowBinder binder = new FlowBinder(1, 1, pages);
      CompositeCanvas canvas = new CompositeCanvas(binder, 1);
      
      // This should not be necessary
      //for (Page page : binder)
      //   page.getPaper().setPaperType(PaperType.Plain);
      
      this.listener.noteLabFileLoaded(canvas, "");
   }
}
