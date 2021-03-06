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
package org.stripesframework.web.util.bean;

/**
 * Exception that is raised when a parsed expression cannot be evaluated against a bean
 * for any reason.  In the special case where no such property exists a specific subclass
 * of this exception is thrown.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class EvaluationException extends ExpressionException {

   private static final long serialVersionUID = 1L;

   /** Constructs an exception with the supplied message. */
   public EvaluationException( String message ) {
      super(message);
   }

   /** Constructs an exception with the supplied message and causing exception. */
   public EvaluationException( String message, Throwable cause ) {
      super(message, cause);
   }
}
