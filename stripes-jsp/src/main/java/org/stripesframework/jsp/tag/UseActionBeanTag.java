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
package org.stripesframework.jsp.tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;

import org.stripesframework.jsp.exception.StripesJspException;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.action.ActionBeanContext;
import org.stripesframework.web.action.Resolution;
import org.stripesframework.web.config.Configuration;
import org.stripesframework.web.controller.ActionResolver;
import org.stripesframework.web.controller.DispatcherHelper;
import org.stripesframework.web.controller.DispatcherServlet;
import org.stripesframework.web.controller.ExecutionContext;
import org.stripesframework.web.controller.Interceptor;
import org.stripesframework.web.controller.LifecycleStage;
import org.stripesframework.web.controller.StripesFilter;


/**
 * <p>This tag supports the use of Stripes ActionBean classes as view helpers.
 * It allows for the use of actions as the controller and then their reuse
 * on the page, creating it if it does not exist. A typical usage pattern would
 * be for a page that contains two types of information, the interaction with each being
 * handled by separate ActionBean implementation. Some page events route to the first
 * action and others to the second, but the page still requires data from both in
 * order to render. This tag would define both ActionBeans in the page scope, creating
 * the one that wasn't executing the event.</p>
 *
 * <p>This class will bind parameters to a created ActionBean just as the execution of
 * an event on an ActionBean would. It does not rebind values to ActionBeans that
 * were previously created for execution of the action. Validation is not done
 * during this binding, except the type conversion required for binding, and no
 * validation errors are produced.</p>
 *
 * <p>The binding of the ActionBean to the page scope happens whether the ActionBean
 * is created or not, making for a consistent variable to always use when referencing
 * the ActionBean.</p>
 *
 * @author Greg Hinkle, Tim Fennell
 */
public class UseActionBeanTag extends StripesTagSupport {

   /** The UrlBinding of the ActionBean to create */
   private String _binding;

   /** The event, if any, to execute when creating */
   private String _event;

   /** A page scope variable to which to bind the ActionBean */
   private String _var;

   /** Indicates that validation should be executed. */
   private boolean _validate = false;

   /** Indicates whether the event should be executed even if the bean was already present. */
   private boolean _alwaysExecuteEvent = false;

   /** Indicates whether the resolution should be executed - false by default. */
   private boolean _executeResolution = false;

   /**
    * Does nothing.
    * @return EVAL_PAGE in all cases.
    */
   @Override
   public int doEndTag() { return EVAL_PAGE; }

   /**
    * The main work method of the tag. Looks up the action bean, instantiates it,
    * runs binding and then runs either the named event or the default.
    *
    * @return SKIP_BODY in all cases.
    * @throws JspException if the ActionBean could not be instantiate and executed
    */
   @Override
   public int doStartTag() throws JspException {
      // Check to see if the action bean already exists
      ActionBean actionBean = (ActionBean)getPageContext().findAttribute(_binding);
      boolean beanNotPresent = actionBean == null;

      try {
         final Configuration config = StripesFilter.getConfiguration();
         final ActionResolver resolver = StripesFilter.getConfiguration().getActionResolver();
         final HttpServletRequest request = (HttpServletRequest)getPageContext().getRequest();
         final HttpServletResponse response = (HttpServletResponse)getPageContext().getResponse();
         Resolution resolution = null;
         ExecutionContext ctx = new ExecutionContext();

         // Lookup the ActionBean if we don't already have it
         if ( beanNotPresent ) {
            ActionBeanContext tempContext = config.getActionBeanContextFactory().getContextInstance(request, response);
            tempContext.setServletContext(getPageContext().getServletContext());
            ctx.setLifecycleStage(LifecycleStage.ActionBeanResolution);
            ctx.setActionBeanContext(tempContext);

            // Run action bean resolution
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.ActionBeanResolution));
            resolution = ctx.wrap(new Interceptor() {

               @Override
               public Resolution intercept( ExecutionContext ec ) throws Exception {
                  ActionBean bean = resolver.getActionBean(ec.getActionBeanContext(), _binding);
                  ec.setActionBean(bean);
                  return null;
               }
            });
         } else {
            ctx.setActionBean(actionBean);
            ctx.setActionBeanContext(actionBean.getContext());
         }

         // Then, if and only if an event was specified, run handler resolution
         if ( resolution == null && _event != null && (beanNotPresent || _alwaysExecuteEvent) ) {
            ctx.setLifecycleStage(LifecycleStage.HandlerResolution);
            ctx.setInterceptors(config.getInterceptors(LifecycleStage.HandlerResolution));
            resolution = ctx.wrap(new Interceptor() {

               @Override
               public Resolution intercept( ExecutionContext ec ) throws Exception {
                  ec.setHandler(resolver.getHandler(ec.getActionBean().getClass(), _event));
                  ec.getActionBeanContext().setEventName(_event);
                  return null;
               }
            });
         }

         // Bind applicable request parameters to the ActionBean
         if ( resolution == null && (beanNotPresent || _validate) ) {
            resolution = DispatcherHelper.doBindingAndValidation(ctx, _validate);
         }

         // Run custom validations if we're validating
         if ( resolution == null && _validate ) {
            String temp = config.getBootstrapPropertyResolver().getProperty(DispatcherServlet.RUN_CUSTOM_VALIDATION_WHEN_ERRORS);
            boolean validateWhenErrors = temp != null && Boolean.valueOf(temp);

            resolution = DispatcherHelper.doCustomValidation(ctx, validateWhenErrors);
         }

         // Fill in any validation errors if they exist
         if ( resolution == null && _validate ) {
            resolution = DispatcherHelper.handleValidationErrors(ctx);
         }

         // And (again) if an event was supplied, then run the handler
         if ( resolution == null && _event != null && (beanNotPresent || _alwaysExecuteEvent) ) {
            resolution = DispatcherHelper.invokeEventHandler(ctx);
         }

         DispatcherHelper.fillInValidationErrors(ctx);  // just in case!

         if ( resolution != null && _executeResolution ) {
            DispatcherHelper.executeResolution(ctx, resolution);
         }

         // If a name was specified, bind the ActionBean into page context
         if ( getVar() != null ) {
            _pageContext.setAttribute(getVar(), ctx.getActionBean());
         }

         return SKIP_BODY;
      }
      catch ( Exception e ) {
         throw new StripesJspException("Unabled to prepare ActionBean for JSP Usage", e);
      }
   }

   /** Get the UrlBinding of the requested ActionBean */
   public String getBinding() { return _binding; }

   /** The event name, if any to execute. */
   public String getEvent() { return _event; }

   /** Alias for getVar() so that the JSTL and jsp:useBean style are allowed. */
   public String getId() { return getVar(); }

   /** Gets the name of the page scope variable to which the ActionBean will be bound. */
   public String getVar() { return _var; }

   public boolean isAlwaysExecuteEvent() { return _alwaysExecuteEvent; }

   public boolean isExecuteResolution() { return _executeResolution; }

   public boolean isValidate() { return _validate; }

   public void setAlwaysExecuteEvent( boolean alwaysExecuteEvent ) {
      _alwaysExecuteEvent = alwaysExecuteEvent;
   }

   /**
    * Sets the binding attribute by figuring out what ActionBean class is identified
    * and then in turn finding out the appropriate URL for the ActionBean.
    *
    * @param beanclass the FQN of an ActionBean class, or a Class object for one.
    */
   public void setBeanclass( Object beanclass ) throws StripesJspException {
      String url = getActionBeanUrl(beanclass);
      if ( url == null ) {
         throw new StripesJspException(
               "The 'beanclass' attribute provided could not be " + "used to identify a valid and configured ActionBean. The value supplied was: " + beanclass);
      } else {
         _binding = url;
      }
   }

   /** Set the UrlBinding of the requested ActionBean */
   public void setBinding( String binding ) { _binding = binding; }

   /** The event name, if any to execute. */
   public void setEvent( String event ) { _event = event; }

   public void setExecuteResolution( boolean executeResolution ) {
      _executeResolution = executeResolution;
   }

   /** Alias for setVar() so that the JSTL and jsp:useBean style are allowed. */
   public void setId( String id ) { setVar(id); }

   public void setValidate( boolean validate ) { _validate = validate; }

   /** Sets the name of the page scope variable to which the ActionBean will be bound. */
   public void setVar( String var ) { _var = var; }
}
