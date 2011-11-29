package bluetooth;

import org.w3c.dom.Document;

import net.rim.device.api.web.WidgetConfig;
import net.rim.device.api.web.WidgetExtension;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.script.ScriptEngine;


public final class BTExtensionInterface implements WidgetExtension {
        public String[] getFeatureList() {
                String[] list = new String[1];
                list[0] = "webworks.bluetooth";
                return list;
        }

        public void loadFeature(String feature, String version, Document doc,
                        ScriptEngine scriptEngine) throws Exception {
                if(feature.equals("webworks.bluetooth")) {
                        scriptEngine.addExtension("webworks.bluetooth",new BTExtension());
                }
        }

        public void register(WidgetConfig widgetConfig, BrowserField browserField) {
                // TODO Auto-generated method stub
        }

        public void unloadFeatures(Document doc) {
                // TODO Auto-generated method stub
        }
}
