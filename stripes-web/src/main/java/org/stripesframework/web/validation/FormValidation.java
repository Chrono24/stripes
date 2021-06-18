package org.stripesframework.web.validation;

import java.util.Collections;
import java.util.Set;

import org.stripesframework.web.action.SingleBeanForm;


public class FormValidation {

   private SingleBeanForm<?> form;
   private Set<String>       on;

   public SingleBeanForm<?> getForm() {
      return form;
   }

   public Set<String> getOn() {
      if ( on == null ) {
         return Collections.emptySet();
      }
      return on;
   }

   public void setForm( SingleBeanForm<?> form ) {
      this.form = form;
   }

   public void setOn( Set<String> on ) {
      this.on = on;
   }
}
