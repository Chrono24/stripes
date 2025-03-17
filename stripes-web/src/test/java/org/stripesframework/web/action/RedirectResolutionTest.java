package org.stripesframework.web.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.stripesframework.web.FilterEnabledTestBase;
import org.stripesframework.web.mock.MockHttpServletRequest;
import org.stripesframework.web.mock.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;


public class RedirectResolutionTest extends FilterEnabledTestBase {

   @Test
   public void testPermanentRedirect() throws Exception {
      RedirectResolution resolution = new RedirectResolution("https://www.stripesframework.org", false).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_PERMANENTLY);
      assertThat(response.getHeader("Location")).isEqualTo("https://www.stripesframework.org");
   }

   @Test
   public void testPermanentRedirectWithParameters() throws Exception {
      RedirectResolution resolution = new RedirectResolution("https://www.stripesframework.org", false).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY)
            .addParameter("test", "test");
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_PERMANENTLY);
      assertThat(response.getHeader("Location")).isEqualTo("https://www.stripesframework.org?test=test");
   }

   @Test
   public void testTemporaryRedirect() throws Exception {
      RedirectResolution resolution = new RedirectResolution("https://www.stripesframework.org", false);
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_TEMPORARILY);
      assertThat(response.getRedirectUrl()).isEqualTo("https://www.stripesframework.org");
   }

   @Test
   public void testTemporaryRedirectWithParameters() throws Exception {
      RedirectResolution resolution = new RedirectResolution("https://www.stripesframework.org", false).addParameter("test", "test");
      MockHttpServletResponse response = new MockHttpServletResponse();
      resolution.execute(buildMockServletRequest(), response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_MOVED_TEMPORARILY);
      assertThat(response.getRedirectUrl()).isEqualTo("https://www.stripesframework.org?test=test");
   }

   //helper method
   private MockHttpServletRequest buildMockServletRequest() {
      MockHttpServletRequest request = new MockHttpServletRequest("/context", "/whatever");
      request.addLocale(Locale.US);
      return request;
   }

}
