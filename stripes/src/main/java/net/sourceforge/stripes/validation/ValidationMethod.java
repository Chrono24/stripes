/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Annotation that marks a method in an ActionBean as a validation method. Validation methods
 * are invoked after required field checks, type conversion and validations specified in
 * {@literal @}Validate annotations, but before event handler methods.</p>
 *
 * <p>Validation methods must be public, may return any type (though any return will be ignored
 * by Stripes) and may throw any exceptions.  They make take either zero parameters, or a single
 * parameter of type {@link ValidationErrors}.  When validation methods are invoked all binding
 * will have taken place and the ActionBean will have access to the
 * {@link net.sourceforge.stripes.action.ActionBeanContext}, therefore methods that do not take
 * ValidationErrors as a parameter may retrieve it by calling
 * {@link net.sourceforge.stripes.action.ActionBeanContext#getValidationErrors()}.</p>
 *
 * <p>The attributes of this annotation confer significant control over when a validation method
 * will be run.  When a single ActionBean class has multiple validation methods the ordering
 * of them can be specified using the priority attribute.  Methods with lower values (i.e. nearer
 * zero) are executed before those with higher values.</p>
 *
 * <p>The {@code on} attribute controls which events the validation method should be invoked for.
 * It should be noted that the 'on' attribute is completely optional.  If omitted then the
 * validation method will be invoked for all events not annotated with {@literal @}DontValidate.
 * The on attribute can take one of two forms.  It can specify a list of events to apply the
 * validation method to, for example 'on={"save", "update"}', in which case it will be invoked only
 * for those events.  It can alternatively specify a list of events <i>not</i> to apply the
 * validation to, for example 'on="!delete"', in which case the validation will be run for all
 * events except those listed.</p>
 *
 * <p>The {@code when} attribute controls whether or not the validation method is executed when
 * one or more validation errors exist. It has no affect when there are no validation errors.
 * A value of {@link ValidationState#ALWAYS} will cause the method to be invoked even if errors
 * exist.  This is useful when you wish to perform additional validations that do not depend
 * on having a well-validated ActionBean since it allows the user to see more validation errors
 * at the same time.  A value of {@link ValidationState#NO_ERRORS} causes the method to be invoked
 * only when there are no pre-existing validation errors.  This is useful if the method relies on
 * a valid ActionBean and might throw exceptions otherwise.  The value
 * {@link ValidationState#DEFAULT} causes Stripes to apply the system level default for this
 * attribute.</p>
 *
 * <p>The default behaviour is such that if validation errors arise from the annotated
 * validations (or type conversion), validation methods will not be called (nor will the handler
 * method). This behaviour is configurable though.  Please see the Stripes documentation on
 * <a href="http://stripesframework.org/display/stripes/Configuration+Reference">Configuration</a>
 * if you would prefer the default behaviour to be to invoke validation methods when validation errors
 * have been generated by type conversion and annotation based validation.</p>
 *
 * @author Tim Fennell
 * @since Stripes 1.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ValidationMethod {

   /**
    * Allows the validation method to be restricted to one or more events. By default the
    * validation method will execute on all events not marked with {@literal @}DontValidate.
    * Can be used to specify one or more events to apply the method to (e.g. on={"save", "update"})
    * or to specify one or more events <i>not</i> to apply the method to
    * (e.g. on="!delete").
    */
   String[] on() default {};

   /**
    * If there are multiple validation methods in an ActionBean, what is the priority of
    * this one?  Lower values == higher priorities and will get run before methods with
    * higher values. If methods have the same priority they are executed in alphabetical
    * order.
    */
   int priority() default 0;

   /**
    * Controls whether or not the validation method will be executed when earlier phases of
    * validation generated validation errors. Valid values are {@link ValidationState#ALWAYS},
    * {@link ValidationState#NO_ERRORS} and {@link ValidationState#DEFAULT}. By specifying
    * {@code ALWAYS} you can ensure that all error messages are presented to the user at once.
    * By specifying {@code NO_ERRORS} you can be sure of the state of the ActionBean has been
    * validated successfully prior to execution.
    */
   ValidationState when() default ValidationState.DEFAULT;
}
