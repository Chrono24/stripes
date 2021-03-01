package net.sourceforge.stripes.validation;

public class BusinessValueFormAsWrapper {

   static ThreadLocal<Integer> counter           = ThreadLocal.withInitial(() -> 1);
   static ThreadLocal<Integer> validateAlwaysRan = ThreadLocal.withInitial(() -> 0);
   static ThreadLocal<Integer> validateOneRan    = ThreadLocal.withInitial(() -> 0);
   static ThreadLocal<Integer> validateTwoRan    = ThreadLocal.withInitial(() -> 0);

   private final BusinessValue businessValue = new BusinessValue();

   @SuppressWarnings("unused")
   public int getNumberOne() {
      return businessValue.getNumberOne();
   }

   @SuppressWarnings("unused")
   public int getNumberTwo() {
      return businessValue.getNumberTwo();
   }

   @SuppressWarnings("unused")
   public int getNumberZero() {
      return businessValue.getNumberZero();
   }

   @Validate(required = true, minvalue = 0)
   public void setNumberOne( int numberOne ) {
      businessValue.setNumberOne(numberOne);
   }

   @Validate()
   public void setNumberTwo( int numberTwo ) {
      businessValue.setNumberTwo(numberTwo);
   }

   @Validate(required = true)
   public void setNumberZero( int numberZero ) {
      businessValue.setNumberZero(numberZero);
   }

   @ValidationMethod(priority = 0)
   @SuppressWarnings("DefaultAnnotationParam")
   public void validateAlways( ValidationErrors errors ) {
      if ( errors == null ) {
         throw new RuntimeException("errors must not be null");
      }
      validateAlwaysRan.set(counter.get());
      counter.set(counter.get() + 1);
   }

   @ValidationMethod(priority = 1, when = ValidationState.NO_ERRORS)
   public void validateOne() {
      validateOneRan.set(counter.get());
      counter.set(counter.get() + 1);
   }

   @ValidationMethod(priority = 1, when = ValidationState.ALWAYS)
   public void validateTwo( ValidationErrors errors ) {
      validateTwoRan.set(counter.get());
      counter.set(counter.get() + 1);
   }
}
