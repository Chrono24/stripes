package org.stripesframework.web.validation;

public class BusinessValue {

   private int numberZero;
   private int numberOne;
   private int numberTwo;

   @SuppressWarnings("unused")
   public int getNumberOne() {
      return numberOne;
   }

   @SuppressWarnings("unused")
   public int getNumberTwo() {
      return numberTwo;
   }

   @SuppressWarnings("unused")
   public int getNumberZero() {
      return numberZero;
   }

   public void setNumberOne( int numberOne ) {
      this.numberOne = numberOne;
   }

   public void setNumberTwo( int numberTwo ) {
      this.numberTwo = numberTwo;
   }

   public void setNumberZero( int numberZero ) {
      this.numberZero = numberZero;
   }
}
