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

package noteLab.util.io;

import java.io.IOException;
import java.io.InputStream;

public class StringInputStream extends InputStream
{
   private String text;
   private int curIndex;
   
   public StringInputStream(String text)
   {
      if (text == null)
         throw new NullPointerException();
      
      this.text = text;
      this.curIndex = 0;
   }
   
   @Override
   public int read() throws IOException
   {
      if (this.curIndex >= this.text.length())
         return -1;
      
      return this.text.charAt(this.curIndex++);
   }
}
