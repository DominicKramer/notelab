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

package noteLab.util.render;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;

import noteLab.model.Path;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.geom.Bounded;

public class QueuedRenderer2D extends Renderer2D
{
   private Queue<Runnable> commandQueue;
   private Renderer2D renderer;
   
   public QueuedRenderer2D(Renderer2D renderer)
   {
      if (renderer == null)
         throw new NullPointerException();
      
      this.renderer = renderer;
      this.commandQueue = new LinkedList<Runnable>();
   }
   
   public void replay()
   {
      Runnable cmmd;
      do 
      {
         cmmd = this.commandQueue.poll();
         if (cmmd != null)
            cmmd.run();
         
      } while (cmmd != null);
   }
   
   public void setRenderer(Renderer2D renderer)
   {
      if (renderer == null)
         throw new NullPointerException();
      
      this.renderer = renderer;
      this.commandQueue.clear();
   }
   
   private void addCommand(Runnable cmmd)
   {
      if (cmmd == null)
         throw new NullPointerException();
      
      this.commandQueue.offer(cmmd);
   }
   
   @Override
   public void tryRenderBoundingBox(final Bounded bounded)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.tryRenderBoundingBox(bounded);
         }
      });
   }
   
   @Override
   public void beginGroup(final Renderable renderable, final String desc, 
                          final float xScaleLevel, final float yScaleLevel)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.beginGroup(renderable, desc, xScaleLevel, yScaleLevel);
         }
      });
   }
   
   @Override
   public void endGroup(final Renderable renderable)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.endGroup(renderable);
         }
      });
   }
   
   @Override
   protected void beginGroupImpl(Renderable renderable, String desc,
                                 float xScaleFactor, float yScaleFactor)
   {
      this.renderer.beginGroupImpl(renderable, desc, 
                                   xScaleFactor, yScaleFactor);
   }
   
   @Override
   protected void endGroupImpl(Renderable renderable)
   {
      this.renderer.endGroupImpl(renderable);
   }
   
   @Override
   public void drawLine(final FloatPoint2D pt1, final FloatPoint2D pt2)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.drawLine(pt1, pt2);
         }
      });
   }
   
   @Override
   public void drawPath(final Path path)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.drawPath(path);
         }
      });
   }
   
   @Override
   public void drawRectangle(final float x, final float y, 
                             final float width, final float height)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.drawRectangle(x, y, width, height);
         }
      });
   }
   
   @Override
   public void fillRectangle(final float x, final float y, 
                             final float width, final float height)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.fillRectangle(x, y, width, height);
         }
      });
   }
   
   @Override
   public void finish()
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.finish();
         }
      });
   }
   
   @Override
   public void translate(final float x, final float y)
   {
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.translate(x, y);
         }
      });
   }
   
   @Override
   public float getLineWidth()
   {
      return this.renderer.getLineWidth();
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      return this.renderer.isInClipRegion(bounded);
   }
   
   @Override
   public boolean isCompletelyInClipRegion(Bounded bounded)
   {
      return this.renderer.isCompletelyInClipRegion(bounded);
   }
   
   @Override
   public void setColor(final Color color)
   {
      this.renderer.setColor(color);
      
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.setColor(color);
         }
      });
   }
   
   @Override
   public Color getColor()
   {
      return this.renderer.getColor();
   }
   
   @Override
   public void setLineWidth(final float width)
   {
      this.renderer.setLineWidth(width);
      
      addCommand(new Runnable()
      {
         public void run()
         {
            renderer.setLineWidth(width);
         }
      });
   }
}
