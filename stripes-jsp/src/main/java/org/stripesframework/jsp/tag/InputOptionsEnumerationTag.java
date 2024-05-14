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

import java.util.Locale;

import jakarta.servlet.jsp.JspException;

import org.stripesframework.jsp.exception.StripesJspException;
import org.stripesframework.web.localization.LocalizationUtility;
import org.stripesframework.web.util.ReflectUtil;
import org.stripesframework.web.util.bean.BeanUtil;
import org.stripesframework.web.util.bean.ExpressionException;


/**
 * <p>Writes a set of {@literal <option value="foo">bar</option>} tags to the page based on the
 * values of a enum.  Each value in the enum is represented by a single option tag on the page. The
 * options will be generated in ordinal value order (i.e. the order they are declared in the
 * enum).</p>
 *
 * <p>Please note that as with the options-collection and options-map tags, resource bundle lookups
 * are on by default but can be turned off by setting the localizeLabels attribute to false.</p>
 *
 * <p>The label (the value the user sees) is generated in one of three ways: by looking up a
 * localized value (unless localizeLabels is set to false), by using the property named by the 'label'
 * tag attribute if it is supplied and lastly by toString()'ing the enumeration value.
 * For example the following tag:</p>
 *
 *<pre>{@literal <stripes:options-enumeration enum="net.kitty.EyeColor" label="description"/>}</pre>
 *
 * when generating the option for the value {@code EyeColor.BLUE} will look for a label in the
 * following order:</p>
 *
 * <ul>
 *   <li>resource: EyeColor.BLUE (skipped if localizeLabels is set to false)</li>
 *   <li>resource: net.kitty.EyeColor.BLUE (skipped if localizeLabels is set to false)</li>
 *   <li>property: EyeColor.BLUE.getDescription() (because of the label="description" above)</li>
 *   <li>failsafe: EyeColor.BLUE.toString()</li>
 * </ul>
 *
 * <p>If the class specified does not exist, or does not specify a Java 1.5 enum then a
 * JspException will be raised.</p>
 *
 * <p>All attributes of the tag, other than enum and label, are passed directly through to
 * the InputOptionTag which is used to generate the individual HTML options tags. As a
 * result the InputOptionsEnumerationTag will exhibit the same re-population/selection behaviour
 * as the regular options tag.</p>
 *
 * <p>Since the tag has no use for one it does not allow a body.</p>
 *
 * @author Tim Fennell
 */
public class InputOptionsEnumerationTag extends InputOptionsCollectionTag {

   private String _className;

   /**
    * Attempts to instantiate the Class object representing the enum and fetch the values of the
    * enum.  Then generates an option per value using an instance of an InputOptionTag.
    *
    * @return SKIP_BODY in all cases.
    * @throws JspException if the class name supplied is not a valid class, or cannot be cast
    *         to Class<Enum>.
    */
   @Override
   @SuppressWarnings("unchecked")
   public int doStartTag() throws JspException {
      Class<Enum> clazz = null;
      try {
         clazz = ReflectUtil.findClass(_className);
      }
      catch ( Exception e ) {
         // Try replacing the last period with a $ just in case the enum in question
         // is an inner class of another class
         try {
            int last = _className.lastIndexOf('.');
            if ( last > 0 ) {
               String n2 = new StringBuilder(_className).replace(last, last + 1, "$").toString();
               clazz = ReflectUtil.findClass(n2);
            }
         }
         // If our second attempt didn't work, wrap the *original* exception
         catch ( Exception e2 ) {
            throw new StripesJspException(
                  "Could not process class [" + _className + "]. Attribute 'enum' on " + "tag options-enumeration must be the fully qualified name of a "
                        + "class which is a java 1.5 enum.", e);
         }
      }

      if ( !clazz.isEnum() ) {
         throw new StripesJspException("The class name supplied, [" + _className + "], does not appear to be " + "a JDK1.5 enum class.");
      }

      Enum[] enums = clazz.getEnumConstants();

      try {
         Locale locale = getPageContext().getRequest().getLocale();
         boolean attemptToLocalizeLabels = isAttemptToLocalizeLabels();

         for ( Enum item : enums ) {
            Object label = null;
            String simpleName = LocalizationUtility.getSimpleName(clazz);

            if ( attemptToLocalizeLabels ) {
               // Check for a localized label using class.ENUM_VALUE and package.class.ENUM_VALUE
               label = LocalizationUtility.getLocalizedFieldName(simpleName + "." + item.name(), null, locale);
            }
            if ( label == null ) {
               if ( getLabel() != null ) {
                  label = BeanUtil.getPropertyValue(getLabel(), item);
               } else {
                  label = item.toString();
               }
            }

            Object group = null;
             if ( getGroup() != null ) {
                 group = BeanUtil.getPropertyValue(getGroup(), item);
             }

            addEntry(item, label, item, group);
         }
      }
      catch ( ExpressionException ee ) {
         throw new StripesJspException(
               "A problem occurred generating an options-enumeration. " + "Most likely either the class [" + getEnum() + "] is not an enum or, [" + getLabel()
                     + "] is not a valid property of the enum.", ee);
      }

      return SKIP_BODY;
   }

   /** Gets the enum class name set with setEnum(). */
   public String getEnum() {
      return _className;
   }

   /** Sets the fully qualified name of an enumeration class. */
   public void setEnum( String name ) {
       _className = name;
   }
}
