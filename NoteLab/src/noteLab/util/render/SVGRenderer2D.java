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
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.binder.Binder;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.unit.Unit;
import noteLab.util.io.noteLab.NoteLabFileConstants;

public class SVGRenderer2D extends Renderer2D implements NoteLabFileConstants
{
   private static final String XML_SPEC = 
                                  "<?xml version=\"1.0\" standalone=\"no\"?>";
   private static final String SVG_SPEC = 
                                    "<!DOCTYPE svg PUBLIC \"-//W3C//DTD " +
                                    "SVG 1.0//EN\" \"http://www.w3.org/" +
                                    "Graphics/SVG/1.0/DTD/svg10.dtd\">";
   private static final String SVG_VERSION_ATTR = "version=\"1.0\"";
   private static final String SVG_NAMESPACE_ATTR = 
                                  "xmlns=\"http://www.w3.org/2000/svg\"";
   
   private float width;
   private String color;
   
   private float originX;
   private float originY;
   private TranslateStack transStack;
   
   private OutputStream outStream;
   
   private Exception curError;
   
   public SVGRenderer2D(Bounded boundsDesc, 
                        OutputStream outStream)
   {
      if (outStream == null)
         throw new NullPointerException();
      
      this.width = 1;
      this.color = "black";
      this.outStream = outStream;
      
      this.curError = null;
      
      this.originX = 0;
      this.originY = 0;
      
      Rectangle2D bounds = boundsDesc.getBounds2D();
      this.transStack = new TranslateStack();
      
      float boundWidthPx = (float)bounds.getWidth();
      float boundHeightPx = (float)bounds.getHeight();
      
      initializeCode(boundWidthPx, boundHeightPx);
   }
   
   private void initializeCode(float boundWidthPx, float boundHeightPx)
   {
      append(XML_SPEC);
      append(SVG_SPEC);
      
      append("<");
      append(SVG_TAG_NAME);
      append(" ");
      append(WIDTH_NAME);
      append("=\"");
      append(""+boundWidthPx);
      append(PIXEL_UNIT_NAME);
      append("\" ");
      append(HEIGHT_NAME);
      append("=\"");
      append(""+boundHeightPx);
      append(PIXEL_UNIT_NAME);
      append("\" ");
      append(SVG_VERSION_ATTR);
      append(" ");
      append(SVG_NAMESPACE_ATTR);
      append(">");
   }
   
   @Override
   public void drawPath(Path path)
   {
      if (path == null)
         throw new NullPointerException();
      
      int size = path.getNumItems();
      if (size == 0)
         return;
      
      append("<");
      append(PATH_NAME);
      append(" ");
      append(PATH_ATT_NAME);
      append("=\"");
      
      FloatPoint2D first = path.getFirst();
      append("M");
      append(""+first.getX());
      append(" ");
      append(""+first.getY());
      
      if (size == 1)
      {
         append(" L");
         append(""+first.getX());
         append(" ");
         append(""+first.getY());
      }
      else
      {
         FloatPoint2D curPt;
         for (int i=1; i<size; i++)
         {
            curPt = path.getItemAt(i);
            
            append(" L");
            append(""+curPt.getX());
            append(" ");
            append(""+curPt.getY());
         }
      }
      
      append("\" ");
      append(STROKE_WIDTH_NAME);
      append("=\"");
      append(""+this.width);
      append("\" ");
      append(STROKE_NAME);
      append("=\"");
      append(this.color.toString());
      append("\" ");
      append(FILL_NAME);
      append("=\"none\" />");
   }
   
   @Override
   public void drawLine(FloatPoint2D pt1, FloatPoint2D pt2)
   {
      if (pt1 == null || pt2 == null)
         throw new NullPointerException();
      
      append("<");
      append(LINE_TAG_NAME);
      append(" ");
      appendLocation(pt1, "1");
      appendSpace();
      appendLocation(pt2, "2");
      append(" ");
      append(STROKE_WIDTH_NAME);
      append("=\"");
      append(""+this.width);
      append("\" ");
      append(STROKE_NAME);
      append("=\"");
      append(this.color.toString());
      append("\" />");
   }

   @Override
   public void drawRectangle(float x, float y, float width, float height)
   {
      renderRectangle(x, y, width, height, false);
   }

   @Override
   public void fillRectangle(float x, float y, float width, float height)
   {
      renderRectangle(x, y, width, height, true);
   }
   
   private void renderRectangle(float x, float y, 
                                float width, float height, boolean fill)
   {
      append("<");
      append(RECT_TAG_NAME);
      append(" ");
      appendLocation(x, y, "");
      appendSpace();
      appendSize(width, height);
      appendSpace();
      
      String fillName = "none";
      if (fill)
         fillName = this.color.toString();
      append(FILL_NAME);
      append("=\"");
      append(fillName);
      append("\" ");
      append(STROKE_NAME);
      append("=\"");
      append(this.color.toString());
      append("\" ");
      append(STROKE_WIDTH_NAME);
      append("=\"");
      append(""+this.width);
      append("\" />");
   }

   @Override
   public void setColor(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      String r = fixColorValue(Integer.toHexString(color.getRed()));
      String g = fixColorValue(Integer.toHexString(color.getGreen()));
      String b = fixColorValue(Integer.toHexString(color.getBlue()));
      
      this.color = "#"+r+g+b;
   }
   
   @Override
   public Color getColor()
   {
      return Color.decode(this.color);
   }
   
   private static String fixColorValue(String val)
   {
      if (val == null)
         throw new NullPointerException();
      
      String newVal = "";
      if (val.length() == 1)
         newVal += "0";
      newVal += val;
      
      return newVal;
   }
   
   @Override
   public void setLineWidth(float width)
   {
      this.width = width;
   }
   
   @Override
   public float getLineWidth()
   {
      return this.width;
   }
   
   @Override
   protected void beginGroupImpl(Renderable renderable, String desc, 
                                 float xScaleLevel, float yScaleLevel)
   {
      append("<");
      append(G_NAME);
      append(" ");
      append(ID_NAME);
      append("=\"");
      
      if (renderable instanceof Binder)
         append(BINDER_ID_NAME);
      else if (renderable instanceof Page)
         append(PAGE_ID_NAME);
      else if (renderable instanceof Stroke)
         append(STROKE_ID_NAME);
      else
         append(renderable.getClass().getName());
      
      append("\" ");
      append(DESC_NAME);
      append("=\"");
      append(desc);
      
      float curOriginX = 0;
      float curOriginY = 0;
      FloatPoint2D curOrigin = this.transStack.getOrigin();
      if (curOrigin != null)
      {
         curOriginX = curOrigin.getX();
         curOriginY = curOrigin.getY();
      }
      
      float diffX = this.originX - curOriginX;
      float diffY = this.originY - curOriginY;
      
      append("\" ");
      append(TRANSFORM_NAME);
      append("=\"");
      append(TRANSLATE_NAME);
      append("(");
      append(""+diffX);
      append(",");
      append(""+diffY);
      append(") ");
      
      append("\">");
      
      this.transStack.push(new FloatPoint2D(diffX, diffY, 1, 1));
   }

   @Override
   protected void endGroupImpl(Renderable renderable)
   {
      this.transStack.pop();
      
      append("</");
      append(G_NAME);
      append(">");
   }
   
   @Override
   public void finish()
   {
      append("</");
      append(SVG_TAG_NAME);
      append(">");
      
      try
      {
         this.outStream.close();
      }
      catch (IOException e)
      {
         this.curError = e;
      }
   }
   
   private void appendSpace()
   {
      append(" ");
   }
   
   private void appendValue(String label, float pxVal)
   {
      if (label == null)
         throw new NullPointerException();
      
      append(label);
      append("=\"");
      append(""+pxVal);
      appendSpace();
      append(Unit.PIXEL.toString());
      append("\"");
   }
   
   private void appendSize(float width, float height)
   {
      appendValue(WIDTH_NAME, width);
      appendSpace();
      appendValue(HEIGHT_NAME, height);
   }
   
   private void appendLocation(FloatPoint2D pt, String suffix)
   {
      if (pt == null)
         throw new NullPointerException();
      
      appendLocation(pt.getX(), pt.getY(), suffix);
   }
   
   private void appendLocation(float x, float y, String suffix)
   {
      if (suffix == null)
         throw new NullPointerException();
      
      appendValue(X_NAME+suffix, x);
      appendSpace();
      appendValue(Y_NAME+suffix, y);
   }
   
   private void append(String str)
   {
      if (this.curError != null)
         return;
      
      if (str == null)
         throw new NullPointerException();
      
      try
      {
         this.outStream.write(str.getBytes());
      }
      catch (IOException e)
      {
         this.curError = e;
      }
   }
   
   public Exception getError()
   {
      return this.curError;
   }
   
   // The Renderer2D class doesn't specify a scale() method
   //@Override
   //public void scale(float x, float y)
   //{
   //}

   @Override
   public void translate(float x, float y)
   {
      this.originX += x;
      this.originY += y;
   }
   
   @Override
   public boolean isInClipRegion(Bounded bounded)
   {
      return true;
   }
   
   private class TranslateStack
   {
      private Vector<FloatPoint2D> stack;
      
      public TranslateStack()
      {
         this.stack = new Vector<FloatPoint2D>();
      }
      
      public FloatPoint2D getOrigin()
      {
         float sumX = 0;
         float sumY = 0;
         
         for (FloatPoint2D pt : this.stack)
         {
            sumX += pt.getX();
            sumY += pt.getY();
         }
         
         return new FloatPoint2D(sumX, sumY, 1, 1);
      }
      
      public void push(FloatPoint2D pt)
      {
         if (pt == null)
            throw new NullPointerException();
         
         this.stack.add(pt);
      }
      
      public FloatPoint2D pop()
      {
         if (this.stack.isEmpty())
            return null;
         
         FloatPoint2D top = this.stack.lastElement();
         this.stack.removeElementAt(this.stack.size()-1);
         return top;
      }
      
      @Override
      public String toString()
      {
         StringBuffer buffer = new StringBuffer("Translation stack:  ");
         for (FloatPoint2D pt : this.stack)
         {
            buffer.append("[");
            buffer.append(pt.toString());
            buffer.append("]  ");
         }
         return buffer.toString();
      }
   }
}
