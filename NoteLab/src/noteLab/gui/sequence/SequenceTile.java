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

package noteLab.gui.sequence;

import java.util.Vector;

import javax.swing.JPanel;

public abstract class SequenceTile extends JPanel implements SequenceTileListener
{
   private SequenceTile prevTile;
   private SequenceTile nextTile;
   private ProceedType proceedType;
   
   private boolean hasNext;
   private boolean hasPrevious;
   
   private Vector<SequenceTileListener> listenerVec;
   
   public SequenceTile(SequenceTile prevTile, boolean hasPrev, boolean hasNext)
   {
      this.prevTile = prevTile;
      this.nextTile = null;
      
      this.hasNext = hasNext;
      this.hasPrevious = hasPrev;
      
      this.listenerVec = new Vector<SequenceTileListener>();
      
      this.proceedType = ProceedType.can_not_proceed;
      
      addSequenceTileListener(this);
   }
   
   public boolean hasPreviousTile()
   {
      return this.hasPrevious;
   }
   
   public boolean hasNextTile()
   {
      return this.hasNext;
   }
   
   public SequenceTile getPreviousTile()
   {
      return this.prevTile;
   }
   
   public SequenceTile getNextTile()
   {
      return this.nextTile;
   }
   
   public void setNextTile(SequenceTile nextTile)
   {
      if (nextTile == null)
         throw new NullPointerException();
      
      this.nextTile = nextTile;
   }
   
   public ProceedType getProceedType()
   {
      return this.proceedType;
   }
   
   public void addSequenceTileListener(SequenceTileListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeSequenceTileListener(SequenceTileListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   protected void notifyTileProceedChanged(ProceedType proceed)
   {
      for (SequenceTileListener listener : this.listenerVec)
         listener.tileProceedChanged(this, proceed);
   }
   
   public void tileProceedChanged(SequenceTile tile, ProceedType type)
   {
      if (type == null || tile == null)
         throw new NullPointerException();
      
      if (tile == this)
         this.proceedType = type;
   }
   
   public abstract void sequenceCancelled();
   public abstract void sequenceCompleted();
}
