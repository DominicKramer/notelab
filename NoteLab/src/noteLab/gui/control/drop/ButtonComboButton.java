package noteLab.gui.control.drop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import noteLab.gui.DefinedIcon;
import noteLab.gui.button.IconedButton;

public class ButtonComboButton 
                extends ImageComboButton 
                           implements ActionListener
{
   private ButtonGroup buttonGroup;
   private boolean firstButton;
   
   public ButtonComboButton(DefinedIcon initialIcon)
   {
      super(initialIcon);
      
      this.buttonGroup = new ButtonGroup();
      this.firstButton = true;
   }
   
   public <T extends AbstractButton & IconedButton> 
                                    void registerButton(T button)
   {
      if (button == null)
         throw new NullPointerException();
      
      button.addActionListener(this);
      this.buttonGroup.add(button);
      
      if (this.firstButton)
      {
         button.doClick();
         this.firstButton = false;
      }
   }

   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      if ( !(source instanceof IconedButton) )
         return;
      
      IconedButton button = (IconedButton)source;
      setActionCommand(event.getActionCommand());
      setControlValue(button.getDefinedIcon());
      getPopupWindow().setVisible(false);
      repaint();
   }
}
