package noteLab.util.arg;

import java.io.File;

import noteLab.util.InfoCenter;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsUtilities;

public class CurrentDirectoryArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = new ParamInfo[1];
   static
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("The directory from which ");
      buffer.append(InfoCenter.getAppName());
      buffer.append(" will start open and save dialogs from.");
      
      PARAM_DESCS[0] = new ParamInfo("dirname", buffer.toString());
   }
   
   private static final String DESC = "Used to set the current " +
   		                                "working directory.";
   
   public CurrentDirectoryArg()
   {
      super(SettingsKeys.CURRENT_DIR, 1, PARAM_DESCS, DESC, false);
   }
   
   public String encode(String dirname)
   {
      if (dirname == null)
         throw new NullPointerException();
      
      return PREFIX+getIdentifier()+" "+dirname;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      boolean failed = false;
      
      if (args.length == 0)
      {
         System.out.println("No directory was specified to set as the " +
         		              "current working directory.");
         failed = true;
      }
      
      File curDir = new File(args[0]);
      if (!curDir.exists())
      {
         System.out.println("The directory specified for the current " +
         		             "working directory '" + 
         		             args[0]+"' does not exist.");
         failed = true;
      }
      
      if (curDir.exists() && !curDir.isDirectory())
      {
         System.out.print("The directory specified for the current " +
                          "working directory '" + 
                          args[0]+"' is not actually a directory.  ");
         File parent = curDir.getParentFile();
         if (parent != null)
         {
            System.out.println("Using the file's parent directory, '" + 
                               parent.getAbsolutePath() + "'.");
            curDir = parent;
         }
         else
            System.out.println();
      }
      
      if (failed || curDir == null)
         SettingsUtilities.setCurrentDirectory(InfoCenter.getUserHome());
      else
         SettingsUtilities.setCurrentDirectory(curDir.getAbsolutePath());
      
      return ArgResult.SHOW_GUI;
   }
}
