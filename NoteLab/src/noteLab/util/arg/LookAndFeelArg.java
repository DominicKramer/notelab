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

package noteLab.util.arg;

import java.util.StringTokenizer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class LookAndFeelArg extends Argument
{
   private static final String[] LOOK_AND_FEELS;
   private static final ParamInfo[] PARAM_DESCS;
   static
   {
      String prefix = "Specifies that the ";
      String suffix = " look and feel should be used to decorate the " +
                      "graphical elements.";
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      
      LOOK_AND_FEELS = new String[infoArr.length];
      PARAM_DESCS = new ParamInfo[infoArr.length];
      
      String name;
      String gluedName;
      for (int i=0; i<infoArr.length; i++)
      {
         name = infoArr[i].getName();
         gluedName = getGluedString(name);
         
         LOOK_AND_FEELS[i] = gluedName;
         PARAM_DESCS[i] = new ParamInfo(gluedName, 
                                        prefix+name+suffix);
      }
   }
   
   public LookAndFeelArg()
   {
      super("lookAndFeel", 1, PARAM_DESCS, 
            "Used to set the look and feel of the graphical elements.", 
            false);
   }
   
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
   
   public static String[] getLookAndFeels()
   {
      return LOOK_AND_FEELS;
   }
   
   public String encode(String lookAndFeel)
   {
      boolean found = false;
      for (String allowedLook : LOOK_AND_FEELS)
      {
         if (allowedLook.equals(lookAndFeel))
         {
            found = true;
            break;
         }
      }
      
      if (!found)
         throw new IllegalArgumentException("ERROR:  The look and feel '"+
                                            lookAndFeel+"' is not a valid " +
                                            "look and feel for this system.");
      
      return PREFIX+getIdentifier()+" "+lookAndFeel;
   }
   
   @Override
   public boolean decode(String[] args)
   {
      if (args.length < 1)
      {
         System.out.println("ERROR:  The look and feel could not be " +
                            "changed because "+LookAndFeelArg.class.getName()+
                            " was not properly given the look and feel to " +
                            "load");
         
         return true;
      }
      
      String lookAndFeel = args[0];
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      
      boolean found = false;
      for (LookAndFeelInfo info : infoArr)
      {
         if (getGluedString(info.getName()).equalsIgnoreCase(lookAndFeel))
         {
            found = true;
            try
            {
               UIManager.setLookAndFeel(info.getClassName());
            }
            catch (Exception e)
            {
               System.out.println("WARNING:  The look and feel \""+
                                  lookAndFeel+"\" could not be loaded.  " +
                                  "The error type is \""+
                                  e.getCause().getClass().getName()+
                                  "\" and the error message returned was \""+
                                  e.getMessage()+"\"");
            }
            break;
         }
      }
      
      if (!found)
         System.out.println("WARNING:  The look and feel \""+lookAndFeel+
                            "\" was not loaded because it was not " +
                            "recognized as a look and feel.");
      
      return true;
   }
   
   public static void main(String[] args)
   {
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      for (LookAndFeelInfo info : infoArr)
        System.out.println(info.getName()+" @ "+info.getClassName());
   }
}
