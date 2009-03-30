package noteLab.util.arg;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.io.noteLab.NoteLabFileLoadedListener;
import noteLab.util.io.noteLab.NoteLabFileLoader;

public class PrintArg extends Argument 
                         implements NoteLabFileLoadedListener
{
   private static final ParamInfo[] PARAM_DESCS = 
      new ParamInfo[]
      {
         new ParamInfo("option", 
                       "Show the print options pane, " +
                       "either true or false."),
         
         new ParamInfo("file", 
                       "The "+InfoCenter.getAppName()+
                       " file to print.")
      };
   
   private boolean showDialog;
   
   public PrintArg()
   {
      super("print", 2, PARAM_DESCS, 
            "Print the given "+InfoCenter.getAppName()+
               " file.", 
            false);
      
      this.showDialog = false;
   }
   
   public String encode(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      return PREFIX+getIdentifier()+" "+file.getAbsolutePath();
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      try
      {
         this.showDialog = Boolean.getBoolean(args[0]);
      }
      catch (NumberFormatException e)
      {
         System.out.println(e.getMessage());
         
         return ArgResult.ERROR;
      }
      
      File file = new File(args[1]);
      
      try
      {
         new NoteLabFileLoader(file, this).loadFile();
      }
      catch (Exception e)
      {
         System.out.println("An error occured while printing the " +
         		             "file.  The error returned was:  ");
         System.out.println();
         System.out.println(e.getMessage());
         
         return ArgResult.ERROR;
      }
      
      return ArgResult.NO_SHOW_GUI;
   }

   public void noteLabFileLoaded(CompositeCanvas canvas, String message)
   {
      if (message != null && message.length() > 0)
      {
         System.out.println(message);
         
         return;
      }
      
      final PrinterJob printerJob = PrinterJob.getPrinterJob();
      printerJob.setPageable(canvas.getBinder());
      
      boolean notCancelled = true;
      if (this.showDialog)
         notCancelled = printerJob.printDialog();
      
      if (notCancelled)
      {
         try
         {
            printerJob.print();
         }
         catch (PrinterException e)
         {
            System.out.println(e.getMessage());
         }
      }
   }
}
