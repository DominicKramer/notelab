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

package noteLab.gui.chooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.fullscreen.FullScreenManager;
import noteLab.util.settings.SettingsUtilities;

public class NoteLabFileChooser extends JFileChooser implements ActionListener
{
   private static final String OVERWRITE_TITLE = "Warning";
   private static final String OVERWRITE_MESSAGE_PRE = 
                                              "The file '";
   private static final String OVERWRITE_MESSAGE_POST = 
                                              "' already exists.  Overwrite?";
   
   private static File LAST_DIR;
   
   private String approveText;
   private boolean checkOverwrite;
   private FileProcessor processor;
   
   public NoteLabFileChooser(String approveText, 
                             boolean multiSelection, 
                             boolean checkOverwrite, 
                             FileProcessor processor)
   {
      this(approveText, multiSelection, 
           checkOverwrite, processor, 
           new File(SettingsUtilities.getCurrentDirectory()));
   }
   
   public NoteLabFileChooser(String approveText, 
                             boolean multiSelection, 
                             boolean checkOverwrite, 
                             FileProcessor processor, 
                             File curDir)
   {
      if (curDir != null)
         setCurrentDirectory(curDir);
      else
         setCurrentDirectory(LAST_DIR);
      
      if (approveText == null || processor == null)
         throw new NullPointerException();
      
      if (approveText.trim().equals(""))
         approveText = "  ";
      
      this.approveText = approveText;
      this.checkOverwrite = checkOverwrite;
      this.processor = processor;
      
      setFileView(new NoteLabFileView());
      setMultiSelectionEnabled(multiSelection);
      setFileSelectionMode(JFileChooser.FILES_ONLY);
      addActionListener(this);
   }
   
   @Override
   public void setCurrentDirectory(File dir)
   {
      if (dir == null)
         dir = new File(SettingsUtilities.getCurrentDirectory());
      
      super.setCurrentDirectory(dir);
      LAST_DIR = dir;
   }
   
   public void actionPerformed(ActionEvent e)
   {
      LAST_DIR = getCurrentDirectory();
   }
   
   public void showFileChooser()
   {
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      FullScreenManager.getSharedInstance().revokeFullScreenMode();
      int returnVal = showDialog(frame, this.approveText);
      
      if (returnVal != JFileChooser.APPROVE_OPTION)
         return;
      
      if (isMultiSelectionEnabled())
      {
         File[] selFiles = getSelectedFiles();
         for (File file : selFiles)
         {
            if (file == null)
               continue;
            
            processFile(this.processor.getFormattedName(file));
         }
      }
      else
      {
         File selFile = getSelectedFile();
         if (selFile != null)
            processFile(this.processor.getFormattedName(selFile));
      }
   }
   
   private void processFile(final File file)
   {
      new Thread(new Runnable()
      {
         public void run()
         {
            doProcessFile(file);
         }
      }).start();
   }
   
   private void doProcessFile(File file)
   {
      if (file.exists() && this.checkOverwrite)
      {
         int size = GuiSettingsConstants.BUTTON_SIZE;
         ImageIcon icon = DefinedIcon.dialog_question.getIcon(size);
         
         int ret = JOptionPane.showConfirmDialog(new JFrame(), 
                                                 OVERWRITE_MESSAGE_PRE+
                                                 file.getName()+
                                                 OVERWRITE_MESSAGE_POST, 
                                                 OVERWRITE_TITLE, 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.QUESTION_MESSAGE, 
                                                 icon);
         
         if (ret == JOptionPane.YES_OPTION)
            this.processor.processFile(file);
      }
      else
         this.processor.processFile(file);
   }
}
