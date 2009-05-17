package noteLab.gui.control.drop.pic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class PrimitivePic implements ButtonPic
{
   public enum Style
   {
      Line, 
      Circle, 
      Square
   }
   
   private int imageWidth;
   private Color color;
   private boolean fill;
   private Style style;
   private float scaleFactor;
   
   public PrimitivePic(int imageWidth, Color color, 
                       boolean fill, Style style, 
                       float scaleFactor)
   {
      setValue(imageWidth);
      setColor(color);
      setStyle(style);
      setFilled(fill);
      scaleTo(scaleFactor);
   }
   
   public void scaleBy(float scaleFactor)
   {
      if (scaleFactor <= 0)
         throw new IllegalArgumentException("The scale factor cannot be " +
                                            "<= 0 but a value of "+
                                            scaleFactor+" was supplied.");
      
      this.scaleFactor *= scaleFactor;
   }
   
   public void scaleTo(float scaleFactor)
   {
      if (scaleFactor <= 0)
         throw new IllegalArgumentException("The scale factor cannot be " +
                                            "<= 0 but a value of "+
                                            scaleFactor+" was supplied.");
      
      this.scaleFactor = scaleFactor;
   }
   
   public Color getColor()
   {
      return this.color;
   }

   public void setColor(Color color)
   {
      this.color = color;
   }
   
   public int getValue()
   {
      return this.imageWidth;
   }

   public void setValue(int width)
   {
      this.imageWidth = width;
   }
   
   public boolean isFilled()
   {
      return this.fill;
   }
   
   public void setFilled(boolean fill)
   {
      this.fill = fill;
   }
   
   public float getScaleFactor()
   {
      return this.scaleFactor;
   }
   
   public void resizeTo(float factor)
   {
      this.imageWidth *= factor;
   }
   
   public Style getStyle()
   {
      return this.style;
   }
   
   public void setStyle(Style style)
   {
      if (style == null)
         throw new NullPointerException();
      
      this.style = style;
   }

   public void paintPic(Graphics g, int width, int height)
   {
      if (g == null)
         throw new NullPointerException();
      
      //setRenderingHints(g);
      
      int realValue = (int)(this.scaleFactor*this.imageWidth);
      if (realValue < 0)
         realValue = (int)(width*0.5f);
      
      int xMid = (int)(width*0.5f);
      int x1 = (int)(xMid-realValue*0.5f);
      int x2 = (int)(xMid+realValue*0.5f);
      
      int yMid = (int)(height*0.5f);
      int y1 = (int)(yMid-realValue*0.5f);
      
      g.setColor(this.color);
      
      if (this.style == Style.Circle)
      {
         if (this.fill)
            g.fillOval(x1, y1, realValue, realValue);
         else
            g.drawOval(x1, y1, realValue, realValue);
      }
      else if (this.style == Style.Square)
      {
         if (this.fill)
            g.fillRect(x1, y1, realValue, realValue);
         else
            g.drawRect(x1, y1, realValue, realValue);
      }
      else if (this.style == Style.Line)
      {
         int littleDelta = 2;
         int yTop = yMid+littleDelta;
         int yBottom = yMid-littleDelta;
         
         g.drawLine(x1, xMid, x2, xMid);
         g.drawLine(x1, yTop, x1, yBottom);
         g.drawLine(x2, yTop, x2, yBottom);
      }
   }
   
   private void setRenderingHints(Graphics g)
   {
      if (g instanceof Graphics2D)
      {
         Graphics2D g2d = (Graphics2D)g;
         
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                              RenderingHints.VALUE_ANTIALIAS_ON);

         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                              RenderingHints.VALUE_RENDER_QUALITY);

         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                              RenderingHints.VALUE_INTERPOLATION_BICUBIC);

         g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                              RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

         g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                              RenderingHints.VALUE_COLOR_RENDER_QUALITY);

         g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                              RenderingHints.VALUE_STROKE_PURE);

         g2d.setRenderingHint(RenderingHints.KEY_DITHERING, 
                              RenderingHints.VALUE_DITHER_ENABLE);
      }
   }
}
