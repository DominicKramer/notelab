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

package noteLab.model.pdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;

public class PDFFileInfo
{
   private File source;
   private PDFFile pdfFile;
   
   public PDFFileInfo(File source) throws IOException
   {
      if (source == null)
         throw new NullPointerException();
      
      this.source = source;
      RandomAccessFile randAccess = new RandomAccessFile(this.source, "r");
      FileChannel channel = randAccess.getChannel();
      ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,
                                         0, channel.size());
      this.pdfFile = new PDFFile(buffer);
   }
   
   public File getSource()
   {
      return this.source;
   }
   
   public PDFFile getPDFFile()
   {
      return this.pdfFile;
   }
}
