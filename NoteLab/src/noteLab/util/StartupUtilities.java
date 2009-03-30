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

package noteLab.util;

import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.main.MainFrame;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.arg.Argument;
import noteLab.util.arg.CombFactorArg;
import noteLab.util.arg.CommandInterpretor;
import noteLab.util.arg.CurrentDirectoryArg;
import noteLab.util.arg.DebugArgGenerator;
import noteLab.util.arg.DebugMenuArg;
import noteLab.util.arg.HistorySizeArg;
import noteLab.util.arg.LookAndFeelArg;
import noteLab.util.arg.PaperColorArg;
import noteLab.util.arg.PaperTypeArg;
import noteLab.util.arg.PenColorArg;
import noteLab.util.arg.PenSizeArg;
import noteLab.util.arg.PrintArg;
import noteLab.util.arg.SmoothFactorArg;
import noteLab.util.arg.UnitScaleArg;
import noteLab.util.arg.VersionArg;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;
import noteLab.util.io.noteLab.NoteLabFileLoader;
import noteLab.util.settings.SettingsKeys;

/**
 * This class serves as the launchpad which starts the application.
 * 
 * @author Dominic Kramer
 */
public class StartupUtilities implements SettingsKeys
{
   /** Private to enforce the static use of this class's members. */
   private StartupUtilities() {}
   
   /**
    * Launches the NoteLab application.
    * 
    * @param args The command line arguments supplied to the 
    *             application at startup.
    */
   public static void main(String[] args)
   {
      // The InfoCenter classfile needs to be loaded by the virtual 
      // machine at the start of the application so that it determine 
      // the intial amount of memory given to the application.  
      // The virtual machine will load the InfoCenter class when 
      // the following code invoked.
      
      // build/validate NoteLab's home directory
      InfoCenter.buildAppHome();
      
      //create an interpretor for the command line arguments
      CommandInterpretor interpretor = new CommandInterpretor();
      
      //register the possible arguments
      interpretor.registerArgument(new VersionArg());
      
      interpretor.registerArgument(new PenSizeArg(PEN_1_SIZE_KEY, 1));
      interpretor.registerArgument(new PenSizeArg(PEN_2_SIZE_KEY, 2));
      interpretor.registerArgument(new PenSizeArg(PEN_3_SIZE_KEY, 3));
      
      interpretor.registerArgument(new PenColorArg(PEN_1_COLOR_KEY, 1));
      interpretor.registerArgument(new PenColorArg(PEN_2_COLOR_KEY, 2));
      interpretor.registerArgument(new PenColorArg(PEN_3_COLOR_KEY, 3));
      
      interpretor.registerArgument(new PaperTypeArg());
      interpretor.registerArgument(new PaperColorArg());
      
      interpretor.registerArgument(new LookAndFeelArg());
      
      interpretor.registerArgument(new HistorySizeArg());
      
      interpretor.registerArgument(new CombFactorArg());
      interpretor.registerArgument(new SmoothFactorArg());
      
      interpretor.registerArgument(new UnitScaleArg());
      
      interpretor.registerArgument(new CurrentDirectoryArg());
      
      interpretor.registerArgument(new DebugMenuArg());
      
      interpretor.registerArgument(new PrintArg());
      
      Argument[] debugArgs = DebugArgGenerator.generateDebugArgs();
      for (Argument arg : debugArgs)
         interpretor.registerArgument(arg);
      
      //interpret the command line arguments
      boolean success = interpretor.processCommands(args);
      
      //if there aren't any problems start the gui
      if (success)
      {
         Vector<File> files = interpretor.getSpecifiedFiles();
         if (files.isEmpty())
            new MainFrameLoader();
         else
            for (File file : files)
               new MainFrameLoader(file);
      }
   }
   
   /**
    * This class is responsible for instructing a 
    * <code>NoteLabFileLoader</code> to load a <code>CompositeCanvas</code> 
    * and construct a <code>MainFrame</code> after the canvas has been 
    * loaded.
    * 
    * @author Dominic Kramer
    */
   private static class MainFrameLoader implements NoteLabFileLoadedListener
   {
      /**
       * Constructs a loader which loads a <code>MainFrame</code> 
       * not loaded from a file.
       */
      public MainFrameLoader()
      {
         this(null);
      }
      
      /**
       * Constructs a loader which loads a <code>CompositeCanvas</code> 
       * from the given file and constructs a <code>MainFrame</code> 
       * for the canvas.
       * 
       * @param file The file which is to be loaded or <code>null</code> 
       *             if the <code>MainFrame</code> should be loaded 
       *             whithout reading a file.
       */
      public MainFrameLoader(File file)
      {
         if (file == null)
            launchMainFrame(null);
         else
         {
            try
            {
               new NoteLabFileLoader(file, this).loadFile();
            }
            catch (Exception e)
            {
               int size = GuiSettingsConstants.BUTTON_SIZE;
               ImageIcon icon = DefinedIcon.dialog_error.getIcon(size);
               
               JOptionPane.showMessageDialog(null, 
                                             e.getClass().getName()+":  "+
                                                  e.getMessage(), 
                                             "Error Loading File", 
                                             JOptionPane.ERROR_MESSAGE, 
                                             icon);
            }
         }
      }
      
      /**
       * Invoked when a NoteLab native file has been loaded.
       * 
       * @param canvas The <code>CompositeCanvas</code> that 
       *               has just been loaded.
       */
      public void noteLabFileLoaded(CompositeCanvas canvas, String message)
      {
         if (message != null && message.length() > 0)
         {
            int size = GuiSettingsConstants.BUTTON_SIZE;
            ImageIcon icon = DefinedIcon.dialog_info.getIcon(size);
            
            JOptionPane.showMessageDialog(new JFrame(), 
                                          message, 
                                          "Notice", 
                                          JOptionPane.INFORMATION_MESSAGE, 
                                          icon);
         }
         
         launchMainFrame(canvas);
      }
      
      /**
       * This method serves to create a <code>MainFrame</code> 
       * using the given <code>CompositeCanvas</code>.  The 
       * <code>MainFrame</code> startup code is placed on the 
       * AWT event dispatching queue and the frame is loaded 
       * when the system is ready.
       * 
       * @param canvas If <code>null</code> a new 
       *               <code>MainFrame</code> not based on a 
       *               <code>CompositeCanvas<code> is created.  
       *               Otherwise, a <code>MainFrame</code> 
       *               based on the given 
       *               <code>CompositeCanvas</code> is 
       *               constructed.
       */
      private void launchMainFrame(final CompositeCanvas canvas)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               MainFrame frame = null;
               if (canvas == null)
               {
                  frame = new MainFrame();
                  frame.setVisible(true);
               }
               else
               {
                  frame = new MainFrame(canvas);
                  frame.setVisible(true);
               }
               
               if (frame != null)
                  frame.hasBeenSaved();
            }
         });
      }
   }
}
