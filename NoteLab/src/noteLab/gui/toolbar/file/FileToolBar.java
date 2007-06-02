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
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.chooser.NoteLabFileChooser;
import noteLab.gui.chooser.filter.ImageFileFilter;
import noteLab.gui.chooser.filter.JarnalFileFilter;
import noteLab.gui.chooser.filter.NoteLabFileFilter;
import noteLab.gui.chooser.filter.SupportedFileFilter;
import noteLab.gui.main.MainFrame;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.Menued;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.settings.SettingsFrame;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.progress.ProgressEvent;

public class FileToolBar 
                extends JToolBar 
                           implements ActionListener, 
                                      Menued, 
                                      GuiSettingsConstants
{
   private static final String NEW = "new";
   private static final String SAVE = "save";
   private static final String SAVE_AS = "save as";
   private static final String OPEN = "open";
   private static final String EXPORT = "export";
   private static final String PRINT = "print";
   private static final String SETTINGS = "settings";
   
   private MainFrame mainFrame;
   private Vector<PathMenuItem> menuItemVec;
   
   public FileToolBar(MainFrame mainFrame)
   {
      if (mainFrame == null)
         throw new NullPointerException();
      
      this.mainFrame = mainFrame;
      
      JButton saveButton = 
         new JButton(DefinedIcon.floppy.getIcon(BUTTON_SIZE));
      saveButton.addActionListener(this);
      saveButton.setActionCommand(SAVE);
      
      JButton newButton = 
         new JButton(DefinedIcon.page.getIcon(BUTTON_SIZE));
      newButton.addActionListener(this);
      newButton.setActionCommand(NEW);
      
      JButton openButton = 
         new JButton(DefinedIcon.directory.getIcon(BUTTON_SIZE));
      openButton.addActionListener(this);
      openButton.setActionCommand(OPEN);
      
      JButton exportButton = 
         new JButton(DefinedIcon.image_page.getIcon(BUTTON_SIZE));
      exportButton.addActionListener(this);
      exportButton.setActionCommand(EXPORT);
      
      JButton printButton = 
         new JButton(DefinedIcon.print.getIcon(BUTTON_SIZE));
      printButton.addActionListener(this);
      printButton.setActionCommand(PRINT);
      
      add(newButton);
      add(openButton);
      add(saveButton);
      add(exportButton);
      add(printButton);
      
      //make the menu items
      this.menuItemVec = new Vector<PathMenuItem>();
      
      ImageIcon newIcon = DefinedIcon.page.getIcon(16);
      ImageIcon openIcon = DefinedIcon.directory.getIcon(16);
      ImageIcon saveIcon = DefinedIcon.floppy.getIcon(16);
      ImageIcon saveAsIcon = DefinedIcon.save_as.getIcon(16);
      ImageIcon exportIcon = DefinedIcon.image_page.getIcon(16);
      ImageIcon printIcon = DefinedIcon.print.getIcon(16);
      ImageIcon settingsIcon = DefinedIcon.preferences.getIcon(16);
      
      //make the menu items
      JMenuItem newItem = new JMenuItem("New", newIcon);
      newItem.setActionCommand(NEW);
      newItem.addActionListener(this);
      
      JMenuItem openItem = new JMenuItem("Open", openIcon);
      openItem.setActionCommand(OPEN);
      openItem.addActionListener(this);
      
      JMenuItem saveItem = new JMenuItem("Save", saveIcon);
      saveItem.setActionCommand(SAVE);
      saveItem.addActionListener(this);
      
      JMenuItem saveAsItem = new JMenuItem("Save As", saveAsIcon);
      saveAsItem.setActionCommand(SAVE_AS);
      saveAsItem.addActionListener(this);
      
      JMenuItem exportItem = new JMenuItem("Export", exportIcon);
      exportItem.setActionCommand(EXPORT);
      exportItem.addActionListener(this);
      
      JMenuItem printItem = new JMenuItem("Print", printIcon);
      printItem.setActionCommand(PRINT);
      printItem.addActionListener(this);
      
      JMenuItem settingsItem = new JMenuItem("Settings", settingsIcon);
      settingsItem.setActionCommand(SETTINGS);
      settingsItem.addActionListener(this);
      
      //add the menu items to the menu vector
      this.menuItemVec.add(new PathMenuItem(newItem, 
                                            MenuConstants.FILE_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(openItem, 
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
      
      String cmmd = e.getActionCommand();
      if (cmmd.equals(NEW))
      {
         new MainFrame().setVisible(true);
      }
      else if (cmmd.equals(OPEN))
      {
         OpenFileProcessor processor = new OpenFileProcessor();
         NoteLabFileChooser openChooser = 
                           new NoteLabFileChooser("Open", true, false, processor);
         openChooser.setAcceptAllFileFilterUsed(false);
         openChooser.addChoosableFileFilter(new JarnalFileFilter());
         openChooser.addChoosableFileFilter(new NoteLabFileFilter());
         openChooser.addChoosableFileFilter(new SupportedFileFilter());
         openChooser.showFileChooser();
      }
      else if (cmmd.equals(SAVE))
      {
         // the boolean argument is false so that saving doesn't force 
         // the "save as" option
         save(false);
      }
      else if (cmmd.equals(SAVE_AS))
      {
         // the boolean argument is true so that saving forces  
         // the "save as" option
         save(true);
      }
      else if (cmmd.equals(EXPORT))
      {
         ExportFileProcessor processor = 
                  new ExportFileProcessor(this.mainFrame);
         NoteLabFileChooser exportChooser = 
                  new NoteLabFileChooser("Export", false, true, processor);
         exportChooser.setAcceptAllFileFilterUsed(false);
         exportChooser.addChoosableFileFilter(new NoteLabFileFilter());
         exportChooser.setFileFilter(new ImageFileFilter());
         
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
   
   public void save(boolean forceSaveAs)
   {
      final File file = this.mainFrame.getCompositeCanvas().getFile();
      if (!forceSaveAs && file != null)
      {
         new Thread(new Runnable()
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
         }).start();
         
         return;
      }
      
      SaveFileProcessor processor = new SaveFileProcessor(this.mainFrame);
      NoteLabFileChooser saveChooser = 
                            new NoteLabFileChooser("Save", false, true, processor);
      saveChooser.setAcceptAllFileFilterUsed(false);
      saveChooser.setFileFilter(new NoteLabFileFilter());
      saveChooser.showFileChooser();
   }
}
