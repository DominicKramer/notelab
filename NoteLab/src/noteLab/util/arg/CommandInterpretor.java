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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import noteLab.util.InfoCenter;

public class CommandInterpretor
{
   private Hashtable<String, Argument> argTable;
   private Vector<File> fileVec;
   
   public CommandInterpretor()
   {
      this.argTable = new Hashtable<String, Argument>();
      this.fileVec = new Vector<File>();
      
      registerArgument(new HelpArgument());
   }
   
   public void registerArgument(Argument arg)
   {
      if (arg == null)
         throw new NullPointerException();
      
      this.argTable.put(arg.getIdentifier(), arg);
   }
   
   public Vector<File> getSpecifiedFiles()
   {
      return this.fileVec;
   }
   
   public boolean processCommands(String cmmdStr)
   {
      if (cmmdStr == null)
         throw new NullPointerException();
      
      StringTokenizer tokenizer = new StringTokenizer(cmmdStr);
      String[] cmmdArr = new String[tokenizer.countTokens()];
      for (int i=0; i<cmmdArr.length; i++)
         cmmdArr[i] = tokenizer.nextToken();
      
      return processCommands(cmmdArr);
   }
   
   public boolean processCommands(String[] cmmdArr)
   {
      if (cmmdArr == null)
         throw new NullPointerException();
      
      if (cmmdArr.length == 0)
         return true;
      
      // The interpretor seems to need to have a "--" at the end of the 
      // command list if no files are specified if the commands are to 
      // be understood without running into an ArrayIndexOutOfBoundsException.
      // This code inserts a "--" command at the end of the command list 
      // if it was not specified as a command in the command list.
      boolean terminatorFound = false;
      for (String str : cmmdArr)
         if (str.equals(Argument.PREFIX))
            terminatorFound = true;
      
      if (!terminatorFound)
      {
         String[] newCmmdArr = new String[cmmdArr.length+1];
         System.arraycopy(cmmdArr, 0, newCmmdArr, 0, cmmdArr.length);
         newCmmdArr[newCmmdArr.length-1] = Argument.PREFIX;
         
         cmmdArr = newCmmdArr;
      }
      
      int index = 0;
      do
      {
         if (index < cmmdArr.length)
            index = processArgument(cmmdArr, index);
      } while (index > 0 && index < cmmdArr.length);
      
      
      if (index == 0)
         return false;
      
      index *= -1;
      
      for (int i=index; i<cmmdArr.length; i++)
         this.fileVec.add(new File(cmmdArr[i]));
      
      return true;
   }
   
   private int processArgument(String[] cmmdArr, int index)
   {
      if (index < 0 || index >= cmmdArr.length)
      {
         System.out.println("Error:  Unexpected end of arguments");
         return 0;
      }
      
      String id = cmmdArr[index];
      
      if (id.equals(Argument.PREFIX))
         return -1*(index+1);
      
      if (!id.startsWith(Argument.PREFIX))
      {
         System.out.println("Error:  The command '"+id+"' was expected " +
                            "to start with '"+Argument.PREFIX+"'");
         
         return 0;
      }
      
      String idName = id.substring(2);
      
      Argument arg = this.argTable.get(idName);
      if (arg == null)
      {
         System.out.println("Error:  Unknown argument identifier '"+id+"'");
         
         return 0;
      }
      
      int numArgs = arg.getNumArgs();
      int numArgsLeft = cmmdArr.length-index-1;
      
      if (numArgsLeft < numArgs)
      {
         System.out.println("Error:  The argument '"+id+"' needs "+numArgs+
                            " parameters but only "+numArgsLeft+" were " +
                            " found");
         
         return 0;
      }
      
      ArgResult result;
      String[] paramArr = new String[numArgs];
      for (int i=0; i<numArgs; i++)
      {
         index++;
         paramArr[i] = cmmdArr[index];
         
         if (paramArr[i].startsWith(Argument.PREFIX))
         {
            System.out.println("Error:  The argument '"+id+
                               "' is missing needed parameters.");
            
            return 0;
         }
      }
      
      if (arg.isDeprecated())
      {
         System.out.println("Warning:  The argument '"+id+
                            "' is deprecated and will be ignored.");
         System.out.println("          Future versions of "+InfoCenter.getAppName()+
                            " may not support this argument.");
         
         return index+1;
      }
      
      result = arg.decode(paramArr);
      
      if (result == null || result == ArgResult.ERROR)
      {
         System.out.println(InfoCenter.getAppName()+" could not be started because " +
                            "the argument "+Argument.PREFIX+arg.getIdentifier()+
                            " has an error.");
         
         return 0;
      }
      else if (result == ArgResult.NO_SHOW_GUI)
         return 0;
      
      return index+1;
   }
   
   private class HelpArgument extends Argument
   {
      public HelpArgument()
      {
         super("help", 0, new ParamInfo[0], "Prints this help message", false);
      }

      @Override
      public ArgResult decode(String[] args)
      {
         StringBuffer buffer = new StringBuffer("Usage:  ");
         buffer.append(InfoCenter.getAppName());
         buffer.append(" [parameters] [-- file1");
         buffer.append(InfoCenter.getFileExtension());
         buffer.append(" file2");
         buffer.append(InfoCenter.getFileExtension());
         buffer.append(" ....]\n\n");
         
         /*
         buffer.append("Note:  If files are specified they must be ");
         buffer.append(InfoCenter.getFileExtension());
         buffer.append(" files.\n");
         
         for (int i=1; i<=55; i++)
            buffer.append("-");
         buffer.append("\n\n");
         */
         
         buffer.append(InfoCenter.getDescription());
         
         buffer.append("\n\nArguments:\n----------\n\n");
         
         Enumeration<String> keys = argTable.keys();
         String curKey;
         Argument curArg;
         ParamInfo[] paramInfo;
         while (keys.hasMoreElements())
         {
            curKey = keys.nextElement();
            curArg = argTable.get(curKey);
            
            if (curArg != null)
            {
               buffer.append(PREFIX);
               buffer.append(curKey);
               
               paramInfo = curArg.getParamDescriptions();
               if (paramInfo != null)
               {
                  for (ParamInfo info : paramInfo)
                  {
                     buffer.append(" ");
                     buffer.append(info.getName());
                  }
                  
                  buffer.append("\n  ");
                  buffer.append(curArg.getDescription());
                  buffer.append("\n\n");
                  
                  if (paramInfo.length != 0)
                  {
                     buffer.append("  Parameters:\n  -----------\n");
                  }
                  
                  for (ParamInfo info : paramInfo)
                  {
                     buffer.append("  ");
                     buffer.append(info.getName());
                     buffer.append("\n    ");
                     buffer.append(info.getDescription());
                     buffer.append("\n");
                  }
               }
            }
            
            buffer.append("\n\n");
         }
         
         System.out.println(buffer.toString());
         
         return ArgResult.NO_SHOW_GUI;
      }
   }
}
