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

package noteLab.util.io.noteLab;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import noteLab.model.Page;
import noteLab.model.Paper;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.Paper.PaperType;
import noteLab.model.binder.Binder;
import noteLab.model.binder.FlowBinder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Pen;
import noteLab.util.InfoCenter;
import noteLab.util.geom.unit.Unit;
import noteLab.util.io.FileLoader;
import noteLab.util.io.ResolvableHandler;
import noteLab.util.settings.SettingsUtilities;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class NoteLabFileLoader 
                extends ResolvableHandler  
                           implements FileLoader, 
                                      NoteLabFileConstants
{
   private static final float SCALE_LEVEL = 1;
   private static final float UNIT_SCALE_LEVEL = 1;
   
   private File file;
   private NoteLabFileLoadedListener listener;
   
   private Binder curBinder;
   private Page curPage;
   private Stroke curStroke;
   
   private String lastID;
   
   private int screenRes;
   
   public NoteLabFileLoader(File file, 
                            NoteLabFileLoadedListener listener) 
                               throws IOException
   {
      if (file == null || listener == null)
         throw new NullPointerException();
      
      String ext = InfoCenter.getFileExtension().toLowerCase();
      if (!file.getPath().toLowerCase().endsWith(ext))
         throw new IOException("The file '"+file.getAbsolutePath()+
                               "' is not a native "+InfoCenter.getAppName()+
                               " file (a file of type "+ext+").");
      
      this.file = file;
      this.listener = listener;
      
      this.curBinder = null;
      this.curPage = null;
      this.curStroke = null;
      
      this.lastID = null;
      
      this.screenRes = Unit.getScreenResolution();
   }
   
   public void loadFile() throws ParserConfigurationException, 
                                 SAXException, 
                                 IOException
   {
      //create a parser factory
      SAXParserFactory parserFac = SAXParserFactory.newInstance();
      parserFac.setNamespaceAware(true);
      
      //get a parser
      SAXParser parser = parserFac.newSAXParser();
      
      //parse the xml document
      //First try to open the file as a gzip file.  If an error occurs 
      //its because the file is not in gzip format so read the file 
      //as a regular text file.
      try
      {
         parser.parse(new GZIPInputStream(new FileInputStream(this.file)), 
                      this);
      }
      catch (IOException e)
      {
         parser.parse(this.file, this);
      }
   }
   
   @Override
   public void startElement(String uri, String localName, 
                            String qName, Attributes attributes) 
                               throws SAXException
   {
      super.startElement(uri, localName, qName, attributes);
      
      if (localName.equals(G_NAME))
      {
         String id = attributes.getValue(ID_NAME);
         this.lastID = id;
         if (id == null)
            return;
         
         if (id.equals(BINDER_ID_NAME))
         {
            // the description of the binder contains the screen 
            // resolution at which the file was saved.  In older 
            // files this information was not saved, and in this 
            // case 'resStr' should be the empty string.
            String resStr = attributes.getValue(DESC_NAME);
            
            // the screen resolution is initialized to be the 
            // current system's screen resolution
            if (resStr != null && resStr.trim().length() > 0)
            {
               // if the 'desc' attribute actually contains something parse it
               try
               {
                  this.screenRes = Integer.parseInt(resStr.trim());
               }
               catch (NumberFormatException e)
               {
                  // if the screen resolution wasn't stored use 
                  // the current screen resolution
                  this.screenRes = Unit.getScreenResolution();
                  
                  System.err.println(NoteLabFileLoader.class.getName()+
                                     " ERROR:  The 'desc' attribute of the " +
                                     "element with id='"+
                                     BINDER_ID_NAME+
                                     "' is nonempty but contains a string \"" +
                                     resStr+"\" which doesn't correspond to a " +
                                     "valid screen resolution.  Any empty " +
                                     "string represents the current system's " +
                                     "screen resolution.");
               }
            }
         }
         else if (id.equals(PAGE_ID_NAME))
         {
            this.curPage = new Page(getPaperType(attributes), 
                                    SCALE_LEVEL, 
                                    SCALE_LEVEL, 
                                    this.screenRes, 
                                    UNIT_SCALE_LEVEL);
            
            if (this.curBinder == null)
               this.curBinder = new FlowBinder(SCALE_LEVEL, 
                                               SCALE_LEVEL, 
                                               this.curPage);
            else
               this.curBinder.addPage(this.curPage);
         }
         else if (id.equals(STROKE_ID_NAME))
         {
            this.curStroke = new Stroke(new Pen(SCALE_LEVEL), 
                                        new Path(SCALE_LEVEL, 
                                                 SCALE_LEVEL));
            this.curPage.addStroke(this.curStroke);
         }
      }
      else if (localName.equals(RECT_TAG_NAME))
      {
         if (this.curPage == null)
         {
            System.err.println(NoteLabFileLoader.class.getName()+
                               " ERROR:  The start of a rectangle was found "+
                               "but a page hasn't been constructed yet.");
            this.curPage = new Page(PaperType.Plain, 
                                    SCALE_LEVEL, 
                                    SCALE_LEVEL, 
                                    this.screenRes, 
                                    UNIT_SCALE_LEVEL);
         }
         
         Rectangle2D.Float rect = constructRectangle(attributes);
         this.curPage.setX(rect.x);
         this.curPage.setY(rect.y);
         this.curPage.setWidth(rect.width);
         this.curPage.setHeight(rect.height);
         
         String colorStr = attributes.getValue(FILL_NAME);
         if (this.curPage != null && !colorStr.toLowerCase().equals("none"))
         {
            Color bgColor = getColor(attributes, FILL_NAME);
            this.curPage.getPaper().setBackgroundColor(bgColor);
         }
      }
      else if (localName.equals(PATH_NAME))
      {
         String pathText = attributes.getValue(PATH_ATT_NAME);
         if (this.curStroke == null)
         {
            System.err.println(NoteLabFileLoader.class.getName()+
                               " ERROR:  The start of a path was found " +
                               "but a stroke hasn't been constructed yet.");
            
            System.err.println("Local name = "+localName);
            System.err.println("Attributes");
            for (int i=0; i<attributes.getLength(); i++)
            {
               System.err.println(""+attributes.getLocalName(i)+"=\""+
                                  attributes.getValue(i)+"\"");
            }
            
            this.curStroke = new Stroke(new Pen(SCALE_LEVEL), 
                                        new Path(SCALE_LEVEL, 
                                                 SCALE_LEVEL));
         }
         
         fillPath(this.curStroke.getPath(), pathText, SCALE_LEVEL);
         
         Color color = getColor(attributes, STROKE_NAME);
         float width = getLineWidth(attributes);
         
         Pen pen = this.curStroke.getPen();
         pen.setColor(color);
         pen.setWidth(width);
      }
      else if (localName.equals(LINE_TAG_NAME))
      {
         if (this.lastID == null || 
             !this.lastID.equals(STROKE_ID_NAME))
            return;
         
         if (this.curStroke == null)
         {
            System.err.println(NoteLabFileLoader.class.getName()+
                               " ERROR:  The start of a line was found " +
                               "but a stroke hasn't been constructed yet.");
            
            System.err.println("Local name = "+localName);
            System.err.println("Attributes");
            for (int i=0; i<attributes.getLength(); i++)
            {
               System.err.println(""+attributes.getLocalName(i)+"=\""+
                                  attributes.getValue(i)+"\"");
            }
            
            this.curStroke = new Stroke(new Pen(SCALE_LEVEL), 
                                        new Path(SCALE_LEVEL, 
                                                 SCALE_LEVEL));
            return;
         }
         
         Path path = this.curStroke.getPath();
         if (path.isEmpty())
         {
            path.addItem(constructPoint(attributes, 1));
            
            Color color = getColor(attributes, STROKE_NAME);
            float width = getLineWidth(attributes);
            
            Pen pen = this.curStroke.getPen();
            pen.setColor(color);
            pen.setWidth(width);
         }
         
         // Always add the second point from the line segment
         // In the case that we are at the first line add both 
         // the first and last point.  Otherwise, the second 
         // point from the first line segment will never 
         // be added to the path.
         path.addItem(constructPoint(attributes, 2));
      }
   }
   
   @Override
   public void endElement(String uri, String localName, 
                          String qName) throws SAXException
   {
      super.endElement(uri, localName, qName);
   }

   @Override
   public void endDocument() throws SAXException
   {
      super.endDocument();
      
      this.curBinder.doLayout();
      
      CompositeCanvas canvas = new CompositeCanvas(this.curBinder, SCALE_LEVEL);
      canvas.setFile(this.file);
      
      float unitScaleFactor = SettingsUtilities.getUnitScaleFactor();
      canvas.getBinder().resizeTo(unitScaleFactor, unitScaleFactor);
      canvas.setUnitScaleFactor(unitScaleFactor);
      
      this.curBinder.setCurrentPage(this.curBinder.getNumberOfPages()-1);
      Paper paper = this.curBinder.getCurrentPage().getPaper();
      
      SettingsUtilities.setPaperType(paper.getPaperType());
      SettingsUtilities.setPaperColor(paper.getBackgroundColor());
      
      this.listener.noteLabFileLoaded(canvas, getMessageBuffer().toString());
   }
   
   private PaperType getPaperType(Attributes atts)
   {
      if (atts == null)
         throw new NullPointerException();
      
      String desc = atts.getValue(DESC_NAME);
      PaperType type = PaperType.Plain;
      if (desc.equals(PaperType.Plain.name()))
         type = PaperType.Plain;
      else if (desc.equals(PaperType.CollegeRuled.name()))
         type = PaperType.CollegeRuled;
      else if (desc.equals(PaperType.WideRuled.name()))
         type = PaperType.WideRuled;
      else if (desc.equals(PaperType.Graph.name()))
         type = PaperType.Graph;
      else
      {
         System.err.println(NoteLabFileLoader.class.getName()+
                            "ERROR:  The paper type '"+desc+
                            "' is not supported.  The default value '"+
                            type+"' will be used.");
      }
      
      return type;
   }
   
   private static Rectangle2D.Float constructRectangle(Attributes atts)
   {
      String xParam = atts.getValue(X_NAME);
      String yParam = atts.getValue(Y_NAME);
      String widthParam = atts.getValue(WIDTH_NAME);
      String heightParam = atts.getValue(HEIGHT_NAME);
      
      float x = getValue(xParam);
      float y = getValue(yParam);
      float width = getValue(widthParam);
      float height = getValue(heightParam);
      
      return new Rectangle2D.Float(x, y, width, height);
   }
   
   private static Color getColor(Attributes atts, String colorAtName)
   {
      if (atts == null)
         throw new NullPointerException();
      
      String colorStr = atts.getValue(colorAtName);
      if (colorStr.length() == 0)
         return Color.BLACK;
      
      colorStr = colorStr.substring(1);
      int rgb = 0;
      try
      {
         rgb = Integer.parseInt(colorStr, 16);
      }
      catch (NumberFormatException e)
      {
         System.out.println(NoteLabFileLoader.class.getName()+
                            "ERROR:  The value '#"+rgb+"' does not "+
                            "specify a valid color.  The default value '"+
                            rgb+"' will be used.");
      }
      
      return new Color(rgb);
   }
   
   private static float getLineWidth(Attributes atts)
   {
      if (atts == null)
         throw new NullPointerException();
      
      String widthStr = atts.getValue(STROKE_WIDTH_NAME);
      float width = 1;
      try
      {
         width = Float.parseFloat(widthStr);
      }
      catch (NumberFormatException e)
      {
         System.out.println(NoteLabFileLoader.class.getName()+
                            "ERROR:  The width '"+widthStr+"' is " +
                            "invalid the default value '"+width+
                            "' will be used.");
      }
      
      return width;
   }
   
   private FloatPoint2D constructPoint(Attributes atts, int num)
   {
      if (atts == null)
         throw new NullPointerException();
      
      String xParam = atts.getValue(X_NAME+num);
      String yParam = atts.getValue(Y_NAME+num);
      
      return new FloatPoint2D(getValue(xParam), getValue(yParam), 
                              SCALE_LEVEL, SCALE_LEVEL);
   }
   
   private static float getValue(String mValStr)
   {
      if (mValStr == null)
         throw new NullPointerException();
      
      mValStr = mValStr.trim();
      if (mValStr.length() < 3)
         return 0;
      
      String unitStr = mValStr.substring(mValStr.length()-2).trim();
      String valStr = mValStr.substring(0, mValStr.length()-2).trim();
      
      Unit unit = Unit.INCH;
      if (unitStr.equals(INCH_UNIT_NAME))
         unit = Unit.INCH;
      else if (unitStr.equals(CM_UNIT_NAME))
         unit = Unit.CM;
      else if (unitStr.equals(PIXEL_UNIT_NAME))
         unit = Unit.PIXEL;
      else
      {
         System.out.println("LoadNoteLabFile:  Found an unsupported unit '"+
                            unitStr+"'.  Using the default unit '"+unit+"'.");
      }
      
      double val = 0;
      try
      {
         val = Double.parseDouble(valStr);
      }
      catch (NumberFormatException e)
      {
         System.out.println("LoadNoteLabFile:  The value '"+valStr+
                            "' is not a proper double precision number.  " +
                            "The default value of '"+val+"' will be used.");
      }
      
      return (float)val;
   }
}
