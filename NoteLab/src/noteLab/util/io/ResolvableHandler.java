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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ResolvableHandler extends DefaultHandler
{
   private StringBuffer messageBuffer;
   
   public ResolvableHandler()
   {
      this.messageBuffer = new StringBuffer();
   }
   
   public StringBuffer getMessageBuffer()
   {
      return this.messageBuffer;
   }
   
   @Override
   public void warning(SAXParseException e)
   {
      appendMessage(e, "A warning");
   }
   
   @Override
   public void error(SAXParseException e)
   {
      appendMessage(e, "An error");
   }
   
   @Override
   public void fatalError(SAXParseException e)
   {
      appendMessage(e, "A fatal error");
   }
   
   @Override
   public InputSource resolveEntity(String publicId, 
                                    String systemId)
                                              throws SAXException, 
                                                     IOException
   {
      if (systemId == null)
         return null;
      
      systemId = systemId.toLowerCase();
      
      if (systemId.contains("www.w3.org") || systemId.contains("www.w3c.org"))
      {
         if (systemId.contains("dtd"))
         {
            InputStream stream = 
                           ClassLoader.
                              getSystemResourceAsStream(
                                 "noteLab/dtd/svg10.dtd");
            
            return new InputSource(stream);
         }
      }
      
      return null;
   }
   
   private void appendMessage(SAXParseException exception, String prefix)
   {
      this.messageBuffer.append(prefix);
      if (exception == null)
      {
         this.messageBuffer.append(" has been encountered.  ");
         this.messageBuffer.append("The exact error is unknown.");
      }
      else
      {
         this.messageBuffer.append(" has been encountered on line ");
         this.messageBuffer.append(exception.getLineNumber());
         this.messageBuffer.append(" at character ");
         this.messageBuffer.append(exception.getColumnNumber());
         this.messageBuffer.append(".  The error is of type ");
         this.messageBuffer.append(exception.getClass().getName());
         this.messageBuffer.append(" and the message returned is '");
         this.messageBuffer.append(exception.getMessage());
         this.messageBuffer.append("'.  ");
      }
   }
}
