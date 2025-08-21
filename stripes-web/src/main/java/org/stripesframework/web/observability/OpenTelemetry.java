package org.stripesframework.web.observability;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;


public class OpenTelemetry {

   private static final Meter meter = GlobalOpenTelemetry.getMeter("org.stripesframework");

   public static Meter getMeter() {
      return meter;
   }
}
