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

package noteLab.util.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import noteLab.util.geom.Bounded;

public class PrinterRenderer2D extends SwingRenderer2D
{
   public PrinterRenderer2D(Graphics g, PageFormat format, 
                            float width, float height) 
                               throws PrinterException
   {
      if ( !(g instanceof Graphics2D) )
         throw new PrinterException(PrinterRenderer2D.class.getName()+
                                    "ERROR:  Printing cannot be completed"+
                                    "because the printer doesn't supported "+
                                    "two-dimensional printing.");
      
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(format.getImageableX(), format.getImageableY());
      
      double pageWidth = format.getImageableWidth();
      double pageHeight = format.getImageableHeight();
      g2d.scale(pageWidth/width, pageHeight/height);
      
      setSwingGraphics(g2d, true);
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      return true;
   }
}
