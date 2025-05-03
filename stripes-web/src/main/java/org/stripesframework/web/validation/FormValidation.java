package org.stripesframework.web.validation;

import java.util.Collections;
import java.util.Set;

import org.stripesframework.web.action.SingleBeanForm;


public class FormValidation {

   private SingleBeanForm<?> _form;
   private Set<String>       _on;

   public SingleBeanForm<?> getForm() {
      return _form;
   }

   public Set<String> getOn() {
      if ( _on == null ) {
         return Collections.emptySet();
      }
      return _on;
   }

   public void setForm( SingleBeanForm<?> form ) {
      _form = form;
   }

   public void setOn( Set<String> on ) {
      _on = on;
   }
}
