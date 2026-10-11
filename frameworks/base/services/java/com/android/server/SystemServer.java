package com.android.server;

import com.android.server.AmbientLightService;

public final class SystemServer implements Dumpable {
  /*
    System Server other components, variables and functions
  */
  
  private void startOtherServices(@NonNull TimingsTraceAndSlog t) {
      /*
        other services
      */
      
      //========== Add the following code ================
      t.traceBegin("StartAmbientLightService");
      try {
          Slog.i(TAG, "Starting AmbientLightService (Kotlin)");
          AmbientLightService ambientLightService = new AmbientLightService(context);
          ServiceManager.addService(Context.AMBIENT_LIGHT_SERVICE, ambientLightService);
      } catch (Throwable e) {
          Slog.e(TAG, "Failure starting AmbientLightService", e);
      }
      t.traceEnd();
      //========== Add the above code ================
      
       /*
        other services
      */
  }
  
  /*
    System Server other components, variables and functions
  */

}
