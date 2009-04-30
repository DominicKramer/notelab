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

package noteLab.gui.chooser.filter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import noteLab.util.InfoCenter;

public class SupportedFileFilter extends FileFilter
{
   private static final String DESC = "Supported Files (*"+
                                      InfoCenter.getFileExtension()+
                                      " ["+InfoCenter.getAppName()+
                                      " files], *"+
                                      InfoCenter.getJarnalExtension()+
                                      " [Jarnal files]), *"+
                                      InfoCenter.getPDFExtension()+
                                      " [PDF files])";
   
   private NoteLabFileFilter nativeFilter;
   private JarnalFileFilter  jarnalFilter;
   private PDFFileFilter     pdfFilter;
   
   public SupportedFileFilter()
   {
      this.nativeFilter = new NoteLabFileFilter();
      this.jarnalFilter = new JarnalFileFilter();
      this.pdfFilter = new PDFFileFilter();
   }
   
   @Override
   public boolean accept(File f)
   {
      return (this.nativeFilter.accept(f) || 
                 this.jarnalFilter.accept(f) || 
                    this.pdfFilter.accept(f));
   }

   @Override
   public String getDescription()
   {
      return DESC;
   }
}
