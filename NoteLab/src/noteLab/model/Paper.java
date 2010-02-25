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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import noteLab.model.geom.FloatPoint2D;
import noteLab.model.geom.TransformRectangle2D;
import noteLab.model.pdf.PDFFileInfo;
import noteLab.model.pdf.PDFPageInfo;
import noteLab.util.CopyReady;
import noteLab.util.Selectable;
import noteLab.util.UnitScaleDependent;
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

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

/**
 * This class represents a piece of paper.
 * 
 * @author Dominic Kramer
 */
public class Paper extends TransformRectangle2D 
                      implements Renderable, CopyReady<Paper>, Bounded, 
                                 Selectable, UnitScaleDependent
{
   private static final String DESC_DELIM = ";";
   
   /**
    * The color that a paper will be outlined with if it is currently 
    * selected.
    */
   private static final Color SELECTION_COLOR = Color.BLUE;
   
   /**
    * The color of the lines drawn on a grid or college or wide ruled 
    * piece of paper.
    */
   // This is the default
   private static final Color LINE_COLOR = new Color(159, 167, 255);
   //private static final Color LINE_COLOR = new Color(205, 205, 205);
   
   /**
    * The color of the margin line on a college or wide ruled 
    * piece of paper.
    */
   // This is the default
   private static final Color MARGIN_COLOR = new Color(78, 248, 137);
   //private static final Color MARGIN_COLOR = new Color(205, 205, 205);
   
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
   
   private boolean selectionEnabled;
   
   private float unitScaleFactor;
   
   private PDFPageInfo pdfPageInfo;
   
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
                int screenRes, float unitScaleLevel)
   {
      super(0, 0, width, height, xScaleLevel, yScaleLevel);
      
      if (paperType == null)
         throw new NullPointerException();
      
      this.screenRes = screenRes;
      
      this.unitScaleFactor = unitScaleLevel;
      
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
      
      setSelectionEnabled(false);
      setBackgroundColor(Color.WHITE);
      setPaperType(paperType);
      setSelected(false);
   }
   
   public void setPDFPageInfo(PDFPageInfo pageInfo)
   {
      if (pageInfo == null)
         throw new NullPointerException();
      
      this.pdfPageInfo = pageInfo;
   }
   
   public PDFPageInfo getPDFPageInfo()
   {
      return this.pdfPageInfo;
   }
   
   public void removePDFPageInfo()
   {
      this.pdfPageInfo = null;
   }
   
   public float getUnitScaleFactor()
   {
      return this.unitScaleFactor;
   }
   
   public void setUnitScaleFactor(float unitScaleFactor)
   {
      this.unitScaleFactor = unitScaleFactor;
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
                             this.screenRes, this.unitScaleFactor);
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
   
   private String getGroupDesc()
   {
      String desc = this.type.name();
      if (this.pdfPageInfo != null)
      {
         desc += DESC_DELIM;
         desc += this.pdfPageInfo.getFileInfo().getSource().getName();
         desc += DESC_DELIM;
         desc += this.pdfPageInfo.getPageNum();
      }
      
      return desc;
   }
   
   public static PaperType decodePaperType(String groupDesc)
   {
      if (groupDesc == null)
         throw new NullPointerException();
      
      int index = groupDesc.indexOf(DESC_DELIM);
      
      String nameDesc = (index == -1)?
                           (groupDesc):(groupDesc.substring(0, index));
      
      System.out.println(nameDesc);
      System.out.println();
      
      PaperType type = PaperType.Plain;
      if (nameDesc.equals(PaperType.Plain.name()))
         type = PaperType.Plain;
      else if (nameDesc.equals(PaperType.CollegeRuled.name()))
         type = PaperType.CollegeRuled;
      else if (nameDesc.equals(PaperType.WideRuled.name()))
         type = PaperType.WideRuled;
      else if (nameDesc.equals(PaperType.Graph.name()))
         type = PaperType.Graph;
      
      return type;
   }
   
   public static PDFPageInfo decodePDFPageInfo(File savedFile, 
                                               String groupDesc) 
                                                  throws NumberFormatException, 
                                                         IOException
   {
      if (savedFile == null || groupDesc == null)
         throw new NullPointerException();
      
      File dir = (savedFile.isDirectory())?
                    (savedFile):(savedFile.getParentFile());
      
      int firstIndex = groupDesc.indexOf(DESC_DELIM);
      int lastIndex = groupDesc.lastIndexOf(DESC_DELIM);
      
      if (firstIndex == -1 || lastIndex == -1)
         return null;
      
      String filename = groupDesc.substring(firstIndex+1, lastIndex);
      String numStr = groupDesc.substring(lastIndex+1, groupDesc.length());
      
      int pageNum = Integer.parseInt(numStr);
      
      File source = new File(dir, filename);
      PDFFileInfo pdfFileInfo = new PDFFileInfo(source);
      return new PDFPageInfo(pdfFileInfo, pageNum);
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
      {
         renderer.setLineWidth(1);
         renderer.setColor(Color.BLACK);
         renderer.drawRectangle(0, 0, getWidth(), getHeight());
         return;
      }
      
      renderer.beginGroup(Paper.this, getGroupDesc(), 
                          getXScaleLevel(), getYScaleLevel());
      
      if (this.pdfPageInfo != null 
            && renderer instanceof SwingRenderer2D)
      {
         // We need a Graphics2D object which only SwingRenderer2D 
         // objects have
         int pageNum = this.pdfPageInfo.getPageNum();
         
         // The following uses PDFRenderer to render the paper.
         final PDFPage pdfPage = this.pdfPageInfo.
                                    getFileInfo().
                                       getPDFFile().
                                          getPage(pageNum);
         
         Rectangle2D.Float bounds = getBounds2D();
         Rectangle rect = new Rectangle(0, 
                                        0, 
                                        (int)(bounds.width), 
                                        (int)(bounds.height));
         
         Graphics2D g2d = ((SwingRenderer2D)renderer).createGraphics();
         if (renderer.isScrolling())
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                 RenderingHints.VALUE_RENDER_SPEED);
         
         PDFRenderer pdfRenderer = new PDFRenderer(pdfPage, 
                                                   g2d, 
                                                   rect, 
                                                   null, 
                                                   getBackgroundColor());
         
         try
         {
            pdfPage.waitForFinish();
            pdfRenderer.run();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         renderer.setColor(this.bgColor);
         renderer.fillRectangle(0, 0, getWidth(), getHeight());
      }
      
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
   }
   
   public void adaptToUnitFactor(float unitScaleFactor)
   {
      float xScale = getXScaleLevel();
      float yScale = getYScaleLevel();
      scaleTo(1, 1);
      
      this.unitScaleFactor = unitScaleFactor;
      
      this.rawGridWidth = calcGridWidth(this.screenRes, unitScaleFactor);
      this.rawColledgeWidth = calcCollegeWidth(this.screenRes, unitScaleFactor);
      this.rawWideWidth = calcWideWidth(this.screenRes, unitScaleFactor);
      
      this.rawCollegeMargin = calcCollegeMargin(this.screenRes, unitScaleFactor);
      this.rawWideMargin = calcWideMargin(this.screenRes, unitScaleFactor);
      
      scaleTo(xScale, yScale);
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
