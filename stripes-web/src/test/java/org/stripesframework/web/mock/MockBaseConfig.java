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
package org.stripesframework.web.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;


/**
 * Common parent class for both MockServletConfig and MockFilterConfig since they are both
 * essentially the same with a couple of method names changed.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
public class MockBaseConfig {

   private ServletContext _servletContext;

   private final Map<String, String> _initParameters = new HashMap<>();

   /** Adds all the values in the provided Map to the set of init parameters. */
   public void addAllInitParameters( Map<String, String> parameters ) {
      _initParameters.putAll(parameters);
   }

   /** Adds a value to the set of init parameters. */
   public void addInitParameter( String name, String value ) {
      _initParameters.put(name, value);
   }

   /** Gets the named init parameter if it exists, or null if it doesn't. */
   public String getInitParameter( String name ) {
      return _initParameters.get(name);
   }

   /** Gets an enumeration of all the init parameter names present. */
   public Enumeration<String> getInitParameterNames() {
      return Collections.enumeration(_initParameters.keySet());
   }

   /** Gets the ServletContext in whiich the filter is running. */
   public ServletContext getServletContext() { return _servletContext; }

   /** Sets the ServletContext that will be returned by getServletContext(). */
   public void setServletContext( ServletContext ctx ) { _servletContext = ctx; }
}
