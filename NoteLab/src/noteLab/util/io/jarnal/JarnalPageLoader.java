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

package noteLab.util.io.jarnal;

import java.awt.Color;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import noteLab.model.Page;
import noteLab.model.Paper;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.Paper.PaperType;
import noteLab.model.tool.Pen;
import noteLab.util.geom.unit.Unit;
import noteLab.util.io.ResolvableHandler;
import noteLab.util.io.StringInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class JarnalPageLoader extends ResolvableHandler implements Runnable
{
   private enum JarnalPaperType
   {
      Plain(PaperType.Plain), 
      Lined(PaperType.CollegeRuled), 
      Graph(PaperType.Graph), 
      Ruled(PaperType.WideRuled);
      
      private PaperType noteLabType;
      
      private JarnalPaperType(PaperType type)
      {
         if (type == null)
            throw new NullPointerException();
         
         this.noteLabType = type;
      }
      
      public static JarnalPaperType findJarnalPaperType(String name)
      {
         if (name == null)
            throw new NullPointerException();
         
         JarnalPaperType[] types = JarnalPaperType.values();
         for (JarnalPaperType type : types)
            if (name.equalsIgnoreCase(type.name()))
               return type;
         
         return JarnalPaperType.Lined;
      }
      
      public PaperType getNoteLabPaperType()
      {
         return this.noteLabType;
      }
   }
   
   private static final String NO_BG_NAME = "none";
   
   private static final String DESC_NAME = "desc";
   
   private static final String PATH_NAME = "path";
   private static final String PATH_ATT_NAME = "d";
   private static final String STROKE_COLOR_ATT_NAME = "stroke";
   private static final String STROKE_WIDTH_ATT_NAME = "stroke-width";
   
   private static final String HEIGHT_KEY = "height";
   private static final String WIDTH_KEY = "width";
   
   private static final String BG_COLOR_KEY = "bcolor";
   
   private static final String PAPER_TYPE_KEY = "paper";
   
   private static final String BG_NAME_KEY = "bgid";
   
   private JarnalPageLoadedListener listener;
   private ZipFile zipFile;
   private int pageNum;
   
   private int width;
   private int height;
   private float scale;
   private Color bgColor;
   private JarnalPaperType paperType;
   private String bgName;
   
   private Vector<Stroke> strokeVec;
   
   private StringBuffer descBuffer;
   private boolean inDesc;
   
   public JarnalPageLoader(ZipFile zipFile, 
                           int pageNum, 
                           float scale, 
                           JarnalPageLoadedListener listener) 
                              throws ParserConfigurationException, 
                                     SAXException, 
                                     IOException
   {
      // validate the input
      if (zipFile == null || listener == null)
         throw new NullPointerException();
      
      this.scale = scale;
      
      this.listener = listener;
      this.pageNum = pageNum;
      this.width = 0;
      this.height = 0;
      
      this.strokeVec = new Vector<Stroke>();
      
      this.descBuffer = new StringBuffer();
      this.inDesc = false;
      
      this.zipFile = zipFile;
   }
   
   private void readPageConfig() throws SAXException
   {
      // construct the stream to read the configuration text
      StringInputStream configStream = 
                           new StringInputStream(this.descBuffer.toString());
      
      // load the configuration
      Properties config = new Properties();
      try
      {
         config.load(configStream);
      }
      catch (IOException e)
      {
         throw new SAXException(e);
      }
      
      // determine the page's width
      Object obFound = config.get(WIDTH_KEY);
      this.width = 0;
      try
      {
         if (obFound != null)
            this.width = Integer.parseInt(obFound.toString());
      }
      catch (NumberFormatException e)
      {
         this.width = 0;
      }
      
      // determine the page's height
      obFound = config.get(HEIGHT_KEY);
      this.height = 0;
      try
      {
         if (obFound != null)
            this.height = Integer.parseInt(obFound.toString());
      }
      catch (NumberFormatException e)
      {
         this.height = 0;
      }
      
      // determine the page's background color
      obFound = config.get(BG_COLOR_KEY);
      this.bgColor = Color.WHITE;
      try
      {
         int colVal = Integer.parseInt(obFound.toString());
         this.bgColor = new Color(colVal);
      }
      catch (NumberFormatException e)
      {
         this.bgColor = Color.WHITE;
      }
      
      // determine the page's paper type
      obFound = config.get(PAPER_TYPE_KEY);
      this.paperType = JarnalPaperType.Lined;
      if (obFound != null)
         this.paperType = JarnalPaperType.findJarnalPaperType(obFound.toString());
      
      // determine the name of the background image
      obFound = config.get(BG_NAME_KEY);
      this.bgName = NO_BG_NAME;
      if (obFound != null)
         this.bgName = obFound.toString();
   }
   
   public boolean hasBackgroundImage()
   {
      return !this.bgName.equalsIgnoreCase(NO_BG_NAME);
   }
   
   private static Color constructColor(String colorStr)
   {
      if (colorStr == null)
         return Color.BLACK;
      
      if (colorStr.equalsIgnoreCase("BLACK"))
         return Color.BLACK;
      else if (colorStr.equalsIgnoreCase("BLUE"))
         return Color.BLUE;
      else if (colorStr.equalsIgnoreCase("RED"))
         return Color.RED;
      else if (colorStr.equalsIgnoreCase("GREEN"))
         return Color.GREEN;
      else if (colorStr.equalsIgnoreCase("GRAY"))
         return Color.GRAY;
      else if (colorStr.equalsIgnoreCase("GREY"))
         return Color.GRAY;
      else if (colorStr.equalsIgnoreCase("MAGENTA"))
         return Color.MAGENTA;
      else if (colorStr.equalsIgnoreCase("CYAN"))
         return Color.CYAN;
      else if (colorStr.equalsIgnoreCase("WHITE"))
         return Color.WHITE;
      else if (colorStr.equalsIgnoreCase("ORANGE"))
         return Color.ORANGE;
      else if (colorStr.equalsIgnoreCase("YELLOW"))
         return Color.YELLOW;
      else if (colorStr.equalsIgnoreCase("PINK"))
         return Color.PINK;
      
      return Color.BLACK;
   }
   
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException
   {
      super.characters(ch, start, length);
      
      if (this.inDesc)
      {
         for (int i=start; i<= start+length; i++)
            this.descBuffer.append(ch[i]);
      }
   }

   @Override
   public void endDocument() throws SAXException
   {
      super.endDocument();
      
      Page page = new Page(this.width, this.height, 
                           PaperType.CollegeRuled, 
                           this.scale, this.scale, 
                           Unit.getScreenResolution(),
                           1);
      
      Paper paper = page.getPaper();
      paper.setBackgroundColor(this.bgColor);
      paper.setPaperType(this.paperType.getNoteLabPaperType());
      paper.adaptToUnitFactor(1);
      
      for (Stroke stroke : this.strokeVec)
         page.addStroke(stroke);
      
      this.listener.pageLoaded(page, this.pageNum, hasBackgroundImage(), 
                               getMessageBuffer().toString());
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      super.endElement(uri, localName, qName);
      
      if (localName.equals(DESC_NAME))
      {
         this.inDesc = false;
         readPageConfig();
      }
   }
   
   @Override
   public void startDocument() throws SAXException
   {
      super.startDocument();
   }

   @Override
   public void startElement(String uri, String localName, 
                            String qName, Attributes attributes) throws SAXException
   {
      super.startElement(uri, localName, qName, attributes);
      
      if (localName.equals(DESC_NAME))
         this.inDesc = true;
      else if (localName.equals(PATH_NAME))
      {
         String pathText = attributes.getValue(PATH_ATT_NAME);
         Path path = new Path(this.scale, this.scale);
         fillPath(path, pathText, this.scale);
         
         String colorStr = attributes.getValue(STROKE_COLOR_ATT_NAME);
         Color color = constructColor(colorStr);
         
         String widthStr = attributes.getValue(STROKE_WIDTH_ATT_NAME);
         float width = 1;
         try
         {
            width = Float.parseFloat(widthStr);
         }
         catch (NumberFormatException e)
         {
            width = 1;
         }
         
         Stroke stroke = new Stroke(new Pen(width, color, this.scale), path);
         stroke.setIsStable(true);
         this.strokeVec.add(stroke);
      }
   }
   
   public void run()
   {
      try
      {
         // locate the entry for the given page
         ZipEntry pageEntry = this.zipFile.getEntry("p"+this.pageNum+".svg");
         if (pageEntry == null)
            throw new IOException("The page "+pageNum+" could not be found.");
         
         // create a parser factory
         SAXParserFactory parserFac = SAXParserFactory.newInstance();
         parserFac.setNamespaceAware(true);
         parserFac.setValidating(true);
         
         // get a parser
         SAXParser parser = parserFac.newSAXParser();
         
         // parse the xml document describing the page
         parser.parse(this.zipFile.getInputStream(pageEntry), this);
      }
      catch (Exception e)
      {
         this.listener.pageInvalid(this.pageNum, e);
      }
   }
   
   public static void main(String[] args) throws Exception
   {
      ZipFile file = new ZipFile("/home/kramer/Desktop/JarnalFiles/" +
                                 "extract_morepages2/morepages.zip");
      
      JarnalPageLoadedListener listener = new JarnalPageLoadedListener()
      {
         public void pageLoaded(Page page, int pageNumber, 
                                boolean usesBgImage, String message)
         {
         }

         public void pageInvalid(int pageNumber, Exception e)
         {
         }
      };
      
      JarnalPageLoader loader = new JarnalPageLoader(file, 0, 1, listener);
   }
}
