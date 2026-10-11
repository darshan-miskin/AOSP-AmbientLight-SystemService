package android.app;

import android.os.IAmbientLightService;
import android.os.AmbientLightManager;

public final class SystemServiceRegistry {

    /*
      Other SystemServiceRegistry variables, components and functions
    */

    @RavenwoodRedirect
    private static void registerServices() {
        
        //============ Add the following code ==================
        registerService(
            Context.AMBIENT_LIGHT_SERVICE,
            AmbientLightManager.class,
            new CachedServiceFetcher<AmbientLightManager>() {
                @Override
                public AmbientLightManager createService(ContextImpl ctx) {
                    IBinder b = ServiceManager.getService(Context.AMBIENT_LIGHT_SERVICE);
                    IAmbientLightService service = IAmbientLightService.Stub.asInterface(b);
                    return new AmbientLightManager(service);
                }
            }
        );
        
        //============ Add the above code ==================
    }
    
    
    /*
      Other SystemServiceRegistry variables, components and functions
    */

}
