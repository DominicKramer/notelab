/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2010, Dominic Kramer
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

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;

public class LoggedSwingRenderer2D extends SwingRenderer2D
{
   private boolean modified;
   
   public LoggedSwingRenderer2D()
   {
      super();
      resetModifiedFlag();
   }
   
   public void resetModifiedFlag()
   {
      this.modified = false;
   }
   
   public boolean hasBeenModified()
   {
      return this.modified;
   }
   
   @Override
   public void drawLine(FloatPoint2D pt1, FloatPoint2D pt2)
   {
      super.drawLine(pt1, pt2);
      this.modified = true;
   }

   @Override
   public void drawPath(Path path)
   {
      super.drawPath(path);
      this.modified = true;
   }

   @Override
   public void drawRectangle(float x, float y, float width, float height)
   {
      super.drawRectangle(x, y, width, height);
      this.modified = true;
   }

   @Override
   public void fillRectangle(float x, float y, float width, float height)
   {
      super.fillRectangle(x, y, width, height);
      this.modified = true;
   }
}
