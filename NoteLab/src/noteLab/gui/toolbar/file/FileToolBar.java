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

package noteLab.gui.toolbar.file;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.Tooltips;
import noteLab.gui.chooser.NoteLabFileChooser;
import noteLab.gui.chooser.filter.ImageFileFilter;
import noteLab.gui.chooser.filter.JarnalFileFilter;
import noteLab.gui.chooser.filter.NoteLabFileFilter;
import noteLab.gui.chooser.filter.PDFFileFilter;
import noteLab.gui.chooser.filter.SupportedFileFilter;
import noteLab.gui.fullscreen.FullScreenManager;
import noteLab.gui.main.MainFrame;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.Menued;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.settings.SettingsFrame;
import noteLab.model.Page;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.model.pdf.PDFPageInfo;
import noteLab.util.InfoCenter;
import noteLab.util.progress.ProgressEvent;

public class FileToolBar 
                extends JToolBar 
                           implements ActionListener, 
                                      Menued, 
                                      GuiSettingsConstants
{
   private static final String NEW = "new";
   private static final String ANNOTATE_PDF = "annotate pdf";
   private static final String SAVE = "save";
   private static final String SAVE_AS = "save as";
   private static final String OPEN = "open";
   private static final String EXPORT = "export";
   private static final String EXPORT_PDF = "export PDF";
   private static final String PRINT = "print";
   private static final String SETTINGS = "settings";
   
   private MainFrame mainFrame;
   private Vector<PathMenuItem> menuItemVec;
   
   public FileToolBar(MainFrame mainFrame)
   {
      if (mainFrame == null)
         throw new NullPointerException();
      
      setFloatable(false);
      
      this.mainFrame = mainFrame;
      
      JButton saveButton = 
         new JButton(DefinedIcon.floppy.getIcon(BUTTON_SIZE));
      saveButton.addActionListener(this);
      saveButton.setActionCommand(SAVE);
      saveButton.setToolTipText(Tooltips.SAVE);
      
      JButton newButton = 
         new JButton(DefinedIcon.page.getIcon(BUTTON_SIZE));
      newButton.addActionListener(this);
      newButton.setActionCommand(NEW);
      newButton.setToolTipText(Tooltips.NEW);
      
      JButton openButton = 
         new JButton(DefinedIcon.directory.getIcon(BUTTON_SIZE));
      openButton.addActionListener(this);
      openButton.setActionCommand(OPEN);
      openButton.setToolTipText(Tooltips.OPEN);
      
      JButton exportButton = 
         new JButton(DefinedIcon.image_page.getIcon(BUTTON_SIZE));
      exportButton.addActionListener(this);
      exportButton.setActionCommand(EXPORT);
      exportButton.setToolTipText(Tooltips.EXPORT);
      
      JButton printButton = 
         new JButton(DefinedIcon.print.getIcon(BUTTON_SIZE));
      printButton.addActionListener(this);
      printButton.setActionCommand(PRINT);
      printButton.setToolTipText(Tooltips.PRINT);
      
      add(newButton);
      add(openButton);
      add(saveButton);
      add(exportButton);
      add(printButton);
      
      //make the menu items
      this.menuItemVec = new Vector<PathMenuItem>();
      
      ImageIcon newIcon = DefinedIcon.page.getIcon(16);
      ImageIcon pdfIcon = DefinedIcon.document.getIcon(16);
      ImageIcon openIcon = DefinedIcon.directory.getIcon(16);
      ImageIcon saveIcon = DefinedIcon.floppy.getIcon(16);
      ImageIcon saveAsIcon = DefinedIcon.save_as.getIcon(16);
      ImageIcon exportIcon = DefinedIcon.image_page.getIcon(16);
      ImageIcon printIcon = DefinedIcon.print.getIcon(16);
      ImageIcon settingsIcon = DefinedIcon.preferences.getIcon(16);
      
      //make the menu items
      JMenuItem newItem = new JMenuItem("New", newIcon);
      newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
                                                    InputEvent.CTRL_DOWN_MASK, 
                                                    true));
      newItem.setActionCommand(NEW);
      //newItem.setToolTipText(Tooltips.NEW);
      newItem.addActionListener(this);
      
      JMenuItem openItem = new JMenuItem("Open", openIcon);
      openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
                                                     InputEvent.CTRL_DOWN_MASK, 
                                                     true));
      openItem.setActionCommand(OPEN);
      //openItem.setToolTipText(Tooltips.OPEN);
      openItem.addActionListener(this);
      
      JMenuItem pdfItem = new JMenuItem("Annotate PDF", pdfIcon);
      pdfItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
                                          InputEvent.CTRL_DOWN_MASK | 
                                             InputEvent.SHIFT_DOWN_MASK, 
                                          true));
      pdfItem.setActionCommand(ANNOTATE_PDF);
      //pdfItem.setToolTipText(Tooltips.ANNOTATE_PDF);
      pdfItem.addActionListener(this);
      
      JMenuItem saveItem = new JMenuItem("Save", saveIcon);
      saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                                                     InputEvent.CTRL_DOWN_MASK, 
                                                     true));
      saveItem.setActionCommand(SAVE);
      //saveItem.setToolTipText(Tooltips.SAVE);
      saveItem.addActionListener(this);
      
      JMenuItem saveAsItem = new JMenuItem("Save As", saveAsIcon);
      saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                                             InputEvent.CTRL_DOWN_MASK | 
                                                InputEvent.SHIFT_DOWN_MASK, 
                                             true));
      saveAsItem.setActionCommand(SAVE_AS);
      //saveAsItem.setToolTipText(Tooltips.SAVE_AS);
      saveAsItem.addActionListener(this);
      
      JMenuItem exportItem = new JMenuItem("Export", exportIcon);
      exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 
                                                       InputEvent.CTRL_DOWN_MASK, 
                                                       true));
      exportItem.setActionCommand(EXPORT);
      //exportItem.setToolTipText(Tooltips.EXPORT);
      exportItem.addActionListener(this);
      
      JMenuItem exportPdfItem = new JMenuItem("Export PDF", pdfIcon);
      exportPdfItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 
                                                InputEvent.CTRL_DOWN_MASK | 
                                                   InputEvent.SHIFT_DOWN_MASK, 
                                                true));
      exportPdfItem.setActionCommand(EXPORT_PDF);
      //exportPdfItem.setToolTipText(Tooltips.EXPORT_PDF);
      exportPdfItem.addActionListener(this);
      
      JMenuItem printItem = new JMenuItem("Print", printIcon);
      printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 
                                                      InputEvent.CTRL_DOWN_MASK, 
                                                      true));
      printItem.setActionCommand(PRINT);
      //printItem.setToolTipText(Tooltips.PRINT);
      printItem.addActionListener(this);
      
      JMenuItem settingsItem = new JMenuItem("Settings", settingsIcon);
      settingsItem.setActionCommand(SETTINGS);
      //settingsItem.setToolTipText(Tooltips.SETTTINGS);
      settingsItem.addActionListener(this);
      
      //add the menu items to the menu vector
      this.menuItemVec.add(new PathMenuItem(newItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(openItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(pdfItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(saveItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(saveAsItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(exportItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(exportPdfItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(printItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(settingsItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                            MenuConstants.FILE_MENU_PATH));
   }
   
   public List<PathMenuItem> getPathMenuItems()
   {
      return this.menuItemVec;
   }
   
   public void actionPerformed(ActionEvent e)
   {
      final CompositeCanvas canvas = this.mainFrame.getCompositeCanvas();
      File file = canvas.getFile();
      
      String cmmd = e.getActionCommand();
      if (cmmd.equals(NEW))
      {
         new MainFrame().setVisible(true);
      }
      else if (cmmd.equals(OPEN))
      {
         OpenFileProcessor processor = new OpenFileProcessor();
         NoteLabFileChooser openChooser = 
                           new NoteLabFileChooser("Open", true, false, processor, file);
         openChooser.setAcceptAllFileFilterUsed(false);
         openChooser.addChoosableFileFilter(new JarnalFileFilter());
         openChooser.addChoosableFileFilter(new NoteLabFileFilter());
         openChooser.addChoosableFileFilter(new SupportedFileFilter());
         openChooser.showFileChooser();
      }
      else if (cmmd.equals(ANNOTATE_PDF))
      {
         OpenFileProcessor processor = new OpenFileProcessor();
         NoteLabFileChooser openChooser = 
                           new NoteLabFileChooser("Annotate PDF", true, false, processor, file);
         openChooser.setAcceptAllFileFilterUsed(false);
         openChooser.addChoosableFileFilter(new PDFFileFilter());
         openChooser.showFileChooser();
      }
      else if (cmmd.equals(SAVE))
      {
         // the boolean argument is false so that saving doesn't force 
         // the "save as" option
         save(false, false);
      }
      else if (cmmd.equals(SAVE_AS))
      {
         // the boolean argument is true so that saving forces  
         // the "save as" option
         save(true, false);
      }
      else if (cmmd.equals(EXPORT) || cmmd.equals(EXPORT_PDF))
      {
         boolean exportPDF = cmmd.equals(EXPORT_PDF);
         
         String defaultExt = InfoCenter.getPNGExt();
         if (exportPDF)
            defaultExt = InfoCenter.getPDFExtension();
         
         ExportFileProcessor processor = 
                  new ExportFileProcessor(this.mainFrame, defaultExt);
         NoteLabFileChooser exportChooser = 
                  new NoteLabFileChooser("Export", false, true, processor, file);
         exportChooser.setAcceptAllFileFilterUsed(false);
         // PDF files can always be selected.
         exportChooser.addChoosableFileFilter(new PDFFileFilter());
         // If the user did not select "Export PDF", then also allow 
         // native files and image files.
         if (!exportPDF)
         {
            exportChooser.addChoosableFileFilter(new NoteLabFileFilter());
            exportChooser.setFileFilter(new ImageFileFilter());
         }
         
         File savedFile = canvas.getFile();
         // If the file has the same name as the file to 
         // which the canvas was saved, trim of the file's 
         // extension.
         if (savedFile != null)
         {
            String name = savedFile.getName();
            if (name == null)
               name = "";
            
            String ext = InfoCenter.getFileExtension();
            if (ext == null)
               ext = "";
            
            if (name.endsWith(ext))
               name = name.substring(0, name.length()-ext.length());
            
            exportChooser.setSelectedFile(new File(name));
         }
         
         exportChooser.showFileChooser();
      }
      else if (cmmd.equals(PRINT))
      {
         final PrinterJob printerJob = PrinterJob.getPrinterJob();
         printerJob.setPageable(canvas.getBinder());
         
         FullScreenManager.getSharedInstance().revokeFullScreenMode();
         boolean notCancelled = printerJob.printDialog();
         if (notCancelled)
         {
            new Thread((new Runnable()
            {
               public void run()
               {
                  try
                  {
                     synchronized(canvas)
                     {
                        mainFrame.setMessage("Printing the session requires momentarily " +
                                             "disabling the canvas.", Color.BLACK);
                        canvas.setEnabled(false);
                        mainFrame.progressOccured(new ProgressEvent(null, null, null, 
                                                                    true, 0, false));
                        printerJob.print();
                        mainFrame.progressOccured(new ProgressEvent(null, null, null, 
                                                                    true, 0, true));
                        canvas.setEnabled(true);
                        mainFrame.setMessage("Printing completed sucessfully.", Color.BLACK);
                     }
                  }
                  catch (PrinterException printEx)
                  {
                     CanvasFileProcessor.notifyOfThrowable(printEx);
                     mainFrame.setMessage("Printing failed.", Color.RED);
                  }
               }
            })).start();
         }
      }
      else if (cmmd.equals(SETTINGS))
      {
         new SettingsFrame().setVisible(true);
      }
   }
   
   public void save(boolean forceSaveAs, boolean block)
   {
      final File file = this.mainFrame.getCompositeCanvas().getFile();
      if (!forceSaveAs && file != null)
      {
         Runnable saver = new Runnable()
         {
            public void run()
            {
               CanvasFileProcessor.saveAsSVG(mainFrame, 
                                             file, 
                                             InfoCenter.getFileExtension(), 
                                             true, // Zip the file 
                                             true, // Report progress to the user 
                                             "Saving the session");
            }
         };
         
         if (block)
            saver.run();
         else
            new Thread(saver).start();
         
         return;
      }
      
      SaveFileProcessor processor = new SaveFileProcessor(this.mainFrame);
      NoteLabFileChooser saveChooser = 
                            new NoteLabFileChooser("Save", false, true, 
                                                   processor, file);
      saveChooser.setAcceptAllFileFilterUsed(false);
      saveChooser.setFileFilter(new NoteLabFileFilter());
      if (file == null || forceSaveAs)
      {
         PDFPageInfo pageInfo = null;
         for (Page page : this.mainFrame.getCompositeCanvas().getBinder())
         {
            pageInfo = page.getPaper().getPDFPageInfo();
            if (pageInfo != null)
               break;
         }
         
         if (pageInfo == null)
            saveChooser.setSelectedFile(getDateFile());
         else
         {
            String name = pageInfo.getFileInfo().getSource().getName();
            int index = name.toLowerCase().
                                lastIndexOf(InfoCenter.getPDFExtension().
                                                          toLowerCase());
            if (index > 0)
               name = name.substring(0, index);
            
            saveChooser.
               setSelectedFile(new File(name+
                                        InfoCenter.getFileExtension()));
         }
      }
      
      saveChooser.showFileChooser();
   }
   
   private static File getDateFile()
   {
      Calendar cal = Calendar.getInstance();
      DecimalFormat formatter = new DecimalFormat("00");
      
      StringBuffer timeBuffer = new StringBuffer();
      timeBuffer.append(formatter.format(cal.get(Calendar.MONTH)+1));
      timeBuffer.append("-");
      timeBuffer.append(formatter.format(cal.get(Calendar.DAY_OF_MONTH)));
      timeBuffer.append("-");
      timeBuffer.append(cal.get(Calendar.YEAR));
      timeBuffer.append("_");
      timeBuffer.append(formatter.format(cal.get(Calendar.HOUR_OF_DAY)));
      timeBuffer.append("-");
      timeBuffer.append(formatter.format(cal.get(Calendar.MINUTE)));
      
      return new File(timeBuffer.toString()+
                         InfoCenter.getFileExtension());
   }
}
