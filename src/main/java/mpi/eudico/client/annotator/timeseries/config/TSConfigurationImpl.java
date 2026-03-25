package mpi.eudico.client.annotator.timeseries.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Default implementation of TSConfiguration.
 *
 * @author Han Sloetjes
 */
public class TSConfigurationImpl implements TSConfiguration {
    /** the properties */
    protected Properties properties;

    /** a map for configuration or track objects */
    protected Map<Object, Object> objectMap;

    /**
     * Creates a new TSConfigurationImpl instance.
     */
    public TSConfigurationImpl() {
        properties = new Properties();
        objectMap = new HashMap<Object, Object>();
    }

    @Override
	public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
	public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
	public Object removeProperty(String key) {
        return properties.remove(key);
    }

    @Override
	public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    @Override
	public void putObject(Object key, Object value) {
        objectMap.put(key, value);
    }

    @Override
	public Object getObject(Object key) {
        return objectMap.get(key);
    }

    @Override
	public Object removeObject(Object key) {
        return objectMap.remove(key);
    }

    @Override
	public Set<Object> objectKeySet() {
        return objectMap.keySet();
    }
}
