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

package noteLab.model;

import java.awt.Color;
import java.awt.image.BufferedImage;

import noteLab.model.geom.FloatPoint2D;
import noteLab.model.geom.TransformRectangle2D;
import noteLab.util.CopyReady;
import noteLab.util.Selectable;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.unit.Unit;
import noteLab.util.mod.ModType;
import noteLab.util.render.ImageRenderer2D;
import noteLab.util.render.PrinterRenderer2D;
import noteLab.util.render.Renderable;
import noteLab.util.render.Renderer2D;
import noteLab.util.render.SVGRenderer2D;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.settings.DebugSettings;
import noteLab.util.settings.SettingsUtilities;

/**
 * This class represents a piece of paper.
 * 
 * @author Dominic Kramer
 */
public class Paper extends TransformRectangle2D 
                      implements Renderable, CopyReady<Paper>, Bounded, 
                                 Selectable
{
   /**
    * The color that a paper will be outlined with if it is currently 
    * selected.
    */
   private static final Color SELECTION_COLOR = Color.BLUE;
   
   /**
    * The color of the lines drawn on a grid or college or wide ruled 
    * piece of paper.
    */
   private static final Color LINE_COLOR = new Color(159, 167, 255);
   
   /**
    * The color of the margin line on a college or wide ruled 
    * piece of paper.
    */
   private static final Color MARGIN_COLOR = new Color(78, 248, 137);
   
   /**
    * Represents a type of paper.
    * 
    * @author Dominic Kramer
    */
   public enum PaperType
   {
      /** Represents a plain piece of paper. */
      Plain, 
      
      /** Represents a piece of graph paper. */
      Graph, 
      
      /** Represents a college ruled piece of paper. */
      CollegeRuled, 
      
      /** Represents a wide ruled piece of paper. */
      WideRuled
   };
   
   /** This paper's type. */
   private PaperType type;
   
   /** This paper's background color. */
   private Color bgColor;
   
   /** Stores if this paper is selected or not. */
   private boolean isSelected;
   
   private final int screenRes;
   
   private float rawGridWidth;
   private float rawColledgeWidth;
   private float rawWideWidth;
   private float rawCollegeMargin;
   private float rawWideMargin;
   
   /**
    * The width of the lines drawn on a piece of graph paper.  
    * This method takes into account the amount this paper 
    * has been scaled.
    */
   private float gridWidth;
   
   /**
    * The width of the lines drawn on a colleged ruled piece 
    * of paper.  This method takes into account the amount this 
    * paper has been scaled.
    */
   private float collegeWidth;
   
   /**
    * The width of the lines drawn on a wide ruled piece paper.  
    * This method takes into account the amount this paper 
    * has been scaled.
    */
   private float wideWidth;
   
   /**
    * The width of the margin line drawn on a college ruled 
    * piece of paper.  This method takes into account the amount 
    * this paper has been scaled.
    */
   private float collegeMargin;
   
   /**
    * The width of the margin line drawn on a wide ruled 
    * piece of paper.  This method takes into account the amount 
    * this paper has been scaled.
    */
   private float wideMargin;
   
   /**
    * The image to which this paper is cached to help speed 
    * rendering.  If this field is <code>null</code> this 
    * paper hasn't been cached or its cache is not up to date.
    */
   private BufferedImage cacheImage;
   
   private boolean selectionEnabled;
   
   /**
    * Constructs a paper with the given parameters.
    * 
    * @param paperType This papers type.
    * @param width The width of this paper.
    * @param height The height of this paper.
    * @param xScaleLevel The amount this paper was scaled in the x 
    *                    direction when this paper was constructed.
    * @param yScaleLevel The amount this paper was scaled in the y 
    *                    direction when this paper was constructed.
    */
   public Paper(PaperType paperType, float width, float height, 
                float xScaleLevel, float yScaleLevel, 
                int screenRes)
   {
      super(0, 0, width, height, xScaleLevel, yScaleLevel);
      
      if (paperType == null)
         throw new NullPointerException();
      
      this.screenRes = screenRes;
      
      float unitScaleLevel = SettingsUtilities.getUnitScaleFactor();
      
      this.rawGridWidth = calcGridWidth(screenRes, unitScaleLevel);
      this.rawColledgeWidth = calcCollegeWidth(screenRes, unitScaleLevel);
      this.rawWideWidth = calcWideWidth(screenRes, unitScaleLevel);
      
      this.rawCollegeMargin = calcCollegeMargin(screenRes, unitScaleLevel);
      this.rawWideMargin = calcWideMargin(screenRes, unitScaleLevel);
      
      this.gridWidth = this.rawGridWidth*yScaleLevel;
      this.collegeWidth = this.rawColledgeWidth*yScaleLevel;
      this.wideWidth = this.rawWideWidth*yScaleLevel;
      
      this.wideMargin = this.rawWideMargin*yScaleLevel;
      this.collegeMargin = this.rawCollegeMargin*yScaleLevel;
      
      this.cacheImage = null;
      
      setSelectionEnabled(false);
      setBackgroundColor(Color.WHITE);
      setPaperType(paperType);
      setSelected(false);
   }
   
   public boolean getSelectionEnabled()
   {
      return this.selectionEnabled;
   }
   
   public void setSelectionEnabled(boolean enabled)
   {
      this.selectionEnabled = enabled;
   }
   
   public int getScreenResolution()
   {
      return this.screenRes;
   }
   
   /**
    * Used to get this paper's background color.
    * 
    * @return This paper's background color.
    */
   public Color getBackgroundColor()
   {
      return this.bgColor;
   }
   
   /**
    * Used to set this paper's background color.
    * 
    * @param bgColor This paper's background color.
    */
   public void setBackgroundColor(Color bgColor)
   {
      if (bgColor == null)
         throw new NullPointerException();
      
      this.bgColor = bgColor;
      
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get this paper's type.
    * 
    * @return This paper's type.
    */
   public PaperType getPaperType()
   {
      return this.type;
   }
   
   /**
    * Used to set this paper's type.
    * 
    * @param type This paper's type.
    */
   public void setPaperType(PaperType type)
   {
      if (type == null)
         throw new NullPointerException();
      
      if (this.type == type)
         return;
      
      this.type = type;
      notifyModListeners(ModType.Other);
      updateCache();
   }
   
   /**
    * Informs this paper to cache itself to the image specified by 
    * the field <code>this.cacheImage</code>.
    */
   private void updateCache()
   {
      if (DebugSettings.getSharedInstance().useCache())
      {
         int width = (int)(getWidth());
         int height = (int)(getHeight());
         
         if (width <= 0)
            width = 1;
         
         if (height <= 0)
            height = 1;
         
         this.cacheImage = new BufferedImage(width, height, 
                                             BufferedImage.TYPE_INT_ARGB);
         
         ImageRenderer2D renderer = new ImageRenderer2D(this.cacheImage);
         doRenderInto(renderer);
      }
   }
   
   /**
    * Used to determine if this paper is selected.
    * 
    * @return <code>True</code> if this paper is selected and 
    *         <code>false</code> if it isn't.
    */
   public boolean isSelected()
   {
      return this.isSelected;
   }
   
   /**
    * Used to set if this paper is selected.
    * 
    * @param isSelected <code>True</code> if this paper is selected and 
    *                   <code>false</code> if it isn't.
    */
   public void setSelected(boolean isSelected)
   {
      this.isSelected = isSelected;
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get a deep copy of this paper.
    * 
    * @return A deep copy of this paper.
    */
   public Paper getCopy()
   {
      Paper copy = new Paper(this.type, this.getWidth(), this.getHeight(), 
                             getXScaleLevel(), getYScaleLevel(), 
                             this.screenRes);
      copy.setBackgroundColor(new Color(this.bgColor.getRed(), 
                                        this.bgColor.getGreen(), 
                                        this.bgColor.getBlue()));
      return copy;
   }
   
   /**
    * Informs this paper to render itself using the given renderer.
    * 
    * @param renderer The renderer used to render this paper.
    */
   public void renderInto(Renderer2D renderer)
   {
      if (renderer == null)
         throw new NullPointerException();
      
      if (this.cacheImage != null && renderer instanceof SwingRenderer2D)
      {
         ((SwingRenderer2D)renderer).drawImage(this.cacheImage);
         return;
      }
      
      doRenderInto(renderer);
      
      if (this.isSelected && getSelectionEnabled() && 
            !(renderer instanceof SVGRenderer2D) && 
            !(renderer instanceof ImageRenderer2D) && 
            !(renderer instanceof PrinterRenderer2D))
      {
         renderer.setLineWidth(2);
         renderer.setColor(SELECTION_COLOR);
         renderer.drawRectangle(0, 0, getWidth(), getHeight());
      }
   }
   
   /**
    * Implementation method that does the actual work of rendering 
    * this paper.
    * 
    * @param renderer The renderer used to render this paper.
    */
   private void doRenderInto(Renderer2D renderer)
   {
      if (renderer == null)
         throw new NullPointerException();
      
      //For debugging purposes
      if (DebugSettings.getSharedInstance().disablePaper())
         return;
      
      renderer.beginGroup(Paper.this, this.type.name(), 
                          getXScaleLevel(), getYScaleLevel());
      
      renderer.setColor(this.bgColor);
      renderer.fillRectangle(0, 0, getWidth(), getHeight());
      
      renderer.setLineWidth(1);
      renderer.tryRenderBoundingBox(Paper.this);
      
      renderer.setColor(LINE_COLOR);
      if (this.type == PaperType.Graph)
      {
         renderLinesVertical(renderer, this.gridWidth, 0, 0, 0, 0);
         renderLinesHorizontal(renderer, this.gridWidth, 0, 0, 0, 0);
      }
      else if (this.type == PaperType.WideRuled)
         renderLinedPage(renderer, this.wideWidth, this.wideMargin);
      else if (this.type == PaperType.CollegeRuled)
         renderLinedPage(renderer, this.collegeWidth, this.collegeMargin);
      
      renderer.setColor(Color.GRAY);
      renderer.drawRectangle(0, 0, getWidth(), getHeight());
      
      renderer.endGroup(Paper.this);
   }
   
   /**
    * Informs this paper to render itself as if it were a lined piece of 
    * paper with the given distance between lines and given margin.
    * 
    * @param renderer The renderer used to render the paper.
    * @param lineWidth The distance between lines on the paper.
    * @param margin The size of the paper's margin.
    */
   private void renderLinedPage(Renderer2D renderer, 
                                float lineWidth, float margin)
   {
      renderLinesHorizontal(renderer, lineWidth, 0, 0, margin, 0);
      
      float redLineX = (float)(margin+getX());
      float redLineY1 = (float)getY();
      float redLineY2 = (float)(redLineY1+getHeight());
      
      renderer.setColor(MARGIN_COLOR);
      



      FloatPoint2D pt1 = new FloatPoint2D(redLineX, redLineY1, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      FloatPoint2D pt2 = new FloatPoint2D(redLineX, redLineY2, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      renderer.drawLine(pt1, pt2);
   }
   
   /**
    * Informs this paper to render lines across the paper.  The lines 
    * are not drawn inside the paper's margins as specified.
    * 
    * @param renderer The renderer used to render the paper.
    * @param gap The distance between the lines drawn on the paper.
    * @param leftMargin The size of left margin drawn.
    * @param rightMargin The size of the right margin drawn.
    * @param topMargin The size of the top margin drawn.
    * @param bottomMargin The size of the bottom drawn.
    */
   private void renderLinesHorizontal(Renderer2D renderer, float gap, 
                                      float leftMargin, float rightMargin, 
                                      float topMargin,  float bottomMargin)
   {
      float height = (float)getHeight();
      float width = (float)getWidth();
      
      float sum = topMargin+bottomMargin;
      float regionHeight = height-sum;
      
      sum = leftMargin+rightMargin;
      float regionWidth = width-sum;
      
      int numGrids = 0;
      try
      {
         numGrids = (int)(regionHeight/gap);
      }
      catch (ArithmeticException e)
      {
         System.err.println(e);
         System.out.println("Paper.renderLinesAcross():  The distance " +
         "between consecutive lines it too small.");
      }
      
      float xVal = (float)(getX()+leftMargin);
      float yVal = (float)(getY()+topMargin);



      
      FloatPoint2D pt1 = new FloatPoint2D(xVal, yVal, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      FloatPoint2D pt2 = new FloatPoint2D(xVal+regionWidth, yVal, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      
      for (int i=0; i<=numGrids; i++)
      {
         renderer.drawLine(pt1, pt2);
         pt1.translateBy(0, gap);
         pt2.translateBy(0, gap);
      }
   }
   
   /**
    * Informs this paper to render lines down the paper.  The lines 
    * are not drawn inside the paper's margins as specified.
    * 
    * @param renderer The renderer used to render the paper.
    * @param gap The distance between the lines drawn on the paper.
    * @param leftMargin The size of left margin drawn.
    * @param rightMargin The size of the right margin drawn.
    * @param topMargin The size of the top margin drawn.
    * @param bottomMargin The size of the bottom drawn.
    */
   private void renderLinesVertical(Renderer2D renderer, float gap, 
                                    float leftMargin, float rightMargin, 
                                    float topMargin,  float bottomMargin)
   {
      float height = (float)getHeight();
      float width = (float)getWidth();
      
      float sum = topMargin+bottomMargin;
      float regionHeight = height-sum;
      
      sum = leftMargin+rightMargin;
      float regionWidth = width-sum;
      
      int numGrids = 0;
      try
      {
         numGrids = (int)(regionWidth/gap);
      }
      catch (ArithmeticException e)
      {
         System.err.println(e);
         System.out.println("Paper.renderLinesAcross():  The distance " +
                            "between consecutive lines it too small.");
      }
      
      float xVal = (float)(getX()+leftMargin);
      float yVal = (float)(getY()+topMargin);



      
      FloatPoint2D pt1 = new FloatPoint2D(xVal, yVal, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      FloatPoint2D pt2 = new FloatPoint2D(xVal, yVal+regionHeight, 
                                          super.getXScaleLevel(), 
                                          super.getYScaleLevel());
      for (int i=0; i<=numGrids; i++)
      {
         renderer.drawLine(pt1, pt2);
         pt1.translateBy(gap, 0);
         pt2.translateBy(gap, 0);
      }
   }
   
   /**
    * Scales this paper by the given amount.
    * 
    * @param x Scales this paper's width by the given amount.
    * @param y Scales this paper's height by the given amount.
    */
   @Override
   public void scaleBy(float x, float y)
   {
      super.scaleBy(x, y);
      
      this.gridWidth *= y;
      this.collegeWidth *= y;
      this.wideWidth *= y;
      
      this.collegeMargin *= y;
      this.wideMargin *= y;
      
      updateCache();
   }
   
   /**
    * Scales this paper to the given amount.
    * 
    * @param x The amount this paper's width is scaled to.
    * @param y The amount this paper's height is scaled to.
    */
   @Override
   public void scaleTo(float x, float y)
   {
      super.scaleTo(x, y);
      
      this.gridWidth = this.rawGridWidth*y;
      this.collegeWidth = this.rawColledgeWidth*y;
      this.wideWidth = this.rawWideWidth*y;
      
      this.collegeMargin = this.rawCollegeMargin*y;
      this.wideMargin = this.rawWideMargin*y;
      
      updateCache();
   }
   
   @Override
   public void resizeTo(float x, float y)
   {
      super.resizeTo(x, y);
      
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      scaleTo(1, 1);
      
      this.rawGridWidth *= y;
      this.rawColledgeWidth *= y;
      this.rawWideWidth *= y;
      
      this.rawCollegeMargin *= y;
      this.rawWideMargin *= y;
      
      scaleTo(xScale, yScale);
      
      updateCache();
   }
   
   public void adaptToUnitFactor(float unitScaleFactor)
   {
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      scaleTo(1, 1);
      
      this.rawGridWidth = calcGridWidth(this.screenRes, unitScaleFactor);
      this.rawColledgeWidth = calcCollegeWidth(this.screenRes, unitScaleFactor);
      this.rawWideWidth = calcWideWidth(this.screenRes, unitScaleFactor);
      
      this.rawCollegeMargin = calcCollegeMargin(this.screenRes, unitScaleFactor);
      this.rawWideMargin = calcWideMargin(this.screenRes, unitScaleFactor);
      
      scaleTo(xScale, yScale);
      
      updateCache();
   }
   
   /**
    * Translates this paper by the given amounts.
    * 
    * @param x The amount this paper is translated in the x direction.
    * @param y THe amount this paper is translated in the y direction.
    */
   @Override
   public void translateBy(float x, float y)
   {
      super.translateBy(x, y);
      updateCache();
   }

   /**
    * Translates this paper to the given coordinates.
    * 
    * @param x The x coordinate to which this paper is translated to.
    * @param y The y coordinate to which this paper is translated to.
    */
   @Override
   public void translateTo(float x, float y)
   {
      super.translateTo(x, y);
      updateCache();
   }
   
   /** Calculates the width of squres on a grided piece of paper. */
   private static float calcGridWidth(int screenRes, float unitScaleLevel)
   {
      return Unit.getValue(0.7f, Unit.CM, Unit.PIXEL, screenRes, unitScaleLevel);
   }
   
   /** Calculates the distance between lines on a college ruled piece of paper. */
   private static float calcCollegeWidth(int screenRes, float unitScaleLevel)
   {
      return Unit.getValue(0.7f, Unit.CM, Unit.PIXEL, screenRes, unitScaleLevel);
   }
   
   /** Calculates the size of the top margin of a college ruled piece of paper. */
   private static float calcCollegeMargin(int screenRes, float unitScaleLevel)
   {
      return Unit.getValue(2f, Unit.CM, Unit.PIXEL, screenRes, unitScaleLevel);
   }
   
   /** Calculates the distance between lines on a wide ruled piece of paper. */
   private static float calcWideWidth(int screenRes, float unitScaleLevel)
   {
      return Unit.getValue(0.9f, Unit.CM, Unit.PIXEL, screenRes, unitScaleLevel);
   }
   
   /** Calculates the size of the top margin of a wide ruled piece of paper. */
   private static float calcWideMargin(int screenRes, float unitScaleLevel)
   {
      return Unit.getValue(3f, Unit.CM, Unit.PIXEL, screenRes, unitScaleLevel);
   }
}
