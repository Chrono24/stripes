package org.stripesframework.web.validation;

import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.SingleBeanForm;


public class BusinessValueForm implements SingleBeanForm<BusinessValue> {

   static ThreadLocal<Integer> counter           = ThreadLocal.withInitial(() -> 1);
   static ThreadLocal<Integer> validateAlwaysRan = ThreadLocal.withInitial(() -> 0);
   static ThreadLocal<Integer> validateOneRan    = ThreadLocal.withInitial(() -> 0);
   static ThreadLocal<Integer> validateTwoRan    = ThreadLocal.withInitial(() -> 0);

   private ActionBeanContext _context;
   private BusinessValue     _businessObject;

   @ValidateNestedProperties({ //
                               @Validate(field = "numberZero", required = true), //
                               @Validate(field = "numberOne", required = true, minvalue = 0), //
                               @Validate(field = "numberTwo") //
                             })
   @Override
   public BusinessValue getBean() {
      return _businessObject;
   }

   @Override
   public ActionBeanContext getContext() {
      return _context;
   }

   @Override
   public void setBean( BusinessValue businessObject ) {
      _businessObject = businessObject;
   }

   @Override
   public void setContext( ActionBeanContext context ) {
      _context = context;
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
