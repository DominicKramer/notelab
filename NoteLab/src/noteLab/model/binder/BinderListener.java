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

package noteLab.model.binder;

import noteLab.model.Page;

/**
 * Classes implement this interface if they want to be informed when a <code>Page</code> was 
 * added or removed from a <code>Binder</code> or if the <code>Binder</code>'s current page 
 * has changed.
 * 
 * @author Dominic Kramer
 */
public interface BinderListener
{
   /**
    * Invoked when a <code>Page</code> has been added to a <code>Binder</code>.
    * 
    * @param source The <code>Binder</code> to which the <code>Page</code> was added.
    * 
    * @param page The <code>Page</code> that was added.
    */
   public void pageAdded(Binder source, Page page);
   
   /**
    * Invoked when a <code>Page</code> has been added to a <code>Binder</code>.
    * 
    * @param source The <code>Binder</code> from which the <code>Page</code> was removed.
    * 
    * @param page The <code>Page</code> that was removed.
    */
   public void pageRemoved(Binder source, Page page);
   
   /**
    * Invoked when a <code>Binder</code>'s current <code>Page</code> has changed.
    * 
    * @param source The <code>Binder</code> whose current <code>Page</code> has changed.
    */
   public void currentPageChanged(Binder source);
}
