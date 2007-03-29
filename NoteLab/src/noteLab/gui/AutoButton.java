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

package noteLab.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * With a regular <code>JButton</code> an <code>ActionEvent</code> is 
 * sent to all listeners when the button is clicked.  A button of this 
 * type of the other hand sends <code>ActionEvents</code> every 
 * <code>t</code> milliseconds, where <code>t</code> can be adjusted.  
 * Specifically, the user must click the button.  Then within a given 
 * amount of time (called the initial delay), the user must press and 
 * hold the button down.  Then every <code>delay</code> milliseconds, 
 * an <code>ActionEvent</code> is sent to all of the button's listeners.
 * 
 * @author Dominic Kramer
 */
public class AutoButton extends JButton
{
   /**
    * The timer that times when <code>ActionEvents</code> should be 
    * sent to this button's listeners.
    */
   private Timer timer;
   
   /**
    * The previous <code>ActionEvent</code> that was just sent to all 
    * of this button's listeners.
    */
   private ActionEvent prevEvent;
   
   /**
    * This variable is set to <code>true</code> to specify that an 
    * <code>ActionEvent</code> sent to this class's 
    * <code>ActionListener</code> was sent from this class.
    */
   private boolean internalEvent;
   
   /** The time at which the user pressed and held down this button. */
   private long startTime;
   
   /**
    * Used to construct a button with the given icon.
    * 
    * @param icon The icon displayed on this button.
    * @param initDelay After pressing the button, this is the amount of 
    *                  time (in milliseconds) in which the user can 
    *                  press and hold down the button to have it 
    *                  continuously send <code>ActionEvents</code>.
    * @param delay The amount of time (in milliseconds) that this class 
    *              waits before resending <code>ActionEvents</code> to 
    *              its listeners (if this class's button is pressed down).
    */
   public AutoButton(Icon icon, int initDelay, int delay)
   {
      super(icon);
      init(initDelay, delay);
   }
   
   /**
    * Used to construct a button with the given test.
    * 
    * @param text The text displayed on this button.
    * @param initDelay After pressing the button, this is the amount of 
    *                  time (in milliseconds) in which the user can 
    *                  press and hold down the button to have it 
    *                  continuously send <code>ActionEvents</code>.
    * @param delay The amount of time (in milliseconds) that this class 
    *              waits before resending <code>ActionEvents</code> to 
    *              its listeners (if this class's button is pressed down).
    */
   public AutoButton(String text, int initDelay, int delay)
   {
      super(text);
      init(initDelay, delay);
   }
   
   /**
    * Used to initialize this button.
    * 
    * @param initDelay After pressing the button, this is the amount of 
    *                  time (in milliseconds) in which the user can 
    *                  press and hold down the button to have it 
    *                  continuously send <code>ActionEvents</code>.
    * @param delay The amount of time (in milliseconds) that this class 
    *              waits before resending <code>ActionEvents</code> to 
    *              its listeners (if this class's button is pressed down).
    */
   private void init(int initDelay, int delay)
   {
      addActionListener(new ButtonListener());
      this.timer = new Timer(initDelay, new TimerListener());
      this.timer.setDelay(delay);
      this.prevEvent = null;
      this.internalEvent = false;
      this.startTime = 0;
   }
   
   /**
    * This class listens to when this button is pressed.
    * 
    * @author Dominic Kramer
    */
   private class ButtonListener implements ActionListener
   {
      /** Invoked when the containing class's button is pressed. */
      public void actionPerformed(ActionEvent event)
      {
         if (internalEvent)
            return;
         
         internalEvent = false;
         
         long curTime = System.currentTimeMillis();
         if ( (curTime-startTime) <= timer.getInitialDelay())
            return;
         
         if (timer.isRunning())
            timer.stop();
         
         prevEvent = event;
         startTime = System.currentTimeMillis();
         timer.start();
      }
   }
   
   /**
    * This class listens to ticks from the containing class's timer.
    * 
    * @author Dominic Kramer
    */
   private class TimerListener implements ActionListener
   {
      /**
       * Invoked when a clock tick occurs.  If the containing 
       * class's button is being pressed when this tick occurs, 
       * this listener resends the previously sent 
       * <code>ActionEvent</code> to all of the containing class's 
       * listeners.
       */
      public void actionPerformed(ActionEvent event)
      {
         boolean isPressed = getModel().isPressed();
         if (isPressed)
         {
            internalEvent = true;
            fireActionPerformed(prevEvent);
         }   
         else
            timer.stop();
      }
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      JFrame frame = new JFrame("AutoButton Demo");
        frame.add(new AutoButton("AutoButton", 1000, 700));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
      frame.setVisible(true);
   }
}
