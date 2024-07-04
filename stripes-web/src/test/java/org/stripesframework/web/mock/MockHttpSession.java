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
import java.util.Random;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;


/**
 * Simple mock implementation of HttpSession that implements most basic operations.
 *
 * @author Tim Fennell
 * @since Stripes 1.1.1
 */
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

   private final long                _creationTime = System.currentTimeMillis();
   private final String              _sessionId    = String.valueOf(new Random().nextLong());
   private       ServletContext      _context;
   private final Map<String, Object> _attributes   = new HashMap<>();

   /** Default constructor which provides the session with access to the context. */
   public MockHttpSession( ServletContext context ) {
      _context = context;
   }

   /** Returns the value of the named attribute from an internal Map. */
   @Override
   public Object getAttribute( String key ) { return _attributes.get(key); }

   @Override
   public Object getValue( String s ) {
      return null;
   }

   /** Returns an enumeration of all the attribute names in the session. */
   @Override
   public Enumeration<String> getAttributeNames() {
      return Collections.enumeration(_attributes.keySet());
   }

   @Override
   public String[] getValueNames() {
      return new String[0];
   }

   /** Returns the time in milliseconds when the session was created. */
   @Override
   public long getCreationTime() { return _creationTime; }

   /** Returns an ID that was randomly generated when the session was created. */
   @Override
   public String getId() { return _sessionId; }

   /** Always returns the current time. */
   @Override
   public long getLastAccessedTime() { return System.currentTimeMillis(); }

   /** Always returns Integer.MAX_VALUE. */
   @Override
   public int getMaxInactiveInterval() { return Integer.MAX_VALUE; }

   @Override
   public HttpSessionContext getSessionContext() {
      return null;
   }

   /** Provides access to the servlet context within which the session exists. */
   @Override
   public ServletContext getServletContext() { return _context; }

   /** Clears the set of attributes, but has no other effect. */
   @Override
   public void invalidate() { _attributes.clear(); }

   /** Always returns false. */
   @Override
   public boolean isNew() { return false; }

   /** Removes any value stored in session with the key supplied. */
   @Override
   public void removeAttribute( String key ) {
      _attributes.remove(key);
   }

   @Override
   public void removeValue( String s ) {

   }

   /** Stores the value in session, replacing any existing value with the same key. */
   @Override
   public void setAttribute( String key, Object value ) {
      _attributes.put(key, value);
   }

   @Override
   public void putValue( String s, Object o ) {

   }

   /** Has no effect. */
   @Override
   public void setMaxInactiveInterval( int i ) { }

   /** Sets the servlet context within which the session exists. */
   public void setServletContext( ServletContext context ) { _context = context; }
}
