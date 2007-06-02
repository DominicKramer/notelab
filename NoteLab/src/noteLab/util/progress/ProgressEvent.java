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

package noteLab.util.progress;

public class ProgressEvent
{
   private String mainMessage;
   private String subMessage;
   private String errorMessage;
   private boolean isIndeterminate;
   private int percent;
   private boolean isComplete;
   
   public ProgressEvent(String mainMessage, 
                        String subMessage, 
                        String errorMessage, 
                        boolean isIndeterminate, 
                        int percent, 
                        boolean isComplete)
   {
      this.mainMessage = mainMessage;
      this.subMessage = subMessage;
      this.errorMessage = errorMessage;
      this.isIndeterminate = isIndeterminate;
      this.percent = percent;
      this.isComplete = isComplete;
   }
   
   public String getErrorMessage()
   {
      return this.errorMessage;
   }
   
   public boolean isComplete()
   {
      return this.isComplete;
   }
   
   public boolean isIndeterminate()
   {
      return this.isIndeterminate;
   }
   
   public String getMainMessage()
   {
      return this.mainMessage;
   }
   
   public int getPercent()
   {
      return this.percent;
   }
   
   public String getSubMessage()
   {
      return this.subMessage;
   }
}
