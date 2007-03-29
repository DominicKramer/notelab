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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import noteLab.util.geom.Bounded;

public class ImageRenderer2D extends SwingRenderer2D
{
   public ImageRenderer2D(BufferedImage image)
   {
      super();
      
      if (image == null)
         throw new NullPointerException();
      
      Graphics2D g2d = image.createGraphics();
      g2d.setBackground(Color.WHITE);
      g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
      setSwingGraphics(g2d, true);
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      return true;
   }
}
