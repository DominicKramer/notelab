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

package noteLab.gui.control.drop;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.TitledBorder;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.control.ValueControl;
import noteLab.gui.control.drop.ComboEvent.ActionType;
import noteLab.gui.control.drop.pic.ButtonPic;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.util.Selectable;

public abstract class ComboButton<V, S> 
                         extends JButton 
                                    implements MouseListener, 
                                               ComponentListener, 
                                               WindowListener, 
                                               ValueControl<V, S>, 
                                               Selectable
{
   private static final float ARROW_PERCENT = 0.25f;
   private static final int MIN_ARROW_WIDTH = 10;
   private static final Color SELECTION_COLOR = Color.DARK_GRAY;
   
   private JWindow popupWindow;
   private ButtonPic pic;
   private int arrowWidth;
   
   protected Vector<ValueChangeListener<V, S>> valueListenerVec;
   protected Vector<ComboListener> eventListenerVec;
   
   private Vector<ActionListener> actionListenerVec;
   
   public ComboButton(ButtonPic pic)
   {
      super(getEmptyIcon(GuiSettingsConstants.BUTTON_SIZE));
      this.arrowWidth = calculateArrowWidth(GuiSettingsConstants.BUTTON_SIZE);
      
      setFocusPainted(false);
      setSelected(false);
      
      this.actionListenerVec = new Vector<ActionListener>();
      
      this.valueListenerVec = new Vector<ValueChangeListener<V, S>>();
      this.eventListenerVec = new Vector<ComboListener>();
      
      setButtonPic(pic);
      
      JPanel popupPane = new JPanel();
      popupPane.setBorder(new TitledBorder(""));
      
      this.popupWindow = new JWindow();
      this.popupWindow.setContentPane(popupPane);
      this.popupWindow.setAlwaysOnTop(true);
      
      this.popupWindow.pack();
      
      addMouseListener(this);
   }
   
   private static ImageIcon getEmptyIcon(int size)
   {
      ImageIcon icon = DefinedIcon.getEmptyIcon(size);
      
      int arrowWidth = calculateArrowWidth(size);
      
      Image scaledImage = icon.getImage().
                             getScaledInstance(size+arrowWidth, size, 
                                               Image.SCALE_FAST);
      return new ImageIcon(scaledImage);
   }
   
   private static int calculateArrowWidth(int size)
   {
      return Math.max((int)(ARROW_PERCENT*size), MIN_ARROW_WIDTH);
   }
   
   public JWindow getPopupWindow()
   {
      return this.popupWindow;
   }
   
   //this method is invoked when this button gets a new parent
   @Override
   public void addNotify()
   {
      super.addNotify();
      
      Container parent = this;
      while ( (parent != null) && !(parent instanceof Window) )
      {
         parent = parent.getParent();
      }
      
      if (parent != null)
      {
         Window parWin = (Window)parent;
         parWin.addComponentListener(this);
         parWin.addWindowListener(this);
         syncPopupWindow();
      }
      else
         System.out.println("DropDownButton:  Could not find parent window");
   }
   
   private void syncPopupWindow()
   {
      try
      {
         Point locOnScreen = getLocationOnScreen();
         locOnScreen.y += getHeight();
         locOnScreen.x += (int)( (1-ARROW_PERCENT)*getWidth() );
         
         this.popupWindow.setLocation(locOnScreen);
         this.popupWindow.pack();
      }
      catch (IllegalComponentStateException e)
      {
         //CHANGED:  This is ignored because if this exception is 
         //          thrown, the componenet is not visible yet.  
         //          This is not a problem because it will be soon 
         //          visible.
         //System.err.println(e);
      }
   }
   
   public void setPopupVisible(boolean visible)
   {
      this.popupWindow.setVisible(visible);
   }
   
   public boolean isPopupVisible()
   {
      return this.popupWindow.isVisible();
   }
   
   public ButtonPic getButtonPic()
   {
      return this.pic;
   }
   
   public void setButtonPic(ButtonPic pic)
   {
      if (pic == null)
         throw new NullPointerException();
      
      this.pic = pic;
   }
   
   public void addComboListener(ComboListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.eventListenerVec.contains(listener))
         this.eventListenerVec.add(listener);
   }
   
   public void removeComboListener(ComboListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.eventListenerVec.remove(listener);
   }
   
   @Override
   public void addActionListener(ActionListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.actionListenerVec.contains(listener))
         this.actionListenerVec.add(listener);
   }
   
   @Override
   public ActionListener[] getActionListeners()
   {
      return (ActionListener[])this.actionListenerVec.toArray();
   }
   
   @Override
   public void removeActionListener(ActionListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.actionListenerVec.remove(listener);
   }
   
   @Override
   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      
      Insets insets = getInsets();
      Dimension size = getSize();
      
      int rawWidth  = (int)(size.getWidth()-insets.left-insets.right);
      int rawHeight = (int)(size.getHeight()-insets.top-insets.bottom);
      int rawArrowWidth = this.arrowWidth - insets.right;
      
      g.translate(insets.left, insets.top);
      
      paintImage(g, rawWidth, rawHeight, rawArrowWidth);
      paintArrow(g, rawWidth, rawHeight, rawArrowWidth);
   }
   
   private void paintImage(Graphics g, int width, int height, int arrowWidth)
   {
      int picWidth = width-arrowWidth;
      int picHeight = height;
      
      this.pic.paintPic(g, picWidth, picHeight);
      
      if (isSelected())
      {
         g.setColor(SELECTION_COLOR);
         
         int boxSize = Math.min(picWidth, picHeight);
         int xOffset = (int)( (picWidth-boxSize)*0.5f );
         int yOffset = (int)( (picHeight-boxSize)*0.5f );
         g.drawRoundRect(xOffset, yOffset, boxSize-1, boxSize-1, 3, 3);
      }
   }
   
   private void paintArrow(Graphics g, int width, int height, int arrowWidth)
   {
      int midW = width - arrowWidth/2;
      int midH = height/2;
      int delta = (int)(Math.min(this.arrowWidth, height)*0.35f);
      
      int[] xPts = 
      {
         midW-delta, 
         midW+delta, 
         midW
      };
      
      int[] yPts = 
      {
         midH-delta, 
         midH-delta, 
         midH+delta
      };
      
      if (isEnabled())
         g.setColor(Color.DARK_GRAY);
      else
         g.setColor(Color.GRAY);
      
      g.fillPolygon(xPts, yPts, 3);
   }
   
   public void mouseClicked(MouseEvent event)
   {
   }

   public void mouseEntered(MouseEvent event)
   {
   }

   public void mouseExited(MouseEvent event)
   {
   }

   public void mousePressed(MouseEvent event)
   {
      if (!isEnabled())
         return;
      
      Insets insets = getInsets();
      int realWidth = getWidth();
      int boxWidth = realWidth-insets.left-insets.right;
      
      int max = realWidth;
      int min = (int)( (1-ARROW_PERCENT)*boxWidth+insets.left );
      
      int xLoc = event.getX();
      boolean isOnArrow = (xLoc >= min && xLoc <= max);
      
      if (isOnArrow)
      {
         syncPopupWindow();
         setPopupVisible(!isPopupVisible());
      }
      
      ActionType action = ActionType.main_type;
      if (isOnArrow)
         action = ActionType.arrow_type;
      
      // Notify the ComboListeners
      ComboEvent comboEvent = 
                    new ComboEvent(this, 
                                   event.getID(), 
                                   getActionCommand(), 
                                   action);
      for (ComboListener listener : this.eventListenerVec)
         listener.comboActionPerformed(comboEvent);
      
      // Notify the ActionListeners
      if (!isOnArrow)
      {
         ActionEvent actionEvent = 
                        new ActionEvent(this, 
                                        event.getID(), 
                                        getActionCommand());
         
         for (ActionListener listener : this.actionListenerVec)
            listener.actionPerformed(actionEvent);
      }
   }

   public void mouseReleased(MouseEvent event)
   {
   }
   
   public void componentResized(ComponentEvent e)
   {
      syncPopupWindow();
   }

   public void componentMoved(ComponentEvent e)
   {
      syncPopupWindow();
   }

   public void componentShown(ComponentEvent e)
   {
   }

   public void componentHidden(ComponentEvent e)
   {
   }

   public void windowOpened(WindowEvent e)
   {
   }

   public void windowClosing(WindowEvent e)
   {
   }

   public void windowClosed(WindowEvent e)
   {
      this.popupWindow.dispose();
   }

   public void windowIconified(WindowEvent e)
   {
      setPopupVisible(false);
   }

   public void windowDeiconified(WindowEvent e)
   {
   }

   public void windowActivated(WindowEvent e)
   {
   }

   public void windowDeactivated(WindowEvent e)
   {
   }
   
   public void addValueChangeListener(ValueChangeListener<V, S> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.valueListenerVec.contains(listener))
         this.valueListenerVec.add(listener);
   }
   
   public void removeValueChangeListener(ValueChangeListener<V, S> listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.valueListenerVec.remove(listener);
   }
   
   public abstract V getPreviousValue();
   
   /*
   public static void main(String[] args)
   {
      TestButton button = new TestButton(-1,Color.BLUE,true,Style.Circle,1);
      button.getPopupWindow().add(new JLabel("Some label"));
      button.setSelected(true);
      
      JFrame frame = new JFrame();
      frame.add(button);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
   */
}

