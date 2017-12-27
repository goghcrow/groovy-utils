//package com.youzan.et.groovy.shell;
//
//import groovy.util.ResourceConnector;
//import groovy.util.ResourceException;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//public class URLConnector implements ResourceConnector {
//    private URL root;
//    private final Map<String, Long> lastModCache = new ConcurrentHashMap<>();
//
//    public URLConnector(URL root) {
//        this.root = root;
//    }
//
//    public class RefreshURLConnection extends URLConnection {
//        RefreshURLConnection(URL url) {
//            super(url);
//        }
//
//        @Override
//        public void connect() throws IOException {
//            url.openConnection();
//        }
//
//        @Override
//        public InputStream getInputStream() throws IOException {
//            return url.openStream();
//        }
//
//        @Override
//        public long getLastModified() {
//            String path = url.toExternalForm();
//            Long lastMod = lastModCache.get(path);
//            if (lastMod == null) {
//                long now = System.currentTimeMillis();
//                lastModCache.put(path, now);
//                return now;
//            } else {
//                return lastMod;
//            }
//        }
//    }
//
//    @Override
//    public URLConnection getResourceConnection(String name) throws ResourceException {
//        try {
//            return new RefreshURLConnection(new URL(root, name));
//        } catch (MalformedURLException e) {
//            String message = "Malformed URL: " + name;
//            throw new ResourceException(message);
//        }
//    }
//
//    public void cacheClear(String name) {
//        try {
//            String path = new URL(root, name).toExternalForm();
//            lastModCache.put(path, System.currentTimeMillis());
//        } catch (MalformedURLException e) {
//            log.error("cacheClare fail with " + name, e);
//        }
//    }
//
//    public void cacheClear() {
//        lastModCache.clear();
//    }
//}