/* Copyright 2008 Aaron Porter
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Resolution for sending HTTP error messages back to the client. errorCode is the HTTP status code
 * to be sent. errorMessage is a descriptive message.
 *
 * @author Aaron Porter
 * @since Stripes 1.5
 */
public class ErrorResolution implements Resolution {

   private int    _errorCode;
   private String _errorMessage;

   /**
    * Sends an error response to the client using the specified status code and clears the buffer.
    *
    * @param errorCode the HTTP status code
    */
   public ErrorResolution( int errorCode ) {
       _errorCode = errorCode;
   }

   /**
    * Sends an error response to the client using the specified status code and message and clears
    * the buffer.
    *
    * @param errorCode the HTTP status code
    * @param errorMessage a descriptive message
    */
   public ErrorResolution( int errorCode, String errorMessage ) {
       _errorCode = errorCode;
       _errorMessage = errorMessage;
   }

   @Override
   public void execute( HttpServletRequest request, HttpServletResponse response ) throws Exception {
      if ( _errorMessage != null ) {
         response.sendError(_errorCode, _errorMessage);
      } else {
         response.sendError(_errorCode);
      }
   }

   /** Accessor for the HTTP status code. */
   public int getErrorCode() {
      return _errorCode;
   }

   /** Accessor for the descriptive error message. */
   public String getErrorMessage() {
      return _errorMessage;
   }

   /** Setter for the HTTP status code. */
   public void setErrorCode( int errorCode ) {
       _errorCode = errorCode;
   }

   /** Setter for the descriptive error message. */
   public void setErrorMessage( String errorMessage ) {
       _errorMessage = errorMessage;
   }
}