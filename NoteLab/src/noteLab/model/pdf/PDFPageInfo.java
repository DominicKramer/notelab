/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2009, Dominic Kramer
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

package noteLab.model.pdf;

public class PDFPageInfo
{
   private PDFFileInfo fileInfo;
   private int pageNum;
   
   public PDFPageInfo(PDFFileInfo fileInfo, int pageNum)
   {
      if (fileInfo == null)
         throw new NullPointerException();
      
      int numPages = fileInfo.getPDFFile().getNumPages();
      if (pageNum < 1 || pageNum > numPages)
         throw new IllegalArgumentException("The page number "+pageNum+
                                            " must be positive" +
                                            " but less than or equal to "+
                                            numPages);
      
      this.fileInfo = fileInfo;
      this.pageNum = pageNum;
   }
   
   public PDFFileInfo getFileInfo()
   {
      return this.fileInfo;
   }
   
   public int getPageNum()
   {
      return this.pageNum;
   }
}
