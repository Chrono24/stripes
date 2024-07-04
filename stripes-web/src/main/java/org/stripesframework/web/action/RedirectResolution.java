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
package org.stripesframework.web.action;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.stripesframework.web.controller.FlashScope;
import org.stripesframework.web.controller.StripesConstants;
import org.stripesframework.web.util.Log;


/**
 * <p>Resolution that uses the Servlet API to <em>redirect</em> the user to another path by issuing
 * a client side redirect. Unlike the ForwardResolution the RedirectResolution can send the user to
 * any URL anywhere on the web - though it is more commonly used to send the user to a location
 * within the same application.<p>
 *
 * <p>By default the RedirectResolution will prepend the context path of the web application to
 * any URL before redirecting the request. To prevent the context path from being prepended
 * use the constructor: {@code RedirectResolution(String,boolean)}.</p>
 *
 * <p>It is also possible to append parameters to the URL to which the user will be redirected.
 * This can be done by manually adding parameters with the addParameter() and addParameters()
 * methods, and by invoking includeRequestParameters() which will cause all of the current
 * request parameters to be included into the URL.</p>
 *
 * <p>
 * The redirect type can be switched from a 302 temporary redirect (default) to a 301 permanent
 * redirect using the setPermanent method.
 * </p>
 *
 * @see ForwardResolution
 * @author Tim Fennell
 */
public class RedirectResolution extends OnwardResolution<RedirectResolution> {

   private static final Log log = Log.getInstance(RedirectResolution.class);

   private boolean                _prependContext = true;
   private boolean                _includeRequestParameters;
   private Collection<ActionBean> _beans; // used to flash action beans
   private boolean                _permanent      = false;

   /**
    * Simple constructor that takes the URL to which to forward the user. Defaults to
    * prepending the context path to the url supplied before redirecting.
    *
    * @param url the URL to which the user's browser should be re-directed.
    */
   public RedirectResolution( String url ) {
      this(url, true);
   }

   /**
    * Constructor that allows explicit control over whether or not the context path is
    * prepended to the URL before redirecting.
    *
    * @param url the URL to which the user's browser should be re-directed.
    * @param prependContext true if the context should be prepended, false otherwise
    */
   public RedirectResolution( String url, boolean prependContext ) {
      super(url);
      _prependContext = prependContext;
   }

   /**
    * Constructs a RedirectResolution that will redirect to the URL appropriate for
    * the ActionBean supplied.  This constructor should be preferred when redirecting
    * to an ActionBean as it will ensure the correct URL is always used.
    *
    * @param beanType the Class object representing the ActionBean to redirect to
    */
   public RedirectResolution( Class<? extends ActionBean> beanType ) {
      super(beanType);
   }

   /**
    * Constructs a RedirectResolution that will redirect to the URL appropriate for
    * the ActionBean supplied.  This constructor should be preferred when redirecting
    * to an ActionBean as it will ensure the correct URL is always used.
    *
    * @param beanType the Class object representing the ActionBean to redirect to
    * @param event the event that should be triggered on the redirect
    */
   public RedirectResolution( Class<? extends ActionBean> beanType, String event ) {
      super(beanType, event);
   }

   /**
    * Attempts to redirect the user to the specified URL.
    *
    * @throws ServletException thrown when the Servlet container encounters an error
    * @throws IOException thrown when the Servlet container encounters an error
    */
   @Override
   @SuppressWarnings("unchecked")
   public void execute( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

      if ( _permanent ) {
         response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
         response = new HttpServletResponseWrapper(response) {

            @Override
            public void sendRedirect( String location ) {
               setHeader("Location", location);
            }

            @Override
            public void setStatus( int sc ) {
            }

         };
      }
      if ( _includeRequestParameters ) {
         addParameters(request.getParameterMap());
      }

      // Add any beans to the flash scope
      if ( _beans != null ) {
         FlashScope flash = FlashScope.getCurrent(request, true);
         for ( ActionBean bean : _beans ) {
            flash.put(bean);
         }
      }

      // If a flash scope exists, add the parameter to the request
      FlashScope flash = FlashScope.getCurrent(request, false);
      if ( flash != null ) {
         addParameter(StripesConstants.URL_KEY_FLASH_SCOPE_ID, flash.key());
      }

      // Prepend the context path if requested
      String url = getUrl(request.getLocale());
      if ( _prependContext ) {
         String contextPath = request.getContextPath();
         if ( contextPath.length() > 1 ) {
            url = contextPath + url;
         }
      }

      url = response.encodeRedirectURL(url);
      log.trace("Redirecting ", _beans == null ? "" : "(w/flashed bean) ", "to URL: ", url);

      response.sendRedirect(url);
   }

   /**
    * Causes the ActionBean supplied to be added to the Flash scope and made available
    * during the next request cycle.
    *
    * @param bean the ActionBean to be added to flash scope
    * @since Stripes 1.2
    */
   public RedirectResolution flash( ActionBean bean ) {
      if ( _beans == null ) {
         _beans = new HashSet<>();
      }

      _beans.add(bean);
      return this;
   }

   /** This method is overridden to make it public. */
   @Override
   public String getAnchor() {
      return super.getAnchor();
   }

   /**
    * If set to true, will cause absolutely all request parameters present in the current request
    * to be appended to the redirect URL that will be sent to the browser. Since some browsers
    * and servers cannot handle extremely long URLs, care should be taken when using this
    * method with large form posts.
    *
    * @param inc whether or not current request parameters should be included in the redirect
    * @return RedirectResolution, this resolution so that methods can be chained
    */
   public RedirectResolution includeRequestParameters( boolean inc ) {
      _includeRequestParameters = inc;
      return this;
   }

   /** This method is overridden to make it public. */
   @Override
   public RedirectResolution setAnchor( String anchor ) {
      return super.setAnchor(anchor);
   }

   /** Sets the redirect type to permanent (301) instead of temporary (302). */
   public RedirectResolution setPermanent( boolean permanent ) {
      _permanent = permanent;
      return this;
   }
}
