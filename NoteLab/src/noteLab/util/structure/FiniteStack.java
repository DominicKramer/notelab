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

package noteLab.util.structure;

/**
 * This class represents a stack of a finite number of elements.  Stacks follow the first 
 * in first out paradigm.
 * 
 * @author Dominic Kramer
 * 
 * @param <E> The type of the element stored in the stack.
 */
public class FiniteStack<E>
{
   /** The array of elements. */
   private E[] stackArr;
   
   /**
    * The index of the last element added to the stack or 
    * <code>-1</code> if the stack is empty.
    */
   private int curIndex;
   
   /** The number of elements stored in the stack. */
   private int curLength;
   
   /**
    * Constructs a stack that can hold the specified number of elements.
    * 
    * @param size The number of elements that can be stored in the stack.
    */
   public FiniteStack(int size)
   {
      if (size <= 0)
         throw new IllegalArgumentException(
                      "The size of a stack cannot be " +
                      "zero or negative.  The requested " +
                      "size was "+size);
      
      this.stackArr = (E[])new Object[size];
      this.curLength = 0;
      this.curIndex = -1;
   }
   
   /**
    * Used to get the number of elements currently stored in the stack.
    * 
    * @return The number of elements in the stack.
    */
   public int getSize()
   {
      return this.curLength;
   }
   
   /**
    * Used to determine if the stack is empty.
    * 
    * @return <code>true</code> if the stack is empty and <code>false</code> 
    *         if not.
    */
   public boolean isEmpty()
   {
      return getSize() == 0;
   }
   
   /**
    * Used to retrieve the top element of the stack without removing it from the 
    * stack.
    * 
    * @return The top element of the stack or <code>null</code> if the stack 
    *         is empty.
    */
   public E peek()
   {
      if (isEmpty())
         return null;
      
      return this.stackArr[this.curIndex];
   }
   
   /**
    * Used to pop the top element off of the stack.
    * 
    * @return The top element of the stack or <code>null</code> if the stack 
    *         is empty.
    */
   public E pop()
   {
      E top = peek();
      if (top == null)
         return null;
      
      this.curIndex = calcIndex(this.curIndex, false);
      this.curLength--;
      return top;
   }
   
   /**
    * Used to push an element on the stack.
    * 
    * @param ob The element to push on the stack.
    * 
    * @return The element pushed on the stack.
    */
   public E push(E ob)
   {
      this.curIndex = calcIndex(this.curIndex, true);
      this.stackArr[this.curIndex] = ob;
      this.curLength++;
      if (this.curLength > this.stackArr.length)
         this.curLength = this.stackArr.length;
         
      return ob;
   }
   
   /**
    * Removes all of the elements in the stack.
    */
   public void clear()
   {
      this.curIndex = -1;
      this.curLength = 0;
   }
   
   public void setSize(int size)
   {
      if (size < 0)
         throw new IllegalArgumentException("The size of a "+
                                            FiniteStack.class.getSimpleName()+
                                            " cannot be negative.  The size " +
                                            "specified was '"+
                                            size+
                                            "'");
      
      if (size == this.stackArr.length)
         return;
      
      int initSize = getSize();
      
      FiniteStack<E> newStack = new FiniteStack<E>(size);
      E top;
      while ( (top = pop()) != null && newStack.getSize() < size)
         newStack.push(top);
      
      this.stackArr = newStack.stackArr;
      this.curIndex = (size > 0)?0:-1;
      this.curLength = Math.min(initSize, size);
   }
   
   /**
    * Used to get a string representation of this stack that is used for debugging 
    * purposes.
    * 
    * @return A detailed description of the stack.
    */
   public String debugToString()
   {
      if (isEmpty())
         return "The stack is empty.";
      
      StringBuffer buffer = 
         new StringBuffer("The stack contains:  ");
      
      E[] stackArrCp = (E[])new Object[this.stackArr.length];
      System.arraycopy(this.stackArr, 0, stackArrCp, 0, 
                       this.stackArr.length);
      
      FiniteStack<E> stackCp = 
         new FiniteStack<E>(this.stackArr.length);
      stackCp.stackArr = stackArrCp;
      stackCp.curIndex = this.curIndex;
      stackCp.curLength = this.curLength;
      
      E top;
      while ( (top = stackCp.pop()) != null)
      {
         buffer.append("{");
         buffer.append(top.toString());
         buffer.append("} ");
      }
      
      return buffer.toString();
   }
   
   /**
    * Used to get a user-friendly description of the stack.
    * 
    * @return A string representation of the stack.
    */
   @Override
   public String toString()
   {
      StringBuffer buffer = 
         new StringBuffer("Stack; size=");
      
      buffer.append(getSize());
      
      if (isEmpty())
         return buffer.toString();
      
      buffer.append(" contents=");
      
      int length = this.curLength;
      int index = this.curIndex;
      while (length > 0)
      {
         buffer.append("{");
         buffer.append(this.stackArr[index]);
         buffer.append("} ");
         
         index = calcIndex(index, false);
         length--;
      }
      
      return buffer.toString();
   }
   
   /**
    * Used to calculate the next index.  This method implements modular arithmetic.  That 
    * is if the index is at the end of the array of elements, it loops back to 0.
    * 
    * @param index The current index.
    * 
    * @param increase <code>true</code> if the index should be increased by one and 
    *                 <code>false</code> if it should be decreased.
    * 
    * @return The new index.
    */
   private int calcIndex(int index, boolean increase)
   {
      if (increase)
         return (index+1)%this.stackArr.length;
      
      if (index > 0)
         return index-1;
      
      return this.stackArr.length-1;
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      FiniteStack<String> stack = new FiniteStack<String>(5);
      System.out.println("Pushing '1' onto the stack");
      stack.push("1");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '2' onto the stack");
      stack.push("2");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '3' onto the stack");
      stack.push("3");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '4' onto the stack");
      stack.push("4");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '5' onto the stack");
      stack.push("5");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '6' onto the stack");
      stack.push("6");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '7' onto the stack");
      stack.push("7");
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pop = "+stack.pop());
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '1' onto the stack");
      stack.push("1");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '2' onto the stack");
      stack.push("2");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '3' onto the stack");
      stack.push("3");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '4' onto the stack");
      stack.push("4");
      System.out.println(stack.debugToString());
      
      System.out.println("Clearing the stack");
      stack.clear();
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '5' onto the stack");
      stack.push("5");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '6' onto the stack");
      stack.push("6");
      System.out.println(stack.debugToString());
      
      System.out.println("Pushing '7' onto the stack");
      stack.push("7");
      System.out.println(stack.debugToString());
   }
}
