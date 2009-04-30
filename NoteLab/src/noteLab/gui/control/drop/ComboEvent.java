package noteLab.gui.control.drop;

import java.awt.event.ActionEvent;

public class ComboEvent extends ActionEvent
{
   public enum ActionType
   {
      main_type, 
      arrow_type
   }
   
   private ActionType type;
   
   public ComboEvent(Object source, int id, 
                     String command, int modifiers, 
                     ActionType type)
   {
      super(source, id, command, modifiers);
      setActionType(type);
   }

   public ComboEvent(Object source, int id, 
                     String command, long when,
                     int modifiers, ActionType type)
   {
      super(source, id, command, when, modifiers);
      setActionType(type);
   }

   public ComboEvent(Object source, int id, 
                     String command, ActionType type)
   {
      super(source, id, command);
      setActionType(type);
   }
   
   private void setActionType(ActionType type)
   {
      if (type == null)
         throw new NullPointerException();
      
      this.type = type;
   }
   
   public ActionType getActionType()
   {
      return this.type;
   }
}
