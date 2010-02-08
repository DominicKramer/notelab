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

package noteLab.util.undoRedo.action;

import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.geom.Transformable;

public class TransformAction extends CanvasAction
{
   private Transformable transformer;
   private float xScale;
   private float yScale;
   private float xTrans;
   private float yTrans;
   
   public TransformAction(CompositeCanvas canvas, Transformable transformer, 
                          float xScale, float yScale, 
                          float xTrans, float yTrans)
   {
      super(canvas);
      
      this.transformer = transformer;
      
      this.xScale = xScale;
      this.yScale = yScale;
      this.xTrans = xTrans;
      this.yTrans = yTrans;
   }
   
   public void run()
   {
      this.transformer.scaleTo(this.xScale, this.yScale);
      this.transformer.translateTo(this.xTrans, this.yTrans);
      
      getCompositeCanvas().redraw();
   }
}
