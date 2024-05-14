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
package org.stripesframework.web.controller.multipart;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.FileRenamePolicy;

import org.stripesframework.web.action.FileBean;
import org.stripesframework.web.controller.FileUploadLimitExceededException;

import org.stripesframework.web.exception.StripesRuntimeException;


/**
 * Implementation of MultipartWrapper that uses Jason Hunter's COS (com.oreilly.servlet)
 * multipart parser implementation. This is the default implementation in Stripes and is
 * generally preferred as it is a) free for use and b) has no other dependencies! However,
 * commercial redistribution of the COS library requires licensing from Jason Hunter, so
 * this implementation may not be applicable for commercial products that are distributed/sold
 * (though it is fine for commercial applications that are simply developed and hosted by the
 * company developing them).
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class CosMultipartWrapper implements MultipartWrapper {

   /** Pattern used to parse useful info out of the IOException cos throws. */
   private static final Pattern EXCEPTION_PATTERN = Pattern.compile("Posted content length of (\\d*) exceeds limit of (\\d*)");

   /* Ensure this class will not load unless COS is on the classpath. */
   static {
      //noinspection ResultOfMethodCallIgnored
      MultipartRequest.class.getName();
   }

   private MultipartRequest multipart;
   private String           charset;

   /**
    * Pseudo-constructor that allows the class to perform any initialization necessary.
    *
    * @param request an HttpServletRequest that has a content-type of multipart.
    * @param tempDir a File representing the temporary directory that can be used to store
    *        file parts as they are uploaded if this is desirable
    * @param maxPostSize the size in bytes beyond which the request should not be read, and a
    *                    FileUploadLimitExceeded exception should be thrown
    * @throws IOException if a problem occurs processing the request of storing temporary
    *                     files
    * @throws FileUploadLimitExceededException if the POST content is longer than the
    *         maxPostSize supplied.
    */
   @Override
   public void build( HttpServletRequest request, final File tempDir, long maxPostSize ) throws IOException, FileUploadLimitExceededException {

      try {
         // Create a new file in the temp directory in case of file name conflict
         FileRenamePolicy renamePolicy = unused -> {
            try {
               return File.createTempFile("cos", "", tempDir);
            }
            catch ( IOException e ) {
               throw new StripesRuntimeException("Caught an exception while trying to rename an uploaded file", e);
            }
         };

          charset = request.getCharacterEncoding();
          multipart = new MultipartRequest(request, tempDir.getAbsolutePath(), (int)maxPostSize, charset, renamePolicy);
      }
      catch ( IOException ioe ) {
         Matcher matcher = EXCEPTION_PATTERN.matcher(ioe.getMessage());

         if ( matcher.matches() ) {
            throw new FileUploadLimitExceededException(Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(1)));
         } else {
            throw ioe;
         }
      }
   }

   /**
    * Fetches the names of all file parameters in the request. Note that these are not the file
    * names, but the names given to the form fields in which the files are specified.
    *
    * @return the names of all file parameters in the request.
    */
   @Override
   @SuppressWarnings("unchecked")
   public Enumeration<String> getFileParameterNames() {
      return multipart.getFileNames();
   }

   /**
    * Responsible for constructing a FileBean object for the named file parameter. If there is no
    * file parameter with the specified name this method should return null.
    *
    * @param name the name of the file parameter
    * @return a FileBean object wrapping the uploaded file
    */
   @Override
   public FileBean getFileParameterValue( String name ) {
      File file = multipart.getFile(name);
      if ( file != null ) {
         return new FileBean(file, multipart.getContentType(name), multipart.getOriginalFileName(name), charset);
      } else {
         return null;
      }
   }

   /**
    * Fetches the names of all non-file parameters in the request. Directly analogous to the method
    * of the same name in HttpServletRequest when the request is non-multipart.
    *
    * @return an Enumeration of all non-file parameter names in the request
    */
   @Override
   @SuppressWarnings("unchecked")
   public Enumeration<String> getParameterNames() {
      return multipart.getParameterNames();
   }

   /**
    * Fetches all values of a specific parameter in the request. To simulate the HTTP request
    * style, the array should be null for non-present parameters, and values in the array should
    * never be null - the empty String should be used when there is value.
    *
    * @param name the name of the request parameter
    * @return an array of non-null parameters or null
    */
   @Override
   public String[] getParameterValues( String name ) {
      String[] values = multipart.getParameterValues(name);
      if ( values != null ) {
         for ( int i = 0; i < values.length; ++i ) {
            if ( values[i] == null ) {
               values[i] = "";
            }
         }
      }

      return values;
   }
}
