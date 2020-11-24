package net.sourceforge.stripes.validation;

import java.util.Collections;
import java.util.Set;

import net.sourceforge.stripes.action.Form;


public class FormValidation {

   private Form<?>     form;
   private Set<String> on;

   public Form<?> getForm() {
      return form;
   }

   public Set<String> getOn() {
      if ( on == null ) {
         return Collections.emptySet();
      }
      return on;
   }

   public void setForm( Form<?> form ) {
      this.form = form;
   }

   public void setOn( Set<String> on ) {
      this.on = on;
   }
}
