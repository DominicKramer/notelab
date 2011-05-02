package noteLab.util;

import java.util.StringTokenizer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class LookAndFeelUtilities
{
   private static final String NIMBUS_NAME = "Nimbus";
   private static final String NIMBUS_LOWER_CASE_NAME = 
                                  NIMBUS_NAME.toLowerCase();
   
   private static final String METAL_LOWER_CASE_NAME = "metal";
   
   private static final String DEFAULT_LOOK_AND_FEEL;
   
   private static final String[] LOOK_AND_FEELS;
   static
   {
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      
      LOOK_AND_FEELS = new String[infoArr.length];
      
      String sysLafClassname = UIManager.getSystemLookAndFeelClassName();
      String sysLafGluedName = null;
      String nimbusGluedName = null;
      
      String classname;
      String name;
      String gluedName;
      for (int i=0; i<infoArr.length; i++)
      {
         classname = infoArr[i].getClassName();
         name = infoArr[i].getName();
         gluedName = getGluedString(name);
         
         if (classname.equalsIgnoreCase(sysLafClassname))
            sysLafGluedName = gluedName;
         else if (classname.toLowerCase().contains(NIMBUS_LOWER_CASE_NAME))
            nimbusGluedName = gluedName;
         
         LOOK_AND_FEELS[i] = gluedName;
      }
      
      if (sysLafGluedName == null)
         DEFAULT_LOOK_AND_FEEL = NIMBUS_NAME;
      else
      {
         if (nimbusGluedName == null)
            DEFAULT_LOOK_AND_FEEL = sysLafGluedName;
         else
         {
            if (sysLafGluedName.toLowerCase().contains(METAL_LOWER_CASE_NAME))
               DEFAULT_LOOK_AND_FEEL = nimbusGluedName;
            else
               DEFAULT_LOOK_AND_FEEL = sysLafClassname;
         }
      }
   }
   
   // private to enforce static access to this class
   private LookAndFeelUtilities() {}
   
   private static String getGluedString(String str)
   {
      if (str.indexOf(' ') == -1)
         return str;
      
      StringBuffer newBuffer = new StringBuffer();
      StringTokenizer tokenizer = new StringTokenizer(str);
      while (tokenizer.hasMoreTokens())
         newBuffer.append(tokenizer.nextToken());
      
      return newBuffer.toString();
   }
   
   public static String getDefaultLookAndFeel()
   {
      return DEFAULT_LOOK_AND_FEEL;
   }
   
   public static String[] getLookAndFeels()
   {
      return LOOK_AND_FEELS;
   }
   
   public static boolean setLookAndFeel(String gluedLaf)
   {
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      
      boolean found = false;
      for (LookAndFeelInfo info : infoArr)
      {
         if (getGluedString(info.getName()).equalsIgnoreCase(gluedLaf))
         {
            try
            {
               UIManager.setLookAndFeel(info.getClassName());
               found = true;
            }
            catch (Exception e)
            {
               throw new IllegalArgumentException("The look and feel \""+
                                  gluedLaf+"\" could not be loaded.  " +
                                  "The error type is \""+
                                  e.getCause().getClass().getName()+
                                  "\" and the error message returned was \""+
                                  e.getMessage()+"\"");
            }
            break;
         }
      }
      
      if (found)
         SettingsManager.getSharedInstance().
            setValue(SettingsKeys.LOOK_AND_FEEL_KEY, gluedLaf);
      
      return found;
   }
}
