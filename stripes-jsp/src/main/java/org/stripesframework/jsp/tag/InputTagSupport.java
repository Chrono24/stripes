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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TryCatchFinally;

import org.stripesframework.jsp.config.JspConfiguration;
import org.stripesframework.jsp.exception.StripesJspException;
import org.stripesframework.web.action.ActionBean;
import org.stripesframework.web.controller.ParameterName;
import org.stripesframework.web.controller.StripesConstants;
import org.stripesframework.web.controller.StripesFilter;
import org.stripesframework.web.exception.StripesRuntimeException;
import org.stripesframework.web.format.Formatter;
import org.stripesframework.web.format.FormatterFactory;
import org.stripesframework.web.localization.LocalizationUtility;
import org.stripesframework.web.util.CryptoUtil;
import org.stripesframework.web.validation.BooleanTypeConverter;
import org.stripesframework.web.validation.ValidationError;
import org.stripesframework.web.validation.ValidationErrors;
import org.stripesframework.web.validation.ValidationMetadata;


/**
 * Parent class for all input tags in stripes.  Provides support methods for retrieving all the
 * attributes that are shared across form input tags.  Also provides accessors for finding the
 * specified &quot;override&quot; value and for finding the enclosing support tag.
 *
 * @author Tim Fennell
 */
public abstract class InputTagSupport extends HtmlTagSupport implements TryCatchFinally {

   private String  _formatType;
   private String  _formatPattern;
   private boolean _focus;
   private boolean _syntheticId;

   /** A list of the errors related to this input tag instance */
   protected List<ValidationError> fieldErrors;
   private   boolean               fieldErrorsLoaded = false; // used to track if fieldErrors is loaded yet

   /** The error renderer to be utilized for error output of this input tag */
   protected TagErrorRenderer errorRenderer;

   /** Rethrows the passed in throwable in all cases. */
   @Override
   public void doCatch( Throwable throwable ) throws Throwable { throw throwable; }

   /** Abstract method implemented in child classes instead of doEndTag(). */
   public abstract int doEndInputTag() throws JspException;

   /**
    * Final implementation of the doEndTag() method that allows the base InputTagSupport class
    * to insert functionality before and after the tag performs it's doEndTag equivalent
    * method.
    *
    * @return int the value returned by the child class from doStartInputTag()
    */
   @Override
   public final int doEndTag() throws JspException {
      // Wrap in a try/finally because a custom error renderer could throw an
      // exception, and some containers in their infinite wisdom continue to
      // cache/pool the tag even after a JSPException is thrown!
      try {
         int result = doEndInputTag();

         if ( getFieldErrors() != null ) {
            errorRenderer.doAfterEndTag();
         }

         if ( _focus ) {
            makeFocused();
         }

         return result;
      }
      finally {
         errorRenderer = null;
         fieldErrors = null;
         fieldErrorsLoaded = false;
         _focus = false;
      }
   }

   /**
    * Used to ensure that the input tag is always removed from the tag stack so that there is
    * never any confusion about tag-parent hierarchies.
    */
   @Override
   public void doFinally() {
      try {
         getTagStack().pop();
      }
      catch ( Throwable t ) {
         /* Suppress anything, because otherwise this might mask any causal exception. */
      }
   }

   /** Abstract method implemented in child classes instead of doStartTag(). */
   public abstract int doStartInputTag() throws JspException;

   /**
    * Final implementation of the doStartTag() method that allows the base InputTagSupport class
    * to insert functionality before and after the tag performs it's doStartTag equivalent
    * method. Finds errors related to this field and intercepts with a {@link TagErrorRenderer}
    * if appropriate.
    *
    * @return int the value returned by the child class from doStartInputTag()
    */
   @Override
   public final int doStartTag() throws JspException {
      getTagStack().push(this);
      registerWithParentForm();

      // Deal with any error rendering
      if ( getFieldErrors() != null ) {
         errorRenderer = JspConfiguration.getInstance().getTagErrorRendererFactory().getTagErrorRenderer(this);
         errorRenderer.doBeforeStartTag();
      }

      return doStartInputTag();
   }

   /**
    * Fetches the ActionBean associated with the form if one is present.  An ActionBean will not
    * be created (and hence not present) by default.  An ActionBean will only be present if the
    * current request got bound to the same ActionBean as the current form uses.  E.g. if we are
    * re-showing the page as the result of an error, or the same ActionBean is used for a
    * &quot;pre-Action&quot; and the &quot;post-action&quot;.
    *
    * @return ActionBean the ActionBean bound to the form if there is one
    */
   public ActionBean getActionBean() throws StripesJspException {
      return getParentFormTag().getActionBean();
   }

   public String getDisabled() { return get("disabled"); }

   public String getRequired() { return get("required"); }

   /**
    * Access for the field errors that occurred on the form input this tag represents
    * @return List<ValidationError> the list of validation errors for this field
    */
   public List<ValidationError> getFieldErrors() throws StripesJspException {
      if ( !fieldErrorsLoaded ) {
         loadErrors();
         fieldErrorsLoaded = true;
      }

      return fieldErrors;
   }

   /** Returns the value set with setFormatPattern() */
   public String getFormatPattern() { return _formatPattern; }

   /** Returns the value set with setFormatAs() */
   public String getFormatType() { return _formatType; }

   /**
    * Fetches the localized name for this field if one exists in the resource bundle. Relies on
    * there being a "name" attribute on the tag, and the pageContext being set on the tag. First
    * checks for a value of {actionBean FQN}.{fieldName} in the specified bundle, then
    * {actionPath}.{fieldName} then just "fieldName".
    *
    * @return a localized field name if one can be found, or null if one cannot be found.
    */
   public String getLocalizedFieldName() throws StripesJspException {
      String name = getAttributes().get("name");
      return getLocalizedFieldName(name);
   }

   public String getName() { return get("name"); }

   /**
    * <p>Locates the enclosing stripes form tag. If no form tag can be found, because the tag
    * was not enclosed in one on the JSP, an exception is thrown.</p>
    *
    * @return FormTag the enclosing form tag on the JSP
    * @throws StripesJspException if an enclosing form tag cannot be found
    */
   public FormTag getParentFormTag() throws StripesJspException {
      FormTag parent = getParentTag(FormTag.class);

      // find the first non-partial parent form tag
      if ( parent != null && parent.isPartial() ) {
         Stack<StripesTagSupport> stack = getTagStack();
         ListIterator<StripesTagSupport> iter = stack.listIterator(stack.size());
         while ( iter.hasPrevious() ) {
            StripesTagSupport tag = iter.previous();
            if ( tag instanceof FormTag && !((FormTag)tag).isPartial() ) {
               parent = (FormTag)tag;
               break;
            }
         }
      }

      if ( parent == null ) {
         throw new StripesJspException("InputTag of type [" + getClass().getName() + "] must be enclosed inside a "
               + "stripes form tag. If, for some reason, you do not wish to render a complete "
               + "form you may surround stripes input tags with <s:form partial=\"true\" ...> "
               + "which will provide support to the input tags but not render the <form> tag.");
      }

      return parent;
   }

   /** Gets the HTML attribute of the same name. */
   public String getReadonly() { return get("readonly"); }

   public String getSize() { return get("size"); }

   /**
    * Used during repopulation to query the tag for a value of values provided to the tag
    * on the JSP.  This allows the PopulationStrategy to encapsulate all decisions about
    * which source to use when repopulating tags.
    *
    * @return May return any of String[], Collection or Object
    */
   public Object getValueOnPage() {
      Object value = getBodyContentAsString();

      if ( value == null ) {
         try {
            Method getValue = getClass().getMethod("getValue");
            value = getValue.invoke(this);
         }
         catch ( Exception e ) {
            // Not a lot we can do about this.  It's either because the subclass in question
            // doesn't have a getValue() method (which is ok), or it threw an exception.
         }
      }

      return value;
   }

   /**
    * Returns true if one or more validation errors exist for the field represented by
    * this input tag.
    */
   public boolean hasErrors() throws StripesJspException {
      List<ValidationError> errors = getFieldErrors();
      return errors != null && errors.size() > 0;
   }

   /**
    * Checks to see if the value provided is either 'disabled' or a value that the
    * {@link BooleanTypeConverter} believes it true. If so, adds a disabled attribute
    * to the tag, otherwise does not.
    */
   public void setDisabled( String disabled ) {
      boolean isDisabled = "disabled".equalsIgnoreCase(disabled);
      if ( !isDisabled ) {
         BooleanTypeConverter converter = new BooleanTypeConverter();
         isDisabled = converter.convert(disabled, Boolean.class, null);
      }

      if ( isDisabled ) {
         set("disabled", "disabled");
      } else {
         getAttributes().remove("disabled");
      }
   }

   /**
    * Checks to see if the value provided is either 'required' or a value that the
    * {@link BooleanTypeConverter} believes it true. If so, adds a required attribute
    * to the tag, otherwise does not.
    */
   public void setRequired( String required ) {
      boolean isRequired = "required".equalsIgnoreCase(required);
      if ( !isRequired ) {
         BooleanTypeConverter converter = new BooleanTypeConverter();
         isRequired = converter.convert(required, Boolean.class, null);
      }

      if ( isRequired ) {
         set("required", "required");
      } else {
         getAttributes().remove("required");
      }
   }



   /**
    * Informs the tag that it should render JavaScript to ensure that it is focused
    * when the page is loaded. If the tag does not have an 'id' attribute a random
    * one will be created and set so that the tag can be located easily.
    *
    * @param focus true if focus is desired, false otherwise
    */
   public void setFocus( boolean focus ) {
      _focus = focus;

      if ( getId() == null ) {
         _syntheticId = true;
         setId("stripes-" + new Random().nextInt());
      }
   }

   /** Sets the named format pattern, or a custom format pattern. */
   public void setFormatPattern( String formatPattern ) { _formatPattern = formatPattern; }

   /** Sets the type of output to format, e.g. date or time. */
   public void setFormatType( String formatType ) { _formatType = formatType; }

   public void setName( String name ) { set("name", name); }

   /**
    * <p>Sets the value of the readonly attribute to "readonly" but only when the value passed
    * in is either "readonly" itself, or is converted to true by the
    * {@link BooleanTypeConverter}.</p>
    *
    * <p>Although not all input tags support the readonly attribute, the method is located here
    * because it is not a simple one-liner and is used by more than one tag.</p>
    */
   public void setReadonly( String readonly ) {
      boolean isReadOnly = "readonly".equalsIgnoreCase(readonly);
      if ( !isReadOnly ) {
         BooleanTypeConverter converter = new BooleanTypeConverter();
         isReadOnly = converter.convert(readonly, Boolean.class, null);
      }

      if ( isReadOnly ) {
         set("readonly", "readonly");
      } else {
         getAttributes().remove("readonly");
      }
   }

   public void setSize( String size ) { set("size", size); }

   /**
    * Calls {@link #format(Object, boolean)} with {@code forOutput} set to true.
    *
    * @param input The object to be formatted
    * @see #format(Object, boolean)
    */
   protected String format( Object input ) {
      return format(input, true);
   }

   /**
    * Attempts to format an object using the Stripes formatting system.  If no formatter can
    * be found, then a simple String.valueOf(input) will be returned.  If the value passed in
    * is null, then the empty string will be returned.
    *
    * @param input The object to be formatted
    * @param forOutput If true, then the object will be formatted for output to the JSP. Currently,
    *            that means that if encryption is enabled for the ActionBean property with the same
    *            name as this tag then the formatted value will be encrypted before it is returned.
    */
   @SuppressWarnings("unchecked")
   protected String format( Object input, boolean forOutput ) {
      if ( input == null ) {
         return "";
      }

      // format the value
      FormatterFactory factory = StripesFilter.getConfiguration().getFormatterFactory();
      Formatter formatter = factory.getFormatter(input.getClass(), getPageContext().getRequest().getLocale(), _formatType, _formatPattern);
      String formatted = (formatter == null) ? String.valueOf(input) : formatter.format(input);

      // encrypt the formatted value if required
      if ( forOutput && formatted != null ) {
         try {
            ValidationMetadata validate = getValidationMetadata();
            if ( validate != null && validate.encrypted() ) {
               formatted = CryptoUtil.encrypt(formatted);
            }
         }
         catch ( JspException e ) {
            throw new StripesRuntimeException(e);
         }
      }

      return formatted;
   }

   // Getters and setters only below this point.

   /**
    * Attempts to fetch a "field name" resource from the localization bundle.
    *
    * @param name the field name or resource to look up
    * @return the localized String corresponding to the name provided
    */
   protected String getLocalizedFieldName( final String name ) {
      Locale locale = getPageContext().getRequest().getLocale();
      FormTag form = null;

      try {
         form = getParentFormTag();
      }
      catch ( StripesJspException sje ) { /* Do nothing. */}

      Class<? extends ActionBean> beanClass = null;

      if ( form != null ) {
         beanClass = form.getActionBeanClass();
      } else {
         ActionBean mainBean = (ActionBean)getPageContext().getRequest().getAttribute(StripesConstants.REQ_ATTR_ACTION_BEAN);
         if ( mainBean != null ) {
            beanClass = mainBean.getClass();
         }
      }
      return LocalizationUtility.getLocalizedFieldName(name, beanClass, locale);
   }

   /**
    * Gets the value for this tag based on the current population strategy.  The value returned
    * could be a scalar value, or it could be an array or collection depending on what the
    * population strategy finds.  For example, if the user submitted multiple values for a
    * checkbox, the default population strategy would return a String[] containing all submitted
    * values.
    *
    * @return Object either a value/values for this tag or null
    * @throws StripesJspException if the enclosing form tag (which is required at all times, and
    *         necessary to perform repopulation) cannot be located
    */
   protected Object getOverrideValueOrValues() throws StripesJspException {
      return JspConfiguration.getInstance().getPopulationStrategy().getValue(this);
   }

   /**
    * Returns a single value for the the value of this field.  This can be used to ensure that
    * only a single value is returned by the population strategy, which is useful in the case
    * of text inputs etc. which can have only a single value.
    *
    * @return Object either a single value or null
    * @throws StripesJspException if the enclosing form tag (which is required at all times, and
    *         necessary to perform repopulation) cannot be located
    */
   protected Object getSingleOverrideValue() throws StripesJspException {
      Object unknown = getOverrideValueOrValues();
      Object returnValue = null;

      if ( unknown != null && unknown.getClass().isArray() ) {
         if ( Array.getLength(unknown) > 0 ) {
            returnValue = Array.get(unknown, 0);
         }
      } else if ( unknown != null && unknown instanceof Collection<?> ) {
         Collection<?> collection = (Collection<?>)unknown;
         if ( collection.size() > 0 ) {
            returnValue = collection.iterator().next();
         }
      } else {
         returnValue = unknown;
      }

      return returnValue;
   }

   protected ValidationMetadata getValidationMetadata() throws StripesJspException {
      // find the action bean class we're dealing with
      Class<? extends ActionBean> beanClass = getParentFormTag().getActionBeanClass();

      if ( beanClass != null ) {
         // ascend the tag stack until a tag name is found
         String name = getName();
         if ( name == null ) {
            InputTagSupport tag = getParentTag(InputTagSupport.class);
            while ( name == null && tag != null ) {
               name = tag.getName();
               tag = tag.getParentTag(InputTagSupport.class);
            }
         }

         // check validation for encryption flag
         return StripesFilter.getConfiguration().getValidationMetadataProvider().getValidationMetadata(beanClass, new ParameterName(name));
      } else {
         return null;
      }
   }

   /**
    * Utility method for determining if a String value is contained within an Object, where the
    * object may be either a String, String[], Object, Object[] or Collection.  Used primarily
    * by the InputCheckBoxTag and InputSelectTag to determine if specific check boxes or
    * options should be selected based on the values contained in the JSP, HttpServletRequest and
    * the ActionBean.
    *
    * @param value the value that we are searching for
    * @param selected a String, String[], Object, Object[] or Collection (of scalars) denoting the
    *        selected items
    * @return boolean true if the String can be found, false otherwise
    */
   protected boolean isItemSelected( Object value, Object selected ) {
      // Since this is a checkbox, there could be more than one checked value, which means
      // this could be a single value type, array or collection
      if ( selected != null ) {
         String stringValue = (value == null) ? "" : format(value, false);

         if ( selected.getClass().isArray() ) {
            int length = Array.getLength(selected);
            for ( int i = 0; i < length; ++i ) {
               Object item = Array.get(selected, i);
               if ( (format(item, false).equals(stringValue)) ) {
                  return true;
               }
            }
         } else if ( selected instanceof Collection<?> ) {
            Collection<?> selectedIf = (Collection<?>)selected;
            for ( Object item : selectedIf ) {
               if ( (format(item, false).equals(stringValue)) ) {
                  return true;
               }
            }
         } else {
            if ( format(selected, false).equals(stringValue) ) {
               return true;
            }
         }
      }

      // If we got this far without returning, then this is not a selected item
      return false;
   }

   /**
    * Find errors that are related to the form field this input tag represents and place
    * them in an instance variable to use during error rendering.
    */
   protected void loadErrors() throws StripesJspException {
      ActionBean actionBean = getActionBean();
      if ( actionBean != null ) {
         ValidationErrors validationErrors = actionBean.getContext().getValidationErrors();

         if ( validationErrors != null ) {
            fieldErrors = validationErrors.get(getName());
         }
      }
   }

   /** Writes out a JavaScript string to set focus on the field as it is rendered. */
   protected void makeFocused() throws JspException {
      try {
         JspWriter out = getPageContext().getOut();
         out.write("<script type=\"text/javascript\">setTimeout(function(){try{var z=document.getElementById('");
         out.write(getId());
         out.write("');z.focus();");
         if ( "text".equals(getAttributes().get("type")) || "password".equals(getAttributes().get("type")) ) {
            out.write("z.select();");
         }
         out.write("}catch(e){}},1);</script>");

         // Clean up tag state involved with focus
         _focus = false;
         if ( _syntheticId ) {
            getAttributes().remove("id");
         }
         _syntheticId = false;
      }
      catch ( IOException ioe ) {
         throw new StripesJspException("Could not write javascript focus code to jsp writer.", ioe);
      }
   }

   /**
    * Registers the field with the parent form within which it must be enclosed.
    * @throws StripesJspException if the parent form tag is not found
    */
   protected void registerWithParentForm() throws StripesJspException {

      // Disabled form fields should not be part of the __fp form parameter.
      // Disabled form fields are not sent with the request but should not set anything to null either.
      if ( getDisabled() == null ) {
         getParentFormTag().registerField(this);
      }
   }

}
