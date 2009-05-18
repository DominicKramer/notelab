package noteLab.gui.control.drop.pic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

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
      
      if ( !(g instanceof Graphics2D) )
         return;
      
      setRenderingHints(g);
      
      float realValue = this.scaleFactor*this.imageWidth;
      if (realValue < 0)
         realValue = width*0.5f;
      
      float xMid = width*0.5f;
      float x1 = xMid-realValue*0.5f;
      float x2 = xMid+realValue*0.5f;
      
      float yMid = height*0.5f;
      float y1 = yMid-realValue*0.5f;
      
      g.setColor(this.color);
      
      // The following operations require a Graphics2D object
      Graphics2D g2d = (Graphics2D)g;
      
      if (this.style == Style.Circle)
      {
         Ellipse2D.Float oval = 
                            new Ellipse2D.Float(x1, y1, realValue, realValue);
         
         if (this.fill)
            g2d.fill(oval);
         else
            g2d.draw(oval);
      }
      else if (this.style == Style.Square)
      {
         Rectangle2D.Float rect = 
                              new Rectangle2D.Float(x1, y1, 
                                                    realValue, realValue);
         
         if (this.fill)
            g2d.fill(rect);
         else
            g2d.draw(rect);
      }
      else if (this.style == Style.Line)
      {
         float littleDelta = 2;
         float yTop = yMid+littleDelta;
         float yBottom = yMid-littleDelta;
         
         g2d.draw(new Line2D.Float(x1, xMid, x2, xMid));
         g2d.draw(new Line2D.Float(x1, yTop, x1, yBottom));
         g2d.draw(new Line2D.Float(x2, yTop, x2, yBottom));
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
